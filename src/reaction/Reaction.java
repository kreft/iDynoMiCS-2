
package reaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import expression.Component;
import expression.ExpressionB;
import generalInterfaces.Copyable;
import generalInterfaces.XMLable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 */
public class Reaction implements XMLable, Copyable, NodeConstructor
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
	
	/**
	 * reaction name
	 * NOTE Bas: if you indeed would like agents to write reactions to a grid
	 * they can only do so if they can refer to it by a name
	 */
	public String _name;
	
	/**
	 * TODO
	 */
	public List<String> variableNames;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct a reaction from an XML node;
	 * 
	 * @param xmlNode
	 */
	public Reaction(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
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
	 */
	public Reaction(Map<String, Double> stoichiometry, Component kinetic, String name)
	{
		this._name = name;
		this._stoichiometry.putAll(stoichiometry);
		this._kinetic = kinetic;
		this.variableNames = this._kinetic.getAllVariablesNames();
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
	public Reaction(Map<String, Double> stoichiometry, String kinetic, String name)
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
	 */
	public Reaction(String chemSpecies, double stoichiometry, 
											String kinetic, String name)
	{
		this(getHM(chemSpecies, stoichiometry), new ExpressionB(kinetic), name);
	}
	
	public void init(Element xmlElem)
	{
		this._name = XmlHandler.obtainAttribute(xmlElem, XmlLabel.nameAttribute);
		/*
		 * Build the stoichiometric map.
		 */
		NodeList stoichs = XmlHandler.getAll(xmlElem, XmlLabel.stoichiometry);
		String str;
		double coeff;
		for ( int i = 0; i < stoichs.getLength(); i++ )
		{
			Element temp = (Element) stoichs.item(i);
			/* Get the coefficient. */
			str = XmlHandler.obtainAttribute(temp, XmlLabel.coefficient);
			coeff = Double.valueOf(str);
			/* Get the component name. */
			str = XmlHandler.obtainAttribute(temp, XmlLabel.component);
			/* Enter these into the stoichiometry. */
			this._stoichiometry.put(str, coeff);
		}
		/*
		 * Build the reaction rate expression.
		 */
		this._kinetic = new 
			ExpressionB(XmlHandler.loadUnique(xmlElem, XmlLabel.expression));
		this.variableNames = this._kinetic.getAllVariablesNames();
	}
	
	/**
	 * Copyable implementation
	 */
	public Object copy() {
		HashMap<String,Double> sto = 
				new HashMap<String,Double>();
		for(String key : _stoichiometry.keySet())
			sto.put(key, (double) ObjectFactory.copy(_stoichiometry.get(key)));
		// NOTE: _kinetic is not copyable, this will become an issue of you
		// want to do evo addaptation simulations
		return new Reaction(sto, _kinetic, _name);
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
	 * XML-ABLE
	 ************************************************************************/
	
	/**
	 * XMLable interface implementation.
	 * @param xmlNode
	 * @return
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		if (XmlHandler.hasNode((Element) xmlNode, XmlLabel.reaction))
		{
			xmlNode = XmlHandler.loadUnique((Element) xmlNode, XmlLabel.reaction);
		}
		return new Reaction(xmlNode);
	}
	
	@Override
	public String getXml() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// TODO required from xmlable interface.. unfinished
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(XmlLabel.reaction, this);
		modelNode.requirement = Requirements.ZERO_OR_ONE;
		modelNode.title = this._name;
		
		modelNode.add(new ModelAttribute(XmlLabel.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add(((ExpressionB) _kinetic).getNode());
		
		return modelNode;
	}
	
	@Override
	public void setNode(ModelNode node) 
	{
		for(ModelNode n : node.childNodes)
			n.constructor.setNode(n);
	}

	@Override
	public NodeConstructor newBlank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlLabel.reaction;
	}
	
	/*************************************************************************
	 * MISCELLANEOUS METHODS
	 ************************************************************************/
	
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
