package dataIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import dataIO.Log.tier;

/**
 * Handles file operations, create folders and files, write output.
 * @author baco
 *
 */
public class FileHandler {
	BufferedWriter output;
	int filewriterfilenr;
	
	/*
	 * set to true if each line needs to be written to file immediately (for
	 * instance for the log file.
	 */
	public boolean flushAll = false;

// FIXME Bas [07.02.2016] what are the use cases for this?
//	/**
//	 * Decimal format used in logging simulation messages.
//	 */
//	private static DecimalFormat _decimalFormat = new DecimalFormat("0.0");
	
	/**
	 * Creates directory if it does not exist
	 * @param dir
	 * @return
	 */
	private boolean dirMake(String dir)
	{
		File base = new File(dir);
		boolean result = false;
		if (!base.exists()) {
			try{
				base.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		}
		return result;
	}
	
	/**
	 * Create (if applicable) and open directory
	 * @param dir
	 * @return
	 */
	public boolean dir(String dir)
	{
		return dir(dir, 0);
	}
	
	/**
	 * Walks trough folder structure to create the full path
	 * @param dir
	 * @param min
	 * @return
	 */
	private boolean dir(String dir, int min)
	{
		String[] folders = dir.split("/");
		String path = "";
		boolean result = false;
		for (int i = 0; i < folders.length - min; i++)
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
	 */
	public void fnew(String file)
	{
		if(file.split("/").length > 1)
			dir(file,1);
		try {
			File f = new File(file);
			f.delete();
			java.io.FileWriter fstream;
			fstream = new java.io.FileWriter(f, true);
			output = new BufferedWriter(fstream);
		} catch (IOException e) {
			// catch
			e.printStackTrace();
		}
	}
	
	/**
	 * write line to file
	 * @param line
	 */
	public void write(String line)
	{
		try {
			output.write(line);
			if(flushAll)
				output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * close file
	 */
	public void fclose() {
		try {
			output.flush();
			output.close();
		} catch (IOException e) {
			// catch
			e.printStackTrace();
		}
	}
}
