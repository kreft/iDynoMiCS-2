package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import linearAlgebra.Vector;
import spatialRegistry.SpatialRegistry;
import surface.BoundingBox;

/**
 * First version of nDimensional tree, behaves like quadtree in 2d and octree in
 * 3d, named "splitTree"
 * 
 * TODO clean-up
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class SplitTree<T> implements SpatialRegistry<T>
{	
	protected boolean _root = false;
	
	public Node node;
	
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
		this.node = new Node(low, high, true, this, periodic);
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
		if ( _periodic == null )
			return node.find(area);
		else
		{
			/* also does periodic search */
			double[] high = area.high();
			double[] low = area.low();
			for (int i = 0; i < high.length; i++ )
			{
				if ( this._periodic[i] && high[i] > this.node.high()[i] )
					high[i] -= this._lengths[i];
				if ( this._periodic[i] && low[i] < this.node.low()[i] )
					low[i] += this._lengths[i];
			}
			return find(new Entry(low, high, null));
		}

	}
	
	public void split(Node leaf)
	{
		Node newNode;
		List<Node> childNodes = new LinkedList<Node>();
		
		for ( boolean[] b : leaf.combinations())
		{
			newNode = new Node( leaf.corner(leaf.low, leaf.splits(), b), 
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
			for (Entry e : allEntries(entries))
				out.add(e.entry);
		return out;
	}
	
	public interface Area {
		
		public double[] low();
		
		public double[] high();
		
		public abstract void add(List<Area> entries);

	}
	
	public class Node implements Area
	{
		
		final double[] low;
		
		final double[] high;
		
		protected boolean _leafNode;

		private LinkedList<Area> _entries = new LinkedList<Area>();
		
		private Predicate<Area> _outTest;
		
		private SplitTree<T> _tree;

		public Node(double[] low, double[] high, boolean isLeaf, SplitTree<T> tree,
				boolean[] periodic)
		{
			this.low = low;
			this.high = high;
			this._leafNode = isLeaf;
			this._tree = tree;
			this._outTest = (periodic == null ? this.new OutsideNormal() :
				this.new OutsidePeriodic(periodic));
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
					{
						for (Entry e :((Node) a).find(area))
							if ( ! out.contains(e))
								out.add(e);
					}
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
				if ( this._leafNode || entry instanceof SplitTree.Node)
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
				for (int dim = 0; dim < _dimensions; dim++)
				{
					if ( ! test(a, area) )
					{
						if ( ! out.contains(a))
							out.add( (Entry) a);
						break;
					}
				}
			}
			return out;
		}
		
		public boolean test(Area a, Area b) 
		{
			/* periodic not set is all normal */
			for (int i = 0; i < _dimensions; i++)
			{
				if ( a.low()[i] > b.high()[i] || 
					a.high()[i] < b.low()[i] )
					return true;
			}
			return false;
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
		
		/**
		 * 
		 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
		 *
		 */
		public class OutsideNormal implements Predicate<Area> 
		{
			@Override
			public boolean test(Area area) 
			{
				/* periodic not set is all normal */
				for (int i = 0; i < _dimensions; i++)
				{
					if ( area.low()[i] > high[i] || 
						area.high()[i] < low[i] )
						return true;
				}
				return false;
			}
		}
		
		/**
		 * 
		 * Note Nodes are not allowed to wrap around the periodic boundaries 
		 * (on the other side of the boundary a new node should start)
		 * 
		 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
		 *
		 */
		public class OutsidePeriodic implements Predicate<Area> 
		{
			private boolean[] _periodic;
			
			public OutsidePeriodic(boolean[] periodic)
			{
				this._periodic = periodic;
			}
			
			@Override
			public boolean test(Area area) 
			{
				/* periodic set use the more expensive periodic check */
				for (int i = 0; i < _dimensions; i++)
					if ( periodic(area, i) )
						return true;
				return false;
						
			}
			
			private boolean normal(Area area, int dim)
			{
				return ( area.low()[dim] > high[dim] || 
						area.high()[dim] < low[dim] );
			}
			
			private boolean periodic(Area area, int dim)
			{
				/* if dim is not periodic */
				if ( !_periodic[dim] ) 
					return this.normal(area, dim);
				/* if area does not wrap around boundary */
				else if ( area.low()[dim] < area.high()[dim] ) 
					return this.normal(area, dim);
				/* else periodic evaluation */
				else
					return ( area.low()[dim] < high[dim] || 
							area.high()[dim] > low[dim] );		
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
		
		T entry;
		
		public Entry(double[] low, double[] high, T entry)
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
	
	/* *************************************************************************
	 * SpatialRegistry implementation
	 * *************************************************************************
	 * FIXME quick and dirty first version for testing.
	 */

	@Override
	public List<T> search(double[] coords, double[] dimension) 
	{

		LinkedList<T> out = new LinkedList<T>();
		for ( Entry e : find(new Entry(coords, Vector.add(coords, dimension), 
				null)))
		{
			out.add(e.entry);
		}
//		System.out.println(Vector.toString(coords) +"\n"+ Vector.toString(dimension)+"\n"+ out.toString());
		return out;
	}

	@Override
	public List<T> cyclicsearch(double[] coords, double[] dimension) 
	{
		return this.search(coords, dimension);
	}

	@Override
	public List<T> cyclicsearch(BoundingBox boundingBox) 
	{
		return this.search(boundingBox.lowerCorner(), boundingBox.ribLengths());
	}

	@Override
	public List<T> cyclicsearch(List<BoundingBox> boundingBoxes) 
	{
		LinkedList<T> out = new LinkedList<T>();
		for (BoundingBox b : boundingBoxes )
			out.addAll(cyclicsearch(b) );
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
		this.add(new Entry(coords, Vector.add(coords, dimensions), entry));
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) 
	{
		this.add(new Entry(boundingBox.lowerCorner(), Vector.add(boundingBox.lowerCorner(), boundingBox.ribLengths()), 
				entry));
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
