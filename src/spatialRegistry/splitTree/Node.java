package spatialRegistry.splitTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import idynomics.Global;
import spatialRegistry.Area;
import spatialRegistry.Entry;

/**
 * \brief: The {@link Node} object is the structural element that forms the
 * {@link SplitTree} a {@link Node} can be either a branch node or a leaf node.
 * Branch will have child nodes but no entries, leaf nodes can have entries but
 * no child nodes. A full leaf node will be promoted to branch node, giving it 
 * 2^nDim child nodes on which its entries will be transfered.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <T>
 */
public class Node<T> extends Area
{
	private final ArrayList<Entry<T>> _entries;
	private final ArrayList<Node<T>> _nodes;
	private final boolean _atomic;
	private final SplitTree<T> _tree;

	/**
	 * Constructor
	 * @param low
	 * @param high
	 */
	public Node( double[] low, double[] high, SplitTree<T> tree)
	{
		super(low, high);
		this._tree = tree;
		this._atomic = isAtomic(low, high);
		this._nodes = new ArrayList<Node<T>>(this._tree.childnodes);
		this._entries = new ArrayList<Entry<T>>(this._tree.maxEntries);
	}
	
	/**
	 * find all T that hit Area
	 * @param out
	 * @param test
	 * @return
	 */
	public List<T> find(List<T> out, Area test) 
	{
		if ( ! this.test(test) )
		{
			for ( Node<T> a : _nodes )
				a.find(out, test);
			return this.collectLocal(out, test);
		}
		return out;
	}

	/**
	 * \brief: add a new entry
	 * @param entry
	 */
	public void add( Entry<T> entry )
	{
		if ( ! this.test(entry) )
		{
			if ( this._nodes.isEmpty() )
			{
				this.getEntries().add(entry);
				if( this.size() > this._tree.maxEntries &! this._atomic )
					split();
			}
			else
			{
				for ( Node<T> a : this._nodes )
					a.add(entry);
			}
		}		
	}
	
	/**
	 * \brief: add multiple entries
	 * @param entries
	 */
	public void add( Collection<Entry<T>> entries ) 
	{
		for (Entry<T> entry : entries) 
			this.add( entry );
	}

	/**
	 * \brief: returns the entry list
	 * @return
	 */
	private List<Entry<T>> getEntries() {
		return _entries;
	}

	/**
	 * \brief: remove entry from this node's entry list
 	 * does not remove the entry from child nodes!
	 * @param entry
	 * @return
	 */
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
	
	/**
	 * \brief: amount of locally stored entries (excluding child nodes)
	 * @return
	 */
	public int size()
	{
		return getEntries().size();
	}
	
	/**
	 * \brief: Test locally stored entries against input area and adds them to
	 * input list on hit.
	 * @param out
	 * @param test
	 * @return
	 */
	private List<T> collectLocal(List<T> out, Area test)
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

	/**
	 * \brief: Promotes this leaf node to branch node and distributes entries
	 * over newly created leaf nodes.
	 */
	private void split()
	{
		Node<T> newNode;
		for ( boolean[] b : this._tree.combinations())
		{
			newNode = new Node<T>( corner(getLow(), midPoint(), b), 
					corner(midPoint(), getHigh(), b), this._tree);
			newNode.add(this.getEntries());
			this._nodes.add(newNode);
		}
		this.getEntries().clear();
	}	
	
	/**
	 * \brief: Whipe all entries from the tree (testing shows rebuilding a new
	 * tree instead is slightly faster.
	 */
	public void whipe() 
	{
		this._entries.clear();
		for ( Node<T> a : _nodes )
			a.whipe();
	}

	/**
	 * \brief: Overrides Area.periodic, SplitTree nodes never cross periodic
	 * boundaries and thus do not require periodic check.
	 */
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

	/**
	 * \brief: returns midpoint to determine corners of child nodes.
	 * @return
	 */
	private double[] midPoint()
	{
		double[] split = new double[this.getLow().length];
		for (int i = 0; i < this.getLow().length; i++)
			split[i] = this.getLow()[i] + 
				( (this.getHigh()[i] - this.getLow()[i]) / 2.0 );
		return split;
	}
	
	/**
	 * \brief: returns appropriate corner location for child node variant bool[]
	 * @param lower
	 * @param higher
	 * @param child
	 * @return
	 */
	private double[] corner(double[] lower, double[] higher, boolean[] child)
	{
		double [] out = new double[child.length];
		for (int i = 0; i < child.length; i++)
			out[i] = (child[i] ? higher[i] : lower[i]);
		return out;
	}
	
	/**
	 * \brief: returns true if this node should not be split any further (and
	 * thus allowing to obtain more entries than usual).
	 * @param low
	 * @param high
	 * @return
	 */
	private boolean isAtomic(double[] low, double[] high)
	{
		return ( high[this._tree.longest] - low[this._tree.longest] < 
				Global.atomic_length);

	}
}