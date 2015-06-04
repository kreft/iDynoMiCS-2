package utility;


import utility.LogFile;
import Jama.Matrix;

public abstract class ODEsolver
{
	/**
	 * 
	 */
	protected Double d = 1.0 / (2.0 + Math.sqrt(2.0));
	
	/**
	 * 
	 */
	protected Double e32  = 6.0 + Math.sqrt(2.0);
	
	/**
	 * The order of this method is 3.
	 */
	protected Double power = 1.0/3.0;
	
	/**
	 * Numerical accuracy for EPS (error per step) 
	 */
	protected Double sqrtE = Math.sqrt(2.22e-16);
	
	/**
	 * Error Per Step: the smallest positive floating-point number such that
	 * 1.0 + EPS > 1.0 
	 */
	protected Double EPS = 2.22e-16;
	
	/**
	 * Number of variables in the solver.
	 */
	protected int nVar;
	
	/**
	 * 
	 */
	protected Boolean allowNegatives;
	
	protected Matrix ynext;
	
	protected Matrix dYdT;
	
	protected Matrix dFdT;
	
	protected Matrix dFdY;
	
	protected Matrix f1, f2, W, invW, k1, k2, k3, kaux;
	
	protected Double t, error, tnext, tdel, h, test;
	
	protected Boolean lastStep, noFailed, usingHMin;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODEsolver()
	{
		
	}
	
	public void init(int nVar)
	{
		this.nVar = nVar;
		
		ynext = new Matrix(nVar, 1, 0.0);
		dYdT  = new Matrix(nVar, 1, 0.0);
		dFdT  = new Matrix(nVar, 1, 0.0);
		dFdY  = new Matrix(nVar, nVar, 0.0);
		f1    = new Matrix(nVar, 1, 0.0);
		f2    = new Matrix(nVar, 1, 0.0);
		W     = new Matrix(nVar, nVar, 0.0);
		invW  = new Matrix(nVar, nVar, 0.0);
		k1    = new Matrix(nVar, 1, 0.0);
		k2    = new Matrix(nVar, 1, 0.0);
		k3    = new Matrix(nVar, 1, 0.0);
		kaux  = new Matrix(nVar, 1, 0.0);
	}
	
	/*************************************************************************
	 * KEY METHOS
	 ************************************************************************/
	
	/**
	 * 
	 * @param y
	 * @param tfinal
	 * @param rtol
	 * @param hmax
	 * @return
	 */
	public Matrix solve(Matrix y, Double tfinal, Double rtol, Double hmax)
	{
		/*
		 * First check that y is the correct size.
		 */
		if ( y.getRowDimension() != nVar || y.getColumnDimension() != 1 )
			throw new IllegalArgumentException("Wrong matrix dimensions");
		/*
		 * Reset the matrices we will need.
		 */
		
		/*
		 * Control statement in case the maximum timestep size, hmax, is too
		 * large.
		 */
		while ( hmax > tfinal )
		{
			hmax *= 0.5;
			rtol *= 0.5;
		}
		/*
		 * First try a step size of hmax.
		 */
		t = 0.0;
		lastStep  = false;
		h = hmax;
		while ( ! lastStep )
		{
			/*
			 * If the next step gets us close to the end, we may as well
			 * just finish.
			 */
			if ( 1.05 * h >= tfinal - t )
			{
				h = tfinal - t;
				lastStep = true;
			}
			/*
			 * Update dFdT based on the mini-timestep tdel. The Jacobian
			 * matrix, dFdY, doesn't need this mini-timestep.
			 */
			tdel = sqrtE * (t+h);
			dFdT = calc2ndDeriv(y, tdel);
			dFdY = calcJacobian(y);
			/*
			 * Try out this value of h, keeping a note of whether it ever
			 * fails.
			 */
			noFailed = true;
			usingHMin = false;
			while ( true )
			{
				tnext = ( lastStep ) ? tfinal : t + h;
				/*
				 * The Rosenbrock method.
				 */
				try
				{
					/*
					 * W = I - h * d * dFdY
					 */
					W = dFdY.times( - h * d );
					W.plusEquals( Matrix.identity(nVar, nVar) );
					if (W.cond() > 10)
					{ 
						LogFile.shoutLog(
								"Warning (ODEsolver): Condition of W is "+W.cond());
					}
					invW = W.inverse();
					/*
					 * k1 = invW * ( dYdT(y) + h * d * dFdT )
					 */
					k1 = invW.times( dFdT.times(h*d).plus(dYdT) );
					/*
					 * f1 = dYdT(y + k1*h/2)
					 */
					f1 = calc1stDeriv( k1.times(h/2).plus(y) );
					/*
					 * k2 = invW * ( f1 - k1 ) + k1
					 */
					k2 = invW.times( f1.minus(k1) );
					k2.plusEquals(k1);
					/*
					 * ynext = y + h * k2
					 */
					ynext = k2.times(h).plus(y);
					/*
					 * f2 = dYdT(ynext)
					 */
					f2 = calc1stDeriv(ynext);
					/*
					 * k3 = invW * ( f2 - e32*(k2-f1) - 2*(k1-y) + h*d*dFdT )
					 * 
					 * First set kaux as the expression inside the brackets,
					 * then multiply by invW on the left.
					 */
					kaux = f2.copy();
					kaux.minusEquals( k2.minus(f1).times(e32) );
					kaux.minusEquals( k1.minus(y).times(2) );
					kaux.plusEquals( dFdT.times(h*d));
					k3 = invW.times(kaux);
					/*
					 * We now use kaux to estimate the error of this step.
					 */
					for (int i = 0; i < nVar; i++)
						kaux.set(i, 0, 1/Math.min(y.get(i,0), ynext.get(i,0)));
					kaux.arrayTimesEquals(
								k1.minus(k2.times(2)).plus(k3).times(h/6));
					error = 0.0;
					for (int i = 0; i < nVar; i++)
						error = Math.max(error, kaux.get(i,0));
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
				 */
				test = Math.pow((rtol/error), power);
				if ( error > rtol )
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
				LogFile.writeLog("error = "+error+", rtol = "+rtol+", h = "+h);
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
			h = Math.min(h, hmax);
			t = tnext;
			/*
			 * Check no variables have gone negative.
			 * TODO Rob 4June2015: This could be done better.
			 */
			if ( ! allowNegatives )
				for (int i = 0; i < nVar; i++)
					if ( ynext.get(i, 0) < 0.0)
					{
						ynext.set(i, 0, 0.0);
						LogFile.shoutLog(
								"Warning (ODE solver): negative variable! "+i);
					}
			/*
			 * Update the y and the first derivative dYdT.
			 */
			y = ynext.copy();
			dYdT = f2.copy();
		} // End of `while ( ! lastStep )`
		/*
		 * Finally, return the answer.
		 */
		return y;
	}
	
	
	
	
	/**
	 * Update the first derivative of Y, i.e. the rate of change of Y with
	 * respect to time (dYdT = F).
	 * 
	 * @param y
	 */
	public abstract Matrix calc1stDeriv(Matrix y);
	
	/**
	 * Update the second derivative of Y, i.e. the rate of change of F with
	 * respect to time (dFdT).
	 * 
	 * @param y 
	 * @param tdel 
	 */
	public abstract Matrix calc2ndDeriv(Matrix y, Double tdel);
	
	/**
	 * Update the Jacobian matrix, i.e. the rate of change of F with respect to
	 * each of the variables in Y (dFdY).
	 * 
	 * @param y 
	 */
	public abstract Matrix calcJacobian(Matrix y);
	
}
