package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import idynomics.Idynomics;
import idynomics.Param;

/**
 * Static class that manages Log and console output, various levels are included
 * to increase or decrease the extend of feedback, a set Feedback level setting 
 * will also include messages of all lower level settings.
 * 
 * @author baco
 *
 */
public class Feedback {
	
	/**
	 * levels of log expressiveness, a Feedback level setting will also include
	 * messages of all lower level settings.
	 * @author baco
	 *
	 */
	public enum LogLevel {
		SILENT, 
		// Should generate no messages, no message should have this output level
		CRITICAL,
		// Only critical (error) messages
		QUIET, 
		// minimal simulation information.
		NORMAL, 
		// Messages for a normal simulation
		EXPRESSIVE, 
		// Elaborate information of the simulation
		DEBUG, 
		// Debug messages
		BULK 
		// Bulk messages that are probably not needed, the messages that would 
		// create too much bulk for normal debug mode.
	}
	
	/**
	 * current output level setting
	 */
	private static LogLevel outputLevel;
	
	/**
	 * logFile handler
	 */
	private static FileHandler logFile = new FileHandler();
	
	/**
	 * Date format
	 */
	private static SimpleDateFormat ft = 
			new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ");
	
	/**
	 * Short date format
	 */
	private static SimpleDateFormat st = new SimpleDateFormat("[HH:mm] ");
	
	/**
	 * Set the output level and create the log file. This method should be
	 * called before any output is created. If output is written before set is
	 * called the level will be set to NORMAL.
	 * @param level
	 */
	public static void set(LogLevel level)
	{
		logFile.fnew(Param.outputLocation + "/log.txt");
		outputLevel = level;
		out(dataIO.Feedback.LogLevel.QUIET, "iDynoMiCS " + 
		Idynomics.version_number  + " "	+ Idynomics.version_description + 
				"\nOutput level is " + outputLevel.toString() + ", starting at " + 
				ft.format(new Date()) + "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
	
	/**
	 * Set Feedback class from string.
	 * @param level
	 */
	public static void set(String level)
	{
		set(LogLevel.valueOf(level));
	}
	
	/**
	 * Log if the message level is lower or equal level setting to the output
	 * level.
	 * NOTE: Bas [03.02.16] we may want to also write extensive messages per
	 * individual time step in a separate time step log file to include a 
	 * detailed log of what happened during that step.
	 * @param message
	 * @param level
	 */
	public static void out(LogLevel level, String message)
	{
		if (outputLevel == null)
		{
			set(dataIO.Feedback.LogLevel.NORMAL);
		}
		
		if (level.compareTo(outputLevel) < 1)
		{
			if (level == LogLevel.CRITICAL)
				System.err.println(st.format(new Date()) + message);
			else
				System.out.println(st.format(new Date()) + message);
			logFile.write(ft.format(new Date()) + message + "\n");
		}
	}
}
