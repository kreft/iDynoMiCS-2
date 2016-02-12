package idynomics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import boundary.Boundary;
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
		if ( this._hasInitialised )
		{
			throw new Error("Cannot add new solutes after the environment"+
												" container has initialised!");
		}
		/*
		 * TODO safety: check if solute already in hashmap
		 */
		SpatialGrid sg = this._shape.gridGetter().newGrid(
											this._shape.getDimensionLengths(),
											this._defaultResolution);
		sg.newArray(ArrayType.CONCN, initialConcn);
		this._solutes.put(soluteName, sg);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Set<String> getSoluteNames()
	{
		return this._solutes.keySet();
	}
	
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		return this._solutes.get(soluteName);
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
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printSolute(String soluteName)
	{
		System.out.println(soluteName+":");
		System.out.println(this._solutes.get(soluteName).arrayAsText(ArrayType.CONCN));
	}
	
	public void printAllSolutes()
	{
		this._solutes.forEach((s,g) -> {this.printSolute(s);;});
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
