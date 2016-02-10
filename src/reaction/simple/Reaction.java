package reaction.simple;

/**
 * general reaction class
 * @author baco
 *
 */
public abstract class Reaction {
	
	public enum ode {
		EULER,
		HEUN
	}

	/**
	 * reaction rate at given concentration
	 * @param concentration
	 * @return
	 */
	public abstract double rate(double concentration);
	
	/**
	 * Negative concentrations don't make any sense, sets negative input to 0.0
	 * used by reaction rate expressions
	 */
	public double noNeg(double input)
	{
		return (input >= 0.0 ? input : 0.0);
	}
	
	/**
	 * ode returns the concentration after a given amount of time dt using an
	 * Ode method
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double ode(double concentration, ode method, double dt, 
			double tstep)
	{
		double ts = dt / Math.ceil(dt/tstep);
		double n = Math.rint(dt/ts);
		double c = concentration;
		for(int i = 0; i < n; i++)
		{
			switch (method)
			{
			case EULER: c = eul(noNeg(c), dt/n);
				break;
			case HEUN: c = heun(noNeg(c), dt/n);
				break;
			}
		}
		return c;
	}
	
	/**
	 * An euler step (mostly here for comparison)
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double eul(double concentration, double dt)
	{
		return concentration + rate(concentration) * dt;
	}
	
	/**
	 * An heun's method step (second order runga kutta).
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double heun(double concentration, double dt)
	{
		double c = concentration + rate(concentration) * dt;
		return concentration + (rate(concentration) + rate(c)) * dt/2.0;
	}
}
