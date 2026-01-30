package solver;

import dataIO.Log;
import linearAlgebra.Vector;

/**
 * \brief Bogacki–Shampine (RK23) with embedded error estimation.
 */
public class ODEbogackiShampinemethod extends ODEsolver
{
    private double hMax;
    private double hMin = 1e-7;
    private double hCurrent;
    private double tol;
    private double[] k1, k2, k3, k4;
    private double[] yTemp, yInitial;
    private boolean adaptive = true;
    private double tolAbs;
    private double tolRel;
    boolean first = true;

    /**
     * \brief Creates a new instance and initialise this solver.
     *
     * @param names List of {@code String} names of the variables this solver
     * deals with.
     * @param allowNegatives {@code true} to allow negative values of
     * variables, {@code false} to force any negative values to zero.
     * @param tolerance relative  tolerance of estimated error.
     * @param hMax Largest allowed internal time step.
     */
    public ODEbogackiShampinemethod(String[] names, boolean allowNegatives,
                                    double tolerance, double hMax)
    {
        this.init(names, allowNegatives, tolerance, hMax);
    }

    /**
     * \brief Initialise this solver.
     *
     * @param names List of {@code String} names of the variables this solver
     * deals with.
     * @param allowNegatives {@code true} to allow negative values of
     * variables, {@code false} to force any negative values to zero.
     * @param tolerance relative  tolerance of estimated error.
     * @param hMax Largest allowed internal time step.
     */
    public void init(String[] names, boolean allowNegatives, double tolerance, double hMax)
    {
        super.init(names, allowNegatives);
        this.hMax = hMax;
        this.hCurrent = hMin;
        this.tol = tolerance;
        this.k1 = new double[names.length];
        this.k2 = new double[names.length];
        this.k3 = new double[names.length];
        this.k4 = new double[names.length];
        this.yTemp = new double[names.length];
        this.yInitial = new double[names.length];

        this.tolRel = this.tol;
        this.tolAbs = this.tol * 1e-3;
    }

    /**
     * \brief (Re)initialise this solver with previously set or default tol and h settings.
     *
     * This method exists as y and k vectors need to be have enough space for potential extra
     * constituents that may have been added between process manager steps (fx. additional agents).
     *
     * @param names List of {@code String} names of the variables this solver
     * deals with.
     * @param allowNegatives {@code true} to allow negative values of
     * variables, {@code false} to force any negative values to zero.
     */
    public void init(String[] names, boolean allowNegatives)
    {
        super.init(names, allowNegatives);
        this.k1 = new double[names.length];
        this.k2 = new double[names.length];
        this.k3 = new double[names.length];
        this.k4 = new double[names.length];
        this.yTemp = new double[names.length];
        this.yInitial = new double[names.length];

        this.tolRel = this.tol;
        this.tolAbs = this.tol * 1e-3;
    }

    /**
     * \brief Solve using Bogacki–Shampine method until tFinal is reached.
     *
     * @param y will be overwritten with the solution
     * @param tFinal total time the solver should run for
     * @return updated vector y
     */
    public double[] solve(double[] y, double tFinal)
            throws Exception, IllegalArgumentException
    {
        super.solve(y, tFinal);
        double[] y2 = new double[y.length];
        double[] yBackup = new double[y.length];

        double timeRemaining = tFinal;

        double dt = this.hCurrent;
        first = true;

        while (timeRemaining > 1e-99)
        {
            dt = Math.min(dt, timeRemaining);

            if (this.adaptive) {
                // Backup current state in case we need to reject
                System.arraycopy(y, 0, yBackup, 0, y.length);

                boolean accepted = false;
                int maxAttempts = 10;
                int attempts = 0;

                while (!accepted && attempts < maxAttempts)
                {
                    System.arraycopy(yBackup, 0, y, 0, y.length);
                    double[] hOut = bogackiShampineAdaptive(y, y2, dt);

                    // Accept if estimated error is small
                    if (hOut[0] < 1.0)
                    {
                        accepted = true;
                        // Increase step for next iteration
                        dt = Math.min(hOut[1], this.hMax);
                    }
                    else
                    {
                        // Reject: reduce step size and retry
                        dt = Math.max(hOut[1], this.hMin);
                        attempts++;
                        this.first = true;

                        if( Log.shouldWrite(Log.Tier.DEBUG) )
                            Log.out(Log.Tier.DEBUG, "ODE tStep rejected, reducing dt.");

                        if (dt == this.hMin) {
                            if( Log.shouldWrite(Log.Tier.CRITICAL) ) {
                                Log.out(Log.Tier.CRITICAL, "hMin step " + dt);
                                System.err.println("hMin step " + dt);
                            }
                            accepted = true;
                            System.arraycopy(yBackup, 0, y, 0, y.length);
                            bogackiShampine(y, this.hMin);
                            dt = this.hMin;
                        }

                        if (attempts >= maxAttempts) {
                            if( Log.shouldWrite(Log.Tier.CRITICAL) ) {
                                Log.out(Log.Tier.CRITICAL,"Warning: Max adaptive iterations reached. Accepting step " + dt);
                                System.err.println("Warning: Max adaptive iterations reached. Accepting step " + dt);
                            }
                            accepted = true;
                            System.arraycopy(yBackup, 0, y, 0, y.length);
                            bogackiShampine(y, this.hMin);
                            dt = this.hMin;
                        }
                    }
                }
            }
            else
            {
                bogackiShampine(y, dt);
            }

            if (!this._allowNegatives)
                Vector.makeNonnegative(y);

            timeRemaining -= dt;
//            this._deriv.postMiniStep(y, dt);
        }
        this.hCurrent = dt;
        return y;
    }

    /**
     * \brief Apply a step of the Bogacki–Shampine method (non-adaptive).
     *
     * @param y will be overwritten with 3rd-order solution
     * @param dt time step
     */
    protected void bogackiShampine(double[] y, double dt)
    {
        if (!this._allowNegatives)
            Vector.makeNonnegative(y);

        /* Stage 1: k1 = f(t, y) */
        if (this.first)
            this._deriv.firstDeriv(this.k1, this.yInitial);

        /* Stage 2: k2 = f(t + h/2, y + (h/2)*k1) */
        System.arraycopy(y, 0, this.yTemp, 0, y.length);
        for (int i = 0; i < y.length; i++)
            this.yTemp[i] += 0.5 * dt * this.k1[i];
        if (!this._allowNegatives)
            Vector.makeNonnegative(this.yTemp);
        this._deriv.firstDeriv(this.k2, this.yTemp);

        /* Stage 3: k3 = f(t + 3h/4, y + (3h/4)*k2) */
        System.arraycopy(y, 0, this.yTemp, 0, y.length);
        for (int i = 0; i < y.length; i++)
            this.yTemp[i] += 0.75 * dt * this.k2[i];
        if (!this._allowNegatives)
            Vector.makeNonnegative(this.yTemp);
        this._deriv.firstDeriv(this.k3, this.yTemp);

        /* Stage 4: k4 only needed for 2nd-order solution */
        this.first = true;

        /* Update y with 3rd-order solution */
        for (int i = 0; i < y.length; i++)
            y[i] += dt * ((2.0/9.0 * this.k1[i]) +
                    (1.0/3.0 * this.k2[i]) +
                    (4.0/9.0 * this.k3[i]));

        if (!this._allowNegatives)
            Vector.makeNonnegative(y);
    }

    /**
     * \brief Apply a step of Bogacki–Shampine with embedded error estimation.
     *
     * @param y will be overwritten with 3rd-order solution
     * @param y2Out will be overwritten with 2nd-order solution
     * @param dt time step
     * @return Normalized error [0] and estimated optimal next step size [1]
     */
    protected double[] bogackiShampineAdaptive(double[] y, double[] y2Out, double dt)
    {
        System.arraycopy(y, 0, this.yInitial, 0, y.length);

        /* Stage 1: k1 = f(t, y_initial) */
        if (this.first)
            this._deriv.firstDeriv(this.k1, this.yInitial);
        first = false;

        /* Stage 2: k2 = f(t + h/2, y_initial + (h/2)*k1) */
        System.arraycopy(this.yInitial, 0, this.yTemp, 0, y.length);
        for (int i = 0; i < y.length; i++)
            this.yTemp[i] += 0.5 * dt * this.k1[i];
        if (!this._allowNegatives)
            Vector.makeNonnegative(this.yTemp);
        this._deriv.firstDeriv(this.k2, this.yTemp);

        /* Stage 3: k3 = f(t + 3h/4, y_initial + (3h/4)*k2) */
        System.arraycopy(this.yInitial, 0, this.yTemp, 0, y.length);
        for (int i = 0; i < y.length; i++)
            this.yTemp[i] += 0.75 * dt * this.k2[i];
        if (!this._allowNegatives)
            Vector.makeNonnegative(this.yTemp);
        this._deriv.firstDeriv(this.k3, this.yTemp);

        /* 3rd-order solution: y_{n+1}^(3) = y_n + h*(2/9*k1 + 1/3*k2 + 4/9*k3) */
        for (int i = 0; i < y.length; i++)
            y[i] = this.yInitial[i] + dt * ((2.0/9.0 * this.k1[i]) +
                    (1.0/3.0 * this.k2[i]) +
                    (4.0/9.0 * this.k3[i]));

        /* Stage 4: k4 = f(t + h, y_initial + (2h/9)*k1 + (h/3)*k2 + (4h/9)*k3) */
//        System.arraycopy(this.yInitial, 0, this.yTemp, 0, y.length);
//        for (int i = 0; i < y.length; i++)
//            this.yTemp[i] += (2.0/9.0 * dt * this.k1[i]) +
//                    (1.0/3.0 * dt * this.k2[i]) +
//                    (4.0/9.0 * dt * this.k3[i]);
//        if (!this._allowNegatives)
//            Vector.makeNonnegative(this.yTemp);
//        this._deriv.firstDeriv(this.k4, this.yTemp);

        /* Stage 4: k4 = f(t + h, y_initial + (2h/9)*k1 + (h/3)*k2 + (4h/9)*k3) */
        System.arraycopy(y, 0, this.yTemp, 0, y.length);
        if (!this._allowNegatives)
            Vector.makeNonnegative(this.yTemp);
        this._deriv.firstDeriv(this.k4, this.yTemp);

        /* 2nd-order solution: y_{n+1}^(2) = y_n + h*(7/24*k1 + 1/4*k2 + 1/3*k3 + 1/8*k4) */
        for (int i = 0; i < y.length; i++)
            y2Out[i] = this.yInitial[i] + dt * ((7.0/24.0 * this.k1[i]) +
                    (1.0/4.0 * this.k2[i]) +
                    (1.0/3.0 * this.k3[i]) +
                    (1.0/8.0 * this.k4[i]));

        /* Wiki: "FSAL—first same as last—property is that the stage value k4 in one step equals k1 in the next step;
        thus, only three function evaluations are needed per step." */
        System.arraycopy(this.k4, 0, this.k1, 0, y.length);

        /* Proper error estimation with relative scaling */
        return computeStepSize(y, y2Out, dt);
    }

    /**
     * \brief Compute optimal step size based on error estimate.
     *
     * <p>Uses relative error scaled by state magnitude and tolerance.
     * Standard approach for adaptive RK methods: h_new = h * (1/eN)^(1/p)
     * where p=3 for 3rd-order method and eN is the max normalized error.</p>
     *
     * @param y3 3rd-order solution
     * @param y2 2nd-order solution
     * @param dt current step size
     * @return normalized error [0] optimal next step size [1]
     */
    private double[] computeStepSize(double[] y3, double[] y2, double dt)
    {
        double eN = errorEstimate(y3, y2);
        double safetyFactor = 0.9;

        /* Third order method so 1/4 -> https://en.wikipedia.org/wiki/Adaptive_step_size */
        double hNew = dt * safetyFactor * Math.pow(1.0 / eN, 1.0/4.0);

//        System.out.println(dt + " " + hNew + " " + eN);
        /* Limit new step size */
        hNew = Math.max(hNew, this.hMin);
        hNew = Math.min(hNew, this.hMax);
        if( Log.shouldWrite(Log.Tier.DEBUG) )
            Log.out(Log.Tier.DEBUG, "ODE err and tStep: " + eN + " " + hNew);
        return new double[] {eN, hNew};
    }

    /**
     * \brief calculate the max normalized error based on the 3rd and 2nd-order solution.
     *
     * @param y3 3rd-order solution
     * @param y2 2nd-order solution
     * @return normalized error
     */
    private double errorEstimate(double[] y3, double[] y2) {
        double eN = 0.0;
//        double[] normalizedErr = new double[y3.length];

        for (int i = 0; i < y3.length; i++) {
            double err = Math.abs(y3[i] - y2[i]);

//            double scale = Math.max(Math.abs(y3[i]), Math.abs(y2[i]));
//            scale = Math.max(scale, this.tol); // Avoid division by very small numbers
//
//            double relErr = err / (this.tol * scale);
//            eN = Math.max(eN, relErr);

            double absScale = this.tolAbs;
            double relScale = this.tolRel * Math.max(Math.abs(y3[i]), Math.abs(y2[i]));

            double scale = Math.max(absScale, relScale);

            double normalizedErr = err / scale;
            eN = Math.max(eN, normalizedErr);
        }

        /* Avoid division by zero */
        eN = Math.max(eN, 1e-6);
        eN = Math.min(eN, 100.0);
        return eN;
    }

    public void setTolerance(double tol) {
        this.tol = tol;
    }
}