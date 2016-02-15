package solver;

import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ODEheunsmethod extends ODEsolver
{
	
	/**
	 * Maximum time-step permissible.
	 */
	protected double _hMax;
	
	/**
	 * Temporary vectors.
	 */
	protected double[] dYdT, k, dKdT;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ODEheunsmethod(String[] names, boolean allowNegatives, double hMax)
	{
		this.init(names, allowNegatives, hMax);
	}
	
	public void init(String[] names, boolean allowNegatives, double hMax)
	{
		super.init(names, allowNegatives);
		this._hMax = hMax;
		this.dYdT = new double[names.length];
		this.k = new double[names.length];
		this.dKdT = new double[names.length];
	}
	
	/*************************************************************************
	 * KEY METHOS
	 ************************************************************************/
	
	public double[] solve(double[] y, double tFinal) 
									throws Exception, IllegalArgumentException
	{
		/*
		 * Check the input vector is acceptable.
		 */
		super.solve(y, tFinal);
		/*
		 * Solve the system.
		 */
		double timeStep;
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);
		while ( tFinal > 0.0 )
		{
			timeStep = Math.min(this._hMax, tFinal);
			heun(y, timeStep);
			if ( ! this._allowNegatives )
				Vector.makeNonnegative(y);
			tFinal -= timeStep;
		}
		return y;
	}
	
	/**
	 * \brief Apply a forward-Euler step.
	 * 
	 * @param destination One-dimensional array of {@code double}s which will
	 * be overwritten with the result.
	 * @param y One-dimensional array of {@code double}s with the current set
	 * of values (preserved).
	 * @param dt Time step.
	 */
	protected void euler(double[] destination, double[] y, double dt)
	{
		this._deriv.firstDeriv(destination, y);
		Vector.timesEquals(destination, dt);
		Vector.addEquals(destination, y);
	}
	
	/**
	 * \brief Apply a step of Heun's method.
	 * 
	 * @param y One-dimensional array of {@code double}s with the current set
	 * of values - this will be overwritten with the result.
	 * @param dt Time step.
	 */
	protected void heun(double[] y, double dt)
	{
		euler(this.k, y, dt);
		this._deriv.firstDeriv(dYdT, y);
		this._deriv.firstDeriv(dKdT, this.k);
		Vector.addEquals(dYdT, dKdT);
		Vector.timesEquals(dYdT, dt/2);
		Vector.addEquals(y, dYdT);
	}
}
