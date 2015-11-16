package linearAlgebra;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

public final class PolarArray {
	public static double[][][] create(int nr, double ires, int nz){
		double[][][] a = new double[nr][0][0];
		for (int i=0; i<nr; ++i){
			a[i] = new double[(int)(ires*(2*(i+1)-1))][nz];
		}
		return a;
	}
	
	public static double[][][] create(int nr, double nt, int nz, double ires, double val){
		return applyToAll(create(nr, ires, nz),()->{return val;});
	}
	
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
	
	public static double[][][] applyToAll(double[][][] array, DoubleConsumer f){
		for (int i=0; i<array.length; ++i){
			double[][] b=array[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					f.accept(c[k]);
				}
				b[j]=c;
			}
			array[i] = b;
		}
		return array;
	}
	
	public static double[][][] applyToAll(double[][][] array, DoubleFunction<Double> f){
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
	
	public static double[][][] applyToAll(double[][][] a1, double[][][] a2, DoubleBinaryOperator f){
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
	
	public static int computeIRES(int nr, double nt, double res){
		 // min 1, +1 for each quadrant (=4 for full circle)
//		return nt*res / ((2*nr-res)*Math.max((int)(2*nt/Math.PI), 1));
//		return nt / ((2*nr-1)*Math.max((int)(2*nt/Math.PI), 1));
		return Math.max((int)(2*nt/Math.PI),1);
	}
	
	public static void checkDimensionsSame(double[][][] a, double[][][] b) throws IllegalArgumentException
	{
		IllegalArgumentException e = new IllegalArgumentException("Array dimensions must agree.");
		if (a.length!=b.length) throw e;
		if (a[0][0].length!=b[0][0].length) throw e;
		for (int i=0; i<a.length; ++i){
			if (a[i].length!=b[i].length) throw e;
		}
	}
	
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
}
