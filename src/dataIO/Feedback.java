package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		QUIET, 
		// Only critical messages
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
			new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
	
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
		out(dataIO.Feedback.LogLevel.QUIET, "Log start " + ft.format(new Date())
				+ " output level: " + outputLevel.toString());
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
			if(Param.outputLevel != null)
				set(LogLevel.valueOf(Param.outputLevel));
			else
				set(dataIO.Feedback.LogLevel.NORMAL);
		}
		
		if (level.compareTo(outputLevel) < 1)
		{
			System.out.println(message);
			logFile.write(message + "\n");
		}
	}
}
