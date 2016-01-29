package idynomics;

import java.util.HashMap;
import java.util.Set;

import grid.GridBoundary.GridMethod;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SpatialGrid.GridGetter;
import shape.BoundarySide;
import shape.Shape;
import linearAlgebra.Vector;
import reaction.Reaction;
import utility.ExtraMath;

public class EnvironmentContainer
{
	protected Shape _shape;
	
	protected GridGetter _gridGetter;
	
	protected double[] _defaultTotalLength = Vector.vector(3, 1.0);
	
	protected double _defaultResolution = 1.0;
	
	/**
	 * 
	 */
	protected HashMap<String, SpatialGrid> _solutes = 
										new HashMap<String, SpatialGrid>();
	
	/**
	 * 
	 */
	protected HashMap<String, Reaction> _reactions = 
											new HashMap<String, Reaction>();
	
	protected HashMap<BoundarySide,HashMap<String,GridMethod>> _boundaries =
					   new HashMap<BoundarySide,HashMap<String,GridMethod>>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public EnvironmentContainer()
	{
		
	}
	
	public void setShape(Shape aShape)
	{
		this._shape = aShape;
		this._gridGetter = this._shape.gridGetter();
	}
	
	public EnvironmentContainer(Shape aShape)
	{
		this._shape = aShape;
		this._gridGetter = this._shape.gridGetter();
	}
	
	public void init()
	{
		for ( BoundarySide aBS : this._shape.getBoundarySides() )
			for ( String soluteName : this._solutes.keySet() )
			{
				this._solutes.get(soluteName).addBoundary(aBS, 
						this._shape.getGridMethod(aBS, soluteName));
			}
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO Rob [8Oct2015]: this probably needs more work, just wanted
	 * something to get me rolling elsewhere. 
	 * 
	 * @param compartmentSize
	 * @param defaultRes
	 */
	public void setSize(double[] compartmentSize, double defaultRes)
	{
		this._defaultResolution = defaultRes;
		this._defaultTotalLength = compartmentSize;
		
//		this._defaultResolution = defaultRes;
//		this._defaultNVoxel = new int[compartmentSize.length];
//		double temp;
//		
//		for ( int i = 0; i < compartmentSize.length; i++ )
//		{
//			this._defaultNVoxel[i] = (int) (compartmentSize[i] / defaultRes);
//			temp = defaultRes * this._defaultNVoxel[i];
//			// TODO message
//			if ( ! ExtraMath.areEqual(compartmentSize[i], temp, 1E-9) )
//				throw new IllegalArgumentException();
//		}
		//System.out.println("\tEnv size: "+Arrays.toString(this._defaultNVoxel)); //bughunt
	}
	
	/**
	 *\brief TODO 
	 * 
	 * TODO Rob [8Oct2015]: This is very temporary, just need to get testing
	 * to work so I can test other bits and bobs. 
	 * 
	 * @param nVoxel
	 * @param padding
	 * @param res
	 */
//	public void setSize(int[] nVoxel, double res)
//	{
//		this._defaultResolution = res;
//		this._defaultNVoxel = nVoxel;
//	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this.addSolute(soluteName, 0.0);
	}
	
	public void addSolute(String soluteName, double initialConcn)
	{
		/*
		 * TODO safety: check if solute already in hashmap
		 */
		//System.out.println("Adding "+soluteName+" with "+Arrays.toString(this._defaultNVoxel)); //Bughunt
		SpatialGrid sg = this._gridGetter.newGrid(this._defaultTotalLength,
													this._defaultResolution);
		sg.newArray(ArrayType.CONCN, initialConcn);
		this._boundaries.forEach( (side, map) ->
							{ sg.addBoundary(side, map.get(soluteName)); });
		this._solutes.put(soluteName, sg);
	}
	
	public void addBoundary(BoundarySide aSide, String soluteName,
														GridMethod aMethod)
	{
		if ( ! this._boundaries.containsKey(aSide) )
			this._boundaries.put(aSide, new HashMap<String,GridMethod>());
		this._boundaries.get(aSide).put(soluteName, aMethod);
		this._solutes.get(soluteName).addBoundary(aSide, aMethod);
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
	 * FIXME: this is really a property of the compartment but we otherwise
	 * cannot access this information from the process manager, consider refact.
	 * @return
	 */
	public double[] getEdgeLengths()
	{
		return _shape.getDimensionLengths();
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
}
