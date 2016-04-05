package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;
import idynomics.NameRef;

/**
 * input mass, density
 * @author baco
 *
 */
public class SimpleVolumeState extends Calculated {
	
	public String MASS = NameRef.agentMass;
	public String DENSITY = NameRef.agentDensity;
	
	public SimpleVolumeState()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  (double) agent.get(MASS) / (double) agent.get(DENSITY);
	}

}
