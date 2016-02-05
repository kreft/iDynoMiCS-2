package agent.state.library;

import agent.Agent;
import agent.state.SecondaryState;
import agent.state.State;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;
import utility.ExtraMath;


public class CoccoidRadius extends SecondaryState implements State {

	/**
	 * input volume
	 * @author baco
	 *
	 */
	public void set(Object state)
	{

	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		// V = 4/3 Pi r^3
		return ExtraMath.radiusOfASphere((double) agent.get(input[0]));
	}
	
	public State copy()
	{
		return this;
	}
}

