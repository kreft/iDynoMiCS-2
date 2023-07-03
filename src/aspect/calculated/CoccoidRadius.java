package aspect.calculated;

import java.util.HashMap;
import java.util.Map;

import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: volume
 *
 * This should only be used to model 3 dimensional spherical cells in 3D or
 * "2.5D" compartments. To model "flat" coccoid cells, use CylinderRadius
 */
public class CoccoidRadius extends Calculated {
	
	public String VOLUME = AspectRef.agentVolume;
	
	public CoccoidRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		Object volume = aspectOwner.getValue(VOLUME);
		double totalVolume = 0.0;

		if (volume instanceof Map) {
			Map <String, Double> volumeMap = (HashMap<String, Double>) volume;
			totalVolume = Helper.totalValue(volumeMap);
		}
		else {
			totalVolume = (double) volume;
		}

		if( aspectOwner.isAspect(AspectRef.transientRadius)) {
			if( ((Body) aspectOwner.getValue(AspectRef.agentBody)).
					getMorphology() == Morphology.BACILLUS)
				return aspectOwner.getDouble(AspectRef.transientRadius);
		}
		return ExtraMath.radiusOfASphere(totalVolume);
	}

}

