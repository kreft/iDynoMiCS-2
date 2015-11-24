package agent.state.secondary;

import utility.Vector;
import agent.Agent;
import agent.state.State;

public class ComponentVolumeState implements State {

	public void init(Object state)
	{
		
	}
	
	public Object get(Agent agent)
	{
		return  Vector.dotQuotient((Double[]) agent.get("mass"), (Double[]) agent.get("density"));
	}

}
