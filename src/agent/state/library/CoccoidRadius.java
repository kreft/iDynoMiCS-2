package agent.state.library;

import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;
import utility.ExtraMath;

/**
 * input volume
 * @author baco
 *
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

