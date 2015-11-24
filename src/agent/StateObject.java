package agent;

import agent.state.CalculatedState;
import agent.state.State;

public interface StateObject {
	
	public boolean isLocalState(String name);
	
	public Object getState(String name);
	
	public void setState(String name, State state);
	
	public void setPrimary(String name, Object state);
	
	public void setCalculated(String name, CalculatedState.stateExpression state);
	
	public void set(String name, Object state);

}