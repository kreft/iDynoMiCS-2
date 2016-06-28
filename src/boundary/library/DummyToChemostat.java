/**
 * 
 */
package boundary.library;

import java.util.HashMap;
import java.util.Map;

import boundary.Boundary;

/**
 * \brief 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class DummyToChemostat extends Boundary
{
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();

	@Override
	protected Class<?> getPartnerClass()
	{
		return null;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	public void setConcentration(String name, double concn)
	{
		this._concns.put(name, concn);
	}
	
	private double getConcentration(String name)
	{
		if ( this._concns.containsKey(name) )
			return this._concns.get(name);
		return 0.0;
	}
	
	@Override
	public void updateMassFlowRates()
	{
		for ( String name : this._environment.getSoluteNames() )
		{
			this._massFlowRate.put(name, 
					this.getConcentration(name) * this._volumeFlowRate);
		}
	}
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

}
