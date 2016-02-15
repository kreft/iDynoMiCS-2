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
	public static String obtainInput(String input, String description, boolean noLog)
	{
		if ( input == null || input == "" )
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );
			
			String msg = "Additional input argument required: " + 
					description + ", please enter a value: ";
			if(noLog)
				System.out.println(msg);
			else
				Log.out(tier.CRITICAL, msg);
			
			input = user_input.next( );
			
			msg = "Aquired input: " + input;
			if(noLog)
				System.out.println(msg);
			else
				Log.out(tier.CRITICAL, msg);
		}
		return input;
	}
	
	public static String obtainInput(String input, String description)
	{
		return obtainInput(input, description, false);
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
	 * \brief TODO
	 * 
	 * @param input
	 * @param ifNone
	 * @return
	 */
	public static double getDouble(Object input, double ifNone)
	{
		if ( input == null || input == "" )
			return getDouble(input);
		else
			return ifNone;
	}
	
	/**
	 * Delayed abort allows user to read abort statement before shutdown
	 * @param delay
	 */
	public static void abort(int delay) {
		Log.out(tier.CRITICAL, "Aborting..");
		pause(delay);
		System.exit(0);
	}
	
	public static void pause(int delay)
	{
		try {
		    Thread.sleep(delay);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	public static String enumToString(Class<?> anEnum)
	{
		Object[] enums = anEnum.getEnumConstants();
		String out = "";
		for(int i = 0; i < enums.length; i++)
			out += enums[i].toString() + " ";	
		return out;	
	}
}
