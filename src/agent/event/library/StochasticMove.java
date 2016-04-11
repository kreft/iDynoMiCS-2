package agent.event.library;

import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
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
public class StochasticMove extends Event
{	
	/**
	 * Perform one stochastic move (uniRand) scaled by the time step) NOTE: for
	 * testing purposes only, incorrect method
	 */
	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep)
	{
		Agent agent = (Agent) initiator;

		Body agentBody = (Body) agent.get(input[0]);
		List<Point> points = agentBody.getPoints();
		
		/* we are in a stochastic pause */
		if (agent.isAspect("stochasticPause"))
		{
			double pause = agent.getDouble("stochasticPause");
			if(pause > 0.0)
				agent.set("stochasticPause", pause-timeStep);
			else
				agent.reg().remove("stochasticPause");
		}
		/* we are stochastically moving */
		else if(agent.isAspect("stochasticDirection"))
		{
			/* calculate the move */
			double[] move = Vector.times((double[]) 
					agent.get("stochasticDirection"), timeStep);
			double dist = agent.getDouble("stochasticDistance");
			
			/* update to move distance */
			agent.set("stochasticDistance", dist
					- Math.sqrt(Vector.dotProduct(move, move)));
			
			/* clear stochastic move if completed */
			if(agent.getDouble("stochasticDistance") < 0.0)
			{
				agent.reg().remove("stochasticDistance");
				agent.reg().remove("stochasticDirection");
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
			if(ExtraMath.random.nextDouble() < 0.2)
			{
				agent.set("stochasticPause", 1.0);
			}
			else
			{
				/* set random directions */
				double [] randDir = Vector.randomPlusMinus(agentBody.nDim(), 
						(double) agent.getDouble(input[1]));
				agent.set("stochasticDirection", randDir);
				
				/* calculate stochasticDistance */
				agent.set("stochasticDistance", Math.sqrt(Vector.dotProduct(
						randDir, randDir)));
			}
		}
	}
}
