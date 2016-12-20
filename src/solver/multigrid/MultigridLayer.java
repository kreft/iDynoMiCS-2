package solver.multigrid;

import java.util.LinkedList;
import java.util.List;

import grid.ArrayType;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import settable.Settable;
import shape.Shape;
import shape.resolution.ResolutionCalculator;

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
		//this._coarser.fillArrayFromFiner();
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
	
	public void fillArrayFromFiner(ArrayType type)
	{
		/* Safety */
		if ( this._finer == null )
			return;
		/* Temporary variables. */
		Shape thisShape = this._grid.getShape();
		SpatialGrid finerGrid = this._finer._grid;
		int[] candidate = new int[3];
		List<int[]> finerVoxels = new LinkedList<int[]>();
		double newValue, voxelVol, totalVolume;
		/*
		 * Loop over all coarser voxels, replacing their value with one
		 * calculated from the finer grid.
		 */
		int[] current = thisShape.resetIterator();
		for (; thisShape.isIteratorValid(); current = thisShape.iteratorNext())
		{
			/* 
			 * Find all relevant finer voxels.
			 */
			finerVoxels.clear();
			for ( int dim = 0; dim < 3; dim++ )
				candidate[dim] = (int)(current[dim]*0.5);
			finerVoxels.add(candidate);
			this.appendFinerVoxels(finerVoxels, 0);
			/* 
			 * Loop over all finer voxels found to get the average value.
			 */
			newValue = 0.0;
			totalVolume = 0.0;
			for ( int[] finerVoxel : finerVoxels )
			{
				voxelVol = finerGrid.getShape().getVoxelVolume(finerVoxel);
				newValue += voxelVol * finerGrid.getValueAt(type, finerVoxel);
				totalVolume += voxelVol;
			}
			newValue /= totalVolume;
			/* 
			 * Use a new value that is half the coarser voxel's current, half
			 * the average of the finer voxels.
			 */
			newValue = 0.5 * (newValue + this._grid.getValueAt(type, current));
			this._grid.setValueAt(type, current, newValue);
		}
	}
	
	private List<int[]> appendFinerVoxels(List<int[]> voxels, int dim)
	{
		Shape shape = this._finer.getGrid().getShape();
		ResolutionCalculator resCalc;
		for (int[] voxel : voxels)
		{
			int[] newVoxel = Vector.copy(voxel);
			resCalc = shape.getResolutionCalculator(voxel, dim);
			if ( resCalc.getNVoxel() > voxel[dim] + 1)
			{
				newVoxel[dim]++;
				voxels.add(newVoxel);
			}
		}
		if ( dim < 2 )
			this.appendFinerVoxels(voxels, dim+1);
		return voxels;
	}
}
