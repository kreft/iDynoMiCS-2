package idynomics;

import java.util.HashMap;
import java.util.Set;

import grid.CartesianGrid;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import reaction.Reaction;
import utility.ExtraMath;

public class EnvironmentContainer
{
	protected int[] _defaultNVoxel = Vector.vector(3, 1);
	
	protected int[] _defaultPadding = Vector.zerosInt(3);
	
	protected double _defaultResolution = 1.0;
	
	/**
	 * 
	 */
	protected HashMap<String, CartesianGrid> _solutes = 
										new HashMap<String, CartesianGrid>();
	
	/**
	 * 
	 */
	protected HashMap<String, Reaction> _reactions = 
											new HashMap<String, Reaction>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public EnvironmentContainer()
	{
		
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
	public void init(double[] compartmentSize, double defaultRes)
	{
		this._defaultResolution = defaultRes;
		this._defaultNVoxel = new int[compartmentSize.length];
		double temp;
		for ( int i = 0; i < compartmentSize.length; i++ )
		{
			this._defaultNVoxel[i] = (int) (compartmentSize[i] / defaultRes);
			temp = defaultRes * this._defaultNVoxel[i];
			// TODO message
			if ( ! ExtraMath.areEqual(compartmentSize[i], temp, 1E-9) )
				throw new IllegalArgumentException();
		}
		//TODO padding (depends on boundaries?)
		this._defaultPadding = Vector.zeros(this._defaultNVoxel);
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
	public void init(int[] nVoxel, int[] padding, double res)
	{
		this._defaultResolution = res;
		this._defaultNVoxel = nVoxel;
		this._defaultPadding = padding;
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
	
	public void addSolute(String soluteName, double initialConcn)
	{
		/*
		 * TODO safety: check if solute already in hashmap
		 */
		CartesianGrid sg = new CartesianGrid(this._defaultNVoxel,
										this._defaultPadding,
										this._defaultResolution);
		sg.newArray(SpatialGrid.concn, initialConcn);
		this._solutes.put(soluteName, sg);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Set<String> getSoluteNames()
	{
		return this._solutes.keySet();
	}
	
	public CartesianGrid getSoluteGrid(String soluteName)
	{
		return this._solutes.get(soluteName);
	}
	
	public HashMap<String, CartesianGrid> getSolutes()
	{
		return this._solutes;
	}
}
