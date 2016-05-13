package idynomics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import reaction.Reaction;
import shape.Shape;
import shape.ShapeConventions.DimName;

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
	 * TODO replace with resolution calculator
	 */
	protected double _defaultResolution = 1.0;
	
	/**
	 * Dictionary of solutes.
	 */
	protected HashMap<String, SpatialGrid> _solutes = 
										new HashMap<String, SpatialGrid>();
	/**
	 * Dictionary of average solute concentrations (useful for chemostat).
	 */
	protected HashMap<String, Double> _averageConcns;
	/**
	 * Dictionary of extracellular reactions.
	 */
	protected HashMap<String, Reaction> _reactions = 
											new HashMap<String, Reaction>();
	/**
	 * Solutes can only be added while this is {@code false}, and the simulation
	 * cannot begin until it is {@code true}.
	 */
	protected boolean _hasInitialised = false;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
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
	 * This should be done after the shape is set up and all solutes added.
	 * TODO make more robust. 
	 */
	public void init()
	{
		SpatialGrid aSG;
		Boundary[] boundaries;
		for ( DimName dimName : this._shape.getDimensionNames() )
		{
			boundaries = this._shape.getDimension(dimName).getBoundaries();
			for ( int i = 0; i < 2; i++ )
				for ( String soluteName : this._solutes.keySet() )
				{
					aSG = this._solutes.get(soluteName);
					aSG.addBoundary(dimName, i,
									boundaries[i].getGridMethod(soluteName));
				}
		}
		this._hasInitialised = true;
	}
	
	/**
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this.addSolute(soluteName, 0.0, null);
	}
	

	/**
	 * @param soluteName
	 * @param initialConcn
	 */
	public void addSolute(String soluteName, double initialConcn)
	{
		this.addSolute(soluteName, 0.0, null);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName, Element resolution)
	{
		this.addSolute(soluteName, 0.0, resolution);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param initialConcn
	 */
	public void addSolute(String soluteName, double initialConcn, 
															Element resolution)
	{
		if ( this._hasInitialised )
		{
			throw new Error("Cannot add new solutes after the environment"+
												" container has initialised!");
		}
		/*
		 * TODO safety: check if solute already in hashmap
		 */
		
		SpatialGrid sg = this._shape.gridGetter().newGrid(
				this._shape.getDimensionLengths(), resolution);
		sg.newArray(ArrayType.CONCN, initialConcn);
		this._solutes.put(soluteName, sg);
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
			name = XmlHandler.obtainAttribute(elem, XmlLabel.nameAttribute);
			/* Try to read in the concentration, using zero by default. */
			concn = XmlHandler.gatherAttribute(elem, XmlLabel.concentration);
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
			name = XmlHandler.obtainAttribute(elem, XmlLabel.nameAttribute);
			/* Construct and intialise the reaction. */
			reac = (Reaction) Reaction.getNewInstance(elem);
			reac.init(elem);
			/* Add it to the environment. */
			this.addReaction(reac, name);
		}
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Shape getShape()
	{
		return this._shape;
	}
	
	public Set<String> getSoluteNames()
	{
		return this._solutes.keySet();
	}
	
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		return this._solutes.get(soluteName);
	}
	
	public boolean isSoluteName(String name)
	{
		return this._solutes.containsKey(name);
	}
	
	public HashMap<String, SpatialGrid> getSolutes()
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
		return this._solutes.get(soluteName).getAverage(ArrayType.CONCN);
	}
	
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
		this._solutes.get(soluteName).setAllTo(ArrayType.CONCN, newConcn);
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
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printSolute(String soluteName)
	{
		Log.out(Tier.QUIET, soluteName+":");
		Log.out(Tier.QUIET, this._solutes.get(soluteName).arrayAsText(ArrayType.CONCN));
	}
	
	public void printAllSolutes()
	{
		for(String s : this.getSoluteNames())
			this.printSolute(s);
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! this._hasInitialised )
			return false;
		// TODO
		return true;
	}
}
