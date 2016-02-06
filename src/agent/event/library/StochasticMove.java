package agent.event.library;

import java.util.List;

import agent.Agent;
import agent.Body;
import agent.event.Event;
import generalInterfaces.AspectInterface;
import linearAlgebra.Vector;
import surface.Point;

/**
 * TODO: this method is simplified and not correct
 * Simple testing method for stochastic movement
 * @author baco
 *
 * NOTE: input "body"
 */
public class StochasticMove extends Event {
	
	/**
	 * Perform one stochastic move (uniRand) scaled by the time step) NOTE: for
	 * testing purposes only, incorrect method
	 */
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		//FIXME this is not correct!! simple event for testing.
		Body agentBody = (Body) agent.get(input[0]);
		List<Point> points = agentBody.getPoints();
		for (Point p : points)
		{
			p.setPosition(Vector.add(p.getPosition(), 
					Vector.randomPlusMinus(agentBody.nDim(), timeStep*0.5)));
		}
	}
	
	/**
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy() 
	{
		return this;
	}
	
}
