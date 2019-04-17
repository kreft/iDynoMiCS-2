package utility;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import gui.GuiConsole;
import idynomics.Idynomics;
import linearAlgebra.Vector;

/**
 * \brief Utilities class of helpful methods used across iDynoMiCS 2.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */
public final class Helper
{
	/**
	 * Single scanner for user input.
	 */
	private static Scanner USER_INPUT;
	
	/**
	 * List of recognised words that signal confirmation by the user.
	 */
	public final static String[] confirmations = new String[] 
			{ "yes", "y", "Y", "true", "TRUE" };
	
	/**
	 * List of recognised words that signal rejection by the user.
	 */
	public final static String[] rejections = new String[]
			{ "no", "n", "N", "false", "FALSE" };
	
	/**
	 * Boolean denoting whether the simulation is running in Graphical User
	 * Interface (GUI) mode.
	 */
	public static boolean isSystemRunningInGUI = false;
	
	public static final String[] DIFFERENTIATING_PALETTE = new String[] { 
			"cyan", "magenta", "yellow", "blue", "red", "green", "violet",
			"orange", "springgreen", "azure", "pink", "chartreuse", "black" };
	
	/**
	 * https://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29
	 * regex expressions for checking for double.
	 */
	private static final String Digits     = "(\\p{Digit}+)";
	private static final String HexDigits  = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally 
	// signed decimal integer.
	private static final String Exp        = "[eE][+-]?"+Digits;
	private static final String fpRegex    =
	    ("[\\x00-\\x20]*"+ // Optional leading "whitespace"
	    "[+-]?(" +         // Optional sign character
	    "NaN|" +           // "NaN" string
	    "Infinity|" +      // "Infinity" string

	    // A decimal floating-point string representing a finite positive
	    // number without a leading sign has at most five basic pieces:
	    // Digits . Digits ExponentPart FloatTypeSuffix
	    // 
	    // Since this method allows integer-only strings as input
	    // in addition to strings of floating-point literals, the
	    // two sub-patterns below are simplifications of the grammar
	    // productions from the Java Language Specification, 2nd 
	    // edition, section 3.10.2.

	    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
	    "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

	    // . Digits ExponentPart_opt FloatTypeSuffix_opt
	    "(\\.("+Digits+")("+Exp+")?)|"+

	    // Hexadecimal strings
	    "((" +
	    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
	    "(0[xX]" + HexDigits + "(\\.)?)|" +

	    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
	    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

	    ")[pP][+-]?" + Digits + "))" +
	    "[fFdD]?))" +
	    "[\\x00-\\x20]*");// Optional trailing "whitespace"
	
	public static String[] giveMeAGradient(int length)
	{
		length = Helper.restrict(length, 256, 2);
		String[] colors = new String[length];
		int step = 256 / length;
		for (int i = 0; i < length; i++)
		{
			int c = (255 - i * step);
			colors[i] = "rgb(" + c + ", " + c + ", " + c + ")";
		}
		return colors;
	}
	
	/**
	 * \brief restrict int to bounds min and max
	 * @param a
	 * @param max
	 * @param min
	 * @return
	 */
	public static int restrict(int a, int max, int min) 
	{
		return Math.max( Math.min(a, max), min );
	}

	/**
	 * \brief Obtain user input as string.
	 * 
	 * @param input What the system currently believes to be the input. This
	 * method will only act if this is null or empty.
	 * @param description Descriptive message to tell the user what input is
	 * required.
	 * @param shouldLogMessage Boolean stating whether this interaction should 
	 * be added to the log (true) or printed to screen (false).
	 * @return The requested input as a string.
	 */
	public static String obtainInput(String input,
			String description, boolean shouldLogMessage)
	{
		if ( isNullOrEmpty(input) || input == "null" )
		{
			if ( isSystemRunningInGUI )
				input = GuiConsole.requestInput(description);
			else
			{
				@SuppressWarnings("resource")
				Scanner user_input = new Scanner( System.in );
				if ( shouldLogMessage )
					Log.out(Tier.CRITICAL, description);
				else
					System.out.println(description);
				input = user_input.next( );
			}
			/* Confirm the input received. */
			String msg = "Aquired input: " + input;
			if ( shouldLogMessage )
				Log.out(Tier.CRITICAL, msg);
			else
				System.out.println(msg);
		}
		return input;
	}
	
	/**
	 * \brief Obtain user input as string from a limited set of options.
	 * 
	 * @param options List of options for the input that the user may choose
	 * from.
	 * @param description Descriptive message to tell the user what input is
	 * required.
	 * @param shouldLogMessage Boolean stating whether this interaction should 
	 * be added to the log (true) or printed to screen (false).
	 * @return The requested input as a string.
	 */
	public static String obtainInput(Collection<String> options,
			String description, boolean shouldLogMessage)
	{
		String[] out = new String[options.size()];
		int i = 0;
		for (String s : options)
			out[i++] = s;
		return obtainInput(out, description, shouldLogMessage);
	}
	
	public static String obtainInput(Object[] options,
			String description, boolean shouldLogMessage)
	{
		String[] out = new String[options.length];
		int i = 0;
		for (Object s : options)
			out[i++] = String.valueOf(s);
		return obtainInput(out, description, shouldLogMessage);
	}
	
	/**
	 * \brief Obtain user input as string from a limited set of options.
	 * 
	 * @param options List of options for the input that the user may choose
	 * from.
	 * @param description Descriptive message to tell the user what input is
	 * required.
	 * @param shouldLogMessage Boolean stating whether this interaction should 
	 * be added to the log (true) or printed to screen (false).
	 * @return The requested input as a string.
	 */
	public static String obtainInput(String[] options,
			String description, boolean noLog)
	{
		String input;
		if ( isSystemRunningInGUI )
		{
			input = GuiConsole.requestInput(options, description);
		} 
		else
		{
			/* Get a scanner if not already open. */
			if ( USER_INPUT == null )
				USER_INPUT = new Scanner( System.in );
			/* Read in the user input. */
			if ( noLog )
			{
				System.out.println(description);
				for ( String option : options )
					System.out.println("\t"+option);
			}
			else
			{
				Log.out(Tier.CRITICAL, description);
				for ( String option : options )
					Log.out(Tier.CRITICAL, "\t"+option);
			}
			input = USER_INPUT.next();
		}
		
		String msg = "Aquired input: " + input;
		if ( noLog )
			System.out.println(msg);
		else
			Log.out(Tier.NORMAL, msg);
	
	return input;
	}
	
	/**
	 * obtain user input as string with logging on.
	 * 
	 * @param input
	 * @param description Message describing the input needed.
	 * @return
	 */
	public static String obtainInput(String input, String description)
	{
		return obtainInput(input, description, true);
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
					Helper.stringAToString(confirmations) + "\n"
					+ "[Rejections] \n" +
					Helper.stringAToString(rejections));
			return obtainInput(description, noLog);	
		}
	}
	
	/**
	 * check whether user input is a confirmation
	 * @param input
	 * @return
	 */
	public static boolean confirmation(String input)
	{
		for ( String s : confirmations )
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
		for ( String s : rejections )
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
	
	public static <T> boolean isNullOrEmpty( T input )
	{
		return (input == null || input == "");
	}
	
	
	public static String obtainIfNone(String input, String description,
			boolean shouldLogMessage, Collection<String> options)
	{
		if( isNullOrEmpty(input) )
			return obtainInput(options, description, shouldLogMessage);
		else 
			return input;
	}
	
	/**
	 * Delayed abort allows user to read abort statement before shutdown
	 * @param delay
	 */
	public static void abort(int delay)
	{
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
	
	public static String stringAToString(String[] array, String delim)
	{
		String out = "";
		if (array != null)
		{
			for ( String o : array )
				out += o+delim;
			return out.substring(0, out.length()-1);
		}
		else
			 return out;
		
	}
	
	public static String[] concatinate(String[] in, String... extra)
	{
		String[] out = new String[ in.length + extra.length ];
		for ( int i = 0; i < in.length; i++)
			out[i] = in[i];
		for ( int i = 0; i < extra.length; i++)
			out[i+in.length] = extra[i];
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
	
	public static double totalMass(Object massObject)
	{
		double totalMass = 0.0;
		if ( massObject instanceof Double )
			totalMass = (double) massObject;
		else if ( massObject instanceof Double[] )
		{
			Double[] massArray = (Double[]) massObject;
			for ( Double m : massArray )
				totalMass += m;
		}
		else if ( massObject instanceof Map )
		{
			// TODO assume all mass types used unless specified otherwise
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) massObject;
			totalMass = Helper.totalValue(massMap);
		}
		else
		{
			// TODO safety?
		}
		return totalMass;
	}
	
	public static boolean compartmentAvailable()
	{
		if (Idynomics.simulator == null || 
				! Idynomics.simulator.hasCompartments() )
		{
			Log.printToScreen("No compartment available", false);
			return false;
		}
		else
			return true;
	}
	
	public static String head(String string)
	{
		String[] lines = string.split("\n");
		String out = "[ lines: " + lines.length + " ]\n";
		for ( int i = 0; i < 5 && i < lines.length; i++)
			out += lines[i] + "\n";
		return out;
	}
	
	public static Color obtainColor(String settingName, Properties properties, 
			String defaultCol)
	{
		return obtainColor( Helper.setIfNone( 
				properties.getProperty( settingName ), defaultCol ) );
	}
	
	public static Color obtainColor(String colorString )
	{
		/* color vector, get color from config file, use default if not 
		 * specified */
		int[] color = Vector.intFromString( colorString );
		
		/* return as color */
		return new Color( color[0], color[1], color[2] );
	}
	
	/**
	 * \brief Select a spatial compartment from the simulator, asking for the
	 * user's help if necessary.
	 * 
	 * @return A spatial compartment in the simulator.
	 */
	public static Compartment selectSpatialCompartment()
	{
		/* Identify the spatial compartments. */
		List<String> names = Idynomics.simulator.getSpatialCompartmentNames();
		Compartment c = null;
		if ( names.isEmpty() )
		{
			/* Abort if no spatial compartment is available */
			Log.printToScreen("No spatial compartments available.", false);
		}
		else if ( names.size() == 1 )
		{
			/* Return first compartment if only one is available. */
			c = Idynomics.simulator.getCompartment(names.get(0));
		}
		else
		{
			/* Otherwise ask for user input. */
			String s = Helper.obtainInput(names, "select compartment", false);
			c = Idynomics.simulator.getCompartment(s);
		}
		return c;
	}
	
	/**
	 * \brief Select a compartment from the simulator, asking for the user's
	 * help if necessary.
	 * 
	 * @return A compartment in the simulator.
	 */
	public static Compartment selectCompartment()
	{
		/* Identify all the compartments. */
		List<String> names = Idynomics.simulator.getCompartmentNames();
		if ( names.isEmpty() )
		{
			/* Return first compartment if only one is available. */
			Log.printToScreen("No compartments available.", false);
			return null;
		}
		else if ( names.size() == 1 )
		{
			/* render directly if only 1 compartment is available */
			return Idynomics.simulator.getCompartment(names.get(0));
		}
		else
		{
			/* otherwise ask for user input */
			String s = Helper.obtainInput(names, "select compartment", false);
			return Idynomics.simulator.getCompartment(s);
		}
	}
	
	public static String removeWhitespace(String input)
	{
		return input.replaceAll("\\s+","");
	}
	
	/**
	 * Submit command to kernel
	 * @param command
	 * @return
	 */
	public static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();
		Process p;
		
		try {
			p = Runtime.getRuntime().exec( command );
			p.waitFor();
			BufferedReader reader = new BufferedReader( 
					new InputStreamReader( p.getInputStream() ) );

            String line = "";
			while ( (line = reader.readLine() ) != null ) 
			{
				if (Log.shouldWrite(Tier.NORMAL) )
						Log.out(Tier.NORMAL, line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();
	}
	
	public static String[] copyStringA(String[] in)
	{
		String[] out = new String[in.length];
		for ( int i = 0; i < in.length; i++ )
			out[i] = in[i];
		return out;
	}
	
	public static String limitLineLength(String in, int length, String linePre)
	{
		String out = "";
		Pattern p = Pattern.compile( "\\G\\s*(.{1," +length+ "})(?=\\s|$)", 
				Pattern.DOTALL);
		Matcher m = p.matcher( in );
		while (m.find())
		    out += linePre + m.group(1) + "\n";
		return out.substring(0, out.length()-1);
	}

	public static String[] subset( String[] in, int start, int stop)
	{
		String[] out = new String[stop-start];
		for ( int i = start; i < stop; i++ )
			out[i-start] = in[i];
		return out;
	}
	
	/**
	 * return next available key that is not already present in integer 
	 * key set
	 * 
	 * @param target
	 * @param keySet
	 * @return
	 */
	public static int nextAvailableKey(int target, Collection<Integer> keySet)
	{
		if (keySet.contains(target))
			return nextAvailableKey( target+1 , keySet );
		return target;
	}
	
	/**
	 * Checks if the passed string can be parsed to double or not.
	 * @param strParse String to be checked
	 * @return true or false
	 */
	public static boolean dblParseable(String strParse)
	{
		if (Pattern.matches(fpRegex, strParse)){
		    return true;
		}
		return false;
	}
	
	public static boolean boolParseable(String strParse)
	{
		for( String s : Helper.rejections)
			if (s.equals(strParse))
				return true;
		for( String s : Helper.confirmations)
			if (s.equals(strParse))
				return true;
		return false;
	}
}
