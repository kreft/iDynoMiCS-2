package agent.event.library;

import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;
import linearAlgebra.Vector;
import surface.Point;
import utility.ExtraMath;

/**
 * TODO: this method is simplified and not correct
 * Simple testing method for stochastic movement
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * NOTE: input "body", scale
 */
public class StochasticMove extends Event {
	
	public String BODY = NameRef.agentBody;
	public String STOCHASTIC_STEP = NameRef.agentStochasticStep;
	public String STOCHASTIC_DIRECTION = NameRef.agentStochasticDirection;
	public String STOCHASTIC_PAUSE = NameRef.agentStochasticPause;
	public String STOCHASTIC_DISTANCE = NameRef.agentStochasticDistance;
	
	public StochasticMove()
	{
		setInput("body,stochasticStep");
	}
	
	/**
	 * Perform one stochastic move (uniRand) scaled by the time step) NOTE: for
	 * testing purposes only, incorrect method
	 */
	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep)
	{
		Agent agent = (Agent) initiator;

		Body agentBody = (Body) agent.get(BODY);
		List<Point> points = agentBody.getPoints();
		
		/* we are in a stochastic pause */
		if (agent.isAspect(STOCHASTIC_PAUSE))
		{
			double pause = agent.getDouble(STOCHASTIC_PAUSE);
			if(pause > 0.0)
				agent.set(STOCHASTIC_PAUSE, pause-timeStep);
			else
				agent.reg().remove(STOCHASTIC_PAUSE);
		}
		/* we are stochastically moving */
		else if(agent.isAspect(STOCHASTIC_DIRECTION))
		{
			/* calculate the move */
			double[] move = Vector.times((double[]) 
					agent.get(STOCHASTIC_DIRECTION), timeStep);
			double dist = agent.getDouble(STOCHASTIC_DISTANCE);
			
			/* update to move distance */
			agent.set(STOCHASTIC_DISTANCE, dist
					- Math.sqrt(Vector.dotProduct(move, move)));
			
			/* clear stochastic move if completed */
			if(agent.getDouble(STOCHASTIC_DISTANCE) < 0.0)
			{
				agent.reg().remove(STOCHASTIC_DISTANCE);
				agent.reg().remove(STOCHASTIC_DIRECTION);
			}
			else
			{
				/* perform the stochastic move, only for coccoid now */
				for (Point p : points)
				{
					p.setPosition(Vector.add(p.getPosition(), move ));
				}
			}
		}
		/* either start moving or pause again */
		else
		{
			/* evaluate a new stochastic move */
			if(ExtraMath.random.nextDouble() > timeStep*4.0) 
				// FIXME this assumes a time step to always be 1.0 or lower, improve on this
			{
				agent.set(STOCHASTIC_PAUSE, timeStep);
			}
			else
			{
				/* set random directions */
				double [] randDir = Vector.randomPlusMinus(agentBody.nDim(), 
						(double) agent.getDouble(input[1]));
				agent.set(STOCHASTIC_DIRECTION, randDir);
				
				/* calculate stochasticDistance */
				agent.set(STOCHASTIC_DISTANCE, Math.sqrt(Vector.dotProduct(
						randDir, randDir)));
			}
		}
	}
}
