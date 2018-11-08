package shape.subvoxel;

public class IntegerArray 
{
	private int[] _array;
	
	public IntegerArray()
	{
		
	}
	
	public IntegerArray(int[] array)
	{
		this.set(array);
	}
	
	@Override
	public int hashCode()
	{
		int out = 1;
		for ( int i : _array )
			out *= i;
		return out;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if ( o instanceof IntegerArray)
		{
			IntegerArray other = (IntegerArray) o;
			for( int i = 0, d=_array.length; i<d; i++)
			{
				if(this._array[i] != other.get(i))
					return false;
			}
			return true;
		}
		return false;
	}
	
	public int get(int i)
	{
		return this._array[i];
	}
	
	public int[] get()
	{
		return this._array;
	}
	
	public void set(int i, int value)
	{
		this._array[i] = value;
	}
	
	public void set(int[] array)
	{
		this._array = array;
	}
}
