package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import idynomics.Param;

/**
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
		SILENT, // Should generate no messages, no message should have this output level
		QUIET, // Only critical messages
		NORMAL, // Messages for a normal simulation
		EXPRESSIVE, // Elaborate information of the simulation
		DEBUG, // Debug messages
		ALL // Bulk messages that are probably not needed, the messages that would create too much bulk for normal debug mode.
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
	private static SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
	
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
		out(dataIO.Feedback.LogLevel.QUIET, "Log start " + ft.format(new Date()) + 
				" output level: " + outputLevel.toString());
	}
	
	/**
	 * Log if the message level is lower or equal level setting to the output
	 * level.
	 * @param message
	 * @param level
	 */
	public static void out(LogLevel level, String message)
	{
		if (outputLevel == null)
			set(dataIO.Feedback.LogLevel.NORMAL);
		if (level.compareTo(outputLevel) < 1)
		{
			System.out.println(message);
			logFile.write(message + "\n");
		}
	}
}