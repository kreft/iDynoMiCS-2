package gereralPredicates;

import java.util.function.Predicate;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 *
 */
public class IsSame implements Predicate<Object>
{
	private Object _object;

	public IsSame(Object object)
	{
		this._object = object;
	}

	@Override
	public boolean test(Object t) 
	{
		return (_object.equals(t));
	}
	
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return " = " + _object.toString();
	}

}
