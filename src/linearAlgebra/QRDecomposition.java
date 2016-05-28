package linearAlgebra;

/**
 * \brief QR Decomposition is a method for breaking a matrix into two.
 * This is useful for solving systems of linear equations.
 * 
 * <p>For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal matrix Q and an n-by-n upper triangular matrix R so that A = Q*R.
 * </p>
 * 
 * <p>The QR decomposition always exists, even if the matrix does not have full
 * rank, so the constructor will never fail.  The primary use of the QR
 * decomposition is in the least squares solution of non-square systems of
 * simultaneous linear equations.  This will fail if {@link #isFullRank()}
 * returns false.</p>
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
public class QRDecomposition
{
	/**
	 * Array for internal storage of decomposition.
	 */
	private double[][] QR;
	/**
	 * Row dimension.
	 */
	private int m;
	/**
	 * Column dimension.
	 */
	private int n;
	/**
	 * Array for internal storage of diagonal of R.
	 */
	private double[] Rd;
	
	/*************************************************************************
	 * CONSTRUCTOR
	 ************************************************************************/
	
	/**
	 * \brief QR Decomposition, computed by Householder reflections.
	 * 
	 * <p>Structure to access R and the Householder vectors and compute Q.</p>
	 * 
	 * @param matrix Rectangular matrix of {@code double} values.
	 */
	public QRDecomposition(double[][] matrix)
	{
		this.QR = Matrix.copy(matrix);
		this.m = Matrix.rowDim(matrix);
		this.n = Matrix.colDim(matrix);
		this.Rd = Vector.zerosDbl(this.n);
		/* Main loop. */
		for ( int k = 0; k < this.n; k++ )
		{
			/* Compute 2-norm of k-th column without under/overflow. */
			double norm = 0.0;
			for ( int i = k; i < this.m; i++ )
				norm = Math.hypot(norm, this.QR[i][k]);
			/* Form k-th Householder vector. */
			if ( norm != 0.0 )
			{
				if ( this.QR[k][k] < 0.0 )
					norm = - norm;
				for ( int i = k; i < m; i++ )
					this.QR[i][k] /= norm;
				this.QR[k][k]++;
				/* Apply transformation to remaining columns. */
				// NOTE this approach is used several times in this class:
				// could make it a private method.
				for ( int j = k + 1; j < this.n; j++ )
				{
					double s = 0.0;
					for ( int i = k; i < this.m; i++ )
						s += this.QR[i][k] * this.QR[i][j];
					s = - s / this.QR[k][k];
					for ( int i = k; i < this.m; i++ )
						this.QR[i][j] += s * this.QR[i][k];
				}
 			}
			this.Rd[k] = - norm;
		}
	}
	
	/*************************************************************************
	 * PUBLIC METHODS
	 ************************************************************************/
	
	/**
	 * \brief It the <b>matrix</b> full rank?
	 * 
	 * @return {@code true} if R, and hence the <b>matrix</b>, has full rank.
	 */
	public boolean isFullRank()
	{
		for ( int j = 0; j < this.n; j++ )
			if ( this.Rd[j] == 0.0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Copy the lower trapezoidal matrix whose columns define the
	 * reflections.
	 * 
	 * @return Matrix of Householder vectors.
	 */
	public double[][] getH()
	{
		double[][] H = Matrix.zeros(this.QR);
		for ( int j = 0; j < this.n; j++ )
			for ( int i = j; i < this.m; i++ )
				H[i][j] = this.QR[i][j];
		return H;
	}
	
	/**
	 * @return Upper triangular factor.
	 */
	public double[][] getR()
	{
		double[][] R = Matrix.asDiagonal(this.Rd);
		for ( int j = 0; j < this.n; j++ )
			for ( int i = 0; i < j; i++ )
				R[i][j] = this.QR[i][j];
		return R;
	}
	
	/**
	 * \brief Generate and return the (economy-sized) orthogonal factor.
	 * 
	 * @return Q
	 */
	public double[][] getQ()
	{
		double[][] Q = Matrix.identity(this.QR);
		for ( int k = this.n - 1; k >= 0; k-- )
			if ( this.QR[k][k] != 0.0 )
				for ( int j = k; j < this.n; j++ )
				{
					double s = 0.0;
					for ( int i = k; i < this.m; i++ )
						s += this.QR[i][k] * Q[i][j];
					s = - s / this.QR[k][k];
					for ( int i = k; i < this.m; i++ )
						Q[i][j] += s * this.QR[i][k];
				}
		return Q;
	}
	
	/**
	 * \brief Solve the system of linear equations represented by three
	 * matrices: <b>matrix</b>, x and <b>b</b>, where
	 * <b>matrix</b> * x = <b>b</b> and x is the matrix to be found.
	 * 
	 * @param b A matrix with as many rows as the original and any number of
	 * columns.
	 * @return X that minimizes the two norm of Q*R*X-B.
	 * @exception  IllegalArgumentException  Matrix row dimensions must agree.
	 * @exception  RuntimeException  Matrix is rank deficient.
	 */
	public double[][] solve(double[][] b)
	{
		if ( Matrix.rowDim(b) != this.m )
		{
			throw new IllegalArgumentException(
					"Matrix row dimensions must agree.");
		}
		if ( ! this.isFullRank() )
			throw new RuntimeException("Matrix is rank deficient.");
		/* Copy right-hand side. */
		int nx = Matrix.colDim(b);
		double[][] x = Matrix.copy(b);
		/* Compute Y = transpose(Q)*B */
		for ( int k = 0; k < this.n; k++ )
			for ( int j = 0; j < nx; j++ )
			{
				double s = 0.0; 
				for ( int i = k; i < this.m; i++ )
					s += this.QR[i][k] * x[i][j];
				s = - s / this.QR[k][k];
				for (int i = k; i < this.m; i++)
					x[i][j] += s * this.QR[i][k];
			}
		/* Solve R*X = Y */
		for ( int k = this.n - 1; k >= 0; k-- )
		{
			for ( int j = 0; j < nx; j++ )
				x[k][j] /= this.Rd[k];
			for ( int i = 0; i < k; i++ )
				for ( int j = 0; j < nx; j++ )
					x[i][j] -= x[k][j] * this.QR[i][k];
		}
		/* The solution is a sub-matrix of what we just calculated. */
		int rowMax = this.n - 1;
		int colMax = nx - 1;
		return Matrix.submatrix(x, 0, rowMax, 0, colMax);
	}
}
