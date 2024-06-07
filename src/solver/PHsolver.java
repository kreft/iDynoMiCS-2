package solver;

import compartment.EnvironmentContainer;
import dataIO.Log;
import grid.ArrayType;
import grid.SpatialGrid;
import optimization.functionImplementation.ObjectiveFunctionNonLinear;
import optimization.functionImplementation.Options;
import org.ejml.data.DMatrixRMaj;
import solvers.NonlinearEquationSolver;

import java.util.Collection;
import java.util.HashMap;

public class PHsolver {

    double rhobeg = 0.25;
    double rhoend = 1.0e-14;
    int iprint = 1;
    int maxfun = 35000;

    public void PHSolver()
    {

    }

    public void solve(EnvironmentContainer environment) {

        Collection<SpatialGrid> solutes = environment.getSolutes();
        /* FIXME: use for initial guess */
        Collection<SpatialGrid> grids = environment.getSpesials();

        int nPKa = 0;
        HashMap<String,Double> solMap = new HashMap<String, Double>();
        HashMap<String,double[]> pKaMap = new HashMap<String,double[]>();

        for ( SpatialGrid s : solutes ) {
            if( s.getpKa() != null ) {
                nPKa += s.getpKa().length;
                pKaMap.put(s.getName(), s.getpKa());
                /* FIXME update to per location */
                solMap.put(s.getName(), s.getAverage(ArrayType.CONCN));
            }
        }

        NonLinearFunction myFun = new NonLinearFunction();
        myFun.setPkaMap(pKaMap);
        myFun.setSolMap(solMap);

        NonlinearEquationSolver solver = chemTestnoLin(( nPKa+1 ) * 2,0, myFun,false);

        double pH = -Math.log10(solver.getX().get(0,0));

        SpatialGrid pHgrid = environment.getSpecialGrid("pH");
        pHgrid.setAllTo(ArrayType.CONCN,pH);

        if(Log.shouldWrite(Log.Tier.NORMAL))
            Log.out(Log.Tier.NORMAL, "pH: " + pH);

    }

    public class NonLinearFunction implements ObjectiveFunctionNonLinear {
        int b = 8;
        double kw = 1.0E-14;
        HashMap<String, double[]> _pKaMap = null;
        HashMap<String, Double> _solMap = null;

        public void setPkaMap(HashMap<String, double[]> pKaMap) {
            this._pKaMap = pKaMap;
        }

        public void setSolMap(HashMap<String, Double> solMap) {
            this._solMap = solMap;
        }

        public int numVars() {
            return 2 + _solMap.size() * 2;
        }

        @Override
        public DMatrixRMaj getF(DMatrixRMaj x) {
            if (_pKaMap == null)
                return null;
            else {
                /* FIXME: update this to work with multi-charge */
                b = numVars();
                DMatrixRMaj fun = new DMatrixRMaj(b, 1);
                /* set variables and define equilibria */
                double h = x.get(0, 0);
                double oh = x.get(1, 0);
                fun.set(0, 0, ((h * oh) - kw));
                double[] s = new double[b - 2];
                int i = 0, j = 2, k = 2;
                for (String sol : _pKaMap.keySet()) {
                    for (Double d : _pKaMap.get(sol)) {
                        s[i] = x.get(j, 0);
                        s[i + 1] = x.get(j + 1, 0);
                        fun.set(k++, 0, ((h * s[i]) / s[i + 1] - d));
                        i += 2;
                        j += 2;
                    }
                }
                /* mass balance */
                i = 0;
                for (String sol : _solMap.keySet()) {
                    fun.set(k++, 0, (s[i]) + s[i + 1] - _solMap.get(sol));
                    i += 2;
                }
                /* charge balance */
                double charge = 0.0;
                for (int l = 0; l < b - 2; l += 2)
                    charge += s[l];
                fun.set(1, 0, h - oh - charge);
                return fun;
            }
        }
        @Override
        public DMatrixRMaj getJ(DMatrixRMaj x) {
            return null;
        }
    }

        public NonlinearEquationSolver chemTestnoLin( int numberOfVariables, int solver, NonLinearFunction f, boolean analyticalJacobian) {

        double now = System.nanoTime();
        //input class



//            @Override
//            public DMatrixRMaj getF(DMatrixRMaj x) {
//
//                double q = 0.1;
//
//                double k1 = 7.2E-4;
//                double k2 = 6.2E-10;
//                double k3 = 1.6E-10;
//                double kw = 1.0E-14;
//
//                double qf = q;
//                double qcn = q;
//                double qa = q;
//
//                DMatrixRMaj fun = new DMatrixRMaj(numberOfVariables, 1);
//                for (int i = 0; i < numberOfVariables / b; i++) {
//                    double h = x.get(b * i, 0);
//                    double oh = x.get(b * i + 1, 0);
//                    double f = x.get(b * i + 2, 0);
//                    double fh = x.get(b * i + 3, 0);
//                    double cn = x.get(b * i + 4, 0);
//                    double cnh = x.get(b * i + 5, 0);
//                    double a = x.get(b * i + 6, 0);
//                    double ah = x.get(b * i + 7, 0);
//
//                    // equilibria
//                    fun.set(b * i, 0, ((h * f) / fh - k1));
//                    fun.set(b * i + 1, 0, ((h * cn) / cnh - k2));
//                    fun.set(b * i + 2, 0, ((a * h) / ah - k3));
//                    fun.set(b * i + 3, 0, ((h * oh) - kw));
//
//                    // mass balances
//                    fun.set(b * i + 4, 0, f + fh - qf);
//                    fun.set(b * i + 5, 0, cn + cnh - qcn);
//                    fun.set(b * i + 6, 0, a + ah - qa);
//
//                    // charge balance
//                    fun.set(b * i + 7, 0, h - oh - f - cn - a);
//                }
//                return fun;
//            }



        //initial guess
        DMatrixRMaj initialGuess = new DMatrixRMaj(numberOfVariables, 1);
        for (int i = 0; i < f.numVars(); i++) {
            /* FIXME implement guess from bulk/previous */
            initialGuess.set(i, 0.1);
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
        if(Log.shouldWrite(Log.Tier.EXPRESSIVE)) {
            Log.out(Log.Tier.EXPRESSIVE,(System.nanoTime() - now) / 1e6 + " ms");
            Log.out(Log.Tier.EXPRESSIVE,nonlinearSolver.getResults().toString());
            nonlinearSolver.getX().print("%.15f");
        }
        
        return nonlinearSolver;
    }

}
