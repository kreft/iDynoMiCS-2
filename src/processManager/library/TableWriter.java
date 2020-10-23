/**
 * 
 */
package processManager.library;

import org.w3c.dom.Element;

import analysis.FilteredTable;
import analysis.Table;
import analysis.specs.AgentSpecification;
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
public class TableWriter extends ProcessManager
{
	/**
	 * XML tag for the table logic expression.
	 */
	public static String TABLE_SPEC = AspectRef.tableSpecification;
	
	public static String FILE_NAME = AspectRef.fileName;
	
	private static String INCLUDE_HEADER = AspectRef.includeHeader;
	
	private static String FILTER = AspectRef.filter;

	
	/**
	 * The Filtered table
	 */
	protected Table table;
	
	/**
	 * csv out
	 */
	protected String csvOut;
	
	protected CsvExport _csv;

	private Boolean _includeHeader;

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		/* Create new filtered table from table logic expression */
		this.table = new Table();
		this.table.build( this.getString(TABLE_SPEC), agents._compartment);
		this.csvOut = this.getString(FILE_NAME);
		this._csv = new CsvExport();
		this._includeHeader = this.getBoolean( INCLUDE_HEADER );
		String filter = this.getString(FILTER);
		if ( !Helper.isNullOrEmpty(filter) )
			this.table.filter( new AgentSpecification(filter));
	}

	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		if (! Helper.isNullOrEmpty( this.csvOut ))
		{		
			this._csv.createFile(csvOut);
			StringBuilder out = new StringBuilder();
			
			if(_includeHeader == null || _includeHeader)
				_csv.writeLine( table.header(out).toString() );
			_csv.writeLine( table.body( new StringBuilder() ).toString() );
			_csv.closeFile();
		}
	}
	
	
}
