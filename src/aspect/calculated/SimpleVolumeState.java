package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.Helper;

/**
 * \brief TODO
 * 
 * <p>Note that this could be calculated using StateExpression, but this is
 * quicker.</p>
 * 
 * [Bas 18.03.19] reverted to previous version, density scaling is calculated on
 * individual basis {@See DensityScaled}.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class SimpleVolumeState extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public String DENSITY = AspectRef.agentDensity;
	public String REPRESENTED_DENSITY = AspectRef.agentRepresentedDensity;
	
	public SimpleVolumeState()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Object massObject = aspectOwner.getValue(this.MASS);
		double totalMass = Helper.totalMass(massObject);
		
		if (aspectOwner.isAspect(REPRESENTED_DENSITY))
		{
			return totalMass / aspectOwner.getDouble(REPRESENTED_DENSITY);
		}
		return totalMass / aspectOwner.getDouble(DENSITY);
	}

}
