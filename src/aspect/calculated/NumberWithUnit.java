package aspect.calculated;


import aspect.AspectInterface;
import aspect.Calculated;
import expression.Expression;
import idynomics.Idynomics;
import utility.Helper;

public class NumberWithUnit extends Calculated {

	private double _value;
	
	@Override
	public void setInput(String input)
	{
		try
		{
			this._value = 
				new Expression( input ).format( Idynomics.unitSystem );
		}
		catch (NumberFormatException | StringIndexOutOfBoundsException
				| NullPointerException f)
		{
			input = Helper.obtainInput(null,
					"The value " + input + "is not in the correct format for "
					+ "the class NumberWithUnit. Please provide a double with "
					+ "decimal point and optional valid unit in square "
					+ "brackets.");
			setInput(input);
		}
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) 
	{
		return _value;
	}

}
