package zArchivedClasses;

public class SimpleInhibition implements RateTerm {
	
	private double k;
	private String[] S;

	public SimpleInhibition(double k)
	{
		this.k = k;
	}
	/**
	 * inhibition term
	 */
	public double rateTerm(double[] concentration)
	{
		return k / (k + RateTerm.noNeg(concentration[0]));
	}
	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for inhibition term");
		return 0.0;
	}
}
