/**
 * 
 */
package testJUnit;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * @author cleggrj
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ LinearAlgebraTest.class,
				XMLableTest.class,
				RateExpressionTest.class })
public class AllTests
{
	final static double TOLERANCE = 1E-6;
}
