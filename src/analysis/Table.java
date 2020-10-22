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
	
	private Compartment compartment;
	
	public String display()
	{
		String out = "";
		for (int i = 0; i < columns.size(); i++)
			out += columns.get(i).header() 
					+ (i < columns.size()-1 ? "\t, " : "\n" );
		
		for (Agent a : compartment.agents.getAllAgents())
				for (int i = 0; i < columns.size(); i++)
					out += columns.get(i).value(a)
							+ (i < columns.size()-1 ? "\t, " : "\n" );

		return out;
	}
	
	public void build(String table, Compartment comp)
	{
		this.compartment = comp;
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
