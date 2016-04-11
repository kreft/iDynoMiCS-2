package agent.event.library;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UpdateBody extends Event
{
	public void start(AspectInterface initiator,
							AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		Body body = (Body) agent.get(input[0]);
		body.update((double) agent.get(input[1]), 0.0);
	}
}