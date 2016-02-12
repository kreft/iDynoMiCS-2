package zArchivedClasses;

import java.util.HashMap;

import linearAlgebra.Vector;

/**
 * 
 * @author baco
 *
 */
public class Reaction {

	public static enum ode {
		EULER,
		HEUN
	}
	
	protected HashMap<String,Double> _stoichiometry = new HashMap<String,Double>();

	protected double[] stoichiometry;
	
	protected RateTerm _rate;
	
	public Reaction(double[] stoichiometry, String[] compounds, RateTerm rate)
	{
		this.stoichiometry = stoichiometry;
		this._rate = rate;
		for(int i = 0; i < stoichiometry.length; i++)
			_stoichiometry.put(compounds[i], stoichiometry[i]);
	}
	
	public Reaction(double stoichiometry, String compound, RateTerm rate)
	{
		this(new double[]{stoichiometry}, new String[]{compound}, rate);
	}
	
	public String reaction()
	{
		String r = "";
		for(String k : _stoichiometry.keySet())
		{
			if(_stoichiometry.get(k) != 0.0)
				r = r + (_stoichiometry.get(k) > 0.0 ? "+" : "" ) + 
					_stoichiometry.get(k) + " " + k + " ";
		}
		return r;
	}
	
	public double[] rate(double[] concentrations)
	{
		double[] r = Vector.zerosDbl(concentrations.length);
		for(int i = 0; i < r.length; i++)
		{
			r[i] = stoichiometry[i] * _rate.rateTerm(concentrations);
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
