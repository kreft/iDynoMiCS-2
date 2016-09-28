package aspect.calculated;

import java.util.Map;

import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.Helper;

/**
 * \brief TODO
 * 
 * <p>Note that this could be calculated using StateExpression, but this is
 * quicker.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class SimpleVolumeState extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public String DENSITY = AspectRef.agentDensity;
	
	public SimpleVolumeState()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Object massObject = aspectOwner.getValue(this.MASS);
		double totalMass = 0.0;
		if ( massObject instanceof Double )
			totalMass = (double) massObject;
		else if ( massObject instanceof Double[] )
		{
			Double[] massArray = (Double[]) massObject;
			for ( Double m : massArray )
				totalMass += m;
		}
		else if ( massObject instanceof Map )
		{
			// TODO assume all mass types used unless specified otherwise
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) massObject;
			totalMass = Helper.totalValue(massMap);
		}
		else
		{
			// TODO safety?
		}
		// TODO could look at what class of object density is, as it may also 
		// be an array or map.
		return totalMass / aspectOwner.getDouble(DENSITY);
	}

}
