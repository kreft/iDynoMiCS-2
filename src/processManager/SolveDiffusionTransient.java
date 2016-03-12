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
				neighbors = agents.treeSearch(
							  					Vector.subset(location, nDim),
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
				for ( Agent a : agents.getAllLocatedAgents() )
					if ( a.isAspect(NameRef.bodyRadius) )
					{
						minRad = Math.min(a.getDouble(NameRef.bodyRadius), minRad);
					}
				sgPoints = solute.getCurrentSubgridPoints(0.25 * minRad);
				/* 
				 * Get the subgrid points and query the agents.
				 */
				for ( Agent a : neighbors )
				{
					if ( ! a.isAspect(NameRef.agentReactions) )
						continue;
					if (! a.isAspect(NameRef.surfaceList) )
						continue;
					List<Surface> surfaces = (List<Surface>) 
							a.get(NameRef.surfaceList);
					distributionMap = (HashMap<int[],Double>) 
											a.getValue("volumeDistribution");
					
					sgLoop: for ( SubgridPoint p : sgPoints )
					{
						/* 
						 * Only give location in actual dimensions.
						 */
						for ( Surface s : surfaces )
							if ( collision.distance(s, Vector.subset( 
									p.realLocation,agents.getNumDims())) < 0.0 )
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
			public void prestep(HashMap<String, SpatialGrid> variables, 
					double dt)
			{
				/* Gather a defaultGrid to iterate over. */
				SpatialGrid defaultGrid = environment.getSoluteGrid(environment.
						getSolutes().keySet().iterator().next());
				
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
				
				worker.executeTask(new AgentReactions(agents,environment,dt));
			}
		};
		/*
		 * Set the updater method and solve.
		 */
		this._solver.setUpdater(updater);
		this._solver.solve(environment.getSolutes(), this._timeStepSize);
	}
}
