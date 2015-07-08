package spatialGrid;

public class DoubleGrid extends ObjectGrid
{
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public DoubleGrid()
	{
		
	}
	
	public void init(int[] nVoxel, int[][] padding, Double[] resolution)
	{
		super.init(nVoxel, padding, resolution);
		this.setAll(0.0);
	}
	
	/*************************************************************************
	 * GRID GETTERS & SETTERS
	 ************************************************************************/
	
	public void setValueAt(int[] position, Double newValue)
	{
		this.setValueAt(this.getIndex(position), newValue);
	}
	
	protected Double getValueAt(int index)
	{
		return (Double) this.getValueAt(index);
	}
	
	public void setAll(Double newValue)
	{
		this.setAll((Object) newValue); 
	}
	
	/*************************************************************************
	 * GRID METHODS
	 ************************************************************************/
	
	public void multiplyAllBy(Double multiplier)
	{
		for ( int index = 0; index < this._voxelsTotal; index++ )
			this.setValueAt(index, multiplier * this.getValueAt(index));
	}
	
}
