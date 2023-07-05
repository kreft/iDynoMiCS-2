/**
 * 
 */
package solver;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief Numerical solver for stiff systems of Ordinary Differential Equations
 * (ODEs).
 * 
 * <b>Based on code in {@code simulator.diffusionSolver.Solve_chemostat} from
 * the iDynoMiCS 1 package.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ODErosenbrock extends ODEsolver
{
	/**
	 * TODO
	 */
	private final double d = 1.0 / (2.0 + Math.sqrt(2.0));
	
	/**
	 * TODO
	 */
	private final double e32  = 6.0 + Math.sqrt(2.0);
	
	/**
	 * The order of this method is 3.
	 */
	private final double power = 1.0/3.0;
	
	/**
	 * Error Per Step: the smallest positive floating-point number such that
	 * 1.0 + EPS > 1.0 
	 */
	private final double EPS = 2.22e-16;
	
	/**
	 * Maximum absolute tolerance. Note that the actual value of absTol used
	 * may be smaller with small time steps.
	 * 
	 * <p><b>[Rob 7Aug2015]</b> Switched to absolute tolerance from relative
	 * tolerance due to error estimates heading to infinity as values approach
	 * zero.</p>
	 */
	private double _absTol;
	
	/**
	 * Maximum time-step permissible. Note that the actual value of hMax used
	 * may be smaller with small time steps.
	 */
	private double _hMax;
	/**
	 * Estimate of the new value of <b>y</b> after a small time step.
	 */
	private double[] ynext;
	/**
	 * First derivative with respect to time.
	 */
	private double[] dYdT;
	/**
	 * Second derivative with respect to time.
	 */
	private double[] dFdT;
	/**
	 * Jacobian matrix.
	 */
	private double[][] dFdY;
	/**
	 * Estimate a of future rates of change, set and used by the solver.
	 */
	private double[] f1, f2;
	/**
	 * Weighing of the importance of a result, set and used by the solver.
	 */
	private double[] k1, k2, k3, kaux;
	
	/**
	 * {@code h * d * dFdT}, calculated once as it used several times.
	 */
	private double[] hddFdT;
	/**
	 * {@code I - ( h * d * dFdY)}
	 */
	private double[][] W;
	/**
	 * {@code double} set and used by the solver.
	 */
	private double t, error, tNext, h, test;
	/**
	 * {@code boolean} set and used by the solver.
	 */
	private boolean lastStep, noFailed, usingHMin, signsOK;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODErosenbrock()
	{

	}
	
	public ODErosenbrock(String[] soluteNames, boolean allowNegatives,
													double absTol, double hMax)
	{
		this.init(soluteNames, allowNegatives, absTol, hMax);
		Log.out(Tier.DEBUG, "ODErosenbrock: " + allowNegatives +  " " +  absTol 
				+  " " +  hMax);
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Initialise this solver.
	 * 
	 * @param names List of {@code String} names of the variables this solver
	 * deals with.
	 * @param allowNegatives {@code true} to allow negative values of
	 * variables, {@code false} to force any negative values to zero.
	 * @param absTol Absolute numerical tolerance of estimated error.
	 * @param hMax Largest allowed internal time step.
	 */
	public void init(String[] names, boolean allowNegatives,
												double absTol, double hMax)
	{
		super.init(names, allowNegatives);
		if ( this.nVar() == 0 )
			return;
		/* Doubles */
		this._absTol = absTol;
		this._hMax = hMax;
		/* Vectors */
		ynext = Vector.zerosDbl(this.nVar());
		dYdT  = Vector.zerosDbl(this.nVar());
		dFdT  = Vector.zerosDbl(this.nVar());
		f1    = Vector.zerosDbl(this.nVar());
		f2    = Vector.zerosDbl(this.nVar());
		k1    = Vector.zerosDbl(this.nVar());
		k2    = Vector.zerosDbl(this.nVar());
		k3    = Vector.zerosDbl(this.nVar());
		kaux  = Vector.zerosDbl(this.nVar());
		hddFdT = Vector.zerosDbl(this.nVar());
		/* Matrices */
		dFdY  = Matrix.zerosDbl(this.nVar());
		W     = Matrix.zerosDbl(this.nVar());
	}
	
	// TODO init from xml node?
	
	/*************************************************************************
	 * KEY METHOS
	 ************************************************************************/
	
	@Override
	public double[] solve(double[] y, double tFinal) throws Exception, 
													IllegalArgumentException
	{
		/*
		 * Check the generic criteria, plus the Rosenbrock method needs the
		 * Jacobian method to be set.
		 */
		super.solve(y, tFinal);
		
		if ( this.nVar() == 0 )
			return y;
		/*
		 * Control statement in case the maximum timestep size, hMax, is too
		 * large.
		 */
		double absTol = this._absTol;
		double hMax = this._hMax;
		if ( hMax > tFinal )
		{
			absTol *= tFinal/hMax;
			hMax = tFinal;
		}
		/*
		 * First try a step size of hmax.
		 */
		t = 0.0;
		lastStep  = false;
		h = hMax;
		while ( ! lastStep )
		{
			/*
			 * If the next step gets us close to the end, we may as well
			 * just finish.
			 */
			if ( 1.05 * h >= tFinal - t )
			{
				h = tFinal - t;
				lastStep = true;
			}
			/*
			 * Update dFdT with a mini-timestep. The Jacobian matrix, dFdY,
			 * doesn't need this.
			 */
			this._deriv.secondDeriv(dFdT, y);
			Vector.timesTo(hddFdT, dFdT, h * d);
			this._deriv.jacobian(dFdY, y);
			/*
			 * Try out this value of h, keeping a note of whether it ever
			 * fails.
			 */
			noFailed = true;
			usingHMin = true;
			while ( true )
			{
				tNext = ( lastStep ) ? tFinal : t + h;
				/*
				 * The Rosenbrock method.
				 */
				try
				{
					/*
					 * W = I - h * d * dFdY
					 */
					Matrix.timesTo(W, dFdY, - h * d);
					for ( int i = 0; i < this.nVar(); i++ )
						W[i][i]++;
					test = Matrix.condition(W);
					if ( test > 10.0)
					{ 
						Log.out(Tier.CRITICAL,
							"Warning (ODEsolver): Condition of W is "+test);
					}
					/*
					 * Find k1 where
					 * W*k1 = dYdT + h*d*dFdT
					 */
					Vector.addTo(k1, hddFdT, dYdT);
					Matrix.solveEquals(W, k1);
					/*
					 * f1 = dYdT(y + k1*h/2)
					 * 
					 * Use f2 as temporary place holder.
					 */
					Vector.timesTo(f2, k1, 0.5*h);
					Vector.addEquals(f2, y);
					this._deriv.firstDeriv(f1, f2);
					/*
					 * Find k2 where
					 * W*(k2-k1) = f1 - k1  
					 */
					Vector.minusTo(k2, f1, k1);
					Matrix.solveEquals(W, k2);
					Vector.addEquals(k2, k1);
					/*
					 * These will be the new values of y and dYdT, assuming the
					 * error is small enough.
					 */ 
					/* ynext = y + h * k2 */
					Vector.timesTo(ynext, k2, h);
					Vector.addEquals(ynext, y);
					if ( ! this._allowNegatives )
						Vector.makeNonnegative(ynext);
					/* f2 = dYdT(ynext) */
					this._deriv.firstDeriv(f2, ynext);
					/*
					 * 
					 * Find k3 where 
					 * W*k3 = ( f2 + e32*(f1-k2) + 2*(y-k1) + h*d*dFdT )
					 * 
					 * First set kaux as the expression inside the brackets,
					 * then multiply by invW on the left.
					 */
					/* f2 + h*d*dFdT */
					Vector.addTo(kaux, hddFdT, f2);
					/* + e32 * (f1 - k2) */
					Vector.minusTo(k3, f1, k2);
					Vector.timesEquals(k3, e32);
					Vector.addEquals(kaux, k3);
					/* + 2 * (y - k1) */
					Vector.minusTo(k3, y, k1);
					Vector.timesEquals(k3, 2.0);
					Vector.addEquals(kaux, k3);
					/* k3 = W-1(...) */ 
					Matrix.solveTo(k3, W, kaux);
					/*
					 * Use kaux to estimate the greatest error:
					 * kaux = (k1 -2*k2 + k3) * h/6
					 */
					Vector.timesTo(kaux, k2, -2.0);
					Vector.addEquals(kaux, k3);
					Vector.addEquals(kaux, k1);
					Vector.timesEquals(kaux, h/6.0);
					error = Vector.max(kaux);
				}
				catch (Exception e)
				{
					Log.out(Tier.CRITICAL, "Problem in Rosenbrock step" + e);
				}
				/*
				 * The solution is accepted if the weighted error is less than
				 * the relative tolerance rtol. If the step fails, calculate a
				 * new h based on the standard rule for selecting a step size
				 * in numerical integration of initial value problems:
				 * h(n+1) = h(n) * ((rtol / error) ^ power).
				 * 
				 * 90% of this estimated value is then used in the next step to
				 * decrease the probability of further failures.
				 * 
				 * Reference:
				 * GEAR, C. W. 1971. Numerical Initial Value Problems in
				 * Ordinary Differential Equations. Prentice-Hall, Englewood
				 * Cliffs, N.J.
				 * 
				 * 
				 * Rob 16July2015: Moved the checking for negatives into this
				 * section on error checking.
				 */
				signsOK = ( ! this._allowNegatives ) || 
												Vector.isNonnegative(ynext);
				test = Math.pow((absTol/error), power);
				if ( error > absTol && signsOK )
				{ 
					noFailed = false;
					lastStep = false;
					if ( usingHMin )
						break;
					else if (EPS * t > h * 0.9 * test)
					{
						usingHMin = true;
						h = EPS * t;
					}
					else
						h *= 0.9 * test;
				}
				else
					break;
			} // End of `while ( true )`
			/*
			 * If there were no failures compute a new h. We use the same
			 * formula as before to compute a new step, h. But in addition, we
			 * adjust the next time step depending on how stiff the problem is.
			 * If the system is extremely stiff, the increase is limited to
			 * 1.2. Otherwise, the increase is set to a factor of 5.
			 * 
			 * Reference:
			 * Shampine LF. 1982. Implementation of Rosenbrock Methods.
			 * ACM Transactions on Mathematical Software. 8: 93-113.
			 */
			if ( noFailed )
				h = ( test < 1.2 ) ? test : h * 5;
			/*
			 * The upper limit of hmax still applies.
			 * Update the time.
			 */
			h = Math.min(h, hMax);
			t = tNext;
			/*
			 * Check if we've reached a steady-state solution.
			 * NOTE: do not use absTol when comparing y and ynext!
			 */
			// FIXME this is the cause of the solute concentrations disappearing
			// when there are no reactions or agents, etc
			//if( (h == hMax) && Vector.areSame(y, ynext) )
			//	return ynext;
			/*
			 * Update the y and the first derivative dYdT.
			 */
			Vector.copyTo(y, ynext);
			Vector.copyTo(dYdT, f2);
			/*
			 * If the method using this solver has anything it needs to update
			 * after every mini-timestep, then do this now.
			 */
			this._deriv.postMiniStep(y, h);
		} // End of `while ( ! lastStep )`
		return y;
	}
}
