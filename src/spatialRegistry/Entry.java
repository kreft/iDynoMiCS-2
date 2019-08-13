package spatialRegistry;

import java.util.List;

public class Entry<T> extends Area
{
	
	private T entry;
	
	public Entry(double[] low, double[] high, T entry)
	{
		super(low, high);
		this.setEntry(entry);
	}

	@Override
	public void add(List<Area> entries) {
		// Do nothing
	}

	public T getEntry() {
		return entry;
	}

	public void setEntry(T entry) {
		this.entry = entry;
	}

}