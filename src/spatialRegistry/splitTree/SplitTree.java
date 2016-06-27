package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;

import linearAlgebra.Vector;
import spatialRegistry.splitTree.SplitTree.Area;

public class SplitTree
{	
	protected boolean _root = false;
	
	public Node node;
	
	static int _dimensions;
	
	static int _minEntries;
	
	static int _maxEntries;
	
	static boolean[] _periodic; //TODO
	
	public SplitTree(int dimensions, int min, int max, 
			double[] low, double[] high, boolean[] periodic, int cores)
	{
		this._root = true;
		_dimensions = dimensions;
		_minEntries = min;
		_maxEntries = max;
		this.node = new Node(low, high, 0, true, this);
	}
	
	public SplitTree(int dimensions, int min, int max, boolean[] periodic, 
			int cores)
	{
		this(dimensions, min, max, 
				Vector.setAll(new double[dimensions], -Math.sqrt(Double.MAX_VALUE)), 
				Vector.setAll(new double[dimensions], Math.sqrt(Double.MAX_VALUE)), 
				periodic, cores);
	}

	public void add(double[] low, double[] high, Object obj)
	{
		this.add(new Entry(low, high, obj));
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
	

	public List<Entry> find(Area area) 
	{
		return node.find(area);
	}
	
	public void split(Node leaf)
	{
		leaf.setDim( ( leaf.dim()+1 >= _dimensions ? 0 : leaf.dim()+1 ) );
		
		Node upper = new Node(leaf.upperLow(), leaf.high, leaf.dim(), true, this);
		Node lower = new Node(leaf.low, leaf.lowerHigh(), leaf.dim(), true, this);

		upper.add(leaf.allLocal());
		lower.add(leaf.allLocal());

		/* promote node from leaf to branch */
		leaf.promote(upper, lower);
	}	

	public List<Entry> allEntries(LinkedList<Entry> out) 
	{
		return this.node.allEntries(out);
	}
	
	public List<Object> allObjects()
	{
		LinkedList<Entry> entries = new LinkedList<Entry>();
		LinkedList<Object> out = new LinkedList<Object>();
			for (Entry e : allEntries(entries))
				out.add(e.entry);
		return out;
	}
	
	public interface Area {
		
		public double[] low();
		
		public double[] high();
		
		public abstract void add(List<Area> entries);

	}
	
	public static class Node implements Area
	{
		
		final double[] low;
		
		final double[] high;
		
		protected boolean _leafNode;
		
		protected int _dim;

		private LinkedList<Area> _entries = new LinkedList<Area>();
		
		private Outside _outTest = this.new Outside();
		
		private SplitTree _tree;

		public Node(double[] low, double[] high, int dim, 
				boolean isLeaf, SplitTree tree)
		{
			this.low = low;
			this.high = high;
			this._dim = dim;
			this._leafNode = isLeaf;
			this._tree = tree;
		}
		
		public List<Entry> find(Area area) 
		{
			LinkedList<Entry> out = new LinkedList<Entry>();
			if ( ! _outTest.test(area) )
			{
				if ( this._leafNode )
					return this.allConc(out, area);
				else
				{
					for ( Area a : _entries )
						out.addAll(((Node) a).find(area));
				}
			}
			return out;
		}

		public void add(Area entry)
		{
			
			if ( ! _outTest.test(entry) )
			{
				if ( this._leafNode || entry instanceof Node)
				{
				this.getEntries().add(entry);
				if( this.size() > SplitTree._maxEntries )
					_tree.split(this);
				}
				else
				{
					for ( Area a : this.getEntries() )
						((Node) a).add(entry);
				}
			}
			
		}
		
		public void add(List<Area> entries) 
		{
			entries.removeIf(_outTest);
			if ( this._leafNode)
			{
				getEntries().addAll(entries);
				if( this.size() > SplitTree._maxEntries )
					_tree.split(this);
			}
			else
			{
				for ( Area a : this.getEntries() )
					((Node) a).add(entries);
			}
		}
		
		protected LinkedList<Area> getEntries() {
			return _entries;
		}

		protected void setEntries(LinkedList<Area> _entries) {
			this._entries = _entries;
		}

		public void remove(Entry entry)
		{
			this.getEntries().remove(entry);
		}
		
		public int size()
		{
			return getEntries().size();
		}
		
		public int dim()
		{
			return _dim;
		}
		
		public List<Area> allLocal()
		{
			return new LinkedList<Area>(this.getEntries());
		}
		
		public List<Area> allUnfiltered(LinkedList<Area> out)
		{
			if ( this._leafNode)
			{
				for (Area a : this.getEntries())
					out.add( (Entry) a);
			}
			else
			{
				for (Area a : this.getEntries())
					((Node) a).allUnfiltered(out);
			}
			return out;
		}
		
		public List<Entry> allConc(LinkedList<Entry> out, Area area)
		{
			for (Area a : this.getEntries())
			{
				if ( ! (area.low()[_dim] > a.high()[_dim] || 
						area.high()[_dim] < a.low()[_dim] ) );
					out.add( (Entry) a);
			}
			return out;
		}
		
		public List<Entry> allEntries(LinkedList<Entry> out) 
		{
			if ( this._leafNode)
			{
				for (Area a : this.getEntries())
					if ( ! out.contains(a))
						out.add( (Entry) a);
			}
			else
			{
				for (Area a : this.getEntries())
					((Node) a).allEntries(out);
			}
			return out;
		}
		
		public void purge()
		{
			getEntries().removeIf(_outTest);
		}
		
		public void promote(Node a, Node b)
		{
			this.getEntries().clear();
			this._leafNode = false;
			this.add(a);
			this.add(b);
		}
		
		public void setDim(int dim)
		{
			this._dim = dim;
		}
		
		
		/* ************************************************************************
		 * Helper methods
		 */
		
		public double[] upperLow()
		{
			double[] out = Vector.copy(this.low);
			out[_dim] = split();
			return out;
		}
		
		public double[] lowerHigh()
		{
			double[] out = Vector.copy(this.high);
			out[_dim] = split();
			return out;
		}
		
		private double split()
		{
			return this.low[_dim] + ( (this.high[_dim] - this.low[_dim]) / 2.0 );
		}
		
		private double[] splits()
		{
			double[] split = new double[this.low.length];
			for (int i = 0; i < this.low.length; i++)
				split[i] = this.low[i] + ( (this.high[i] - this.low[i]) / 2.0 );
			return split;
		}
		
		public class Outside implements Predicate<Area> 
		{

			@Override
			public boolean test(Area area) 
			{
				return ( area.low()[_dim] > high[_dim] || 
						area.high()[_dim] < low[_dim] );
			}
		}

		@Override
		public double[] low() 
		{
			return low;
		}

		@Override
		public double[] high() 
		{
			return high;
		}
	}
	
	public class Entry implements Area
	{
		final double[] low;
		
		final double[] high;
		
		Object entry;
		
		public Entry(double[] low, double[] high, Object entry)
		{
			this.low = low;
			this.high = high;
			this.entry = entry;
		}
		
		@Override
		public double[] low() 
		{
			return low;
		}

		@Override
		public double[] high() 
		{
			return high;
		}

		@Override
		public void add(List<Area> entries) {
			// Do nothing
		}

	}

}
