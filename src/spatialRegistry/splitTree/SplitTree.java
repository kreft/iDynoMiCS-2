package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import linearAlgebra.Vector;

public class SplitTree
{	
	protected boolean _root = false;
	
	public Node node;
	
	static int _dimensions;
	
	static int _minEntries;
	
	static int _maxEntries;
	
	static boolean[] _periodic; //TODO
	
	public SplitTree(int dimensions, int min, int max, 
			double[] low, double[] high, boolean[] periodic)
	{
		this._root = true;
		_dimensions = dimensions;
		_minEntries = min;
		_maxEntries = max;
		this.node = new Node(low, high, true, this);
	}
	
	public SplitTree(int dimensions, int min, int max, boolean[] periodic)
	{
		this(dimensions, min, max, 
				Vector.setAll(new double[dimensions], -Math.sqrt(Double.MAX_VALUE)), 
				Vector.setAll(new double[dimensions], Math.sqrt(Double.MAX_VALUE)), 
				periodic);
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
		Node newNode;
		List<Node> childNodes = new LinkedList<Node>();
		
		for ( boolean[] b : leaf.combinations())
		{
			newNode = new Node( leaf.corner(leaf.low, leaf.splits(), b), 
					leaf.corner(leaf.splits(), leaf.high, b), true, this);
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

		private LinkedList<Area> _entries = new LinkedList<Area>();
		
		private Outside _outTest = this.new Outside();
		
		private SplitTree _tree;

		public Node(double[] low, double[] high, 
				boolean isLeaf, SplitTree tree)
		{
			this.low = low;
			this.high = high;
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
		
		public List<Entry> findUnfiltered(Area area) 
		{
			LinkedList<Entry> out = new LinkedList<Entry>();
			if ( ! _outTest.test(area) )
			{
				if ( this._leafNode )
					return this.allUnfiltered(out);
				else
				{
					for ( Area a : _entries )
						out.addAll(((Node) a).findUnfiltered(area));
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

		public List<Area> allLocal()
		{
			return new LinkedList<Area>(this.getEntries());
		}
		
		public List<Entry> allUnfiltered(LinkedList<Entry> out)
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
				if ( ! this._outTest.test(area) );
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
		
		public List<Node> allLeaves(LinkedList<Node> out) 
		{
			if ( this._leafNode)
			{
				out.add( this );
			}
			else
			{
				for (Area a : this.getEntries())
					((Node) a).allLeaves(out);
			}
			return out;
		}
		
		public void purge()
		{
			getEntries().removeIf(_outTest);
		}
		
		public void promote(List<Node> nodes)
		{
			this.getEntries().clear();
			this._leafNode = false;
			for (Node n : nodes)
				this.add(n);
		}
		
		/* ************************************************************************
		 * Helper methods
		 */
			
		private double split(int dim)
		{
			return this.low[dim] + ( (this.high[dim] - this.low[dim]) / 2.0 );
		}
		
		private double[] splits()
		{
			double[] split = new double[this.low.length];
			for (int i = 0; i < this.low.length; i++)
				split[i] = this.low[i] + ( (this.high[i] - this.low[i]) / 2.0 );
			return split;
		}
		
		private double[] corner(double[] lower, double[] higher, boolean[] combination)
		{
			double [] out = new double[combination.length];
			for (int i = 0; i < combination.length; i++)
				out[i] = (combination[i] ? higher[i] : lower[i]);
			return out;
		}
		
		private List<boolean[]> combinations()
		{
			return this.combinations(low.length);
		}
		
		private List<boolean[]> combinations(int length)
		{
			boolean[] a = new boolean[length];
			Vector.setAll(a, false);
			List<boolean[]> b = new LinkedList<boolean[]>();
			b.add(a);
			for ( int i = 0; i < length; i++)
				combinations(i, b);
			return b;
		}
		
		private void combinations(int pos, List<boolean[]> build)
		{
			int length = build.size();
			for ( int i = 0; i < length; i++ )
			{
				boolean[] c = Vector.copy(build.get(i));
				c[pos] = true;
				build.add(c);
			}
		}
		
		public class Outside implements Predicate<Area> 
		{

			@Override
			public boolean test(Area area) 
			{
				for (int i = 0; i < _dimensions; i++)
				{
					if ( area.low()[i] > high[i] || 
						area.high()[i] < low[i] )
						return true;
				}
				return false;
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
