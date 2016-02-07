package agent.state.library;

import agent.state.Calculated;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;
import linearAlgebra.Vector;

/**
 * input mass, density
 * @author baco
 *
 */
public class ComponentVolumeState extends Calculated {
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  Vector.dotQuotient((double[]) agent.get(input[0]), 
									(double[]) agent.get(input[1]));
	}

}
