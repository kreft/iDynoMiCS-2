package utility;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.ExpressionB;
import guiTools.GuiConsole;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Helper
{
	/**
	 * 
	 */
	public static boolean gui = false;

	/**
	 * Obtain user input as string.
	 * @param input
	 * @param description
	 * @param noLog
	 * @return
	 */
	public static String obtainInput(String input, String description, boolean noLog)
	{
		if ( input == null || input == "" )
		{
			String msg = description;
			
			if ( gui )
			{
				input = GuiConsole.requestInput(msg);
			} 
			else
			{
				@SuppressWarnings("resource")
				Scanner user_input = new Scanner( System.in );

				if ( noLog )
					System.out.println(msg);
				else
					Log.out(Tier.CRITICAL, msg);
				input = user_input.next( );
			}
			msg = "Aquired input: " + input;
			if ( noLog )
				System.out.println(msg);
			else
				Log.out(Tier.CRITICAL, msg);
		}
		return input;
	}
	
	/**
	 * obtain input with a limited set of options
	 * @param options
	 * @param description
	 * @param noLog
	 * @return
	 */
	public static String obtainInput(List<String> options, String description, boolean noLog)
	{
		String[] out = new String[options.size()];
		int i = 0;
		for (String s : options)
			out[i++] = s;
		return obtainInput(out, description, noLog);
	}
	
	/**
	 * obtain input with a limited set of options
	 * @param options
	 * @param description
	 * @param noLog
	 * @return
	 */
	public static String obtainInput(String[] options, String description, boolean noLog)
	{
		String input;
		String msg = description;
		
		if ( gui )
		{
			input = GuiConsole.requestInput(options, msg);
		} 
		else
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );

			if ( noLog )
				System.out.println(msg);
			else
				Log.out(Tier.NORMAL, msg);
			input = user_input.next( );
		}
		msg = "Aquired input: " + input;
		if ( noLog )
			System.out.println(msg);
		else
			Log.out(Tier.NORMAL, msg);
	
	return input;
	}
	
	/**
	 * obtain user input as string with logging on.
	 * @param input
	 * @param description
	 * @return
	 */
	public static String obtainInput(String input, String description)
	{
		return obtainInput(input, description, false);
	}
	
	/**
	 * obtain yes/no user input
	 * @param description
	 * @param noLog
	 * @return
	 */
	public static boolean obtainInput(String description, boolean noLog)
	{
		String input = obtainInput(new String[] { "yes", "no" } , description, 
				noLog);
		if ( confirmation( input ) )
			return true;
		else if ( rejection( input ) )
			return false;
		else if ( input == null )
			return false;
		else
		{
			Log.out(Tier.QUIET, "User input was not recognised, try:\n"
					+ "[Confirming] \n" + 
					Helper.stringAToString(confirmations()) + "\n"
					+ "[Rejections] \n" +
					Helper.stringAToString(rejections()));
			return obtainInput(description, noLog);	
		}
	}
	
	/**
	 * list of known confirmations.
	 * @return
	 */
	public static String[] confirmations()
	{
		return new String[] {
				"yes",
				"y",
				"Y",
				"true",
				"TRUE"				
		};
	}
	
	/**
	 * List of known rejections
	 * @return
	 */
	public static String[] rejections()
	{
		return new String[] {
				"no",
				"n",
				"N",
				"false",
				"FALSE"
		};
	}
	
	/**
	 * check whether user input is a confirmation
	 * @param input
	 * @return
	 */
	public static boolean confirmation(String input)
	{
		for ( String s : confirmations() )
			if ( s == input )
				return true;
		return false;
	}
	
	/**
	 * Check whether user input is a rejection
	 * @param input
	 * @return
	 */
	public static boolean rejection(String input)
	{
		for ( String s : rejections() )
			if ( s == input )
				return true;
		return false;
	}
	
	/**
	 * return string interpretation of mathematical expression.
	 * @param expression
	 * @return
	 */
	public static double interpretExpression(String expression)
	{
		return Double.parseDouble(expression);
//		ExpressionB expres = new ExpressionB(expression);
//		return expres.getValue();
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
	
	public static <T> boolean isNone( T input )
	{
		if (input == null || input == "")
			return true;
		else
			return false;
	}
	
	
	/**
	 * Delayed abort allows user to read abort statement before shutdown
	 * @param delay
	 */
	public static void abort(int delay) {
		Log.out(Tier.CRITICAL, "Aborting..");
		pause(delay);
		System.exit(0);
	}
	
	/**
	 * pause the current thread by delay
	 * @param delay
	 */
	public static void pause(int delay)
	{
		try {
		    Thread.sleep(delay);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * write enum to string space separation
	 * @param anEnum
	 * @return
	 */
	public static String enumToString(Class<?> anEnum)
	{
		Object[] enums = anEnum.getEnumConstants();
		String out = "";
		for ( Object o : enums )
			out += o.toString()+" ";
		return out;	
	}
	
	public static String[] enumToStringArray(Class<?> anEnum)
	{
		Object[] enums = anEnum.getEnumConstants();
		String[] out = new String[enums.length];
		int i = 0;
		for ( Object o : enums )
			out[i++] = o.toString();
		return out;	
	}
	
	/**
	 * Write String array to comma separated string
	 * @param array
	 * @return
	 */
	public static String stringAToString(String[] array)
	{
		String out = "";
		if (array != null)
		{
			for ( String o : array )
				out += o+",";
			return out.substring(0, out.length()-1);
		}
		else
			 return out;
		
	}
	
	/**
	 * convert first character of String to uppercase.
	 * @param string
	 * @return
	 */
	public static String firstToUpper(String string)
	{
		String firstLetter = string.substring(0, 1);
		if ( firstLetter == firstLetter.toLowerCase() )
			string = firstLetter.toUpperCase() + string.substring(1);
		return string;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param classes
	 * @return
	 */
	public static String[] getClassNamesSimple(Class<?>[] classes)
	{
		int num = classes.length;
		String[] out = new String[num];
		String str;
		int dollarIndex;
		for ( int i = 0; i < num; i++ )
		{
			str = classes[i].getName();
			dollarIndex = str.indexOf("$");
			out[i] = str.substring(dollarIndex+1);
		}
		return out;
	}

	/**
	 * Convert a java List of strings to a String array
	 * @param all
	 * @return
	 */
	public static String[] listToArray(List<String> all) {
		String[] out = new String[all.size()];
		for (int i = 0; i < all.size(); i++)
			out[i] = all.get(i);
		return out;
	}
	
	/**
	 * Convert a java Set of strings to a String array
	 * @param all
	 * @return
	 */
	public static String[] setToArray(Set<String> all) {
		String[] out = new String[all.size()];
		int i =0;
		for (String s : all)
		{
			out[i] = s;
			i++;
		}
		return out;
	}
	
	/**
	 * Convert a java Set of strings to a String array
	 * @param all
	 * @return
	 */
	public static String[] collectionToArray(Collection<String> all) {
		String[] out = new String[all.size()];
		int i =0;
		for (String s : all)
		{
			out[i] = s;
			i++;
		}
		return out;
	}
	
	/**
	 * \brief Calculate the sum of all {@code Double} values in a map.
	 * 
	 * @param map Map where the values are real numbers.
	 * @return The sum of all the values.
	 */
	public static double totalValue(Map<String,Double> map)
	{
		double out = 0.0;
		for ( Double x : map.values() )
			out += x;
		return out;
	}
}
