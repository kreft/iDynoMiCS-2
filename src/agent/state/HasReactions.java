package agent.state;

import java.util.HashMap;

import agent.state.State.StatePredicate;

public interface HasReactions
{
	/**
	 * 
	 */
	public final StatePredicate<State> tester = new StatePredicate<State>()
	{
		public boolean test(State aState)
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
