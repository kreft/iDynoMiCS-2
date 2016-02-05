package processManager;

import dataIO.SvgExport;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public class WriteAgentsSvg extends ProcessManager
{
	
	//FIXME: very quick and dirty, this ProcessManager really needs more
	// information than it can get from the environment and agents...
	protected SvgExport svg = new SvgExport();
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		svg.writepov("PlaceHolderSinceProcessManagerCannotAccessCompartmentName", agents);
	}
}