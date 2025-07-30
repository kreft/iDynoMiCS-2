package test.junit.newTests;

import debugTools.Testable;
import optimization.functionImplementation.*;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;
import solvers.*;

import static org.junit.Assert.assertEquals;

public class AcidBaseTest3 implements Testable {

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

	public static NonlinearEquationSolver chemTestnoLin(int numberOfVariables, int solver, boolean analyticalJacobian) {

		double now = System.nanoTime();
		//input class
		int b = 8;

		ObjectiveFunctionNonLinear f = new ObjectiveFunctionNonLinear() {
			@Override
			public DMatrixRMaj getF(DMatrixRMaj x) {

				double q = 0.1;

				double k1 = 7.2E-4;
				double k2 = 6.2E-10;
				double k3 = 1.6E-10;
				double kw = 1.0E-14;

				double qf = q;
				double qcn = q;
				double qa = q;

				DMatrixRMaj fun = new DMatrixRMaj(numberOfVariables, 1);
				for (int i = 0; i < numberOfVariables/ b; i++) {
					double h =  x.get(b*i,0);
					double oh = x.get(b*i+1,0);
					double f =  x.get(b*i+2,0);
					double fh =  x.get(b*i+3,0);
					double cn = x.get(b*i+4,0);
					double cnh =  x.get(b*i+5,0);
					double a =  x.get(b*i+6,0);
					double ah = x.get(b*i+7,0);

					// equilibria
					fun.set(b * i, 0, (( h * f ) / fh - k1));
					fun.set(b * i + 1, 0, (( h * cn ) / cnh - k2));
					fun.set(b * i + 2, 0, (( a * h ) / ah - k3));
					fun.set(b * i + 3, 0, (( h * oh ) - kw));

					// mass balances
					fun.set(b * i + 4, 0, f + fh - qf);
					fun.set(b * i + 5, 0, cn + cnh - qcn);
					fun.set(b * i + 6, 0, a + ah - qa);

					// charge balance
					fun.set(b * i + 7, 0, h-oh-f-cn-a);
				}
				return fun;
			}

			@Override
			public DMatrixRMaj getJ(DMatrixRMaj x) {
				return null;
			}
		};
		//initial guess
		DMatrixRMaj initialGuess = new DMatrixRMaj(numberOfVariables, 1);
		for (int i = 0; i < numberOfVariables / b; i++) {
			initialGuess.set(b * i, 0.1 );
			initialGuess.set(b * i + 1, 0.1 );
			initialGuess.set(b * i + 2, 0.1 );
			initialGuess.set(b * i + 3, 0.1 );
			initialGuess.set(b * i + 4, 0.1 );
			initialGuess.set(b * i + 5, 0.1 );
			initialGuess.set(b * i + 6, 0.1 );
			initialGuess.set(b * i + 7, 0.1 );
		}
		//options
		Options options = new Options(numberOfVariables);
		options.setAnalyticalJacobian(false);
		options.setAlgorithm(solver);
		options.setSaveIterationDetails(true);
		options.setAllTolerances(1e-14);
		NonlinearEquationSolver nonlinearSolver = new NonlinearEquationSolver(f, options);
		//solve and print output
		nonlinearSolver.solve(new DMatrixRMaj(initialGuess));
		System.out.println((System.nanoTime()-now)/1e6 + " ms");
		System.out.println(nonlinearSolver.getResults());
//		System.out.println("F: " + nonlinearSolver.getFx());
		System.out.println("x: ");
		nonlinearSolver.getX().print("%.15f");

		return nonlinearSolver;
	}
	public void test(TestMode mode)
	{
		NonlinearEquationSolver solver = chemTestnoLin(8,0,false);
		double pH = -Math.log10(solver.getX().get(0,0));
		assertEquals(2.0897535160247753, pH, 1E-4);
		System.out.println("calculated pH: " + pH);
	}
}
