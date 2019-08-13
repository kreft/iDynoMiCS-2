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
	
	private double[] _lengths;
	
	private double _minWidth;
	
	private double _slitWidth;
	
	private Slit<T> _previous = null;
	
	private Slit<T> _next = null;
	
	private int _dim = 0;

	private LinkedList<Slit<T>> _slits = null;

	private LinkedList<Area> _entries = new LinkedList<Area>();

	
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
		super(low, high);
		_root = this;
		_minWidth = min;
		_periodic = periodic;
		_lengths = Vector.minus(high, low);
		_slitWidth = _lengths[0] / numberOfSlits(0);
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
		super(low, high);
		_minWidth = min;
		_root = root;
		this._dim = dim;
		_lengths = Vector.minus(high, low);
		if ( dim < levels)
		{
			_slitWidth = _lengths[dim+1] / numberOfSlits(dim+1);
			_slits = buildSlits( dim+1 );
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
			Slit<T> slit = new Slit<T>( _minWidth, Vector.replace(dim, cur, getLow()), 
					Vector.replace( dim, cur+_slitWidth, getHigh() ), 
					dim, _lengths.length-1, this._root );
			_slits.add( slit );
			cur += _slitWidth;
			if (_slits.size() != 1) 
			{
				slit.setPrev( previous );
				previous.setNext( slit );
			}
			if ( _slits.size() == numberOfSlits(dim) && this._root._periodic[dim])
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
		int n = (int) (_lengths[dim]/_minWidth - (_lengths[dim]/_minWidth % 3));
		if ( n > 1 )
			return n;
		else
			return 1;
	}
	
	private int destination(double coord)
	{
		if ( this._root._periodic[this._dim] && coord < this._root.getLow()[_dim] )
			coord += this._root._lengths[_dim];
		return (int) Math.floor((coord-this._root.getLow()[_dim])/_slitWidth);
	}
	
	public void setPrev(Slit<T> slit)
	{
		this._previous = slit;
	}
	
	public void setNext(Slit<T> slit)
	{
		this._next = slit;
	}

	@Override
	public List<T> localSearch(double[] coords, double[] dimension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> search(double[] coords, double[] dimension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> search(BoundingBox boundingBox) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> search(List<BoundingBox> boundingBoxes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> all() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(double[] coords, double[] dimensions, T entry) {
		if( this._slits != null)
			_slits.get( destination(coords[_dim]) ).insert(coords, dimensions, entry);
		else
		{
			double[] high = Vector.add(coords, dimensions);
			double[] low = coords;
			for (int i = 0; i < high.length; i++ )
			{
				if ( this._root._periodic[i] && high[i] > this._root.getHigh()[i] )
					high[i] -= this._root._lengths[i];
				if ( this._root._periodic[i] && low[i] < this._root.getLow()[i] )
					low[i] += this._root._lengths[i];
			}
			this._entries.add(new Entry<T>(low, high, entry));
		}
			
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) {
		this.insert(boundingBox.lowerCorner(), boundingBox.ribLengths(), entry);
		
	}

	@Override
	public T getRandom() {
		System.out.println("unsuported method Slit getRandom");
		return null;
	}

	@Override
	public boolean delete(T entry) 
	{
		System.out.println("unsuported method Slit delete");
		return false;
	}


}
