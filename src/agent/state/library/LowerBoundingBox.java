package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

/**
 * input body, radius
 * @author baco
 *
 */
public class LowerBoundingBox extends Calculated {
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).coord((double) agent.get(input[1]));
	}
	
}
