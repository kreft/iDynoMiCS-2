package agent.state;

import agent.Agent;

public class CalculatedState implements State {
	
	private stateExpression expression;
	private Agent agent;
	
	public interface stateExpression 
	{
		Object calculate(Agent agent);
	}

	public void init(Agent agent, Object stateExpression)
	{
		this.agent = agent;
		this.expression = (stateExpression) stateExpression;
	}
	
	public Object get()
	{
		return expression.calculate(agent);
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