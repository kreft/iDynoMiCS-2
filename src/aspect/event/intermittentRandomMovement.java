package aspect.event;

import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
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
// TODO documentation, explanation
public class intermittentRandomMovement extends Event
{
	public String BODY = AspectRef.agentBody;
	public String STOCHASTIC_STEP = AspectRef.agentStochasticStep;
	public String STOCHASTIC_DIRECTION = AspectRef.agentStochasticDirection;
	public String STOCHASTIC_PAUSE = AspectRef.agentStochasticPause;
	public String STOCHASTIC_DISTANCE = AspectRef.agentStochasticDistance;
	
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
		
		/* Check if we are in a stochastic pause. */
		if ( agent.isAspect(STOCHASTIC_PAUSE) )
		{
			double pause = agent.getDouble(STOCHASTIC_PAUSE);
			if ( pause > 0.0 )
				agent.set(STOCHASTIC_PAUSE, pause - timeStep);
			else
				agent.reg().remove(STOCHASTIC_PAUSE);
		}
		/* Check if we are stochastically moving. */
		else if ( agent.isAspect(STOCHASTIC_DIRECTION) )
		{
			/* calculate the move */
			double[] move = Vector.times((double[]) 
					agent.get(STOCHASTIC_DIRECTION), timeStep);
			double dist = agent.getDouble(STOCHASTIC_DISTANCE)
					- Vector.normEuclid(move);
			if ( dist < 0.0 )
			{
				/* Clear stochastic move if completed. */
				agent.reg().remove(STOCHASTIC_DISTANCE);
				agent.reg().remove(STOCHASTIC_DIRECTION);
			}
			else
			{
				/* Update to move distance. */
				agent.set(STOCHASTIC_DISTANCE, dist);
				/* Perform the stochastic move, only for coccoid now */
				for (Point p : points)
				{
					p.setPosition( Vector.add(p.getPosition(), move) );
				}
			}
		}
		/* Either start moving or pause again. */
		else
		{
			/* Evaluate a new stochastic move */
			// FIXME this assumes a time step to always be 1.0 or lower,
			// improve on this
			if ( ExtraMath.getUniRandDbl() > timeStep*4.0 ) 
				agent.set(STOCHASTIC_PAUSE, timeStep);
			else
			{
				/* Set random directions. */
				double [] randDir = Vector.randomPlusMinus(agentBody.nDim(), 
						(double) agent.getDouble(AspectRef.agentStochasticStep));
				agent.set(STOCHASTIC_DIRECTION, randDir);
				/* Calculate stochasticDistance. */
				agent.set(STOCHASTIC_DISTANCE, Vector.normEuclid(randDir));
			}
		}
	}
}
