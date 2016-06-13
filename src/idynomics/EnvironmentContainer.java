package idynomics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import grid.ArrayType;
import grid.SpatialGrid;
import reaction.Reaction;
import shape.Shape;

/**
 * \brief Manages the solutes in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class EnvironmentContainer implements CanPrelaunchCheck
{
	/**
	 * This dictates both geometry and size, and it inherited from the
	 * {@code Compartment} this {@code EnvrionmentContainer} belongs to.
	 */
	protected Shape _shape;
	/**
	 * Collection of solutes (each SpatialGrid knows its own name).
	 */
	protected Collection<SpatialGrid> _solutes = 
			new LinkedList<SpatialGrid>();
	/**
	 * Dictionary of extracellular reactions.
	 */
	// TODO convert to a Collection, since Reaction now has a name variable
	protected Map<String, Reaction> _reactions = 
											new HashMap<String, Reaction>();
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Construct an {@code EnvironmentContainer} from a {@code Shape}.
	 * 
	 * @param aShape {@code Shape} object (see shape.ShapeLibrary).
	 */
	public EnvironmentContainer(Shape aShape)
	{
		this._shape = aShape;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this.addSolute(soluteName, 0.0);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param initialConcn
	 */
	public void addSolute(String soluteName, double initialConcn)
	{
		// TODO safety: check if solute already present
		SpatialGrid sg = this._shape.getNewGrid(soluteName);
		sg.newArray(ArrayType.CONCN, initialConcn);
		this._solutes.add(sg);
		Log.out(Tier.DEBUG, "Added solute \""+soluteName+"\" to environment");
	}
	
	/**
	 * \brief TODO
	 * 
	 * NOTE Rob[26Feb2016]: not yet used, work in progress
	 * 
	 * TODO Get general solutes from Param?
	 * 
	 * @param soluteNodes
	 */
	public void readSolutes(NodeList soluteNodes)
	{
		Element elem;
		String name, concn;
		double concentration;
		for ( int i = 0; i < soluteNodes.getLength(); i++)
		{
			elem = (Element) soluteNodes.item(i);
			name = XmlHandler.obtainAttribute(elem, XmlRef.nameAttribute);
			/* Try to read in the concentration, using zero by default. */
			concn = XmlHandler.gatherAttribute(elem, XmlRef.concentration);
			concentration = ( concn.equals("") ) ? 0.0 : Double.valueOf(concn);
			/* Finally, add the solute to the list. */
			this.addSolute(name, concentration);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * NOTE Rob[26Feb2016]: not yet used, work in progress
	 * 
	 * TODO Get general reactions from Param?
	 * 
	 * @param reactionNodes
	 */
	public void readReactions(NodeList reactionNodes)
	{
		Element elem;
		String name;
		Reaction reac;
		for ( int i = 0; i < reactionNodes.getLength(); i++)
		{
			elem = (Element) reactionNodes.item(i);
			// TODO does a reaction need to have a name?
			name = XmlHandler.obtainAttribute(elem, XmlRef.nameAttribute);
			/* Construct and intialise the reaction. */
			reac = (Reaction) Reaction.getNewInstance(elem);
			reac.init(elem);
			/* Add it to the environment. */
			this.addReaction(reac, name);
		}
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	public Shape getShape()
	{
		return this._shape;
	}
	
	public Collection<String> getSoluteNames()
	{
		Collection<String> out = new LinkedList<String>();
		for ( SpatialGrid sg : this._solutes )
			out.add(sg.getName());
		return out;
	}
	
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName() == soluteName )
				return sg;
		return null;
	}
	
	public boolean isSoluteName(String name)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName() == name )
				return true;
		return false;
	}
	
	public Collection<SpatialGrid> getSolutes()
	{
		return this._solutes;
	}
	
	/**
	 * \brief Get a list of this {@code Compartment}'s extracellular reactions.
	 * 
	 * @return
	 */
	public Collection<Reaction> getReactions()
	{
		return this._reactions.values();
	}
	
	/**
	 * get specific reaction 
	 * @param reaction
	 * @return
	 */
	public Reaction getReaction(String reaction)
	{
		return _reactions.get(reaction);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param reaction
	 * @param name
	 */
	public void addReaction(Reaction reaction, String name)
	{
		this._reactions.put(name, reaction);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @return
	 */
	public double getAverageConcentration(String soluteName)
	{
		return this.getSoluteGrid(soluteName).getAverage(ArrayType.CONCN);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Map<String,Double> getAverageConcentrations()
	{
		Map<String,Double> out = new HashMap<String,Double>();
		for ( String name : this.getSoluteNames() )
			out.put(name, this.getAverageConcentration(name));
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param newConcn
	 */
	public void setAllConcentration(String soluteName, double newConcn)
	{
		this.getSoluteGrid(soluteName).setAllTo(ArrayType.CONCN, newConcn);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._shape.getOtherBoundaries();
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public void printSolute(String soluteName)
	{
		Log.out(Tier.QUIET, soluteName+":");
		Log.out(Tier.QUIET, this.getSoluteGrid(soluteName).arrayAsText(ArrayType.CONCN));
	}
	
	public void printAllSolutes()
	{
		for(String s : this.getSoluteNames())
			this.printSolute(s);
	}
	
	/* ***********************************************************************
	 * PRE-LAUNCH CHECK
	 * **********************************************************************/
	
	@Override
	public boolean isReadyForLaunch()
	{
		// TODO
		return true;
	}
}
