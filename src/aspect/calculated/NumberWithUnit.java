package aspect.calculated;


import aspect.AspectInterface;
import java.util.HashMap;
import java.util.Map;

import aspect.Aspect;
import aspect.Calculated;
import expression.Expression;
import expression.Unit;
import expression.Unit.SI;
import utility.GenericTrio;

public class NumberWithUnit extends Calculated {

	
	private double value;
	private boolean calculated = false;
	private Expression expression;
	private Unit _userUnit;
	private Double _userNumber;
	private HashMap<String, Double> variables = new HashMap<String, Double>();
	private Map<SI,GenericTrio<SI, String, Double>> _unitSystem = 
			Unit.formatMap("pg","µm","min");
	
	
	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new Expression( input.replaceAll("\\s+","") );
		this._userUnit = this.expression.getUnit();
		this._userNumber = 
				Double.parseDouble(this.expression.getNumberComponent());
		
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) {
		if (calculated)
		{
			return value;
		}
		else
		{
			value = _userNumber * _userUnit.format(_unitSystem);
			value = expression.getValue(variables);
			calculated = true;
			return value;
		}
	}

}
