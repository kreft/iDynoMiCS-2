package dataIO;

import java.util.ArrayList;

import dataIO.Log.Tier;
import linearAlgebra.Matrix;
import utility.Helper;


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
	
	public static String DELIMITER = ",";
	
	public static String NEWLINE = ";";
	
	/**
	 * Read the csv file and return the data as a String[][] matrix
	 * @param filePath Path to the csv file (including filename)
	 * @return String[][] table of data from the file.
	 */
	public static String[][] getStringMatrixFromCSV(String filePath)
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
	 * Gets the matrix as a double[][] removing any headers, if present
	 * 
	 * @param filePath
	 * @return
	 */
	public static double[][] getDblMatrixFromCSV(String filePath)
	{
		String[][] strData = getStringMatrixFromCSV(filePath);
		String dataAsStr = "";
		for (int i = 0; i < strData.length; i++)
		{
			for (int j = 0; j < strData[i].length; j++)
			{
				String delim = (j == (strData[i].length - 1) ) ? NEWLINE : DELIMITER;
				if (Helper.dblParseable(strData[i][j]))
				{
					dataAsStr += strData[i][j] + delim;
							
				}
				else if (strData[i][j] == "" || strData[i][j] == "NaN" ||
						strData[i][j] == null || strData[i][j] == "null")
				{
					dataAsStr += "NaN" + delim;
				}
			}
		}
		double[][] values = Matrix.dblFromString(dataAsStr);
		return values;
	}
}
