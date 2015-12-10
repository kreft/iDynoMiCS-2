package agent.event.library;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import agent.event.Event;

public class SimpleGrowth implements Event {

	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		// TODO: We are going to do this different (integrate into ODE/PDE)
		// this method is just for testing purposes.
		// simple ask the agents at what rate they grow, they should than figure
		// this out from their local conditions
		double newMass = (double) agent.get("growthRate") * 
		(double) agent.get("mass") * timeStep + (double) agent.get("mass");
		agent.set("mass", newMass);
	}
	
}
