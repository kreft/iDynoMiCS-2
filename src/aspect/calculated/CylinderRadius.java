package aspect.calculated;

import java.util.HashMap;
import java.util.Map;

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
 * This is used to model coccoid cells in a 2 dimensional compartment
 * such that agents are represented as cylinders spanning the "virtual"
 * Z dimension, which has a length of 1.0
 */
public class CylinderRadius extends Calculated {
	
	public String VOLUME = AspectRef.agentVolume;
	
	public CylinderRadius()
	{
		setInput("volume");
	}

	public Object get(AspectInterface aspectOwner)
	{
		
		Object volume = aspectOwner.getValue(VOLUME);
		
		double totalVolume = 0.0;
		
		if (volume instanceof Map)
		{
			Map <String, Double> volumeMap = (HashMap<String, Double>) volume;
			
			totalVolume = Helper.totalValue(volumeMap);
		}
		
		else
		{
			totalVolume = (double) volume;
		}
		
		return ExtraMath.radiusOfACylinder(totalVolume, 1.0);
	}

}

