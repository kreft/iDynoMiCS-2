package solver;

import java.util.HashMap;
import java.util.function.Function;
import grid.SpatialGrid;

public abstract class PDEsolver
{
	/**
	 * List of solute names that this solver is responsible for.
	 */
	protected String[] _soluteNames;
	
	protected HashMap<String, SpatialGrid> _solute;
	
	/**
	 * TODO
	 */
	protected Function<SpatialGrid, SpatialGrid> _reacFunc;
	
	/**
	 * TODO
	 */
	protected HashMap<String, SpatialGrid> _reaction;
	
	/**
	 * TODO
	 */
	protected SpatialGrid _domain;
	
	/**
	 * TODO
	 */
	protected HashMap<String, SpatialGrid> _diffusivity;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public PDEsolver()
	{
		
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	public void setSolute(HashMap<String, SpatialGrid> solute)
	{
		this._solute = solute;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param f
	 */
	public void setReactionFunction(Function<SpatialGrid, SpatialGrid> f)
	{
		this._reacFunc = f;
	}
	
	
	/**
	 * \brief Tell the solver where to solve (e.g. biofilm, boundary layer), 
	 * and where not to (e.g. bulk). 
	 * 
	 * 
	 * @param domain TODO
	 */
	public void setDomain(SpatialGrid domain)
	{
		this._domain = domain;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param diffusivity
	 */
	public void setDiffusivity(HashMap<String, SpatialGrid> diffusivity)
	{
		this._diffusivity = diffusivity;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 * @param solute
	 */
	protected void updateReactionGrid(HashMap<String, SpatialGrid> solute)
	{
		if ( this._reacFunc == null )
			return;
		for ( String key : solute.keySet() )
			this._reaction.put(key, this._reacFunc.apply(solute.get(key)));
	}
	
	
	/**
	 * \brief TODO
	 *
	 */
	protected void applyLOp()
	{
		for ( String sName : this._soluteNames)
			applyLOp(sName, "lop");
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param solute
	 */
	private void applyLOp(String sName, String tempName)
	{
		SpatialGrid solute = this._solute.get(sName);
		SpatialGrid diffusivity = this._diffusivity.get(sName);
		SpatialGrid reac = this._reaction.get(sName);
		
		int[] current;
		/*
		 * Solute concentration and diffusivity at the current grid 
		 * coordinates.
		 */
		double currConcn, currDiff;
		/*
		 * Solute concentration and diffusivity in the neighboring voxels;
		 */
		double[][] concnNbh, diffNbh;
		/*
		 * Temporary storage for the L-Operator.
		 */
		double lop;
		/*
		 * Iterate over all core voxels calculating the L-Operator. 
		 */
		solute.resetIterator();
		while ( solute.iteratorHasNext() )
		{
			current = solute.iteratorNext();
			if ( this._domain.getValueAt(current) == 0.0 )
				continue;
			currConcn = solute.getValueAt(current);
			currDiff = diffusivity.getValueAt(current);
			concnNbh = solute.getNeighborValues(current);
			diffNbh = diffusivity.getNeighborValues(current);
			lop = 0.0;
			for ( int axis = 0; axis < 3; axis++ )
				for ( int i = 0; i < 2; i++ )
				{
					try {	lop += (diffNbh[axis][i] + currDiff) *
											(concnNbh[axis][i] - currConcn); }
					catch (ArrayIndexOutOfBoundsException e) {}
				}
			lop *= 0.5 / Math.pow(solute.getResolution(), 2.0);
			lop += reac.getValueAt(current);
			solute.addToTempArray(tempName, current, lop);
		}
	}
}
