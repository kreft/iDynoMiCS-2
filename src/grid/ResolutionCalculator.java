/**
 * 
 */
package grid;

import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * \brief Collection of methods for calculating appropriate grid resolutions. 
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class ResolutionCalculator
{
	public abstract class ResCalc
	{
		/**
		 * Total number of voxels along this dimension.
		 */
		protected int _nVoxel;
		/**
		 * Total length along this dimension.
		 */
		protected double _length;
		
		// TODO void init(Node xmlNode);
		
		abstract void init(double targetResolution, double totalLength);
		
		public int getNVoxel()
		{
			return this._nVoxel;
		}
		
		public double getTotalLength()
		{
			return _length;
		}
		
		public abstract double getResolution(int voxelIndex);
		
		/**
		 * \brief Calculates the sum of all resolutions until 
		 * and including the resolution at voxelIndex.
		 * 
		 * @param voxelIndex
		 * @return
		 * @throws IllegalArgumentException if voxel is outside [0, nVoxel)
		 */
		public abstract double getCumulativeResolution(int voxelIndex);
		
		/**
		 * \brief Calculates which voxel the given location lies inside.
		 * 
		 * @param location Continuous location along this axis.
		 * @return Index of the voxel this location is inside.
		 * @throws IllegalArgumentException if location is outside [0, length)
		 */
		public abstract int getVoxelIndex(double location);
	}
	
	public abstract class SameRes extends ResCalc
	{
		/**
		 * The resolution for every voxel. 
		 */
		protected double _resolution;
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution;
		}
		
		@Override
		public double getCumulativeResolution(int voxelIndex)
		{
			if ( voxelIndex >= this._nVoxel )
			{
				throw new IllegalArgumentException("Voxel index out of range");
			}
			
			if (voxelIndex < 0)
				return 0;
			
			return this._resolution * (voxelIndex + 1);
		}
		
		@Override
		public int getVoxelIndex(double location)
		{
			if ( location < 0.0 || location >= this._length )
			{
				throw new IllegalArgumentException("Voxel index out of range");
			}
			return (int) (location / this._resolution);
		}
	}
	
	public abstract class VariableRes extends ResCalc
	{
		/**
		 * An array of voxel resolutions, one for each _nVoxel.
		 */
		protected double[] _resolution;
		/**
		 * The sum of all resolutions up to the focal voxel. Pre-calculated for
		 * speed.
		 */
		protected double[] _cumulativeRes;
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution[voxelIndex];
		}
		
		@Override
		public double getCumulativeResolution(int voxelIndex)
		{
			if ( this._cumulativeRes == null )
			{
				/* If this hasn't been calculated yet, do it now. */
				this._cumulativeRes = Vector.copy(this._resolution);
				for ( int i = 1; i < this._nVoxel; i++ )
					this._cumulativeRes[i] += this._cumulativeRes[i-1];
				this._length = this._cumulativeRes[this._nVoxel - 1];
			}
			return this._cumulativeRes[voxelIndex];
		}
		
		@Override
		public int getVoxelIndex(double location)
		{
			if ( location < 0.0 || location >= this._length )
			{
				throw new IllegalArgumentException("Location out of range");
			}
			int out = 0;
			while ( location > this._cumulativeRes[out] )
				out++;
			return out;
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
			this._length = getCumulativeResolution(this._nVoxel - 1);
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
			this._length = getCumulativeResolution(this._nVoxel - 1);
		}
	}
	
	public class VaryingResolution extends VariableRes
	{
		@Override
		void init(double targetResolution, double totalLength)
		{
			// TODO
		}
		
	}
}