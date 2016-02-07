package agent.state.library;

import agent.Body;
import agent.state.Calculated;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;

/**
 * input: body
 * @author baco
 *
 */
public class JointsState extends Calculated {
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).getJoints();
	}

}
