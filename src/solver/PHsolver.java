package solver;

import dataIO.Log;
import optimization.functionImplementation.ObjectiveFunctionNonLinear;
import optimization.functionImplementation.Options;
import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.MatrixIO;
import solvers.NonlinearEquationSolver;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class PHsolver implements Callable<PKstruct[]> {

    private PKstruct[] pkSolutes;

    public PHsolver() {
    }

    public PHsolver(PKstruct[] pkSolutes) {
        this.pkSolutes = pkSolutes;
    }

    private boolean negConc = false;

    private static final double THRESHOLD = 1e-15;
    private static final double EPSILON = 1e-300;

    static double softplusThreshold = THRESHOLD;
    @Override
    public PKstruct[] call() {
        return solve(pkSolutes);
    }

    private double smoothPositiveTransform(double x) {
        // Ensure input non-negative (or clip if needed)
        double val = Math.max(x, 0);

        // Safeguard inside log
        double shiftedLog = Math.log(val + EPSILON) - Math.log(THRESHOLD);

        // Standard softplus on shifted log scale
        double sp = softplus(shiftedLog);

        // Scale back to original magnitude
        return sp * THRESHOLD;
    }

    private double smoothPositiveInverse(double y) {
        // Inverse of smoothPositiveTransform

        // Guard y >= 0
        double val = Math.max(y, 0);

        // Inverse softplus
        double invSp = softplusInverse(val / THRESHOLD);

        // Undo shifting and exponentiate
        double x = Math.exp(invSp + Math.log(THRESHOLD)) - EPSILON;

        // Avoid negative numerics
        return Math.max(x, 0);
    }

    // Helper functions for softplus transformation
    private double softplus(double y) {
        // x = ln(1 + exp(y)) - numerically stable version
        if (y > 30) return y; // For large y, softplus(y) ≈ y
        return Math.log1p(Math.exp(y)); // More stable than log(1 + exp(y))
    }

    private double softplusInverse(double x) {
        // y = ln(exp(x) - 1) - inverse of softplus
        if (x > 30) return x; // For large x
        return Math.log(Math.expm1(x)); // More stable than log(exp(x) - 1)
    }

    public PKstruct[] solve(PKstruct[] pkSolutes) {
        this.negConc = false;
        // Existing solve logic remains unchanged
        normalizePKaValues(pkSolutes);
        int nVar = 2;

        for (PKstruct struct : pkSolutes) {
            if (struct.pKa != null ) nVar += struct.pStates.length;
        }

        NonLinearFunction myFun = new NonLinearFunction();
        myFun.setPKstructs(pkSolutes);
        myFun.setInitial(pkSolutes);

        NonlinearEquationSolver solver = chemTestnoLin(nVar, 0, myFun);

        // Convert results based on which variables used transformation
        double h = myFun.shouldTransformVariable(0) ? smoothPositiveTransform(solver.getX().get(0, 0)) : solver.getX().get(0, 0);
        double oh = myFun.shouldTransformVariable(1) ? smoothPositiveTransform(solver.getX().get(1, 0)) : solver.getX().get(1, 0);

        System.out.println("h, oh, ph" + h + " " + oh);
        double pH = (h > 1E-10 ? -Math.log10(h) : 14 + Math.log10(oh));
        int i = 2;

        pkSolutes[0].conc = pH;

        for (PKstruct struct : pkSolutes) {
            if (struct.pStates != null) {
                for (int j = 0; j < struct.pStates.length; j++) {
                    if (myFun.shouldTransformVariable(i)) {
                        struct.pStates[j] = smoothPositiveTransform(solver.getX().get(i, 0));
                    } else {
                        struct.pStates[j] = minimumConcentration(solver.getX().get(i, 0));
                    }
                    i++;
                }
            }
        }

        for (PKstruct p : pkSolutes) {
            if (p.pKa != null ) {
                double sum = 0.0;
                for( double d : p.pStates ) {
                    sum += d;
                }
                if( sum - p.conc > 1e-9)
                    System.out.println("protonation state mass not balanced " + (sum - p.conc));
            }
        }
        return pkSolutes;
    }

    private double minimumConcentration(double conc) {
        if( conc < 1.0E-99 ) {
            negConc = true;
            System.out.println(this.getClass().getSimpleName() + " minimum concentration safety triggered");
            return 1.0E-99;
        }
        else
            return conc;
    }

    public void normalizePKaValues(PKstruct[] pkStructs) {
        double smallValue = 1E-1;
        for (PKstruct pkStruct : pkStructs) {
            if (pkStruct.pKa != null) {
                for (int i = 0; i < pkStruct.pKa.length; i++) {
                    if (pkStruct.pKa[i] < smallValue) {
                        pkStruct.pKa[i] = smallValue;
                    } else if (pkStruct.pKa[i] > 14.0-smallValue) {
                        pkStruct.pKa[i] = 14.0-smallValue;
                    }
                }
            }
        }
    }

    public class NonLinearFunction implements ObjectiveFunctionNonLinear {
        int b = 0;
        double kw = 1.0E-14;
        PKstruct[] _pkSolutes;
        Double[] initial = null;
        private boolean[] transformVariable; // Track which variables need transformation

        public void setPKstructs(PKstruct[] pkSolutes) {
            this._pkSolutes = pkSolutes;
        }

        public boolean shouldTransformVariable(int index) {
            return transformVariable != null && index < transformVariable.length && transformVariable[index];
        }

        public void setInitial(PKstruct[] pKsolutes) {
            int nvar = 2;
            for (PKstruct struct : _pkSolutes) {
                if (struct.pKa != null)
                    nvar += struct.pKa.length + 1;
            }
            this.initial = new Double[nvar];
            this.transformVariable = new boolean[nvar];

            int i = 0;

            // H+ and OH- - only transform if we expect very low concentrations
            double hConc = (pKsolutes[0].conc == 7.0 ? 1e-4 : Math.pow(10.0, -pKsolutes[0].conc));
            double ohConc = (pKsolutes[0].conc == 7.0 ? 1e-4 : Math.pow(10.0, -(14.0 - pKsolutes[0].conc)));

            // Transform H+ if concentration is very low (high pH)
            if (hConc < softplusThreshold) {
                transformVariable[i] = true;
                initial[i] = smoothPositiveInverse(Math.max(hConc, 1E-12));
            } else {
                transformVariable[i] = false;
                initial[i] = Math.max(hConc, 1E-12);
            }
            i++;

            // Transform OH- if concentration is very low (low pH)
            if (ohConc < softplusThreshold) {
                transformVariable[i] = true;
                initial[i] = smoothPositiveInverse(Math.max(ohConc, 1E-12));
            } else {
                transformVariable[i] = false;
                initial[i] = Math.max(ohConc, 1E-12);
            }
            i++;

            // Species concentrations - only transform very small ones
            for (PKstruct struct : pKsolutes) {
                if (struct.pStates != null) {
                    for (double d : struct.pStates) {
                        double conc = Math.max(d, 1e-15);
                        if (d < softplusThreshold) {
                            transformVariable[i] = true;
                            initial[i] = smoothPositiveInverse(Math.max(d, 1e-15));
                        } else {
                            transformVariable[i] = false;
                            initial[i] = conc;
                        }
                        i++;
                    }
                }
            }
        }

        public double getAdaptiveTolerance() {
            double minConc = Arrays.stream(_pkSolutes).mapToDouble(s -> s.conc).min().orElse(1e-3);
            return Math.min(0.001 * kw, minConc * 1e-10);
        }

        public Double[] getInitial() {
            return initial;
        }

        public double[] calculateSpeciesConcentrations(PKstruct pkStruct, double hConcentration) {
            int numStates = pkStruct.pKa.length + 1;
            double[] pStates = new double[numStates];

            double[] alpha = new double[numStates];
            for (int i = 0; i < numStates; i++) {
                double numerator = Math.pow(hConcentration, numStates - 1 - i);
                for (int j = 0; j < i; j++) {
                    numerator *= Math.pow(10, -pkStruct.pKa[j]);
                }
                double denominator = 0;
                for (int j = 0; j < numStates; j++) {
                    double term = Math.pow(hConcentration, numStates - 1 - j);
                    for (int k = 0; k < j; k++) {
                        term *= Math.pow(10, -pkStruct.pKa[k]);
                    }
                    denominator += term;
                }
                alpha[i] = numerator / denominator;
            }

            for (int i = 0; i < numStates; i++) {
                pStates[i] = alpha[i] * pkStruct.conc;
            }
            return pStates;
        }

        @Override
        public DMatrixRMaj getF(DMatrixRMaj x) {
            /* number of vars = total number of protonation states +2 for h and oh */
            b = getInitial().length;
            DMatrixRMaj fun = new DMatrixRMaj(b, 1);

            /* "negs" used to push the objective function away from negative concentrations */
            /* Only apply to non-transformed variables */
            double negs = 0.0;
            for( int l = 0; l < b; l++) {
                if (!shouldTransformVariable(l)) {
                    double temp = x.get(l, 0);
                    if( temp < 0.0 )
                        negs += temp;
                }
            }

            negs = negs * 0.1;
            /* Get concentrations - transform only selected variables */
            double h = shouldTransformVariable(0) ? smoothPositiveTransform(x.get(0, 0)) : Math.max(x.get(0, 0), 1E-199);
            double oh = shouldTransformVariable(1) ? smoothPositiveTransform(x.get(1, 0)) : Math.max(x.get(1, 0), 1E-199);

            /* Water dissociation */
            fun.set(0, 0, ((h * oh) - kw) +0.1*negs); // negs);

            /* initial guess if no prior pStates exist, distribute the mass evenly over the protonation states */
            double[] s = new double[b - 2];
            int j = 2;
            for( PKstruct p : _pkSolutes) {
                if (p.pStates != null) {
                    int k = 0;
                    for (double d : p.pStates) {
                        double[] pStateArray = calculateSpeciesConcentrations(p, h);
                        if (d == 0.0) {
                            if (shouldTransformVariable(j)) {
                                initial[j] = smoothPositiveInverse(Math.max(pStateArray[k], 1E-15));
                            } else {
                                initial[j] = Math.max(pStateArray[k], 1E-15);
                            }
                        } else {
                            if (shouldTransformVariable(j)) {
                                initial[j] = smoothPositiveInverse(Math.max(d, 1E-15));
                            } else {
                                initial[j] = Math.max(d, 1E-15);
                            }
                        }
                        j++;
                        k++;
                    }
                }
            }

            /* Solute dissociation equations (pH equilibria) */
            int i = 0, k = 2;
            j = 2;
            for( PKstruct p : _pkSolutes) {
                if (p.pStates != null) {
                    for (double d : p.pKa) {
                        // Get concentrations based on transformation
                        s[i] = shouldTransformVariable(j) ? smoothPositiveTransform(x.get(j, 0)) : Math.max(x.get(j, 0), 1E-199);
                        s[i + 1] = shouldTransformVariable(j + 1) ? smoothPositiveTransform(x.get(j + 1, 0)) : Math.max(x.get(j + 1, 0), 1E-199);

                        /* calculate dissociation in acidic solutions with H+, for alkaline solutions use OH- */
                        if( -Math.log10(h) <= 7.0 )
                            fun.set(k++, 0, (((h * s[i+1]) / noZeroDiv(s[i])) - Math.pow(10,-d)) +0.1*negs ); //+ negs);
                        else
                            fun.set(k++, 0, (((oh * s[i]) / noZeroDiv(s[i+1])) - kw/Math.pow(10,-d)) +0.1*negs ); //+ negs);
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
                    fun.set(k++, 0, (sum - p.conc) -negs ); //+ negs);
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
            fun.set(1, 0, (h - oh + charge) -negs );
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
        options.setAllTolerances(1E-16);
        options.setMaxStep(5_000); // min 5k in some cases
        options.setMaxIterations(5_000); // could increase if we notice this doesn't work
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
