package reaction.simple;

public class Firstorder extends Reaction{
	
	final double k;
	
	public Firstorder(double k)
	{
		this.k = k;
	}

	public double rate(double concentration)
	{
		return -k*concentration;
	}
	
	public double C(double concentration, double dt)
	{
		return concentration * Math.exp(-k*dt);
	}

}
