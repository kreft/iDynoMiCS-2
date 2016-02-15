package utility;

import java.util.Scanner;

import dataIO.Log;
import dataIO.Log.tier;

/**
 * 
 * @author baco
 *
 */
public class Helper {

	/**
	 * Assisting with badly written protocol files.
	 */
	public static String obtainInput(String input, String description)
	{
		if ( input == null || input == "" )
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );
			Log.out(tier.CRITICAL," Additional input argument required: " + 
			description + ", please enter a value: " );
			input = user_input.next( );
			Log.out(tier.CRITICAL, "Aquired input: " + input);
		}
		return input;
	}
	
	/**
	 * Returns any input object <T> from input, if not set returns ifNone <T>.
	 */
	public static <T> T setIfNone(T input, T ifNone)
	{
		if (input == null || input == "")
			return ifNone;
		else
			return input;
	}

	/**
	 * Delayed abort allows user to read abort statement before shutdown
	 * @param delay
	 */
	public static void abort(int delay) {
		Log.out(tier.CRITICAL, "Aborting..");
		try {
		    Thread.sleep(delay);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		System.exit(0);
	}
}
