
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
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEsolver.Updater;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @since August 2015
 */
public class SolveDiffusionTransient extends ProcessManager
{
	/**
	 * TODO
	 */
	protected PDEsolver _solver;
	
	/**
	 * TODO
	 */
	protected String[] _soluteNames;
	
	/**
	 * TODO this may need to be generalised to some method for setting
	 * diffusivities, e.g. lower inside biofilm.
	 */
	protected HashMap<String,Double> _diffusivity;
	
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
	
	/**
	 * Bas please add commenting on the function and approach of this process
	 * manager
	 */
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		Updater updater = new Updater()
		{
			public void presolve(HashMap<String, SpatialGrid> variables)
			{
				SpatialGrid sg;
				for ( String soluteName : _soluteNames )
				{
					sg = variables.get(soluteName);
					/*
					 * Reset the production rate and diffusivity arrays,
					 * creating them if they do not already exist.
					 * 
					 * TODO use a diffusion setter
					 */
					// FIXME this was nullifying all the work don by the ConstructProductRateGrids
//					sg.newArray(ArrayType.PRODUCTIONRATE);
					sg.newArray(ArrayType.DIFFUSIVITY, 
												_diffusivity.get(soluteName));
					/*
					 * TODO use a DomainSetter
					 */
					sg.newArray(ArrayType.DOMAIN, 1.0);
				}
			}
			
			public void prestep(HashMap<String, SpatialGrid> variables)
			{
				SpatialGrid solute;
				int[] coord;
				double[] origin;
				double[] dimension = new double[3];
				List<Agent> localAgents;
				double subRes;
				List<SubgridPoint> sgPoints;
				for ( String soluteName : variables.keySet() )
				{
					solute = variables.get(soluteName);
					for ( coord = solute.resetIterator();
												solute.isIteratorValid();
												coord = solute.iteratorNext() )
					{
						/* Find all agents that overlap with this voxel. */
						origin = solute.getVoxelOrigin(coord);
						solute.getVoxelSideLengthsTo(dimension, coord);
						
						/* NOTE the agent tree is always the amount of actual dimension */
						localAgents = agents._agentTree.cyclicsearch(
									  Vector.subset(origin,agents.getNumDims()),
									  Vector.subset(dimension,agents.getNumDims()));
						/* If there are none, move onto the next voxel. */
						if ( localAgents.isEmpty() )
							continue;
						/* Filter the agents for those with reactions. */
						localAgents.removeIf(hasNoReactions());
						/* Subgrid resolution from the smallest agent. */
						// TODO Job for Bas
						subRes = Vector.min(dimension) * 0.25;
						/* Get the subgrid points and query the agents. */
						
					}
				}
				
				/*
				 * 
				 */
				for ( Agent agent : agents.getAllLocatedAgents() )
				{
					if (agent.aspectRegistry.isGlobalAspect("reactions"))
					{
						/**
						 * 
						 */
						
						@SuppressWarnings("unchecked")
						List<Reaction> reactions = 
								(List<Reaction>) agent.get("reactions");

						for (Reaction reaction : reactions)
						{
							System.out.println(reaction.toString());
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
	
	/**
	 * \brief Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	private static Predicate<Agent> hasNoReactions()
	{
		return a -> ! a.aspectRegistry.isGlobalAspect("reactions");
	}
}
