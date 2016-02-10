package reaction.simple;

/**
 * general reaction class
 * @author baco
 *
 */
public interface ReactionRate {
	
	public enum ode {
		EULER,
		HEUN
	}

	/**
	 * reaction rate at given concentration
	 * @param concentration
	 * @return
	 */
	public abstract double rateTerm(double[] concentration);
	
	public abstract double direct(double concentration, double dt);
	
	/**
	 * Negative concentrations don't make any sense, sets negative input to 0.0
	 * used by reaction rate expressions
	 */
	public static double noNeg(double input)
	{
		return (input >= 0.0 ? input : 0.0);
	}
	
	public static double[] noNeg(double[] input)
	{
		for(int i = 0; i < input.length; i++)
			input[i] = noNeg(input[i]);
		return input;
	}

}
