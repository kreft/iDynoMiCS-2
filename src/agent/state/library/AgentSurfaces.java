package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

public class AgentSurfaces extends Calculated {
	
	/**
	 * input body
	 */
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).getSurfaces();
	}

}