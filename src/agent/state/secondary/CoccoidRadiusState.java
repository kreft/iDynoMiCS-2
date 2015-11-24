package agent.state.secondary;

import agent.Agent;
import agent.state.State;

public class CoccoidRadiusState  implements State {
	
	public void init(Object state)
	{
		
	}
	
	public Object get(Agent agent)
	{
		// V = 4/3 Pi r^3
		return  Math.pow((((double) agent.get("volume") * 3) / (4 * Math.PI)),0.3333333333333333333333333);
	}

}

