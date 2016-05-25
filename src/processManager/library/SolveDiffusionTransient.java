/**
 * 
 */
package processManager.library;

import static grid.SpatialGrid.ArrayType.CONCN;
import static grid.SpatialGrid.ArrayType.DIFFUSIVITY;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;
import static dataIO.Log.Tier.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlLabel;
import grid.SpatialGrid;
import grid.wellmixedSetter.AllSameMixing;
import grid.wellmixedSetter.IsWellmixedSetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import processManager.PMToolsAgentEvents;
import processManager.ProcessManager;
import reaction.Reaction;
import shape.subvoxel.CoordinateMap;
import shape.Shape;
import shape.subvoxel.SubvoxelPoint;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;
import surface.Collision;
import surface.Surface;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a time-dependent manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionTransient extends ProcessManager
{
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	protected final static Predicate<Agent> NO_REAC_FILTER = 
			(a -> ! a.isAspect(NameRef.agentReactions));
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have relevant components of a body.
	 */
	protected final static Predicate<Agent> NO_BODY_FILTER = 
			(a -> (! a.isAspect(NameRef.surfaceList)) ||
					( ! a.isAspect(NameRef.bodyRadius)));
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
	private static final String VD_TAG = NameRef.agentVolumeDistributionMap;
	/**
	 * Aspect name for the TODO
	 */
	private static final String IP_TAG = NameRef.internalProduction;
	/**
	 * Aspect name for the TODO
	 */
	private static final String GR_TAG = NameRef.growthRate;
	/**
	 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
	 */
	protected PDEsolver _solver;
	/**
	 * The names of all solutes this solver is responsible for.
	 */
	protected String[] _soluteNames;
	/**
	 * TODO
	 */
	protected HashMap<String,IsWellmixedSetter> _wellmixed;
	/**
	 * TODO 
	 */
	// TODO replace with diffusivitySetter
	protected HashMap<String,Double> _diffusivity;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		this.init(getStringA("solutes"));
	}
	
	/**
	 * \brief Initialise this diffusion-reaction process manager with a list of
	 * solutes it is responsible for.
	 * 
	 * @param soluteNames The list of solutes this is responsible for.
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();
		this._solver.init(this._soluteNames, false);
		
		// TODO quick fix for now
		this._wellmixed = new HashMap<String,IsWellmixedSetter>();
		for ( String soluteName : this._soluteNames )
		{
			AllSameMixing mixer = new AllSameMixing();
			mixer.setValue(1.0);
			this._wellmixed.put(soluteName, mixer);
		}
		// TODO enter a diffusivity other than one!
		this._diffusivity = new HashMap<String,Double>();
		for ( String sName : soluteNames )
			this._diffusivity.put(sName, 1.0);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/*
		 * Set up the agent mass distribution maps, to ensure that agent
		 * reactions are spread over voxels appropriately.
		 */
		setupAgentDistributionMaps(environment, agents);
		/*
		 * Set up the relevant arrays in each of our solute grids: diffusivity 
		 * & well-mixed need only be done once each process manager time step,
		 * but production rate must be reset every time the PDE updater method
		 * is called.
		 */
		for ( String soluteName : this._soluteNames )
		{
			SpatialGrid solute = environment.getSoluteGrid(soluteName);
			// TODO use diffusivitySetter
			solute.newArray(DIFFUSIVITY, this._diffusivity.get(soluteName));
			this._wellmixed.get(soluteName).updateWellmixed(solute, agents);
		}
		/*
		 * Set the updater method and solve.
		 */
		this._solver.setUpdater(standardUpdater(environment, agents));
		this._solver.solve(environment.getSolutes(), this._timeStepSize);
	}
	
	/*************************************************************************
	 * INTERNAL METHODS
	 ************************************************************************/
	
	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 */
	@SuppressWarnings("unchecked")
	private static void setupAgentDistributionMaps(
					EnvironmentContainer environment, AgentContainer agents)
	{
		Log.out(DEBUG, "Setting up agent distribution maps");
		Tier level = DEBUG;
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
		 * Now fill these agent biomass distribution maps.
		 */
		Shape shape = environment.getShape();
		int nDim = agents.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		double[] sides;
		List<SubvoxelPoint> svPoints;
		List<Agent> nhbs;
		List<Surface> surfaces;
		double[] pLoc;
		Collision collision = new Collision(null, shape);
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			/* Find all agents that overlap with this voxel. */
			// TODO a method for getting a voxel's bounding box directly?
			location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
			shape.getVoxelSideLengthsTo(dimension, coord);
			sides = Vector.subset(dimension, nDim);
			/* NOTE the agent tree is always the amount of actual dimension */
			nhbs = agents.treeSearch(location, sides);
			/* Filter the agents for those with reactions, radius & surface. */
			nhbs.removeIf(NO_REAC_FILTER);
			nhbs.removeIf(NO_BODY_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( nhbs.isEmpty() )
				continue;
			Log.out(level, "  "+nhbs.size()+" agents overlap with coord "+
					Vector.toString(coord));
			/* 
			 * Find the sub-voxel resolution from the smallest agent, and
			 * get the list of sub-voxel points.
			 */
			double minRad = Vector.min(sides);
			double radius;
			for ( Agent a : nhbs )
			{
				radius = a.getDouble(NameRef.bodyRadius);
				Log.out(level, "   agent "+a.identity()+" has radius "+radius);
				minRad = Math.min(radius, minRad);
			}
			minRad *= SUBGRID_FACTOR;
			Log.out(level, "  using a min radius of "+minRad);
			svPoints = shape.getCurrentSubvoxelPoints(minRad);
			Log.out(level, "  gives "+svPoints.size()+" sub-voxel points");
			/* Get the sub-voxel points and query the agents. */
			for ( Agent a : nhbs )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(NameRef.agentReactions) )
					continue;
				if ( ! a.isAspect(NameRef.surfaceList) )
					continue;
				surfaces = (List<Surface>) a.get(NameRef.surfaceList);
				Log.out(level, "  "+"   agent "+a.identity()+" has "+
						surfaces.size()+" surfaces");
				distributionMap = (CoordinateMap) a.getValue(VD_TAG);
				sgLoop: for ( SubvoxelPoint p : svPoints )
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
		Log.out(DEBUG, "Finished setting up agent distribution maps");
	}
	
	/**
	 * \brief The standard PDE updater method resets the solute
	 * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
	 * {@code Agent}s to grow.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 * @return PDE updater method.
	 */
	private static PDEupdater standardUpdater(
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
				for ( String solute : variables.keySet() )
					environment.getSoluteGrid(solute).newArray(PRODUCTIONRATE);
				applyEnvReactions(environment);
				applyAgentReactions(environment, agents);
				PMToolsAgentEvents.agentsGrow(agents, dt);
			}
		};
	}
	
	/**
	 * \brief Iterate over all solute grids, applying any reactions that occur
	 * in the environment to the grids' {@code PRODUCTIONRATE} arrays.
	 * 
	 * @param environment The environment container of a {@code Compartment}.
	 */
	private static void applyEnvReactions(EnvironmentContainer environment)
	{
		Log.out(DEBUG, "Applying environmental reactions");
		Collection<Reaction> reactions = environment.getReactions();
		if ( reactions.isEmpty() )
		{
			Log.out(DEBUG, "No reactions to apply, skipping");
			return;
		}
		/*
		 * Iterate over the spatial discretization of the environment, applying
		 * extracellular reactions as required.
		 */
		Shape shape = environment.getShape();
		Set<String> soluteNames = environment.getSoluteNames();
		SpatialGrid solute;
		HashMap<String,Double> concns = new HashMap<String,Double>();
		for ( String soluteName : soluteNames )
			concns.put(soluteName, 0.0);
		Set<String> productNames;
		double rate, productRate;
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext() )
		{
			/* Get concentrations in grid voxel. */
			for ( String soluteName : soluteNames )
			{
				solute = environment.getSoluteGrid(soluteName);
				concns.put(soluteName, solute.getValueAt(CONCN, coord));
			}	
			/* Iterate over each compartment reactions. */
			for ( Reaction r : reactions )
			{
				rate = r.getRate(concns);
				productNames = r.getStoichiometry().keySet();
				/* Write rate for each product to grid. */
				// TODO verify that all environmental reactions have
				// only solute products, so we don't have to check here.
				for ( String product : productNames )
					if ( environment.isSoluteName(product) )
					{
						productRate = rate * r.getStoichiometry(product);
						solute = environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
					}
			}
		}
		Log.out(DEBUG, "Finished applying environmental reactions");
	}
	
	/**
	 * \brief 
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution maps
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
		Log.out(DEBUG, "Applying agent reactions");
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
		HashMap<String,Double> internalProdctn;
		for ( Agent a : agentList )
		{
			reactions = (List<Reaction>) a.getValue(XmlLabel.reactions);
			distributionMap = (CoordinateMap) a.getValue(VD_TAG);
			a.set(GR_TAG, 0.0);
			if ( a.isAspect(IP_TAG) )
			{
				internalProdctn = (HashMap<String,Double>) a.getValue(IP_TAG);
				for (String key : internalProdctn.keySet())
					internalProdctn.put(key, 0.0);
			}
			/*
			 * Scale the distribution map so that its contents sum up to one.
			 */
			distributionMap.scale();
			/*
			 * Now look at all the voxels this agent covers.
			 */
			double concn, rate, productionRate;
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
					rate = r.getRate(concns);
					/* 
					 * Now that we have the reaction rate, we can 
					 * distribute the effects of the reaction. Note
					 * again that the names in the stoichiometry may
					 * not be the same as those in the reaction
					 * variables (although there is likely to be a
					 * large overlap).
					 */
					for ( String productName : r.getStoichiometry().keySet())
					{
						productionRate = rate * r.getStoichiometry(productName);
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
							internalProdctn = 
									(HashMap<String,Double>) a.getValue(IP_TAG);
							double curRate = productionRate * 
													distributionMap.get(coord);
							if ( internalProdctn.containsKey(productName) )
								curRate += internalProdctn.get(productName);
							internalProdctn.put(productName, curRate);
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
		Log.out(DEBUG, "Finished applying agent reactions");
	}
}