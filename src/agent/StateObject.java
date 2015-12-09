package agent;

import agent.state.State;

public interface StateObject {
	
	public boolean isLocalState(String name);
	
	public Object getState(String name);
	
	public void setState(String name, State state);
	
	public void setPrimary(String name, Object state);
	
	public void set(String name, Object state);

}