package spatialRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import boundary.PeriodicAgentBoundary;

/**
 * A dummy spatial registry for dimensionless compartments.
 */
public class DummyTree<T> extends SpatialRegistry<T>
{
	private List<T> _emptyList;
	
	public DummyTree()
	{
		this._emptyList = new LinkedList<T>();
	}
	
	@Override
	public List<T> search(double[] coords, double[] dimension)
	{
		return this._emptyList;
	}

	@Override
	public List<T> cyclicsearch(double[] coords, double[] dimension)
	{
		return this._emptyList;
	}

	@Override
	public List<T> all()
	{
		return this._emptyList;
	}

	@Override
	public void insert(double[] coords, double[] dimensions, T entry)
	{
		// TODO Some sort of warning message?
	}
}
