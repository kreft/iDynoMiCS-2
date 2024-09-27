package solver;

import compartment.EnvironmentContainer;
import dataIO.Log;
import expression.arithmetic.Unit;
import grid.SpatialGrid;
import optimization.functionImplementation.ObjectiveFunctionNonLinear;
import optimization.functionImplementation.Options;
import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.MatrixIO;
import solvers.NonlinearEquationSolver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

public class PHsolver {

    // FIXME to make this robust can we build this directly from Idynomics.unitSystem?
    Unit microUnit = new Unit("amol/um+3");
    Unit siUnit = new Unit("mol/m+3");

    public void PHSolver()
    {

    }
    public PKstruct[] solve(PKstruct[] pkSolutes) {
        int nVar = 2;
        for ( PKstruct struct : pkSolutes ) {
            if (struct.pKa != null )
                nVar+=struct.pStates.length;
        }
        NonLinearFunction myFun = new NonLinearFunction();
        myFun.setPKstructs(pkSolutes);
        myFun.setInitial(pkSolutes);
        NonlinearEquationSolver solver = chemTestnoLin(nVar,0, myFun);
        double pH = -Math.log10(solver.getX().get(0,0) * siUnit.modifier());

        int i = 2;
        pkSolutes[0].conc = pH;
        for (PKstruct struct : pkSolutes) {
            if( struct.pStates != null) {
                for (int j=0; j < struct.pStates.length; j++)
                    struct.pStates[j] = solver.getX().get(i++, 0);
            }
        }
        return pkSolutes;
    }

    public class NonLinearFunction implements ObjectiveFunctionNonLinear {
        int b = 0;
        double kw = 1.0E-14;
        PKstruct[] _pkSolutes;
        Double[] initial = null;

        public void setPKstructs(PKstruct[] pkSolutes) {
            this._pkSolutes = pkSolutes;
        }
        public void setInitial(PKstruct[] pKsolutes) {
            int nvar = 2;
            for ( PKstruct struct : _pkSolutes ) {
                if (struct.pKa != null )
                    nvar+=struct.pKa.length+1;
            }
            this.initial = new Double[nvar];
            int i = 0;
            initial[i++] = (pKsolutes[0].conc == 7.0 ? 0.01 : Math.pow(10.0,-pKsolutes[0].conc) * microUnit.modifier() );
            initial[i++] = (pKsolutes[0].conc == 7.0 ? 0.01 : Math.pow(10.0,-(14.0-pKsolutes[0].conc)) * microUnit.modifier() );
            for (PKstruct struct : pKsolutes) {
                if( struct.pStates != null) {
                    for (double d : struct.pStates)
                        initial[i++] = d;
                }
            }
        }

        public Double[] getInitial() {
            return initial;
        }

        @Override
        public DMatrixRMaj getF(DMatrixRMaj x) {
            /* number of vars = total number of protonation states +2 for h and oh */
            b = getInitial().length;
            DMatrixRMaj fun = new DMatrixRMaj(b, 1);
            /* "negs" used to push the objective function away from negative concentrations */
            double negs = 0.0;
            for( int l = 0; l < b; l++) {
                double temp = x.get(l, 0);
                if( temp < 0.0 )
                    negs += temp;
            }
            negs = negs*1E2;
            /* Water dissociation */
            double h = x.get(0, 0);
            double oh = x.get(1, 0);
            fun.set(0, 0, ((h * oh) - kw) + negs);
            /* initial guess if no prior pStates exist, distribute the mass evenly over the protonation states */
            double[] s = new double[b - 2];
            int j = 2;
            for( PKstruct p : _pkSolutes) {
                if (p.pStates != null) {
                    for (double d : p.pStates) {
                        if (d == 0.0)
                            initial[j] = p.conc / p.pStates.length;
                        else
                            initial[j] = d;
                        j++;
                    }
                }
            }
            /* Solute dissociation equations (pH equilibria) */
            int i = 0, k = 2;
            j = 2;
            for( PKstruct p : _pkSolutes) {
                if (p.pStates != null) {
                    for (double d : p.pKa) {
                        s[i] = x.get(j, 0);
                        s[i + 1] = x.get(j + 1, 0);
                        /* calculate dissociation in acidic solutions with H+, for alkaline solutions use OH- */
                        if( Math.log10(h) < 7.0 )
                            fun.set(k++, 0, (((h * s[i+1]) / noZeroDiv(s[i])) - Math.pow(10,-d)) + negs);
                        else
                            fun.set(k++, 0, (((oh * s[i]) / noZeroDiv(s[i+1])) - kw/Math.pow(10,-d)) + negs);
                        i++;
                        j++;
                    }
                    /* hop over to the next solute */
                    i++;
                    j++;
                }
            }
            /* mass balances */
            i = 0;
            for (PKstruct p : _pkSolutes) {
                if (p.pKa != null ) {
                    double sum = 0.0;
                    for( double d : p.pStates ) {
                        sum += s[i++];
                    }
                    fun.set(k++, 0, (sum - p.conc) + negs);
                }
            }
            /* charge balance */
            double charge = 0.0;
            i = 0;
            for (PKstruct p : _pkSolutes) {
                if (p.pStates != null) {
                    double molCharge = p.maxCharge;
                    while (molCharge > p.maxCharge - p.pStates.length) {
                        charge += (s[i++] * molCharge--);
                    }
                }
            }
            fun.set(1, 0, h - oh + charge + negs);
            return fun;
        }
        @Override
        public DMatrixRMaj getJ(DMatrixRMaj x) {
            return null;
        }
    }

    public NonlinearEquationSolver chemTestnoLin( int numberOfVariables, int solver, NonLinearFunction f) {

        double now = System.nanoTime();
        DMatrixRMaj initialGuess = new DMatrixRMaj(numberOfVariables, 1);
        for (int i = 0; i < numberOfVariables; i++) {
            /* FIXME implement guess from bulk/previous */
            initialGuess.set(i, f.getInitial()[i]);
        }
        Options options = new Options(numberOfVariables);
        options.setAnalyticalJacobian(false);
        options.setSaveIterationDetails(true);
        options.setAlgorithm(solver);
        options.setAllTolerances(1e-15);
        options.setMaxStep(2000);
        options.setMaxIterations(2000);
        NonlinearEquationSolver nonlinearSolver = new NonlinearEquationSolver(f, options);

        nonlinearSolver.solve(new DMatrixRMaj(initialGuess));
        if(Log.shouldWrite(Log.Tier.EXPRESSIVE)) {
            Log.out(Log.Tier.EXPRESSIVE,"pH " + nonlinearSolver.getResults().toString() +
                    matrixToString(nonlinearSolver.getX(), "%6.3e") + (System.nanoTime() - now) / 1e6 + " ms");
        }
        return nonlinearSolver;
    }

    private double noZeroDiv(double d) {
        return Math.max(d,1E-99);
    }

    public String matrixToString(DMatrixRMaj m, String format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixIO.print(new PrintStream(stream), m, format);
        return stream.toString();
    }
}