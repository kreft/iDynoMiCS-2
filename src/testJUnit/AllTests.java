/**
 * 
 */
package testJUnit;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LinearAlgebraTest.class,
				XMLableTest.class,
				RateExpressionTest.class,
				CoordinateMapTest.class})
public class AllTests
{
	/**
	 * Numerical tolerance when comparing two {@code double} numbers for
	 * equality.
	 */
	public final static double TOLERANCE = 1E-6;
}
