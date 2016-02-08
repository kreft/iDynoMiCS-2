package processManager;

import dataIO.SvgExport;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import utility.Helper;

public class WriteAgentsSvg extends ProcessManager
{
	
	//FIXME: very quick and dirty, this ProcessManager really needs more
	// information than it can get from the environment and agents...
	protected SvgExport svg = new SvgExport();
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		svg.writepov( Helper.obtainInput((String) reg().getValue(this, 
				"comparmentName"), "svg writer misses compartment name"), 
				agents);
	}
}