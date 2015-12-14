package agent.event.library;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import agent.event.Event;

public class SimpleGrowth extends Event {
	
	// input "mass" "growthRate"

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
	
}
