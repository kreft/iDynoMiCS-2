package reaction.term;

public class Monod implements RateTerm{

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
		return - (muMax * RateTerm.noNeg(concentration[0]))/ (k + RateTerm.noNeg(concentration[0]));
	}
	
	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for Monod equation");
		return 0.0;
	}
}
