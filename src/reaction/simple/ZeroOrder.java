package reaction.simple;

public class ZeroOrder extends Reaction {
	
	final double k;
	
	public ZeroOrder(double k)
	{
		this.k = k;
	}

	public double rate(double concentration)
	{
		return -k;
	}
	
	public double conc(double concentration, double dt)
	{
		return -k*dt + concentration;
	}

}
