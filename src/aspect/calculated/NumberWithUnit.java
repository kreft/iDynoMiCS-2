package aspect.calculated;


import aspect.AspectInterface;
import java.util.Map;

import aspect.Calculated;
import expression.Expression;
import expression.Unit;
import expression.Unit.SI;
import idynomics.Idynomics;
import utility.GenericTrio;

public class NumberWithUnit extends Calculated {

	private double _value;
	
	@Override
	public void setInput(String input)
	{
		this._value = new Expression( input ).format( Idynomics.unitSystem );
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) 
	{
		return _value;
	}

}
