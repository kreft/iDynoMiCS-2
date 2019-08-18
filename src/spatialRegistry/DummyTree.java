package spatialRegistry;

import java.util.LinkedList;
import java.util.List;

import surface.BoundingBox;

/**
 * A dummy spatial registry for dimensionless compartments.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class DummyTree<T> implements SpatialRegistry<T>
{
	private List<T> _emptyList;
	
	public DummyTree()
	{
		this._emptyList = new LinkedList<T>();
	}

	@Override
	public List<T> search(double[] low, double[] high)
	{
		return this._emptyList;
	}

	@Override
	public void insert(double[] low, double[] high, T entry)
	{
		// TODO Some sort of warning message?
	}

	@Override
	public void insert(BoundingBox boundingBox, T entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<T> search(Area area) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<T> search(List<BoundingBox> boundingBoxes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean delete(T entry)
	{
		return false;
	}
	
	public void clear()
	{

	}
}
