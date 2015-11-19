package agent.state.secondary;

import agent.Agent;
import agent.state.State;

public class CoccoidRadius  implements State {
	
	private Agent agent;

	public void init(Agent agent, Object state)
	{
		this.agent = agent;
	}
	
	public Object get()
	{
		// V = 4/3 Pi r^3
		return  Math.pow((((double) agent.get("volume") * 3) / (4 * Math.PI)),0.3333333333333333333333333);
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

