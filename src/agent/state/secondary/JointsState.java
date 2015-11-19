package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.State;

public class JointsState implements State {
	
	private Agent agent;

	public void init(Agent agent, Object state)
	{
		this.agent = agent;
	}
	
	public Object get()
	{
		Body myBody = (Body) agent.get("body");
		return myBody.getJoints();
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
