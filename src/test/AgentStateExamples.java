package test;

import agent.Agent;
import agent.state.CalculatedState;
import agent.state.PrimaryState;
import agent.state.State;
import agent.state.secondary.SimpleVolumeState;

public class AgentStateExamples {

	public static void main(String[] args) {

		// our test agent
		Agent testagent = new Agent();

		// add a new state
		State mass = new PrimaryState();
		mass.init(testagent, 0.1);
		testagent.setState("mass",mass);
		
		// add a new state the automated way
		testagent.setPrimary("density", 0.2);
		
		// add a predefined secondary state
		State volume = new SimpleVolumeState();
		volume.init(testagent, null);
		testagent.setState("volume",volume);
		
		// add a secondary state that was not previously defined (anonymous class).
		State anonymous = new CalculatedState();
		anonymous.init(testagent, new CalculatedState.stateExpression() {
			
			@Override
			public Object calculate(Agent agent) {
				return (Double) agent.get("mass") / (Double) agent.get("density");
			}
		});
		testagent.setState("volume2",anonymous);
		
		System.out.println(testagent.get("mass"));
		System.out.println(testagent.getState("mass").getClass());
		System.out.println(testagent.get("density"));
		System.out.println(volume.get());
		System.out.println(testagent.get("volume"));
		System.out.println(anonymous.get());
		System.out.println(anonymous.getClass());
		System.out.println(testagent.get("volume2"));
		
		
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
		ezagent.set("volume",new SimpleVolumeState());
		
		// add a secondary state that was not previously defined (anonymous class).
		ezagent.set("volume2", new CalculatedState.stateExpression() {
			
			@Override
			public Object calculate(Agent agent) {
				return (Double) agent.get("mass") / (Double) agent.get("density");
			}
		});
		
		
		}
		System.out.println(times + " times in: " + (System.currentTimeMillis()-tic) + " milisecs");

	}

}
