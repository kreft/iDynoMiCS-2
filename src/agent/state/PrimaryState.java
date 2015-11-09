package agent.state;

import agent.Agent;

public class PrimaryState implements State {
	private Object state;
	private Agent agent;

	public void init(Agent agent, Object state)
	{
		this.agent = agent;
		this.state = state;
	}
	
	public Object get()
	{
		return state;
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
