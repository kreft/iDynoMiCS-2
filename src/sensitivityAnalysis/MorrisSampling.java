package sensitivityAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.*;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import utility.ExtraMath;

/**
 * Morris sampling follows the procedure described in: 
 * 
 * Max D. Morris. 1991. Factorial sampling plans for preliminary computational 
 * experiments. Technometrics 33, 2 (April 1991), 161-174. 
 * DOI: http://dx.doi.org/10.2307/1269043
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */

public class MorrisSampling {

	public MorrisSampling() 
	{
		/* This constructor intentionally left blank */
	}
	
	/**
	 * \brief Morris method for creating sample distribution probabilities 
	 * (between 0 and 1).
	 * @param k Integer value for number of input parameters to be changed
	 * @param p Integer value for the number of levels.
	 * @param r Integer value for the number of repetitions.
	 */
	public static double[][] morrisProbs( int k, int p, int r ) 
	{
		/* initialise random number generator */
		ExtraMath.initialiseRandomNumberGenerator();
		
		double delta = p/(2.0*(p-1));
		double[][] B = new double[k+1][k];
		double[][] J = Matrix.matrix(k+1, k, 1.0);
		for (int row = 0; row < k+1; row++) {
			for (int col = 0; col < k; col++) {
				if (row <= col)
					B[row][col] = 0;
				else
					B[row][col] = 1;
			}
		}		
		
		List<Double> xRange = new ArrayList<Double>();
		for (Double val = 0.0; val <= (1-delta); val=val+(1.0/(p-1.0))) {
			xRange.add(val);
		}
		int m = xRange.size();
		
		double X[][] = new double[r*(k+1)][k];
		
		for (int idx = 0; idx < r; idx++) 
		{
			int[][] D = Matrix.identityInt(k);
			
			/* 
			 * NOTE: it is important to use the iDynoMiCS random number
			 * generator as this allows us to produce the same result given a
			 * random seed
			 */
			double[] probs = Vector.randomZeroOne( k ); 
			int[] randInts = Vector.randomInts( k, 0, m );
			double[] xVals = new double[k];
			
			int[][] idMatrix = Matrix.identityInt(k);
			int[][] permIdMatrix = new int[k][k];
			
			/* construct a shuffled list of integers from 0 to k-1. */
			List<Integer> rowNums = new ArrayList<Integer>();
			for (int row = 0; row < k; row++)
			{
				rowNums.add(row);
			}
			/* Implementing Fisher-Yates shuffle */
			for (int row = k; row > 1; row--)
			{
				Collections.swap(rowNums, row-1, ExtraMath.getUniRandInt(row));
			}
			
			/* generate random input matrix */
			for (int row = 0; row < k; row++) 
			{
				for (int col = 0; col < k; col++) 
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
			for (int row = 0; row < k+1; row++) 
			{
				for (int col = 0; col < k; col++) 
				{
					X[ row+( idx*( k+1 ) ) ][col] = Bp[row][col];
				}
			}
		}
		return X;
	}
	
	/**
	 * \brief Morris method for creating sample distribution within provided 
	 * range.
	 * @param k Integer value for number of input parameters to be changed
	 * @param p Integer value for the number of levels.
	 * @param r Integer value for the number of repetitions.
	 * @param elementParameters List of XML elements from master protocol file
	 */
	public static double[][] morrisSamples( int k, int p, int r, List<Element> elementParameters ) 
	{
		double[][] discreteUniformProbs = morrisProbs(k,p,r);
		double[] ones = Vector.onesDbl(r*(k+1));

		double[] inpMax = new double[k];
		double[] inpMin = new double[k];
		
		String[] rangeAspectNames = new String[k];
		String[][] ranges = new String[k][2];
		
		for (Element currAspect : elementParameters) 
		{
			int idx = elementParameters.indexOf( currAspect );
			rangeAspectNames[idx] = currAspect.getAttribute( 
					XmlRef.nameAttribute );
			/* TODO Vector.dblFromString(vectorString) */
			String[] inRange = currAspect.getAttribute(
					XmlRef.rangeAttribute ).split(",");
			inRange = XmlCreate.checkRange(inRange);
			inpMax[idx] = Double.parseDouble(inRange[1]);
			inpMin[idx] = Double.parseDouble(inRange[0]);
			ranges[idx] = inRange.clone();
			XmlCreate.csvHeader += currAspect.getAttribute( XmlRef.nameAttribute )
					+ ( (idx == (elementParameters.size() - 1)) ? "" : ", ");
		}
		
		/* Variable states: ( ones*inpMin + 
		 * ( ones*( inpMax-inpMin ) ).*discreteUniformProbs )
		 */
		double[][] states = Matrix.add(Vector.outerProduct(ones, inpMin),
				Matrix.elemTimes(Vector.outerProduct(ones, 
						Vector.minus(inpMax, inpMin) ), discreteUniformProbs) );
		return states;
		
	}
}
