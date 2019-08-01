package aspect.calculated;


import aspect.AspectInterface;
import java.util.Map;

import aspect.Calculated;
import expression.Expression;
import expression.Unit;
import expression.Unit.SI;
import utility.GenericTrio;

public class NumberWithUnit extends Calculated {

	
	private double value;
	private boolean calculated = false;
	private Expression expression;
	private Map<SI,GenericTrio<SI, String, Double>> _unitSystem = 
			Unit.formatMap("pg","µm","min");
	
	
	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new Expression( input );
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) {
		if (calculated)
		{
			return value;
		}
		else
		{
			value = Double.parseDouble(expression.getExpression()) * 
					expression.getUnit().format(_unitSystem);
			calculated = true;
			return value;
		}
	}

}
