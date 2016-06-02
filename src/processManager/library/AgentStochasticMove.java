package processManager.library;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import surface.Point;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentStochasticMove extends ProcessManager
{
	protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
	{
		for ( Agent agent : agents.getAllLocatedAgents() )
		{
			agent.event("stochasticMove", this._timeStepSize);
			/* Save agents that stochastically move out of the domain. */
			// FIXME still some agents seem to end up just outside
			Body body = ((Body) agent.get(AspectRef.agentBody));
			for ( Point point: body.getPoints() )
				agents.getShape().applyBoundaries( point.getPosition() );
		}
	}
	
	

}
