package test.junit.newTests;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;
import debugTools.Testable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AcidBaseTest2 implements Testable {

	double q = 0.1;

	double p = 10;
	private double rhobeg = 0.25;
	private double rhoend = 1.0e-12;
	private int iprint = 1;
	private int maxfun = 1_000_000;
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}

	public void test(TestMode mode)
	{
		double k1 = 7.2E-4;
		double k2 = 6.2E-10;
		double k3 = 1.6E-10;
		double kw = 1.0E-14;

		double qf = q;
		double qcn = q;
		double qa = q;
		Calcfc calcfc = new Calcfc() {
			@Override
			public double Compute(int n, int m, double[] x, double[] con) {
				/*
				 *	0	1	2	3	4	5	6	7
				 *	h	oh	f	hf	cn	hcn	a	ha
				 */
				
				// equilibria
				con[0] = (( x[0] * x[2] ) / x[3] - k1);
				con[1] = (( x[0] * x[4] ) / x[5] - k2);
				con[2] = (( x[0] * x[6] ) / x[7] - k3);
				con[3] = (( x[0] * x[1] ) - kw);

				// mass balances
				con[4] = (x[2] + x[3] - qf);
				con[5] = (x[4] + x[5] - qcn);
				con[6] = (x[6] + x[7] - qa);

				// charge balance
				con[7] = (x[0]-x[1]-x[2]-x[4]-x[6]);

				// concentrations are positive
				con[8] = (x[0] < 0.0 ? -Math.pow(x[0],2): 0.0);
				con[9] = (x[1] < 0.0 ? -Math.pow(x[1],2): 0.0);
				con[10] = (x[2] < 0.0 ? -Math.pow(x[2],2): 0.0);
				con[11] = (x[3] < 0.0 ? -Math.pow(x[3],2): 0.0);
				con[12] = (x[4] < 0.0 ? -Math.pow(x[4],2): 0.0);
				con[13] = (x[5] < 0.0 ? -Math.pow(x[5],2): 0.0);
				con[14] = (x[6] < 0.0 ? -Math.pow(x[6],2): 0.0);
				con[15] = (x[7] < 0.0 ? -Math.pow(x[7],2): 0.0);

				// negative concentration stop
//				for(double s : x)
//					if( s < 0.0 )
//						con[8] += s;

				double out = 0;

				for (double r : con) {
					out += Math.abs(r);
				}

//				for (double r : con) {
//					out += Math.pow(r,2);
//				}
				return out;
//				return out/con.length;
			}
		};

		double[] x = {0.05, 0.05, qf, qf, qcn, qcn, qa, qa};

//		 double guess = 2;
//		 double[] x = {Math.pow(10,-(guess)), Math.pow(10,-(14-guess)), qf, qf, qcn, qcn, qa, qa};
		CobylaExitStatus result = Cobyla.FindMinimum(calcfc, 8 , 16, x, rhobeg, rhoend, iprint, maxfun);

		double pH = -Math.log10(x[0]);
		assertEquals(2.0897535160247753, pH, 1E-4);
		System.out.println("calculated pH: " + pH);
	}
}
