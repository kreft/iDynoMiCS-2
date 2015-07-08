package spatialGrid;

public abstract class ObjectGrid
{
	/**
	 * Name of this grid.
	 */
	protected String _name;
	
	/**
	 * Grid resolution, i.e. side length of each voxel.
	 */
	protected Double[] _resolution;
	
	/**
	 * 
	 */
	protected int _nDim;
	
	/**
	 * Number of voxels in each dimension.
	 */
	protected int[] _nVoxel;
	
	/**
	 * Number of padding voxels in each dimension.
	 */
	protected int[][] _padding;
	
	/**
	 * 
	 */
	protected int _voxelsUnpadded;
	
	/**
	 * 
	 */
	protected int _voxelsTotal;
	
	/**
	 * 
	 */
	protected Object[] _grid;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ObjectGrid()
	{
		
	}
	
	public void init(int[] nVoxel, int[][] padding, Double[] resolution)
	{
		this._nDim = nVoxel.length;
		this._nVoxel = new int[this._nDim];
		this._padding = new int[this._nDim][2];
		this._resolution = new Double[this._nDim];
		this._voxelsUnpadded = 1;
		this._voxelsTotal = 1;
		for ( int i = 0; i < this._nDim; i++ )
		{
			this._nVoxel[i] = nVoxel[i];
			this._padding[i][0] = padding[i][0];
			this._padding[i][1] = padding[i][1];
			this._voxelsUnpadded *= this._nVoxel[i];
			this._voxelsTotal *= padding[i][0] + nVoxel[i] + padding[i][1];
			this._resolution[i] = resolution[i];
		}
		this._grid = new Object[this._voxelsTotal];
	}
	
	public void init(int[] nVoxel, int[][] padding, Double resolution)
	{
		this._nDim = nVoxel.length;
		this._resolution = new Double[this._nDim];
		for ( int i = 0; i < this._nDim; i++ )
			this._resolution[i] = resolution;
		this.init(nVoxel, padding, this._resolution);
	}
	
	public void init(int[] nVoxel, Double resolution)
	{
		this._nDim = nVoxel.length;
		this._padding = new int[this._nDim][2];
		for ( int i = 0; i < this._nDim; i++ )
			this._padding[i][0] = this._padding[i][1] = 0;
		this.init(nVoxel, this._padding, resolution);
	}
	
	/*************************************************************************
	 * BASIC GETTERS & SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Sets this grid's name to the String supplied.
	 * 
	 * @param name	String to be set as this grid's name.
	 */
	public void setName(String name)
	{
		this._name = name;
	}
	
	/**
	 * \brief Returns this grid's name.
	 * 
	 * @return	String value of this grid's name.
	 */
	public String getName()
	{
		return this._name;
	}
	
	/**
	 * \brief Checks whether the String given matches this grid's name.
	 * 
	 * @param name	String to be checked.
	 * @return	Whether the String given matches this grid's name or not.
	 */
	public Boolean isName(String name)
	{
		return this._name == name;
	}
	
	/**
	 * 
	 * @param dimensionIndex
	 * @return
	 */
	public Double getResolution(int dimensionIndex)
	{
		return this._resolution[dimensionIndex];
	}
	
	/**
	 * 
	 * @return
	 */
	public Double getVoxelVolume()
	{
		Double out = 1.0;
		for ( int i = 0; i < this._nDim; i++ )
			out *= this._resolution[i];
		return out;
	}
	
	/*************************************************************************
	 * GRID INDEXING
	 ************************************************************************/
	
	protected int getIndex(int[] position)
	{
		int index = 0;
		int buffer = 1;
		for ( int i = 0; i < this._nDim; i++ )
		{
			index += buffer * ( this._padding[i][0] + position[i]);
			buffer *=
				this._padding[i][0] + this._nVoxel[i] + this._padding[i][1];
		}
		return index;
	}
	
	protected int[] getPosition(int index)
	{
		int[] position = new int[this._nDim];
		int buffer = this._voxelsTotal;
		for (int i = this._nDim - 1; i >= 0; i--)
		{
			buffer = buffer/ this._nVoxel[i];
			position[i] = index / buffer;
			index = index % buffer;
		}
		return position;
	}
	
	/*************************************************************************
	 * GRID GETTERS & SETTERS
	 ************************************************************************/
	
	protected void setValueAt(int index, Object newValue)
	{
		this._grid[index] = newValue;
	}
	
	protected void setValueAt(int[] position, Object newValue)
	{
		this.setValueAt( this.getIndex(position), newValue );
	}
	
	protected void setAll(Object newValue)
	{
		for ( int index = 0; index < this._voxelsTotal; index++ )
			this.setValueAt(index, newValue);
	}
	
	protected Object getValueAt(int index)
	{
		return this._grid[index];
	}
	
	protected Object getValueAt(int[] position)
	{
		return this.getValueAt( this.getIndex(position) );
	}
}