
/**
 * 
 */
package processManager;

import java.util.HashMap;
import java.util.List;

import agent.Agent;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
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
					 * FIXME Bas: why not one grid with reaction rate and salvage
					 * the production/consumption rate directly from the reaction
					 * stochiometry??
					 */
					sg.newArray(ArrayType.PRODUCTIONRATE);
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
				/*
				 * FIXME Bas: what is this methods supposed to provide?
				 * TODO agents put reaction rates on grids.
				 * Bas [17.02.16] for consistency sake please choose something
				 * consistent it make total sense to simply scale the reactions
				 * with the amount of catalyst (biomass) in a grid cell, this
				 * is the convention, or if you want to let the agents perform
				 * their own reaction (less fast but oke). Don't simply copy
				 * stuff around, I don't like hard coded spaghetti. 
				 */
				for ( Agent agent : agents.getAllLocatedAgents() )
				{
					// FIXME Bas[3NOV2015]: removed dependence on depreciated class
//					for (Object aState : agent.getStates(HasReactions.tester))

					// TODO Bas [09.12.15] don't use getState unless you want
					// to obtain the State (object) from the agent. Use 
					// agent.get to retrieve the value from the agent state
					// agent.get returns null if the state does not exist!
					if (agent.aspectRegistry.isGlobalAspect("reactions"))
					{
						/**
						 * TODO Bas: actually this would work better if the agent
						 * maintains a hashmap of activities for it's reactions
						 * this way it can regulate it's own rate and this can
						 * be transfered to a grid with a simple expression
						 * biomass * activity = active biomass in grid cell
						 * (the X in your rate expression)
						 */
						
						@SuppressWarnings("unchecked")
						List<String> reactions = 
								(List<String>) agent.get("reactions");
						// NOTE Bas: I rather not copy identical Reaction objects'
						// to every single agent that performs it if you can
						// refer to them in a list
						for (String reaction : reactions)
						{
							environment.getReaction(reaction);
							//TODO whatever is required from this method -> please comment
							// testing
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
}
