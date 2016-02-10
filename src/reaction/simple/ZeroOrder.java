package reaction.simple;

public class ZeroOrder implements ReactionRate {
	
	final double k;
	
	public ZeroOrder(double k)
	{
		this.k = k;
	}

	public double rateTerm(double[] concentration)
	{
		return -k;
	}
	
	public double direct(double concentration, double dt)
	{
		return -k*dt + concentration;
	}

}
