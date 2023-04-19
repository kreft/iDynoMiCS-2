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
public class SimpleCoccoidRadius extends Calculated {
	public Object get(AspectInterface aspectOwner)
	{
		//NOTE: only for 3D simulations
		return ExtraMath.cubeRoot(aspectOwner.getDouble(AspectRef.agentVolume)*0.75/Math.PI);
	}
}

