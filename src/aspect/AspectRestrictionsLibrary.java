/**
 * 
 */
package aspect;

import java.util.function.Predicate;

/**
 * @author cleggrj
 *
 */
public final class AspectRestrictionsLibrary
{
	public static final Predicate<Integer> positiveInt()
	{
		return d -> d > 0;
	}
	
	public static final Predicate<Double> positiveDbl()
	{
		return d -> d > 0.0;
	}
	
	public static final Predicate<Double> nonNegativeDbl()
	{
		return d -> d >= 0.0;
	}
	
	
	
	public static Predicate<Double> greaterThan(Double minimum)
	{
		return d -> d > minimum;
	}
}
