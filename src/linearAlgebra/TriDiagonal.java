package linearAlgebra;

public final class TriDiagonal
{
	/**
	 * \brief Check the column numbers of a given <b>tridiag</b> matrix.
	 * 
	 * <p>Note that <b>tridiag</b> will be unaffected by this method.</p>
	 * 
	 * @param tridiag Two-dimensional array of doubles.
	 * @exception TriDiagonal matrix must have 3 columns.
	 */
	public static void checkTriDiag(double[][] tridiag)
	{
		if ( tridiag[0].length != 3 )
		{
			throw new IllegalArgumentException(
								"TriDiagonal matrix must have 3 columns.");
		}
		Matrix.checkDimensions(tridiag);
	}
	
	/**
	 * \brief Converts a square matrix to a tridiagonal matrix.
	 * 
	 * <p>Input <b>matrix</b> is unaffected by this method. If <b>matrix</b> is
	 * n-by-n, then the output will be a new n-by-3 matrix.</p>
	 * 
	 * <p>The output format is consistent within the TriDiagonal module. To
	 * convert back to a standard matrix, use 
	 * {@link #getMatrix(double[][])}.</p>
	 * 
	 * <p>Note that if there are any non-zero entries anywhere else in the
	 * <b>matrix</b>, they will simply be ignored. There is no automatic error
	 * checking here!</p>
	 * 
	 * <p>Corner elements are included for solving problems with periodic
	 * boundaries. For example, the <b>matrix</b><br>
	 * b<sub>0</sub>, c<sub>0</sub>, 0, 0, a<sub>0</sub>;<br>
	 * a<sub>1</sub>, b<sub>1</sub>, c<sub>1</sub>, 0, 0;<br>
	 * 0, a<sub>2</sub>, b<sub>2</sub>, c<sub>2</sub>, 0;<br>
	 * 0, 0, a<sub>3</sub>, b<sub>3</sub>, c<sub>3</sub>;<br>
	 * c<sub>4</sub>, 0, 0, a<sub>4</sub>, b<sub>4</sub>;<br>
	 * would be converted to<br>
	 * a<sub>0</sub>, b<sub>0</sub>, c<sub>0</sub>;<br>
	 * a<sub>1</sub>, b<sub>1</sub>, c<sub>1</sub>;<br>
	 * a<sub>2</sub>, b<sub>2</sub>, c<sub>2</sub>;<br>
	 * a<sub>3</sub>, b<sub>3</sub>, c<sub>3</sub>;<br>
	 * a<sub>4</sub>, b<sub>4</sub>, c<sub>4</sub>;</p>
	 * 
	 * @param matrix Two-dimensional array of doubles: n-by-n.
	 * @return Two-dimensional array of doubles: n-by-3.
	 */
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
		out[0][0] = matrix[0][m-1];
		out[m-1][2] = matrix[m-1][0];
		return out;
	}
	
	/**
	 * \brief Convert a tridiagonal matrix back into standard square matrix
	 * form.
	 * 
	 * <p>Note that <b>tridiag</p> will be unaffected by this method.</p>
	 * 
	 * @param tridiag Two-dimensional array of doubles: n-by-3.
	 * @return Two-dimensional array of doubles: n-by-n.
	 */
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
	 * \brief Solve a system of linear equations represented as a
	 * tridiagonal matrix <b>tridiag</b> * x = <b>vector</b>. There should be
	 * zeros in the non-diagonal corners of the original matrix.
	 * 
	 * <p>This code is an implementation of the Thomas Algorithm.</p>
	 * 
	 * <p>Note also that both inputs will be overwritten by this method.
	 * <b>tridiag</b> will not be useful any more, but <b>vector</b> will be
	 * overwritten with the solution.</p>
	 * 
	 * TODO check stability (|b| > |a| + |c|) for all rows of matrix ?
	 * 
	 * @param tridiag Two-dimensional array of doubles: n-by-3.
	 * @param vector One-dimensional array of doubles (length n).
	 * @return One-dimensional array of doubles (length n).
	 */
	private static double[] solveNonPeriodic(double[][] tridiag,
															double[] vector)
	{
		int m = Matrix.rowDim(tridiag);
		/*
		 * First scale the top row so that the (0, 0) element of the matrix is
		 * one (implicitly).
		 */
		double temp = tridiag[0][1];
		double gamma = tridiag[0][2] / temp ;
		double rho = vector[0] / temp;
		//matrix[0][1] = 1.0;
		tridiag[0][2] = gamma;
		vector[0] = rho;
		/*
		 * Loop forward through subsequent rows, (implicitly) eliminating the
		 * subdiagonal and making the main diagonal all ones.
		 */
		for ( int i = 1; i < m; i++ )
		{
			temp = (tridiag[i][1] - tridiag[i][0]*gamma);
			rho = (vector[i] - tridiag[i][0]*rho) / temp;
			gamma = tridiag[i][2] / temp;
			//matrix[i][0] = 0.0;
			//matrix[i][1] = 1.0;
			tridiag[i][2] = gamma;
			vector[i] = rho;
		}
		/*
		 * Loop backward, (implicitly) eliminating the superdiagonal and
		 * storing the system solution in vector.
		 */
		for ( int i = m - 1; i > 0; i-- )
		{
			vector[i-1] -= tridiag[i-1][2] * vector[i];
			//matrix[i-1][2] = 0.0;
		}
		return vector;
	}
	
	/**
	 * \brief Solve a system of linear equations represented as a
	 * tridiagonal matrix <b>tridiag</b> * x = <b>vector</b>.
	 * 
	 * <p>See {@link #getTriDiag(double[][])} on how to convert any square
	 * matrix into a tridiagonal matrix.</p>
	 * 
	 * <p>Note that <b>tridiag</b> and <b>vector</b> must have the same
	 * length.</p>
	 * 
	 * <p>Note also that <b>tridiag</b> will be unaffected by this method.
	 * <b>vector</b> will be overwritten with the solution.</p>
	 * 
	 * <p>The following sources were invaluable in writing this code (both
	 * accessed in July 2015):<br>
	 * http://www3.ul.ie/wlee/ms6021_thomas.pdf<br>
	 * http://www.cfm.brown.edu/people/gk/chap6/node14.html</p>
	 * 
	 * @param tridiag Two-dimensional array of doubles: n-by-3.
	 * @param vector One-dimensional array of doubles (length n). 
	 * @return One-dimensional array of doubles (length n).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 */
	public static double[] solve(double[][] tridiag, double[] vector)
	{
		checkTriDiag(tridiag);
		int m = Matrix.rowDim(tridiag);
		if ( vector.length != m )
		{
			throw new IllegalArgumentException(
										"Matrix row dimensions must agree.");
		}
		/*
		 * If this tridiagonal matrix is not periodic, just solve and return.
		 */
		if ( (tridiag[0][0] == 0.0) && (tridiag[m-1][2] == 0.0) )
			return solveNonPeriodic(Matrix.copy(tridiag), vector);
		/*
		 * Condensed matrix has the last row and last column removed, and is
		 * made non-periodic.
		 */
		double[][] condensed = Matrix.submatrix(tridiag, 0, m-1, 0, 3);
		condensed[0][0] = 0.0;
		condensed[m-2][2] = 0.0;
		/*
		 * 
		 */
		double[] vc1 = Vector.subset(vector, 0, m-1);
		double[] vc2 = Vector.zerosDbl(m-1);
		vc2[0]   -= tridiag[0][0];
		vc2[m-2] -= tridiag[m-2][2];
		/*
		 * Solve these two vectors separately, making sure to copy the 
		 * condensed matrix before it is modified.
		 */
		solveNonPeriodic(Matrix.copy(condensed), vc1);
		solveNonPeriodic(condensed, vc2);
		/*
		 * Solve the last element directly, and use it to find the rest.
		 */
		vector[m-1] = (vector[m-1] - tridiag[m-1][2]*vc1[0] 
												- tridiag[m-1][0]*vc1[m-2]) /
						(tridiag[m-1][1] + tridiag[m-1][2]*vc2[0] 
												+ tridiag[m-1][0]*vc2[m-2]);
		for ( int i = 0; i < m-1; i++)
			vector[i] = vc1[i] + vector[m-1] * vc2[i];
		return vector;
	}
}
