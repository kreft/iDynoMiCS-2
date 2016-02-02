package test;

import agent.Agent;
import agent.state.PrimaryState;
import agent.state.State;
import agent.state.StateLoader;

public class AgentStateExamples {

	public static void main(String[] args) {

		// our test agent
		Agent testagent = new Agent();

		// add a new state
		State mass = new PrimaryState();
		mass.set(0.1);
		testagent.set("mass",mass);
				
		// add a new state the automated way
		testagent.set("density", 0.2);
		
		// add a predefined secondary state
		State volume =  StateLoader.getSecondary("SimpleVolumeState","mass,density");
		
		testagent.set("volume",volume);
		
		// removed "calculated state" since we don't want to use anonymous
		// states but it is still possible..
		testagent.set("volume2",
		// add a secondary state that was not previously defined (anonymous class).
		new State() {
			@Override
			public void set(Object state) {

			}
			
			@Override
			public Object get(Agent agent) {
				return (Double) agent.get("mass") / (Double) agent.get("density");
			}

			@Override
			public State copy() {
				return this; // *information is only stored in primary states
			}
			
			@Override
			public State duplicate(Agent agent) {
				// TODO Auto-generated method stub
				return this;
			}
		});
		
		System.out.println(testagent.get("mass"));
		System.out.println(testagent.getState("mass").getClass());
		System.out.println(testagent.get("density"));
		System.out.println(volume.get(testagent));
		System.out.println(testagent.get("volume"));
		System.out.println(testagent.get("volume2"));
		System.out.println(testagent.get("volume2"));
		
		testagent.set("myint", 0);
		testagent.set("mybool", true);
		testagent.set("mystring", "hello!");
		
		//////////////
		// now the same thing the ezway
		/////////////
		
		
		long tic = System.currentTimeMillis();
		int times = 1000000;
		for (int b = 0; b < times; b++)
		{
		// our test agent
		Agent ezagent = new Agent();

		// add a new state
		ezagent.set("mass",0.1);
		
		// add a new state again
		ezagent.set("density", 0.2);
		
		// add a predefined secondary state
		ezagent.set("volume",  StateLoader.getSecondary("SimpleVolumeState","mass,density"));
		
		}
		System.out.println(times + " times in: " + (System.currentTimeMillis()-tic) + " milisecs");

	}

}
