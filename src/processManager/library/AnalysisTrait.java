package processManager.library;

import java.util.Arrays;
import java.util.LinkedList;

import org.w3c.dom.Element;

import analysis.FilteredTable;
import analysis.quantitative.Raster;
import dataIO.CsvExport;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
import spatialRegistry.SpatialMap;
import utility.Helper;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import processManager.ProcessManager;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AnalysisTrait extends ProcessManager {

	public static String FILE_PREFIX = AspectRef.filePrefix;

	/**
	 * The CSV exporter.
	 */
	protected CsvExport _csvExport = new CsvExport();

	/**
	 * The prefix for the file output path.
	 */
	protected String _prefix;
	
	protected String[] filters;
	
	protected int[] colocalizationSteps;
	
	protected String[] header = new String[]{
			"#",
			"time"
	};
	

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._prefix = this.getString(FILE_PREFIX);
		
		/* Initiate new file. */
		this._csvExport.createCustomFile(this._prefix);
		
		filters = this.getStringA( AspectRef.filterSet );
		
		String steps = Helper.setIfNone( 
				this.getString( AspectRef.colocalizationSteps ) , "1" );
		this.colocalizationSteps = Vector.intFromString( steps );

		this._csvExport.writeLine( getHeader() );
	}

	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		Raster raster = new Raster( _agents );

		raster.rasterize( this.getDouble( AspectRef.rasterScale ) );

		double[] time = new double[] { 
				Idynomics.simulator.timer.getCurrentIteration(), 
				this.getTimeForNextStep() };
		
		StringBuilder builder = new StringBuilder();
		builder.append( Vector.toString( time ) );
		String line = "";
		
		for ( int i = 0; i < this.filters.length; i++)
		{
			builder.append(Vector.DELIMITER);
			builder.append( raster.averageDiffusionDistance(filters[i]));
		}
		
		LinkedList<String[]> combList = combinations( filters );
		for ( int i = 0; i < combList.size(); i++)
		{
			int[] distAB = raster.distanceVector(
					combList.get(i)[0], combList.get(i)[1]);
			int[] distBA = raster.distanceVector(
					combList.get(i)[1], combList.get(i)[0]);
			
			builder.append( Vector.DELIMITER );
			builder.append( raster.averageDist( distAB ) );
			
			builder.append( Vector.DELIMITER );
			builder.append( raster.averageDist( distBA ) );
			
			builder.append( Vector.DELIMITER );
			for ( int j = 0; j < colocalizationSteps.length; j++)
				builder.append( Vector.toString( raster.colocalization( 
						distAB, distBA, colocalizationSteps[j] ) ) + "," );
			
			line = builder.toString().substring( 0, builder.length()-1 );
		}
		
		
		this._csvExport.writeLine( line );

		/* output table summary to log/console */
		Log.out(Tier.EXPRESSIVE, "Trait analysis:\n" +
				getHeader() + "\n" + builder.toString() );
	}

	public LinkedList<String[]> combinations( String[] input )
	{
		LinkedList<String[]> out = new LinkedList<String[]>();
		for ( int i = 1; i < input.length; i++)
			out.add( new String[] { input[0], input[i] } );
		if( input.length > 2 )
			out.addAll( combinations( 
					Arrays.copyOfRange( input, 1, input.length ) ) );
		return out;
	}
	
	public String getHeader()
	{

		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < header.length; i++)
		{
			builder.append( header[i] );
			builder.append( Vector.DELIMITER );
		}
		for ( int i = 0; i < this.filters.length; i++)
		{
			builder.append( "average diff " + filters[i] );
			builder.append(Vector.DELIMITER);
		}
		LinkedList<String[]> combList = combinations( filters );
		for ( int i = 0; i < combList.size(); i++)
		{
			 builder.append( "average codist AB " + combList.get(i)[0] + " " + combList.get(i)[1] + 
					", BA,");
			 for ( int j = 0; j < colocalizationSteps.length; j++)
			 {
				 builder.append( colocalizationSteps[j] + "AB, " + 
						 colocalizationSteps[j] + "BA," );
			 }
		}
		String comb = builder.toString().substring( 0, builder.length()-1 );
		return comb;
	}
	
	/**
	 * TODO this pm needs a post run call to properly close the file,
	 * alternatively we could open and close the file each time step.
	 * Doing neither should still release the file after the program is
	 * closed, but this is not a correct way of handling this.
	 */
	protected void postRun()
	{
		this._csvExport.closeFile();
	}
}
