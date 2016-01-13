package linearAlgebra;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

public final class PolarArray {
	public static double[][][] createCylinder(int nr, double ires, int nz){
		double[][][] a = new double[nr][0][0];
		for (int i=0; i<nr; ++i){
			a[i] = new double[(int)(ires*(2*i+1))][nz];
		}
		return a;
	}
	
	public static double[][][] createCylinder(int nr, int nz, double ires, double val){
		return applyToAll(createCylinder(nr, ires, nz),()->{return val;});
	}
	
	public static double[][][] createSphere(int nr, double iresT, double iresP){
		double[][][] a = new double[nr][0][0];
		for (int i=0; i<nr; ++i){
			int ntp=2*(i+1)+1;
			a[i] = new double[(int)(iresT*ntp)][0];
			for (int j=0; j<a[i].length; ++j){
				// rectangular for iresP > 2, else triangular
				a[i][j] = new double[(int)(j+1+(iresP-1)*(ntp-j))];		
			}
		}
		return a;
	}
	
	public static double[][][] createSphere(int nr, double iresT, double iresP, double val){
		return applyToAll(createSphere(nr, iresT, iresP),()->{return val;});
	}
	
	public static double[][][] applyToAll(double[][][] array, DoubleSupplier f){
//		int iresT=4, iresP=2;
		for (int i=0; i<array.length; ++i){
			double[][] b=array[i];
			for (int j=0; j<b.length;++j){
				double[] c=b[j];
				for (int k=0; k<c.length;++k) {
					c[k]=f.getAsDouble();
//					int idx = (int)(1.0/6*i*(i+1)*(2*i+1)*8 + (k+(j%(i+1))*(j%(i+1))+(i+1)*(i+1)*(j/(i+1))))+1;
////					int idx = (int)(j%(i+1)*j%(i+1)+k+(i+1)*j+4.0/3*i*(i+1)*(2*i+1))+1;
//					int r=(int) Math.ceil(Math.pow(idx/8.0,1.0/2))-1;
//					double idx_tp = idx-1.0/6*(r-1)*r*(2*r-1)*8;
//					if(r>1) r=(int) Math.ceil(Math.pow(idx_tp/8,1.0/2))-1;
//					double idx_tp2 = idx-1.0/6*r*(r+1)*(2*r+1)*8;
//					int t=(int) (Math.ceil(Math.pow((idx_tp2-1)%((r+1)*(r+1))+1,1.0/2))+(r+1)*(Math.ceil((idx_tp2)/((r+1)*(r+1)))-1))-1;
//					int p=(int) (((idx_tp2-1)%((r+1)*(r+1))+1) - Math.pow(j%(r+1),2))-1;
////					int idx_tp=(int)((idx-(1/6*r*(r+1)*(2*r+1)*8))%((r+1)*(r+1)));
////					int t = (int) (Math.ceil(Math.pow(idx_tp,1.0/2))-1);
////					int p = (int) (idx_tp - 2*Math.pow(t,2))-1;
//					System.out.println(idx);
//					System.out.println("r: "+r+",t: "+t+",p: "+p);
//					System.out.println();
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
	
//	public static double computeIRES(int nr, double nt, double res){
//		 // min 1, +1 for each quadrant (=4 for full circle)
////		return nt*res / ((2*nr-res)*Math.max((int)(2*nt/Math.PI), 1));
////		return nt / ((2*nr-1)*Math.max((int)(2*nt/Math.PI), 1));
//		return Math.max(2*nt/Math.PI,1);
//	}
	
	public static double computeIRES(int nr, double nt){
		 // min 1, +1 for each quadrant (=4 for full circle)
		return Math.max(2*nt/Math.PI,1);
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
