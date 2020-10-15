package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import gui.GuiConsole;
import idynomics.Global;
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
		 * Only for critical (error) messages.
		 */
		CRITICAL,
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
	}
	
	/**
	 * Current output level setting.
	 */
	private static Tier _outputLevel;
	
	private static Tier _screenLevel;
	
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
	 * Allows to suspend writing to file (for instance we do not need to create
	 * a log for every time we open a file, just when we are actually run a sim
	 */
	private static boolean suspend = true;
	
	/**
	 * Keep suspended output here for when we switch suspend of and can write
	 * this to file.
	 */
	private static String suspendOut = "";
	
	/**
	 * 
	 */
	private static boolean terminalOutput = false;
	
	/**
	 * After this call the log is written to file. Keeping log files for any
	 * Log.out call will result in a large amount of output files when simply
	 * looking at simulation states. Instead it is more useful to log when
	 * actual simulations are ran.
	 */
	public static void keep() 
	{
		if (Global.write_to_disc)
		{
			suspend = false;
			if ( !_logFile.isReady() )
				setupFile();
			_logFile.write(suspendOut);
		}
	}
	
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
	 * @return String representation of the log file's output level.
	 */
	public static String level()
	{
		return String.valueOf(_outputLevel);
	}
	
	/**
	 * \brief Set the output level and create the log file. This method should
	 * be called before any output is created. If output is written before set
	 * is called the level will be set to NORMAL.
	 * 
	 * @param level Output level to set to.
	 */
	public static void set(Tier level)
	{
		_outputLevel = level;
		if ( Idynomics.global.outputLocation != null && !_logFile.isReady() && 
				!suspend )
			setupFile();
		if (Log.shouldWrite(Tier.DEBUG))
			Log.out(Tier.DEBUG, "Log output level was set to " + 
					level.toString());
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
	 * \brief Check if the given output level should be written to log.
	 * 
	 * <p>Use this for computational efficiency: composing strings is more
	 * expensive than comparing enums.</p>
	 * 
	 * @param level Tier of output level.
	 * @return {@code true} if this should be written, {@code false} if it
	 * should not.
	 */
	public static boolean shouldWrite(Tier level)
	{
		if ( _outputLevel == null )
		{
			 _outputLevel = Tier.NORMAL;
			 set(Tier.NORMAL);
			 out(Tier.NORMAL,"Reporting at default log level " + _outputLevel +
					 " as simulation level was not yet identified.");
			 /* NOTE: This would only happen if a out call is made before the
			  * log is properly set up (and thus set(Tier) has not yet been
			  * called. Please use System.out.println() for any important
			  * messages before log setup, the log is the first thing to be set
			  * up so this should almost never occur.  */
		}
		return ( level.compareTo(_outputLevel) < 1 );
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
		if (message != null)
		{
			/* Set up the file if this hasn't been done yet (e.g. GUI launch). */
			if ( !_logFile.isReady() && !suspend )
				setupFile();
			/* Try writing to screen and to the log file. */
			if ( shouldWrite(level) )
			{
				printToScreen(_st.format(new Date())+message, level==Tier.CRITICAL);
				if ( suspend )
					suspendOut += _ft.format(new Date()) + message + "\n";
				else
					_logFile.write(_ft.format(new Date()) + message + "\n");
				if ( terminalOutput && Helper.isSystemRunningInGUI )
					System.out.println( _st.format(new Date()) + message + "\n");
			}
		}
	}
	
	/**
	 * \Default out can be on Normal Tier.
	 * @param message
	 */
	public static void out(String message)
	{
		out(Tier.NORMAL, message);
	}
	
	/**
	 * \brief Create the log file and get it ready to write.
	 */
	public static void setupFile()
	{
		_logFile.fnew(Idynomics.global.outputLocation + "/log.txt");
		_logFile.flushAll();
		if( shouldWrite(Tier.NORMAL))
			out(Tier.NORMAL, Idynomics.fullDescription() + 
					"\nOutput level is " + _outputLevel +
					", starting at " + _ft.format(new Date()) + 
					"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
					+ "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
	
	/**
	 * \brief Print a message to console screen, ignoring the log file.
	 * 
	 * @param message String message to display.
	 * @param isError True if this message should be highlighted to the user.
	 */
	// TODO move this method to Helper?
	public static void printToScreen(String message, boolean isError)
	{
		/* Disabled line as this creates line-breaks even if the line-break is 
		 * already included in the string message.
		 * 
		 * message = Helper.limitLineLength(message, 80, "");
		 */
		if ( Helper.isSystemRunningInGUI )
		{
			if ( isError )
				GuiConsole.writeErr(message + "\n");
			else
				GuiConsole.writeOut(message + "\n");
		}
		else
		{
			if ( isError )
			{
				System.err.println(message);
			}
			else
				System.out.println(message);
		}
	}
	
	public static void step()
	{
		GuiConsole.scroll();
	}
}
