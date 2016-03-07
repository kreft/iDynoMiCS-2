package boundary;

import java.util.HashMap;

import dataIO.Log;
import dataIO.Log.Tier;

public class ChemostatConnection extends BoundaryConnected
{
	/**
	 * Positive for flow into the chemostat, negative for flow out.
	 */
	protected double _flowRate;
	
	protected HashMap<String, Double> _concentrations;
	
	protected double _agentsToDiluteTally;
	
	public ChemostatConnection()
	{
		super();
		/*
		 * A chemostat shouldn't ask for this, but if it does it will always
		 * get the origin.
		 * TODO 
		 */
		//this._gridMethod = 
	}
	
	public void setFlowRate(double flowRate)
	{
		this._flowRate = flowRate;
	}
	
	public double getFlowRate()
	{
		return this._flowRate;
	}
	
	public void setConcentrations(HashMap<String, Double> concns)
	{
		if ( this._flowRate < 0.0 )
			return;
		this._concentrations = concns;
		/*
		 * TODO set partner boundary concentrations in a more robust way
		 */
		if ( this._partnerBoundary instanceof ChemostatConnection )
		{
			((ChemostatConnection) this._partnerBoundary)
												.setConcentrations(concns);
		}
	}
	
	public HashMap<String, Double> getConcentrations()
	{
		/*
		 * TODO this should be done more robustly
		 */
		if ( this._flowRate < 0.0 )
			return null;
		return this._concentrations;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @return
	 */
	public double getConcentration(String soluteName)
	{
		if ( this._concentrations.containsKey(soluteName) )
			return this._concentrations.get(soluteName);
		return 0.0;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAgentsToDiluteTally()
	{
		return this._agentsToDiluteTally;
	}
	
	/**
	 * 
	 * @param timeStep
	 */
	public void updateAgentsToDiluteTally(double timeStep)
	{
		/* Remember to subtract, since flow rate out is negative. */
		this._agentsToDiluteTally -= this._flowRate * timeStep;
	}
	
	public void knockDownAgentsToDiluteTally()
	{
		this._agentsToDiluteTally--;
	}
	
	/**
	 * 
	 */
	public void resetAgentsToDiluteTally()
	{
		this._agentsToDiluteTally = 0.0;
	}
}