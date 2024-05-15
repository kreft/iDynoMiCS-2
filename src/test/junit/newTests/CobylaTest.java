package test.junit.newTests;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;
import debugTools.Testable;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


/**
 * \brief: Unit test for Cobyla
 *
 * jcobyla
 *
 * The MIT License
 *
 * Copyright (c) 2012 Anders Gustafsson, Cureos AB.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Remarks:
 *
 * The original Fortran 77 version of this code was by Michael Powell (M.J.D.Powell @ damtp.cam.ac.uk)
 * The Fortran 90 version was by Alan Miller (Alan.Miller @ vic.cmis.csiro.au). Latest revision - 30 October 1998
 *
 */
public class CobylaTest implements Testable {

	private double rhobeg = 0.5;
	private double rhoend = 1.0e-6;
	private int iprint = 1;
	private int maxfun = 3500;
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}

	/**
	 * This problem is taken from page 415 of Luenberger's book Applied
	 * Nonlinear Programming. It is to maximize the area of a hexagon of
	 * unit diameter.
	 */
	public void test(TestMode mode)
	{
		Calcfc calcfc = new Calcfc() {
			@Override
			public double Compute(int n, int m, double[] x, double[] con) {
				con[0] = 1.0 - x[2] * x[2] - x[3] * x[3];
				con[1] = 1.0 - x[8] * x[8];
				con[2] = 1.0 - x[4] * x[4] - x[5] * x[5];
				con[3] = 1.0 - x[0] * x[0] - Math.pow(x[1] - x[8], 2.0);
				con[4] = 1.0 - Math.pow(x[0] - x[4], 2.0) - Math.pow(x[1] - x[5], 2.0);
				con[5] = 1.0 - Math.pow(x[0] - x[6], 2.0) - Math.pow(x[1] - x[7], 2.0);
				con[6] = 1.0 - Math.pow(x[2] - x[4], 2.0) - Math.pow(x[3] - x[5], 2.0);
				con[7] = 1.0 - Math.pow(x[2] - x[6], 2.0) - Math.pow(x[3] - x[7], 2.0);
				con[8] = 1.0 - x[6] * x[6] - Math.pow(x[7] - x[8], 2.0);
				con[9] = x[0] * x[3] - x[1] * x[2];
				con[10] = x[2] * x[8];
				con[11] = -x[4] * x[8];
				con[12] = x[4] * x[7] - x[5] * x[6];
				con[13] = x[8];
				return -0.5 * (x[0] * x[3] - x[1] * x[2] + x[2] * x[8] - x[4] * x[8] + x[4] * x[7] - x[5] * x[6]);
			}
		};
		double[] x = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		CobylaExitStatus result = Cobyla.FindMinimum(calcfc, 9, 14, x, rhobeg, rhoend, iprint, maxfun);
		assertArrayEquals(null,
				new double[] { x[0], x[1], x[2], x[3], x[0], x[1], x[2], x[3], 0.0 }, x, 1.0e-4);
	}

}
