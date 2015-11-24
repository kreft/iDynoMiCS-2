package agent.state.secondary;

import agent.Agent;
import agent.state.State;
import linearAlgebra.Vector;

public class ComponentVolumeState implements State {

	public void init(Object state)
	{
		
	}
	
	public Object get(Agent agent)
	{
		return  Vector.dotQuotient((double[]) agent.get("mass"), 
									(double[]) agent.get("density"));
	}
}
