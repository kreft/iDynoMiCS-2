package reaction;

import java.util.ArrayList;
import java.util.HashMap;

import expression.*;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Reaction_depcricated
{
	/**
	 * TODO
	 */
	protected Component _kinetic;
	
	/**
	 * TODO
	 */
	protected HashMap<String, Component> _diffKinetics;
	
	/**
	 * TODO
	 * 
	 * TODO Insist that on no overlapping names between solutes and particles.
	 */
	protected HashMap<String, Double> _stoichiometry;
	
	/**
	 * this is a dummy constructor
	 * TODO constructors?
	 */
	public Reaction_depcricated(String construct)
	{
		
	}
	
	public Reaction_depcricated()
	{
		
	}
	
	/*************************************************************************
	 * COMMONLY USED REACTIONS
	 * Note that stoichiometries must be set after the reaction is created.
	 ************************************************************************/
	
	/**
	 * FIXME Bas[3NOV2015]: method comes from HasReaction (depreciated) used in
	 * solver methods I've put it here for now.
	 */
	public HashMap<String,Double> 
			get1stTimeDerivatives(HashMap<String,Double> concns)
	{
		return concns;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param rate
	 * @return
	 */
	public static Reaction_depcricated constitutiveReaction(double rate)
	{
		Reaction_depcricated out = new Reaction_depcricated();
		Component c1 = new Constant("rate", rate);
		out.setKinetic(c1);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param k
	 * @param variables
	 * @return
	 */
	public static Reaction_depcricated massLawReaction(double k, ArrayList<String> variables)
	{
		Reaction_depcricated out = new Reaction_depcricated();
		Component c1 = new Constant("k", k);
		for ( String var : variables )
			c1 = Expression.multiply(c1, new Variable(var));
		out.setKinetic(c1);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param muMax
	 * @param kS
	 * @param substrate
	 * @param catalyst
	 * @return
	 */
	public static Reaction_depcricated monodReaction(double muMax, double kS, 
											String substrate, String catalyst)
	{
		Reaction_depcricated out = new Reaction_depcricated();
		Component c1 = new Constant("muMax", muMax);
		Component c2 = new Constant("kS", kS);
		Component c3 = new Variable(substrate);
		Component c4 = new Variable(catalyst);
		c1 = Expression.multiply(c1, c3);
		c2 = Expression.add(c2, c3);
		c3 = new Division(c1, c2);
		out.setKinetic(Expression.multiply(c3, c4));
		return out;
	}
	
	
	/*************************************************************************
	 * SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Set this reaction's kinetic rate law.
	 * 
	 * @param c
	 */
	public void setKinetic(Component c)
	{
		this._kinetic = c;
	}
	
	/**
	 * \brief Set this reaction's stoichiometries.
	 * 
	 * <p>I.e. the amount of each substance that is produced (positive) or
	 * consumed (negative) each time this reaction occurs.</p> 
	 * 
	 * @param stoichiometry
	 */
	public void setStroichiometry(HashMap<String, Double> stoichiometry)
	{
		this._stoichiometry = stoichiometry;
	}
	
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
	 * REPORTING
	 ************************************************************************/
	
}
