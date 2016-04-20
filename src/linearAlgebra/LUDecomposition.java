package linearAlgebra;

/**
 * \brief TODO
 * 
 * 
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
 *
 */
public class LUDecomposition
{
	private double[][] lu;
	
	private int m, n, _pivotSign;
	
	private int[] _pivot;
	
	public LUDecomposition(double[][] matrix)
	{
		Matrix.checkDimensions(matrix);
		this.lu = Matrix.copy(matrix);
		this.m = Matrix.rowDim(this.lu);
		this.n = Matrix.colDim(this.lu);
		double[] tempRow;
		double[] tempCol = new double[this.m];
		this._pivot = Vector.range(this.m);
		this._pivotSign = 1;
		/*
		 * Loop over columns.
		 */
		for ( int j = 0; j < this.n; j++ )
		{
			/*
			 * Copy this column temporarily.
			 */
			Matrix.getColumnTo(tempCol, this.lu, j);
			/*
			 * Apply previous transformations.
			 */
			for ( int i = 0; i < this.m; i++ )
			{
				tempRow = this.lu[i];
				/*
				 * Most of the time is spent in the following dot product. No
				 * point using Vector.dotProduct(), as we only go up to kMax.
				 */
				int kMax = Math.min(i,  j);
				double sum = 0.0;
				for ( int k = 0; k < kMax; k++ )
					sum += tempRow[k] * tempCol[k];
				tempRow[j] = tempCol[i] -= sum;
			}
			/*
			 * Find pivot and exchange if necessary. (Parallel assignment, as
			 * in e.g. Python, would be more elegant when swapping values).
			 */
			int p = j;
			for ( int i = j+1; i < this.m; i++ )
				if ( Math.abs(tempCol[i]) > Math.abs(tempCol[p]) )
					p = i;
			if ( p != j )
			{
				this._pivotSign *= -1;
				int tempJ = 0;
				for ( ; tempJ < this.n; tempJ++ )
				{
					double temp = this.lu[p][tempJ];
					this.lu[p][tempJ] = this.lu[j][tempJ];
					this.lu[j][tempJ] = temp;
				}
				tempJ = this._pivot[p];
				this._pivot[p] = this._pivot[j];
				this._pivot[j] = tempJ;
			}
			/*
			 * Compute multipliers.
			 */
			if ( ( j < this.m ) && ( this.lu[j][j] != 0.0 ) )
				for ( int i = j+1; i < this.m; i++ )
					this.lu[i][j] /= this.lu[j][j];
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isNonsingular()
	{
		int min = Matrix.minDim(this.lu);
		for ( int i = 0; i < min; i++ )
			if ( this.lu[i][i] == 0.0 )
				return false;
		return true;
	}
	
	public boolean isSingular()
	{
		return ! isNonsingular();
	}
	
	public double[][] getL()
	{
		double[][] out = new double[this.m][this.n];
		int i;
		for ( int j = 0; j < this.n; j++ )
		{
			for ( i = 0 ; i < j; i++ )
				out[i][j] = 0.0;
			out[j][j] = 1.0;
			for ( i++ ; i < this.m; i++ )
				out[i][j] = this.lu[i][j];
		}
		return out;
	}
	
	public double[][] getU()
	{
		double[][] out = Matrix.zerosDbl(this.m, this.n);
		int i;
		for ( int j = 0; j < this.n; j++ )
		{
			for ( i = 0 ; i <= j; i++ )
				out[i][j] = this.lu[i][j];
			for ( ; i < this.m; i++ )
				out[i][j] = 0.0;
		}
		return out;
	}
	
	/**
	 * \brief Get a copy of this L-U Decomposition's pivot vector.
	 * 
	 * @return An {@code int[]} copy of this Lower-Upper Decomposition's pivot
	 *    vector.
	 */
	public int[] getPivot()
	{
		return Vector.copy(this._pivot);
	}
	
	/**
	 * \brief Get a copy of this L-U Decomposition's pivot vector, with 
	 * {@code int} indices recast as {@code double}s.
	 * 
	 * @return An {@code double[]} copy of this Lower-Upper Decomposition's
	 *    pivot vector.
	 */
	public double[] getDblPivot()
	{
		return Vector.toDbl(this._pivot);
	}
	
	/**
	 * \brief Calculate the determinant.
	 * 
	 * @return Determinant of the matrix given originally.
	 * @exception IllegalArgumentException Matrix must be square.
	 */
	public double determinant()
	{
		Matrix.checkSquare(this.lu);
		double out = (double) this._pivotSign;
		for ( int i = 0; i < this.m; i++ )
			out *= this.lu[i][i];
		return out;
	}
	
	/**
	 * \brief Solve a * x = <b>b</b>, where a is the matrix given originally.
	 * 
	 * @param b Two-dimensional array of doubles.
	 * @return x such that lu * x = <b>b</b>(pivot, :)
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
     * @exception RuntimeException Matrix is singular.
	 */
	public double[][] solve(double[][] b)
	{
		if ( Matrix.rowDim(b) != this.m )
		{
			throw new IllegalArgumentException(
										"Matrix row dimensions must agree.");
		}
		if ( this.isSingular() )
			throw new RuntimeException("Matrix is singular.");
		/*
		 * Copy right-hand side with pivoting.
		 */
		int nx = Matrix.colDim(b);
		double[][] x = Matrix.submatrix(b, this._pivot, Vector.range(nx));
		/*
		 * Solve l * y = b(pivot, :)
		 */
		for ( int k = 0; k < this.n-1; k++ )
			for ( int i = k+1; i < this.n; i++ )
				for ( int j = 0; j < nx; j++ )
					x[i][j] -= x[k][j] * this.lu[i][k];
		/*
		 * Solve u * x = y
		 */
		for (int k = n-1; k >= 0; k--)
		{
	         for ( int j = 0; j < nx; j++ )
	            x[k][j] /= this.lu[k][k];
	         for ( int i = 0; i < k; i++ )
	            for ( int j = 0; j < nx; j++ )
	               x[i][j] -= x[k][j] * this.lu[i][k];
	    }
		return x;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param destination
	 * @param b
	 * @see #solve(double[])
	 * @see #solveEquals(double[])
	 */
	public void solveTo(double[] destination, double[] b)
	{
		if ( b.length != this.m )
		{
			throw new IllegalArgumentException(
										"Matrix row dimensions must agree.");
		}
		if ( this.isSingular() )
			throw new RuntimeException("Matrix is singular.");
		/*
		 * Copy right-hand side with pivoting. Vector.subsetTo() should check
		 * that destination and pivot have the same length.
		 */
		Vector.subsetTo(destination, b, this._pivot);
		/*
		 * Solve l * y = b(pivot, :)
		 */
		for ( int k = 0; k < this.n - 1; k++ )
			for ( int i = k+1; i < this.n; i++ )
				destination[i] -= destination[k] * this.lu[i][k];
		/*
		 * Solve u * x = y
		 */
		for (int k = n - 1; k >= 0; k--)
		{
			destination[k] /= this.lu[k][k];
	         for ( int i = 0; i < k; i++ )
	        	 destination[i] -= destination[k] * this.lu[i][k];
	    }
	}
	
	/**
	 * \brief Solve the system of linear equations represented by a
	 * <b>matrix</b> and two vectors, x and <b>vector</b>, where
	 * <b>matrix</b> * x = <b>vector</b> and x is the vector to be found.
	 * 
	 * @param b One-dimensional array of {@code double}s (preserved).
	 * @return New {@code double[]} of x.
	 * @see #solveTo(double[], double[])
	 * @see #solveEquals(double[])
	 */
	public double[] solve(double[] b)
	{
		double[] out = new double[this.m];
		this.solveTo(out, b);
		return out;
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
		this.solveTo(b, b);
	}
}