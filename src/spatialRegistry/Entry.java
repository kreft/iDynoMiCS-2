package spatialRegistry;


import linearAlgebra.Vector;

public class Entry<T> extends Area
{
	
	private T entry;
	
	public Entry(double[] low, double[] high, boolean[] periodic, T entry)
	{
		super(low, high, Vector.copy(periodic));
		this.setEntry(entry);
	}

	public T getEntry() {
		return entry;
	}

	public void setEntry(T entry) {
		this.entry = entry;
	}

}