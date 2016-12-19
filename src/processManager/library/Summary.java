/**
 * 
 */
package processManager.library;

import org.w3c.dom.Element;

import analysis.FilteredTable;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;

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
	
	/**
	 * The Filtered table
	 */
	protected FilteredTable table;

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		/* Create new filtered table from table logic expression */
		this.table = new FilteredTable( this.getString(TABLE_SPEC) );
	}

	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		/* output table summary to log/console */
		Log.out(Tier.NORMAL, "Summary for table:\n"
				+ "[" + table.toString() + "]\n|" + 
				table.summary().replaceAll("\\n+","\n|") );
	}
}
