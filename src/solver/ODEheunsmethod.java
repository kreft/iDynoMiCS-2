package solver;

import linearAlgebra.Vector;

/**
 * \brief Numerical solver for systems of Ordinary Differential Equations
 * (ODEs).
 * 
 * <p>Also known as improved or modified Euler's method, and closely related to
 * two-stage second-order Runge-Kutta methods.</p>
 * 
 * TODO Rob[15Feb2016]: Is there an error estimate for this method?
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ODEheunsmethod extends ODEsolver
{
	/**
	 * Maximum time-step permissible.
	 */
	private double hMax;
	/**
	 * Temporary vector, set and used by the solver.
	 */
	private double[] dYdT, k, dKdT;
	
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
		this.hMax = hMax;
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
		double timeRemaining = tFinal;
		while ( timeRemaining > 0.0 )
		{
			timeStep = Math.min(this.hMax, tFinal);
			heun(y, timeStep);
			if ( ! this._allowNegatives )
				Vector.makeNonnegative(y);
			timeRemaining -= timeStep;
			/*
			 * If the method using this solver has anything it needs to update
			 * after every mini-timestep, then do this now.
			 */
			this._deriv.postMiniStep(y, timeStep);
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
	
	protected void euler(double[] y, double dt)
	{
		this._deriv.firstDeriv(this.k, y);
		Vector.timesEquals(this.k, dt);
		Vector.addEquals(y, this.k);
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);
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
		/* Any (intermediate) minor negative concentration, mass or volume can 
		 * result in very weird behavior  */
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);
		euler(this.k, y, dt);
		this._deriv.firstDeriv(dYdT, y);
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(this.k);
		this._deriv.firstDeriv(dKdT, this.k);
		Vector.addEquals(dYdT, dKdT);
		Vector.timesEquals(dYdT, dt/2);
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);
		Vector.addEquals(y, dYdT);
		if ( ! this._allowNegatives )
			Vector.makeNonnegative(y);

	}
}
