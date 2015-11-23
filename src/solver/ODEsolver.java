/**
 * 
 */
package solver;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public abstract class ODEsolver extends Solver 
{
	/**
	 * TODO
	 */
	public interface Derivatives
	{
		/**
		 * Small change in time or variable used by numerical methods for
		 * estimating the 2nd derivative or the Jacobian. 
		 */
		double delta = 1E-6;
		
		/**
		 * \brief You must specify the first derivative with respect to time.
		 * 
		 * @param y
		 * @return
		 */
		double[] firstDeriv(double[] y);
		
		/**
		 * \brief You may specify the second derivative with respect to time,
		 * but by default this is estimated numerically.
		 * 
		 * @param y
		 * @return
		 */
		default double[] secondDeriv(double[] y)
		{
			double[] dYdT = firstDeriv(y);
			/*
			 * yNext = y + (deltaT * dYdT)
			 */
			double[] dFdT = Vector.copy(dYdT);
			dFdT = Vector.timesEquals(dFdT, delta);
			dFdT = Vector.add(dFdT, y);
			/*
			 * dFdT = ( dYdT(ynext) - dYdT(y) )/tdel
			 */
			dFdT = firstDeriv(dFdT);
			dFdT = Vector.minus(dFdT, dYdT);
			dFdT = Vector.timesEquals(dFdT, 1.0/delta);
			return dFdT;
		};
		
		/**
		 * 
		 * @param y
		 * @return
		 */
		default double[][] jacobian(double[] y)
		{
			double[][] dFdY = Matrix.zerosDbl(y.length);
			//TODO numerical Jacobian estimation
			return dFdY;
		}
	};
	
	/**
	 * TODO
	 */
	protected Derivatives _deriv;
	
	/**
	 * 
	 */
	public void init(String[] variableNames, boolean allowNegatives)
	{
		super.init(variableNames, allowNegatives);
	}
	
	/**
	 * 
	 * @param deriv
	 */
	public void setDerivatives(Derivatives deriv)
	{
		this._deriv = deriv;
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO Should choose a more specific subclass of Exception if no 1st
	 * derivative method set.
	 * 
	 * TODO Check tFinal is positive and finite?
	 * 
	 * @param y
	 * @param tFinal
	 * @return
	 * @throws Exception No first derivative set.
	 * @exception IllegalArgumentException Wrong vector dimensions.
	 */
	public double[] solve(double[] y, double tFinal) throws Exception, 
													IllegalArgumentException
	{
		if ( this._deriv == null )
			throw new Exception("No derivatives set.");
		if ( y.length != this.nVar() )
			throw new IllegalArgumentException("Wrong vector dimensions.");
		return y;
	}
}
