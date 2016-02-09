package reaction.simple;

public class MichaelisMenten extends Reaction {
	
	final double k;
	final double Vm;
	
	public MichaelisMenten(double k, double maxRate)
	{
		this.k = k;
		this.Vm = maxRate;
	}

	public double rate(double concentration)
	{
		return - (Vm * concentration)/ (k + concentration);
	}
	
	public double conc(double concentration, double dt)
	{
		/**
		 * Goličnik, M. (2011). Exact and approximate solutions for the 
		 * decades-old Michaelis-Menten equation: Progress-curve analysis 
		 * through integrated rate equations. Biochemistry and Molecular 
		 * Biology Education, 39(2), 117–125. doi:10.1002/bmb.20479
		 */
		return k * LambertW( (concentration / k) * 
				Math.exp( (concentration - Vm * dt) / k) );
	}
	
	/////////////////////////////////////////////////////////
	// The following block commes from cab1729/functions.java
	// https://gist.github.com/cab1729/1318030
	// Source is unclear
	//////////////////////////////////////////////////////////

	private static double PRECISION = 1E-12;

	public static double LambertW(double z)
	{
		double S = 0.0;
		for (int n=1; n <= 100; n++)
		{
			double Se = S * StrictMath.pow(StrictMath.E, S);
			double S1e = (S+1) * 
				StrictMath.pow(StrictMath.E, S);
			if (PRECISION > StrictMath.abs((z-Se)/S1e))
				return S;
			S -= (Se-z) / (S1e - (S+2) * (Se-z) / (2*S+2));
		}
		return S;
	}
	
	///////////////////////////////////////////////////////
}
