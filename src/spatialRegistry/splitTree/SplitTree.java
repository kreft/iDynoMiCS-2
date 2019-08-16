package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;
import spatialRegistry.Area;
import spatialRegistry.Entry;
import spatialRegistry.SpatialRegistry;
import surface.BoundingBox;

/**
 * First version of nDimensional tree, behaves like quadtree in 2d and octree in
 * 3d, named "splitTree"
 * 
 * TODO clean-up, further optimizations 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class SplitTree<T> implements SpatialRegistry<T>
{	
	public Node<T> node;
	
	public int _dimensions;
	
	static int _minEntries;
	
	static int _maxEntries;
	
	private boolean[] _periodic; 
	
	private double[] _lengths;
	
	public SplitTree(int dims, int min, int max, 
			double[] low, double[] high, boolean[] periodic)
	{
		_dimensions = dims;
		_minEntries = min;
		_maxEntries = max;
		_periodic = periodic;
		_lengths = Vector.minus(high, low);
		this.node = new Node<T>(low, high);
	}

	public void add(Entry<T> entry) 
	{
		this.node.add(entry);
	}
	
	/** Area must have been updated for periodicy */
	public List<Entry<T>> find(Area area) 
	{
		return node.find(area);
	}

	/* *************************************************************************
	 * SpatialRegistry implementation
	 * *************************************************************************
	 * FIXME quick and dirty first version for testing.
	 */

	@Override
	public List<T> search(double[] low, double[] high) 
	{
		LinkedList<T> out = new LinkedList<T>();
		/* also does periodic search */
		boolean[] periodic = Vector.setAll(new boolean[low.length], false);
		for (int i = 0; i < high.length; i++ )
		{
			if ( this._periodic[i] ) 
			{
				if ( high[i] > this.node.getHigh()[i] )
				{
					high[i] -= this._lengths[i];
					periodic[i] = true;
				}
				if ( low[i] < this.node.getLow()[i] )
				{
					low[i] += this._lengths[i];
					periodic[i] = true;
				}
			}
		}
		for ( Entry<T> e : node.find(new Area(low, high, periodic)))
		{
			out.add(e.getEntry());
		}
		return out;
	}

	@Override
	public List<T> search(BoundingBox boundingBox) 
	{
		return this.search(boundingBox.getLow(), 
				boundingBox.getHigh());
	}
	
	@Override
	public List<T> search(List<BoundingBox> boundingBoxes) 
	{
		LinkedList<T> out = new LinkedList<T>();
		for (BoundingBox b : boundingBoxes )
			out.addAll(search(b) );
		return out;
	}
	
	@Override
	public void insert(double[] low, double[] high, T entry) 
	{
		boolean[] periodic = Vector.setAll(new boolean[low.length], false);
		for (int i = 0; i < high.length; i++ )
		{
			if ( this._periodic[i] ) 
			{
				if ( high[i] > this.node.getHigh()[i] )
				{
					high[i] -= this._lengths[i];
					periodic[i] = true;
				}
				if ( low[i] < this.node.getLow()[i] )
				{
					low[i] += this._lengths[i];
					periodic[i] = true;
				}
			}
		}
		this.add(new Entry<T>(low, high, periodic, entry));
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) 
	{
		this.insert(boundingBox.getLow(), 
				boundingBox.getHigh(), entry);
	}
	
	@Override
	public boolean delete(T entry)
	{
		return this.node.delete(entry);
	}
}
