package processManager.library;

import org.w3c.dom.Element;

import analysis.FilteredTable;
import analysis.quantitative.Raster;
import dataIO.CsvExport;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
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
public class Analysis extends ProcessManager {


		public static String FILE_PREFIX = AspectRef.filePrefix;

		/**
		 * The CSV exporter.
		 */
		protected CsvExport _csvExport = new CsvExport();

		/**
		 * The prefix for the file output path.
		 */
		protected String _prefix;
		
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
			
			this._csvExport.writeLine( getHeader() + Raster.getHeader() );
			
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
			
			String line = Vector.toString( time ) + "," + raster.toString();
			this._csvExport.writeLine( line );
			
			/* output table summary to log/console */
			Log.out(Tier.EXPRESSIVE, "Structural analysis:\n" +
					getHeader() + "," + Raster.getHeader() + "\n" + line );
		}

		
		public String getHeader()
		{

			StringBuilder builder = new StringBuilder();
			for ( int i = 0; i < header.length; i++)
			{
				builder.append( header[i] );
				builder.append( Vector.DELIMITER );
			}
			return builder.toString();
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
