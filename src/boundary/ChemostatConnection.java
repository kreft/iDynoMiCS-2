package boundary;

import java.util.HashMap;

import grid.SpatialGrid.GridMethod;
import linearAlgebra.Vector;

public class ChemostatConnection extends BoundaryConnected
{
	/**
	 * Positive for flow into the chemostat, negative for flow out.
	 */
	protected double _flowRate;
	
	protected HashMap<String, Double> _concentrations;
	
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
}