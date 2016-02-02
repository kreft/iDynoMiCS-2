package agent.event.library;

import java.util.List;

import agent.Agent;
import agent.Body;
import agent.event.Event;
import linearAlgebra.Vector;
import surface.Point;

public class StochasticMove extends Event {
	
	// input "body"

	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		//FIXME this is not correct!! simple event for testing.
		Body agentBody = (Body) agent.get(input[0]);
		List<Point> points = agentBody.getPoints();
		for (Point p : points)
		{
			p.setPosition(Vector.add(p.getPosition(), Vector.randomPlusMinus(agentBody.nDim(), timeStep*2.0)));
		}
	}
	
	public Object copy() {
		return this;
	}
	
}
