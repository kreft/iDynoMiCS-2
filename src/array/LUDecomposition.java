package array;

/**
 * 
 * <p>Credit to the JAMA package</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 *
 */
public class LUDecomposition
{
	private double[][] lu;
	
	private int m, n, pivotSign;
	
	private int[] pivot;
	
	public LUDecomposition(double[][] matrix)
	{
		Matrix.checkDimensions(matrix);
		this.lu = Matrix.copy(matrix);
		this.m = Matrix.rowDim(this.lu);
		this.n = Matrix.colDim(this.lu);
		double[] tempRow;
		double[] tempCol = new double[this.m];
		this.pivot = new int[this.m];
		this.pivotSign = 1;
		for ( int i = 0; i < this.m; i++ )
			this.pivot[i] = i;
		/*
		 * Loop over columns.
		 */
		for ( int j = 0; j < this.n; j++ )
		{
			/*
			 * Copy this column temporarily.
			 */
			for ( int i = 0; i < this.m; i++ )
				tempCol[i] = this.lu[i][j];
			/*
			 * Apply previous transformations.
			 */
			for ( int i = 0; i < this.m; i++ )
			{
				tempRow = this.lu[i];
				/*
				 * Most of the time is spent in the following dot product.
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
				this.pivotSign *= -1;
				int tempJ = 0;
				for ( ; tempJ < this.n; tempJ++ )
				{
					double temp = this.lu[p][tempJ];
					this.lu[p][tempJ] = this.lu[j][tempJ];
					this.lu[j][tempJ] = temp;
				}
				tempJ = this.pivot[p];
				this.pivot[p] = this.pivot[j];
				this.pivot[j] = tempJ;
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
	
	public int[] getPivot()
	{
		return Vector.copy(this.pivot);
	}
	
	public double[] getDblPivot()
	{
		return Vector.toDbl(this.pivot);
	}
	
	/**
	 * \brief
	 * 
	 * 
	 * @return Determinant of the matrix given originally.
	 * @exception IllegalArgumentException : Matrix must be square.
	 */
	public double determinant()
	{
		Matrix.checkSquare(this.lu);
		double out = (double) this.pivotSign;
		for ( int i = 0; i < this.m; i++ )
			out *= this.lu[i][i];
		return out;
	}
	
	/**
	 * \brief Solve a * x = <b>b</b>, where a is the matrix given originally.
	 * 
	 * @param b Two-dimensional array of doubles.
	 * @return x such that lu * x = <b>b</b>(pivot, :)
	 * @exception IllegalArgumentException : Matrix row dimensions must agree.
     * @exception RuntimeException : Matrix is singular.
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
		double[][] x = Matrix.submatrix(b, this.pivot, Vector.range(nx));
		/*
		 * Solve l * y = b(pivot, :)
		 */
		for ( int k = 0; k < this.n-1; k++ )
			for ( int i = k+1; k < this.n; i++ )
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
	 * 
	 * @param b
	 * @return
	 */
	public double[] solve(double[] b)
	{
		if ( b.length != this.m )
		{
			throw new IllegalArgumentException(
										"Matrix row dimensions must agree.");
		}
		if ( this.isSingular() )
			throw new RuntimeException("Matrix is singular.");
		/*
		 * Copy right-hand side with pivoting.
		 */
		double[] x = Vector.subset(b, this.pivot);
		/*
		 * Solve l * y = b(pivot, :)
		 */
		for ( int k = 0; k < this.n-1; k++ )
			for ( int i = k+1; k < this.n; i++ )
				x[i] -= x[k] * this.lu[i][k];
		/*
		 * Solve u * x = y
		 */
		for (int k = n-1; k >= 0; k--)
		{
	         x[k] /= this.lu[k][k];
	         for ( int i = 0; i < k; i++ )
	        	 x[i] -= x[k] * this.lu[i][k];
	    }
		return x;
	}
	
}