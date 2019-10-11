package reaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import compartment.EnvironmentContainer;
import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import expression.Component;
import expression.Expression;
import generalInterfaces.Copyable;
import instantiable.Instantiable;
import instantiable.object.InstantiableList;
import instantiable.object.InstantiableMap;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
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
public class RegularReaction 
	implements Instantiable, Copyable, Settable, Reaction
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

	private ArrayList<String> _constituents;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Empty reaction for ReactionLibrary construction.
	 */
	public RegularReaction()
	{
		
	}
	
	/**
	 * \brief Construct a reaction from an XML node;
	 * 
	 * @param xmlNode XMl node from a protocol file.
	 */
	public RegularReaction(Node xmlNode, Settable parent)
	{
		Element elem = (Element) xmlNode;
		this.instantiate( elem, parent );
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
	public RegularReaction(
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
	public RegularReaction(
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
	public RegularReaction(String chemSpecies, double stoichiometry, 
											String kinetic, String name)
	{
		this( getHM(chemSpecies, stoichiometry), new Expression(kinetic), name);
	}
	
	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElem, Settable parent)
	{
		this._parentNode = parent;
		if ( parent instanceof EnvironmentContainer )
			((EnvironmentContainer) parent).addReaction(this);
		if (parent instanceof InstantiableList)
			((InstantiableList<RegularReaction>) parent).add(this);

		if ( !Helper.isNullOrEmpty(xmlElem) && XmlHandler.hasChild(xmlElem, XmlRef.reaction))
		{
			xmlElem = XmlHandler.findUniqueChild(xmlElem, XmlRef.reaction);
		}
		
		this._name = XmlHandler.obtainAttribute(xmlElem, XmlRef.nameAttribute, this.defaultXmlTag());
		/*
		 * Build the stoichiometric map.
		 */
		this._stoichiometry.instantiate( xmlElem, this, 
				String.class.getSimpleName(), Double.class.getSimpleName(), 
				XmlRef.stoichiometric );

		/*
		 * Build the reaction rate expression.
		 */
		if ( Helper.isNullOrEmpty(xmlElem) || !XmlHandler.hasChild(xmlElem, XmlRef.expression))
			this._kinetic = new Expression("");
		else
			this._kinetic = new 
				Expression(XmlHandler.findUniqueChild(xmlElem, XmlRef.expression));
		
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
		return new RegularReaction(stoichiometry, this._kinetic, this._name);
	}
	
	/* ***********************************************************************
	 * GETTERS
	 * **********************************************************************/
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getName()
	 */
	@Override
	public String getName()
	{
		return this._name;
	}
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getVariableNames()
	 */
	@Override
	public Collection<String> getVariableNames()
	{
		return this._kinetic.getAllVariablesNames();
	}
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getReactantNames()
	 */
	@Override
	public Collection<String> getReactantNames()
	{
		return this.getStoichiometryAtStdConcentration().keySet();
	}
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getConstituentNames()
	 */
	@Override
	public Collection<String> getConstituentNames()
	{
		if ( this._constituents == null )
		{
			this._constituents = new ArrayList<String>();
			this._constituents.addAll(this.getVariableNames());
		}
		for(String s : this.getStoichiometryAtStdConcentration().keySet() )
			if(! _constituents.contains(s) )
				this._constituents.add(s);
		return this._constituents;
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
	private double getRate(Map<String, Double> concentrations)
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
	private double getStoichiometry(String reactantName)
	{
		if ( this._stoichiometry.containsKey(reactantName) )
			return this._stoichiometry.get(reactantName);
		return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getStoichiometryAtStdConcentration()
	 */
	@Override
	public Map<String,Double> getStoichiometryAtStdConcentration()
	{
		return this._stoichiometry;
	}
	
	/* (non-Javadoc)
	 * @see reaction.ReactionInterface#getProductionRate(java.util.Map, java.lang.String)
	 */
	@Override
	public double getProductionRate(Map<String, Double> concentrations, 
														String reactantName)
	{
		checkNegatives(concentrations);
//		reaction_tally++;
//		if( reaction_tally/1000.0 == Math.round(reaction_tally/1000.0) && Log.shouldWrite(Log.Tier.DEBUG))
//			Log.out(Log.Tier.DEBUG, reaction_tally + " reactions");
		return this.getStoichiometry(reactantName) * 
											this.getRate(concentrations);
	}
	
	private void checkNegatives( Map<String, Double> concentrations )
	{
		for ( String s : concentrations.keySet() )
		{
			if( concentrations.get(s) < 0.0 )
				System.out.println( s + " "  + concentrations.get(s) );
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param concentrations
	 * @param withRespectTo
	 * @return
	 */
	// TODO never used, consider deletion
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
