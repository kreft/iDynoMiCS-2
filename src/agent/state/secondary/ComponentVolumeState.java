package agent.state.secondary;

import agent.Agent;
import agent.state.SecondaryState;
import agent.state.State;
import linearAlgebra.Vector;

public class ComponentVolumeState extends SecondaryState implements State {

	public void set(Object state)
	{
		
	}
	
	public Object get(Agent agent)
	{
		return  Vector.dotQuotient((double[]) agent.get("mass"), 
									(double[]) agent.get("density"));
	}
	
	public State copy()
	{
		return this;
	}
}
