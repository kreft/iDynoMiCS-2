package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import utility.ExtraMath;

/**
 * input volume
 * @author baco
 *
 */
public class CoccoidRadius extends Calculated {
	
	public CoccoidRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		return ExtraMath.radiusOfASphere(aspectOwner.getDouble(input[0]));
	}

}

