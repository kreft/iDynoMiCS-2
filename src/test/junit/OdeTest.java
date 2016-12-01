/**
 * 
 */
package test.junit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static test.AllTests.TOLERANCE;

import linearAlgebra.Vector;
import solver.ODEderivatives;
import solver.ODErosenbrock;
import test.AllTests;
import utility.ExtraMath;

/**
 * \brief Test checking that the Ordinary Differential Equation (ODE) solvers
 * behave as they should.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class OdeTest
{
	@Test
	public void exponentialDecayWorks() throws IllegalArgumentException, Exception
	{
		AllTests.setupSimulatorForTest(1.0, 1.0, "exponentialDecayWorks");
		/* Parameters. */
		String[] names = new String[]{"var"};
		double absTol = 0.001;
		double hMax = 0.01;
		double k = -1.0;
		double tStep = 1.0;
		int nStep = 10;
		double[] y = new double[]{1.0};
		/* Solve the system. */
		ODErosenbrock solver = new ODErosenbrock(names, false, absTol, hMax);
		solver.setDerivatives(new ODEderivatives()
		{
			@Override
			public void firstDeriv(double[] destination, double[] y)
			{
				Vector.timesTo(destination, y, k);
			}
		});
		double t = 0.0;
		for ( int i = 0; i < nStep; i++ )
		{
			y = solver.solve(y, tStep);
			t += tStep;
			assertTrue(ExtraMath.areEqual(y[0], Math.exp(k*t), TOLERANCE));
		}
	}
}
