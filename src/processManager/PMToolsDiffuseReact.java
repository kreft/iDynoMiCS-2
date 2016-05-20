package processManager;

import static grid.SpatialGrid.ArrayType.CONCN;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import agent.Agent;
import dataIO.XmlLabel;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import reaction.Reaction;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.SubvoxelPoint;
import solver.PDEupdater;
import surface.Collision;
import surface.Surface;

/**
 * \brief Tool set for {@code ProcessManager}s that control diffusion-reaction
 * like processes in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, UK
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public final class PMToolsDiffuseReact
{
	/**
	 * Useful filter for removing non-reactive agents from a list.
	 */
	private static final Predicate<Agent> NO_REAC_FILTER = 
							(a -> ! a.isAspect(NameRef.agentReactions));
	/**
	 * When choosing an appropriate sub-voxel resolution for building agents'
	 * {@code coordinateMap}s, the smallest agent radius is multiplied by this
	 * factor to ensure it is fine enough.
	 */
	// NOTE the value of a quarter is chosen arbitrarily
	private static double SUBGRID_FACTOR = 0.25;
	/**
	 * Aspect name for the {@code coordinateMap} used for establishing which
	 * voxels a located {@code Agent} covers.
	 */
	// TODO Consider moving this to NameRef or XmlLabel?
	private static final String VD_TAG = "volumeDistribution";
	// TODO Consider moving this to NameRef or XmlLabel?
	private static final String IP_TAG = "internalProduction";
	// TODO Consider moving this to NameRef or XmlLabel?
	private static final String GR_TAG = "growthRate";
	
	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 */
	@SuppressWarnings("unchecked")
	public static void setupAgentDistributionMaps(
					EnvironmentContainer environment, AgentContainer agents)
	{
		/*
		 * Reset the agent biomass distribution maps.
		 */
		CoordinateMap distributionMap;
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			distributionMap = new CoordinateMap();
			a.set(VD_TAG, distributionMap);
		}
		/*
		 * Set up the solute grids and the agents before we start to solve.
		 */
		
		Shape shape = environment.getShape();
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		int nDim = agents.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		List<Agent> neighbors;
		List<SubvoxelPoint> sgPoints;
		double[] pLoc;
		Collision collision = new Collision(null, agents.getShape());
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			/* Find all agents that overlap with this voxel. */
			location = shape.getVoxelOrigin(coord);
			shape.getVoxelSideLengthsTo(dimension, coord);
			/* NOTE the agent tree is always the amount of actual dimension */
			neighbors = agents.treeSearch(Vector.subset(location, nDim),
											Vector.subset(dimension, nDim));
			/* Filter the agents for those with reactions. */
			neighbors.removeIf(NO_REAC_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( neighbors.isEmpty() )
				continue;
			/* 
			 * Find the sub-grid resolution from the smallest agent, and
			 * get the list of sub-grid points.
			 */
			double minRad = Vector.min(dimension);
			for ( Agent a : neighbors )
				if ( a.isAspect(NameRef.bodyRadius) )
					minRad = Math.min(a.getDouble(NameRef.bodyRadius), minRad);
			sgPoints = shape.getCurrentSubvoxelPoints(SUBGRID_FACTOR * minRad);
			/* 
			 * Get the subgrid points and query the agents.
			 */
			for ( Agent a : neighbors )
			{
				if ( ! a.isAspect(NameRef.surfaceList) )
					continue;
				List<Surface> surfaces =
									(List<Surface>) a.get(NameRef.surfaceList);
				distributionMap = (CoordinateMap) a.getValue(VD_TAG);
				sgLoop: for ( SubvoxelPoint p : sgPoints )
				{
					/* Only give location in significant dimensions. */
					pLoc = p.getRealLocation(nDim);
					for ( Surface s : surfaces )
						if ( collision.distance(s, pLoc) < 0.0 )
						{
							distributionMap.increase(coord, p.volume);
							/*
							 * We only want to count this point once, even
							 * if other surfaces of the same agent hit it.
							 */
							continue sgLoop;
						}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param environment
	 * @param agents
	 * @return
	 */
	public static PDEupdater standardUpdater(
					EnvironmentContainer environment, AgentContainer agents)
	{
		return new PDEupdater()
		{
			/*
			 * This is the updater method that the PDEsolver will use before
			 * each mini-timestep.
			 */
			public void prestep(HashMap<String, SpatialGrid> variables, 
					double dt)
			{
				applyEnvReactions(environment);
				applyAgentReactions(environment, agents);
				PMToolsAgentEvents.agentsGrow(agents, dt);
			}
		};
	}
	
	/**
	 * \brief Iterate over all solute grids, applying any reactions that occur
	 * in the environment to the grids' PRODUCTIONRATE arrays.
	 * 
	 * @param environment The environment container of a {@code Compartment}.
	 */
	private static void applyEnvReactions(EnvironmentContainer environment)
	{
		Collection<Reaction> reactions = environment.getReactions();
		if ( reactions.isEmpty() )
			return;
		Shape shape = environment.getShape();
		/*
		 * Iterate over the spatial discretisation of the environment, applying
		 * extracellular reactions as required.
		 */
		Set<String> soluteNames = environment.getSoluteNames();
		SpatialGrid solute;
		HashMap<String,Double> concns = new HashMap<String,Double>();
		Set<String> productNames;
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); 
				coord = shape.iteratorNext())
		{
			/* Get concentrations in grid voxel. */
			concns.clear();
			for ( String soluteName : soluteNames )
			{
				solute = environment.getSoluteGrid(soluteName);
				concns.put(soluteName, solute.getValueAt(CONCN, coord));
			}	
			/* Iterate over each compartment reactions. */
			for ( Reaction r : reactions )
			{
				productNames = r.getStoichiometry().keySet();
				/* Calculate rate of the reaction. */
				double rate = r.getRate(concns);
				/* Write rate for each product to grid. */
				double productRate;
				for ( String product : productNames )
					if ( environment.isSoluteName(product) )
					{
						productRate = rate * r.getStoichiometry(product);
						solute = environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
					}
			}
		}
	}
	
	/**
	 * \brief 
	 * 
	 * <p><b>NOTE</b>: this method assumes that the volume distribution maps
	 * of all relevant agents have already been calculated. This is typically
	 * done just once per process manager step, rather than at every PDE solver
	 * mini-timestep.</p>
	 * 
	 * @param environment
	 * @param agents
	 */
	@SuppressWarnings("unchecked")
	private static void applyAgentReactions(
			EnvironmentContainer environment, AgentContainer agents)
	{
		SpatialGrid solute;
		HashMap<String,Double> concns = new HashMap<String,Double>();
		/*
		 * Loop over all agents, applying their reactions to the
		 * relevant solute grids, in the voxels calculated before the 
		 * updater method was set.
		 */
		List<Reaction> reactions;
		CoordinateMap distributionMap;
		List<Agent> agentList = agents.getAllLocatedAgents();
		agentList.removeIf(NO_REAC_FILTER);
		for ( Agent a : agentList )
		{
			reactions = (List<Reaction>) a.getValue(XmlLabel.reactions);
			distributionMap = (CoordinateMap) a.getValue(VD_TAG);
			a.set(GR_TAG, 0.0);
			if ( a.isAspect(IP_TAG) )
			{
				HashMap<String,Double> internalProduction = 
								(HashMap<String,Double>) a.getValue(IP_TAG);
				for (String key : internalProduction.keySet())
					internalProduction.put(key, 0.0);
			}
			/*
			 * Scale the distribution map so that its contents sum up to one.
			 */
			distributionMap.scale();
			/*
			 * Now look at all the voxels this agent covers.
			 */
			double concn;
			for ( int[] coord : distributionMap.keySet() )
			{
				for ( Reaction r : reactions )
				{
					/* 
					 * Build the dictionary of variable values. Note
					 * that these will likely overlap with the names in
					 * the reaction stoichiometry (handled after the
					 * reaction rate), but will not always be the same.
					 * Here we are interested in those that affect the
					 * reaction, and not those that are affected by it.
					 */
					concns.clear();
					for ( String varName : r.variableNames )
					{
						if ( environment.isSoluteName(varName) )
						{
							solute = environment.getSoluteGrid(varName);
							concn = solute.getValueAt(CONCN, coord);
						}
						else if ( a.isAspect(varName) )
						{
							// TODO divide by the voxel volume here?
							concn = a.getDouble(varName); 
							concn *= distributionMap.get(coord);
						}
						else
						{
							// TODO safety?
							concn = 0.0;
						}
						concns.put(varName, concn);
					}
					/*
					 * Calculate the reaction rate based on the 
					 * variables just retrieved.
					 */
					double rate = r.getRate(concns);
					/* 
					 * Now that we have the reaction rate, we can 
					 * distribute the effects of the reaction. Note
					 * again that the names in the stoichiometry may
					 * not be the same as those in the reaction
					 * variables (although there is likely to be a
					 * large overlap).
					 * TODO move this part to a "poststep" updater 
					 * method?
					 */
					double productionRate;
					for ( String productName : r.getStoichiometry().keySet())
					{
						productionRate = rate * 
								r.getStoichiometry(productName);
						if ( environment.isSoluteName(productName) )
						{
							solute = environment.getSoluteGrid(productName);
							solute.addValueAt(PRODUCTIONRATE, 
									coord, productionRate);
						}
						else if ( a.isAspect(productName) )
						{
							System.out.println("agent reaction catched " + 
									productName);
							/* 
							 * NOTE Bas [17Feb2016]: Put this here as 
							 * example, though it may be nicer to
							 * launch a separate agent growth process
							 * manager here.
							 */
							/* 
							 * NOTE Bas [17Feb2016]: The average growth
							 * rate for the entire agent, not just for
							 * the part that is in one grid cell later
							 * this may be specific separate
							 * expressions that control the growth of
							 * separate parts of the agent (eg lipids/
							 * other storage compounds)
							 */
						}
						else if ( 
							a.getString(XmlLabel.species).equals(productName) )
						{
							double curRate = a.getDouble(GR_TAG);
							a.set(GR_TAG, curRate + productionRate * 
									distributionMap.get(coord));
						}
						else if ( a.isAspect(IP_TAG) )
						{
							HashMap<String,Double> internalProduction = 
									(HashMap<String,Double>) a.getValue(IP_TAG);
							double curRate = productionRate * 
													distributionMap.get(coord);
							if ( internalProduction.containsKey(productName) )
								curRate += internalProduction.get(productName);
							internalProduction.put(productName, curRate);
						} 
						else
						{
							System.out.println("agent reaction catched " + 
									productName);
							// TODO safety?
						}
					}
				}
			}
			// TODO clear the volumeDistribution map? Saves copying at division
		}
	}
}
