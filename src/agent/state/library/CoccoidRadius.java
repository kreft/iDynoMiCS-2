package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: volume
 */
public class CoccoidRadius extends Calculated {

	public Object get(AspectInterface aspectOwner)
	{
		return ExtraMath.radiusOfASphere(aspectOwner.getDouble(input[0]));
	}

}

