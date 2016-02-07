package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

/**
 * 
 * @param input: body
 */
public class SimpleSurface extends Calculated {
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).getSurface();
	}

}