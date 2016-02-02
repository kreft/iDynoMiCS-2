package processManager;

import dataIO.SvgExport;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public class WriteAgentsSvg extends ProcessManager
{
	
	//FIXME: very quick and dirty, this ProcessManager really needs more
	// information than it can get from the environment and agents...
	private String CompName;
	protected SvgExport svg = new SvgExport();
	

	public void init(String compartmentName, AgentContainer agents)
	{
		//FIXME: very quick and dirty
		this.CompName = compartmentName;
		svg.writepov(CompName, agents);
	}
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		svg.writepov(CompName, agents);
	}
}