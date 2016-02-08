package utility;

import java.util.Scanner;

import dataIO.Log;
import dataIO.Log.tier;

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
	 * Returns String from input, if not set returns string value of ifNone.
	 */
	public static String setIfNone(String input, Object ifNone)
	{
		if (input == null || input == "")
			input = String.valueOf(ifNone);
		return input;
	}
	
	/**
	 * 
	 */
	public static double getDouble(Object input)
	{
		return Double.valueOf(String.valueOf(input));
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