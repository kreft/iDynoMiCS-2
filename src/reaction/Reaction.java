package reaction;

import java.util.HashMap;
import java.util.Map;

import expression.Component;
import expression.ExpressionBuilder;

/**
 * 
 * @author baco
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 *
 */
public class Reaction
{
	/**
	 * Reaction stoichiometry
	 */
	private HashMap<String,Double> _stoichiometry = new HashMap<String,Double>();
	
	/**
	 * TODO
	 */
	private Component _kinetic;
	
	/**
	 * 
	 */
	private HashMap<String, Component> _diffKinetics;

	public Reaction(Map<String, Double> stoichiometry, String rate)
	{
		/**
		 * Lambda expressions are slow in Java, but okay if they are only used
		 * once
		 */
		stoichiometry.forEach((k,v)-> _stoichiometry.put(k, v));
		ExpressionBuilder e = new ExpressionBuilder(rate, new HashMap<String,Double>());
		this._kinetic = e.component;
	}
	
	public Reaction(String chemicalSpecies, double stoichiometry, String rate)
	{
		_stoichiometry.put(chemicalSpecies, stoichiometry);
		ExpressionBuilder e = new ExpressionBuilder(rate, new HashMap<String,Double>());
		this._kinetic = e.component;
	}
	
	/**
	 * Returns the reaction rate depending on concentrations
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
		HashMap<String, Double> out  = new HashMap<String,Double>();
		_stoichiometry.forEach((k,v)-> out.put(k, v * rate));
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 * @param withRespectTo
	 * @return
	 */
	public double getDiffRate(HashMap<String, Double> concentrations, 
														String withRespectTo)
	{
		/*
		 * If this is the first time we've tried to do this, make the HashMap.
		 */
		if ( this._diffKinetics == null )
			this._diffKinetics = new HashMap<String, Component>();
		/*
		 * If we haven't tried differentiating w.r.t. this variable, do so now.
		 */
		if ( ! this._diffKinetics.containsKey(withRespectTo) )
		{
			this._diffKinetics.put(withRespectTo,
								this._kinetic.differentiate(withRespectTo));
		}
		/*
		 * Finally, calculate and return the value at this set of
		 * concentrations.
		 */
		return this._diffKinetics.get(withRespectTo).getValue(concentrations);
	}


}
