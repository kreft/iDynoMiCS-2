/**
 * 
 */
package shape.resolution;

import generalInterfaces.Copyable;
import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * \brief Collection of methods for calculating appropriate grid resolutions. 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ResolutionCalculator 
{
	public static abstract class ResCalc implements Copyable
	{
		/**
		 * Total number of voxels along this dimension.
		 */
		protected int _nVoxel;
		/**
		 * Total extremes of this dimension.
		 */
		protected double _min, _max;

		// TODO void init(Node xmlNode);
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
			return _max - _min;
		}
		
		public abstract void setResolution(double res);

		public abstract double getMinResolution();

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
			out += this.getResolution(voxelIndex) * inside;
			return out;
		}
		
		/**
		 * \brief Calculates which voxel the given location lies inside.
		 * 
		 * @param location Continuous location along this axis.
		 * @return Index of the voxel this location is inside.
		 * @throws IllegalArgumentException if location is outside [0, length)
		 */
		public abstract int getVoxelIndex(double location);
		
		public Object copy()
		{
			ResCalc out = null;
			try
			{
				out = this.getClass().newInstance();
				out._nVoxel = this._nVoxel;
				out._min = this._min;
				out._max = this._max;
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
			return out;
		}
	}

	public static abstract class SameRes extends ResCalc
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
		public double getMinResolution()
		{
			return this._resolution;
		}

		@Override
		public double getCumulativeResolution(int voxelIndex)
		{
			if ( voxelIndex >= this._nVoxel )
				throw new IllegalArgumentException("Voxel index out of range");
			if ( voxelIndex < 0 )
				return this._min;
			return this._min + this._resolution * (voxelIndex + 1);
		}

		@Override
		public int getVoxelIndex(double location)
		{
			if ( location < this._min || location >= this._max )
				throw new IllegalArgumentException("Location out of range");
			return (int) ((location - this._min) / this._resolution);
		}
		
		public Object copy()
		{
			SameRes out = (SameRes) super.copy();
			out._resolution = this._resolution;
			return out;
		}
	}

	@Deprecated
	public static abstract class VariableRes extends ResCalc
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
		public double getMinResolution()
		{
			return Vector.min(this._resolution);
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
			}
			return this._cumulativeRes[voxelIndex];
		}
		
		@Override
		public int getVoxelIndex(double location)
		{
			if ( location < this._min || location >= this._max )
				throw new IllegalArgumentException("Location out of range");
			int out = 0;
			while ( location > this.getCumulativeResolution(out) )
				out++;
			return out;
		}
		
		public Object copy()
		{
			VariableRes out = (VariableRes) super.copy();
			out._resolution = this._resolution;
			// NOTE Probably shouldn't copy the cumulative resolution, as the
			// resolution itself may change and so this would need to be
			// re-calculated
			//out._cumulativeRes = this._cumulativeRes;
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
	public static class UniformResolution extends SameRes
	{
		protected double _targetRes;
		
		@Override
		public void init(double targetResolution, double min, double max)
		{
			this._min = min;
			this._max = max;
			this._targetRes = targetResolution;
			this._nVoxel = (int) (getTotalLength() / targetResolution);
			this._resolution = getTotalLength() / this._nVoxel;
			double altRes = getTotalLength() / (this._nVoxel + 1);
			if ( isAltResBetter(
					this._resolution, altRes, targetResolution) )
			{
				this._nVoxel++;
				this._resolution = altRes;
			}
		}
		
		@Override
		public void setResolution(double targetResolution)
		{
			this.init(targetResolution, this._min, this._max);
		}
		
		public Object copy()
		{
			UniformResolution out = (UniformResolution) super.copy();
			out._targetRes = this._targetRes;
			return out;
		}
	}

	/**
	 * \brief A distribution of resolutions that guarantees there will be
	 * <i>2<sup>n</sup> + 1</i> voxels, where <i>n</i> is a natural number.
	 */
	public static class MultiGrid extends SameRes
	{
		protected double _targetRes;
		
		@Override
		public void init(double res, double min, double max)
		{
			this._min = min; 
			this._max = max;
			this._targetRes = res;
			/* Single-voxel test to start with. */
			this._nVoxel = 1;
			this._resolution = res;
			/* Variables to test splitting the grid into more voxels. */
			int exponent = 0;
			int altNVoxel = 2;
			double altRes = getTotalLength() / altNVoxel;
			/* Testing loop. */
			while( isAltResBetter(
					this._resolution, altRes, res) )
			{
				this._nVoxel = altNVoxel;
				exponent++;
				altNVoxel = ExtraMath.exp2(exponent) + 1;
				this._resolution = altRes;
				altRes = getTotalLength() / altNVoxel;
			}
		}
		
		@Override
		public void setResolution(double targetResolution)
		{
			this.init(targetResolution, this._min, this._max);
		}
		
		public Object copy()
		{
			UniformResolution out = (UniformResolution) super.copy();
			out._targetRes = this._targetRes;
			return out;
		}
	}
}