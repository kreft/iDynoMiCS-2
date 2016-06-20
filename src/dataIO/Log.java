package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import guiTools.GuiConsole;
import idynomics.Idynomics;
import utility.Helper;

/**
 * Static class that manages Log and console output, various levels are included
 * to increase or decrease the extend of feedback, a set Feedback level setting 
 * will also include messages of all lower level settings.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Log
{
	/**
	 * Levels of log expressiveness, a Feedback level setting will also include
	 * messages of all lower level settings.
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	public enum Tier
	{
		/**
		 * Should generate no messages: no message should have this output
		 * level.
		 */
		// TODO Rob: Do we really want this? CRITICAL seems extreme enough. 
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
	 * Current output level setting.
	 */
	private static Tier _outputLevel;
	
	/**
	 * Log file handler.
	 */
	private static FileHandler _logFile = new FileHandler();
	
	/**
	 * Full date format.
	 */
	private static SimpleDateFormat _ft = 
						new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ");
	
	/**
	 * Short date format.
	 */
	private static SimpleDateFormat _st = new SimpleDateFormat("[HH:mm] ");
	
	/**
	 * \brief Check if this log file is ready to start writing.
	 * 
	 * @return true if it is ready, false if it is not ready.
	 */
	public static boolean isSet()
	{
		return ( _outputLevel != null );
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static String level()
	{
		return String.valueOf(_outputLevel);
	}
	
	/**
	 * Set the output level and create the log file. This method should be
	 * called before any output is created. If output is written before set is
	 * called the level will be set to NORMAL.
	 * 
	 * <p>FIXME Rob [1Mar2016]: If NORMAL is the default, then why does
	 * {@link #out(Tier,String)} have an error statement if it is null?</p>
	 * 
	 * @param level
	 */
	public static void set(Tier level)
	{
		_outputLevel = level;
		if ( Idynomics.global.outputLocation != null &&  ! _logFile.isReady() )
			setupFile();
	}
	
	/**
	 * Set Feedback class from string.
	 * @param level
	 */
	public static void set(String level)
	{
		set(Tier.valueOf(level));
	}
	
	/**
	 * \brief Log if the message level is lower or equal level setting to the
	 * output level.
	 * 
	 * <p>NOTE: Bas [03.02.16] we may want to also write extensive messages per
	 * individual time step in a separate time step log file to include a 
	 * detailed log of what happened during that step.</p>
	 * 
	 * @param level
	 * @param message
	 */
	public static void out(Tier level, String message)
	{
		/* Set up the file if this hasn't been done yet (e.g. GUI launch). */
		if ( ! _logFile.isReady() )
			setupFile();
		/* Try writing to screen and to the log file. */
		if ( _outputLevel == null )
		{
			 _outputLevel = Tier.NORMAL;
			 printToScreen(
					 "No output level set, so using NORMAL be default", true);
			// FIXME this response contradicts the javadoc to set(Tier)
			//printToScreen("Error: attempt to write log before it is set", true);
		}
		else if ( level.compareTo(_outputLevel) < 1 )
		{
			printToScreen(_st.format(new Date())+message, level==Tier.CRITICAL);
			_logFile.write(_ft.format(new Date()) + message + "\n");
		}
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public static void setupFile()
	{
		//FIXME for some reason this sometimes fails with user provided location
		_logFile.fnew(Idynomics.global.outputLocation + "/log.txt");
		_logFile.flushAll();
		out(Tier.QUIET, Idynomics.fullDescription() + 
				"\nOutput level is " + _outputLevel +
				", starting at " + _ft.format(new Date()) + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param message
	 * @param isError
	 */
	// TODO move this method to Helper?
	public static void printToScreen(String message, boolean isError)
	{
		if ( Helper.gui )
		{
			if ( isError )
				GuiConsole.writeErr(message + "\n");
			else
				GuiConsole.writeOut(message + "\n");
		}
		else
		{
			if ( isError )
				System.err.println(message);
			else
				System.out.println(message);
		}
	}
}
