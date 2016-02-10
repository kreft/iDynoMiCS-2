package reaction.simple;

public class Monod implements ReactionRate{

	final double k;
	private double muMax;
	
	/**
	 * Monod constructor
	 * @param muMax
	 * @param k
	 */
	public Monod(double muMax, double k)
	{
		this.k = k;
		this.muMax = muMax;
	}

	/**
	 * growth rate at concentration
	 */
	public double rateTerm(double[] concentration)
	{
		return - (muMax * ReactionRate.noNeg(concentration[0]))/ (k + ReactionRate.noNeg(concentration[0]));
	}
	
	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for Monod equation");
		return 0.0;
	}
}
