package aspect.calculated;

import java.util.Map;

import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: volume
 */
public class WetWeight extends Calculated {
	
	public String MASS_MAP = AspectRef.agentMassMap;
	public String WET_DRY = AspectRef.WetDryRatio;
	
	public WetWeight()
	{
		setInput("massMap");
	}

	public Object get(AspectInterface aspectOwner)
	{
		@SuppressWarnings("unchecked")
		Map<String,Double> massMap = (Map<String,Double>) 
				aspectOwner.getValue(MASS_MAP);
		
		Double out = 0.0;
		for (Double d : massMap.values() )
			out += d;
		return out * (Double) aspectOwner.getValue(WET_DRY);
	}

}