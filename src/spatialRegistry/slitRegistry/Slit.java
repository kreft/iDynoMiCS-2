package spatialRegistry.slitRegistry;

import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;
import spatialRegistry.Area;
import spatialRegistry.Entry;
import spatialRegistry.SpatialRegistry;
import surface.BoundingBox;

public class Slit<T> extends Area implements SpatialRegistry<T> {
	
	private Slit<T> _root;
	
	private boolean[] _periodic; 
	
	private double[] _domainHigh;
	
	private double[] _domainLow;
		
	private double _minWidth;
	
	private double _slitWidth;
	
	private Slit<T> _previous = null;
	
	private Slit<T> _next = null;
	
	private int _dim = 0;

	private LinkedList<Slit<T>> _slits = null;

	private LinkedList<Entry<T>> _entries = new LinkedList<Entry<T>>();

	
	/**
	 * \brief Root constructor
	 * 
	 * @param dimensions
	 * @param min
	 * @param max
	 * @param low
	 * @param high
	 * @param periodic
	 */
	public Slit(int dimensions, double min, 
	double[] low, double[] high, boolean[] periodic)
	{
		super(low, high, Vector.copy(periodic));
		_root = this;
		_minWidth = min;
		_periodic = periodic;
		_domainHigh = high;
		_domainLow = low;
		_slitWidth = lengths(low,high)[0] / numberOfSlits(0);
		_slits = buildSlits(0);

	}
	
	/** 
	 * \brief Child slit constructor (internal use only).
	 * @param min
	 * @param max
	 * @param dim
	 * @param levels
	 */
	public Slit(double min, double[] low, double[] high, int dim, int levels, Slit<T> root)
	{
		super(low, high, Vector.copy(root._periodic));
		_minWidth = min;
		_root = root;
		_periodic = root._periodic;
		_domainHigh = root.getHigh();
		_domainLow = root.getLow();
		this._dim = dim+1;
		if ( dim < levels)
		{
			_slitWidth = lengths(low,high)[_dim] / numberOfSlits(_dim);
			_slits = buildSlits( _dim );
		}
	}
	
	/**
	 * \brief build slits for given dimension
	 * @param dim
	 * @return
	 */
	public LinkedList<Slit<T>> buildSlits(int dim) 
	{
		_slits = new LinkedList<Slit<T>>();
		double cur = this.getLow()[dim];
		Slit<T> previous = null;
		for( int i = 0; i < numberOfSlits(dim); i++)
		{
			Slit<T> slit = new Slit<T>( _minWidth, 
					Vector.replace(dim, cur, getLow() ), 
					Vector.replace( dim, cur+_slitWidth, getHigh() ), 
					dim, _domainLow.length-1, _root );
			_slits.add( slit );
			cur += _slitWidth;
			if (_slits.size() != 1) 
			{
				slit.setPrev( previous );
				previous.setNext( slit );
			}
			if ( _slits.size() == numberOfSlits(dim) && _periodic[dim])
			{
				slit.setNext( _slits.getFirst() );
				_slits.getFirst().setPrev( slit );
			}
			previous = slit;
		}
		return _slits;
	}
	
	/**
	 * \brief returns the first lower number that results in slits > minWidth
	 * and divisible by 3, or 1 if that number is 3 or less.
	 * @param dim
	 * @return
	 */
	public int numberOfSlits(int dim)
	{
		int n = (int) ( lengths()[dim] / _minWidth - 
				( lengths()[dim] / _minWidth % 3 ) );
		if ( n > 1 )
			return n;
		else
			return 1;
	}
	
	private int destination(double coord)
	{
		if ( _periodic[_dim] && coord < _domainLow[_dim] )
			coord += lengths()[_dim];
		return (int) Math.floor( (coord-_domainLow[_dim]) / _slitWidth );
	}
	
	public void setPrev(Slit<T> slit)
	{
		this._previous = slit;
	}
	
	public void setNext(Slit<T> slit)
	{
		this._next = slit;
	}
	
	public double[] lengths(double[] low, double[] high)
	{
		return Vector.minus(high, low);
	}
	
	public double[] lengths()
	{
		return lengths( _domainLow, _domainHigh );
	}

	@Override
	public List<T> search(double[] low, double[] high) {
		LinkedList<T> out = new LinkedList<T>();
		/* also does periodic search */
		for (int i = 0; i < high.length; i++ )
		{
			if ( _periodic[i] && high[i] > _domainHigh[i] )
				high[i] -= lengths()[i];
			if ( _periodic[i] && low[i] < _domainLow[i] )
				low[i] += lengths()[i];
		}
		if ( _root == this )
			return _slits.get( destination(low[_dim]) ).search(low, high);
		else
		{
			out.addAll( this.localSearch(low, high) );
			if( _previous != null )
				out.addAll( _previous.localSearch(low, high) );
			if( _next != null )
			{
				out.addAll( _next.localSearch(low, high) );
			}
		}
		return out;
	}
	
	public List<T> localSearch(double[] low, double[] high) 
	{
		for (int i = 0; i < high.length; i++ )
		{
			if ( _periodic[i] && high[i] > _domainHigh[i] )
				high[i] -= lengths()[i];
			if ( _periodic[i] && low[i] < _domainLow[i] )
				low[i] += lengths()[i];
		}
		LinkedList<T> out = new LinkedList<T>();
		if (_slits != null )
			return _slits.get( destination(low[_dim]) ).search(low, high);
		else
		{
			for (Entry<T> e : find(new Entry<T>(low, 
					high, _periodic, null)))
				out.add(e.getEntry());
		}
		return out;
	}
	
	public Entry<T> searchField(double[] coords, double[] dimension)
	{
		double[] high = Vector.add(coords, dimension);
		double[] low = coords;
		for (int i = 0; i < high.length; i++ )
		{
			if ( _periodic[i] && high[i] > _domainHigh[i] )
				high[i] -= lengths()[i];
			if ( _periodic[i] && low[i] < _domainLow[i] )
				low[i] += lengths()[i];
		}
		return new Entry<T>(low, high, _periodic, null);
	}
	
	public List<Entry<T>> allConc(LinkedList<Entry<T>> out, Area test)
	{
		if (out.isEmpty())
		{
			for (Entry<T> a : this._entries)
			{
				for (int dim = 0; dim < this._domainLow.length; dim++)
				{
					if ( ! a.test(test) )
					{
						out.add( (Entry<T>) a);
						break;
					}
				}
			}
			return out;
		}
		else
		{
			for (Entry<T> a : this._entries)
			{
				for (int dim = 0; dim < this._domainLow.length; dim++)
				{
					if ( ! a.test(test) )
					{
						if ( ! out.contains(a) )
							out.add( (Entry<T>) a);
						break;
					}
				}
			}
			return out;
		}
	}
	
	private List<Entry<T>> find(Entry<T> area)
	{
		LinkedList<Entry<T>> out = new LinkedList<Entry<T>>();
		return allConc(out, area);
	}

	@Override
	public List<T> search(BoundingBox boundingBox) 
	{
		return this.search(boundingBox.lowerCorner(), boundingBox.higherCorner());
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
	public void insert(double[] low, double[] high, T entry) {
		if( this._slits != null)
			_slits.get( destination(low[_dim]) ).insert(low, high, entry);
		else
		{
			for (int i = 0; i < high.length; i++ )
			{
				if ( _periodic[i] && high[i] > _domainHigh[i] )
					high[i] -= lengths()[i];
				if ( _periodic[i] && low[i] < _domainLow[i] )
					low[i] += lengths()[i];
			}
			this._entries.add(new Entry<T>(low, high, _periodic, entry));
		}
			
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) {
		this.insert(boundingBox.lowerCorner(), boundingBox.higherCorner(), entry);
		
	}
	
	@Override
	public boolean delete(T entry)
	{
		return false;
	}
}
