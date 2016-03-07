package dataIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * \brief Handles file operations, create folders and files, write output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class FileHandler
{
	/**
	 * The file output stream.
	 */
	private BufferedWriter _output;
	
	/**
	 * TODO Intended usage: giving files in a series unique and sequential
	 * numbers for easy identification.  
	 */
	int fileWriterFileNumber;
	
	/**
	 * Set to true if each line needs to be written to file immediately (for
	 * instance for the log file).
	 */
	public boolean flushAll = false;
	
	/**
	 * \brief Creates directory if it does not exist.
	 * 
	 * @param dir Path to the directory.
	 * @return true if the directory was made new, false if it already exists
	 * or there was a problem creating it. 
	 */
	// TODO what is the purpose of the boolean returned?
	private boolean dirMake(String dir)
	{
		File base = new File(dir);
		if ( base.exists() )
			return false;
		else
		{
			try
			{
				base.mkdir();
		        // NOTE Do not write log before output dir is created
				Log.printToScreen(
					"New directory created " + base.getAbsolutePath(), false);
		        return true;
		    } 
		    catch(SecurityException se)
			{
		    	// NOTE Do not write log before output dir is created.
		    	// NOTE do not print this as an error, as this would cause
		    	// problems in the GUI
		    	Log.printToScreen("Unable to create dir: "+dir+"\n"+se, false);
		    	return false;
		    }
		}
	}
	
	/**
	 * Create (if applicable) and open directory
	 * 
	 * @param dir
	 * @return
	 */
	public boolean dir(String dir)
	{
		return dir(dir, 0);
	}
	
	/**
	 * Walks through folder structure to create the full path
	 * 
	 * @param dir
	 * @param min
	 * @return
	 */
	private boolean dir(String dir, int min)
	{
		String[] folders = dir.split("/");
		String path = "";
		boolean result = false;
		for ( int i = 0; i < folders.length - min; i++ )
		{
			path = path + folders[i] + "/";
			result = dirMake(path);
		}
		return result;
	}
	
	/**
	 * opens file
	 */
	public void fopen(String file)
	{
		//TODO
	}
	
	/**
	 * Create file (overwrites if file already exists)
	 * 
	 * TODO instead of overwriting, we should be using fileWriterFileNumber to
	 * make a new file with unique name.
	 */
	public void fnew(String file)
	{
		if ( file.split("/").length > 1 )
			dir(file, 1);
		try
		{
			File f = new File(file);
			f.delete();
			FileWriter fstream = new FileWriter(f, true);
			this._output = new BufferedWriter(fstream);
		}
		catch (IOException e)
		{
			Log.printToScreen(e.toString(), false);
		}
	}
	
	/**
	 * \brief Write line to file.
	 * 
	 * @param line
	 */
	public void write(String line)
	{
		try
		{
			this._output.write(line);
			if ( this.flushAll )
				this._output.flush();
		}
		catch (IOException e)
		{
			Log.printToScreen(e.toString(), false);
		}
	}
	
	/**
	 * close file
	 */
	public void fclose()
	{
		try
		{
			this._output.flush();
			this._output.close();
		}
		catch (IOException e)
		{
			Log.printToScreen(e.toString(), false);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean isReady()
	{
		return ( this._output != null );
	}
}
