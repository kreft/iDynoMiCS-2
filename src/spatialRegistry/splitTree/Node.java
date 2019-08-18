package spatialRegistry.splitTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spatialRegistry.Area;
import spatialRegistry.Entry;

public class Node<T> extends Area
{
	private ArrayList<Entry<T>> _entries = new ArrayList<Entry<T>>(SplitTree._maxEntries+1);
	private ArrayList<Node<T>> _nodes;
	private final boolean atomic;


	public Node( double[] low, double[] high)
	{
		super(low, high);
		this.atomic = isAtomic(low, high);
	}
	
	public List<Entry<T>> find(Area test) 
	{
		LinkedList<Entry<T>> out = new LinkedList<Entry<T>>();
		if ( ! this.test(test) )
		{
			if ( this._nodes == null )
				return this.allConc(out, test);
			else
			{
				for ( Node<T> a : _nodes )
				{
					if ( out.isEmpty() )
					{
						for ( Entry<T> e : a.find(test) )
							out.add(e);
					}
					else
					{
						for ( Entry<T> e : a.find(test) )
							if ( ! out.contains(e) )
								out.add(e);
					}
				}
			}
		}
		return out;
	}
	
	public List<Entry<T>> findUnfiltered(Area area) 
	{
		LinkedList<Entry<T>> out = new LinkedList<Entry<T>>();
		if ( ! this.test(area) )
		{
			if ( this._nodes == null )
				return this.allUnfiltered(out);
			else
			{
				for ( Node<T> a : _nodes )
					out.addAll( a.findUnfiltered(area) );
			}
		}
		return out;
	}
	
	public void add( Entry<T> entry )
	{
		if ( ! this.test(entry) )
		{
			if ( this._nodes == null )
			{
				this.getEntries().add(entry);
				if( this.size() > SplitTree._maxEntries && !this.atomic )
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

	protected List<Entry<T>> getEntries() {
		return _entries;
	}

	public boolean remove(Entry<T> entry)
	{
		return this.getEntries().remove(entry);
	}
	
	public boolean delete(T member) 
	{
		for( Entry<T> t : _entries)
			if ( t.getEntry() == member )
				return remove(t);
		for ( Node<T> a : _nodes )
			return a.delete(member);
		return false;	
	}
	
	public int size()
	{
		return getEntries().size();
	}

	public List<Entry<T>> allUnfiltered(LinkedList<Entry<T>> out)
	{
		if ( this._nodes == null )
		{
			for (Entry<T> a : this.getEntries())
				out.add( a);
		}
		else
		{
			for (Node<T> a : this._nodes)
				a.allUnfiltered(out);
		}
		return out;
	}
	
	public List<Entry<T>> allConc(LinkedList<Entry<T>> out, Area test)
	{
		if (out.isEmpty())
		{
			for (Entry<T> a : this.getEntries())
					if ( ! a.test(test) )
						out.add( (Entry<T>) a);
		}
		else
		{
			for (Entry<T> a : this.getEntries())
					if ( ! a.test(test) )
						if ( ! out.contains(a) )
							out.add( (Entry<T>) a);
		}
		return out;
	}

	public void promote(ArrayList<Node<T>> nodes)
	{
		this.getEntries().clear();
		this._nodes = nodes;
	}
	
	public void split()
	{
		Node<T> newNode;
		ArrayList<Node<T>> childNodes = new ArrayList<Node<T>>(SplitTree._childnodes);
		
		for ( boolean[] b : combinations())
		{
			newNode = new Node<T>( corner(getLow(), splits(), b), 
					corner(splits(), getHigh(), b));
			newNode.add(this.getEntries());
			childNodes.add(newNode);
		}

		/* promote node from leaf to branch */
		promote(childNodes);
	}	
	
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
	double[] splits()
	{
		double[] split = new double[this.getLow().length];
		for (int i = 0; i < this.getLow().length; i++)
			split[i] = this.getLow()[i] + 
				( (this.getHigh()[i] - this.getLow()[i]) / 2.0 );
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
		return this.combinations(getLow().length);
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
			boolean[] c = linearAlgebra.Vector.copy(build.get(i));
			c[pos] = true;
			build.add(c);
		}
	}
	
	private boolean isAtomic(double[] low, double[] high)
	{
		for ( int i = 0; i < low.length; i++ )
			if( high[i] - low[i] > 1.0)
				return false;
		return true;
	}
}