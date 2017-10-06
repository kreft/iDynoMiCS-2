/**
 * 
 */
package solver;

import linearAlgebra.Vector;

/**
 * \brief Abstract class of solver for set of Ordinary Differential Equations
 * (ODEs). 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class ODEsolver extends Solver 
{
	/**
	 * Method for calculating the derivatives of the variables, with respect to
	 * the common in dependent variable (e.g. time).
	 */
	protected ODEderivatives _deriv;
	
	/**
	 * Set the method for calculating the derivatives of the variables, with
	 * respect to the common in dependent variable (e.g. time).
	 * 
	 * @param deriv Sub-class of the abstract class {@code ODEderivatives}.
	 */
	public void setDerivatives(ODEderivatives deriv)
	{
		this._deriv = deriv;
	}
	
	/**
	 * \brief TODO
	 * 
	 * FIXME [Bas] do we really need throws here? we can also simply print a 
	 * warning.
	 * 
	 * TODO Should choose a more specific subclass of Exception if no 1st
	 * derivative method set.
	 * 
	 * TODO Check tFinal is positive and finite?
	 * 
	 * @param y One-dimensional array of doubles.
	 * @param tFinal Time duration to solve for.
	 * @return One-dimensional array of doubles.
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
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);
		return y;
	}
}
