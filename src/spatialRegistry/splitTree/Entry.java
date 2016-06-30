package spatialRegistry.splitTree;

import java.util.List;

public class Entry<T> extends Area
{
	
	T entry;
	
	public Entry(double[] low, double[] high, T entry)
	{
		super(low, high);
		this.entry = entry;
	}

	@Override
	public void add(List<Area> entries) {
		// Do nothing
	}

}