package solver;

import linearAlgebra.Vector;
import zArchivedClasses.RateTerm;

/**
 * 
 * @author baco
 *
 */
public class ODEheunsmethod extends ODEsolver {
	
	/**
	 * Maximum time-step permissible.
	 */
	protected double _hMax;
	
	protected double[] dYdT;
	
	public ODEheunsmethod(String[] names, boolean allowNegatives, double hMax)
	{
		this.init(names, allowNegatives, hMax);
	}
	
	public void init(String[] names, boolean allowNegatives, double hMax)
	{
		super.init(names, allowNegatives);
		this._hMax = hMax;
	}
	
	public double[] solve(double[] y, double tFinal) throws Exception, 
	IllegalArgumentException
	{
		double ts = tFinal / Math.ceil(tFinal/_hMax);
		double n = Math.rint(tFinal/ts);
		double[] c = Vector.copy(noNeg(y));
		for(int i = 0; i < n; i++)
			c = heun(c, tFinal/n);
		return noNeg(c);
	}
	
	public double[] noNeg(double[] c)
	{
		if(! _allowNegatives)
			for(double d : c)
				d = (d > 0.0 ? d : 0.0);
		return c;
	}

	public double[] eul(double[] y, double dt)
	{
		return Vector.add(y, Vector.times(this._deriv.firstDeriv(y), dt));
	}

	public double[] heun(double[] y, double dt)
	{
		double[] k = eul(y, dt);
		return Vector.add(y, Vector.times( Vector.add( 
				this._deriv.firstDeriv(y), this._deriv.firstDeriv(k)) ,dt/2));
	}
}
