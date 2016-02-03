/**
 * 
 */
package grid;

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
		 * \brief calculates the sum of all resolutions until 
		 * and including the resolution at voxelIndex.
		 * 
		 * @param voxelIndex
		 * @return
		 */
		public abstract double getCumResSum(int voxelIndex);
	}
	
	public abstract class SameRes extends ResCalc
	{
		protected double _resolution, _cumResSum[];
		
		protected void init(){
			this._cumResSum = new double[_nVoxel];
			for (int i=0; i<_nVoxel; ++i)
				this._cumResSum[i] = _resolution * (i+1);
			this._length = _cumResSum[_nVoxel - 1];
		}
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution;
		}
		
		@Override
		public double getCumResSum(int voxelIndex)
		{
			if (voxelIndex < 0) return 0;
			if (voxelIndex >= _nVoxel) return Double.NaN;
			return _cumResSum[voxelIndex];
		}
	}
	
	public abstract class VariableRes extends ResCalc
	{
		protected double _resolution[],  _cumResSum[];
		
		@Override
		public double getResolution(int voxelIndex)
		{
			return this._resolution[voxelIndex];
		}
		
		@Override
		public double getCumResSum(int voxelIndex)
		{
			return this._cumResSum[voxelIndex];
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
			super.init();
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
			super.init();
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