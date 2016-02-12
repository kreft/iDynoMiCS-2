/**
 * 
 */
package grid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.DoubleFunction;

import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

/**
 * \brief Collection of methods for calculating appropriate grid resolutions. 
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class ResolutionCalculator
{
	public static abstract class ResCalc
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

		//		abstract void init(Object targetResolution, double totalLength);

		public int getNVoxel()
		{
			return this._nVoxel;
		}

		public double getTotalLength()
		{
			return _length;
		}

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
		 * \brief Calculates which voxel the given location lies inside.
		 * 
		 * @param location Continuous location along this axis.
		 * @return Index of the voxel this location is inside.
		 * @throws IllegalArgumentException if location is outside [0, length)
		 */
		public abstract int getVoxelIndex(double location);
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
	public static class UniformResolution extends SameRes
	{
		public void init(double targetResolution, double totalLength)
		{
			this._nVoxel = (int) (totalLength / targetResolution);
			this._resolution = totalLength / this._nVoxel;
			double altRes = totalLength / (this._nVoxel + 1);
			if ( isAltResBetter(
					this._resolution, altRes, targetResolution) )
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
	public static class MultiGrid extends SameRes
	{
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
			while( isAltResBetter(
					this._resolution, altRes, targetResolution) )
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

	public static class SimpleVaryingResolution extends VariableRes
	{
		public void init(double[] targetResolution,	double totalLength) {
			this._length = 0;
			this._resolution = targetResolution;
			this._nVoxel = targetResolution.length;
			this._length = getCumulativeResolution(this._nVoxel - 1);
			
			double diff_per_voxel = (this._length - totalLength) / this._nVoxel;
			Vector.addTo(this._resolution, this._resolution, diff_per_voxel);			
		}
	}
	
	public static class ResolutionFunction extends VariableRes
	{
		public void init(DoubleFunction<Double> targetResolution, double totalLength) {
			double length = 0;
			ArrayList<Double> res = new ArrayList<>();
			while (length < totalLength){
				double r =  targetResolution.apply(length / totalLength);
				res.add(r);
				length += r;
				this._nVoxel++;
			}
			double diff_per_voxel = (totalLength - length) / _nVoxel;
			_resolution = new double[_nVoxel];
			for (int i = 0; i<_nVoxel; ++i)
				_resolution[i] = res.get(i) - diff_per_voxel;
			this._length = getCumulativeResolution(this._nVoxel - 1);
		}
	}

	/**************************************************************************/
	/****************** RESOLUTION CALCULATOR FACTORY *************************/
	/**************************************************************************/

	public static class ResCalcFactory {
		
		/**********************************************************************/
		/*********************** STANDARD CREATOR  ****************************/
		/**********************************************************************/
		
		/* Uniform resolution in all dimensions */
		
		public static ResCalc[] createUniformResCalcForCube(
									double[] totalLength, double resolution){
			return createResCalcForCube(
					totalLength,
					resolution,
					double.class,
					UniformResolution.class);
		}
		
		public static ResCalc[][] createUniformResCalcForCylinder(
				double[] totalLength, double resolution){
			return createResCalcForCylinder(
					totalLength,
					resolution,
					double.class,
					UniformResolution.class);
		}
		
		public static ResCalc[][][] createUniformResCalcForSphere(
				double[] totalLength, double resolution){
			return createResCalcForSphere(
					totalLength,
					resolution,
					double.class,
					UniformResolution.class);
		}
		
		/**********************************************************************/
		/**************************** SECOND LEVEL ****************************/
		/**********************************************************************/
		
		/* generic for resolution calculator, 
		 * 	but all of the same class in the three dimensions */
		
		public static ResCalc[] createResCalcForCube(
				double[] totalLength,
				Object res, 
				Class<?> resClass,
				Class<?> resCalcClass)
		{
			 return createResCalcForCube(
					 totalLength, 
					 new Object[]{res, res, res},
					 new Class[]{resCalcClass, resCalcClass, resCalcClass},
					 new Class[]{resClass, resClass, resClass});
		}
		
		public static ResCalc[][] createResCalcForCylinder(
				double[] totalLength,
				Object res, 
				Class<?> resClass,
				Class<?> resCalcClass)
		{
			 return createResCalcForCylinder(
					 totalLength, 
					 new Object[]{res, res, res},
					 new Class[]{resCalcClass, resCalcClass, resCalcClass},
					 new Class[]{resClass, resClass, resClass});
		}
		
		public static ResCalc[][][] createResCalcForSphere(
				double[] totalLength,
				Object res, 
				Class<?> resClass,
				Class<?> resCalcClass)
		{
			 return createResCalcForSphere(
					 totalLength, 
					 new Object[]{res, res, res},
					 new Class[]{resCalcClass, resCalcClass, resCalcClass},
					 new Class[]{resClass, resClass, resClass});
		}
		
		/**********************************************************************/
		/***************************** THIRD LEVEL ****************************/
		/**********************************************************************/
		
		/* generic for resolution calculator */
		
		public static ResCalc[] createResCalcForCube(
				double[] totalLength,
				Object[] res, 
				Class<?>[] resClasses, 
				Class<?>[] resCalcClasses)
		{
			ResCalc[] out = new ResCalc[3];
			DimName[] dims = new DimName[]{DimName.X,DimName.Y,DimName.Z};
			for (int dim=0; dim<3; ++dim)
				out[dim] = (ResCalc) createResCalcForDimension(
						dims[dim],
						null,
						totalLength[dim],
						res[dim], 
						resCalcClasses[dim],
						resClasses[dim]);
			
			return out;
		}
		
		public static ResCalc[][] createResCalcForCylinder(
				double[] totalLength,
				Object[] res, 
				Class<?>[] resClasses, 
				Class<?>[] resCalcClasses)
		{
			ResCalc[][] out = new ResCalc[3][];
			out[0] = new ResCalc[1];
			out[2] = new ResCalc[1];
			
			DimName[] dims = new DimName[]{DimName.R,DimName.THETA,DimName.Z};
			
			Object[] resCalc = createResCalcForDimensions(dims,
					totalLength, res, resClasses, resCalcClasses);

			out[0][0] = (ResCalc) resCalc[0];
			out[1] = (ResCalc[]) resCalc[1];
			out[2][0] = (ResCalc) resCalc[2];
			
			return out;
		}
		
		public static ResCalc[][][] createResCalcForSphere(
				double[] totalLength,
				Object[] res, 
				Class<?>[] resClasses, 
				Class<?>[] resCalcClasses)
		{
			ResCalc[][][] out = new ResCalc[3][][];
			out[0] = new ResCalc[1][1];
			out[1] = new ResCalc[1][];
			
			DimName[] dims = new DimName[]{DimName.R,DimName.PHI,DimName.THETA};
			Object[] resCalc = createResCalcForDimensions(dims,
					totalLength, res, resClasses, resCalcClasses);

			out[0][0][0] = (ResCalc) resCalc[0];
			out[1][0] = (ResCalc[]) resCalc[1];
			out[2] = (ResCalc[][]) resCalc[2];
			
			return out;
		}
		
		/**********************************************************************/
		/************************* DIMENSION LEVEL ****************************/
		/**********************************************************************/
		
		/* generic for all currently implemented grids */
		
		public static Object[] createResCalcForDimensions(
				DimName[] dims,
				double[] totalLength,
				Object[] res, 
				Class<?>[] resClasses, 
				Class<?>[] resCalcClasses)
		{
			Object[] out = new Object[3];
			ArrayList<Object> dimArgs = new ArrayList<>();
			for (int dim = 0; dim < 3; ++dim){
				/* add the last resCalc to arguments for polar dimensions */
				if (dims[dim]==DimName.THETA || dims[dim]==DimName.PHI)
					dimArgs.add(out[dim-1]);
				
				out[dim] = createResCalcForDimension(
						dims[dim],
						dimArgs,
						dim < totalLength.length ?  totalLength[dim] : 1,
						res[dim], 
						resCalcClasses[dim],
						resClasses[dim]);
				
			}
			return out;
		}
		
		private static Object createResCalcForDimension( 
				DimName dim,
				ArrayList<Object> dimArgs,
				double totalLength,
				Object res, 
				Class<?> resClass, 
				Class<?> resCalcClass) {

			try {
				/* get new ResCalc instance and right initializing method */
				
				Object rFun_singleVal = resCalcClass.newInstance(); 
				Method init = resCalcClass.getMethod(
						"init", resClass, double.class);

				switch (dim){
				case R: case Z: case X: case Y:
					init.invoke(rFun_singleVal, res, totalLength);
					return rFun_singleVal;
				case THETA: case PHI:
					boolean is_for_cylinder = dimArgs.size() == 1;
					/* init r-dependent dimension 
					 * this is theta for the cylinder and phi for the sphere.
					 */
					int nr = ((ResCalc) dimArgs.get(0)).getNVoxel();
					
					Object[] rFun_oneDim = new ResCalc[nr];
					for ( int shell = 0; shell < nr; shell++ )
					{
						rFun_singleVal = resCalcClass.newInstance();
						Object scaled_res = manipulateResolutionObject(res, shell);
						init.invoke(rFun_singleVal, scaled_res, totalLength);
						rFun_oneDim[shell] = rFun_singleVal;
					}

					if (is_for_cylinder || dim==DimName.PHI)
						return rFun_oneDim;

					Object[][] rFun_twoDim = new ResCalc[nr][];
					ResCalc[] rC_phi = ((ResCalc[]) dimArgs.get(1));
					for (int shell=0; shell<nr; ++shell){
						int np = rC_phi[shell].getNVoxel();
						rFun_twoDim[shell] = new ResCalc[np];
						for ( int ring = 0; ring < np; ++ring )
						{
							rFun_singleVal = resCalcClass.newInstance(); 
							Object scaled_res = manipulateResolutionObject(res, shell, ring);
							init.invoke(rFun_singleVal, scaled_res, totalLength);
							rFun_twoDim[shell][ring] = (ResCalc)rFun_singleVal;
						}
					}
					return rFun_twoDim;
				}
			} catch (NoSuchMethodException | SecurityException 
					| IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException | InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		private static Object manipulateResolutionObject(Object res, int shell){
			if (res instanceof DoubleFunction){
				//TODO safety
				DoubleFunction<Double> r = (DoubleFunction<Double>)res;
				DoubleFunction<Double> fun = 
						x -> PolarGrid.getTargetResolution(shell, r.apply(x));
				return fun;
			}
			else if (res instanceof double[]){
				double[] r = (double[]) res;
				for (int i=0; i<r.length; ++i){
					r[i] = PolarGrid.getTargetResolution(shell, r[i]);
				}
				return r;
			}else { //double
				double r = (double) res;
				return PolarGrid.getTargetResolution(shell, r);
			}
		}
		
		private static Object manipulateResolutionObject(Object res, int shell, int ring){
			if (res instanceof DoubleFunction){
				//TODO safety
				DoubleFunction<Double> r = (DoubleFunction<Double>)res;
				DoubleFunction<Double> fun = 
						x -> PolarGrid.getTargetResolution(
													shell, ring, r.apply(x));
				return fun;
			}
			else if (res instanceof double[]){
				double[] r = (double[]) res;
				for (int i=0; i<r.length; ++i){
					r[i] = PolarGrid.getTargetResolution(shell, ring, r[i]);
				}
				return r;
			}else { //double
				double r = (double) res;
				return PolarGrid.getTargetResolution(shell, ring, r);
			}
		}
	}
}