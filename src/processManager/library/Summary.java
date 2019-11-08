/**
 * 
 */
package processManager.library;

import org.w3c.dom.Element;

import analysis.FilteredTable;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.CsvExport;
import dataIO.Log;
import dataIO.Log.Tier;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Summary extends ProcessManager
{
	/**
	 * XML tag for the table logic expression.
	 */
	public static String TABLE_SPEC = AspectRef.tableSpecification;
	
	public static String FILE_NAME = AspectRef.fileName;
	
	private static String INCLUDE_HEADER = AspectRef.includeHeader;

	
	/**
	 * The Filtered table
	 */
	protected FilteredTable table;
	
	/**
	 * csv out
	 */
	protected String csvOut;
	
	protected CsvExport _csv;

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		/* Create new filtered table from table logic expression */
		this.table = new FilteredTable( this.getString(TABLE_SPEC), compartmentName );
		
		this.csvOut = this.getString(FILE_NAME);
		
		if (! Helper.isNullOrEmpty( this.csvOut ))
		{
			this._csv = new CsvExport();
			_csv.createCustomFile( this.csvOut );
		}
		
		Boolean includeHeader = this.getBoolean( INCLUDE_HEADER );
		
		includeHeader = Helper.setIfNone(includeHeader, false);
		
		if(includeHeader)
			_csv.writeLine( table.header(", ") );
			
	}

	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		/* output table summary to log/console */
		if( Log.shouldWrite(Tier.EXPRESSIVE) )
			Log.out(Tier.EXPRESSIVE, table.summary() );
		
		if (! Helper.isNullOrEmpty( this.csvOut ))
		{
			_csv.writeLine( table.line(", ") );
		}
	}
	
	
}
