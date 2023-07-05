package debugTools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import debugTools.Testable.TestMode;
import instantiable.Instance;
import test.junit.newTests.CollisionEvaluation;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class Tester {
	
	public static boolean verbose = true;
	
	public static void main(String[] args)
	{
		/* If no arguments are given run an example test */
		if (args.length == 0)
			args = new String[] { TestMode.CONSOLE.toString(), 
					CollisionEvaluation.class.getName() };
		
		/* First argument is the test mode */
		TestMode mode = TestMode.valueOf(args[0]);
		String[] tests = Arrays.copyOfRange(args, 1, args.length);
		
		/* We assume all arguments 1 to n are valid Testables, if not one of the
		 * above exceptions is thrown.  */
		for (String a : tests)
		{
			try {
				test(( Testable) Instance.getNew( 
						Class.forName(a).getName(), null ), mode );
			} catch (ClassNotFoundException e) {
				System.out.println("Could not find class " + a);
			}
		}
	}

	/**
	 * Method calls the test method of the Testable and passes the test Mode
	 * @param testable
	 * @param mode
	 */
	public static void test(Testable testable, TestMode mode)
	{
		testable.test(mode);
	}
	
	/**
	 * Assess whether the 2 inputs are equal, depends on test mode, the input
	 * class must implement a sensible equals(T) method.
	 * 
	 * @param result - the test result
	 * @param expected - the expected value
	 * @param mode - the test mode (eg unit-test or console)
	 */
	public static <T> void assess(T result, T expected, TestMode mode)
	{
		assess(result, expected, mode, "");
	}
	
	public static <T> void assess(double result, double expected, 
			double errorTolerance, TestMode mode)
	{
		assess(result, expected, errorTolerance, mode, "");
	}
	
	/**
	 * Assess whether the 2 inputs are equal, depends on test mode, the input
	 * class must implement a sensible equals(T) method.
	 * 
	 * @param result - the test result
	 * @param expected - the expected value
	 * @param mode - the test mode (eg unit-test or console)
	 * @param description - short description of the test
	 */
	public static <T> void assess(T result, T expected, TestMode mode, 
			String desription)
	{
		switch(mode) 
		{
			case UNIT:
				assertEquals( result, expected);
				if ( !Tester.verbose )
					break;
			default:
				if( result.equals(expected) )
					println(" pass" +  " (" + desription + ")", mode);
				else
					println(" fail" + " (" + desription + ")", mode);
		}
	}
	
	/**
	 * (for double types only)
	 * @param <T>
	 * @param result
	 * @param expected
	 * @param errorTolerance
	 * @param mode
	 * @param desription
	 */
	public static <T> void assess(double result, double expected, 
			double errorTolerance, TestMode mode, String desription)
	{
		switch(mode) 
		{
			case UNIT:
				assertEquals( result, expected, errorTolerance);
				if ( !Tester.verbose )
					break;
			default:
				if( assessDoubles(result,  expected, errorTolerance) )
					println(" pass" +  " (" + desription + ")", mode);
				else
					println(" fail" + " (" + desription + ")", mode);
		}
	}
	private static boolean assessDoubles(double result, double expected, 
			double errorTolerance)
	{
		return (result - expected < errorTolerance || 
				expected - result < errorTolerance);
	}
	/**
	 * Quick println method that can change behavior based on TestMode.
	 * @param msg
	 * @param mode
	 */
	public static void println(String msg, TestMode mode)
	{
		print(msg + "\n", mode);
	}
	/**
	 * Quick print method that can change behavior based on TestMode.
	 * @param msg
	 * @param mode
	 */
	public static void print(String msg, TestMode mode)
	{
		switch(mode) 
		{
			case UNIT:
				if ( !Tester.verbose )
					break;
			default:
				System.out.print( msg );
		}
	}
}
