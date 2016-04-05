package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

/**
 * input mass, density
 * @author baco
 *
 */
public class SimpleVolumeState extends Calculated {
	
	public SimpleVolumeState()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}

}
