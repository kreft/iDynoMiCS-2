package array;

public class NDimDouble extends NDimArray
{
	public NDimDouble()
	{
		
	}
	
	public NDimDouble(int[] nObject)
	{
		this.init(nObject, 0.0);
	}
	
	public NDimDouble(int[] nObject, Double value)
	{
		this.init(nObject, value);
	}
	
	protected void init(int[] nObject, Double value)
	{
		this.init(nObject, (Object) value);
	}
	
	public NDimDouble copy()
	{
		NDimDouble out = new NDimDouble();
		out.init(this._nObject);
		for ( int index = 0; index < this._totalObjects; index++ )
			out.setValueAt(index, this.getValueAt(index));
		return out;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	protected void setAll(Double newValue)
	{
		for ( int index = 0; index < this._totalObjects; index++ )
			this.setValueAt(index, newValue);
	}
	
	protected Double getValueAt(int index)
	{
		return (Double) this.getValueAt(index);
	}
	
	protected Double getValueAt(int[] position)
	{
		return (Double) this.getValueAt(position);
	}
	
	/*************************************************************************
	 * SCALAR METHODS
	 ************************************************************************/
	
	public void plusEquals(Double scalar)
	{
		for ( int index = 0; index < this._totalObjects; index++ )
			this.setValueAt(index, scalar + this.getValueAt(index));
	}
	
	public NDimDouble plus(Double multiplier)
	{
		NDimDouble out = this.copy();
		out.plusEquals(multiplier);
		return out;
	}
	
	public void timesEquals(Double scalar)
	{
		for ( int index = 0; index < this._totalObjects; index++ )
			this.setValueAt(index, scalar * this.getValueAt(index));
	}
	
	public NDimDouble times(Double scalar)
	{
		NDimDouble out = this.copy();
		out.timesEquals(scalar);
		return out;
	}
	
	/*************************************************************************
	 * BASIC METHODS INVOLVING OTHER ARRAYS
	 ************************************************************************/
	
	public void plusEquals(NDimDouble other)
	{
		
	}
}
