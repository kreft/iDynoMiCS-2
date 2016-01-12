/**
 * 
 */
package grid;

import org.w3c.dom.Node;

import utility.ExtraMath;

/**
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class ResolutionCalculator
{
	public abstract class ResCalc
	{
		protected int _nVoxel;
		
		// TODO void init(Node xmlNode);
		
		abstract void init(double targetResolution, double totalLength);
		
		public int getNVoxel()
		{
			return this._nVoxel;
		}
		
		public abstract double getResolution(int voxelIndex);
	}
	
	public abstract class SameRes extends ResCalc
	{
		protected double _resolution;
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution;
		}
	}
	
	public abstract class VariableRes extends ResCalc
	{
		protected double _resolution[];
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution[voxelIndex];
		}
	}
	
	/*************************************************************************
	 * USEFUL SUBMETHODS
	 ************************************************************************/
	
	private static double resDiff(double trialRes, double targetRes)
	{
		return Math.abs(trialRes - targetRes)/targetRes;
	}
	
	private static boolean isAltResBetter(double res, double altRes,
															double targetRes)
	{
		return resDiff(altRes, targetRes) < resDiff(res, targetRes);
	}
	
	/**************************************************************************
	 * COMMON RESOLUTION CALCULATORS
	 *************************************************************************/
	
	/**
	 * \brief The simplest distribution of resolutions, where all are the same,
	 * no matter where in the compartment.
	 */
	public class UniformResolution extends SameRes
	{
		@Override
		public void init(double targetResolution, double totalLength)
		{
			this._nVoxel = (int) (totalLength / targetResolution);
			this._resolution = totalLength / this._nVoxel;
			double altRes = totalLength / (this._nVoxel + 1);
			if ( isAltResBetter(this._resolution, altRes, targetResolution) )
			{
				this._nVoxel++;
				this._resolution = altRes;
			}
		}
	}
	
	/**
	 * \brief A distribution of resolutions that guarantees there will be
	 * <i>2<sup>n</sup> + 1</i> voxels, where <i>n</i> is a natural number.
	 */
	public class MultiGrid extends SameRes
	{
		@Override
		public void init(double targetResolution, double totalLength)
		{
			/* Single-voxel test to start with. */
			this._nVoxel = 1;
			this._resolution = totalLength;
			/* Variables to test splitting the grid into more voxels. */
			int exponent = 0;
			int altNVoxel = 2;
			double altRes = totalLength / altNVoxel;
			/* Testing loop. */
			while( isAltResBetter(this._resolution, altRes, targetResolution) )
			{
				this._nVoxel = altNVoxel;
				exponent++;
				altNVoxel = ExtraMath.exp2(exponent) + 1;
				this._resolution = altRes;
				altRes = totalLength / altNVoxel;
			}
		}
	}
}