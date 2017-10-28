package dataIO;

import java.util.ArrayList;

import dataIO.Log.Tier;


/**
 * \brief CSV file import
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */

public class CsvImport
{

	/**
	 * File handler
	 */
	protected static FileHandler _csvFile = new FileHandler();	
	
	/**
	 * Read the csv file and return the data as a String[][] matrix
	 * @param filePath Path to the csv file (including filename)
	 * @return String[][] table of data from the file.
	 */
	public static String[][] readFile(String filePath)
	{
		if (_csvFile.doesFileExist(filePath))
		{
			ArrayList<String> csvLines = _csvFile.fopen(filePath);
			int headerLength = csvLines.get(0).split(",").length;
			String[][] csvData = 
					new String[csvLines.size()][headerLength];
			for (int i = 0; i < csvLines.size(); i++) {
				csvData[i] = csvLines.get(i).split(",");
			}
			closeFile();
			return csvData;
		}
		else 
		{
			Log.shouldWrite(Tier.NORMAL);
			Log.out(Tier.NORMAL, "No csv file found in the given path: "+ filePath);
			return null;
		}
	}
	
	/**
	 * close the csv file
	 */
	public static void closeFile()
	{
		_csvFile.fclose();
	}
}
