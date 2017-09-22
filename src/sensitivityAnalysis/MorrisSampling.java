package sensitivityAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;

class MorrisSampling {

	public MorrisSampling() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * \brief Morris method for creating sample distribution within provided range.
	 * @param k Integer value for number of input parameters to be changed
	 * @param p Integer value for the number of levels.
	 * @param r Integer value for the number of repetitions.
	 */
	public static double[][] morris(int k, int p, int r) {
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
		
		for (int idx = 0; idx < r; idx++) {
			int[][] D = Matrix.identityInt(k);
			double[] probs = ThreadLocalRandom.current().doubles(k, 0, 1).toArray();
					//Vector.randomZeroOne(k); 
			
			int[] randInts = ThreadLocalRandom.current().ints(m, 0, k).toArray();
					//Vector.randomInts(m, 0, k);
			double[] xVals = new double[k];
			
			int[][] idMatrix = Matrix.identityInt(k);
			List<Integer> rowNums = new ArrayList<Integer>();
			int[][] permIdMatrix = new int[k][k];
			
			for (int row = 0; row < k; row++) {
				rowNums.add(row);
			}
			Collections.shuffle(rowNums);
			
			for (int row = 0; row < k; row++) {
				for (int col = 0; col < k; col++) {
				// step 1: make a diagonal matrix with integer values of 1 or -1 selected with equal probability.
					if (row == col && probs[row] < 0.5) {
						D[row][col] = -1;
					}
				}
				
				// step 2: select randomly from discrete uniform distribution with p levels for each input factors k 
				xVals[row] = xRange.get(randInts[row]);
				
				// step 3: randomly permuted identity matrix
				permIdMatrix[row] = idMatrix[rowNums.get(row)];
			}
			//first Expression: J[:,1]*xVals
			double[] Jcol = Matrix.getColumn(J, 0);
			double[][] firstExpr = Vector.outerProduct(Jcol, xVals);
			
			//second Expression: (delta/2)*( (2*B-J)*D + J ) 
			double[][] secondExpr = Matrix.times(Matrix.add(
					Matrix.times(Matrix.minus(Matrix.times(B, 2), J), Matrix.toDbl(D)), J), delta/2);
			
			// Bp = (first Expression + second Expression)*permIdMatrix
			double[][] Bp = Matrix.times(Matrix.add(firstExpr, secondExpr), Matrix.toDbl(permIdMatrix));
			for (int row = 0; row < k+1; row++) {
				for (int col = 0; col < k; col++) {
					X[row+(idx*(k+1))][col] = Bp[row][col];
				}
			}
		}
		return X;
	}

}
