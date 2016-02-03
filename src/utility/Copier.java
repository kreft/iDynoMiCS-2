package utility;

import linearAlgebra.Vector;

import java.util.LinkedList;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import generalInterfaces.Copyable;

/**
 * return deep copies of input objects
 * @author baco
 *
 */
public final class Copier {
	
	/**
	 * Attempts to create a deep copy of any input object
	 * @param copyable
	 * @return
	 */
	public static Object copy(Object copyable)
	{
		if (copyable == null)
		{
			Feedback.out(LogLevel.DEBUG, "Copier returns a null object");
			return null;
		}
		if (copyable instanceof Double || copyable.getClass() == double.class)
		{
			return new Double((double) copyable);
		}
		if (copyable instanceof Double[] || copyable.getClass() == 
				double[].class)
		{
			return Vector.copy((double[]) copyable);
		}
		if (copyable instanceof Integer || copyable.getClass() == int.class)
		{
			return new Integer((int) copyable);
		}
		if (copyable instanceof Boolean || copyable.getClass() == boolean.class)
		{
			return new Boolean((boolean) copyable);
		}
		if  (copyable instanceof String)
		{
			// copy.set(String.copyValueOf(((String) state).toCharArray()));
			// FIXME double check whether this works, Strings are immutable,
			// thus if changed a new Object is
			return String.valueOf((String) copyable);
		}
		if (copyable instanceof Copyable)
		{
			return ((Copyable) copyable).copy();
		} 
		else 
		{
			Feedback.out(LogLevel.NORMAL,"WARNING: Unable to copy the input object"
					+ ", returning null object instead");
			return null;
		}
	}
}
