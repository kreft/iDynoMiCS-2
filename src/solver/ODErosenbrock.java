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
		if ( this._jacobian == null )
			throw new Exception("No first derivative set.");
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
		 * 
		 */
		dYdT = calc1stDeriv(y);
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
			/*System.out.println("dFdT is"); //Bughunt
			for ( double elem : dFdT)
				System.out.println(elem);*/
			hddFdT = Vector.times(Vector.copy(dFdT), h*d);
			/*System.out.println("h*d*dFdT is"); //Bughunt
			for ( double elem : hddFdT)
				System.out.println(elem);*/
			dFdY = calcJacobian(y);
			/*System.out.println("dFdY is"); //Bughunt
			for ( double[] row : dFdY ) 
			{
				for ( double elem : row)
					System.out.print(elem+", ");
				System.out.println("");
			}*/
			/*
			 * Try out this value of h, keeping a note of whether it ever
			 * fails.
			 */
			noFailed = true;
			usingHMin = false;
			while ( true )
			{
				tNext = ( lastStep ) ? tFinal : t + h;
				//System.out.println("-> Trying from "+t+" to "+tNext); //Bughunt
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
					/*System.out.println("\tW is"); //Bughunt
					for ( double[] row : W ) 
					{
						System.out.print("\t\t");
						for ( double elem : row)
							System.out.print(elem+", ");
						System.out.println("");
					}*/
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
					k1 = Vector.add(Vector.copy(hddFdT),dYdT);
					/*System.out.println("dYdT + h*d*dFdT is"); //Bughunt
					for ( double elem : k1)
						System.out.println(elem);*/
					k1 = Matrix.solve(W, k1);
					/*System.out.println("k1 is"); //Bughunt
					for ( double elem : k1)
						System.out.println(elem);*/
					/*
					 * f1 = dYdT(y + k1*h/2)
					 */
					f1 = Vector.times(Vector.copy(k1), 0.5*h);
					/*System.out.println("\tK1*h/2 is"); //Bughunt
					for ( double elem : f1)
						System.out.println(elem);*/
					f1 = calc1stDeriv( Vector.add(f1, y));
					/*System.out.println("f1 is"); //Bughunt
					for ( double elem : f1)
						System.out.println(elem);*/
					/*
					 * Find k2 where
					 * W*(k2-k1) = f1 - k1  
					 */
					k2 = Vector.subtract(Vector.copy(f1), k1);
					/*System.out.println("f1 - k1 is"); //Bughunt
					for ( double elem : k2)
						System.out.println(elem);*/
					k2 = Matrix.solve(W, k2);
					/*System.out.println("(f1 - k1)/W is"); //Bughunt
					for ( double elem : k2)
						System.out.println(elem);*/
					k2 = Vector.add(k2, k1);
					/*System.out.println("k2 is"); //Bughunt
					for ( double elem : k2)
						System.out.println(elem);*/
					/*
					 * ynext = y + h * k2
					 */
					ynext = Vector.times(Vector.copy(k2), h);
					ynext = Vector.add(ynext, y);
					/*System.out.println("ynext is"); //Bughunt
					for ( double elem : ynext)
						System.out.println(elem);*/
					/*
					 * f2 = dYdT(ynext)
					 */
					f2 = calc1stDeriv(ynext);
					/*System.out.println("f2 is"); //Bughunt
					for ( double elem : f2)
						System.out.println(elem);*/
					/*
					 * 
					 * Find k3 where 
					 * W*k3 = ( f2 + e32*(f1-k2) + 2*(y-k1) + h*d*dFdT )
					 * 
					 * First set kaux as the expression inside the brackets,
					 * then multiply by invW on the left.
					 */
					kaux = Vector.add(Vector.copy(hddFdT), f2);
					/*System.out.println("f2 + h*d*dFdT is"); //Bughunt
					for ( double elem : kaux)
						System.out.println(elem);*/
					k3 = Vector.subtract(Vector.copy(f1), k2);
					/*System.out.println("f2 + h*d*dFdT is"); //Bughunt
					for ( double elem : kaux)
						System.out.println(elem);*/
					kaux = Vector.add(kaux, Vector.times(k3, e32));
					k3 = Vector.subtract(Vector.copy(y), k1);
					kaux = Vector.add(kaux, Vector.times(k3, 2.0));
					k3 = Matrix.solve(W, kaux);
					/*
					 * We now use kaux to estimate the error of this step.
					 */
					for (int i = 0; i < this.nVar(); i++)
						kaux[i] = 1/Math.min(y[i], ynext[i]);
					/*System.out.println("1/min(y, ynext) is"); //Bughunt
					for ( double elem : kaux)
						System.out.println(elem);*/
					/*
					 * kaux *= (2*k2 + k3) * h / 6
					 */
					//Vector.add(Vector.times(k2, 2.0), k3);
					//Vector.times(kaux, Vector.times(k2, h/6.0));
					
					/*
					 * kaux = (k1 -2*k2 + k3) * h/6
					 */
					kaux = Vector.times(k2, -2.0);
					Vector.add(kaux, k3);
					Vector.add(kaux, k1);
					Vector.times(kaux, h/6.0);
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
				LogFile.writeLog("error = "+error+", absTol = "+absTol+", h = "+h);
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
			y = Vector.copy(ynext);
			dYdT = Vector.copy(f2);
		} // End of `while ( ! lastStep )`
		/*
		 * Finally, return the answer.
		 */
		return y;
	}
}
