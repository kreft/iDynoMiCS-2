package aspect.calculated;

import java.util.Map;

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
	
	public String MASS_MAP = AspectRef.agentMassMap;
	public Expression _expression;
	
	@Override
	public void setInput(String input)
	{
		this._expression = new Expression(input);
	}
	
	@Override
	public Object get(AspectInterface aspectOwner)
	{
		Map<String,Double> massMap = (Map<String,Double>) 
				aspectOwner.getValue(MASS_MAP);
		return this._expression.getValue(massMap);
	}
	
	
	
	
}
