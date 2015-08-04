package solver;

import utility.LogFile;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class ODErosenbrock extends ODEsolver
{
	/**
	 * 
	 */
	protected final double d = 1.0 / (2.0 + Math.sqrt(2.0));
	
	/**
	 * 
	 */
	protected final double e32  = 6.0 + Math.sqrt(2.0);
	
	/**
	 * The order of this method is 3.
	 */
	protected final double power = 1.0/3.0;
	
	/**
	 * Numerical accuracy for EPS (error per step) 
	 */
	protected final double sqrtE = Math.sqrt(2.22e-16);
	
	/**
	 * Error Per Step: the smallest positive floating-point number such that
	 * 1.0 + EPS > 1.0 
	 */
	protected final double EPS = 2.22e-16;
	
	/**
	 * Relative tolerance.
	 */
	protected double _rTol;
	
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
	
	protected double t, error, tnext, h, test;
	
	protected boolean lastStep, noFailed, usingHMin, signsOK;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODErosenbrock()
	{
		
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	public void init(String[] names, boolean allowNegatives,
													double rTol, double hMax)
	{
		super.init(names, allowNegatives);
		
		this._rTol = rTol;
		this._hMax = hMax;
		
		ynext = Vector.zerosDbl(_nVar);
		dYdT  = Vector.zerosDbl(_nVar);
		dFdT  = Vector.zerosDbl(_nVar);
		f1    = Vector.zerosDbl(_nVar);
		f2    = Vector.zerosDbl(_nVar);
		k1    = Vector.zerosDbl(_nVar);
		k2    = Vector.zerosDbl(_nVar);
		k3    = Vector.zerosDbl(_nVar);
		kaux  = Vector.zerosDbl(_nVar);
		hddFdT = Vector.zerosDbl(_nVar);
		
		dFdY  = Matrix.zerosDbl(_nVar);
		W     = Matrix.zerosDbl(_nVar);
		invW  = Matrix.zerosDbl(_nVar);
		
		identity = Matrix.identityDbl(_nVar);
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
		if ( this._jacobian == null )
			throw new Exception("No first derivative set.");
		/*
		 * Control statement in case the maximum timestep size, hMax, is too
		 * large.
		 */
		double rTol = this._rTol;
		double hMax = this._hMax;
		if ( hMax > tFinal )
		{
			rTol *= tFinal/hMax;
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
			dFdT = calc2ndDeriv(y, sqrtE * ( t + h ));
			hddFdT = Vector.times(Vector.copy(dFdT), -h*d);
			dFdY = calcJacobian(y);
			/*
			 * Try out this value of h, keeping a note of whether it ever
			 * fails.
			 */
			noFailed = true;
			usingHMin = false;
			while ( true )
			{
				tnext = ( lastStep ) ? tFinal : t + h;
				/*
				 * The Rosenbrock method.
				 */
				try
				{
					/*
					 * W = I - h * d * dFdY
					 */
					W = Matrix.times(Matrix.copy(dFdY), -h*d);
					W = Matrix.add(W, identity);
					test = Matrix.condition(W);
					if ( test > 10.0)
					{ 
						LogFile.shoutLog(
							"Warning (ODEsolver): Condition of W is "+test);
					}
					/*
					 * Find k1 where
					 * W*k1 = dYdT + h*d*dFdT
					 */
					k1 = Matrix.solve(W,Vector.add(Vector.copy(hddFdT),dYdT));
					/*
					 * f1 = dYdT(y + k1*h/2)
					 */
					f1 = Vector.times(Vector.copy(k1), 0.5*h);
					f1 = calc1stDeriv( Vector.add(f1, y));
					/*
					 * Find k2 where
					 * W*(k2-k1) = f1 - k1  
					 */
					k2 = Vector.subtract(Vector.copy(f1), k1);
					k2 = Matrix.solve(W, k2);
					k2 = Vector.add(k2, k1);
					/*
					 * ynext = y + h * k2
					 */
					ynext = Vector.times(Vector.copy(k2), h);
					ynext = Vector.add(ynext, y);
					/*
					 * f2 = dYdT(ynext)
					 */
					f2 = calc1stDeriv(ynext);
					/*
					 * 
					 * Find k3 where 
					 * W*k3 = ( f2 + e32*(f1-k2) + 2*(y-k1) + h*d*dFdT )
					 * 
					 * First set kaux as the expression inside the brackets,
					 * then multiply by invW on the left.
					 */
					kaux = Vector.add(Vector.copy(hddFdT), f2);
					k3 = Vector.subtract(Vector.copy(f1), k2);
					kaux = Vector.add(kaux, Vector.times(k3, e32));
					k3 = Vector.subtract(Vector.copy(y), k1);
					kaux = Vector.add(kaux, Vector.times(k3, 2.0));
					k3 = Matrix.solve(W, kaux);
					/*
					 * We now use kaux to estimate the error of this step.
					 */
					for (int i = 0; i < _nVar; i++)
						kaux[i] = 1/Math.min(y[i], ynext[i]);
					/*
					 * kaux *= (2*k2 + k3) * h / 6
					 */
					Vector.add(Vector.times(k2, 2.0), k3);
					Vector.times(kaux, Vector.times(k2, h/6.0));
					error = Vector.max(kaux);
				}
				catch (Exception e)
				{
					LogFile.writeError("Problem in Rosenbrock step", e);
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
				test = Math.pow((rTol/error), power);
				if ( error > rTol && signsOK )
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
				LogFile.writeLog("error = "+error+", rTol = "+rTol+", h = "+h);
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
			t = tnext;
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
			y = Vector.copy(ynext);
			dYdT = Vector.copy(f2);
		} // End of `while ( ! lastStep )`
		/*
		 * Finally, return the answer.
		 */
		return y;
	}
}
