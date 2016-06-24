package idynomics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import boundary.SpatialBoundary;
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
	 * Grid of common attributes, such as well-mixed.
	 */
	protected SpatialGrid _commonGrid;
	/**
	 * Name of the common grid.
	 */
	public final static String COMMON_GRID_NAME = "common";
	/**
	 * Collection of solutes (each SpatialGrid knows its own name).
	 */
	protected Collection<SpatialGrid> _solutes = new LinkedList<SpatialGrid>();
	/**
	 * Collection of extracellular reactions (each Reaction knows its own name).
	 */
	protected Collection<Reaction> _reactions = new LinkedList<Reaction>();
	
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
		this._commonGrid = this._shape.getNewGrid(COMMON_GRID_NAME);
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
		Reaction reac;
		for ( int i = 0; i < reactionNodes.getLength(); i++)
		{
			elem = (Element) reactionNodes.item(i);
			/* Construct and intialise the reaction. */
			reac = (Reaction) Reaction.getNewInstance(elem);
			reac.init(elem);
			/* Add it to the environment. */
			this.addReaction(reac);
		}
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	/**
	 * @return The shape of this compartment.
	 */
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * @return The names of all solutes present in this compartment.
	 */
	public Collection<String> getSoluteNames()
	{
		Collection<String> out = new LinkedList<String>();
		for ( SpatialGrid sg : this._solutes )
			out.add(sg.getName());
		return out;
	}
	
	/**
	 * \brief Get the spatial grid for a solute.
	 * 
	 * <p>Returns null if this solute is not present.</p>
	 * 
	 * @param soluteName Name of a solute.
	 * @return The spatial grid object representing this solute.
	 */
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName().equals(soluteName) )
				return sg;
		Log.out(Tier.CRITICAL,
				"EnvironmentContainer can't find grid for \""+soluteName+"\"");
		return null;
	}
	
	/**
	 * \brief Checks if this compartment has a solute with the name given.
	 * 
	 * @param name Name of a potential solute.
	 * @return {@code true} if there is a solute with this name in this
	 * compartment, {@code false} if there is not.
	 */
	public boolean isSoluteName(String name)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName().equals(name) )
				return true;
		return false;
	}
	
	/**
	 * @return The SpatialGrid representation for every solute present in this
	 * compartment.
	 */
	public Collection<SpatialGrid> getSolutes()
	{
		return this._solutes;
	}
	
	/**
	 * @return All of this {@code Compartment}'s extracellular reactions.
	 */
	public Collection<Reaction> getReactions()
	{
		return this._reactions;
	}
	
	/**
	 * \brief Get a specific reaction by name.
	 * 
	 * <p>Returns null if this reaction is unknown here.</p>
	 * 
	 * @param name Name of the reaction required.
	 * @return {@code true} if there is an extracellular reaction with this
	 * name in this compartment, {@code false} if there is not.
	 */
	public Reaction getReaction(String name)
	{
		for ( Reaction reac : this._reactions )
			if ( reac.getName().equals(name) )
				return reac;
		return null;
	}
	
	/**
	 * \brief Give this compartment an extracellular reaction.
	 * 
	 * @param reaction Reaction to add to this compartment.
	 */
	public void addReaction(Reaction reaction)
	{
		// TODO Safety: check this reaction is not already present?
		this._reactions.add(reaction);
	}
	
	/**
	 * \brief Get the average concentration of a solute.
	 * 
	 * @param soluteName Name of the solute to use.
	 * @return Average (arithmetic mean) concentration of this solute.
	 */
	public double getAverageConcentration(String soluteName)
	{
		return this.getSoluteGrid(soluteName).getAverage(ArrayType.CONCN);
	}
	
	/**
	 * @return Average concentrations of all solutes in this compartment.
	 */
	public Map<String,Double> getAverageConcentrations()
	{
		Map<String,Double> out = new HashMap<String,Double>();
		for ( String name : this.getSoluteNames() )
			out.put(name, this.getAverageConcentration(name));
		return out;
	}
	
	/**
	 * \brief Set the concentration of a solute over the entire compartment.
	 * 
	 * <p>All old concentrations of this solute will be lost.</p>
	 * 
	 * @param soluteName Name of the solute to set.
	 * @param newConcn Set all voxels to this value.
	 */
	public void setAllConcentration(String soluteName, double newConcn)
	{
		this.getSoluteGrid(soluteName).setAllTo(ArrayType.CONCN, newConcn);
	}
	
	/* ***********************************************************************
	 * SOLUTE BOUNDARIES
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._shape.getOtherBoundaries();
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public void updateSoluteBoundaries()
	{
		Tier level = Tier.DEBUG;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Updating solute boundaries...");
		for ( Boundary b : this._shape.getAllBoundaries() )
			b.updateMassFlowRates();
		if ( Log.shouldWrite(level) )
			Log.out(level, " All solute boundaries now updated");
	}
	
	/* ***********************************************************************
	 * COMMON GRID METHODS
	 * **********************************************************************/

	/**
	 * @return
	 */
	public SpatialGrid getCommonGrid()
	{
		return this._commonGrid;
	}
	
	/**
	 *\brief TODO
	 */
	public void updateWellMixed()
	{
		/*
		 * Reset the well-mixed array for this environment. If none of the
		 * boundaries need it to be updated, it will be full of zeros (i.e.
		 * nowhere is well-mixed).
		 */
		this._commonGrid.newArray(ArrayType.WELLMIXED);
		/*
		 * Check if any of the boundaries need to update the well-mixed array.
		 * If none do, then there is nothing more to do.
		 */
		Collection<SpatialBoundary> bndrs = this._shape.getSpatialBoundaries();
		boolean shouldUpdate = false;
		for ( SpatialBoundary b: bndrs )
			if ( b.needsPartner() )
			{
				shouldUpdate = true;
				break;
			}
		if ( ! shouldUpdate )
			return;
		/*
		 * At least one of the boundaries need to update the well-mixed array,
		 * so loop through all of them.
		 */
		for ( SpatialBoundary b: bndrs )
			b.updateWellMixedArray();
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public void printSolute(String soluteName)
	{
		Log.out(Tier.QUIET, soluteName+":");
		Log.out(Tier.QUIET, 
				this.getSoluteGrid(soluteName).arrayAsText(ArrayType.CONCN));
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
