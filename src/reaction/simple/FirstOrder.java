package reaction.simple;

public class FirstOrder implements ReactionRate{
	
	final double k;
	
	public FirstOrder(double k)
	{
		this.k = k;
	}

	public double rateTerm(double[] concentration)
	{
		return -k*ReactionRate.noNeg(concentration[0]);
	}
	
	public double direct(double concentration, double dt)
	{
		return concentration * Math.exp(-k*dt);
	}

}
