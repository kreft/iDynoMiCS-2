package reaction.term;

public class ZeroOrder implements RateTerm {
	
	final double k;
	private String[] S;
	
	public ZeroOrder(double k)
	{
		this.k = k;
	}

	public double rateTerm(double[] concentration)
	{
		return k;
	}
	
	public double direct(double concentration, double dt)
	{
		return RateTerm.noNeg(-k*dt + concentration);
	}

}
