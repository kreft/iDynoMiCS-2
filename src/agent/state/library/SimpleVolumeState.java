package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class SimpleVolumeState extends Calculated
{
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}

}
