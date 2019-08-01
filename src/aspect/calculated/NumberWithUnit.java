package aspect.calculated;


import aspect.AspectInterface;
import java.util.Map;

import aspect.Calculated;
import expression.Expression;
import expression.Unit;
import expression.Unit.SI;
import utility.GenericTrio;

public class NumberWithUnit extends Calculated {

	
	private double _value;
	private boolean _calculated = false;
	private Expression _expression;
	final static private Map<SI,GenericTrio<SI, String, Double>> _unitSystem = 
			Unit.formatMap("pg","µm","min");
	
	
	@Override
	public void setInput(String input)
	{
		this._input = input;
		this._expression = new Expression( input );
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) {
		if (_calculated)
		{
			return _value;
		}
		else
		{
			_value = Double.parseDouble(_expression.getExpression()) * 
					_expression.getUnit().format(_unitSystem);
			_calculated = true;
			return _value;
		}
	}

}
