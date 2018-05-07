package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;

@SuppressWarnings( {"rawtypes", "unchecked"} )
public class Node<T> extends Area
{
	
	/**
	 * 
	 */
	private final SplitTree<T> splitTree;
	
	protected boolean _leafNode;

	private LinkedList<Area> _entries = new LinkedList<Area>();
	
	private SplitTree<T> _tree;

	public Node(SplitTree<T> splitTree, double[] low, double[] high, boolean isLeaf, SplitTree<T> tree,
			boolean[] periodic)
	{
		super(low, high);
		this.splitTree = splitTree;
		this._leafNode = isLeaf;
		this._tree = tree;
	}

	
	public List<Entry> find(Area test) 
	{
		LinkedList<Entry> out = new LinkedList<Entry>();
		if ( ! this.test(test) )
		{
			if ( this._leafNode )
				return this.allConc(out, test);
			else
			{
				for ( Area a : _entries )
				{
					if ( out.isEmpty() )
					{
						for (Entry e :((Node<T>) a).find(test))
							out.add(e);
					}
					else
					{
						for (Entry e :((Node<T>) a).find(test))
							if ( ! out.contains(e))
								out.add(e);
					}
				}
			}
		}
		return out;
	}
	
	public List<Entry> findUnfiltered(Area area) 
	{
		LinkedList<Entry> out = new LinkedList<Entry>();
		if ( ! this.test(area) )
		{
			if ( this._leafNode )
				return this.allUnfiltered(out);
			else
			{
				for ( Area a : _entries )
					out.addAll(((Node<T>) a).findUnfiltered(area));
			}
		}
		return out;
	}

	public void add(Area entry)
	{
		if ( ! this.test(entry) )
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
					((Node<T>) a).add(entry);
			}
		}
		else if ( this.equals(_tree._root))
		{
			Log.out(Tier.CRITICAL, "Split tree skipped entry outside the domain");
		}
		
	}
	
	public void add(List<Area> entries) 
	{
		entries.removeIf(this);
		for (Area entry : entries) 
			this.add(entry);
	}
	
	protected LinkedList<Area> getEntries() {
		return _entries;
	}
	
	protected LinkedList<Area> getSectorEntries() {
		LinkedList<Area> out = new LinkedList<Area>();
		for (Area a : getEntries())
			if( this.sectorTest(a))
				out.add(a);
		return out;
	}

	public boolean leafless()
	{
		if (this._leafNode)
			return false;
		for ( Area entry : this.getEntries() )
			if( ((Node) entry)._leafNode )
				return false;
		return true;
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
				((Node<T>) a).allUnfiltered(out);
		}
		return out;
	}
	
	public List<Entry> allConc(LinkedList<Entry> out, Area test)
	{
		if (out.isEmpty())
		{
			for (Area a : this.getEntries())
			{
				for (int dim = 0; dim < this.splitTree._dimensions; dim++)
				{
					if ( ! a.test(test) )
					{
						out.add( (Entry) a);
						break;
					}
				}
			}
			return out;
		}
		else
		{
			for (Area a : this.getEntries())
			{
				for (int dim = 0; dim < this.splitTree._dimensions; dim++)
				{
					if ( ! a.test(test) )
					{
						if ( ! out.contains(a) )
							out.add( (Entry) a);
						break;
					}
				}
			}
			return out;
		}
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
				((Node<T>) a).allEntries(out);
		}
		return out;
	}
	
	public List<Node<T>> allLeaves(LinkedList<Node<T>> out) 
	{
		if ( this._leafNode)
		{
			out.add( this );
		}
		else
		{
			for (Area a : this.getEntries())
				((Node<T>) a).allLeaves(out);
		}
		return out;
	}
	
	public void purge()
	{
		getEntries().removeIf(this);
	}
	
	public void promote(List<Node<T>> nodes)
	{
		this.getEntries().clear();
		this._leafNode = false;
		for (Node<T> n : nodes)
			this.add(n);
	}
	
	/* ************************************************************************
	 * Helper methods
	 */
	double[] splits()
	{
		double[] split = new double[this.low.length];
		for (int i = 0; i < this.low.length; i++)
			split[i] = this.low[i] + ( (this.high[i] - this.low[i]) / 2.0 );
		return split;
	}
	
	double[] corner(double[] lower, double[] higher, boolean[] combination)
	{
		double [] out = new double[combination.length];
		for (int i = 0; i < combination.length; i++)
			out[i] = (combination[i] ? higher[i] : lower[i]);
		return out;
	}
	
	List<boolean[]> combinations()
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
}