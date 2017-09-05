package analysis;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import analysis.filter.Filter;
import aspect.AspectInterface;
import dataIO.CsvExport;
import dataIO.Log;
import idynomics.Compartment;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import utility.Helper;

/**
 * FilteredTable objects can be used to output data tables of 
 * agent/aspectInterface properties. The table can contain all agents of a
 * compartment or a select set based on a filter with boolean representation.
 * 
 * Each column of the table is governed by it's own filter. The filter may
 * filter specific properties of the agent (value filter). The filter may
 * return a boolean based on whether a statement or statements is/are true 
 * (specification or multi-filter). Or the filter may assign a category for the
 * agent (category filter).
 * 
 * FilteredTable is the top level object for data retrieval following *table 
 * logics*
 * 
 * example:
 * 
 * 0 ? a > 1.0 + b == mySpecies ~ mass | glucose > 1e-2 , glucose > 0.5e-2 
 * 
 * represents:
 * first Compartment ? 
 * filter specification for what needs to be included in the table (leave blank for all) ~
 * filter column (value filter) | 
 * filter column (Category filter)
 * 
 * Thus: ? ~ and | are operators that split the different aspects of the table
 * 
 * See FilterLogic for the other operators
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class FilteredTable {
	
	private Compartment compartment;
	private Filter filter;
	private LinkedList<Filter> columns = new LinkedList<Filter>();
	CsvExport _csv = new CsvExport();
	private String _logic;

	public FilteredTable(String logic)
	{
		this._logic = logic.replaceAll("\\s+","");
		String t = this._logic.split("~")[0];
		String c = this._logic.split("~")[1];
		for ( String s : c.split("\\|") ) //FIXME maybe not the best operator
			this.columns.add( FilterLogic.filterFromString( s ) );
		if ( t.contains("?"))
		{
			this.filter = FilterLogic.filterFromString( t.split("\\?")[1] );
			t = t.split("\\?")[0];
		}
		else
			this.filter = null;
		
		if (Idynomics.simulator.hasCompartment(t))
			compartment = Idynomics.simulator.getCompartment(t);
		else
		{
			List<String> comps = Idynomics.simulator.getCompartmentNames();
			compartment = Idynomics.simulator.getCompartment( 
					comps.get( Integer.valueOf( t ) ) );
		}
	}
	
	public String display()
	{
		String out = "";
		for (int i = 0; i < columns.size(); i++)
			out += columns.get(i).header() 
					+ (i < columns.size()-1 ? "\t, " : "\n" );
		
		for (Agent a : compartment.agents.getAllAgents())
		{
			if ( this.filter == null || this.filter.match( ( a ) ) )
			{
				for (int i = 0; i < columns.size(); i++)
					out += columns.get(i).stringValue(a) 
							+ (i < columns.size()-1 ? "\t, " : "\n" );
			}
		}
		return out;
	}
	
	public String summary()
	{
		String out = "";
		LinkedList<AspectInterface> subjects = new LinkedList<AspectInterface>();
		for (Agent a : compartment.agents.getAllAgents())
			if ( this.filter == null || this.filter.match( ( a ) ) )
				subjects.add(a);
		
		for (int i = 0; i < columns.size(); i++)
		{
			out += columns.get(i).header() + "\n";
			out += Vector.toString( Counter.count( columns.get(i), subjects ) ) 
					+ "\n";
		}
		return out;
	}
	
	public void toFile()
	{
		_csv.createCustomFile(Helper.obtainInput("", "file name"));
		_csv.writeLine(this.display());
		_csv.closeFile();
		
		Log.printToScreen(Helper.head(this.display()), false);
	}
	
	public String toString()
	{
		return this._logic;
	}
}
