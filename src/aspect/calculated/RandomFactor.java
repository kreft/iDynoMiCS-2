package aspect.calculated;

import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.ExtraMath;
import utility.Helper;

import java.util.HashMap;
import java.util.Map;

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
public class RandomFactor extends Calculated {
	public RandomFactor()
	{

	}

	public Object get(AspectInterface aspectOwner)
	{
		return ExtraMath.getUniRandDbl();
	}

}

