package debugTools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import debugTools.Testable.TestMode;
import instantiable.Instance;
import test.junit.oldTests.CollisionTest;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class Tester {
	
	public static void main(String[] args)
	{
		/* If no arguments are given run an example test */
		if (args.length == 0)
			args = new String[] { TestMode.CONSOLE.toString(), 
					CollisionTest.class.getName() };
		
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
	 * @param result
	 * @param expected
	 * @param mode
	 */
	public static <T> void assess(T result, T expected, TestMode mode)
	{
		switch(mode) 
		{
			case UNIT:
				assertEquals( result, expected);
				break;
			default:
				if( result.equals(expected) )
					System.out.println(" pass");
				else
					System.out.println(" fail");
		}
	}
	
	/**
	 * Quick println method that can change behavior based on TestMode.
	 * 
	 * @param msg
	 * @param mode
	 */
	public static void println(String msg, TestMode mode)
	{
		print(msg + "\n", mode);
	}
	
	/**
	 * Quick print method that can change behavior based on TestMode.
	 * 
	 * @param msg
	 * @param mode
	 */
	public static void print(String msg, TestMode mode)
	{
		switch(mode) 
		{
			case UNIT:
				break;
			default:
				System.out.print( msg );
		}
	}
}
