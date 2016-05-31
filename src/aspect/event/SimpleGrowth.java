package aspect.event;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;
import utility.Helper;

/**
 * TODO: We are going to do this different (integrate into ODE/PDE), this event
 * is simplified and not correct.
 * Simple event that increases the agents mass according to its growth rate
 * and the time step
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * NOTE: input "mass" "growthRate"
 */
public class SimpleGrowth extends Event {

	public String MASS = NameRef.agentMass;
	public String GROWTH_RATE = NameRef.growthRate;
	public SimpleGrowth()
	{
		setInput("mass,growthRate");
	}

	// TODO switch over to the internal production paradigm
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		// TODO: We are going to do this different (integrate into ODE/PDE)
		// this method is just for testing purposes.
		// simple ask the agents at what rate they grow, they should than figure
		// this out from their local conditions
		double newMass = (double) Helper.setIfNone(agent.get(GROWTH_RATE), 0.0) * timeStep + 
									(double) agent.get(MASS);
		agent.set(MASS, newMass);
	}
}
