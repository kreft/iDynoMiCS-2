package reaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import expression.Component;
import expression.Expression;
import generalInterfaces.Copyable;
import idynomics.EnvironmentContainer;
import instantiable.Instantiable;
import instantiable.object.InstantiableMap;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import utility.Helper;

/**
 * \brief The reaction class handles the chemical conversions of various
 * different molecule types, which may be present in the environment or owned
 * by agents.
 * 
 * <p>The core concepts are <i>rate</i> and <i>stoichiometry</i>. Rate
 * determines how many reaction events happen per unit time. Stoichiometry
 * determines how much of each reactant is produced (positive) or consumed
 * (negative) in each single reaction event.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 */
public class Reaction implements Instantiable, Copyable, Settable
{
	/**
	 * The name of this reaction. This is particularly useful for writing
	 * reaction rate to a grid as output.
	 */
	protected String _name;
	
	/**
	 * identifies what environment hosts this reaction, null if this reaction
	 * is not a compartment reaction
	 */
	protected Settable _parentNode;
	/**
	 * Dictionary of reaction stoichiometries. Each chemical species involved
	 * in this reaction may be produced (stoichiometry > 0), consumed (< 0), or
	 * unaffected (stoichiometry = 0, or unlisted) by the reaction.
	 */
	private InstantiableMap<String,Double> _stoichiometry = new InstantiableMap<String,Double>(
			String.class, Double.class, XmlRef.component, XmlRef.coefficient, 
			XmlRef.stoichiometry, XmlRef.stoichiometric, true);
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
	// TODO consider deletion
	private HashMap<String, Component> _diffKinetics;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Empty reaction for ReactionLibrary construction.
	 */
	// TODO check this is the right approach
	public Reaction()
	{
		
	}
	
	/**
	 * \brief Construct a reaction from an XML node;
	 * 
	 * @param xmlNode XMl node from a protocol file.
	 */
	public Reaction(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		this.instantiate(elem, null);
	}
	
	public Reaction(Node xmlNode, Settable parent)
	{
		Element elem = (Element) xmlNode;
		this.instantiate(elem, parent);
	}
	
	/**
	 * \brief Construct a reaction from a dictionary of reactants and a
	 * {@code Component} description of the kinetic rate. 
	 * 
	 * @param stoichiometry Dictionary of amounts that each reactant is
	 * produced per reaction event (use negative values for reactants that are
	 * consumed).
	 * @param kinetic {@code Component} describing the rate at which this
	 * reaction proceeds.
	 * @param name Name of the reaction.
	 */
	public Reaction(
			Map<String,Double> stoichiometry, Component kinetic, String name)
	{
		this._name = name;
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
	 * @param name Name of the reaction.
	 */
	public Reaction(
			Map<String, Double> stoichiometry, String kinetic, String name)
	{
		this(stoichiometry, new Expression(kinetic), name);
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
	 * @param name Name of the reaction.
	 */
	public Reaction(String chemSpecies, double stoichiometry, 
											String kinetic, String name)
	{
		this(getHM(chemSpecies, stoichiometry), new Expression(kinetic), name);
	}
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		this._parentNode = parent;
		if ( parent instanceof EnvironmentContainer )
			((EnvironmentContainer) parent).addReaction(this);

		if ( !Helper.isNone(xmlElem) && XmlHandler.hasNode(xmlElem, XmlRef.reaction))
		{
			xmlElem = XmlHandler.loadUnique(xmlElem, XmlRef.reaction);
		}
		
		this._name = XmlHandler.obtainAttribute(xmlElem, XmlRef.nameAttribute, this.defaultXmlTag());
		/*
		 * Build the stoichiometric map.
		 */
		this._stoichiometry.instantiate(xmlElem, this);

		/*
		 * Build the reaction rate expression.
		 */
		if ( Helper.isNone(xmlElem) || !XmlHandler.hasNode(xmlElem, XmlRef.expression))
			this._kinetic = new Expression("");
		else
			this._kinetic = new 
				Expression(XmlHandler.loadUnique(xmlElem, XmlRef.expression));
		
	}
	
	/**
	 * Copyable implementation
	 */
	public Object copy()
	{
		@SuppressWarnings("unchecked")
		HashMap<String,Double> stoichiometry = (HashMap<String,Double>)
				ObjectFactory.copy(this._stoichiometry);
		// NOTE: _kinetic is not copyable, this will become an issue of you
		// want to do evo addaptation simulations
		return new Reaction(stoichiometry, this._kinetic, this._name);
	}
	
	/* ***********************************************************************
	 * GETTERS
	 * **********************************************************************/
	
	/**
	 * @return Name of this reaction.
	 */
	public String getName()
	{
		return this._name;
	}
	
	/**
	 * @return Names of all variables in the rate of this reaction.
	 */
	public Collection<String> getVariableNames()
	{
		return this._kinetic.getAllVariablesNames();
	}
	
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
	public double getRate(Map<String, Double> concentrations)
	{
		double out = this._kinetic.getValue(concentrations);
		//String msg = "   reaction \""+this._name+"\" variables: ";
		//for ( String name : concentrations.keySet() )
		//	msg += name +"@"+concentrations.get(name)+" ";
		//Log.out(Tier.DEBUG, msg+"=> rate "+out);
		return out;
	}
	
	/**
	 * \brief Fetch the amount of this chemical species produced per reaction
	 * event.
	 * 
	 * @param reactantName The name of the chemical species of interest.
	 * @return The stoichiometry of this chemical species in this reaction.
	 */
	public double getStoichiometry(String reactantName)
	{
		if ( this._stoichiometry.containsKey(reactantName) )
			return this._stoichiometry.get(reactantName);
		return 0.0;
	}
	
	/**
	 * @return This reaction's whole stoichiometric dictionary.
	 */
	public Map<String,Double> getStoichiometry()
	{
		return this._stoichiometry;
	}
	
	/**
	 * \brief Calculate the production rate of a given chemical species.
	 * 
	 * <p>Note that the reaction rate is calculated each time this method is
	 * called. If you are likely to want the production rates of many
	 * reactants, call {@link #getRate(HashMap)} first, then multiply it with
	 * {@link #getStoichiometry(String)} for each solute separately.</p>
	 * 
	 * @param concentrations Dictionary of concentrations of reactants.
	 * @param reactantName The name of the chemical species of interest.
	 * @return The rate of production (positive) or consumption (negative) of
	 * this reactant chemical species.
	 */
	public double getProductionRate(Map<String, Double> concentrations, 
														String reactantName)
	{
		return this.getStoichiometry(reactantName) * 
											this.getRate(concentrations);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 * @param withRespectTo
	 * @return
	 */
	// TODO consider deletion
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
	
	
	// TODO required from xmlable interface.. unfinished
	public Module getModule()
	{
		Module modelNode = new Module(XmlRef.reaction, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add(((Expression) _kinetic).getModule());
		
		modelNode.add( this._stoichiometry.getModule() );

		
		return modelNode;
	}
	
	public Module getStoNode(Settable constructor, String component, 
			Double coefficient) {
		
		Module modelNode = new Module(XmlRef.stoichiometric, constructor);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.add(new Attribute(XmlRef.component, 
				component, null, false ));
		
		modelNode.add(new Attribute(XmlRef.coefficient, 
				String.valueOf(coefficient), null, false ));
		
		return modelNode;
	}

	public void removeModule(String specifier)
	{
		if (this.getParent() instanceof EnvironmentContainer)
			((EnvironmentContainer) this.getParent()).deleteReaction(this);
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlRef.reaction;
	}
	
	/* ***********************************************************************
	 * MISCELLANEOUS METHODS
	 * **********************************************************************/
	
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

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}

}
