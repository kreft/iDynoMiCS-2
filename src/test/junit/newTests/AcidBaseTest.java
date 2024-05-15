package test.junit.newTests;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;
import debugTools.Testable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AcidBaseTest implements Testable {

	double q = 0.1;
	private double rhobeg = 0.25;
	private double rhoend = 1.0e-14;
	private int iprint = 1;
	private int maxfun = 35000;
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
				con[0] = ( x[0] * x[2] ) / x[3] - k1;
				con[1] = ( x[0] * x[4] ) / x[5] - k2;
				con[2] = ( x[0] * x[6] ) / x[7] - k3;
				con[3] = ( x[0] * x[1] ) - kw;

				// mass balances
				con[4] = x[2] + x[3] - qf;
				con[5] = x[4] + x[5] - qcn;
				con[6] = x[6] + x[7] - qa;

				// charge balance
				con[7] = x[0]-x[1]-x[2]-x[4]-x[6];

				// negative concentration stop
//				for(double s : x)
//					if( s < 0.0 )
//						con[8] += s;

				double out = 0;

				for (double r : con) {
					out += Math.abs(r);
				}
				for (double s : x) {
					if( s < 0.0)
						out += Math.pow(s*2,2);
				}
				return out;

//				for (double r : con) {
//					out += Math.pow(r,2);
//				}
//				return out/con.length;
			}
		};

		double[] x = {0.05, 0.05, qf, qf, qcn, qcn, qa, qa};

//		 double guess = 5;
//		 double[] x = {Math.pow(10,-(guess)), Math.pow(10,-(14-guess)), qf, qf, qcn, qcn, qa, qa};
		CobylaExitStatus result = Cobyla.FindMinimum(calcfc, 8 , 8, x, rhobeg, rhoend, iprint, maxfun);

		double pH = -Math.log10(x[0]);
		assertEquals(2.0897535160247753, pH, 1E-4);
		System.out.println("calculated pH: " + pH);
	}
}
