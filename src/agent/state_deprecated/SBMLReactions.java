package agent.state_deprecated;

import java.util.HashMap;

import org.sbml.jsbml.Reaction;

public class SBMLReactions implements HasReactions
{
	protected Reaction[] _reactions;
	
	/*************************************************************************
	 * STATE METHODS
	 ************************************************************************/
	
	public Reaction[] get()
	{
		return this._reactions;
	}
	
	public void set(Object newState)
	{
		this._reactions = (Reaction[]) newState;
	}
	
	/*************************************************************************
	 * HASREACTIONS METHODS
	 ************************************************************************/
	
	public HashMap<String,Double> 
						get1stTimeDerivatives(HashMap<String,Double> concns)
	{
		HashMap<String,Double> out = new HashMap<String,Double>();
		//TODO
		return out;
	}
}