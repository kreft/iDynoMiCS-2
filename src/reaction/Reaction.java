package reaction;

import linearAlgebra.Vector;
import reaction.term.RateTerm;
import reaction.term.RateTerm.*;

public class Reaction {

	public static enum ode {
		EULER,
		HEUN
	}

	protected double[] _stoichiometry;
	
	protected RateTerm _rate;
	
	public Reaction(double[] stoichiometry, RateTerm rate)
	{
		this._stoichiometry = stoichiometry;
		this._rate = rate;
	}
	
	public Reaction(double stoichiometry, RateTerm rate)
	{
		this._stoichiometry = new double[]{stoichiometry};
		this._rate = rate;
	}
	
	public double[] rate(double[] concentrations)
	{
		double[] r = Vector.zerosDbl(concentrations.length);
		for(int i = 0; i < r.length; i++)
		{
			r[i] -= _stoichiometry[i] * _rate.rateTerm(concentrations);
		}
		return r;
	}
	
	public double directMethod(double concentration, double dt)
	{
		return _rate.direct(concentration, dt);
	}
	
	/**************************************************
	 * NOTE ode methods will be moved out of this class
	 */
	
	/**
	 * ode returns the concentration after a given amount of time dt using an
	 * Ode method
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double[] ode(double[] concentration, Reaction.ode method, double dt, 
			double tstep)
	{
		double ts = dt / Math.ceil(dt/tstep);
		double n = Math.rint(dt/ts);
		double[] c = Vector.copy(RateTerm.noNeg(concentration));
		for(int i = 0; i < n; i++)
		{
			switch (method)
			{
			case EULER: c = eul(c, dt/n);
				break;
			case HEUN: c = heun(c, dt/n);
				break;
			}
		}
		return RateTerm.noNeg(c);
	}
	
	/**
	 * ode returns the concentration after a given amount of time dt using an
	 * Ode method
	 * @param concentration
	 * @param method
	 * @param dt
	 * @param tstep
	 * @return
	 */
	public double ode(double concentration, Reaction.ode method, double dt, 
			double tstep)
	{
		return ode(new double[]{concentration}, method, dt, tstep)[0];
	}
	
	/**
	 * An euler step (here for comparison)
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double[] eul(double[] concentration, double dt)
	{
		return Vector.add(concentration, Vector.times(rate(concentration), dt));
	}
	
	/**
	 * An heun's method step (second order runga kutta).
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double[] heun(double[] concentration, double dt)
	{
		double[] k = eul(concentration, dt);
		return Vector.add(concentration, Vector.times( Vector.add( 
				rate(concentration), rate(k)) ,dt/2));
	}

	
}
