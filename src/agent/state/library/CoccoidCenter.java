package agent.state.library;

import agent.Body;
import agent.state.Calculated;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;

/**
 * input body
 * @author baco
 *
 */
public class CoccoidCenter extends Calculated {

	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		// V = 4/3 Pi r^3
		return ((Body) agent.get(input[0])).getJoints().get(0);
	}

}
