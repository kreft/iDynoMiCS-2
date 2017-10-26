package optimization.constraint;

import java.util.Collection;
import java.util.function.Predicate;

public interface Constraint extends Predicate<double[]> {
	
	public boolean isUpperBound();
	
	public static boolean allMet(Collection<Constraint> constraints, double[] t)
	{
		for( Constraint c : constraints)
			if ( !c.test(t) )
				return false;
		return true;
	}
}
