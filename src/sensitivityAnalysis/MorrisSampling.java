package sensitivityAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import optimization.sampling.Sampler;
import utility.ExtraMath;

/**
 * Morris sampling follows the procedure described by: 
 * 
 * Max D. Morris. 1991. Factorial sampling plans for preliminary computational 
 * experiments. Technometrics 33, 2 (April 1991), 161-174. 
 * DOI: http://dx.doi.org/10.2307/1269043
 * 
 * As well as by:
 * 
 * Sin, Gürkan, Anne S. Meyer, and Krist V. Gernaey. "Assessing reliability of 
 * cellulose hydrolysis models to support biofuel process design—Identifiability
 * and uncertainty analysis." Computers & Chemical Engineering 34.9 (2010): 
 * 1385-1392.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */

public class MorrisSampling extends Sampler {
	
	private int _k, _p, _r;
	
	public MorrisSampling() 
	{
		/* This constructor intentionally left blank */
	}
	
	public MorrisSampling( int k, int p, int r ) 
	{
		this._k = k;
		this._p = p;
		this._r = r;
	}
	
	public int size()
	{
		return _r*(_k+1);
	}
	
	/**
	 * \brief Morris method for creating sample distribution probabilities 
	 * (between 0 and 1).
	 * @param k Integer value for number of input parameters to be changed
	 * @param p Integer value for the number of levels.
	 * @param r Integer value for the number of repetitions.
	 */
	public double[][] morrisProbs( int k, int p, int r ) 
	{
		this._k = k;
		this._p = p;
		this._r = r;
		return sample() ;
	}
	
	/**
	 * \brief Morris method for creating sample distribution probabilities 
	 * (between 0 and 1).
	 * @param _k Integer value for number of input parameters to be changed
	 * @param _p Integer value for the number of levels.
	 * @param _r Integer value for the number of repetitions.
	 * 
	 */
	@Override
	public double[][] sample() 
	{
		/* initialise random number generator */
		if( !ExtraMath.isAvailable() )
			ExtraMath.initialiseRandomNumberGenerator();
		
		double delta = _p/(2.0*(_p-1));
		double[][] B = new double[_k+1][_k];
		double[][] J = Matrix.matrix(_k+1, _k, 1.0);
		for (int row = 0; row < _k+1; row++) {
			for (int col = 0; col < _k; col++) {
				if (row <= col)
					B[row][col] = 0;
				else
					B[row][col] = 1;
			}
		}		
		
		List<Double> xRange = new ArrayList<Double>();
		for (Double val = 0.0; val <= (1-delta); val=val+(1.0/(_p-1.0))) {
			xRange.add(val);
		}
		int m = xRange.size();
		
		double X[][] = new double[_r*(_k+1)][_k];
		
		for (int idx = 0; idx < _r; idx++) 
		{
			int[][] D = Matrix.identityInt(_k);
			
			/* 
			 * NOTE: it is important to use the iDynoMiCS random number
			 * generator as this allows us to produce the same result given a
			 * random seed
			 */
			double[] probs = Vector.randomZeroOne( _k ); 
			int[] randInts = Vector.randomInts( _k, 0, m );
			double[] xVals = new double[_k];
			
			int[][] idMatrix = Matrix.identityInt(_k);
			int[][] permIdMatrix = new int[_k][_k];
			
			/* construct a shuffled list of integers from 0 to k-1. */
			List<Integer> rowNums = new ArrayList<Integer>();
			for (int row = 0; row < _k; row++)
			{
				rowNums.add(row);
			}
			/* Implementing Fisher-Yates shuffle */
			for (int row = _k; row > 1; row--)
			{
				Collections.swap(rowNums, row-1, ExtraMath.getUniRandInt(row));
			}
			
			/* generate random input matrix */
			for (int row = 0; row < _k; row++) 
			{
				for (int col = 0; col < _k; col++) 
				{
				/* step 1: make a diagonal matrix with integer values of 1 or -1 
				 * selected with equal probability. */
					if (row == col && probs[row] < 0.5) 
					{
						D[row][col] = -1;
					}
				}
				
				/* step 2: select randomly from discrete uniform distribution 
				 * with p levels for each input factors k. */
				xVals[row] = xRange.get(randInts[row]);
				
				/* step 3: randomly permuted identity matrix */
				permIdMatrix[row] = idMatrix[rowNums.get(row)];
			}
			
			//first Expression: J[:,1]*xVals
			double[] Jcol = Matrix.getColumn(J, 0);
			double[][] firstExpr = Vector.outerProduct(Jcol, xVals);
			
			//second Expression: (delta/2)*( (2*B-J)*D + J ) 
			double[][] secondExpr = Matrix.times(Matrix.add(
					Matrix.times( Matrix.minus( Matrix.times(B, 2), J ), 
							Matrix.toDbl( D ) ), J ), delta/2 );
			
			// Bp = (first Expression + second Expression)*permIdMatrix
			double[][] Bp = Matrix.times( Matrix.add( firstExpr, secondExpr ), 
					Matrix.toDbl( permIdMatrix ) );
			for (int row = 0; row < _k+1; row++) 
			{
				for (int col = 0; col < _k; col++) 
				{
					X[ row+( idx*( _k+1 ) ) ][col] = Bp[row][col];
				}
			}
		}
		return X;
	}
}
