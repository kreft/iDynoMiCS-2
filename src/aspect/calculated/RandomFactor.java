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
 * \brief A uniformly distributed random number in [0,1).
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
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

