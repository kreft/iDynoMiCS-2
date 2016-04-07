package processManager;

import static grid.SpatialGrid.ArrayType.CONCN;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import agent.Agent;
import grid.SpatialGrid;
import grid.subgrid.CoordinateMap;
import grid.subgrid.SubgridPoint;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.PDEupdater;
import surface.Collision;
import surface.Surface;

public final class PMToolsDiffuseReact
{
	private static final Predicate<Agent> NO_REAC_FILTER = 
							(a -> ! a.isAspect(NameRef.agentReactions));
	
	/**
	 * \brief TODO
	 * 
	 * @param environment
	 * @param agents
	 */
	@SuppressWarnings("unchecked")
	public static void setupAgentDistributionMaps(
					EnvironmentContainer environment, AgentContainer agents)
	{
		String vdTag = "volumeDistribution";
		/*
		 * Reset the agent biomass distribution maps.
		 */
		CoordinateMap distributionMap;
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			distributionMap = new CoordinateMap();
			a.set(vdTag, distributionMap);
		}
		/*
		 * Set up the solute grids and the agents before we start to solve.
		 */
		// FIXME this is a temporary fix until we unify all grid resolutions.
		String firstSolute = environment.getSoluteNames().iterator().next();
		SpatialGrid solute = environment.getSoluteGrid(firstSolute);
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		int nDim = agents.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		List<Agent> neighbors;
		List<SubgridPoint> sgPoints;
		double[] pLoc;
		Collision collision = new Collision(null, agents.getShape());
		for ( int[] coord = solute.resetIterator(); 
				solute.isIteratorValid(); coord = solute.iteratorNext())
		{
			/* Find all agents that overlap with this voxel. */
			location = solute.getVoxelOrigin(coord);
			solute.getVoxelSideLengthsTo(dimension, coord);
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
			// TODO the scaling factor of a quarter is chosen arbitrarily
			double minRad = Vector.min(dimension);
			for ( Agent a : neighbors )
				if ( a.isAspect(NameRef.bodyRadius) )
					minRad = Math.min(a.getDouble(NameRef.bodyRadius), minRad);
			sgPoints = solute.getCurrentSubgridPoints(0.25 * minRad);
			/* 
			 * Get the subgrid points and query the agents.
			 */
			for ( Agent a : neighbors )
			{
				if ( ! a.isAspect(NameRef.surfaceList) )
					continue;
				List<Surface> surfaces =
									(List<Surface>) a.get(NameRef.surfaceList);
				distributionMap = (CoordinateMap) a.getValue(vdTag);
				sgLoop: for ( SubgridPoint p : sgPoints )
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
				agentsGrow(agents, dt);
			}
		};
	}
	
	private static void applyEnvReactions(EnvironmentContainer environment)
	{
		// FIXME this is a temporary fix until we unify all grid resolutions.
		String firstSolute = environment.getSoluteNames().iterator().next();
		SpatialGrid defaultGrid = environment.getSoluteGrid(firstSolute);
		SpatialGrid solute;
		HashMap<String,Double> concns = new HashMap<String,Double>();
		for ( int[] coord = defaultGrid.resetIterator(); 
				defaultGrid.isIteratorValid(); 
				coord = defaultGrid.iteratorNext())
		{
			/* Iterate over all compartment reactions. */
			for (Reaction r : environment.getReactions() )
			{
				/* Obtain concentrations in gridCell. */
				concns.clear();
				for ( String varName : r.variableNames )
				{
					if ( environment.isSoluteName(varName) )
					{
						solute = environment.getSoluteGrid(varName);
						concns.put(varName,
									solute.getValueAt(CONCN, coord));
					}
				}
				/* Obtain rate of the reaction. */
				double rate = r.getRate(concns);
				double productionRate;
				for ( String product : r.getStoichiometry().keySet())
				{
					productionRate = rate * r.getStoichiometry(product);
					if ( environment.isSoluteName(product) )
					{
						/* Write rate for each product to grid. */
						solute = environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, 
								coord, productionRate);
					}
				}
			}
		}
	}
	
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
			reactions = (List<Reaction>) a.getValue("reactions");
			distributionMap = (CoordinateMap)
					a.getValue("volumeDistribution");
			a.set("growthRate",0.0);
			if (a.isAspect("internalProduction"))
			{
				HashMap<String,Double> internalProduction = 
						(HashMap<String,Double>) 
						a.getValue("internalProduction");
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
						else if ( a.getString("species").equals(productName) )
						{
							double curRate = a.getDouble("growthRate");
							a.set("growthRate", curRate + productionRate * 
									distributionMap.get(coord));
						}
						else if ( a.isAspect("internalProduction") )
						{
							HashMap<String,Double> internalProduction = 
									(HashMap<String,Double>) 
									a.getValue("internalProduction");
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
		}
	}
	
	private static void agentsGrow(AgentContainer agents, double dt)
	{
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			// TODO these strings are important, so should probably be in
			// XmlLabel or NameRef. What is "produce"?
			a.event("growth", dt);
			a.event("produce", dt);
		}
	}
}
