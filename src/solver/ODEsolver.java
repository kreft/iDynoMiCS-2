/**
 * 
 */
package solver;

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
		double[] firstDeriv(double[] y);
		double[] secondDeriv(double[] y);
		double[][] jacobian(double[] y);
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
