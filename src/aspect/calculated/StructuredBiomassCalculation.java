package aspect.calculated;

import java.util.HashMap;
import java.util.Map;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.Calculated;
import expression.Expression;
import referenceLibrary.AspectRef;


/**
 * This aspect allows calculations to be carried out on the components of an
 * agent's massMap, returning a double.
 * 
 * @author Tim Foster
 *
 */
public class StructuredBiomassCalculation extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public Expression _expression;
	
	@Override
	public void setInput(String input)
	{
		this._expression = new Expression(input);
	}
	
	@Override
	public Object get(AspectInterface aspectOwner)
	{
		Object mass = aspectOwner.getValue(MASS);
		if ( mass != null && mass instanceof Map )
		{
		@SuppressWarnings("unchecked")
		Map<String,Double> massMap = (Map<String,Double>) mass;
		return this._expression.getValue(massMap);
		}
		else if ( mass != null && mass instanceof Double )
		{
			Map<String,Double> massMap = new HashMap<String,Double>();
			massMap.put(MASS, (Double) mass);
			return this._expression.getValue(massMap);
		}
		else
		{
			return null;
		}
	}
	
	
	
	
}
