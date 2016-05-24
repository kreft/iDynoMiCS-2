/**
 * 
 */
package linearAlgebra;

/**
 * \brief Cholesky Decomposition is a method for breaking a matrix into two.
 * This is useful for solving systems of linear equations.
 * 
 * <p>For a symmetric, positive definite matrix A, the Cholesky decomposition
 * is an lower triangular matrix L so that A = L*L<sup>T</sup>. If the matrix
 * is not symmetric or positive definite, the constructor returns a partial
 * decomposition and sets an internal flag that may be queried by the isSPD()
 * method.</p>
 * 
 * <p>
 * A matrix is 
 * <ul>
 * <li>symmetric if it is equal to its transpose: A = A<sup>T</sup></li>
 * <li>positive definite if the scalar z<sup>T</sup>*A*z is positive for every
 * non-zero column vector z of real numbers</li>
 * </ul>
 * Cholesky Decomposition is useful, e.g., for efficient numerics of Monte
 * Carlo simulations: it is roughly twice as efficient as LU Decomposition.
 * <i>(Source: Wikipedia)</i>
 * </p>
 * 
 * <p>This class is largely taken from the JAMA package, and merely modified by
 * Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.</p>
 * 
 * <p><b>JAMA Copyright Notice</b>: This software is a cooperative product of 
 * The MathWorks and the National Institute of Standards and Technology (NIST) 
 * which has been released to the public domain. Neither The MathWorks nor NIST
 * assumes any responsibility whatsoever for its use by other parties, and 
 * makes no guarantees, expressed or implied, about its quality, reliability, 
 * or any other characteristic.</p>
 */
public class CholeskyDecomposition
{
	/** 
	 * Array for internal storage of decomposition.
	 */
	private double[][] L;
	/** 
	 * Row and column dimension (square matrix).
	 */
	private int n;
	/** 
	 * Symmetric and positive definite flag.
	 */
	private boolean isspd;
	
	public CholeskyDecomposition (double[][] matrix)
	{
		this.L = Matrix.zeros(matrix);
		this.n = Matrix.rowDim(matrix);
		/* A matrix must be square to be symmetric. */
		this.isspd = Matrix.isSquare(matrix);
		
		for ( int j = 0; j < this.n; j++ )
		{
			double[] Lrowj = this.L[j];
			double d = 0.0;
			for ( int k = 0; k < j; k++ )
			{
				double[] Lrowk = this.L[k];
				double s = 0.0;
				for ( int i = 0; i < k; i++ )
					s += Lrowk[i]*Lrowj[i];
				Lrowj[k] = s = (matrix[j][k] - s)/this.L[k][k];
				d = d + s*s;
				/* A symmetric matrix is mirrored about the diagonal . */
				this.isspd = this.isspd && (matrix[k][j] == matrix[j][k]); 
			}
			d = matrix[j][j] - d;
			/* Check the matrix is positive definite.*/
			this.isspd = this.isspd && (d > 0.0);
			this.L[j][j] = Math.sqrt( Math.max(d, 0.0) );
			for (int k = j+1; k < this.n; k++)
				this.L[j][k] = 0.0;
		}
	}
	
	/**
	 * @return {@code true} if the given matrix is symmetric and positive
	 * definite, {@code false} if it is asymmetric and/or not positive definite.
	 */
	public boolean isSymmetricPositiveDefinite()
	{
		return this.isspd;
	}
	
	/**
	 * @return Triangular factor matrix.
	 */
	public double[][] getL()
	{
		return this.L;
	}
	
	/**
	 * \brief Solve the system of linear equations represented by three
	 * matrices: <b>matrix</b>, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the matrix to be found.
	 * 
	 * @param destination Two-dimensional array of {@code double}s to be filled
	 * with the result (overwritten).
	 * @param b Two-dimensional array of {@code double}s (preserved).
	 * @see #solveTo(double[][], double[][])
	 * @see #solve(double[][])
	 */
	public void solveTo(double[][] destination, double[][] b)
	{
		Matrix.copyTo(destination, b);
		int nx = Matrix.colDim(destination);
		/* Solve L*Y = B */
		for (int k = 0; k < this.n; k++)
			for (int j = 0; j < nx; j++)
			{
				for (int i = 0; i < k ; i++)
					destination[k][j] -= destination[i][j]*this.L[k][i];
				destination[k][j] /= this.L[k][k];
			}
		/* Solve L'*X = Y */
		for (int k = this.n-1; k >= 0; k--)
			for (int j = 0; j < nx; j++)
			{
				for (int i = k+1; i < this.n ; i++)
					destination[k][j] -= destination[i][j]*this.L[i][k];
				destination[k][j] /= this.L[k][k];
			}
	}
	
	/**
	 * \brief Solve the system of linear equations represented by three
	 * matrices: <b>matrix</b>, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the matrix to be found.
	 * 
	 * @param b Two-dimensional array of {@code double}s (preserved).
	 * @return New {@code double[][]} of x.
	 * @see #solveTo(double[][], double[][])
	 * @see #solve(double[][])
	 */
	public double[][] solve(double[][] b)
	{
		double[][] x = new double[b.length][b[0].length];
		solveTo(x, b);
		return x;
	}
	
	/**
	 * \brief Solve the system of linear equations represented by three
	 * matrices: <b>matrix</b>, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the matrix to be found.
	 * 
	 * @param b Two-dimensional array of {@code double}s (overwritten).
	 * @see #solveTo(double[][], double[][])
	 * @see #solve(double[][])
	 */
	public void solveEquals(double[][] b)
	{
		solveTo(b, b);
	}
	
	/**
	 * \brief Solve the system of linear equations represented by a
	 * <b>matrix</b> and two vectors, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the vector to be found.
	 * 
	 * @param destination One-dimensional array of {@code double}s to be filled
	 * with the result (overwritten).
	 * @param b One-dimensional array of {@code double}s (preserved).
	 * @see #solveTo(double[], double[])
	 * @see #solveEquals(double[])
	 */
	public void solveTo(double[] destination, double[] b)
	{
		if ( b.length != this.n )
		{
			throw new IllegalArgumentException(
					"Matrix row dimensions must agree.");
		}
		if ( ! this.isspd )
		{
			throw new RuntimeException(
					"Matrix is not symmetric positive definite.");
		}
		/* Copy right-hand side (RHS) */
		Vector.copyTo(destination, b);
		/* Solve L*Y = B */
		for (int k = 0; k < this.n; k++)
		{
			for (int i = 0; i < k ; i++)
				destination[k] -= destination[i]*this.L[k][i];
			destination[k] /= this.L[k][k];
		}
		/* Solve L'*X = Y */
		for (int k = this.n-1; k >= 0; k--)
		{
			for (int i = k+1; i < this.n ; i++)
				destination[k] -= destination[i]*this.L[i][k];
			destination[k] /= this.L[k][k];
		}
	}
	
	/**
	 * \brief Solve the system of linear equations represented by a
	 * <b>matrix</b> and two vectors, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the vector to be found.
	 * 
	 * @param b One-dimensional array of {@code double}s (preserved).
	 * @return New {@code double[]} of x.
	 * @see #solveTo(double[], double[])
	 * @see #solveEquals(double[])
	 */
	public double[] solve(double[] b)
	{
		double[] x = new double[b.length];
		solveTo(x, b);
		return x;
	}
	
	/**
	 * \brief Solve the system of linear equations represented by a
	 * <b>matrix</b> and two vectors, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the vector to be found.
	 * 
	 * @param b One-dimensional array of {@code double}s (overwritten).
	 * @see #solveTo(double[], double[])
	 * @see #solve(double[])
	 */
	public void solveEquals(double[] b)
	{
		solveTo(b, b);
	}
}
