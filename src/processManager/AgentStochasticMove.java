package processManager;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import surface.Point;

public class AgentStochasticMove extends ProcessManager {


	protected void internalStep(EnvironmentContainer environment, AgentContainer agents) {
		for ( Agent agent : agents.getAllLocatedAgents() )
		{
			agent.event("stochasticMove", _timeStepSize);
			
			/**
			 * Save agents that stochastically move out of the domain
			 * NOTE: still some agents seem to end up just outside
			 */
			for (Point point: ((Body) agent.get("body")).getPoints())
			{
				agents.getShape().applyBoundaries(point.getPosition());
			}
		}
	}
	

}
