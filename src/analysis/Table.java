package analysis;

import java.util.LinkedList;

import agent.Agent;
import analysis.specs.AgentSpecification;
import analysis.specs.EnvironmentSpecification;
import analysis.specs.Specification;
import compartment.Compartment;

public class Table {
	/**
	 * Specifying the column content
	 */
	private LinkedList<Specification> columns = new LinkedList<Specification>();
	
	private Compartment _compartment;
	
	private Specification _filter;
	
	public String display()
	{
		StringBuilder out = new StringBuilder();
		return header(out).append( body(out) ).toString();
	}
	
	/**
	 * Add a filter to only select a subset of agents
	 * @param filter
	 */
	public void filter(Specification filter)
	{
		this._filter = filter;
	}
	
	/** 
	 * returns the header line
	 * @param out
	 * @return
	 */
	public StringBuilder header(StringBuilder out)
	{
		for ( int i = 0; i < columns.size(); i++ )
			out.append( columns.get(i).header() ).append( 
					( i < columns.size()-1 ? "\t, " : "\n" ) );
		return out;
	}
	
	/**
	 * returns the body
	 * @param out
	 * @return
	 */
	public StringBuilder body(StringBuilder out)
	{
		for ( Agent a : _compartment.agents.getAllAgents() )
		{
			if( this._filter == null || this._filter.test(a) )
				for ( int i = 0; i < columns.size(); i++ )
					out.append( columns.get(i).value(a) ).append(
							(i < columns.size()-1 ? "\t, " : "\n" ) );
		}
		return out;
	}
	
	/**
	 * Read out the table specifications from String and build the table.
	 * @param table
	 * @param comp
	 */
	public void build(String table, Compartment comp)
	{
		this._compartment = comp;
		String c = table.replaceAll("\\s+","");

		/* work out the columns */
		for ( String s : c.split("\\|") )
		{
			if( s.contains("%"))
				this.columns.add( new EnvironmentSpecification(s, comp));
			else
				this.columns.add( new AgentSpecification(s) );
		}
	}
}
