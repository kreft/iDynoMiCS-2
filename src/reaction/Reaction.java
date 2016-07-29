package reaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import expression.Component;
import expression.ExpressionB;
import generalInterfaces.Copyable;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.primarySetters.BundleMap;
import nodeFactory.primarySetters.HashMapSetter;
import referenceLibrary.ClassRef;
import referenceLibrary.ObjectRef;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;

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
public class Reaction implements Instantiatable, Copyable, NodeConstructor
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
	protected NodeConstructor _parentNode;
	/**
	 * Dictionary of reaction stoichiometries. Each chemical species involved
	 * in this reaction may be produced (stoichiometry > 0), consumed (< 0), or
	 * unaffected (stoichiometry = 0, or unlisted) by the reaction.
	 */
	private BundleMap<String,Double> _stoichiometry = new BundleMap<String,Double>(
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
		this.init(elem);
	}
	
	public Reaction(Node xmlNode, NodeConstructor parent)
	{
		Element elem = (Element) xmlNode;
		this._parentNode = parent;
		this.init(elem);
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
		this(stoichiometry, new ExpressionB(kinetic), name);
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
		this(getHM(chemSpecies, stoichiometry), new ExpressionB(kinetic), name);
	}
	
	public void init(Element xmlElem)
	{
		this._name = XmlHandler.obtainAttribute(xmlElem, XmlRef.nameAttribute, this.defaultXmlTag());
		/*
		 * Build the stoichiometric map.
		 */
		this._stoichiometry.init(xmlElem, this);

		/*
		 * Build the reaction rate expression.
		 */
		if ( xmlElem == null || !XmlHandler.hasNode(xmlElem, XmlRef.expression))
			this._kinetic = new ExpressionB("");
		else
			this._kinetic = new 
				ExpressionB(XmlHandler.loadUnique(xmlElem, XmlRef.expression));
	}
	
	public void init(Element xmlElem, NodeConstructor parent)
	{
		this.init(xmlElem);
		this._parentNode = parent;
		parent.addChildObject(this);
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
	
	/* ***********************************************************************
	 * XML-ABLE
	 * **********************************************************************/
	
	/**
	 * XMLable interface implementation.
	 * @param xmlNode
	 * @return
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		if (XmlHandler.hasNode((Element) xmlNode, XmlRef.reaction))
		{
			xmlNode = XmlHandler.loadUnique((Element) xmlNode, XmlRef.reaction);
		}
		return new Reaction(xmlNode);
	}
	
	// TODO required from xmlable interface.. unfinished
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(XmlRef.reaction, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add(((ExpressionB) _kinetic).getNode());
		
		modelNode.add( this._stoichiometry.getNode() );

		
		return modelNode;
	}
	
	public ModelNode getStoNode(NodeConstructor constructor, String component, 
			Double coefficient) {
		
		ModelNode modelNode = new ModelNode(XmlRef.stoichiometric, constructor);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.add(new ModelAttribute(XmlRef.component, 
				component, null, false ));
		
		modelNode.add(new ModelAttribute(XmlRef.coefficient, 
				String.valueOf(coefficient), null, false ));
		
		return modelNode;
	}

	public void removeNode(String specifier)
	{
		this._parentNode.removeChildNode(this);
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
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
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public NodeConstructor getParent() 
	{
		return this._parentNode;
	}

}
