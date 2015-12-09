package linearAlgebra;

import utility.ExtraMath;

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
	 * STANDARD NEW MATRICES
	 ************************************************************************/
	
	/* Given value */
	
	/**
	 * \brief A new m-by-n matrix of integers.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param value Fill the matrix with this integer value.
	 * @return Two-dimensional array of integers, all of <b>value</b> given.
	 * @see #matrix(int, int)
	 * @see #matrix(int, int, double)
	 */
	public static int[][] matrix(int m, int n, int value)
	{
		int[][] out = new int[m][n];
		setAll(out, value);
		return out;
	}
	
	/**
	 * \brief A new square matrix of integers.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @param value Fill the matrix with this integer value.
	 * @return Two-dimensional array of integers, all of <b>value</b> given.
	 * @see #matrix(int, int, int)
	 * @see #matrix(int, double)
	 */
	public static int[][] matrix(int mn, int value)
	{
		return matrix(mn, mn, value);
	}
	
	/**
	 * \brief A new m-by-n matrix of doubles.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param value Fill the matrix with this double value.
	 * @return Two-dimensional array of doubles, all of <b>value</b> given.
	 * @see #matrix(int, double)
	 * @see #matrix(int, int, int)
	 */
	public static double[][] matrix(int m, int n, double value)
	{
		double[][] matrix = new double[m][n];
		setAll(matrix, value);
		return matrix;
	}
	
	/**
	 * \brief A new square matrix of doubles.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @param value Fill the matrix with this double value.
	 * @return Two-dimensional array of doubles, all of <b>value</b> given.
	 * @see #matrix(int, int, double)
	 * @see #matrix(int, int)
	 */
	public static double[][] matrix(int mn, double value)
	{
		return matrix(mn, mn, value);
	}
	
	/* Zeros */
	
	/**
	 * \brief A new m-by-n matrix of integer zeros.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of integers, all of value 0.
	 * @see #zerosInt(int)
	 * @see #zeros(int[][])
	 * @see #zerosDbl(int, int)
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
	 * @see #zerosInt(int, int)
	 * @see #zeros(int[][])
	 * @see #zerosDbl(int)
	 */
	public static int[][] zerosInt(int mn)
	{
		return matrix(mn, 0);
	}
	
	/**
	 * \brief A new m-by-n matrix of integer zeros.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return Two-dimensional array of all of integer value 0, with the same
	 * number of rows and of columns as <b>matrix</b>.
	 * @see #zerosInt(int, int)
	 * @see #zerosInt(int)
	 * @see #zeros(double[][])
	 */
	public static int[][] zeros(int[][] matrix)
	{
		return zerosInt(rowDim(matrix), colDim(matrix));
	}

	/**
	 * \brief A new m-by-n matrix of double zeros.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of doubles, all of value 0.0.
	 * @see #zerosDbl(int)
	 * @see #zeros(double[][])
	 * @see #zerosInt(int, int)
	 */
	public static double[][] zerosDbl(int m, int n)
	{
		return matrix(m, n, 0.0);
	}
	
	/**
	 * \brief Constructs a square matrix full of double zeros.
	 * 
	 * @param mn	Number of rows (same as number of columns)
	 * @return	double[][] array composed of zeros.
	 * @see #zerosDbl(int, int)
	 * @see #zeros(double[][])
	 * @see #zerosInt(int)
	 */
	public static double[][] zerosDbl(int mn)
	{
		return matrix(mn, 0.0);
	}
	
	/**
	 * \brief A new m-by-n matrix of double zeros.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @return Two-dimensional array of doubles.
	 * @param matrix Two-dimensional array of doubles.
	 * @return Two-dimensional array of all of double value 0.0, with the same
	 * number of rows and of columns as <b>matrix</b>.
	 * @see #zerosDbl(int, int)
	 * @see #zerosDbl(int)
	 * @see #zeros(int[][])
	 */
	public static double[][] zeros(double[][] matrix)
	{
		return zerosDbl(rowDim(matrix), colDim(matrix));
	}
	
	/* Identity */
	
	/**
	 * \brief A new identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of integers with ones on the diagonal and
	 * zeros elsewhere.
	 * @see #identityInt(int)
	 * @see #identity(int[][])
	 * @see #identityDbl(int, int)
	 */
	public static int[][] identityInt(int m, int n)
	{
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = ( i == j ) ? 1 : 0;
		return out;
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of integers with ones on the diagonal and
	 * zeros elsewhere.
	 * @see #identityInt(int, int)
	 * @see #identity(int[][])
	 * @see #identityDbl(int)
	 */
	public static int[][] identityInt(int mn)
	{
		return identityInt(mn, mn);
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Two-dimensional array of integers with ones on the diagonal and
	 * zeros elsewhere, with the same number of rows and of columns as
	 * <b>matrix</b>.
	 * @see #identityInt(int, int)
	 * @see #identityInt(int)
	 * @see #identity(double[][])
	 */
	public static int[][] identity(int[][] matrix)
	{
		return identityInt(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief A new identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of doubles with ones on the diagonal and
	 * zeros elsewhere.
	 * @see #identityDbl(int, int)
	 * @see #identity(double[][])
	 * @see #identityInt(int, int)
	 */
	public static double[][] identityDbl(int m, int n)
	{
		double[][] out = new double[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = ( i == j ) ? 1.0 : 0.0;
		return out;
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of doubles with ones on the diagonal and
	 * zeros elsewhere.
	 * @see #identityDbl(int, int)
	 * @see #identity(double[][])
	 * @see #identityInt(int)
	 */
	public static double[][] identityDbl(int mn)
	{
		return identityDbl(mn, mn);
	}
	
	/**
	 * \brief A new identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Two-dimensional array of doubles with ones on the diagonal and
	 * zeros elsewhere, with the same number of rows and of columns as
	 * <b>matrix</b>.
	 * @see #identityDbl(int, int)
	 * @see #identityDbl(int)
	 * @see #identity(int[][])
	 */
	public static double[][] identity(double[][] matrix)
	{
		return identityDbl(rowDim(matrix), colDim(matrix));
	}
	
	/*************************************************************************
	 * COPYING AND SETTING
	 ************************************************************************/
	
	/**
	 * \brief Make a deep copy of the given <b>matrix</b>, placing the result
	 * into a <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of integers (overwritten).
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @see #copy(int[][])
	 * @see #copyTo(double[][], double[][])
	 */
	public static void copyTo(int[][] destination, int[][] matrix)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				destination[i][j] = matrix[i][j];
	}
	
	/**
	 * \brief Make a deep copy of the given <b>matrix</b>, placing the result
	 * into a new int[][].
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return int[][] array that is an exact copy of the given <b>matrix</b>.
	 * @see #copyTo(int[][], int[][])
	 * @see #copy(double[][])
	 */
	public static int[][] copy(int[][] matrix)
	{
		int[][] out = new int[matrix.length][matrix[0].length];
		copyTo(out, matrix);
		return out;
	}
	
	/**
	 * \brief Make a deep copy of the given <b>matrix</b>, placing the result
	 * into a <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of doubles (overwritten).
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @see #copy(double[][])
	 * @see #copyTo(int[][], int[][])
	 */
	public static void copyTo(double[][] destination, double[][] matrix)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				destination[i][j] = matrix[i][j];
	}
	
	/**
	 * \brief Make a deep copy of the given <b>matrix</b>, placing the result
	 * into a new double[][].
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return double[][] array that is an exact copy of the given <b>matrix</b>.
	 * @see #copyTo(double[][], double[][])
	 * @see #copy(int[][])
	 */
	public static double[][] copy(double[][] matrix)
	{
		double[][] out = new double[matrix.length][matrix[0].length];
		copyTo(out, matrix);
		return out;
	}
	

	/**
	 * \brief Set all elements of the given <b>matrix</b> to the integer
	 * <b>value</b> given.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param value Fill the <b>matrix</b> with this integer value.
	 * @see #setAll(double[][], double)
	 */
	public static void setAll(int[][] matrix, int value)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				matrix[i][j] = value;
	}
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to the double
	 * <b>value</b> given.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param value Fill the matrix with this double value.
	 * @see #setAll(int[][], int)
	 */
	public static void setAll(double[][] matrix, double value)
	{
		for ( int i = 0; i < rowDim(matrix); i++ )
			for ( int j = 0; j < colDim(matrix); j++ )
				matrix[i][j] = value;
	}
	
	/*************************************************************************
	 * CHECKING METHODS
	 ************************************************************************/
	
	/* Internal consistency */
	
	/**
	 * \brief Check the consistency of the row lengths in a given
	 * <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException All rows must have the same length.
	 * @see #checkDimensions(double[][])
	 * @see #checkDimensionsSame(int[][], int[][])
	 * @see #checkSquare(int[][])
	 */
	public static void checkDimensions(int[][] matrix)
	{
		for ( int i = 1; i < matrix.length; i++ )
			if ( matrix[i].length != matrix[0].length )
			{
				throw new IllegalArgumentException(
									"All rows must have the same length.");
			}
	}
	
	/**
	 * \brief Check the consistency of the row lengths in a given
	 * <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException All rows must have the same length.
	 * @see #checkDimensions(int[][])
	 * @see #checkDimensionsSame(double[][], double[][])
	 * @see #checkSquare(double[][])
	 */
	public static void checkDimensions(double[][] matrix)
	{
		for ( int i = 1; i < matrix.length; i++ )
			if ( matrix[i].length != matrix[0].length )
			{
				throw new IllegalArgumentException(
									"All rows must have the same length.");
			}
	}
	
	/* Two matrices */
	
	/**
	 * \brief Check that the two matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 * @see #checkDimensions(int[][])
	 * @see #checkSquare(int[][])
	 * @see #checkDimensionsSame(double[][], double[][])
	 */
	public static void checkDimensionsSame(int[][] a, int[][] b)
	{
		if ( rowDim(a) != rowDim(b) || colDim(a) != colDim(b) )
		{
			throw new 
				IllegalArgumentException("Matrix row dimensions must agree.");
		}
	}
	
	/**
	 * \brief Check that the two matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 * @see #checkDimensions(double[][])
	 * @see #checkSquare(double[][])
	 * @see #checkDimensionsSame(int[][], int[][])
	 */
	public static void checkDimensionsSame(double[][] a, double[][] b)
	{
		if ( rowDim(a) != rowDim(b) || colDim(a) != colDim(b) )
		{
			throw new 
				IllegalArgumentException("Matrix row dimensions must agree.");
		}
	}
	
	/* Square matrix */
	
	/**
	 * \brief Reports if the <b>matrix</b> has as many rows as columns.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return boolean reporting whether the <b>matrix</b> is square (true) or
	 * not (false).
	 * @see #checkSquare(int[][])
	 * @see #isSquare(double[][])
	 */
	public static boolean isSquare(int[][] matrix)
	{
		return ( rowDim(matrix) == colDim(matrix) );
	}
	
	/**
	 * \brief Checks that the given <b>matrix</b> is square, throwing an error
	 * if not.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Matrix must be square.
	 * @see #isSquare(int[][])
	 * @see #checkSquare(double[][])
	 * @see #checkDimensions(int[][])
	 */
	public static void checkSquare(int[][] matrix)
	{
		if ( ! isSquare(matrix) )
			throw new IllegalArgumentException("Matrix must be square.");
	}
	
	/**
	 * \brief Reports if the given <b>matrix</b> has as many rows as columns.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return boolean reporting whether the <b>matrix</b> is square (true) or
	 * not (false).
	 * @see #checkSquare(double[][])
	 * @see #isSquare(int[][])
	 */
	public static boolean isSquare(double[][] matrix)
	{
		return ( rowDim(matrix) == colDim(matrix) );
	}
	
	/**
	 * \brief Checks that the given <b>matrix</b> is square, throwing an error
	 * if not.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Matrix must be square.
	 * @see #isSquare(double[][])
	 * @see #checkDimensions(double[][])
	 * @see #checkSquare(int[][])
	 */
	public static void checkSquare(double[][] matrix)
	{
		if ( ! isSquare(matrix) )
			throw new IllegalArgumentException("Matrix must be square.");
	}
	
	/*************************************************************************
	 * BASIC ARTHIMETIC
	 ************************************************************************/
	
	/* Adding */
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, writing the result into <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param matrix Two-dimensional array of integers from which to take 
	 * pre-existing values (preserved).
	 * @param value integer value to add to all elements.
	 * @see #add(int[][], int)
	 * @see #addEquals(int[][], int)
	 * @see #addTo(int[][], int[][], int[][])
	 * @see #addTo(double[][], double[][], double)
	 */
	public static void addTo(int[][] destination, int[][] matrix, int value)
	{
		for ( int i = 0; i < rowDim(matrix); i++ )
			for ( int j = 0; j < colDim(matrix); j++ )
				destination[i][j] = matrix[i][j] + value;
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, returning the result as a new int[][].
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @param value integer value to add to all elements.
	 * @return new int[][] with elements equals to those in <b>matrix</b>,
	 * plus <b>value</b>.
	 * @see #addTo(int[][], int[][], int)
	 * @see #addEquals(int[][], int)
	 * @see #add(int[][], int[][])
	 * @see #add(double[][], double)
	 */
	public static int[][] add(int[][] matrix, int value)
	{
		int[][] out = new int[matrix.length][matrix[0].length];
		addTo(out, matrix, value);
		return out;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>matrix</b>,
	 * overwriting the old values of <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param value integer value to add to all elements.
	 * @see #addTo(int[][], int[][], int)
	 * @see #add(int[][], int)
	 * @see #addEquals(int[][], int[][])
	 * @see #addEquals(double[][], double)
	 */
	public static void addEquals(int[][] matrix, int value)
	{
		addTo(matrix, matrix, value);
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, writing the result into <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param matrix Two-dimensional array of doubles from which to take 
	 * pre-existing values (preserved).
	 * @param value double value to add to all elements.
	 * @see #add(double[][], double)
	 * @see #addEquals(double[][], double)
	 * @see #addTo(double[][], double[][], double[][])
	 * @see #addEquals(int[][], int)
	 */
	public static void addTo(double[][] destination, double[][] matrix, double value)
	{
		for ( int i = 0; i < rowDim(matrix); i++ )
			for ( int j = 0; j < colDim(matrix); j++ )
				destination[i][j] = matrix[i][j] + value;
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, returning the result as a new double[][].
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @param value double value to add to all elements.
	 * @return new double[][] with elements equals to those in <b>matrix</b>,
	 * plus <b>value</b>.
	 * @see #addTo(double[][], double[][], double)
	 * @see #addEquals(double[][], double)
	 * @see #add(double[][], double[][])
	 * @see #add(int[][], int)
	 */
	public static double[][] add(double[][] matrix, double value)
	{
		double[][] out = new double[matrix.length][matrix[0].length];
		addTo(out, matrix, value);
		return out;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>matrix</b>,
	 * overwriting the old values of <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param value double value to add to all elements.
	 * @see #addTo(double[][], double[][], double)
	 * @see #add(double[][], double)
	 * @see #addEquals(double[][], double[][])
	 * @see #addEquals(int[][], int)
	 */
	public static void addEquals(double[][] matrix, double value)
	{
		addTo(matrix, matrix, value);
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #add(int[][], int[][])
	 * @see #addEquals(int[][], int[][])
	 * @see #addTo(int[][], int[][], int)
	 * @see #addTo(double[][], double[][], double[][]) 
	 */
	public static void addTo(int[][] destination, int[][] a, int[][] b)
	{
		for ( int i = 0; i < rowDim(destination); i++ )
			for ( int j = 0; j < colDim(destination); j++ )
				destination[i][j] = a[i][j] + b[i][j];
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into a new matrix.
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @return new int[][] array of <b>a</b>+<b>b</b>.
	 * @see #addTo(int[][], int[][], int[][])
	 * @see #addEquals(int[][], int[][])
	 * @see #add(int[][], int)
	 * @see #add(double[][], double[][])
	 */
	public static int[][] add(int[][] a, int[][] b)
	{
		int[][] out = new int[a.length][a[0].length];
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into <b>a</b>.
	 * 
	 * @param a Two-dimensional array of integers (overwritten).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #addTo(int[][], int[][], int[][])
	 * @see #add(int[][], int[][])
	 * @see #addEquals(int[][], int)
	 * @see #addEquals(double[][], double[][])
	 */
	public static void addEquals(int[][] a, int[][] b)
	{
		addTo(a, a, b);
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #add(double[][], double[][])
	 * @see #addEquals(double[][], double[][])
	 * @see #addTo(double[][], double[][], double)
	 * @see #addTo(double[][], double[][], double[][]) 
	 */
	public static void addTo(double[][] destination, double[][] a, double[][] b)
	{
		for ( int i = 0; i < rowDim(destination); i++ )
			for ( int j = 0; j < colDim(destination); j++ )
				destination[i][j] = a[i][j] + b[i][j];
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into a new matrix.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @return new double[][] array of <b>a</b>+<b>b</b>.
	 * @see #addTo(double[][], double[][], double[][])
	 * @see #addEquals(double[][], double[][])
	 * @see #add(double[][], double)
	 * @see #add(int[][], int[][])
	 */
	public static double[][] add(double[][] a, double[][] b)
	{
		double[][] out = new double[a.length][a[0].length];
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add matrix <b>a</b> to matrix <b>b</b>, element-by-element,
	 * writing the result into <b>a</b>.
	 * 
	 * @param a Two-dimensional array of doubles (overwritten).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #addTo(double[][], double[][], double[][])
	 * @see #add(double[][], double[][])
	 * @see #addEquals(double[][], double)
	 * @see #addEquals(int[][], int[][])
	 */
	public static void addEquals(double[][] a, double[][] b)
	{
		addTo(a, a, b);
	}
	
	/* Subtracting */
	

	
	/**
	 * \brief Subtract one matrix from another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>subtract({@link #copy(int[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of integers.
	 * @param b Two-dimensional array of integers.
	 * @return double[][] array of <b>a</b>-<b>b</b>.
	 */
	public static int[][] subtract(int[][] a, int[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] -= b[i][j];
		return a;
	}
	
	/**
	 * \brief Subtract one matrix from another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>subtract({@link #copy(double[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of doubles.
	 * @param b Two-dimensional array of doubles.
	 * @return double[][] array of <b>a</b>-<b>b</b>.
	 */
	public static double[][] subtract(double[][] a, double[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] -= b[i][j];
		return a;
	}
	
	/* Multiplication */
	
	/**
	 * \brief times a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, writing the result into <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param matrix Two-dimensional array of integers from which to take 
	 * pre-existing values (preserved).
	 * @param value integer value to times to all elements.
	 * @see #times(int[][], int)
	 * @see #timesEquals(int[][], int)
	 * @see #timesTo(int[][], int[][], int[][])
	 * @see #timesTo(double[][], double[][], double)
	 */
	public static void timesTo(int[][] destination, int[][] matrix, int value)
	{
		for ( int i = 0; i < rowDim(matrix); i++ )
			for ( int j = 0; j < colDim(matrix); j++ )
				destination[i][j] = matrix[i][j] + value;
	}
	
	/**
	 * \brief times a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, returning the result as a new int[][].
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @param value integer value to times to all elements.
	 * @return new int[][] with elements equals to those in <b>matrix</b>,
	 * plus <b>value</b>.
	 * @see #timesTo(int[][], int[][], int)
	 * @see #timesEquals(int[][], int)
	 * @see #times(int[][], int[][])
	 * @see #times(double[][], double)
	 */
	public static int[][] times(int[][] matrix, int value)
	{
		int[][] out = new int[matrix.length][matrix[0].length];
		timesTo(out, matrix, value);
		return out;
	}
	
	/**
	 * \brief times a scalar <b>value</b> to every element of a <b>matrix</b>,
	 * overwriting the old values of <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param value integer value to times to all elements.
	 * @see #timesTo(int[][], int[][], int)
	 * @see #times(int[][], int)
	 * @see #timesEquals(int[][], int[][])
	 * @see #timesEquals(double[][], double)
	 */
	public static void timesEquals(int[][] matrix, int value)
	{
		timesTo(matrix, matrix, value);
	}
	
	/**
	 * \brief times a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, writing the result into <b>destination</b>.
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param matrix Two-dimensional array of doubles from which to take 
	 * pre-existing values (preserved).
	 * @param value double value to times to all elements.
	 * @see #times(double[][], double)
	 * @see #timesEquals(double[][], double)
	 * @see #timesTo(double[][], double[][], double[][])
	 * @see #timesEquals(int[][], int)
	 */
	public static void timesTo(double[][] destination, double[][] matrix, double value)
	{
		for ( int i = 0; i < rowDim(matrix); i++ )
			for ( int j = 0; j < colDim(matrix); j++ )
				destination[i][j] = matrix[i][j] + value;
	}
	
	/**
	 * \brief times a given <b>value</b> to all elements in a given
	 * <b>matrix</b>, returning the result as a new double[][].
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @param value double value to times to all elements.
	 * @return new double[][] with elements equals to those in <b>matrix</b>,
	 * plus <b>value</b>.
	 * @see #timesTo(double[][], double[][], double)
	 * @see #timesEquals(double[][], double)
	 * @see #times(double[][], double[][])
	 * @see #times(int[][], int)
	 */
	public static double[][] times(double[][] matrix, double value)
	{
		double[][] out = new double[matrix.length][matrix[0].length];
		timesTo(out, matrix, value);
		return out;
	}
	
	/**
	 * \brief times a scalar <b>value</b> to every element of a <b>matrix</b>,
	 * overwriting the old values of <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param value double value to times to all elements.
	 * @see #timesTo(double[][], double[][], double)
	 * @see #times(double[][], double)
	 * @see #timesEquals(double[][], double[][])
	 * @see #timesEquals(int[][], int)
	 */
	public static void timesEquals(double[][] matrix, double value)
	{
		timesTo(matrix, matrix, value);
	}
	
	/**
	 * \brief Times one matrix by another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>elemTimes({@link #copy(int[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of integers.
	 * @param b Two-dimensional array of integers.
	 * @return int[][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static int[][] elemTimes(int[][] a, int[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] *= b[i][j];
		return a;
	}
	
	/**
	 * \brief Times one matrix by another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>elemTimes({@link #copy(double[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of doubles.
	 * @param b Two-dimensional array of doubles.
	 * @return double[][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static double[][] elemTimes(double[][] a, double[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] *= b[i][j];
		return a;
	}
	
	/**
	 * \brief Linear algebra matrix multiplication of the two given matrices.
	 * 
	 * <p>Inner dimensions must match, i.e. n<sub>a</sub> = m<sub>b</sub>. The
	 * output will be a new m<sub>a</sub>-by-n<sub>b</sub> matrix.</p>
	 * 
	 * <p>Note that the given matrices will be unaffected by this method.</p>
	 * 
	 * @param a Two-dimensional array of integers.
	 * @param b Two-dimensional array of integers.
	 * @return Two-dimensional array of integers: <b>a</b> x <b>b</b>.
	 * @exception IllegalArgumentException Matrix inner row dimensions must
	 * agree.
	 */
	public static int[][] times(int[][] a, int[][] b)
	{
		if ( colDim(a) != rowDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		int[][] out = new int[rowDim(a)][colDim(b)];
		int[] bCol;
		for ( int j = 0; j < colDim(b); j++ )
		{
			bCol = getColumn(b, j);
			for ( int i = 0; i < rowDim(a); i++ )
				out[i][j] = Vector.dotProduct(getRowAsColumn(a, i), bCol);
		}
		return out;
	}
	
	/**
	 * \brief Linear algebra matrix multiplication of the given <b>matrix</b>
	 * (left) by the given column <b>vector</b> (right).
	 * 
	 * <p>Inner dimensions must match, i.e. n<sub>matrix</sub> =
	 * length<sub>vector</sub>. The output will be a new column vector of 
	 * length m<sub>matrix</sub></p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @param vector One-dimensional array of integers.
	 * @return One-dimensional array of integers that is <b>matrix</b> x
	 * <b>vector</b>.
	 */
	public static int[] times(int[][] matrix, int[] vector)
	{
		if ( colDim(matrix) != vector.length )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		int[] out = new int[rowDim(matrix)];
		for ( int i = 0; i < rowDim(matrix); i++ )
			out[i] = Vector.dotProduct(getRowAsColumn(matrix, i), vector);
		return out;
	}
	
	/**
	 * \brief Linear algebra matrix multiplication of the two given matrices.
	 * 
	 * <p>Inner dimensions must match, i.e. n<sub>a</sub> = m<sub>b</sub>. The
	 * output will be a new m<sub>a</sub>-by-n<sub>b</sub> matrix.</p>
	 * 
	 * <p>Note that the given matrices will be unaffected by this method.</p>
	 * 
	 * @param a Two-dimensional array of doubles.
	 * @param b Two-dimensional array of doubles.
	 * @return Two-dimensional array of doubles: <b>a</b> x <b>b</b>.
	 * @exception IllegalArgumentException Matrix inner row dimensions must
	 * agree.
	 */
	public static double[][] times(double[][] a, double[][] b)
	{
		if ( colDim(a) != rowDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		double[][] out = new double[rowDim(a)][colDim(b)];
		double[] bCol;
		for ( int j = 0; j < colDim(b); j++ )
		{
			bCol = getColumn(b, j);
			for ( int i = 0; i < rowDim(a); i++ )
				out[i][j] = Vector.dotProduct(getRowAsColumn(a, i), bCol);
		}
		return out;
	}
	
	/**
	 * \brief Linear algebra matrix multiplication of the given <b>matrix</b>
	 * (left) by the given column <b>vector</b> (right).
	 * 
	 * <p>Inner dimensions must match, i.e. n<sub>matrix</sub> =
	 * length<sub>vector</sub>. The output will be a new column vector of 
	 * length m<sub>matrix</sub></p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param vector One-dimensional array of doubles.
	 * @return One-dimensional array of doubles that is <b>matrix</b> x
	 * <b>vector</b>.
	 */
	public static double[] times(double[][] matrix, double[] vector)
	{
		if ( colDim(matrix) != vector.length )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		double[] out = new double[rowDim(matrix)];
		for ( int i = 0; i < rowDim(matrix); i++ )
			out[i] = Vector.dotProduct(getRowAsColumn(matrix, i), vector);
		return out;
	}
	
	/* Division */
	
	/**
	 * \brief Divide one matrix by another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>elemDivide({@link #copy(int[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of integers.
	 * @param b Two-dimensional array of integers.
	 * @return int[][] array of <b>a</b> divided by <b>b</b> element-wise.
	 */
	public static int[][] elemDivide(int[][] a, int[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] /= b[i][j];
		return a;
	}
	
	/**
	 * \brief Divide one matrix by another, element-by-element.
	 * 
	 * <p>Matrices must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>elemDivide({@link #copy(double[][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Two-dimensional array of doubles.
	 * @param b Two-dimensional array of doubles.
	 * @return double[][] array of <b>a</b> divided by <b>b</b> element-wise.
	 */
	public static double[][] elemDivide(double[][] a, double[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			for ( int j = 0; j < colDim(a); j++ )
				a[i][j] /= b[i][j];
		return a;
	}
	
	/*************************************************************************
	 * SUBMATRICES
	 ************************************************************************/
	
	/**
	 * \brief Extract a subsection of the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @param rows int[] array of row indices to include, in the order given.
	 * @param cols int[] array of column indices to include, in the order
	 * given.
	 * @return Two-dimensional array of integers selectively copied from
	 * <b>matrix</b>.
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
	 * \brief Extract a subsection of the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param rows int[] array of row indices to include, in the order given.
	 * @param cols int[] array of column indices to include, in the order
	 * given.
	 * @return Two-dimensional array of doubles selectively copied from
	 * <b>matrix</b>.
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
	 * brief Extract a subsection of the given <b>matrix</b>.
	 * 
	 * <p>For example, if <b>matrix</b> = <br>1, 2, 3, 4;<br>5, 6, 7, 8;<br>
	 * 9, 10, 11, 12;<br> then <i>submatrix(</i><b>matrix</b><i>, 0, 2, 1,
	 * 4)</i> returns a new matrix<br>2, 3, 4;<br>6, 7, 8;</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param rStart int row index at which to start (inclusive).
	 * @param rStop int row index at which to stop (exclusive).
	 * @param cStart int column index at which to start (inclusive).
	 * @param cStop int column index at which to stop (exclusive).
	 * @return Two-dimensional array of doubles selectively copied from
	 * <b>matrix</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check submatrix indices.
	 */
	public static double[][] submatrix(double[][] matrix, int rStart,
										int rStop, int cStart, int cStop)
	{
		double[][] out = new double[rStop-rStart][cStop-cStart];
		try
		{
			for ( int i = rStart; i < rStop; i++ )
				for ( int j = cStart; j < cStop; j++ )
					out[i][j] = matrix[i][j];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check submatrix indices");
		}
		return out;
	}
	
	/*************************************************************************
	 * SIMPLE INTEGER METHODS
	 ************************************************************************/
	
	/**
	 * \brief Number of rows in the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int number of rows in the given <b>matrix</b>.
	 */
	public static int rowDim(int[][] matrix)
	{
		return matrix.length;
	}
	
	/**
	 * \brief Number of columns in the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int number of columns in the given <b>matrix</b>.
	 */
	public static int colDim(int[][] matrix)
	{
		return matrix[0].length;
	}
	
	/**
	 * \brief Returns the size of the largest of the two dimensions (# rows or
	 * # columns) of the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
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
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Size of the smallest dimension of the given <b>matrix</b>.
	 */
	public static int minDim(int[][] matrix)
	{
		return Math.min(matrix.length, matrix[0].length);
	}
	
	/**
	 * \brief Transpose a column <b>vector</b> to a row vector.
	 * 
	 * <p>Use {@link #toVector(int[][])} to reverse this.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return int[1][] array with the same elements as <b>vector</b>.
	 */
	public static int[][] transpose(int[] vector)
	{
		int[][] out = new int[1][vector.length];
		out[0] = Vector.copy(vector);
		return out;
	}
	
	/**
	 * \brief Transpose a <b>matrix</b>, i.e. flip it over its diagonal.
	 * 
	 * <p>For example, the matrix <i>(1, 2; 3, 4)</i> transposed is
	 *  <i>(1, 3; 2, 4)</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int[][] array of the given <b>matrix</b> transposed.
	 */
	public static int[][] transpose(int[][] matrix)
	{
		int[][] out = new int[colDim(matrix)][rowDim(matrix)];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	/**
	 * \brief Extracts the required row as a column vector.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @param index int index of the row required.
	 * @return int[] array of the required row.
	 */
	public static int[] getRowAsColumn(int[][] matrix, int index)
	{
		return matrix[index];
	}
	
	/**
	 * \brief Extracts the required column as a vector.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @param index int index of the column required.
	 * @return int[] array of the required column.
	 */
	public static int[] getColumn(int[][] matrix, int index)
	{
		int[] out = new int[rowDim(matrix)];
		for ( int i = 0; i < out.length; i++ )
			out[i] = matrix[i][index];
		return out;
	}
	
	/**
	 * \brief Converts a row vector to a column vector.
	 * 
	 * <p>Note that <b>rowVector</b> will be unaffected by this method.</p>
	 * 
	 * @param rowVector int[1][] array of integer values;
	 * @return One-dimensional array of integers.
	 * @exception IllegalArgumentException Matrix must have only one row.
	 */
	public static int[] toVector(int[][] rowVector)
	{
		if ( rowDim(rowVector) != 1 )
		{
			throw new 
				IllegalArgumentException("Matrix must have only one row.");
		}
		return Vector.copy(rowVector[1]);
	}
	
	/**
	 * \brief Converts a column vector to a diagonal matrix.
	 * 
	 * <p>For example, if <b>vector</b> = <br>1<br>2<br>3<br>then
	 * <i>asDiagonal(</i><b>vector</b><i>)</i>) =<br>1, 0, 0;<br>0, 2, 0;<br>
	 * 0, 0, 3;</p> 
	 * 
	 * <p>This is equivalent to <br>
	 * double out = {@link #identityInt(int n)};<br>
	 * {@link #times(int[][] out, int[] vector)};<br>
	 * where n is the length of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return Two-dimensional array of integers with the elements of
	 * <b>vector</b> along the main diagonal and zeros elsewhere. 
	 */
	public static int[][] asDiagonal(int[] vector)
	{
		int[][] out = zerosInt(vector.length);
		for ( int i = 0; i < vector.length; i++ )
			out[i][i] = vector[i];
		return out;
	}
	
	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * <p>The trace is the sum of all elements on the main diagonal.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
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
	 * \brief Finds the value of the greatest element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. maximum of the matrix <i>(1, -3; 2, 0)</i> is <i>2</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int value of the greatest element in the <b>matrix</b>.
	 */
	public static int max(int[][] matrix)
	{
		int out = matrix[0][0];
		for ( int[] row : matrix )
			out = Math.max(out, Vector.max(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. minimum of the matrix <i>(1, -3; 2, 0)</i> is <i>-3</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int value of the least element in the <b>matrix</b>.
	 */
	public static int min(int[][] matrix)
	{
		int out = matrix[0][0];
		for ( int[] row : matrix )
			out = Math.max(out, Vector.min(row));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>3</i> (left) and of <i>7</i> (right). The maximum 
	 * of these is 7.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int value of the greatest sum of all columns in the given
	 * <b>matrix</b>.
	 */
	public static int maxColumnSum(int[][] matrix)
	{
		int n = colDim(matrix);
		int out = 0;
		for ( int j = 0; j < n; j++ )
			out = Math.max(out, Vector.sum(getColumn(matrix, j)));
		return out;
	}
	
	/**
	 * \brief Least sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>3</i> (left) and of <i>7</i> (right). The minimum 
	 * of these is 3.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int value of the least sum of all columns in the given
	 * <b>matrix</b>.
	 */
	public static int minColumnSum(int[][] matrix)
	{
		int n = colDim(matrix);
		int out = 0;
		for ( int j = 0; j < n; j++ )
			out = Math.min(out, Vector.sum(getColumn(matrix, j)));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>4</i> (top) and of <i>6</i> (bottom). The maximum 
	 * of these is 6.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return integer value of the greatest sum of all rows in the given
	 * <b>matrix</b>.
	 */
	public static int maxRowSum(int[][] matrix)
	{
		int out = 0;
		for ( int[] row : matrix )
			out = Math.max(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Least sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>4</i> (top) and of <i>6</i> (bottom). The minimum 
	 * of these is 4.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return int value of the least sum of all rows in the given
	 * <b>matrix</b>.
	 */
	public static int minRowSum(int[][] matrix)
	{
		int out = 0;
		for ( int[] row : matrix )
			out = Math.min(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Frobenius norm of a given <b>matrix</b>.
	 * 
	 * <p>Note that the Frobenius norm is often called the Euclidean norm, but
	 * this may be confused with the L<sup>2</sup>-norm of a complex vector.
	 * See e.g. http://mathworld.wolfram.com/FrobeniusNorm.html for more
	 * details.</p>
	 * 
	 * <p>The original state of <b>matrix</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param matrix Two-dimensional array of integers.
	 * @return Square root of the sum of all elements squared.
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
	 * \brief Number of rows in the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return int number of rows in the given <b>matrix</b>.
	 */
	public static int rowDim(double[][] matrix)
	{
		return matrix.length;
	}
	
	/**
	 * \brief Number of columns in the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return int number of columns in the given <b>matrix</b>.
	 */
	public static int colDim(double[][] matrix)
	{
		return matrix[0].length;
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
	
	/**
	 * \brief Create a new <b>m</b>-by-<b>n</b> matrix with random elements.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of doubles with elements drawn from a 
	 * uniform distribution.
	 */
	public static double[][] random(int m, int n)
	{
		double[][] out = new double[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = ExtraMath.getUniRandDbl();
		return out;
	}
	
	/**
	 * \brief Create a new square matrix with random elements.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of doubles with elements drawn from a 
	 * uniform distribution.
	 */
	public static double[][] random(int mn)
	{
		return random(mn, mn);
	}
	
	/**
	 * \brief Create a new matrix with random elements.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Two-dimensional array of random doubles with the same number of
	 * rows and of columns as <b>matrix</b>.
	 */
	public static double[][] random(double[][] matrix)
	{
		return random(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief Transpose a column <b>vector</b> to a row vector.
	 * 
	 * <p>Use {@link #toVector(double[][])} to reverse this.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[1][] array with the same elements as <b>vector</b>.
	 */
	public static double[][] transpose(double[] vector)
	{
		double[][] out = new double[1][vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[0][i] = vector[i];
		return out;
	}
	
	/**
	 * \brief Transpose a <b>matrix</b>, i.e. flip it over its diagonal.
	 * 
	 * <p>For example, the matrix <i>(1.0, 2.0; 3.0, 4.0)</i> transposed is
	 *  <i>(1.0, 3.0; 2.0, 4.0)</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double[][] array of the given <b>matrix</b> transposed.
	 */
	public static double[][] transpose(double[][] matrix)
	{
		double[][] out = new double[matrix[0].length][matrix.length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	/**
	 * \brief Extracts the required row as a column vector.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param index int index of the row required.
	 * @return double[] array of the required row.
	 */
	public static double[] getRowAsColumn(double[][] matrix, int index)
	{
		return matrix[index];
	}
	
	/**
	 * \brief Extracts the required column as a vector.
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param index int index of the column required.
	 * @return int[] array of the required column.
	 */
	public static double[] getColumn(double[][] matrix, int index)
	{
		double[] out = new double[rowDim(matrix)];
		for ( int i = 0; i < out.length; i++ )
			out[i] = matrix[i][index];
		return out;
	}
	
	/**
	 * \brief Converts a row vector to a column vector.
	 * 
	 * <p>Note that <b>rowVector</b> will be unaffected by this method.</p>
	 * 
	 * @param rowVector double[1][] array of double values;
	 * @return One-dimensional array of doubles.
	 * @exception IllegalArgumentException Matrix must have only one row.
	 */
	public static double[] toVector(double[][] rowVector)
	{
		if ( rowDim(rowVector) != 1 )
		{
			throw new 
				IllegalArgumentException("Matrix must have only one row.");
		}
		return Vector.copy(rowVector[1]);
	}
	
	/**
	 * \brief Converts a column vector to a diagonal matrix.
	 * 
	 * <p>For example, if <b>vector</b> = <br>1<br>2<br>3<br>then
	 * <i>asDiagonal(</i><b>vector</b><i>)</i>) =<br>1, 0, 0;<br>0, 2, 0;<br>
	 * 0, 0, 3;</p> 
	 * 
	 * <p>This is equivalent to <br>
	 * double out = {@link #identityDbl(int n)};<br>
	 * {@link #times(double[][] out, double[] vector)};<br>
	 * where n is the length of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return Two-dimensional array of doubles with the elements of
	 * <b>vector</b> along the main diagonal and zeros elsewhere. 
	 */
	public static double[][] asDiagonal(double[] vector)
	{
		double[][] out = zerosDbl(vector.length);
		for ( int i = 0; i < vector.length; i++ )
			out[i][i] = vector[i];
		return out;
	}
	
	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * <p>The trace is the sum of all elements on the main diagonal.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
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
	 * \brief Finds the value of the greatest element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. maximum of the matrix <i>(1.0, -3.0; 2.0, 0.0)</i> is
	 * <i>2.0</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the greatest element in the <b>matrix</b>.
	 */
	public static double max(double[][] matrix)
	{
		double out = matrix[0][0];
		for ( double[] row : matrix )
			out = Math.max(out, Vector.max(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. minimum of the matrix <i>(1.0, -3.0; 2.0, 0.0)</i> is
	 * <i>-3.0</i>.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the least element in the <b>matrix</b>.
	 */
	public static double min(double[][] matrix)
	{
		double out = matrix[0][0];
		for ( double[] row : matrix )
			out = Math.max(out, Vector.min(row));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>3.0</i> (left) and of <i>7.0</i> (right). The maximum 
	 * of these is 7.0.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the greatest sum of all columns in the given
	 * <b>matrix</b>.
	 */
	public static double maxColumnSum(double[][] matrix)
	{
		double out = 0.0;
		for ( int j = 0; j < colDim(matrix); j++ )
			out = Math.max(out, Vector.sum(getColumn(matrix, j)));
		return out;
	}
	
	/**
	 * \brief Least sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>3.0</i> (left) and of <i>7.0</i> (right). The minimum 
	 * of these is 3.0.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the least sum of all columns in the given
	 * <b>matrix</b>.
	 */
	public static double minColumnSum(double[][] matrix)
	{
		double out = 0.0;
		for ( int j = 0; j < colDim(matrix); j++ )
			out = Math.max(out, Vector.sum(getColumn(matrix, j)));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>4.0</i> (top) and of <i>6.0</i> (bottom). The maximum 
	 * of these is 6.0.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the greatest sum of all rows in the given
	 * <b>matrix</b>.
	 */
	public static double maxRowSum(double[][] matrix)
	{
		double out = 0.0;
		for ( double[] row : matrix )
			out = Math.max(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Least sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>4.0</i> (top) and of <i>6.0</i> (bottom). The minimum 
	 * of these is 4.0.</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of the least sum of all rows in the given
	 * <b>matrix</b>.
	 */
	public static double minRowSum(double[][] matrix)
	{
		double out = 0.0;
		for ( double[] row : matrix )
			out = Math.min(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Frobenius norm of a given <b>matrix</b>.
	 * 
	 * <p>Note that the Frobenius norm is often called the Euclidean norm, but
	 * this may be confused with the L<sup>2</sup>-norm of a complex vector.
	 * See e.g. http://mathworld.wolfram.com/FrobeniusNorm.html for more
	 * details.</p>
	 * 
	 * <p>The original state of <b>matrix</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Square root of the sum of all elements squared.
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
		for ( double[] row : matrix )
			for ( double elem : row )
				out = Math.hypot(out, elem);
		return out;
	}
	
	/**
	 * \brief Finds the inverse of the given <b>matrix</b>, if possible.
	 * 
	 * <p>The inverse of a matrix is another matrix, say x, such that
	 * <b>matrix</b> * x = I, where I is the identity matrix.</p>
	 * 
	 * <p>Note that an exception will be throw if the <b>matrix</b>'s
	 * determinant is zero (i.e. it is singular).</p>
	 * 
	 * TODO JAMA solve() uses QRDecomposition if matrix is non-square
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Two-dimensional array of doubles that is the inverse of
	 * <b>matrix</b>.
	 */
	public static double[][] invert(double[][] matrix)
	{
		return (new LUDecomposition(matrix)).solve(identity(matrix)); 
	}
	
	/**
	 * \brief Calculate the determinant of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return double value of det(<b>matrix</b>).
	 */
	public static double determinant(double[][] matrix)
	{
		return (new LUDecomposition(matrix)).determinant();
	}
	
	/**
	 * \brief Condition number of the given <b>matrix</b>.
	 * 
	 * <p>The solution of a system of linear equations involving a
	 * <b>matrix</b> with a high condition number is very sensitive to small
	 * changes in that <b>matrix</b>.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @return Ratio of largest to smallest singular value.
	 */
	public static double condition(double[][] matrix)
	{
		return (new SingularValueDecomposition(matrix)).condition();
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 ************************************************************************/
	
	/**
	 * \brief Recast a double[][] as an int[][].
	 * 
	 * <p>Note that any digits after the decimal point are simply discarded.
	 * See {@link #round(double[][])}, etc for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>matrix</b> will be unaffected.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles. 
	 * @return	int[][] array where each element is the recast double in the
	 * corresponding position of <b>matrix</b>.
	 */
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
	
	/**
	 * \brief Round a double[][] as an int[][].
	 * 
	 * <p>Note that elements of <b>matrix</b> are rounded as in
	 * <i>Math.round(double x)</i>. See {@link #toDbl(double[][])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>matrix</b> will be unaffected.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles. 
	 * @return	int[][] array where each element is the rounded double in the
	 * corresponding position of <b>matrix</b>.
	 */
	public static int[][] round(double[][] matrix)
	{
		int m = rowDim(matrix);
		int n = colDim(matrix);
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = (int) Math.round(matrix[i][j]);
		return out;
	}
	
	/**
	 * \brief Floor a double[][] as an int[][].
	 * 
	 * <p>Note that elements of <b>matrix</b> are floored as in
	 * <i>Math.floor(double x)</i>. See {@link #toInt(double[][])}, etc
	 * for alternate methods. This method should give identical output to
	 * <i>toInt()</i> when all elements of <b>matrix</b> are positive.</p>
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>matrix</b> will be unaffected.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles. 
	 * @return	int[][] array where each element is the floored double in the
	 * corresponding position of <b>matrix</b>.
	 */
	public static int[][] floor(double[][] matrix)
	{
		int m = rowDim(matrix);
		int n = colDim(matrix);
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = (int) Math.floor(matrix[i][j]);
		return out;
	}
	
	/**
	 * \brief Ceiling a double[][] as an int[][].
	 * 
	 * <p>Note that elements of <b>matrix</b> are ceilinged as in
	 * <i>Math.ceil(double x)</i>. See {@link #toInt(double[][])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>matrix</b> will be unaffected.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles. 
	 * @return	int[][] array where each element is the ceilinged double in
	 * the corresponding position of <b>matrix</b>.
	 */
	public static int[][] ceil(double[][] matrix)
	{
		int m = rowDim(matrix);
		int n = colDim(matrix);
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = (int) Math.ceil(matrix[i][j]);
		return out;
	}
	
	/**
	 * \brief Recast an int[][] as a double[][].
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>matrix</b> will be unaffected.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles. 
	 * @return	double[][] array where each element is the recast int in the
	 * corresponding position of <b>matrix</b>.
	 */
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
	 * TWO MATRIX METHODS
	 ************************************************************************/
	
	/**
	 * \brief Solve the system of linear equations represented by three
	 * matrices, where <b>a</b>*x = <b>b</b> and x is the matrix to be found.
	 * 
	 * <p>Neither input matrix will be affected by this method and both
	 * matrices must have the same number of rows (i.e. m<sub>a</sub> =
	 * m<sub>b</sub>). The output matrix, x, will be a new
	 * n<sub>a</sub>-by-n<sub>b</sub> matrix.</p>
	 * 
	 * TODO JAMA code uses QRDecomposition if matrices are non-square.
	 * 
	 * @param a Two-dimensional array of doubles.
	 * @param b Two-dimensional array of doubles.
	 * @return Two-dimensional array of doubles x, such that <b>a</b>*x =
	 * <b>b</b>. 
	 */
	public static double[][] solve(double[][] a, double[][] b)
	{
		return (new LUDecomposition(a)).solve(b);
	}
	
	/**
	 * \brief Solve the system of linear equations represented by a
	 * <b>matrix</b> and two vectors, x and <b>vector</b>, where
	 * <b>matrix</b> * x = <b>vector</b> and x is the vector to be found.
	 * 
	 * <p>Neither input will be affected by this method and both
	 * inputs must have the same number of rows (i.e. m<sub>matrix</sub> =
	 * length<sub>vector</sub>). The output vector, x, will be a new
	 * vector of length n<sub>matrix</sub>.</p>
	 * 
	 * TODO JAMA code uses QRDecomposition if matrices are non-square.
	 * 
	 * @param matrix Two-dimensional array of doubles.
	 * @param vector One-dimensional array of doubles.
	 * @return One-dimensional array of doubles, x, such that
	 * <b>matrix</b> * x = <b>vector</b>. 
	 */
	public static double[] solve(double[][] matrix, double[] vector)
	{
		return (new LUDecomposition(matrix)).solve(vector);
	}
}