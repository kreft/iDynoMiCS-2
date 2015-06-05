package agent.activity;

import utility.Vect;
import agent.Agent;

public class Divide extends Activity {
	
	public boolean prerequisites(Agent agent)
	{
		if (agent.getMass() > (Double) agent.getState("divisionTreshold"))
			return true;
		else
			return false;
	}
		
	public void execute(Agent agent) {
		if (prerequisites(agent)) 
		{
			/*
			 * create your child.
			 */
			Agent child = new Agent();
			
			/*
			 * do all you splitting up work here
			 */
			Double[] mass = Vect.product((Double[]) agent.getState("mass"),0.5);
			child.setState("mass", mass);
			agent.setState("mass", Vect.copy(mass));
						
			/*
			 * initiate follow-up activities
			 */
			agent.doActivity("Segregation",child);
			
			/*
			 * register birth of new agent.
			 */
			child.registerBirth();
		}
	}

	@Override
	public void execute(Agent agent, Agent secondActor) {

	}
	
}
