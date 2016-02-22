package grid.resolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.DoubleFunction;

import generalInterfaces.XMLable;
import grid.PolarGrid;
import grid.resolution.ResolutionCalculator.ResCalc;
import grid.resolution.ResolutionCalculator.UniformResolution;
import shape.ShapeConventions.DimName;

/**
 * @author Stefan Lang
 * 
 * \brief A factory class for different resolution calculators and different 
 * spatial grids.
 *
 */
public final class ResCalcFactory implements XMLable{
	
	/**********************************************************************/
	/*********************** STANDARD CREATOR  ****************************/
	/**********************************************************************/

	/* Uniform resolution in all dimensions */

	/**
	 * \brief Creates a uniformly distributed resolution calculator array 
	 * valid for a Cube. 
	 * 
	 * @param totalLength
	 * @param resolution
	 * @return
	 */
	public static ResCalc[] createUniformResCalcForCube(
			double[] totalLength, double resolution){
		return createResCalcForCube(
				totalLength,
				resolution,
				UniformResolution.class);
	}

	/**
	 * \brief Creates a uniformly distributed resolution calculator array 
	 * valid for a Cylinder. 
	 * 
	 * @param totalLength
	 * @param resolution
	 * @return
	 */
	public static ResCalc[][] createUniformResCalcForCylinder(
			double[] totalLength, double resolution){
		return createResCalcForCylinder(
				totalLength,
				resolution,
				UniformResolution.class);
	}

	/**
	 * \brief Creates a uniformly distributed resolution calculator array 
	 * valid for a Sphere. 
	 * 
	 * @param totalLength
	 * @param resolution
	 * @return
	 */
	public static ResCalc[][][] createUniformResCalcForSphere(
			double[] totalLength, double resolution){
		return createResCalcForSphere(
				totalLength,
				resolution,
				UniformResolution.class);
	}

	/**********************************************************************/
	/**************************** SECOND LEVEL ****************************/
	/**********************************************************************/

	/* generic for resolution calculator, 
	 * 	but all of the same class in the three dimensions */

	//TODO: determine resCalc class automatically if null argument.

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object used for all dimension.
	 * @param resCalcClasses The desired ResCalc class to use for all dimensions.
	 * @return A resolution calculator array valid for a Cube.
	 */
	public static ResCalc[] createResCalcForCube(
			double[] totalLength,
			Object res,
			Class<?> resCalcClass)
	{
		return createResCalcForCube(
				totalLength, 
				new Object[]{res, res, res},
				new Class[]{resCalcClass, resCalcClass, resCalcClass});
	}

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object used for all dimension.
	 * @param resCalcClasses The desired ResCalc class to use for all dimensions.
	 * @return A resolution calculator array valid for a Cylinder.
	 */
	public static ResCalc[][] createResCalcForCylinder(
			double[] totalLength,
			Object res,
			Class<?> resCalcClass)
	{
		return createResCalcForCylinder(
				totalLength, 
				new Object[]{res, res, res},
				new Class[]{resCalcClass, resCalcClass, resCalcClass}
				);
	}

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object used for all dimension.
	 * @param resCalcClasses The desired ResCalc class to use for all dimensions.
	 * @return A resolution calculator array valid for a Sphere.
	 */
	public static ResCalc[][][] createResCalcForSphere(
			double[] totalLength,
			Object res,
			Class<?> resCalcClass)
	{
		return createResCalcForSphere(
				totalLength, 
				new Object[]{res, res, res},
				new Class[]{resCalcClass, resCalcClass, resCalcClass});
	}

	/**********************************************************************/
	/***************************** THIRD LEVEL ****************************/
	/**********************************************************************/

	/* generic for resolution calculator */

	//TODO: determine resCalc class automatically if null argument.

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object for each dimension.
	 * @param resCalcClasses The desired ResCalc class in each dimension.
	 * @return A resolution calculator array valid for a Cube.
	 */
	public static ResCalc[] createResCalcForCube(
			double[] totalLength,
			Object[] res, 
			Class<?>[] resCalcClasses)
	{
		/* define ResCalc array and names for the three dimensions */
		ResCalc[] out = new ResCalc[3];
		DimName[] dims = new DimName[]{DimName.X,DimName.Y,DimName.Z};

		/* create appropriate ResCalc Objects for dimension combinations*/
		Object[] resCalc = createResCalcForDimensions(dims,
				totalLength, res, resCalcClasses);

		/* cast to correct data type and update the array */
		for (int i=0; i<3; ++i)
			out[i] = (ResCalc) resCalc[i];

		return out;
	}

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object for each dimension.
	 * @param resCalcClasses The desired ResCalc class in each dimension.
	 * @return A resolution calculator array valid for a Cylinder.
	 */
	public static ResCalc[][] createResCalcForCylinder(
			double[] totalLength,
			Object[] res, 
			Class<?>[] resCalcClasses)
	{
		/* define ResCalc array */
		ResCalc[][] out = new ResCalc[3][];
		out[0] = new ResCalc[1];
		out[2] = new ResCalc[1];

		/* initialize names for the three dimensions */
		DimName[] dims = new DimName[]{DimName.R,DimName.THETA,DimName.Z};

		/* create appropriate ResCalc Objects for dimension combinations*/
		Object[] resCalc = createResCalcForDimensions(dims,
				totalLength, res, resCalcClasses);

		/* cast to correct data type and update the array */
		out[0][0] = (ResCalc) resCalc[0];
		out[1] = (ResCalc[]) resCalc[1];
		out[2][0] = (ResCalc) resCalc[2];

		return out;
	}

	/**
	 * \brief Creates a resolution calculator Array for a resolution object
	 * and any specified resolution calculator class.
	 * 
	 * The resolution Object has to be one of {@code double},
	 *  {@code double[]}, {@code DoubleFunction<Double>}
	 * 
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object for each dimension.
	 * @param resCalcClasses The desired ResCalc class in each dimension.
	 * @return A resolution calculator array valid for a Sphere.
	 */
	public static ResCalc[][][] createResCalcForSphere(
			double[] totalLength,
			Object[] res, 
			Class<?>[] resCalcClasses)
	{
		/* define ResCalc array */
		ResCalc[][][] out = new ResCalc[3][][];
		out[0] = new ResCalc[1][1];
		out[1] = new ResCalc[1][];

		/* initialize names for the three dimensions */
		DimName[] dims = new DimName[]{DimName.R,DimName.PHI,DimName.THETA};

		/* create appropriate ResCalc Objects for dimension combinations*/
		Object[] resCalc = createResCalcForDimensions(dims,
				totalLength, res, resCalcClasses);

		/* cast to correct data type and update the array */
		out[0][0][0] = (ResCalc) resCalc[0];
		out[1][0] = (ResCalc[]) resCalc[1];
		out[2] = (ResCalc[][]) resCalc[2];

		return out;
	}

	/**********************************************************************/
	/************************* DIMENSION LEVEL ****************************/
	/**********************************************************************/

	/* generic for all currently implemented grids */

	//TODO: determine resCalc class automatically if null argument.

	/**
	 * \brief Creates a resolution calculator object for a given array 
	 * of dimensions.
	 * 
	 * The result will be one of {@code ResCalc[]}, {@code ResCalc[][]} 
	 * or {@code ResCalc[][][]}, 
	 * depending on the dimensions themselves and their combinations. 
	 * 
	 * @param dims An array of dimensions.
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object for each dimension.
	 * @param resCalcClasses The desired ResCalc class in each dimension.
	 * @return
	 */
	public static Object[] createResCalcForDimensions(
			DimName[] dims,
			double[] totalLength,
			Object[] res, 
			Class<?>[] resCalcClasses)
	{
		Object[] out = new Object[3];
		ArrayList<Object> dimArgs = new ArrayList<>();
		for (int dim = 0; dim < 3; ++dim){
			/* 
			 * Add the last resCalc to arguments for polar dimensions. 
			 * This has to be done because in polar grids resolutions in one
			 * dimension depend on their preceding dimension(s).
			 */
			if (dims[dim]==DimName.THETA || dims[dim]==DimName.PHI)
				dimArgs.add(out[dim-1]);
			/* 
			 * create the appropriate ResCalc object for the current 
			 * dimension 
			 */ 
			out[dim] = createResCalcForDimension(
							dims[dim],
							dimArgs,
							(dim < totalLength.length ?  totalLength[dim] : 1),
							res[dim], 
							resCalcClasses[dim]);

		}
		return out;
	}

	/**
	 * \brief Creates a resolution calculator array for a given dimension 
	 * with given arguments. 
	 * 
	 * The result will be one of {@code ResCalc}, {@code ResCalc[]} or
	 * {@code ResCalc[][]} depending on the dimension and the number of 
	 * arguments for that dimension. The arguments need to be a resolution 
	 * calculator for shell-only-dependent dimensions (θ for the 
	 * cylinder and φ for the sphere) or a resolution calculator and a 
	 * resolution calculator array for shell- and ring- dependent dimensions 
	 * (θ in the sphere).  
	 * 
	 * 
	 * @param dim A dimension name.
	 * @param dimArgs The required resolution calculators for that dimension.
	 * @param totalLength The totalLength of the grid.
	 * @param res A valid resolution object.
	 * @param resCalcClass The desired ResCalc class.
	 * @return
	 */
	private static Object createResCalcForDimension( 
			DimName dim,
			ArrayList<Object> dimArgs,
			double totalLength,
			Object res,
			Class<?> resCalcClass) {

		try {
			/* get new ResCalc instance */
			Object rC = resCalcClass.newInstance(); 

			/* fetch class of resolution object */
			Class<?> resClass = res.getClass();

			/* cast Integer to double */
			if (resClass.equals(Integer.class)){
				res = Double.valueOf((Integer) res);
				resClass = double.class;
			}

			/* cast Double to double */
			if (resClass.equals(Double.class))
				resClass = double.class;

			/* 
			 * getClass() will not determine the functional interface,
			 * so do it manually here.
			 */
			if (res instanceof DoubleFunction )
				resClass = DoubleFunction.class;

			/* get suitable initializing method */
			Method init = resCalcClass.getMethod(
					"init", 
					resClass, 
					double.class);
			switch (dim){
			/* for cartesian(-like) dimensions just call init method */
			case R: case Z: case X: case Y:
				init.invoke(rC, res, totalLength);
				return rC;
				/* for polar dimensions we have to do some more stuff... */
			case THETA: case PHI:
				/* used to discriminate whether this is 
				 * THETA of cylinder (shell-only-dependent) or 
				 * PHI of sphere (shell-only-dependent) or
				 * THETA of sphere (shell and ring dependent).
				 * TODO: some safety would be more user-friendly I guess
				 */
				boolean is_for_cylinder = dimArgs.size() == 1;

				/* determine the number of shells we have from the dimArgs */
				int nShells = ((ResCalc) dimArgs.get(0)).getNVoxel();
				/* 
				 * init shell-only-dependent dimension 
				 * this is theta for the cylinder and phi for the sphere.
				 */
				Object[] rC_oneDim = new ResCalc[nShells];
				for ( int shell = 0; shell < nShells; shell++ )
				{
					rC = resCalcClass.newInstance();
					/* scale resolution for shell 
					 * (using the static method provided by PolarGrid)
					 */
					Object scaled_res = manipulateResolutionObject(res, shell);
					/* initialize ResCalc appropriately and save in array */
					init.invoke(rC, scaled_res, totalLength);
					rC_oneDim[shell] = rC;
				}

				/* 
				 * If this is to be computed for a shell-only-dependent 
				 * dimension we are done 
				 */
				if (is_for_cylinder || dim==DimName.PHI)
					return rC_oneDim;

				/* only needed for theta dimension of sphere */
				Object[][] rC_twoDim = new ResCalc[nShells][];
				/* get resolution calculator of phi dimension from dimArgs */
				ResCalc[] rC_phi = ((ResCalc[]) dimArgs.get(1));
				for (int shell=0; shell<nShells; ++shell){
					/* fetch number of rings */
					int nRings = rC_phi[shell].getNVoxel();
					/* set up ring */
					rC_twoDim[shell] = new ResCalc[nRings];
					for ( int ring = 0; ring < nRings; ++ring )
					{
						rC = resCalcClass.newInstance(); 
						/* scale resolution for shell and ring
						 * (using the static method provided by PolarGrid)
						 */
						Object scaled_res 
							= manipulateResolutionObject(res, shell, ring);
						init.invoke(rC, scaled_res, totalLength);
						/* initialize ResCalc appropriately and save in array */
						rC_twoDim[shell][ring] = (ResCalc)rC;
					}
				}
				return rC_twoDim;
			}
		} catch (InvocationTargetException e){
			/* lets only report the causing error here */
			e.getCause().printStackTrace();
		} catch (NoSuchMethodException | SecurityException 
				| IllegalAccessException | IllegalArgumentException
				| InstantiationException e) {
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
					x -> PolarGrid.scaleResolutionForShell(shell, r.apply(x));
					return fun;
		}
		else if (res instanceof double[]){
			double[] r = (double[]) res;
			for (int i=0; i<r.length; ++i){
				r[i] = PolarGrid.scaleResolutionForShell(shell, r[i]);
			}
			return r;
		}else if (res instanceof Double){ //double
			double r = (double) res;
			return PolarGrid.scaleResolutionForShell(shell, r);
		} 
		return null;
	}

	private static Object manipulateResolutionObject(Object res, int shell, int ring){
		if (res instanceof DoubleFunction){
			//TODO safety
			DoubleFunction<Double> r = (DoubleFunction<Double>)res;
			DoubleFunction<Double> fun = 
					x -> PolarGrid.scaleResolutionForRing(
							shell, ring, r.apply(x));
					return fun;
		}
		else if (res instanceof double[]){
			double[] r = (double[]) res;
			for (int i=0; i<r.length; ++i){
				r[i] = PolarGrid.scaleResolutionForRing(shell, ring, r[i]);
			}
			return r;
		}else if (res instanceof Double){ //double
			double r = (double) res;
			return PolarGrid.scaleResolutionForRing(shell, ring, r);
		}
		return null;
	}
}