package processManager.library;

import agent.Agent;
import agent.Body;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import surface.Point;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentStochasticMove extends ProcessManager
{
	
	public static String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	public static String BODY = AspectRef.agentBody;
	
	protected void internalStep()
	{
		for ( Agent agent : this._agents.getAllLocatedAgents() )
		{
			agent.event(STOCHASTIC_MOVE, this._timeStepSize);
			/* Save agents that stochastically move out of the domain. */
			// FIXME still some agents seem to end up just outside
			Body body = ((Body) agent.get(BODY));
			for ( Point point: body.getPoints() )
				point.setPosition( this._agents.getShape().applyBoundaries( point.getPosition() ) );
		}
	}
	
	

}
