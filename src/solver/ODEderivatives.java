/**
 * 
 */
package solver;

import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class ODEderivatives
{
	/**
	 * Small change in time or variable used by numerical methods for
	 * estimating the 2nd derivative or the Jacobian. 
	 */
	private double _delta = 1E-6;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODEderivatives()
	{
		
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	public void setDelta(double delta)
	{
		this._delta = delta;
	}
	
	/*************************************************************************
	 * DERIVATIVES
	 ************************************************************************/
	
	/**
	 * \brief You must specify the first derivative with respect to time.
	 * 
	 * @param y
	 * @return
	 */
	public abstract void firstDeriv(double[] destination, double[] y);
	
	/**
	 * \brief You may specify the second derivative with respect to time,
	 * but by default this is estimated numerically.
	 * 
	 * @param y
	 * @return
	 */
	public void secondDeriv(double[] destination, double[] y)
	{
		/*
		 * Get the first time derivative at this y. 
		 */
		double[] dYdT = new double[y.length];
		firstDeriv(dYdT, y);
		/*
		 * yNext = y + (deltaT * dYdT)
		 */
		double[] ynext = Vector.times(dYdT, _delta);
		Vector.addEquals(ynext, y);
		/*
		 * 
		 * dFdT = ( dYdT(ynext) - dYdT(y) )/tdel
		 */
		firstDeriv(destination, ynext);
		Vector.minusEquals(destination, dYdT);
		Vector.timesEquals(destination, 1.0/_delta);
	};
	
	/**
	 * \brief TODO
	 * 
	 * @param y
	 * @return
	 */
	public void jacobian(double[][] destination, double[] y)
	{
		/* Temporary vector to be perturbed by one variable at a time. */
		double[] ynext = Vector.copy(y);
		/* The . */
		double[] dFdT = new double[y.length];
		this.firstDeriv(dFdT, y);
		double[] dFdY = new double[y.length];
		for ( int i = 0; i < y.length; i++ )
		{
			ynext[i] += _delta;
			this.firstDeriv(dFdY, ynext);
			Vector.minusEquals(dFdY, dFdT);
			Vector.timesEquals(dFdY, 1/_delta);
			Vector.copyTo(destination[i], dFdY);
			ynext[i] = y[i];
		}
		
	}
}