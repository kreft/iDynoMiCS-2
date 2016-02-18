/**
 * 
 */
package processManager;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import agent.Agent;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.subgrid.SubgridPoint;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;
import surface.Ball;
import surface.Collision;
import surface.Surface;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SolveDiffusionTransient extends ProcessManager
{
	/**
	 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
	 */
	protected PDEsolver _solver;
	/**
	 * The names of all solutes this solver is responsible for.
	 */
	protected String[] _soluteNames;
	/**
	 * TODO this may need to be generalised to some method for setting
	 * diffusivities, e.g. lower inside biofilm.
	 */
	protected HashMap<String,Double> _diffusivity;
	
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	protected final static Predicate<Agent> NO_REAC_FILTER = 
								(a -> ! a.checkAspect(NameRef.agentReactions));
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public SolveDiffusionTransient()
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();
		this._solver.init(this._soluteNames, false);
		// TODO enter a diffusivity other than one!
		this._diffusivity = new HashMap<String,Double>();
		for ( String sName : soluteNames )
			this._diffusivity.put(sName, 1.0);
	}
	
	public void init()
	{
		this.init(getStringA("solutes"));
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * Bas please add commenting on the function and approach of this process
	 * manager
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/*
		 * Set up the solute grids and the agents before we start to solve.
		 */
		SpatialGrid solute;
		for ( String soluteName : _soluteNames )
		{
			solute = environment.getSoluteGrid(soluteName);
			/*
			 * Set up the relevant arrays in each of our solute grids.
			 */
			solute.newArray(ArrayType.PRODUCTIONRATE);
			// TODO use a diffusion setter
			solute.newArray(ArrayType.DIFFUSIVITY, 
										_diffusivity.get(soluteName));
			// TODO use a DomainSetter
			solute.newArray(ArrayType.DOMAIN, 1.0);
			/*
			 * Set up the agent biomass distribution maps.
			 */
			for ( Agent a : agents.getAllLocatedAgents() )
			{
				HashMap<int[],Double> distributionMap = new HashMap<int[],Double>();
				a.set("volumeDistribution", distributionMap);
			}
			/*
			 * Now fill these agent biomass distribution maps.
			 */
			double[] location;
			double[] dimension = new double[3];
			List<Agent> neighbors;
			List<SubgridPoint> sgPoints;
			HashMap<int[],Double> distributionMap;
			Collision collision = new Collision(null, agents.getShape());
			for ( int[] coord = solute.resetIterator(); 
					solute.isIteratorValid(); coord = solute.iteratorNext())
			{
				/* Find all agents that overlap with this voxel. */
				location = solute.getVoxelOrigin(coord);
				solute.getVoxelSideLengthsTo(dimension, coord);
				/* NOTE the agent tree is always the amount of actual dimension */
				neighbors = agents._agentTree.cyclicsearch(
							  Vector.subset(location,agents.getNumDims()),
							  Vector.subset(dimension,agents.getNumDims()));
				/* If there are none, move onto the next voxel. */
				if ( neighbors.isEmpty() )
					continue;
				/* Filter the agents for those with reactions. */
				neighbors.removeIf(NO_REAC_FILTER);
				/* 
				 * Find the sub-grid resolution from the smallest agent, and
				 * get the list of sub-grid points.
				 */
				// TODO the scaling factor of a quarter is chosen arbitrarily
				double minRad = Vector.min(dimension);
				for ( Agent a : agents.getAllLocatedAgents() )
					if ( a.checkAspect(NameRef.bodyRadius) )
					{
						minRad = Math.min(a.getDouble(NameRef.bodyRadius), minRad);
					}
				sgPoints = solute.getCurrentSubgridPoints(0.25 * minRad);
				/* 
				 * Get the subgrid points and query the agents.
				 */
				for ( Agent a : neighbors )
				{
					if ( ! a.checkAspect(NameRef.agentReactions) )
						continue;
					List<Surface> surfaces = 
									(List<Surface>) a.get(NameRef.surfaceList);
					distributionMap = (HashMap<int[],Double>) 
											a.getValue("volumeDistribution");
					
					sgLoop: for ( SubgridPoint p : sgPoints )
					{
						/* Only give location in actual dimensions. */
						// NOTE Rob [18Feb2016]: out of curiosity, why do we make a
						// Ball and not a Point?
						Ball b = new Ball(
								Vector.subset(p.realLocation,agents.getNumDims()),
								0.0);
						b.init(collision);
						for ( Surface s : surfaces )
							if ( b.distanceTo(s) < 0.0 )
							{
								/*
								 * If this is not the first time the agent has seen
								 * this coordinate, we need to add the volume
								 * rather than overwriting it.
								 * 
								 * Note that we need to copy the coord vector so
								 * that it does not change when the SpatialGrid
								 * iterator moves on!
								 */
								double newVolume = p.volume;
								if ( distributionMap.containsKey(coord) )
									newVolume += distributionMap.get(coord);
								distributionMap.put(Vector.copy(coord), newVolume);
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
		/*
		 * Make the updater method
		 */
		PDEupdater updater = new PDEupdater()
		{
			/*
			 * This is the updater method that the PDEsolver will use before
			 * each mini-timestep.
			 */
			public void prestep(HashMap<String, SpatialGrid> variables)
			{
				/*
				 * Loop over all agents, applying their reactions to the
				 * relevant solute grids, in the voxels calculated before the 
				 * updater method was set.
				 */
				HashMap<String,Double> concns = new HashMap<String,Double>();
				SpatialGrid aSG;
				List<Reaction> reactions;
				HashMap<int[],Double> distributionMap;
				for ( Agent a : agents.getAllLocatedAgents() )
				{
					if ( ! a.checkAspect(NameRef.agentReactions) )
						continue;
					reactions = (List<Reaction>) a.get("reactions");
					distributionMap = (HashMap<int[],Double>)
											a.getValue("volumeDistribution");
					/*
					 * Calculate the total volume covered by this agent,
					 * according to the distribution map. This is likely to be
					 * slightly different to the agent volume calculated 
					 * directly.
					 */
					double totalVoxVol = 0.0;
					for ( double voxVol : distributionMap.values() )
						totalVoxVol += voxVol;
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
									aSG = environment.getSoluteGrid(varName);
									concn = aSG.getValueAt(ArrayType.CONCN,
															coord);
									// FIXME: was getting strange [16,0,0] 
									// coord values here (index out of bounds)
								}
								else if ( a.checkAspect(varName) )
								{
									// TODO divide by the voxel volume here?
									concn = a.getDouble(varName); 
									concn *= distributionMap.get(coord);
									concn /= totalVoxVol;
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
							 */
							// TODO move this part to a "poststep" updater method?
							double productionRate;
							for ( String productName : 
												r.getStoichiometry().keySet())
							{
								productionRate = rate * 
											r.getStoichiometry(productName);
								if ( environment.isSoluteName(productName) )
								{
									aSG = environment.getSoluteGrid(productName);
									aSG.addValueAt(ArrayType.PRODUCTIONRATE, 
														coord, productionRate);
								}
								else if ( a.checkAspect(productName) )
								{
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
									productionRate += (double) 
													a.getDouble("growthRate");
									a.set("growthRate", productionRate);
									
									/* Timespan of growth event */
									// TODO Rob[18Feb2016]: Surely this should
									// happen at the very end?
									double dt = 0.0;
									a.event("growth", dt * productionRate);
								}
								else
								{
									// TODO safety?
								}
							}
						}
					}
				}
			}
		};
		/*
		 * Set the updater method and solve.
		 */
		this._solver.setUpdater(updater);
		this._solver.solve(environment.getSolutes(), this._timeStepSize);
	}
}
