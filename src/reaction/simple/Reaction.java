package reaction.simple;

import linearAlgebra.Vector;
import reaction.simple.ReactionRate.*;

public class Reaction {

	protected double[] _stoichiometry;
	
	protected ReactionRate _rate;
	
	public Reaction(double[] stoichiometry, ReactionRate rate)
	{
		this._stoichiometry = stoichiometry;
		this._rate = rate;
	}
	
	public Reaction(double stoichiometry, ReactionRate rate)
	{
		this._stoichiometry = new double[]{stoichiometry};
		this._rate = rate;
	}
	
	private double[] conc(double[] concentrations)
	{
		double[] r = Vector.zerosDbl(concentrations.length);
		for(int i = 0; i < r.length; i++)
		{
			r[i] += _stoichiometry[i] * _rate.rateTerm(concentrations);
		}
		return r;
	}
	
	public double directMethod(double concentration, double dt)
	{
		return _rate.direct(concentration, dt);
	}
	
	/**
	 * ode returns the concentration after a given amount of time dt using an
	 * Ode method
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double[] ode(double[] concentration, ode method, double dt, 
			double tstep)
	{
		double ts = dt / Math.ceil(dt/tstep);
		double n = Math.rint(dt/ts);
		double[] c = Vector.copy(ReactionRate.noNeg(concentration));
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
		return ReactionRate.noNeg(c);
	}
	
	public double ode(double concentration, ode method, double dt, 
			double tstep)
	{
		return ode(new double[]{concentration}, method, dt, tstep)[0];
	}
	
	/**
	 * An euler step (mostly here for comparison)
	 * @param concentration
	 * @param dt
	 * @return
	 */
	public double[] eul(double[] concentration, double dt)
	{
		return Vector.add(concentration, Vector.times(conc(concentration), dt));
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
				conc(concentration), conc(k)) ,dt/2));
	}

	
}
