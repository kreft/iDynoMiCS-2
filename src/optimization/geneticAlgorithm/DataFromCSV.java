package optimization.geneticAlgorithm;

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
import utility.Helper;


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
			TreeMap<Integer,String> fileMap = new TreeMap<Integer,String>();
			for(String s : fileNames )
			{
				String[] chunks = s.split("_");
				String[] tail = chunks[chunks.length-1].replaceAll("\\\\", "").replaceAll("/", "").split("d");
				fileMap.put( Integer.valueOf( tail[0] ), s);
			}
			
			outData = new double[ fileMap.size() ][_dataPoints.size() ];
			int cnt = 0;
			for (String fl : fileMap.values())
			{
				if( Log.shouldWrite(Tier.DEBUG))
					Log.out(Tier.DEBUG, fl);
				double[][] simOutput = 
						CsvImport.getDblMatrixFromCSV( fl );
				for( DataPoint p : _dataPoints.keySet())
				{
					int t = 0;
					while( (t+1 < simOutput.length) && 
							(p.timePoint > simOutput[t][0] ) && 
							(p.timePoint - simOutput[t][0] ) > 
							(p.timePoint - simOutput[t+1][0] ) )
						t++; 
					outData[cnt][p.p] = simOutput[t][p.coll];
				}
				cnt++;
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return outData;
	}
}
