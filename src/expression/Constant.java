/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Constant extends ComponentSimple
{
	protected double _value;
	
	public Constant(String name, double value)
	{
		super(name);
		this._value = value;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return Double.toString(this._value);
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._value;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		return Constant.zero();
	}
	
	
	
	
	public static Constant zero()
	{
		return new Constant("0", 0.0);
	}
	
	public static boolean isConstantWithValue(Component c, double value)
	{
		if ( c instanceof Constant )
			if ( ((Constant) c).getValue(null) == value )
				return true;
		return false;
	}
	
	public static Constant minus()
	{
		return new Constant("-1", -1.0);
	}
	
	public static Constant one()
	{
		return new Constant("1", 1.0);
	}
	
	public static Constant two()
	{
		return new Constant("2", 2.0);
	}
	
	public static Constant ten()
	{
		return new Constant("10", 10.0);
	}
	
	public static Constant euler()
	{
		return new Constant("e", Math.E);
	}
	
	public static Constant pi()
	{
		return new Constant("\\pi", Math.PI);
	}
}