package array;

public class NDimArray
{
	/**
	 * 
	 */
	protected int _nDim;
	
	/**
	 * Number of voxels in each dimension.
	 */
	protected int[] _nObject;
	
	/**
	 * 
	 */
	protected int _totalObjects;
	
	/**
	 * 
	 */
	protected Object[] _array;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public NDimArray()
	{
		
	}
	
	protected void init(int[] nObject)
	{
		this.init(nObject, null);
	}
	
	protected void init(int[] nObject, Object value)
	{
		this._nDim = nObject.length;
		this._totalObjects = 1;
		for ( int i = 0; i < this._nDim; i++ )
		{
			this._nObject[i] = nObject[i];
			this._totalObjects *= nObject[i];
		}
		
		this._array = new Object[this._totalObjects];
		this.setAll(value);
	}
	
	/*************************************************************************
	 * BASIC GETTERS & SETTERS
	 ************************************************************************/
	
	public int getNDim()
	{
		return this._nDim;
	}
	
	public int[] getDims()
	{
		return this._nObject;
	}
	
	/*************************************************************************
	 * ARRAY INDEXING
	 ************************************************************************/
	
	protected int getIndex(int[] position)
	{
		int index = 0;
		int buffer = 1;
		for ( int i = 0; i < this._nDim; i++ )
		{
			index += buffer * position[i];
			buffer *= this._nObject[i];
		}
		return index;
	}
	
	protected int[] getPosition(int index)
	{
		int[] position = new int[this._nDim];
		int buffer = this._totalObjects;
		for (int i = this._nDim - 1; i >= 0; i--)
		{
			buffer = buffer/ this._nObject[i];
			position[i] = index / buffer;
			index = index % buffer;
		}
		return position;
	}
	
	/*************************************************************************
	 * ARRAY GETTERS & SETTERS
	 ************************************************************************/
	
	protected void setValueAt(int index, Object newValue)
	{
		this._array[index] = newValue;
	}
	
	protected void setValueAt(int[] position, Object newValue)
	{
		this.setValueAt( this.getIndex(position), newValue );
	}
	
	protected void setAll(Object newValue)
	{
		for ( int index = 0; index < this._totalObjects; index++ )
			this.setValueAt(index, newValue);
	}
	
	protected Object getValueAt(int index)
	{
		return this._array[index];
	}
	
	protected Object getValueAt(int[] position)
	{
		return this.getValueAt( this.getIndex(position) );
	}
}
