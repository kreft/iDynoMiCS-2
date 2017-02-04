/**
 * 
 */
package shape.resolution;

import org.w3c.dom.Element;

import generalInterfaces.Copyable;
import instantiable.Instantiable;
import settable.Settable;

/**
 * \brief Abstract class for calculating grid resolutions.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ResolutionCalculator implements Copyable, Instantiable
{
	/**
	 * Total number of voxels along this dimension.
	 */
	protected int _nVoxel;
	/**
	 * Total extremes of this dimension.
	 */
	protected double _min, _max;
	/**
	 * The resolution for every voxel.
	 */
	protected double _resolution;
	/**
	 * Target resolution for every voxel.
	 */
	protected double _targetRes;

	public void instantiate(Element xmlElement, Settable parent)
	{
		
	}
	
	public abstract void init(double resolution, double min, double max);

	public int getNVoxel()
	{
		return this._nVoxel;
	}

	public void setExtremes(double min, double max)
	{
		this._min = min;
		this._max = max;
	}
	
	public double getTotalLength()
	{
		return this._max - this._min;
	}
	
	public void setResolution(double targetResolution)
	{
		this.init(targetResolution, this._min, this._max);
	}
	
	public double getResolution()
	{
		return this._resolution;
	}
	
	/**
	 * \brief Calculates the sum of all resolutions until 
	 * and including the resolution at voxelIndex.
	 * 
	 * @param voxelIndex Integer index of a voxel.
	 * @return Position of the minimum extreme of this voxel.
	 * @throws IllegalArgumentException if voxel is >= nVoxel.
	 */
	public double getCumulativeResolution(int voxelIndex)
	{
		if ( voxelIndex >= this._nVoxel )
			throw new IllegalArgumentException("Voxel index out of range");
		if ( voxelIndex < 0 )
			return this._min;
		return this._min + this._resolution * (voxelIndex + 1);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param voxelIndex
	 * @param inside
	 * @return
	 */
	public double getPosition(int voxelIndex, double inside)
	{
		// TODO safety
		double out = this.getCumulativeResolution(voxelIndex - 1);
		out += this._resolution * inside;
		return out;
	}
	
	/**
	 * \brief Calculates which voxel the given location lies inside.
	 * 
	 * @param location Continuous location along this axis.
	 * @return Index of the voxel this location is inside.
	 * @throws IllegalArgumentException if location is outside [0, length)
	 */
	public int getVoxelIndex(double location)
	{
		if ( location < this._min || location >= this._max )
			throw new IllegalArgumentException("Location out of range");
		return (int) ((location - this._min) / this._resolution);
	}
	
	public Object copy()
	{
		ResolutionCalculator out = null;
		try
		{
			out = this.getClass().newInstance();
			out._nVoxel = this._nVoxel;
			out._min = this._min;
			out._max = this._max;
			out._resolution = this._resolution;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return out;
	}
	
	/*************************************************************************
	 * USEFUL STATIC METHODS
	 ************************************************************************/
	
	protected static double resDiff(double trialRes, double targetRes)
	{
		return Math.abs(trialRes - targetRes)/targetRes;
	}

	protected static boolean isAltResBetter(double res, double altRes,
			double targetRes)
	{
		return resDiff(altRes, targetRes) < resDiff(res, targetRes);
	}
}
