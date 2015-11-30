package spatialRegistry;

import java.util.LinkedList;
import java.util.List;

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
	public List<T> search(float[] coords, float[] dimension)
	{
		return this._emptyList;
	}

	@Override
	public List<T> cyclicsearch(float[] coords, float[] dimension)
	{
		return this._emptyList;
	}

	@Override
	public List<T> all()
	{
		return this._emptyList;
	}

	@Override
	public void insert(float[] coords, float[] dimensions, T entry)
	{
		// TODO Some sort of warning message?
	}
}
