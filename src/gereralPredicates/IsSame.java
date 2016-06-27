package gereralPredicates;

import java.util.function.Predicate;

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

}
