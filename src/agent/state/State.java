package agent.state;

import agent.Agent;

public interface State {
	
	public void init(Agent agent, Object state);
	
	public Object get();
	
	public Agent getAgent();
	
	public void setAgent(Agent agent);

}
