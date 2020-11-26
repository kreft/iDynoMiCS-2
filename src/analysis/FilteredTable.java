package analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import agent.Agent;
import analysis.filter.Filter;
import analysis.filter.SoluteFilter;
import analysis.filter.TimerFilter;
import aspect.AspectInterface;
import compartment.Compartment;
import dataIO.CsvExport;
import dataIO.Log;
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
	
	/**
	 * Specifies compartment of table
	 */
	private Compartment compartment;
	
	/**
	 * Only include agents in table that pass this filter
	 */
	private Filter filter;
	
	/**
	 * Specifying the column content
	 */
	private LinkedList<Filter> columns = new LinkedList<Filter>();
	
	/**
	 * Only include agents that pass the qualification filter in the column
	 */
	private LinkedList<Filter> qualification = new LinkedList<Filter>();
	
	/**
	 * csv exporter
	 */
	CsvExport _csv = new CsvExport();
	
	/**
	 * Input table logic string
	 */
	private String _logic;

	public FilteredTable(String logic)
	{
		this(logic, null);
	}
	
	public FilteredTable(String logic, String com)
	{
		this._logic = logic.replaceAll("\\s+","");

		String t;
		String c;
		
		/* what compartment */
		if ( this._logic.contains("~") )
		{
			t = this._logic.split("~")[0];
			c = this._logic.split("~")[1];
		} 
		else if ( com != null ) 
		{
			t = com;
			c = this._logic;
		}
		else
		{
			t = "0";
			c = this._logic;
		}
		
		/* work out whether agents need to be excluded from the table */
		if ( t.contains("?"))
		{
			this.filter = FilterLogic.filterFromString( t.split("\\?")[1] );
			t = t.split("\\?")[0];
		}
		else
			this.filter = null;
		
		/* associate the compartment */
		if (Idynomics.simulator.hasCompartment(t))
			compartment = Idynomics.simulator.getCompartment(t);
		else
		{
			List<String> comps = Idynomics.simulator.getCompartmentNames();
			compartment = Idynomics.simulator.getCompartment( 
					comps.get( Integer.valueOf( t ) ) );
		}
		
		/* work out the columns */
		for ( String s : c.split("\\|") )
		{
			if ( s.contains("?"))
			{
				this.qualification.add( 
						FilterLogic.filterFromString( s.split("\\?")[1] ) );
				s = s.split("\\?")[0];
			}
			else
				this.qualification.add( null );
			
			this.columns.add( FilterLogic.filterFromString( s, this.compartment ) );
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
				{
					if( this.qualification.get(i) == null || 
							this.qualification.get(i).match( ( a ) ) )
						out += columns.get(i).stringValue(a, "%s") 
								+ (i < columns.size()-1 ? "\t, " : "\n" );
				}
			}
		}
		return out;
	}
	
	public String summary()
	{
		String out = compartment.getName() + (this.filter == null ? 
				"" : " ? " + this.filter.header() ) + " summary\n";
		LinkedList<AspectInterface> subjects = new LinkedList<AspectInterface>();
		LinkedList<String> values = new LinkedList<String>();
		
		for (int i = 0; i < columns.size(); i++)
		{
			if ( columns.get(i) instanceof SoluteFilter )
			{
				values.add( columns.get(i).stringValue(null, "%1.3e")	);
			}
			else if ( columns.get(i) instanceof TimerFilter )
			{
				values.add( columns.get(i).stringValue(null, "%s")	);
			}
			else
			{
				for (Agent a : compartment.agents.getAllAgents())
					if ( this.filter == null || this.filter.match( ( a ) ) )
						if( this.qualification.get(i) == null || 
								this.qualification.get(i).match( ( a ) ) )
							subjects.add(a);
				
				double[] count = Counter.count( columns.get(i), subjects );
				values.add( (count.length == 1 ? 
						String.format(Filter.screenLocale, "%1.3g", count[0]) :
						Vector.toString( Counter.count( columns.get(i), subjects 
						) ) ) );
				subjects.clear();
			}
		}
		
		for (int i = 0; i < columns.size(); i++)
		{
			out += String.format("%45s | ",columns.get(i).header() + ( this.qualification.get(i) == null 
					? "" : " ? " +  this.qualification.get(i).header()))
					+ values.get( i ) + (i+1 < columns.size() ? "\n": "");
		}
		
		return out;
	}
	
	public String line(String delimiter)
	{
		String out = "";
		LinkedList<AspectInterface> subjects = new LinkedList<AspectInterface>();

		
		for (int i = 0; i < columns.size(); i++)
		{
			if ( columns.get(i) instanceof SoluteFilter )
			{
				out += columns.get(i).stringValue(null, "%e")
						+ (i < columns.size()-1 ? delimiter : "" );
			}
			else if ( columns.get(i) instanceof TimerFilter )
			{
				out += columns.get(i).stringValue(null, "%g")
						+ (i < columns.size()-1 ? delimiter : "" );
			}
			else
			{
				for (Agent a : compartment.agents.getAllAgents())
					if ( this.filter == null || this.filter.match( ( a ) ) )
						if( this.qualification.get(i) == null || 
								this.qualification.get(i).match( ( a ) ) )
							subjects.add(a);
				
				out += Vector.toString( Counter.count( columns.get(i), subjects ) )
						+ (i < columns.size()-1 ? delimiter : "" );
				subjects.clear();
			}
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

	public String header(String delimiter) {
		String out = "";
		for (int i = 0; i < columns.size(); i++)
		{
			if ( columns.get(i) instanceof SoluteFilter )
			{
				out += columns.get(i).header()
						+ (i < columns.size()-1 ? delimiter : "" );
			}
			else if ( columns.get(i) instanceof TimerFilter)
			{
				out += columns.get(i).header()
						+ (i < columns.size()-1 ? delimiter : "");
			}
			else 
			{			
				if( qualification != null )
					out += columns.get(i).header() + " " + 
							(i < columns.size()-1 ? delimiter : "" );
				else
					out += columns.get(i).header() + " " + 
							qualification.get(i).header() +
							(i < columns.size()-1 ? delimiter : "" );
			}
		}
		return out;
	}
}
