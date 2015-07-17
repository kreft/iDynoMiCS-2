package array;

public final class TriDiagonal
{
	
	public static double[][] getTriDiag(double[][] matrix)
	{
		Matrix.checkSquare(matrix);
		int m = Matrix.rowDim(matrix);
		double[][] out = new double[m][3];
		for ( int i = 0; i < m-1; i++ )
		{
			out[i+1][0] = matrix[i+1][i];
			out[i][1] = matrix[i][i];
			out[i][2] = matrix[i][i+1];
		}
		out[m-1][1] = matrix[m-1][m-1];
		return out;
	}
	
	public static double[][] getTriDiagPeriodic(double[][] matrix)
	{
		double[][] out = getTriDiag(matrix);
		int m = Matrix.rowDim(matrix);
		out[0][0] = matrix[0][m-1];
		out[m-1][2] = matrix[m-1][0];
		return out;
	}
	
	public static double[][] getMatrix(double[][] tridiag)
	{
		int m = Matrix.rowDim(tridiag);
		double[][] matrix = Matrix.zerosDbl(m);
		for ( int i = 0; i < m-1; i++ )
		{
			matrix[i+1][i] = tridiag[i+1][0];
			matrix[i][i] = tridiag[i][1];
			matrix[i][i+1] = tridiag[i][2];
		}
		matrix[m-1][m-1] = tridiag[m-1][1];
		matrix[0][m-1] = tridiag[0][0];
		matrix[m-1][0] = tridiag[m-1][2];
		return matrix;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * Thomas algorithm
	 * 
	 * TODO number of columns must be 3
	 * 
	 * TODO length of vector must be m
	 * 
	 * http://www3.ul.ie/wlee/ms6021_thomas.pdf
	 * TODO check stability (|b| > |a| + |c|) for all rows of matrix ?
	 * 
	 * 
	 * Assumes matrix[0][0] = matrix[m-1][2] = 0.0
	 * i.e. no periodic boundaries
	 *  
	 * 
	 * @param matrix
	 * @param vector
	 * @return
	 */
	public static double[] solve(double[][] matrix, double[] vector)
	{
		int m = Matrix.rowDim(matrix);
		/*
		 * First scale the top row so that the (0, 0) element of the matrix is
		 * one (implicitly).
		 */
		double temp = matrix[0][1];
		double gamma = matrix[0][2] / temp ;
		double rho = vector[0] / temp;
		//matrix[0][1] = 1.0;
		matrix[0][2] = gamma;
		vector[0] = rho;
		/*
		 * Loop forward through subsequent rows, (implicitly) eliminating the
		 * subdiagonal and making the main diagonal all ones.
		 */
		for ( int i = 1; i < m; i++ )
		{
			temp = (matrix[i][1] - matrix[i][0]*gamma);
			rho = (vector[i] - matrix[i][0]*rho) / temp;
			gamma = matrix[i][2] / temp;
			//matrix[i][0] = 0.0;
			//matrix[i][1] = 1.0;
			matrix[i][2] = gamma;
			vector[i] = rho;
		}
		/*
		 * Loop backward, (implicitly) eliminating the superdiagonal and
		 * storing the system solution in vector.
		 */
		for ( int i = m - 1; i > 0; i-- )
		{
			vector[i-1] -= matrix[i-1][2] * vector[i];
			//matrix[i-1][2] = 0.0;
		}
		return vector;
	}
	
	/**
	 * TODO
	 * 
	 * http://www.cfm.brown.edu/people/gk/chap6/node14.html
	 * 
	 * @param matrix
	 * @param vector
	 * @return
	 */
	public static double[] solvePeriodic(double[][] matrix, double[] vector)
	{
		int m = Matrix.rowDim(matrix);
		/*
		 * Condensed matrix has the last row and last column removed, and is
		 * made non-periodic.
		 */
		double[][] condensed = Matrix.submatrix(matrix, 0, m-1, 0, 3);
		condensed[0][0] = 0.0;
		condensed[m-2][2] = 0.0;
		/*
		 * 
		 */
		double[] vc1 = Vector.subset(vector, 0, m-1);
		double[] vc2 = Vector.zerosDbl(m-1);
		vc2[0]   -= matrix[0][0];
		vc2[m-2] -= matrix[m-1][2];
		
		for ( int i = 0; i < m-1; i++ )
			System.out.println(condensed[i][0]+" "+condensed[i][1]+" "+condensed[i][2]+"\t\t"+vc1[i]+"\t"+vc2[i]);
		System.out.println("condensed\t\tvc1\tvc2\n");
		
		//double[][] conM = getMatrix(condensed);
		/*double[] solve1 = Vector.copy(vc1);
		Matrix.solve(conM, solve1);
		for ( int i = 0; i < m-1; i++ )
			System.out.println(solve1[i]);
		System.out.println("^ solve1 ^");
		*/
		/*
		 * Solve these two vectors separately, making sure to copy the 
		 * condensed matrix before it is modified.
		 */
		solve(Matrix.copy(condensed), vc1);
		solve(condensed, vc2);
		//vc1 = Matrix.times(conM, vc1);
		//for ( int i = 0; i < m-1; i++ )
		//	System.out.println(vc1[i]);
		//System.out.println("^ solve1 ^");
		//for ( int i = 0; i < m-1; i++ )
		//	System.out.println(vc1[i]+"\t\t"+vc2[i]);
		//System.out.println("");
		/*
		 * 
		 */
		vector[m-1] = (vector[m-1] - matrix[m-1][2]*vc1[0] 
							- matrix[m-1][0]*vc1[m-2]) /
						(matrix[m-1][1] + matrix[m-1][2]*vc2[0] 
								+ matrix[m-1][0]*vc1[m-2]);
		for ( int i = 0; i < m-1; i++)
			vector[i] = vc1[i] + vector[m-1] * vc2[i];
		
		return vector;
	}
}
