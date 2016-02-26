package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import idynomics.GuiLaunch;
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
public class Log {
	
	/**
	 * levels of log expressiveness, a Feedback level setting will also include
	 * messages of all lower level settings.
	 * @author baco
	 *
	 */
	public enum tier
	{
		/**
		 * Should generate no messages: no message should have this output
		 * level.
		 */
		SILENT,
		/**
		 * Only for critical (error) messages.
		 */
		CRITICAL,
		/**
		 * Minimal simulation information.
		 */
		QUIET,
		/**
		 * Messages for a normal simulation.
		 */
		NORMAL,
		/**
		 * Elaborate information of the simulation.
		 */
		EXPRESSIVE,
		/**
		 * Debug messages.
		 */
		DEBUG,
		/**
		 * Bulk messages that are probably not needed, the messages that would
		 * create too much bulk for normal debug mode.
		 */
		BULK
	}
	
	/**
	 * current output level setting
	 */
	private static tier outputLevel;
	
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
	
	public static boolean isSet()
	{
		return (outputLevel != null);
	}
	
	/**
	 * Set the output level and create the log file. This method should be
	 * called before any output is created. If output is written before set is
	 * called the level will be set to NORMAL.
	 * @param level
	 */
	public static void set(tier level)
	{
		//FIXME for some reason this sometimes fails with user provided location
		logFile.fnew(Param.outputLocation + "/log.txt");
		logFile.flushAll = true;
		outputLevel = level;
		out(tier.QUIET, "iDynoMiCS " + 
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
		set(tier.valueOf(level));
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
	public static void out(tier level, String message)
	{
		if (outputLevel == null)
		{
			System.err.println("Error attempt to write log before it is set");
		}
		else if (level.compareTo(outputLevel) < 1)
		{
			if (level == tier.CRITICAL)
				System.err.println(st.format(new Date()) + message);
			else
				System.out.println(st.format(new Date()) + message);
			logFile.write(ft.format(new Date()) + message + "\n");
			GuiLaunch.guiWrite(st.format(new Date()) + message + "\n");

		}
	}
}
