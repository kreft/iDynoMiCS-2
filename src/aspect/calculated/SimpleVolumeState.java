package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;

/**
 * \brief TODO
 * 
 * <p>Note that this could be calculated using StateExpression, but this is
 * quicker.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
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
		return aspectOwner.getDouble(MASS) / aspectOwner.getDouble(DENSITY);
	}

}
