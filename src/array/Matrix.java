package array;

/**
 * 
 * <p>Credit to the JAMA package</p>
 * 
 * <p>Note that all arrays from the <b>array.Vector</b> class are treated here
 * as column vectors. These may be converted to row vectors here using the 
 * {@link #transpose(int[] vector)} or {@link #transpose(double[] vector)}
 * methods.</p> 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class Matrix
{
	/*************************************************************************
	 * SIMPLE INTEGER METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check the consistency of the row lengths in a given
	 * <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 */
	public static void checkDimensions(int[][] matrix)
	{
		for ( int i = 1; i < matrix.length; i++ )
			if ( matrix[i].length != matrix[0].length )
			{
				throw new IllegalArgumentException(
									"Matrix dimensions are inconsistent.");
			}
	}
	
	public static int rowDim(int[][] matrix)
	{
		return matrix.length;
	}
	
	public static int colDim(int[][] matrix)
	{
		return matrix[0].length;
	}
	
	public static void checkSquare(int[][] matrix)
	{
		if ( rowDim(matrix) != colDim(matrix) )
			throw new IllegalArgumentException("Matrix must be square.");
	}
	
	/**
	 * \brief Returns the size of the largest of the two dimensions (# rows or
	 * # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Size of the largest dimension of the given <b>matrix</b>.
	 */
	public static int maxDim(int[][] matrix)
	{
		return Math.max(matrix.length, matrix[0].length);
	}
	
	/**
	 * \brief Returns the size of the smallest of the two dimensions (# rows
	 * or # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Size of the smallest dimension of the given <b>matrix</b>.
	 */
	public static int minDim(int[][] matrix)
	{
		return Math.min(matrix.length, matrix[0].length);
	}
	
	public static boolean isSquare(int[][] matrix)
	{
		return ( matrix.length == matrix[0].length );
	}
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to the integer
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>matrix</b> will be overwritten; use
	 * <i>setAll(copy(<b>matrix</b>), <b>value</b>)</i> or
	 * <i>newInt(<b>matrix</b>.length, <b>matrix</b>[0].length,
	 * <b>value</b>)</i> to preserve the original state of <b>matrix</b>.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @param value Fill the matrix with this integer value.
	 * @return Given <b>matrix</b> with all elements set to <b>value</b>.
	 */
	public static int[][] setAll(int[][] matrix, int value)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				matrix[i][j] = value;
		return matrix;
	}
	
	/**
	 * \brief A new m-by-n matrix of integers.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param value Fill the matrix with this integer value.
	 * @return Two-dimensional array of integers, all of <b>value</b> given.
	 */
	public static int[][] matrix(int m, int n, int value)
	{
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = value;
		return out;
	}
	
	/**
	 * \brief A new square matrix of integers.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @param value Fill the matrix with this integer value.
	 * @return Two-dimensional array of integers, all of <b>value</b> given.
	 */
	public static int[][] matrix(int mn, int value)
	{
		return matrix(mn, mn, value);
	}
	
	/**
	 * \brief A new m-by-n matrix of integer zeros.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of integers, all of value 0.
	 */
	public static int[][] zerosInt(int m, int n)
	{
		return matrix(m, n, 0);
	}
	
	/**
	 * \brief A new square matrix of integer zeros.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of integers, all of value 0.
	 */
	public static int[][] zerosInt(int mn)
	{
		return matrix(mn, 0);
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of integers with ones on the diagonal and
	 * zeros elsewhere.
	 */
	public static int[][] identityInt(int mn)
	{
		int[][] out = new int[mn][mn];
		for ( int i = 0; i < mn; i++ )
			for ( int j = 0; j < mn; j++ )
				out[i][j] = ( i == j ) ? 1 : 0;
		return out;
	}
	
	
	public static int[][] copy(int[][] matrix)
	{
		int[][] out = new int[matrix.length][matrix[0].length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[i][j] = matrix[i][j];
		return out;
	}
	
	public static int[][] transpose(int[] vector)
	{
		int[][] out = new int[1][vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[0][i] = vector[i];
		return out;
	}
	
	public static int[][] transpose(int[][] matrix)
	{
		int[][] out = new int[matrix[0].length][matrix.length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param matrix
	 * @param rows
	 * @param cols
	 * @return
	 * @exception  ArrayIndexOutOfBoundsException Check submatrix indices.
	 */
	public static int[][] submatrix(int[][] matrix, int[] rows, int[] cols)
	{
		int[][] out = new int[rows.length][cols.length];
		try
		{
			for ( int i = 0; i < rows.length; i++ )
				for ( int j = 0; j < cols.length; j++ )
					out[i][j] = matrix[rows[i]][cols[j]];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check submatrix indices");
		}
		return out;
	}
	
	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Integer sum of elements in the diagonal.
	 */
	public static int trace(int[][] matrix)
	{
		int out = 0;
		int min = minDim(matrix);
		for (int i = 0; i < min; i++)
	         out += matrix[i][i];
	    return out;
	}
	
	/**
	 * \brief Frobenius norm of a given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Square root of 
	 */
	public static double normFrobenius(int[][] matrix)
	{
		/*
		 * JAMA method uses Math.hypot, presumably to avoid the risk of out
		 * exceeding the maximum size for a double. Something like
		 * 	...
		 * 		out = Math.pow(matrix[i][j], 2);
		 * 	return Math.sqrt(out);
		 * may be quicker, but riskier. 
		 */
		double out = 0.0;
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out = Math.hypot(out, matrix[i][j]);
		return out;
	}
	
	/*************************************************************************
	 * SIMPLE DOUBLE METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check the consistency of the row lengths in a given
	 * <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 */
	public static void checkDimensions(double[][] matrix)
	{
		for ( int i = 1; i < matrix.length; i++ )
			if ( matrix[i].length != matrix[0].length )
			{
				throw new IllegalArgumentException(
									"Matrix dimensions are inconsistent.");
			}
	}
	
	public static int rowDim(double[][] matrix)
	{
		return matrix.length;
	}
	
	public static int colDim(double[][] matrix)
	{
		return matrix[0].length;
	}
	
	public static void checkSquare(double[][] matrix)
	{
		if ( rowDim(matrix) != colDim(matrix) )
			throw new IllegalArgumentException("Matrix must be square.");
	}
	
	/**
	 * \brief Returns the size of the largest of the two dimensions (# rows or
	 * # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Size of the largest dimension of the given <b>matrix</b>.
	 */
	public static int maxDim(double[][] matrix)
	{
		return Math.max(matrix.length, matrix[0].length);
	}
	
	/**
	 * \brief Returns the size of the smallest of the two dimensions (# rows
	 * or # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Size of the smallest dimension of the given <b>matrix</b>.
	 */
	public static int minDim(double[][] matrix)
	{
		return Math.min(matrix.length, matrix[0].length);
	}
	
	public static boolean isSquare(double[][] matrix)
	{
		return ( matrix.length == matrix[0].length );
	}
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to the double
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>matrix</b> will be overwritten; use
	 * <i>setAll(copy(<b>matrix</b>), <b>value</b>)</i> or
	 * <i>newInt(<b>matrix</b>.length, <b>matrix</b>[0].length,
	 * <b>value</b>)</i> to preserve the original state of <b>matrix</b>.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param value Fill the matrix with this double value.
	 * @return Given <b>matrix</b> with all elements set to <b>value</b>.
	 */
	public static double[][] setAll(double[][] matrix, double value)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				matrix[i][j] = value;
		return matrix;
	}
	
	public static double[][] matrix(int m, int n, double value)
	{
		double[][] matrix = new double[m][n];
		return setAll(matrix, value);
	}
	
	public static double[][] matrix(int mn, double value)
	{
		return matrix(mn, mn, value);
	}
	
	public static double[][] zerosDbl(int m, int n)
	{
		return matrix(m, n, 0.0);
	}
	
	/**
	 * \brief Constructs a square matrix full of double zeros.
	 * 
	 * @param mn	Number of rows (same as number of columns)
	 * @return	double[][] array composed of zeros.
	 */
	public static double[][] zerosDbl(int mn)
	{
		return matrix(mn, 0.0);
	}
	
	public static double[][] identityDbl(int mn)
	{
		double[][] out = new double[mn][mn];
		for ( int i = 0; i < mn; i++ )
			for ( int j = 0; j < mn; j++ )
				out[i][j] = ( i == j ) ? 1.0 : 0.0;
		return out;
	}
	
	public static double[][] copy(double[][] matrix)
	{
		double[][] out = new double[matrix.length][matrix[0].length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[i][j] = matrix[i][j];
		return out;
	}
	
	public static double[][] transpose(double[][] matrix)
	{
		double[][] out = new double[matrix[0].length][matrix.length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	public static double[][] transpose(double[] vector)
	{
		double[][] out = new double[1][vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[0][i] = vector[i];
		return out;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param matrix
	 * @param rows
	 * @param cols
	 * @return
	 * @exception  ArrayIndexOutOfBoundsException Check submatrix indices.
	 */
	public static double[][] submatrix(double[][] matrix, int[] rows,
																int[] cols)
	{
		double[][] out = new double[rows.length][cols.length];
		try
		{
			for ( int i = 0; i < rows.length; i++ )
				for ( int j = 0; j < cols.length; j++ )
					out[i][j] = matrix[rows[i]][cols[j]];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check submatrix indices");
		}
		return out;
	}
	
	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Integer sum of elements in the diagonal.
	 */
	public static double trace(double[][] matrix)
	{
		double out = 0;
		int min = minDim(matrix);
		for (int i = 0; i < min; i++)
	         out += matrix[i][i];
	    return out;
	}
	
	/**
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return
	 */
	public static double maxColumnSum(double[][] matrix)
	{
		int n = colDim(matrix);
		double out = 0.0;
		double sum;
		for ( int j = 0; j < n; j++ )
		{
			sum = 0.0;
			for ( double[] row : matrix )
				sum += row[j];
			out = Math.max(out, sum);
		}
		return out;
	}
	
	/**
	 * 
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return
	 */
	public static double maxRowSum(double[][] matrix)
	{
		double out = 0.0;
		for ( double[] row : matrix )
			out = Math.max(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Frobenius norm of a given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Square root of 
	 */
	public static double normFrobenius(double[][] matrix)
	{
		/*
		 * JAMA method uses Math.hypot, presumably to avoid the risk of out
		 * exceeding the maximum size for a double. Something like
		 * 	...
		 * 		out = Math.pow(matrix[i][j], 2);
		 * 	return Math.sqrt(out);
		 * may be quicker, but riskier. 
		 */
		double out = 0.0;
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out = Math.hypot(out, matrix[i][j]);
		return out;
	}
	
	
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 ************************************************************************/
	
	
	public static int[][] toInt(double[][] matrix)
	{
		int m = rowDim(matrix);
		int n = colDim(matrix);
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = (int) matrix[i][j];
		return out;
	}
	
	
	public static double[][] toDbl(int[][] matrix)
	{
		int m = rowDim(matrix);
		int n = colDim(matrix);
		double[][] out = new double[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = (double) matrix[i][j];
		return out;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	public static void checkDimensions(double[][] a, double[][] b)
	{
		if ( rowDim(a) != rowDim(b) || colDim(a) != colDim(b) )
		{
			throw new 
				IllegalArgumentException("Matrix row dimensions must agree.");
		}
	}
	
	/**
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[][] elemAdd(double[][] a, double[][] b)
	{
		checkDimensions(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] += b[i][j];
		return a;
	}
	
	/**
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[][] elemTimes(double[][] a, double[][] b)
	{
		checkDimensions(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] *= b[i][j];
		return a;
	}
	
}