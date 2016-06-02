package processManager.library;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import aspect.AspectRef;
import dataIO.XmlLabel;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
/**
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentGrowth extends ProcessManager
{
	
	public static String AGENT_GROWTH = AspectRef.growth;
	public static String AGENT_DIVISION = AspectRef.agentDivision;
	
	protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.event(AGENT_GROWTH, this._timeStepSize);
			agent.event(AGENT_DIVISION, this._timeStepSize);
		}
	}
}