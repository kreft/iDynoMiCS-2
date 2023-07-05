package gereralPredicates;

import java.util.function.Predicate;

/**
 * predicate to test whether the DOUBLE value of an object matches that of the
 * reference
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class IsEquals implements Predicate<Object>
{
	private Double _object;

	public IsEquals(Double object)
	{
		if ( object instanceof Double )
			this._object = ( Double ) object;
		else
			this._object = Double.valueOf( String.valueOf( object ) );
	}

	@Override
	public boolean test(Object t) 
	{
		return ( this._object.equals( Double.valueOf( String.valueOf( t ) ) ) );
	}
	
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return " = " + _object;
	}
}