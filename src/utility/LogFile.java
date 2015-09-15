package utility;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogFile
{
	/**
	 * Output stream to which log messages are written.
	 */
	private static FileOutputStream _log;
	
	/**
	 * Format of the date used in logging simulation messages.
	 */
	private static DateFormat _dateFormat =
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Decimal format used in logging simulation messages.
	 */
	private static DecimalFormat _decimalFormat = new DecimalFormat("0.0");
	
	/**
	 * Name of the log file.
	 */
	private static String _name;
	
	private static final int normal = 2;
	
	private static final int loudest = 1;
	
	public static final int quietest = 3;
	
	private static int _verbosity = normal;
	
	public LogFile()
	{
		
	}
	
	/**
	 * 
	 * @param dirName
	 */
	public static void openFile(String dirName)
	{
		try
		{
			/*
			 * Iterate through numbers until the filename is free.
			 */
			int i = 0;
			for ( File f = new File(""); f.exists(); i++ )
				f = new File( _name = dirName+File.separator+"log"+i+".txt" );
			/*
			 * Create the file output stream.
			 */
			_log = new FileOutputStream(_name);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			System.out.println("Failed to create a log file at ");
			System.out.println(_name);
		}
	}
	
	public static void setToNormal()
	{
		_verbosity = normal;
	}
	
	public static void setToLoud()
	{
		_verbosity = loudest;
	}
	
	public static void setToQuiet()
	{
		_verbosity = quietest;
	}
	
	
	/**
	 * 
	 * @param message
	 * @param importance
	 */
	private static void write(String message, int importance)
	{
		if ( importance < _verbosity )
			return;
		try
		{
			System.out.println(message);
			_log.write( _dateFormat.format( Calendar.getInstance().getTime() )
																 .getBytes());
			_log.write( (" : "+message+"\n").getBytes() );
		}
		catch ( Exception e )
		{
			//e.printStackTrace();
			System.out.println("Failed to write into log file");
			//System.out.println(message);
		}
	}
	
	public static void writeLog(String message)
	{
		write(message, normal);
	}
	
	public static void shoutLog(String message)
	{
		write(message, loudest);
	}
	
	public static void whisperLog(String message)
	{
		write(message, quietest);
	}
	
	public static void writeError(String message, Exception e)
	{
		shoutLog(message);
		e.printStackTrace();
	}
}
