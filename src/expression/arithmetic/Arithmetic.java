/**
 * 
 */
package expression.arithmetic;

import java.util.ArrayList;

import expression.Component;

/**
 * \brief Set of useful components for easy reference.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public final class Arithmetic
{
	/*************************************************************************
	 * CONSTANTS
	 ************************************************************************/
	
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
	
	public static Constant zero()
	{
		return new Constant("0", 0.0);
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
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * TODO deal with signs
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Addition add(Component a, Component b)
	{
			ArrayList<Component> list = new ArrayList<Component>();
			list.add(a);
			list.add(b);
			return new Addition(a,b);
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO deal with signs
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Multiplication multiply(Component a, Component b)
	{
			ArrayList<Component> list = new ArrayList<Component>();
			list.add(a);
			list.add(b);
			return new Multiplication(a,b);
	}
	
	/**
	 * \brief
	 * 
	 * TODO b instanceof Power with index < 0
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Division divide(Component a, Component b)
	{
		Component top = a;
		Component bottom = b;
		if ( a instanceof Division )
		{
			if ( b instanceof Division )
			{
				top = multiply(((Division) a).getNumerator(),
											((Division) b).getDenominator());
				bottom = multiply(((Division) a).getDenominator(),
											((Division) b).getNumerator());
			}
			else
			{
				top = ((Division) a).getNumerator();
				bottom = multiply(((Division) a).getDenominator(), b);
			}
		}
		else if ( b instanceof Division )
		{
			top = multiply(a, ((Division) b).getDenominator());
			bottom = ((Division) b).getNumerator();
		}
		return new Division(top, bottom);
	}
}