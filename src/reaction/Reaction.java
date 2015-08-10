package reaction;

import java.util.HashMap;

import expression.Component;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public abstract class Reaction
{
	/**
	 * TODO
	 */
	protected Component _kinetic;
	
	/**
	 * TODO
	 * 
	 * TODO Insist that on no overlapping names between solutes and particles.
	 */
	protected HashMap<String, Double> _stoichiometry;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Reaction()
	{
		
	}
	
	
	
	
	/*************************************************************************
	 * SETTERS
	 ************************************************************************/
	
	
	/*************************************************************************
	 * GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Makes a deep copy of this Reaction's stoichiometry HashMap.
	 * 
	 * TODO there may be a more elegant way of doing this.
	 * 
	 * @return
	 */
	public HashMap<String, Double> copyStoichiometry()
	{
		HashMap<String, Double> out = new HashMap<String, Double>();
		for ( String key : this._stoichiometry.keySet() )
			out.put(key, this._stoichiometry.get(key));
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 * @return
	 */
	public double getRate(HashMap<String, Double> concentrations)
	{
		return this._kinetic.getValue(concentrations);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 * @return
	 */
	public HashMap<String, Double> getFluxes(
									HashMap<String, Double> concentrations)
	{
		double rate = this.getRate(concentrations);
		HashMap<String, Double> out = this.copyStoichiometry();
		out.replaceAll((s, d) -> {return d * rate;});
		return out;
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}
