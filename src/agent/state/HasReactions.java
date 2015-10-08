package agent.state;

import java.util.HashMap;

import agent.Agent.StatePredicate;

public interface HasReactions
{
	/**
	 * 
	 */
	public final StatePredicate<Object> tester = new StatePredicate<Object>()
	{
		public boolean test(Object aState)
		{
			return aState instanceof HasReactions;
		}
	};
	
	/**
	 * 
	 * @param soluteNames
	 * @return
	 */
	public abstract HashMap<String,Double> 
						get1stTimeDerivatives(HashMap<String,Double> concns);
	
}
