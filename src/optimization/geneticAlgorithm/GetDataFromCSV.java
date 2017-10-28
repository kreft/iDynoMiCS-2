package optimization.geneticAlgorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;


import dataIO.CsvImport;
import linearAlgebra.Matrix;
import utility.Helper;

/**
 * \brief Get the input, output and data matrices from csv files.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */

public class GetDataFromCSV 
{
	public static String DELIMITER = ",";
	
	public static String NEWLINE = ";";
	
	private static ArrayList<Double> _timePoints = new ArrayList<Double>();
	
	private static ArrayList<int[]> _nanPos = new ArrayList<int[]>();
	
	private static int _outCols;
	
	/**
	 * Gets the matrix as a double[][] removing any headers, if present
	 * 
	 * @param filePath
	 * @return
	 */
	protected static double[][] getMatrix(String filePath)
	{
		String[][] strData = CsvImport.readFile(filePath);
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
	
	/**
	 * Get observed data, storing timepoints to extract the simulation
	 * output data at correct time points.
	 * 
	 * @param dataPath
	 * @return
	 */
	public static double[] getData(String dataPath)
	{
		double[][] obsData = getMatrix(dataPath);
		ArrayList<Double> dataVals = new ArrayList<Double>();
		for (int i = 0; i < obsData.length; i++)
		{
			_timePoints.add(obsData[i][0]);
			for (int j = 1; j < obsData[i].length; j++)
			{
				if (Double.isNaN(obsData[i][j]))
				{
					_nanPos.add(new int[] {i, j});
				}
				else
				{
					dataVals.add(obsData[i][j]);
				}
			}
		}
		double[] dataValues = dataVals.stream()
				.mapToDouble(Double::doubleValue).toArray();
		_outCols = dataValues.length;
		return dataValues;
	}
	
	/**
	 * Get input data
	 * 
	 * @param filePath
	 * @return
	 */
	
	public static double[][] getInput(String filePath)
	{
		double[][] inputData = getMatrix(filePath);
		return inputData;
	}
	
	/**
	 * Get output data formatted to match timepoints in observed data
	 * Each row represents output data from a single run
	 * 
	 * @param genFolderPath
	 * @return
	 */
	public static double[][] getOutput(String genFolderPath)
	{
		double[][] outData = new double[_timePoints.size()][_outCols];
		try (Stream<Path> dataFilePaths = Files.find(
				Paths.get(genFolderPath), 2,
				(p, bfa) -> p.getFileName().toString().equalsIgnoreCase("data.csv")))
		{
			dataFilePaths.forEach((f) -> {
				String fileName = f.toString();
				double[][] simOutput = getMatrix(fileName);
				for (int i = 0; i < _timePoints.size(); i++)
				{
					ArrayList<Double> absDiff = new ArrayList<Double>();
					for (int j = 0; j < simOutput.length; j++)
					{
						absDiff.add(Math.abs(
								_timePoints.get(i) - simOutput[j][0]));
					}
					int idxMin = absDiff.indexOf(Collections.min(absDiff));
					for (int j = 0; j < _outCols; j++)
					{
						outData[i][j] = simOutput[idxMin][j+1];
					}
				}
			});
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return outData;
	}
}
