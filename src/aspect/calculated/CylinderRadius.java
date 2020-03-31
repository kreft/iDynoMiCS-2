package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: volume
 */
public class CylinderRadius extends Calculated {
	
	public String VOLUME = AspectRef.agentVolume;
	
	public CylinderRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		// FIXME is this appropriate in 1D & 2D compartments?
		return ExtraMath.radiusOfACylinder(aspectOwner.getDouble(VOLUME),1.0);
	}

}

