/**
 * 
 */
package grid;

import org.w3c.dom.Node;

import grid.resolution.ResolutionCalculator.ResCalc;
import linearAlgebra.Array;
import linearAlgebra.Vector;

/**
 * \brief A dummy grid for use by dimensionless shapes (e.g. for a chemostat).
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class DummyGrid extends SpatialGrid
{
	/**
	 * The volume of the single voxel in this grid.
	 */
	protected double _volume;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief New {@code DummyGrid} object, where the volume must be specified.
	 */
	public DummyGrid(double volume)
	{
		this._volume = volume;
	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		if ( this.hasArray(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
			this._array.put(type, Array.array(1, initialValues));
	}
	
	/*************************************************************************
	 * USEFUL METHODS
	 ************************************************************************/
	
	@Override
	public String arrayAsText(ArrayType type)
	{
		return String.valueOf(this._array.get(type)[0][0][0]);
	}
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	public static final GridGetter dimensionlessGetter(double volume)
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(double[] totalLength, Node node)
			{
				return new DummyGrid(volume);
			}
		};
	}
	
	/*************************************************************************
	 * INHERITED METHODS THAT DO NOTHING
	 ************************************************************************/
	
	// TODO throw exception when these are called?
	
	@Override
	public void calcMinVoxVoxResSq()
	{
		
	}
	
	@Override
	protected int[] getNVoxel(int[] coords, int[] outNVoxel)
	{
		return Vector.onesInt(3);
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		return null;
	}
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		return 0;
	}
	
	@Override
	public int[] resetNbhIterator()
	{
		return null;
	}

	@Override
	public int[] nbhIteratorNext()
	{
		return null;
	}
	
	@Override
	public double getNbhSharedSurfaceArea()
	{
		return 0.0;
	}

	@Override
	public double getTotalLength(int dim) {
		// TODO Auto-generated method stub
		return 0;
	}
}
