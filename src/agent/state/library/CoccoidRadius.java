package agent.state.library;

import agent.state.Calculated;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;
import utility.ExtraMath;

/**
 * input volume
 * @author baco
 *
 */
public class CoccoidRadius extends Calculated {

	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		// V = 4/3 Pi r^3
		return ExtraMath.radiusOfASphere((double) agent.get(input[0]));
	}

}

