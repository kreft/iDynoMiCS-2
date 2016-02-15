package solver;

import java.util.Arrays;

import dataIO.Log;
import dataIO.Log.tier;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ODErosenbrock extends ODEsolver
{
	/**
	 * TODO
	 */
	protected final double d = 1.0 / (2.0 + Math.sqrt(2.0));
	
	/**
	 * TODO
	 */
	protected final double e32  = 6.0 + Math.sqrt(2.0);
	
	/**
	 * The order of this method is 3.
	 */
	protected final double power = 1.0/3.0;
	
	/**
	 * Error Per Step: the smallest positive floating-point number such that
	 * 1.0 + EPS > 1.0 
	 */
	protected final double EPS = 2.22e-16;
	
	/**
	 * Numerical accuracy for EPS (error per step)
	 * 
	 * TODO is this used?
	 */
	protected final double sqrtE = Math.sqrt(EPS);
	
	/**
	 * Absolute tolerance.
	 * 
	 * <p><b>[Rob 7Aug2015]</b> Switched to absolute tolerance from relative
	 * tolerance due to error estimates heading to infinity as values approach
	 * zero.</p>
	 */
	protected double _absTol;
	
	/**
	 * Maximum time-step permissible.
	 */
	protected double _hMax;
	
	protected double[] ynext;
	
	protected double[] dYdT;
	
	protected double[] dFdT;
	
	protected double[][] dFdY;
	
	protected double[] f1, f2, k1, k2, k3, kaux, hddFdT;
	
	protected double[][] W, invW;
	
	protected double[][] identity;
	
	protected double t, error, tNext, h, test;
	
	protected boolean lastStep, noFailed, usingHMin, signsOK;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODErosenbrock()
	{

	}
	
	public ODErosenbrock(String[] soluteNames, boolean allowNegatives, double absTol, double hMax)
	{
		this.init(soluteNames, allowNegatives, absTol, hMax);
		Log.out(tier.DEBUG, "ODErosenbrock: " + allowNegatives +  " " +  absTol 
				+  " " +  hMax);
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param names
	 * @param allowNegatives
	 * @param absTol
	 * @param hMax
	 */
	public void init(String[] names, boolean allowNegatives,
												double absTol, double hMax)
	{
		/**
		 * NOTE Bas [12.02.16] seems ODErosenbrock does not like absTol < hMax
		 */
		hMax = ( absTol > hMax ? hMax : absTol);
		
		super.init(names, allowNegatives);
		
		this._absTol = absTol;
		this._hMax = hMax;
		
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
		
		dFdY  = Matrix.zerosDbl(this.nVar());
		W     = Matrix.zerosDbl(this.nVar());
		invW  = Matrix.zerosDbl(this.nVar());
		
		identity = Matrix.identityDbl(this.nVar());
	}
	
	/*************************************************************************
	 * KEY METHOS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * 
	 * @param y One-dimensional array of doubles.
	 * @param tFinal Time duration to solve for.
	 * @return One-dimensional array of doubles.
	 */
	@Override
	public double[] solve(double[] y, double tFinal) throws Exception, 
													IllegalArgumentException
	{
		/*
		 * Check the generic criteria, plus the Rosenbrock method needs the
		 * Jacobian method to be set.
		 */
		super.solve(y, tFinal);
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
			usingHMin = false;
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
					Matrix.addEquals(W, identity);
					test = Matrix.condition(W);
					if ( test > 10.0)
					{ 
						Log.out(tier.CRITICAL,
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
					 * ynext = y + h * k2
					 */
					Vector.timesTo(ynext, k2, h);
					Vector.addEquals(ynext, y);
					/*
					 * f2 = dYdT(ynext)
					 */
					this._deriv.firstDeriv(f2, ynext);
					/*
					 * 
					 * Find k3 where 
					 * W*k3 = ( f2 + e32*(f1-k2) + 2*(y-k1) + h*d*dFdT )
					 * 
					 * First set kaux as the expression inside the brackets,
					 * then multiply by invW on the left.
					 * 
					 * f2 + h*d*dFdT
					 */
					Vector.addTo(kaux, hddFdT, f2);
					/* + e32 * (f1 - k2) */
					Vector.minusTo(k3, f1, k2);
					Vector.timesEquals(k3, e32);
					Vector.addEquals(kaux, k3);
					/* + 2 * (y - k1) */
					Vector.minusTo(k3, y, k1);
					Vector.timesEquals(k3, 2.0);
					Vector.addEquals(kaux, k3);
					// 
					Matrix.solveTo(k3, W, kaux);
					/*
					 * We now use kaux to estimate the error of this step.
					 */
					//for (int i = 0; i < this.nVar(); i++)
					//	kaux[i] = 1/Math.min(y[i], ynext[i]);
					/*
					 * kaux *= (2*k2 + k3) * h / 6
					 */
					//Vector.add(Vector.times(k2, 2.0), k3);
					//Vector.times(kaux, Vector.times(k2, h/6.0));
					/*
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
					Log.out(tier.CRITICAL,"Problem in Rosenbrock step" + e);
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
				//Log.out(tier.NORMAL,"error = "+error+", absTol = "+absTol+", h = "+h);
				//return y; //Bughunt
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
			 * 
			 * TODO use the relative tolerance here? Could use another
			 * parameter and set it negative if we want never to escape early.
			 */
			if( (h == hMax) && Vector.areSame(y, ynext) )
				return ynext;
			/*
			 * Update the y and the first derivative dYdT.
			 */
			Vector.copyTo(y, ynext);
			Vector.copyTo(dYdT, f2);
		} // End of `while ( ! lastStep )`
		return y;
	}
}
