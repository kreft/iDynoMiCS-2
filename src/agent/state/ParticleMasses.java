package agent.state;

import java.util.HashMap;

public class ParticleMasses implements HasMasses
{
	/**
	 * 
	 */
	protected HashMap<String, Double> _masses;
	
	/*************************************************************************
	 * STATE METHODS
	 ************************************************************************/
	
	public HashMap<String, Double> get()
	{
		return this._masses;
	}
	
	@SuppressWarnings("unchecked")
	public void set(Object newState) throws IllegalArgumentException
	{
		try
		{
			this._masses = (HashMap<String,Double>) newState;
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(
			"ParticleMasses state must be given as a HashMap<String,Double>");
		}
	}
	
	/*************************************************************************
	 * HASMASSES METHODS
	 ************************************************************************/
	
	/**
	 * @return The total mass of the agent (including all components).
	 */
	public double getTotal()
	{
		double sum = 0.0;
		for ( double val : this._masses.values() )
			sum += val;
		return sum;
	}
	
	/**
	 * @return The total volume of the agent.
	 */
	public double getVolume(HashMap<String,Double> densities)
	{
		double out = 0.0;
		for ( String particleName : this._masses.keySet() )
			out = this._masses.get(particleName) * densities.get(particleName);
		return out;
	}
}
