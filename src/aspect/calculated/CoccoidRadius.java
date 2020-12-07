package aspect.calculated;

import agent.Body;
import agent.Body.Morphology;
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
public class CoccoidRadius extends Calculated {
	
	public String VOLUME = AspectRef.agentVolume;
	
	public CoccoidRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		if( aspectOwner.isAspect(AspectRef.transientRadius))
		{
			if( ((Body) aspectOwner.getValue(AspectRef.agentBody)).getMorphology() == Morphology.BACILLUS)
				return aspectOwner.getDouble(AspectRef.transientRadius);
		}
		// FIXME is this appropriate in 1D & 2D compartments?
		return ExtraMath.radiusOfASphere(aspectOwner.getDouble(VOLUME));
	}

}

