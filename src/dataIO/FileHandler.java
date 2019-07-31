package dataIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import dataIO.Log.Tier;
import idynomics.Global;

/**
 * \brief Handles file operations, create folders and files, write output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
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
	int _fileWriterFileNumber;

	/**
	 * Set to true if each line needs to be written to file immediately (for
	 * instance for the log file).
	 */
	protected boolean _flushAll = false;

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
				return true;
			} 
			catch(SecurityException se)
			{
				Log.out(Tier.CRITICAL, "Unable to create dir: "+dir+" "+se);
				return false;
			}
		}
	}

	/**
	 * \brief Set this for each line to be written to file immediately (for
	 * instance for the log file).
	 */
	public void flushAll()
	{
		this._flushAll = true;
	}

	/**
	 * \brief Create (if applicable) and open directory.
	 * 
	 * @param dir TODO
	 * @return
	 */
	// NOTE this is currently never used
	public boolean dir(String dir)
	{
		return this.dir(dir, 0);
	}

	/**
	 * \brief Walks through folder structure to create the full path.
	 * 
	 * @param dir TODO
	 * @param min 
	 * @return 
	 */
	// NOTE the boolean returned is currently never used.
	private boolean dir(String dir, int min)
	{
		String[] folders = dir.split("/");
		String path = "";
		boolean result = false;
		for ( int i = 0; i < folders.length - min; i++ )
		{
			path = path + folders[i] + "/";
			result = this.dirMake(path);
		}
		return result;
	}

	/**
	 * opens file
	 */
	public ArrayList<String> fopen(String file)
	{
		ArrayList<String> lines = new ArrayList<String>();
		try
		  {
		    BufferedReader reader = new BufferedReader(new FileReader(file));
		    String line;
		    while ( (line = reader.readLine()) != null )
		    {
		      lines.add(line);
		    }
		    reader.close();
		    return lines;
		  }
		  catch (Exception e)
		  {
		    System.err.format("Exception occurred trying to read '%s'.", file);
		    e.printStackTrace();
		    return null;
		  }
	}

	/**
	 * \brief Create file at the location given by the path.
	 * 
	 * <p>Currently overwrites if file already exists.</p>
	 * 
	 * @param file String path for the output location.
	 */
	//TODO instead of overwriting, we should be using fileWriterFileNumber to
	// make a new file with unique name.
	public void fnew(String file)
	{
		if ( Global.write_to_disc ) 
		{
			if ( file.split("/").length > 1 )
				this.dir(file, 1);
			try
			{
				File f = new File(file);
				FileWriter fstream = new FileWriter(f, true);
				this._output = new BufferedWriter(fstream);
				if( Log.shouldWrite(Tier.EXPRESSIVE) )
					Log.out(Tier.EXPRESSIVE, "New file: " + file);
			}
			catch (IOException e)
			{
				Log.printToScreen(e.toString(), false);
			}
		}
	}

	/**
	 * \brief Delete the file specified by the given path.
	 * 
	 * @param file String path to the file to be deleted.
	 */
	public void deleteFile(String file)
	{
		File f = new File(file);
		f.delete();
	}

	/**
	 * \brief Check whether a file exists.
	 * 
	 * @param file String path to the file.
	 * @return True if file exists, false if it does not exist.
	 */
	public boolean doesFileExist(String file)
	{
		File f = new File(file);
		return f.exists();
	}

	/**
	 * \brief Write text to file.
	 * 
	 * @param text String line to write: needs to end with a carriage return if
	 * it should be a line.
	 */
	public void write(String text)
	{
		if ( Global.write_to_disc ) 
		{
			try
			{
				this._output.write(text);
				if ( this._flushAll )
					this._output.flush();
			}
			catch (IOException e)
			{
				Log.printToScreen(e.toString(), false);
				Log.printToScreen("skipped line: " + text, false);
			}
		}
	}

	/**
	 * Close this file handler's output file.
	 */
	public void fclose()
	{
		if( this._output != null )
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
	}

	/**
	 * \brief Check if this is ready to write.
	 * 
	 * @return True if it is ready, false if it needs more input.
	 */
	public boolean isReady()
	{
		return ( this._output != null );
	}
}
