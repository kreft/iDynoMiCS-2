/**
 * 
 */
package solver;

import linearAlgebra.Vector;

/**
 * \brief Abstract class of the time derivatives used by any {@code ODEsolver}.
 * 
 * <p>The absolutely essential method here is
 * {@link #firstDeriv(double[], double[])}: this must be defined by any class
 * wishing to use an {@code ODEsolver}.</p> 
 * 
 * <p>Overwriting {@link #secondDeriv(double[], double[])} and/or
 * {@link #jacobian(double[][], double[])} is optional: it may help the solver
 * run faster if it has an analytic, more accurate calculation of these
 * quantities. However, any mistakes introduced here are the fault of the
 * developer who overwrote the numerical estimates!</p> 
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
	
	/**
	 * \brief Set the small change in time or variable used by numerical
	 * methods for estimating the 2nd derivative or the Jacobian. 
	 * 
	 * @param delta New value to use.
	 */
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
	 * @param destination One-dimensional array of {@code double}s that will
	 * be overwritten with the result.
	 * @param y One-dimensional array of {@code double}s with the current
	 * values of the variables in the system.
	 */
	public abstract void firstDeriv(double[] destination, double[] y);
	
	/**
	 * \brief You may specify the second derivative with respect to time,
	 * but by default this is estimated numerically.
	 * 
	 * @param destination One-dimensional array of {@code double}s that will
	 * be overwritten with the result.
	 * @param y One-dimensional array of {@code double}s with the current
	 * values of the variables in the system.
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
		double[] ynext = Vector.times(dYdT, this._delta);
		Vector.addEquals(ynext, y);
		/*
		 * dFdT = ( dYdT(ynext) - dYdT(y) )/tdel
		 */
		firstDeriv(destination, ynext);
		Vector.minusEquals(destination, dYdT);
		Vector.timesEquals(destination, 1.0/this._delta);
	};
	
	/**
	 * \brief You may specify the Jacobian matrix with respect to time,
	 * but by default this is estimated numerically.
	 * 
	 * @param destination Two-dimensional array of {@code double}s that will
	 * be overwritten with the result.
	 * @param y One-dimensional array of {@code double}s with the current
	 * values of the variables in the system.
	 */
	public void jacobian(double[][] destination, double[] y)
	{
		/* Temporary vector to be perturbed by one variable at a time. */
		double[] ynext = Vector.copy(y);
		/* The first derivative of the system with respect to time. */
		double[] dFdT = new double[y.length];
		this.firstDeriv(dFdT, y);
		/*
		 * Iterate over all variables, estimating the effect on each variable
		 * in a forward-Euler like fashion.
		 */
		double[] dFdY = new double[y.length];
		for ( int i = 0; i < y.length; i++ )
		{
			ynext[i] += this._delta;
			this.firstDeriv(dFdY, ynext);
			Vector.minusEquals(dFdY, dFdT);
			Vector.timesEquals(dFdY, 1/this._delta);
			Vector.copyTo(destination[i], dFdY);
			ynext[i] = y[i];
		}
	}
}