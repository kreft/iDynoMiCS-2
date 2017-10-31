package optimization.geneticAlgorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;


import dataIO.CsvImport;

/**
 * \brief Get the input, output and data matrices from csv files.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */

public class GetDataFromCSV 
{	
	private static ArrayList<Double> _timePoints = new ArrayList<Double>();
	
	private static ArrayList<int[]> _nanPos = new ArrayList<int[]>();
	
	private static int _outCols;
	
	/**
	 * Get observed data, storing timepoints to extract the simulation
	 * output data at correct time points.
	 * 
	 * @param dataPath
	 * @return
	 */
	public static double[] getData(String dataPath)
	{
		double[][] obsData = CsvImport.getDblMatrixFromCSV(dataPath);
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
		double[][] inputData = CsvImport.getDblMatrixFromCSV(filePath);
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
			String[] fileNames = dataFilePaths
									.map(path -> path.toString())
									.toArray(String[]::new);
			for (int cnt = 0; cnt < fileNames.length; cnt++)
			{
				double[][] simOutput = CsvImport.getDblMatrixFromCSV(fileNames[cnt]);
				for (int i = 0; i < _timePoints.size(); i++)
				{
					ArrayList<Double> absDiff = new ArrayList<Double>();
					for (int j = 0; j < simOutput.length; j++)
					{
						absDiff.add(Math.abs(
								_timePoints.get(i) - simOutput[j][0]));
					}
					int idxMin = absDiff.indexOf(Collections.min(absDiff));
					for (int j = 1; j < simOutput[idxMin].length; j++)
					{
						if (! _nanPos.contains(new int[] {idxMin, j}))
						{
							int ii = j - 1 + i*(simOutput[idxMin].length - 1);
							outData[cnt][ii] = simOutput[idxMin][j];
						}
					}
				}
			}
			return outData;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return outData;
	}
}
