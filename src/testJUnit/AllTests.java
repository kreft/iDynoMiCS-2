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
@SuiteClasses({ LinearAlgebraTest.class, XMLableTest.class })
public class AllTests {

}
