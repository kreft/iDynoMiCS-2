/**
 * 
 */
package processManager;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import concurentTasks.AgentReactions;
import concurentTasks.ConcurrentWorker;
import concurentTasks.EnvironmentReactions;
import grid.SpatialGrid;
import static grid.SpatialGrid.ArrayType.*;
import grid.subgrid.SubgridPoint;
import grid.wellmixedSetter.AllSame;
import grid.wellmixedSetter.IsWellmixedSetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;
import surface.Collision;
import surface.Surface;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionTransient extends ProcessManager
{

	ConcurrentWorker worker = new ConcurrentWorker();
	/**
	 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
	 */
	protected PDEsolver _solver;
	/**
	 * The names of all solutes this solver is responsible for.
	 */
	protected String[] _soluteNames;
	/**
	 * 
	 */
	protected HashMap<String,IsWellmixedSetter> _wellmixed;
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
								(a -> ! a.isAspect(NameRef.agentReactions));
	
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
		
		// TODO quick fix for now
		this._wellmixed = new HashMap<String,IsWellmixedSetter>();
		for ( String soluteName : this._soluteNames )
		{
			AllSame mixer = new AllSame();
			mixer.setValue(1.0);
			this._wellmixed.put(soluteName, mixer);
		}
		// TODO enter a diffusivity other than one!
		this._diffusivity = new HashMap<String,Double>();
		for ( String sName : soluteNames )
			this._diffusivity.put(sName, 1.0);
	}
	
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		
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
		int nDim = agents.getNumDims();
		/*
		 * Set up the solute grids and the agents before we start to solve.
		 */
		SpatialGrid solute;
		
		
		
		solute = environment.getSoluteGrid(_soluteNames[0]);
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
			neighbors = agents.treeSearch(Vector.subset(location, nDim),
						  					Vector.subset(dimension, nDim));
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
			
			// FIXME maybe an expensive way to figure out the smallest agent, 
			//the smallest agents may even lack their own reactions (eps part)
			// Rob [14Mar2016]: agents without reactions will already have been
			// removed (see line 166) but you're right that we don't need to
			// look at all agents. I've changed it to only look at neighbors.
			for ( Agent a : neighbors )
				if ( a.isAspect(NameRef.bodyRadius) )
					minRad = Math.min(a.getDouble(NameRef.bodyRadius), minRad);
			sgPoints = solute.getCurrentSubgridPoints(0.25 * minRad);
			/* 
			 * Get the subgrid points and query the agents.
			 */
			for ( Agent a : neighbors )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(NameRef.agentReactions) )
					continue;
				if ( ! a.isAspect(NameRef.surfaceList) )
					continue;
				List<Surface> surfaces =
									(List<Surface>) a.get(NameRef.surfaceList);
				distributionMap = (HashMap<int[],Double>) 
										a.getValue("volumeDistribution");
				double[] pLoc;
				sgLoop: for ( SubgridPoint p : sgPoints )
				{
					/* 
					 * Only give location in actual dimensions.
					 */
					pLoc = p.getRealLocation(nDim);
					for ( Surface s : surfaces )
						if ( collision.distance(s, pLoc) < 0.0 )
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
							// NOTE discovered strange hashmap behavior
							int[] coordCopy = Vector.copy(coord);
							if ( Vector.containsKey(distributionMap, coord) )
							{
								distributionMap.put(coordCopy,
										distributionMap.get(coord) + p.volume);
							}
							else
							{
								distributionMap.put(coordCopy, p.volume);
							}
							/*
							 * We only want to count this point once, even
							 * if other surfaces of the same agent hit it.
							 */
							continue sgLoop;
						}
				}
			}
		}
		
		
		for ( String soluteName : _soluteNames )
		{
			solute = environment.getSoluteGrid(soluteName);
			/*
			 * Set up the relevant arrays in each of our solute grids.
			 */
			solute.newArray(PRODUCTIONRATE);
			// TODO use a diffusion setter
			solute.newArray(DIFFUSIVITY, _diffusivity.get(soluteName));
			this._wellmixed.get(soluteName).updateWellmixed(solute, agents);


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
			public void prestep(HashMap<String, SpatialGrid> variables, 
					double dt)
			{
				/* Gather a defaultGrid to iterate over. */
				SpatialGrid defaultGrid = environment.getSoluteGrid(environment.
						getSolutes().keySet().iterator().next());
				
				//FIXME seems to be pretty thread unsafe 
//				worker.executeTask(new EnvironmentReactions(
//						defaultGrid.getAllCoords(),environment,agents));

				SpatialGrid solute;

				for ( int[] coord = defaultGrid.resetIterator(); 
						defaultGrid.isIteratorValid(); 
							coord = defaultGrid.iteratorNext())
				{
					/* Iterate over all compartment reactions. */
					for (Reaction r : environment.getReactions() )
					{
						/* Obtain concentrations in gridCell. */
						HashMap<String,Double> concentrations = 
								new HashMap<String,Double>();
						for ( String varName : r.variableNames )
						{
							if ( environment.isSoluteName(varName) )
							{
								solute = environment.getSoluteGrid(varName);
								concentrations.put(varName,
											solute.getValueAt(CONCN, coord));
							}
						}
						/* Obtain rate of the reaction. */
						double rate = r.getRate(concentrations);
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
				
				
				
//				worker.concurrentEnabled(true);
				//FIXME also seems thread unsafe
//				worker.executeTask(new AgentReactions(agents,environment,dt));

				
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
					if ( ! a.isAspect("reactions") )
						continue;
					reactions = (List<Reaction>) a.getValue("reactions");
					distributionMap = (HashMap<int[],Double>)
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
									concn = aSG.getValueAt(CONCN, coord);
								}
								else if ( a.isAspect(varName) )
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
									aSG.addValueAt(PRODUCTIONRATE, 
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
								else if ( a.getString("species").equals(productName))
								{
									double curRate = a.getDouble("growthRate");
									a.set("growthRate", curRate + productionRate * 
											distributionMap.get(coord) / totalVoxVol);

									
								}
								else if ( a.isAspect("internalProduction"))
								{
									HashMap<String,Double> internalProduction = 
											(HashMap<String,Double>) 
											a.getValue("internalProduction");
									for( String p : internalProduction.keySet())
									{
										if(p.equals(productName))
										{
											internalProduction.put(productName, 
													internalProduction.get(productName) 
													+ productionRate * distributionMap.get(coord) / totalVoxVol);
										}
									}

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
					a.event("growth", dt);
					a.event("produce", dt);
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
