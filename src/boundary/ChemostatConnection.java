package boundary;

import java.util.HashMap;

public class ChemostatConnection extends BoundaryConnected
{
	/**
	 * Positive for flow into the chemostat, negative for flow out.
	 */
	protected double _flowRate;
	
	protected HashMap<String, Double> _concentrations;
	
	public ChemostatConnection()
	{
		
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
}