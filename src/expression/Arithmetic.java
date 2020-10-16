/**
 * 
 */
package expression;

import java.util.ArrayList;

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
		if ( a instanceof Addition)
		{
			if ( b instanceof Addition )
				for ( Component c : ((Addition) b).getAllComponents() )
					((Addition) a).appendComponent(c);
			else
				((Addition) a).appendComponent(b);
			return ((Addition) a);
		}
		else if ( b instanceof Addition )
		{
			((Addition) b).prependComponent(a);
			return ((Addition) b);
		}
		else
		{
			ArrayList<Component> list = new ArrayList<Component>();
			list.add(a);
			list.add(b);
			return new Addition(list);
		}
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
		if ( a instanceof Multiplication)
		{
			if ( b instanceof Multiplication )
				for ( Component c : ((Multiplication) b).getAllComponents() )
					((Multiplication) a).appendComponent(c);
			else
				((Multiplication) a).appendComponent(b);
			return ((Multiplication) a);
		}
		else if ( b instanceof Multiplication )
		{
			((Multiplication) b).prependComponent(a);
			return ((Multiplication) b);
		}
		else
		{
			ArrayList<Component> list = new ArrayList<Component>();
			list.add(a);
			list.add(b);
			return new Multiplication(list);
		}
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