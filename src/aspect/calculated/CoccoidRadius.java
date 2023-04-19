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
	public Object get(AspectInterface aspectOwner)
	{
		if( aspectOwner.isAspect(AspectRef.transientRadius))
		{
			if( ((Body) aspectOwner.getValue(AspectRef.agentBody)).getMorphology() == Morphology.BACILLUS)
				return aspectOwner.getDouble(AspectRef.transientRadius);
		}
		//NOTE: only for 3D simulations
		return ExtraMath.radiusOfASphere(aspectOwner.getDouble(AspectRef.agentVolume));
	}

}

