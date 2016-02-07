package agent.state.library;

import agent.state.Calculated;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;

/**
 * input mass, density
 * @author baco
 *
 */
public class SimpleVolumeState extends Calculated {
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}

}
