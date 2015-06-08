package agent.activity;

import utility.Vect;
import agent.Agent;

public class Divide extends Activity {
	
	public boolean prerequisites(Agent[] actors)
	{
		if (actors[0].getMass() > (Double) actors[0].getState("divisionTreshold"))
			return true;
		else
			return false;
	}
		
	public void execute(Agent[] agents, Double timestep) {
		if (prerequisites(agents)) 
		{
			/*
			 * create your child.
			 */
			Agent child = new Agent();
			
			/*
			 * do all you splitting up work here
			 */
			Double[] mass = Vect.product((Double[]) agents[0].getState("mass"),0.5);
			child.setState("mass", mass);
			agents[0].setState("mass", Vect.copy(mass));
						
			/*
			 * initiate follow-up activities, child is the second actor.
			 */
			agents[0].doActivity("Segregation",child,null);
			
			/*
			 * register birth of new agent.
			 */
			child.registerBirth();
		}
	}
	
}
