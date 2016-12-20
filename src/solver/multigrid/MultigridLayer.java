package solver.multigrid;

import grid.SpatialGrid;
import settable.Settable;
import shape.Shape;

/**
 * \brief Single layer of a multi-grid collection of spatial grids.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class MultigridLayer
{
	/**
	 * The spatial grid that this object wraps.
	 */
	private SpatialGrid _grid;
	/**
	 * The wrapper objects for the layers that are immediate neighbours of this
	 * one: coarser has fewer grid voxels, finer has more grid voxels. One (or
	 * in extreme cases, both) of these may be null.
	 */
	private MultigridLayer _coarser, _finer;
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public MultigridLayer(SpatialGrid grid)
	{
		this._grid = grid;
	}
	
	public MultigridLayer constructCoarser()
	{
		Shape shape = this._grid.getShape();
		Shape coarserShape = shape.generateCoarserMultigridLayer();
		String name = this._grid.getName();
		Settable parent = this._grid.getParent();
		SpatialGrid coarserGrid = new SpatialGrid(coarserShape, name, parent);
		this._coarser = new MultigridLayer(coarserGrid);
		this._coarser._finer = this;
		this._coarser.fillArrayFromFiner();
		return this._coarser;
	}
	
	/* ***********************************************************************
	 * SIMPLE GETTERS
	 * **********************************************************************/
	
	public boolean hasCoarser()
	{
		return this._coarser != null;
	}
	
	public MultigridLayer getCoarser()
	{
		return this._coarser;
	}
	
	public boolean hasFiner()
	{
		return this._finer != null;
	}
	
	public MultigridLayer getFiner()
	{
		return this._finer;
	}
	
	public SpatialGrid getGrid()
	{
		return this._grid;
	}
	
	/* ***********************************************************************
	 * ARRAY VALUES
	 * **********************************************************************/
	
	public void fillArrayFromCoarser()
	{
		
	}
	
	public void fillArrayFromFiner()
	{
		
	}
}
