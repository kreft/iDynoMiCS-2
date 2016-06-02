package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import aspect.AspectRef;
import generalInterfaces.Quizable;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class ComponentVolumeState extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public String DENSITY = AspectRef.agentDensity;
	
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
