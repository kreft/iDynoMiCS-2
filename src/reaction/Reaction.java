package reaction;

import java.util.HashMap;
import java.util.Map;

import expression.Component;
import expression.ExpressionBuilder;

/**
 * \brief 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 */
public class Reaction
{
	/**
	 * Dictionary of reaction stoichiometries. Each chemical species involved
	 * in this reaction may be produced (stoichiometry > 0), consumed (< 0), or
	 * unaffected (stoichiometry = 0, or unlisted) by the reaction.
	 */
	private HashMap<String,Double> _stoichiometry = 
												new HashMap<String,Double>();
	/**
	 * The mathematical expression describing the rate at which this reaction
	 * proceeds.
	 */
	private Component _kinetic;
	/**
	 * Dictionary of mathematical expressions describing the differentiation
	 * of {@code this._kinetic} with respect to variables, whose names are
	 * stored as {@code Strings}.
	 */
	private HashMap<String, Component> _diffKinetics;
	
	private double _rate;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct a reaction from a dictionary of reactants and a
	 * {@code Component} description of the kinetic rate. 
	 * 
	 * @param stoichiometry Dictionary of amounts that each reactant is
	 * produced per reaction event (use negative values for reactants that are
	 * consumed).
	 * @param kinetic {@code Component} describing the rate at which this
	 * reaction proceeds.
	 */
	public Reaction(Map<String, Double> stoichiometry, Component kinetic)
	{
		this._stoichiometry.putAll(stoichiometry);
		this._kinetic = kinetic;
	}
	
	/**
	 * \brief Construct a reaction from a dictionary of reactants and a
	 * {@code String} description of the kinetic rate.
	 * 
	 * @param stoichiometry Dictionary of amounts that each reactant is
	 * produced per reaction event (use negative values for reactants that are
	 * consumed).
	 * @param kinetic {@code String} describing the rate at which this reaction
	 * proceeds.
	 */
	public Reaction(Map<String, Double> stoichiometry, String kinetic)
	{
		this(stoichiometry, getComponent(kinetic));
	}
	
	/**
	 * \brief Construct a reaction with a single reactant.
	 * 
	 * @param chemSpecies The name of the chemical species which is the sole
	 * reactant in this reaction.
	 * @param stoichiometry The amount of this reactant that is produced per
	 * reaction event (use a negative value here for a reactant that is
	 * consumed).
	 * @param kinetic {@code String} describing the rate at which this reaction
	 * proceeds.
	 */
	public Reaction(String chemSpecies, double stoichiometry, String kinetic)
	{
		this(getHM(chemSpecies, stoichiometry), getComponent(kinetic));
	}
	
	/*************************************************************************
	 * GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Calculate the reaction rate depending on concentrations.
	 * 
	 * <p>Note that this rate is in units of "reaction events" per unit time.
	 * To find the rate of production of a particular reactant, use 
	 * {@link #getFluxes(HashMap)}. </p>
	 * 
	 * @param concentrations Dictionary of concentrations of reactants.
	 * @return The rate of this reaction.
	 * @see #getFluxes(HashMap)
	 */
	public double getRate(HashMap<String, Double> concentrations)
	{
		return this._kinetic.getValue(concentrations);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 */
	public double updateRate(HashMap<String, Double> concentrations)
	{
		return (this._rate = this.getRate(concentrations));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param reactantName
	 */
	public double getProductionRate(String reactantName)
	{
		if ( this._stoichiometry.containsKey(reactantName) )
			return this._stoichiometry.get(reactantName) * this._rate;
		return 0.0;
	}
	
	/**
	 * \brief Calculate the rates of production for each reactant in this
	 * reaction's stoichiometry, writing the result into the <b>fluxes</b>
	 * {@code HashMap<String,Double>} given.
	 * 
	 * @param concentrations
	 * @param fluxes
	 */
	public void putFluxes(HashMap<String, Double> concentrations,
												HashMap<String, Double> fluxes)
	{
		double rate = this.getRate(concentrations);
		for ( String name : this._stoichiometry.keySet() )
			fluxes.put(name, this._stoichiometry.get(name) * rate);
	}
	
	/**
	 * \brief Calculate the rates of production for each reactant in this
	 * reaction's stoichiometry, writing the result into a new dictionary.
	 * 
	 * @param concentrations
	 * @return
	 */
	public HashMap<String, Double> getFluxes(
									HashMap<String, Double> concentrations)
	{
		HashMap<String, Double> fluxes  = new HashMap<String,Double>();
		this.putFluxes(concentrations, fluxes);
		return fluxes;
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
	
	/*************************************************************************
	 * MISCELLANEOUS METHODS
	 ************************************************************************/
	
	/**
	 * \brief Get a {@code Component} from a {@code String}.
	 * 
	 * @param expression {@code String} describing the mathematical expression.
	 * @return {@code Component} describing the mathematical expression.
	 */
	private static Component getComponent(String expression)
	{
		ExpressionBuilder e = new ExpressionBuilder(expression);
		return e.component;
	}
	
	/**
	 * \brief Get a {@code HashMap<String,Double>} initialised with a single
	 * pair binding.
	 * 
	 * <p>This is necessary because
	 * "{@code new HashMap<String,Double>().put(key, value)}" returns a
	 * {@code Double} rather than a {@code HashMap}!</p>
	 * 
	 * @param key {@code String} name of the only key.
	 * @param value {@code double} of the only value.
	 * @return Initialised {@code HashMap<String,Double>} with this binding. 
	 */
	private static HashMap<String, Double> getHM(String key, double value)
	{
		HashMap<String,Double> out = new HashMap<String,Double>();
		out.put(key, value);
		return out;
	}
}
