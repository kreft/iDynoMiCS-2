package optimization.geneticAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.stream.Stream;

import dataIO.CsvImport;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;


public class DataFromCSV 
{	
	/**
	 * 
	 * DataPoint holds time (timePoint) and measured entity (collumn) of
	 * dataSet, it also saves what integer position it will have in the data
	 * vector
	 *
	 */
	public class DataPoint implements Comparable<DataPoint> {
		public double timePoint;
		public int coll;
		public Integer p;
		
		public DataPoint(double time, int collumn, int pos)
		{
			p = pos;
			timePoint = time;
			coll = collumn;
		}

		@Override
		public int compareTo(DataPoint o) 
		{
			return p.compareTo(o.p);
		}
		
		public String toString()
		{
			return "DataPoint: " + p + " " + timePoint + " " + coll;
		}
	}
	
	private static TreeMap<DataPoint, Double> _dataPoints = 
			new TreeMap<DataPoint, Double>();

	
	/**
	 * Get observed data, storing timepoints to extract the simulation
	 * output data at correct time points.
	 * 
	 * @param dataPath
	 * @return
	 */
	public double[] getData(String dataPath)
	{
		double[][] obsData = CsvImport.getDblMatrixFromCSV(dataPath);
		int p=0;
		for (int i = 0; i < obsData.length; i++)
			for (int j = 1; j < obsData[i].length; j++)
				if ( !Double.isNaN(obsData[i][j]) )
					_dataPoints.put( new DataPoint( obsData[i][0],j,p++), 
							obsData[i][j] );
		return Vector.vector( _dataPoints.values() );

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
		double[][] outData = null; 
		
		try (Stream<Path> dataFilePaths = Files.find(
				Paths.get(genFolderPath), 2, (p, bfa) -> 
				p.getFileName().toString().equalsIgnoreCase("data.csv")))
		{
			String[] fileNames = dataFilePaths.map(path -> 
					path.toString()).toArray(String[]::new);
			outData = new double[ fileNames.length ][ _dataPoints.size() ];
			for(String s : fileNames )
			{
				// get the row number from the folder name
				int rowIdx = Integer.parseInt(s.substring(s.lastIndexOf("_")+1)
						.split(File.separator)[0]) - 1;
				
				if( Log.shouldWrite(Tier.DEBUG))
					Log.out(Tier.DEBUG, s);
				
				double[][] simOutput = 
						CsvImport.getDblMatrixFromCSV( s );
				
				for( DataPoint p : _dataPoints.keySet())
				{
					int t = 0;
					
					// Could be calculated as the minimum of the difference
					// indexOf ( min(abs( p.timePoint - simOutput[:,0] )))
					// Too much hassle in this case though.
					while( (t+1 < simOutput.length) && 
							(p.timePoint > simOutput[t][0] ) && 
							(p.timePoint - simOutput[t][0] ) > 
							(p.timePoint - simOutput[t+1][0] ) )
						t++;
					// Fill rowIdx
					outData[rowIdx][p.p] = simOutput[t][p.coll];
				}
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return outData;
	}
}
