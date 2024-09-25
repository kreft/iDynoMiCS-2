package solver;

import compartment.EnvironmentContainer;
import dataIO.Log;
import expression.arithmetic.Unit;
import grid.SpatialGrid;
import optimization.functionImplementation.ObjectiveFunctionNonLinear;
import optimization.functionImplementation.Options;
import org.ejml.data.DMatrixRMaj;
import solvers.NonlinearEquationSolver;

import java.util.HashMap;
import java.util.LinkedList;

public class PHsolver {

    // FIXME to make this robust can we build this directly from Idynomics.unitSystem?
    Unit microUnit = new Unit("amol/um+3");
    Unit siUnit = new Unit("mol/m+3");
    double rhobeg = 0.25;
    double rhoend = 1.0e-14;
    int iprint = 1;
    int maxfun = 35000;

    public void PHSolver()
    {

    }

    private double pow10(double a){
        return Math.pow(10,a);
    }

    public HashMap<String,Double> solve(EnvironmentContainer environment, HashMap<String,Double> solMap,
                                        HashMap<String,double[]> pKaMap) {
            return solve(environment, solMap, null, pKaMap);
    }
    public HashMap<String,Double> solve(EnvironmentContainer environment, HashMap<String,Double> solMap,
                                        HashMap<String,Double> specMap, HashMap<String,double[]> pKaMap) {
        int nPKa = 0;
        /* FIXME obtain fom pKaMap */
        for ( SpatialGrid s : environment.getSolutes() ) {
            if( s.getpKa() != null ) {
                nPKa += s.getpKa().length;
            }
        }
        NonLinearFunction myFun = new NonLinearFunction();
        myFun.setSolMap(solMap);
        myFun.setPkaMap(pKaMap);
        if( specMap != null)
            myFun.setInitial(specMap);

        NonlinearEquationSolver solver = chemTestnoLin(( nPKa+1 ) * 2,0, myFun,false);
        double pH = -Math.log10(solver.getX().get(0,0) * siUnit.modifier());

        HashMap<String,Double> specialMap = new HashMap<String, Double>();
        int i=0;
        specialMap.put("pH", pH);
        for(String s : myFun.getLabels()) {
            if( s.equals("h") || s.equals("oh")) {
                // do nothing skipping h and oh for now, may include later
            }
            else
                specialMap.put(s,solver.getX().get(i,0));
            i++;
        }
        return specialMap;
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
        NonlinearEquationSolver solver = chemTestnoLin(nVar,1, myFun,false);
        double pH = solver.getX().get(0,0);

//        HashMap<String,Double> specialMap = new HashMap<String, Double>();
//        int i=0;
//        specialMap.put("pH", pH);
//        for(String s : myFun.getLabels()) {
//            if( s.equals("h") || s.equals("oh")) {
//                // do nothing skipping h and oh for now, may include later
//            }
//            else
//                specialMap.put(s,solver.getX().get(i,0));
//            i++;
//        }

        int i = 2, j = 0;
        pkSolutes[0].conc = pH;
        for (PKstruct struct : pkSolutes) {
            if( struct.pStates != null) {
                for (double d : struct.pStates) {
                    struct.pStates[j++] = pow10(-solver.getX().get(i++, 0));
                }
            }
            j=0;
        }
        return pkSolutes;
    }

    public class NonLinearFunction implements ObjectiveFunctionNonLinear {
        int b = 8;
        double kw = 1.0E-14;

        PKstruct[] _pkSolutes;
        HashMap<String, double[]> _pKaMap = null;
        HashMap<String, Double> _solMap = null;
        Double[] initial = null;
        LinkedList<String> out = new LinkedList<String>();

        public void setPkaMap(HashMap<String, double[]> pKaMap) {
            this._pKaMap = pKaMap;
            this.initial = new Double[ 2 + _pKaMap.size() * 2 ];
            initial[0] = 0.01;
            initial[1] = 0.01;
            int j = 2;
            for (String sol : _pKaMap.keySet()) {
                /* initial guess 50/50 */
                initial[j] = _solMap.get(sol) / 2;
                initial[j + 1] = _solMap.get(sol) / 2;
                j+=2;
            }
        }

        public void setSolMap(HashMap<String, Double> solMap) {
            this._solMap = solMap;
        }

        public void setPKstructs(PKstruct[] pkSolutes) {
            this._pkSolutes = pkSolutes;
        }
        public void setInitial(HashMap<String, Double> specMap) {
            int i = 0;
            initial[i++] = (specMap.get("pH") == 7.0 ? 0.01 : Math.pow(10.0,-specMap.get("pH")) * microUnit.modifier() );
            initial[i++] = (specMap.get("pH") == 7.0 ? 0.01 : Math.pow(10.0,-(14.0-specMap.get("pH"))) * microUnit.modifier() );
            for (String sol : _pKaMap.keySet()) {
                initial[i++] = specMap.get(sol+"___0");
                initial[i++] = specMap.get(sol+"___1");
            }
        }

        public void setInitial(PKstruct[] pKsolutes) {
            int nvar = 2;
            for ( PKstruct struct : _pkSolutes ) {
                if (struct.pKa != null )
                    nvar+=struct.pKa.length+1;
            }
            this.initial = new Double[nvar];
            int i = 0;
            initial[i++] = -Math.log10(pKsolutes[0].conc == 7.0 ? 0.01 : Math.pow(10.0,-pKsolutes[0].conc) * microUnit.modifier() );
            initial[i++] = -Math.log10(pKsolutes[0].conc == 7.0 ? 0.01 : Math.pow(10.0,-(14.0-pKsolutes[0].conc)) * microUnit.modifier() );
            for (PKstruct struct : pKsolutes) {
                if( struct.pStates != null) {
                    for (double d : struct.pStates)
                        initial[i++] = -Math.log10(d);
                }
            }
        }

        public int numVars() {
            return 2 + _solMap.size()*2;
        }

        public Double[] getInitial() {
            return initial;
        }

        @Override
        public DMatrixRMaj getF(DMatrixRMaj x) {
            if ( _pkSolutes != null ) {
                /* number of vars to solve for (+2 for h and oh) */
                b = getInitial().length;

                DMatrixRMaj fun = new DMatrixRMaj(b, 1);
                /* set variables and define equilibria */
                double h = x.get(0, 0);
                double oh = x.get(1, 0);

//                fun.set(0, 0, ((h * oh) - kw));
                fun.set(0, 0, h + oh - 14);
//                System.out.println(((h * oh) - kw));
                double[] s = new double[b - 2];
                int j = 2;
                for( PKstruct p : _pkSolutes) {
                    /* initial guess, use last, if none 50% of total */
                    if (p.pStates != null) {
                        for (double d : p.pStates) {
                            if (d == 0.0)
                                initial[j] = -Math.log10(p.conc / p.pStates.length);
                            else
                                initial[j] = -Math.log10(d);
                            j++;
                        }
                    }
                }
                int i = 0, k = 2;
                j = 2;
                for( PKstruct p : _pkSolutes) {
                    if (p.pStates != null) {
                        for (double d : p.pKa) {
                            s[i] = x.get(j, 0);
                            s[i + 1] = x.get(j + 1, 0);
//                            fun.set(k++, 0, (((h * s[i]) / s[i + 1]) - Math.pow(10,-d)));
                            fun.set(k++, 0, h + s[i] - s[i + 1] - d);
//                            System.out.println((h * s[i]) / s[i + 1] - Math.pow(10,-d));
                            i++;
                            j++;
                        }
                        /* move one over to the next solute */
                        i++;
                        j++;
                    }
                }
                /* mass balance */
                i = 0;
                for (PKstruct struct : _pkSolutes) {
                    if (struct.pKa != null ) {
                        double sum = 0;
                        double neg = 0;
                        for( double d : struct.pStates ) {
                            if ( s[i] < 0.0 )
                                neg += pow10(-s[i]);
                            sum += pow10(-s[i++]);

                        }
                        /* punish negative concentrations by neg^n Math.pow(neg,n) +*/
                        fun.set(k++, 0,  (sum - struct.conc));
                    }
                }
                /* charge balance FIXME do we need extra info on pState charges? */
                double charge = 0.0;

                charge = 0.0;
                i = 0;
                double molCharge = 1.0;
                for( PKstruct p : _pkSolutes) {
                    if (p.pStates != null) {
                        molCharge = p.pStates.length - (1.0 + p.maxCharge);
                        for (double d : p.pStates) {
                            charge += (pow10(-s[i]) * molCharge);
                            i++;
                            molCharge -= 1.0;
                        }
                    }
                    /* move one over to the next solute */
                }
//                System.out.println(h - oh - charge);
//                System.out.println(h + " " + oh + " " +  charge);
                fun.set(1, 0, pow10(-h) - pow10(-oh) - charge);

                return fun;
            } else if (_pKaMap != null) {

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
                    /* initial guess 50/50 */
                    initial[j] = _solMap.get(sol)/2;
                    initial[j+1] = _solMap.get(sol)/2;
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
            else {
                return null;
            }
        }
        @Override
        public DMatrixRMaj getJ(DMatrixRMaj x) {
            return null;
        }

        public LinkedList<String> getLabels(){
            out.add("h");
            out.add("oh");
            for (String sol : _pKaMap.keySet()) {
                // assuming 1 pka for now
                out.add(sol+"___0");
                out.add(sol+"___1");
            }
            return this.out;
        }
    }


    public NonlinearEquationSolver chemTestnoLin( int numberOfVariables, int solver, NonLinearFunction f, boolean analyticalJacobian) {
        return chemTestnoLin( numberOfVariables, solver, f, analyticalJacobian, null);
    }
    public NonlinearEquationSolver chemTestnoLin( int numberOfVariables, int solver, NonLinearFunction f, boolean analyticalJacobian, Double[] initial) {

        double now = System.nanoTime();
        //input class


/**
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
**/


        //initial guess
        DMatrixRMaj initialGuess = new DMatrixRMaj(numberOfVariables, 1);
        for (int i = 0; i < numberOfVariables; i++) {
            /* FIXME implement guess from bulk/previous */
            initialGuess.set(i, f.getInitial()[i]);
        }
        //options
        Options options = new Options(numberOfVariables);
        options.setAnalyticalJacobian(false);
        options.setAlgorithm(solver);
        options.setSaveIterationDetails(true);
        options.setAllTolerances(1e-14);
        options.setMaxStep(1000);
        options.setMaxIterations(1000);
        NonlinearEquationSolver nonlinearSolver = new NonlinearEquationSolver(f, options);
        //solve and print output
        nonlinearSolver.solve(new DMatrixRMaj(initialGuess));
        if(Log.shouldWrite(Log.Tier.EXPRESSIVE)) {
            Log.out(Log.Tier.EXPRESSIVE,(System.nanoTime() - now) / 1e6 + " ms");
            Log.out(Log.Tier.EXPRESSIVE, nonlinearSolver.getResults().toString());
            nonlinearSolver.getX().print("%.15f");
        }
        
        return nonlinearSolver;
    }

}
