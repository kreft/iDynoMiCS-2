package linearAlgebra;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 *
 * Utility class for PolarGrids
 */
public final class PolarArray {
	
	/**
	 * Used to create an array to store a CylindricalGrid
	 * 
	 * @param resCalc
	 * @return - An array used to store a CylindricalGrid
	 */
	public static double[][][] createCylinder(ResCalc[][] resCalc){
		int nr, nt, nz;
		nr = resCalc[0][0].getNVoxel();
		nz = resCalc[2][0].getNVoxel();
		
		double[][][] a = new double[nr][][];
		for (int i=0; i<nr; ++i){
			nt = resCalc[1][i].getNVoxel();
//			System.out.println(nt);
			a[i] = new double[nt][nz];
		}
		return a;
	}
	
	/**
	 * Used to create an array to store a CylindricalGrid
	 * 
	 * @param resCalc
	 * @param val - initial value
	 * @return - An array used to store a CylindricalGrid
	 */
	public static double[][][] createCylinder(
			ResCalc[][] resCalc, double val){
		return applyToAll(createCylinder(resCalc),()->{return val;});
	}
	
	/**
	 * Used to create an array to store a SphericalGrid
	 * 
	 * @param nr - number of voxels in r direction
	 * @param nt - number of voxels in t direction for each r
	 * @param np - number of voxels in p direction for each r and each t
	 * @return - An array used to store a SphericalGrid
	 */
	public static double[][][] createSphere(ResCalc[][][] resCalc){
		int nr, nt, np;
		nr = resCalc[0][0][0].getNVoxel();
		
		double[][][] a = new double[nr][][];
		for (int r=0; r<nr; ++r){
//			System.out.println(r);
			np = resCalc[1][r][0].getNVoxel();
			a[r] = new double[np][];
			for (int p=0; p<np; ++p){
//				System.out.println(np[r][t]);
				nt = resCalc[2][r][p].getNVoxel();
//				System.out.println(nt);
				a[r][p] = new double[nt];		
			}
//			System.out.println();
		}
		return a;
	}
	
	/**
	 * Used to create an array to store a SphericalGrid
	 * 
	 * @param nr - number of voxels in r direction
	 * @param nt - number of voxels in t direction for each r
	 * @param np - number of voxels in p direction for each r and each t
	 * @param val - initial value
	 * @return - An array used to store a SphericalGrid
	 */
	public static double[][][] createSphere(ResCalc[][][] resCalc, double val){
		return applyToAll(createSphere(resCalc),()->{return val;});
	}
	
	/**
	 * Used to set all grid cells to a value supplied by f
	 * 
	 * @param array - a PolarArray
	 * @param f - function supplying a double value
	 * @return - the array with all grid cells set to the value supplied by f
	 */
	public static double[][][] applyToAll(double[][][] array, DoubleSupplier f){
		for (int i=0; i<array.length; ++i){
			double[][] b=array[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					c[k]=f.getAsDouble();
				}
				b[j]=c;
			}
			array[i] = b;
		}
		return array;
	}
	
	/**
	 * Used to read all values in the array.
	 * 
	 * @param array - a PolarArray
	 * @param f - a function consuming a double value
	 */
	public static void applyToAll(double[][][] array, DoubleConsumer f){
		for (int i=0; i<array.length; ++i){
			double[][] b=array[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					f.accept(c[k]);
				}
			}
		}
	}
	
	/**
	 * Used to manipulate all values in the array.
	 * 
	 * @param array - a PolarArray
	 * @param f - a function manipulating a double value
	 * @return - the manipulated array
	 */
	public static double[][][] applyToAll(
			double[][][] array, DoubleFunction<Double> f){
		for (int i=0; i<array.length; ++i){
			double[][] b=array[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					c[k]=f.apply(c[k]);
				}
				b[j]=c;
			}
			array[i] = b;
		}
		return array;
	}
	
	/**
	 * Used to perform a binary operation between each grid cell 
	 * of the both arrays. Writes the result into a1.
	 * 
	 * @param a1 - a PolarArray
	 * @param a2 - a PolarArray
	 * @param f - a function providing the binary operation
	 * @return - array a1 with the results of the operation f
	 */
	public static double[][][] applyToAll(
			double[][][] a1, double[][][] a2, DoubleBinaryOperator f){
		checkDimensionsSame(a1, a2);
		for (int i=0; i<a1.length; ++i){
			double[][] b1=a1[i];
			double[][] b2=a2[i];
			for (int j=0; j<b1.length;++j){
				double[] c1=b1[j];
				double[] c2=b2[j];
				for (int k=0; k<c1.length;++k) {
					c1[k]=f.applyAsDouble(c1[k], c2[k]);
				}
				b1[j]=c1;
			}
			a1[i] = b1;
		}
		return a1;
	}
	
	/**
	 * Computes the inner resolution for phi or theta dimensions.
	 * 
	 * @param n - total size in theta or phi direction (in radian)
	 * @return - the inner resolution
	 */
	public static double ires(double n){
		 // min 1, +1 for each quadrant (=4 for full circle)
//		return Math.max(2*nt/(Math.PI*res),1);
//		System.out.println(2*nt/Math.PI);
		return 2*n/Math.PI;
	}
	
	/**
	 * @param a - a polar array
	 * @param b - another polar array
	 * @throws IllegalArgumentException if dimensions are not the same
	 */
	public static void checkDimensionsSame(double[][][] a, double[][][] b) 
			throws IllegalArgumentException
	{
		IllegalArgumentException e = 
				new IllegalArgumentException("Array dimensions must agree.");
		if (a.length!=b.length) throw e;
		if (a[0][0].length!=b[0][0].length) throw e;
		for (int i=0; i<a.length; ++i){
			if (a[i].length!=b[i].length) throw e;
		}
	}
	
	/**
	 * @param a - a polar array.
	 * @return - the maximum value in array a.
	 */
	public static double max(double[][][] a){
		double max=Double.NEGATIVE_INFINITY;
		for (int i=0; i<a.length; ++i){
			double[][] b=a[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					max = c[k]>max ? c[k] : max;
				}
			}
		}
		return max;
	}
	
	public static double area(
			double r, double theta0, double theta1, double phi0, double phi1){
		return r * r * (theta1 - theta0) * (Math.cos(phi0)-Math.cos(phi1));
	}
	
	public static double arcLength(double r, double theta0, double theta1){
		return r * (theta1 - theta0);
	}
}
