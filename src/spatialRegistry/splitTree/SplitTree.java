package spatialRegistry.splitTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;
import spatialRegistry.Area;
import spatialRegistry.Entry;
import spatialRegistry.SpatialRegistry;
import surface.BoundingBox;

/**
 * \brief: nDimensional {@link SpatialRegistry}, behaves like quadtree in 2d and
 * octree in 3d, named {@link SplitTree}
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class SplitTree<T> implements SpatialRegistry<T>
{	

	protected final int maxEntries;
	
	protected final int childnodes;	
	
	protected final int longest;
	
	protected final List<boolean[]> combinations;
	
	private Node<T> node;
	
	private boolean[] _periodic; 
	
	private double[] _lengths;

	
	public SplitTree(int max, 
			double[] low, double[] high, boolean[] periodic)
	{
		this._periodic = periodic;
		this._lengths = Vector.minus(high, low);
		
		this.maxEntries = max;
		this.longest = longest(low, high);
		this.childnodes = 1 << low.length;
		this.combinations = this.combinations(low.length);
		this.node = new Node<T>(low, high, this);
	}

	/**
	 * Adds an Tree-entry to the tree (Area parameters must have already been
	 * set).
	 * @param entry
	 */
	public void add(Entry<T> entry) 
	{
		this.node.add(entry);
	}
	
	/**
	 * returns longest dimension given a specific area defined by provided lower
	 * and higher corner coordinates
	 * @param low
	 * @param high
	 * @return
	 */
	public int longest(double[] low, double[] high)
	{
		int out = 0;
		double longest = high[0] - low[0];
		for ( int i = 1; i < low.length; i++ )
			if( high[i] - low[i] > longest)
			{
				out = i;
				longest = high[i] - low[i];
			}
		return out;
	}
	
	/**
	 * \brief: returns profile for all applicable child nodes.
	 * @return
	 */
	protected List<boolean[]> combinations()
	{
		return this.combinations;
	}
	
	/**
	 * \brief: returns profile for all applicable child nodes given a number of
	 * dimensions.
	 * @return
	 */
	private List<boolean[]> combinations(int length)
	{
		boolean[] a = new boolean[length];
		List<boolean[]> b = new ArrayList<boolean[]>(this.childnodes);
		b.add(a);
		for ( int i = 0; i < length; i++)
			combinations(i, b);
		return b;
	}
	
	/**
	 * \brief: intermediate for combinations(int) add all unique combinations
	 * for dimension pos
	 * @param pos
	 * @param build
	 */
	private void combinations(int pos, List<boolean[]> build)
	{
		int length = build.size();
		for ( int i = 0; i < length; i++ )
		{
			boolean[] c = new boolean[this._lengths.length];
			for ( int j = 0; j < pos; j++ )
				c[j] = build.get(i)[j];
			c[pos] = true;
			build.add(c);
		}
	}

	/* *************************************************************************
	 * SpatialRegistry implementation
	 * *************************************************************************
	 * FIXME quick and dirty first version for testing.
	 */

	/**
	 * \brief search a specific area within provided area defined by its lower 
	 * and higher corner in the tree, returns all entries with overlapping 
	 * bounding boxes
	 */
	@Override
	public List<T> search(double[] low, double[] high) 
	{
		/* also does periodic search */
		boolean[] periodic = new boolean[low.length];
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
		LinkedList<T> out = new LinkedList<T>();
		return node.find(out,new Area(low, high, periodic));
	}

	/**
	 * \brief search a specific area in the tree, returns all entries with 
	 * overlapping bounding boxes
	 */
	@Override
	public List<T> search(Area area) 
	{
		/* also does periodic search */
		boolean[] periodic = new boolean[_periodic.length];
		for (int i = 0; i < _periodic.length; i++ )
		{
			if ( this._periodic[i] ) 
			{
				/*
				 * If the bounding box is bigger than the compartment in periodic
				 * dimension i, simply set the bounds to equal the extremes of 
				 * the dimension. This avoids the bounding box wrapping around
				 * at both ends, causing the middle of the bounding box to be
				 * effectively cancelled out.
				 */
				if ((area.getHigh()[i] - area.getLow()[i]) > this._lengths[i])
				{
					double[] low = area.getLow();
					double[] high = area.getHigh();
					low[i] = this.node.getLow()[i];
					high[i] = this.node.getHigh()[i];
					area.set(low, high);
				}
				if ( area.getHigh()[i] > this.node.getHigh()[i] )
				{
					area.getHigh()[i] -= this._lengths[i];
					periodic[i] = true;
				}
				if ( area.getLow()[i] < this.node.getLow()[i] )
				{
					area.getLow()[i] += this._lengths[i];
					periodic[i] = true;
				}
			}
		}
		area.setperiodic(periodic);

		LinkedList<T> out = new LinkedList<T>();
		return node.find(out,area);
	}
	
	/**
	 * \brief search a specific set of areas in the tree, returns all entries
	 * with overlapping bounding boxes
	 */
	@Override
	public List<T> search(List<BoundingBox> boundingBoxes) 
	{
		LinkedList<T> out = new LinkedList<T>();
		for (BoundingBox b : boundingBoxes )
			out.addAll(search(b) );
		return out;
	}
	
	/**
	 * \brief insert entry with corresponding lower and higher corner 
	 * coordinates into the tree.
	 */
	@Override
	public void insert(double[] low, double[] high, T entry) 
	{
		boolean[] periodic = new boolean[low.length];
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

	/**
	 * \brief insert entry with corresponding bounding box into the tree.
	 */
	@Override
	public void insert(BoundingBox boundingBox, T entry) 
	{
		this.insert(boundingBox.getLow(), 
				boundingBox.getHigh(), entry);
	}
	
	/**
	 * Delete any occurrence of object member (Warning, the entire tree has
	 * to be searched for member, this is a slow process, do not use this if 
	 * the tree will be rebuild before it is searched anyway).
	 * @param member
	 * @return
	 */
	@Override
	public boolean delete(T entry)
	{
		return this.node.delete(entry);
	}
	
	/**
	 * brief: removes all entries from the tree
	 */
	public void clear()
	{
		/* Some testing showed that it is faster to let the garbage collector
		 * handle this and rebuilding than it wiping the tree. */
		this.node = new Node<T>(node.getLow(), node.getHigh(), this);
	}
}
