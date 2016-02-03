package agent.event.library;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import agent.event.Event;

/**
 * TODO: We are going to do this different (integrate into ODE/PDE), this event
 * is simplified and not correct.
 * Simple event that increases the agents mass according to it's growth rate
 * and the time step
 * @author baco
 *
 * NOTE: input "mass" "growthRate"
 */
public class SimpleGrowth extends Event {

	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		// TODO: We are going to do this different (integrate into ODE/PDE)
		// this method is just for testing purposes.
		// simple ask the agents at what rate they grow, they should than figure
		// this out from their local conditions
		double newMass = (double) agent.get(input[1]) * 
		(double) agent.get(input[0]) * timeStep + (double) agent.get(input[0]);
		agent.set(input[0], newMass);
	}
	
	/**
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy() 
	{
		return this;
	}
	
}
