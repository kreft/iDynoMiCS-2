package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: volume
 */
public class CoccoidRadius extends Calculated {
	
	public String VOLUME = NameRef.agentVolume;
	
	public CoccoidRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		return ExtraMath.radiusOfASphere(aspectOwner.getDouble(VOLUME));
	}

}

