package gereralPredicates;

import java.util.function.Predicate;

/**
 * predicate to test object t for being smaller than reference value.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class IsSmaller implements Predicate<Object>
{
	private Double _object;

	public IsSmaller(Double object)
	{
		if ( object instanceof Double )
			this._object = (Double) object;
		else
			this._object = Double.valueOf( String.valueOf(object));
	}

	@Override
	public boolean test(Object t) 
	{
		return (this._object > Double.valueOf( String.valueOf( t ) ) );
	}
	
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return " < " + _object;
	}
}