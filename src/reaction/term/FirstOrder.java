package reaction.term;

public class FirstOrder implements RateTerm{
	
	final double k;
	private String[] S;
	
	public FirstOrder(double k)
	{
		this.k = k;
	}

	public double rateTerm(double[] concentration)
	{
		return k*RateTerm.noNeg(concentration[0]);
	}
	
	public double direct(double concentration, double dt)
	{
		return concentration * Math.exp(-k*dt);
	}

}
