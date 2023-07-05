package test.other;

import test.junit.oldTests.Lsoda;

public class LsodaTest {
	
	public static void eqns(double[] y, double[] dYdt)
	{
		dYdt[0] = 1.0E4 * y[1] * y[2] - 0.04E0 * y[0];
		dYdt[2] = 3.0E7 * y[1] * y[1];
		dYdt[1] = -1.0 * (dYdt[0] + dYdt[2]);
	}
	
	public static void main(String args[])
	{
		double rwork1, rwork5, rwork6, rwork7;
		double[] atol = new double[4];
		double[] rtol = new double[4];
		double tout;

		int iwork1, iwork2, iwork5, iwork6, iwork7, iwork8, iwork9;
		int neq = 3;
		int itol, itask, iopt, jt, iout;

		iwork1 = iwork2 = iwork5 = iwork7 = iwork8 = iwork9 = 0;
		rwork1 = rwork5 = rwork6 = rwork7 = 0.0;
		iwork6 = 50000;
		
		tout = 0.4E0;
		itol = 2;
		rtol[0] = 0.0;
		atol[0] = 0.0;
		rtol[1] = rtol[3] = 1.0E-4;
		rtol[2] = 1.0E-8;
		atol[1] = 1.0E-6;
		atol[2] = 1.0E-10;
		atol[3] = 1.0E-6;
		itask = 1;
		iopt = 0;
		jt = 2;

		Lsoda.y = new double[4];
		Lsoda.y[1] = 1.0E0;
		Lsoda.y[2] = 0.0E0;
		Lsoda.y[3] = 0.0E0;
		Lsoda.t = 0.0E0;
		Lsoda.istate = 1;

		for (iout = 1; iout <= 12; iout++) {
			Lsoda.ODElsoda(neq, tout, itol, rtol, atol, itask, iopt, jt,
			      iwork1, iwork2, iwork5, iwork6, iwork7, iwork8, iwork9,
			      rwork1, rwork5, rwork6, rwork7, 0);
			System.out.println(" at t= "+ Lsoda.t +" y= "+ Lsoda.y[0] +", "+ Lsoda.y[1] +", "+ Lsoda.y[2] +", "+ Lsoda.y[3]);
			if (Lsoda.istate <= 0) {
				System.err.println("error istate = "+ Lsoda.istate);
			}
			tout = tout * 10.0E0;
		}
		if (Lsoda.init)
			Lsoda._freevectors();
		Lsoda.init = false;
	}

}

