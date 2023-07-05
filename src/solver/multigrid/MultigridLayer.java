package solver.multigrid;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import grid.ArrayType;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import linearAlgebra.Vector;
import settable.Settable;
import shape.Shape;
import shape.resolution.ResolutionCalculator;

/**
 * \brief Single layer of a multi-grid collection of spatial grids.
 * 
 * <p>For reference, see <i>Numerical Recipes in C</i> (Press, Teukolsky,
 * Vetterling & Flannery, 1997), Chapter 19.6: Multigrid Methods for Boundary
 * Value Problems. Equation numbers used in this chapter will be referenced
 * throughout the class source code.</p>
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
	
	public boolean canConstructCoarser()
	{
		return this._grid.getShape().canGenerateCoarserMultigridLayer();
	}
	
	public MultigridLayer constructCoarser()
	{
		Shape shape = this._grid.getShape();
		Shape coarserShape = shape.generateCoarserMultigridLayer();
		return this.constructCoarser(coarserShape);
	}
	
	public MultigridLayer constructCoarser(Shape coarserShape)
	{
		String name = this._grid.getName();
		Settable parent = this._grid.getParent();
		SpatialGrid coarserGrid = new SpatialGrid(coarserShape, name, parent);
		this._coarser = new MultigridLayer(coarserGrid);
		this._coarser._finer = this;
		for ( ArrayType type : this._grid.getAllArrayTypes() )
		{
			this._coarser._grid.newArray(type);
			this._coarser.fillArrayFromFiner(type, 0.0, null);
		}
		return this._coarser;
	}
	
	public static MultigridLayer generateCompleteMultigrid(SpatialGrid grid)
	{
		MultigridLayer newMultigrid = new MultigridLayer(grid);
		MultigridLayer currentLayer = newMultigrid;
		while (currentLayer.canConstructCoarser() )
			currentLayer = currentLayer.constructCoarser();
		return newMultigrid;
	}
	
	public static MultigridLayer generateCompleteMultigrid(
			SpatialGrid grid, Collection<Shape> shapes)
	{
		MultigridLayer newMultigrid = new MultigridLayer(grid);
		MultigridLayer currentLayer = newMultigrid;
		Iterator<Shape> iterator = shapes.iterator();
		iterator.next();
		while (iterator.hasNext())
			currentLayer = currentLayer.constructCoarser(iterator.next());
		return newMultigrid;
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
	
	/**
	 * \brief For every layer coarser than the one given, replaces the array
	 * values with those from the layer given, for every ArrayType present in
	 * the layer given.
	 * 
	 * @param layer A MultigridLayer (assumed to be the finest).
	 */
	public static void replaceAllLayersFromFinest(MultigridLayer layer)
	{
		Collection<ArrayType> types = layer.getGrid().getAllArrayTypes();
		while ( layer.hasCoarser() )
		{
			layer = layer.getCoarser();
			for ( ArrayType type : types )
				layer.fillArrayFromFiner(type, 0.0, null);
		}
	}/**
	 * \brief Use array values from the layer that is coarser than this one to
	 * update the array values in this layer.
	 * 
	 * <p>This approach is also known as <i>interpolation</i> or
	 * <i>prolongation</i>. This method corresponds to Equation (19.6.10) in
	 * <i>Numerical Recipes in C</i>.</p>
	 * 
	 * @param type The type of array to update.
	 * @param commonGrid The spatial grid which contains the well-mixed array.
	 */
	public void fillArrayFromCoarser(ArrayType type, SpatialGrid commonGrid)
	{
		this.fillArrayFromCoarser(type, type, commonGrid);
	}
	
	/**
	 * \brief Use array values from the layer that is coarser than this one to
	 * update the array values in this layer.
	 * 
	 * <p>This approach is also known as <i>interpolation</i> or
	 * <i>prolongation</p>. This method corresponds to Equation (19.6.10) in
	 * <i>Numerical Recipes in C</i>.</p>
	 * 
	 * @param finerType The type of array to update in this layer (overwritten).
	 * @param coarserType The type of array to get values from (unaffected).
	 * @param commonGrid The spatial grid which contains the well-mixed array.
	 */
	public void fillArrayFromCoarser(
			ArrayType finerType, ArrayType coarserType, SpatialGrid commonGrid)
	{
		/* Safety */
		if ( this._coarser == null )
			return;
		/* Start timer */
		long tick, tock;
		tick = System.currentTimeMillis();
		/* Temporary variables. */
		Shape thisShape = this._grid.getShape();
		SpatialGrid coarserGrid = this._coarser._grid;
		double newValue;
		/*
		 * Mimic red-black iteration: on the first sweep (red) take values
		 * straight from the coarser grid; on the second sweep (black)
		 * interpolate between neighbouring voxels (i.e. red voxels that
		 * already took values from the coarser grid).
		 */
		int[] current = thisShape.resetIterator();
		int[] coarserVoxel = Vector.zeros(current);
		for (; thisShape.isIteratorValid(); current = thisShape.iteratorNext())
		{
			if ( WellMixedConstants.isWellMixed(commonGrid, current) )
				continue;
			/*
			 * (i & 1) == 1 is a slightly quicker way of determining evenness
			 * that (i % 2) == 0. (The modulo operation also deals with the
			 * positive/negative, which is irrelevant here).
			 */
			if ((Vector.sum(current) & 1) == 1)
				continue;
			Vector.copyTo(coarserVoxel, current);
			for (int i = 0; i < coarserVoxel.length; i++)
				coarserVoxel[i] = (int)(coarserVoxel[i]*0.5);
			newValue = coarserGrid.getValueAt(coarserType, coarserVoxel);
			this._grid.setValueAtCurrent(finerType, newValue);
		}
		current = thisShape.resetIterator();
		int[] nhb;
		double volume, totalVolume;
		for (; thisShape.isIteratorValid(); current = thisShape.iteratorNext())
		{
			if ( WellMixedConstants.isWellMixed(commonGrid, current) )
				continue;
			/*
			 * (i & 1) == 0 is a slightly quicker way of determining evenness
			 * that (i % 2) == 0. (The modulo operation also deals with the
			 * positive/negative, which is irrelevant here).
			 */
			if ((Vector.sum(current) & 1) == 0)
				continue;
			newValue = 0.0;
			totalVolume = 0.0;
			for ( nhb = thisShape.resetNbhIterator(); 
					thisShape.isNbhIteratorValid();
					nhb = thisShape.nbhIteratorNext())
			{
				/* We do not want to interact with boundaries here. */
				if ( ! thisShape.isNbhIteratorInside() )
					continue;
				volume = thisShape.getVoxelVolume(nhb);
				newValue += this._grid.getValueAtNhb(finerType) * volume;
				totalVolume += volume;
			}
			this._grid.setValueAtCurrent(finerType, newValue/totalVolume);
		}
	}
	
	/**
	 * \brief Use array values from the layer that is finer than this one to
	 * update the array values in this layer.
	 * 
	 * <p>This approach is also known as <i>restriction</i> or
	 * <i>injection</i>. This method corresponds to Equation (19.6.9) in
	 * <i>Numerical Recipes in C</i>.</p>
	 * 
	 * @param type The type of array to update.
	 * @param fracOfOldValueKept Weighting to give the old values when
	 * updating: 0 to completely replace them, 1 to leave them unchanged, any
	 * value between 0 and 1 as a compromise.
	 */
	public void fillArrayFromFiner(ArrayType type,
			double fracOfOldValueKept, SpatialGrid commonGrid)
	{
		this.fillArrayFromFiner(type, type, fracOfOldValueKept, commonGrid);
	}
	
	/**
	 * \brief Use array values from the layer that is finer than this one to
	 * update the array values in this layer.
	 * 
	 * <p>This approach is also known as <i>restriction</i> or
	 * <i>injection</i>. This method corresponds to Equation (19.6.9) in
	 * <i>Numerical Recipes in C</i>.</p>
	 * 
	 * @param coarserType The type of array to update in this layer (overwritten).
	 * @param finerType The type of array to get values from (unaffected).
	 * @param fracOfOldValueKept Weighting to give the old values when
	 * updating: 0 to completely replace them, 1 to leave them unchanged, any
	 * value between 0 and 1 as a compromise.
	 */
	public void fillArrayFromFiner(ArrayType coarserType, ArrayType finerType,
			double fracOfOldValueKept, SpatialGrid commonGrid)
	{
		double fracOfNewValueUsed = 1.0 - fracOfOldValueKept;
		/* Safety */
		if ( this._finer == null )
			return;
		if ( ! this._grid.hasArray(coarserType) )
			this._grid.newArray(coarserType);
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
			if ( WellMixedConstants.isWellMixed(commonGrid, current) )
				continue;
			/* 
			 * Find all relevant finer voxels.
			 */
			finerVoxels.clear();
			for ( int dim = 0; dim < 3; dim++ )
				candidate[dim] = current[dim]*2;
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
				newValue += voxelVol * 
						finerGrid.getValueAt(finerType, finerVoxel);
				totalVolume += voxelVol;
			}
			newValue /= totalVolume;
			/* 
			 * Use a new value that is half the coarser voxel's current, half
			 * the average of the finer voxels.
			 */
			newValue = (fracOfNewValueUsed * newValue) +
					(fracOfOldValueKept * this._grid.getValueAtCurrent(coarserType));
			if ( grid.ArrayType.CONCN.equals(coarserType) && newValue < 0.0 )
				newValue = 0.0;
			this._grid.setValueAt(coarserType, current, newValue);
		}
	}
	
	private List<int[]> appendFinerVoxels(List<int[]> voxels, int dim)
	{
		Shape shape = this._finer.getGrid().getShape();
		ResolutionCalculator resCalc;
		List<int[]> newVoxels = new LinkedList<int[]>();
		for (int[] voxel : voxels)
		{
			int[] newVoxel = Vector.copy(voxel);
			resCalc = shape.getResolutionCalculator(voxel, dim);
			if ( resCalc.getNVoxel() > voxel[dim] + 1)
			{
				newVoxel[dim]++;
				newVoxels.add(newVoxel);
			}
		}
		voxels.addAll(newVoxels);
		if ( dim < 2 )
			this.appendFinerVoxels(voxels, dim+1);
		return voxels;
	}
}
