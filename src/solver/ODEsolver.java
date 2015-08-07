/**
 * 
 */
package solver;

import java.util.function.Function;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public abstract class ODEsolver extends Solver 
{
	/**
	 * Number of variables in the solver.
	 */
	protected int _nVar;
	
	/**
	 * TODO Consider switching to a BiConsumer?
	 */
	protected Function<double[], double[]> _firstDeriv;
	
	/**
	 * TODO Consider switching to a BiConsumer?
	 */
	protected Function<double[], double[]> _secondDeriv;
	
	/**
	 * TODO Consider switching to a BiConsumer?
	 */
	protected Function<double[], double[][]> _jacobian;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public void init(String[] variableNames, boolean allowNegatives)
	{
		this._nVar = variableNames.length;
	}
	
	/*************************************************************************
	 * SETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param dYdT
	 */
	public void set1stDeriv(Function<double[], double[]> dYdT)
	{
		this._firstDeriv = dYdT;
	}

	/**
	 * \brief TODO
	 * 
	 * @param dFdT
	 */
	public void set2ndDeriv(Function<double[], double[]> dFdT)
	{
		this._secondDeriv = dFdT;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param jac
	 */
	public void setJacobian(Function<double[], double[][]> jac)
	{
		this._jacobian = jac;
	}
	
	/*************************************************************************
	 * SOLVER METHODS
	 ************************************************************************/
	/**
	 * \brief TODO
	 * 
	 * TODO Should choose a more specific subclass of Exception if no 1st
	 * derivative method set.
	 * 
	 * TODO Check tFinal is positive and finite?
	 * 
	 * @param y
	 * @param tFinal
	 * @return
	 * @throws Exception No first derivative set.
	 * @exception IllegalArgumentException Wrong vector dimensions.
	 */
	public double[] solve(double[] y, double tFinal) throws Exception, 
													IllegalArgumentException
	{
		if ( this._firstDeriv == null )
			throw new Exception("No first derivative set.");
		if ( y.length != _nVar )
			throw new IllegalArgumentException("Wrong vector dimensions.");
		return y;
	}
	
	/*************************************************************************
	 * DERIVATIVES
	 ************************************************************************/
	
	/**
	 * Update the first derivative of Y, i.e. the rate of change of Y with
	 * respect to time (dYdT = F).
	 * 
	 * @param y
	 */
	protected double[] calc1stDeriv(double[] y)
	{
		return this._firstDeriv.apply(y);
	}
	
	/**
	 * \brief Update the second derivative of Y, i.e. the rate of change of F
	 * with respect to time (dFdT).
	 * 
	 * <p>Tries the user-defined second-derivative first, but switched to a
	 * numerical method if this is not available.</p>
	 * 
	 * @param y 
	 * @param deltaT 
	 */
	protected double[] calc2ndDeriv(double[] y, double deltaT)
	{
		try
		{
			return this._secondDeriv.apply(y);
		}
		catch ( NullPointerException e)
		{
			System.out.println("\tFinding dFdT numerically"); //Bughunt
			double[] dYdT = calc1stDeriv(y);
			double[] out = Vector.copy(dYdT);
			/*
			 * yNext = y + (deltaT * dYdT)
			 */
			Vector.add(Vector.times(out, deltaT), y);
			System.out.println("\tyNext is"); //Bughunt
			for ( double elem : out)
				System.out.println(elem);
			/*
			 * dFdT = ( dYdT(ynext) - dYdT(y) )/tdel
			 */
			out = calc1stDeriv(out);
			System.out.println("\tDeriv there is"); //Bughunt
			for ( double elem : out)
				System.out.println(elem);
			out = Vector.subtract(out, dYdT);
			System.out.println("\tsubtract dYdT"); //Bughunt
			for ( double elem : out)
				System.out.println(elem);
			out = Vector.times(out, 1.0/deltaT);
			System.out.println("\tdivide by deltaT"); //Bughunt
			for ( double elem : out)
				System.out.println(elem);
			return out;
		}
	}
	
	/**
	 * Calculate the Jacobian matrix, i.e. the rate of change of F with
	 * respect to each of the variables in Y (dFdY).
	 * 
	 * <p>Tries the user-defined Jacobian, returning zeros if this is not
	 * available.</p>
	 * 
	 * @param y 
	 */
	protected double[][] calcJacobian(double[] y)
	{
		try
		{
			return this._jacobian.apply(y);
		}
		catch ( NullPointerException e)
		{
			return Matrix.zerosDbl(this._nVar);
		}
	}
}
