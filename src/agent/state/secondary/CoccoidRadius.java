package agent.state.secondary;

import agent.Agent;
import agent.state.State;
import utility.ExtraMath;

public class CoccoidRadius  implements State {
	
	private Agent agent;

	public void init(Agent agent, Object state)
	{
		this.agent = agent;
	}
	
	public Object get()
	{
		// V = 4/3 Pi r^3
		return ExtraMath.radiusOfASphere((double) agent.get("Volume"));
	}
	
	public Agent getAgent()
	{
		return agent;
	}
	
	public void setAgent(Agent agent)
	{
		this.agent = agent;
	}
}

