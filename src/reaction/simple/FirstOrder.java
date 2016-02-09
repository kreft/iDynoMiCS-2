package reaction.simple;

public class FirstOrder extends Reaction{
	
	final double k;
	
	public FirstOrder(double k)
	{
		this.k = k;
	}

	public double rate(double concentration)
	{
		return -k*concentration;
	}
	
	public double conc(double concentration, double dt)
	{
		return concentration * Math.exp(-k*dt);
	}

}
