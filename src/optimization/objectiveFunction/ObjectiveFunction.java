package optimization.objectiveFunction;

/**
 * \brief public interface for objective functions or loss functions used to 
 * score the fit of x with data.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface ObjectiveFunction {

	/**
	 * \brief set measured data.
	 * 
	 * @param data vector
	 */
	public void setData( double[][] data );
	
	/**
	 * \brief evaluate loss function and score the fit of x with the set data.
	 * 
	 * @param x vector of output data corresponding to measured data vector
	 * @return loss score
	 */
	public double loss( double[] x );
}
