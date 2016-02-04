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
				 * TODO agents put reaction rates on grids.
				 */
				for ( Agent agent : agents.getAllLocatedAgents() )
				{
					// FIXME Bas[3NOV2015]: removed dependence on depreciated class
//					for (Object aState : agent.getStates(HasReactions.tester))

					// TODO Bas [09.12.15] don't use getState unless you want
					// to obtain the State (object) from the agent. Use 
					// agent.get to retrieve the value from the agent state
					// agent.get returns null if the state does not exist!
					if (agent.isGlobalState("reactions"))
					{
						@SuppressWarnings("unchecked")
						List<Reaction> reactions = 
								(List<Reaction>) agent.get("reactions");
						for (Reaction reaction : reactions)
						{
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
