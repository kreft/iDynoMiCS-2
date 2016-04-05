package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;
import idynomics.NameRef;
import linearAlgebra.Vector;

/**
 * input mass, density
 * @author baco
 *
 */
public class ComponentVolumeState extends Calculated {
	
	public String MASS = NameRef.agentMass;
	public String DENSITY = NameRef.agentDensity;
	
	public ComponentVolumeState()
	{
		setInput("mass,density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  Vector.dotQuotient((double[]) agent.get(MASS), 
									(double[]) agent.get(DENSITY));
	}

}
