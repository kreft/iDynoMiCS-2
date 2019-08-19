package spatialRegistry.splitTree;

import java.util.ArrayList;
import java.util.List;

import spatialRegistry.Area;
import spatialRegistry.Entry;

public class Node<T> extends Area
{
	private final ArrayList<Entry<T>> _entries = 
			new ArrayList<Entry<T>>(SplitTree._maxEntries);
	private final ArrayList<Node<T>> _nodes =
			new ArrayList<Node<T>>(SplitTree._childnodes);
	private final boolean atomic;


	public Node( double[] low, double[] high)
	{
		super(low, high);
		this.atomic = isAtomic(low, high);
	}
	
	public List<T> find(List<T> out, Area test) 
	{
		if ( ! this.test(test) )
		{
			for ( Node<T> a : _nodes )
				a.find(out, test);
			return this.allConc(out, test);
		}
		return out;
	}

	public void add( Entry<T> entry )
	{
		if ( ! this.test(entry) )
		{
			if ( this._nodes.isEmpty() )
			{
				this.getEntries().add(entry);
				if( this.size() > SplitTree._maxEntries &! this.atomic )
					split();
			}
			else
			{
				for ( Node<T> a : this._nodes )
					a.add(entry);
			}
		}		
	}
	
	public void add( List<Entry<T>> entries ) 
	{
		for (Entry<T> entry : entries) 
			this.add( entry );
	}

	private List<Entry<T>> getEntries() {
		return _entries;
	}

	private boolean remove(Entry<T> entry)
	{
		return this._entries.remove(entry);
	}
	
	/**
	 * Delete any occurrence of object member (Warning, the entire tree has
	 * to be searched for member, this is a slow process, do not use this if 
	 * the tree will be rebuild before it is searched anyway).
	 * @param member
	 * @return
	 */
	public boolean delete(T member) 
	{
		boolean out = false;
		for( Entry<T> t : _entries)
			if ( t.getEntry() == member )
				out = remove(t);
		for ( Node<T> a : _nodes )
			out = ( out ? out : a.delete(member) );
		return out;	
	}
	
	public int size()
	{
		return getEntries().size();
	}
	
	private List<T> allConc(List<T> out, Area test)
	{
		if (out.isEmpty())
		{
			for (Entry<T> a : this.getEntries())
				if ( ! a.test(test) )
					out.add( a.getEntry() );
		}
		else
		{
			for (Entry<T> a : this.getEntries())
				if ( ! out.contains(a.getEntry()) )
					if ( ! a.test(test) )
						out.add( a.getEntry() );
		}
		return out;
	}

	private void split()
	{
		Node<T> newNode;
		for ( boolean[] b : combinations())
		{
			newNode = new Node<T>( corner(getLow(), splits(), b), 
					corner(splits(), getHigh(), b));
			newNode.add(this.getEntries());
			this._nodes.add(newNode);
		}

		/* promote node from leaf to branch */
		this.getEntries().clear();
	}	
	
	/**
	 * Whipe all entries from the tree (testing shows rebuilding a new tree
	 * instead is slightly faster.
	 */
	public void whipe() 
	{
		this._entries.clear();
		for ( Node<T> a : _nodes )
			a.whipe();
	}

	@Override
	public boolean periodic(Area area, int dim)
	{
		/* if the partner area is not passing a periodic boundary in
		 * this dimension  */
		if ( !area.periodic()[dim] )
		{
			return normal(area, dim);
		}
		else
		{
			/* if the partner area is passing a periodic boundary in
			 * this dimension  */
			return ( getLow()[dim] > area.getHigh()[dim] && 
					getHigh()[dim] < area.getLow()[dim] );	
		}
	}


	/* ************************************************************************
	 * Helper methods
	 */
	private double[] splits()
	{
		double[] split = new double[this.getLow().length];
		for (int i = 0; i < this.getLow().length; i++)
			split[i] = this.getLow()[i] + 
				( (this.getHigh()[i] - this.getLow()[i]) / 2.0 );
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
		return combinations(getLow().length);
	}
	
	private List<boolean[]> combinations(int length)
	{
		boolean[] a = new boolean[length];
		List<boolean[]> b = new ArrayList<boolean[]>(SplitTree._childnodes);
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
			boolean[] c = new boolean[getLow().length];
			for ( int j = 0; j < pos; j++ )
				c[j] = build.get(i)[j];
			c[pos] = true;
			build.add(c);
		}
	}
	
	private boolean isAtomic(double[] low, double[] high)
	{
		return ( high[SplitTree._longest] - low[SplitTree._longest] < 0.1);

	}
}