package grid.resolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.DoubleFunction;

import grid.PolarGrid;
import grid.resolution.ResolutionCalculator.ResCalc;
import grid.resolution.ResolutionCalculator.UniformResolution;
import shape.ShapeConventions.DimName;

/**
 * \brief TODO
 * 
 * @author Stefan Lang, TODO
 */
public class ResCalcFactory
{
	
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
		for ( int dim = 0; dim < 3; dim++ )
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
		for (int dim = 0; dim < 3; ++dim)
		{
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
	
	/**
	 * \brief TODO
	 * 
	 * @param dim
	 * @param dimArgs
	 * @param totalLength
	 * @param res
	 * @param resClass
	 * @param resCalcClass
	 * @return
	 */
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
				for ( int shell = 0; shell < nr; shell++ )
				{
					int np = rC_phi[shell].getNVoxel();
					rFun_twoDim[shell] = new ResCalc[np];
					for ( int ring = 0; ring < np; ring++ )
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
	
	/**
	 * \brief TODO
	 * 
	 * Presumably this is for polar grids...?
	 * 
	 * @param res
	 * @param shell
	 * @return
	 */
	private static Object manipulateResolutionObject(Object res, int shell)
	{
		if ( res instanceof DoubleFunction )
		{
			//TODO safety
			DoubleFunction<Double> r = (DoubleFunction<Double>) res;
			DoubleFunction<Double> fun = x -> 
							PolarGrid.getTargetResolution(shell, r.apply(x));
			return fun;
		}
		else if ( res instanceof double[] )
		{
			double[] r = (double[]) res;
			for ( int i = 0; i < r.length; i++ )
				r[i] = PolarGrid.getTargetResolution(shell, r[i]);
			return r;
		}
		else
		{
			/* double */
			double r = (double) res;
			return PolarGrid.getTargetResolution(shell, r);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * Presumably this is for polar grids...?
	 * 
	 * @param res
	 * @param shell
	 * @param ring
	 * @return
	 */
	private static Object manipulateResolutionObject(
											Object res, int shell, int ring)
	{
		if ( res instanceof DoubleFunction )
		{
			//TODO safety
			DoubleFunction<Double> r = (DoubleFunction<Double>) res;
			DoubleFunction<Double> fun = x -> 
						PolarGrid.getTargetResolution(shell, ring, r.apply(x));
			return fun;
		}
		else if (res instanceof double[])
		{
			double[] r = (double[]) res;
			for ( int i = 0; i < r.length; i++ )
				r[i] = PolarGrid.getTargetResolution(shell, ring, r[i]);
			return r;
		}
		else
		{
			/* double */
			double r = (double) res;
			return PolarGrid.getTargetResolution(shell, ring, r);
		}
	}
}