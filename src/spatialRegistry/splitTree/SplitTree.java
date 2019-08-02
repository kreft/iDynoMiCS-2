package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;

import dataIO.Log.Tier;
import dataIO.Log;
import linearAlgebra.Vector;
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
@SuppressWarnings( {"rawtypes", "unchecked"} )
public class SplitTree<T> implements SpatialRegistry<T>
{	
	protected boolean _root = false;
	
	public Node<T> node;
	
	public int _dimensions;
	
	static int _minEntries;
	
	static int _maxEntries;
	
	private boolean[] _periodic; 
	
	private double[] _lengths;
	
	public SplitTree(int dimensions, int min, int max, 
			double[] low, double[] high, boolean[] periodic)
	{
		this._root = true;
		_dimensions = dimensions;
		_minEntries = min;
		_maxEntries = max;
		_periodic = periodic;
		_lengths = Vector.minus(high, low);
		if ( Vector.allOfValue(periodic, false) )
			this.node = new Node<T>(this, low, high, true, this, null);
		else
			this.node = new Node<T>(this, low, high, true, this, periodic);
	}
	
	public SplitTree(int dimensions, int min, int max, boolean[] periodic)
	{
		this(dimensions, min, max, 
				Vector.setAll(new double[dimensions], -Math.sqrt(Double.MAX_VALUE)), 
				Vector.setAll(new double[dimensions], Math.sqrt(Double.MAX_VALUE)), 
				periodic);
	}

	public void add(double[] low, double[] high, T obj)
	{
		this.add(new Entry<T>(low, high, obj));
	}

	public void add(Area entry) 
	{
		this.node.add(entry);
	}
	
	public void add(List<Area> entries)
	{
		if (node._leafNode)
			node.add(entries);
		else
			for (Area n : this.node.getEntries())
				n.add(entries);
	}
	
	/** Area must have been updated for periodicy */
	public List<Entry> find(Area area) 
	{
		return node.find(area);
	}
//	public List<Entry> find(Area area) 
//	{
//		if ( _periodic == null )
//			return node.find(new OutsideNormal(area));
//		else
//		{
//			return node.find(new OutsidePeriodic(area,_periodic));
//		}
//
//	}
	
	public void split(Node<T> leaf)
	{
		Node<T> newNode;
		List<Node<T>> childNodes = new LinkedList<Node<T>>();
		
		for ( boolean[] b : leaf.combinations())
		{
			newNode = new Node<T>( this, leaf.corner(leaf.low, leaf.splits(), b), 
					leaf.corner(leaf.splits(), leaf.high, b), true, this, this._periodic);
			newNode.add(leaf.allLocal());
			childNodes.add(newNode);
		}

		/* promote node from leaf to branch */
		leaf.promote(childNodes);
	}	
	
	public List<Entry> allEntries(LinkedList<Entry> out) 
	{
		return this.node.allEntries(out);
	}
	
	public List<T> allObjects()
	{
		LinkedList<Entry> entries = new LinkedList<Entry>();
		LinkedList<T> out = new LinkedList<T>();
			for (Entry<T> e : allEntries(entries))
				out.add(e.entry);
		return out;
	}
	
	
	
	/* *************************************************************************
	 * SpatialRegistry implementation
	 * *************************************************************************
	 * FIXME quick and dirty first version for testing.
	 */

	@Override
	public List<T> localSearch(double[] coords, double[] dimensions) 
	{
		Log.out(Tier.CRITICAL, "warning local search not implemented in "
				+ "splittree");
		return search(coords, dimensions);
	}

	@Override
	public List<T> search(double[] coords, double[] dimensions) 
	{
		LinkedList<T> out = new LinkedList<T>();
		/* also does periodic search */
		double[] high = Vector.add(coords, dimensions);
		double[] low = coords;
		for (int i = 0; i < high.length; i++ )
		{
			if ( this._periodic[i] && high[i] > this.node.high[i] )
				high[i] -= this._lengths[i];
			if ( this._periodic[i] && low[i] < this.node.low[i] )
				low[i] += this._lengths[i];
		}
		for ( Entry<T> e : node.find(new Entry<T>(low, high, null)))
		{
			out.add(e.entry);
		}
		return out;
	}

	@Override
	public List<T> search(BoundingBox boundingBox) 
	{
		return this.search(boundingBox.lowerCorner(), boundingBox.ribLengths());
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
	public List<T> all() 
	{
		return this.allObjects();
	}

	@Override
	public void insert(double[] coords, double[] dimensions, T entry) 
	{
		double[] high = Vector.add(coords, dimensions);
		double[] low = coords;
		for (int i = 0; i < high.length; i++ )
		{
			if ( this._periodic[i] && high[i] > this.node.high[i] )
				high[i] -= this._lengths[i];
			if ( this._periodic[i] && low[i] < this.node.low[i] )
				low[i] += this._lengths[i];
		}
		this.add(new Entry<T>(low, high, entry));
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) 
	{
		this.insert(boundingBox.lowerCorner(), boundingBox.ribLengths(), entry);
	}

	@Override
	public T getRandom() {
		System.out.println("unsuported method Split tree getRandom");
		return null;
	}

	@Override
	public boolean delete(T entry) 
	{
		System.out.println("unsuported method Split tree DELETE");
		return false;
	}

}
