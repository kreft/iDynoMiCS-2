package linearAlgebra;

import utility.ExtraMath;

/**
 * \brief Library of useful matrix functions.
 * 
 * <p>By convention:<ul><li>a method <i>function(matrix)</i> returns a new
 * matrix,</li><li>the equivalent <i>functionEquals(matrix)</i> overwrites the
 * values in the argument matrix,</li><li> and <i>functionTo(destination,
 * source)</i> uses <i>source</i> as the input and writes the result into 
 * <i>destination</i>.</li></ul> If in doubt, the description of the method
 * should make it clear which arguments are preserved and which are
 * overwritten.</p>
 * 
 * <p>The layout of this class is:<p>
 * <ul>
 *   <li>standard new matrices</i>
 *   <li>dimensions</i>
 *   <li>copying and setting</i>
 *   <li>checking methods (isZero, etc)</li>
 *   <li>basic arithmetic (+, -, *, /)</li>
 *   <li>submatrices and reordering</li>
 *   <li>scalars from matrices (sum, min/max, etc)</i>
 *   <li>new random matrices</li>
 *   <li>converting between integer and double</li>
 *   <li>advanced methods</li>
 * </ul>
 * 
 * <p>Note that all arrays from the <b>array.Vector</b> class are treated here
 * as column vectors. These may be converted to row vectors here using the 
 * {@link #transpose(int[] vector)} or {@link #transpose(double[] vector)}
 * methods.</p> 
 * 
 * <p>Parts of this class are adapted from the JAMA package by Robert Clegg.</p>
 * 
 * <p><b>JAMA Copyright Notice</b>: This software is a cooperative product of 
 * The MathWorks and the National Institute of Standards and Technology (NIST) 
 * which has been released to the public domain. Neither The MathWorks nor NIST
 * assumes any responsibility whatsoever for its use by other parties, and 
 * makes no guarantees, expressed or implied, about its quality, reliability, 
 * or any other characteristic.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class Matrix
{
	/**
	 * Character that separates rows of a matrix in {@code String} format.
	 */
	public final static String DELIMITER = ";";
	
	/*************************************************************************
	 * STANDARD NEW MATRICES
	 ************************************************************************/
	
	/* Given value */
	
	/**
	 * \brief A new m-by-n matrix of {@code int}s.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param value Fill the matrix with this {@code int} value.
	 * @return Two-dimensional array of {@code int}s, all of <b>value</b>
	 *    given.
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
	 * \brief A new square matrix of {@code int}s.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @param value Fill the matrix with this {@code int} value.
	 * @return Two-dimensional array of {@code int}s, all of <b>value</b>
	 *    given.
	 * @see #matrix(int, int, int)
	 * @see #matrix(int, double)
	 */
	public static int[][] matrix(int mn, int value)
	{
		return matrix(mn, mn, value);
	}
	
	/**
	 * \brief A new m-by-n matrix of {@code double}s.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param value Fill the matrix with this {@code double} value.
	 * @return Two-dimensional array of {@code double}s, all of <b>value</b>
	 *    given.
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
	 * \brief A new square matrix of {@code double}s.
	 * 
	 * @param mn Number of rows = number of columns.
	 * @param value Fill the matrix with this {@code double} value.
	 * @return Two-dimensional array of {@code double}s, all of <b>value</b>
	 *    given.
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
	 * @return Two-dimensional array of {@code int}s, all of value 0.
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
	 * @return Two-dimensional array of {@code int}s, all of value 0.
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
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return Two-dimensional array of all of {@code int} value 0, with the same
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
	 * \brief A new m-by-n matrix of {@code double} zeros.
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of {@code double}s, all of value 0.0.
	 * @see #zerosDbl(int)
	 * @see #zeros(double[][])
	 * @see #zerosInt(int, int)
	 */
	public static double[][] zerosDbl(int m, int n)
	{
		return matrix(m, n, 0.0);
	}
	
	/**
	 * \brief Constructs a square matrix full of {@code double} zeros.
	 * 
	 * @param mn Number of rows (same as number of columns)
	 * @return {@code double[][]} array composed of zeros.
	 * @see #zerosDbl(int, int)
	 * @see #zeros(double[][])
	 * @see #zerosInt(int)
	 */
	public static double[][] zerosDbl(int mn)
	{
		return matrix(mn, 0.0);
	}
	
	/**
	 * \brief A new m-by-n matrix of {@code double} zeros.
	 * 
	 * @return Two-dimensional array of {@code double}s (preserved).
	 * @param matrix Two-dimensional array of {@code double}s.
	 * @return Two-dimensional array of all of {@code double} value 0.0, with
	 *    the same number of rows and of columns as <b>matrix</b>.
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
	 * \brief Overwrite the given <b>matrix</b> with an identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param matrix Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 */
	public static void identityTo(int[][] matrix)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[i].length; j++ )
				matrix[i][j] = ( i == j ) ? 1 : 0;
	}
	
	/**
	 * \brief A new identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of {@code int}s with ones on the diagonal
	 *    and zeros elsewhere.
	 * @see #identityInt(int)
	 * @see #identity(int[][])
	 * @see #identityDbl(int, int)
	 */
	public static int[][] identityInt(int m, int n)
	{
		int[][] out = new int[m][n];
		identityTo(out);
		return out;
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of {@code int}s with ones on the diagonal
	 *    and zeros elsewhere.
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
	 * @param matrix Two-dimensional array of {@code int}s.
	 * @return Two-dimensional array of {@code int}s with ones on the diagonal
	 *    and zeros elsewhere, with the same number of rows and of columns as
	 *    <b>matrix</b>.
	 * @see #identityInt(int, int)
	 * @see #identityInt(int)
	 * @see #identity(double[][])
	 */
	public static int[][] identity(int[][] matrix)
	{
		return identityInt(rowDim(matrix), colDim(matrix));
	}
	

	/**
	 * \brief Overwrite the given <b>matrix</b> with an identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 */
	public static void identityTo(double[][] matrix)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[i].length; j++ )
				matrix[i][j] = ( i == j ) ? 1.0 : 0.0;
	}
	
	
	/**
	 * \brief A new identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of {@code double}s with ones on the
	 *    diagonal and zeros elsewhere.
	 * @see #identityDbl(int, int)
	 * @see #identity(double[][])
	 * @see #identityInt(int, int)
	 */
	public static double[][] identityDbl(int m, int n)
	{
		double[][] out = new double[m][n];
		identityTo(out);
		return out;
	}
	
	/**
	 * \brief A new square identity matrix.
	 * 
	 * <p>An identity matrix is filled with zeros, except on the main diagonal
	 * where it has ones instead.</p>
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of {@code double}s with ones on the
	 *    diagonal and zeros elsewhere.
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
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return Two-dimensional array of {@code double}s with ones on the
	 *    diagonal and zeros elsewhere, with the same number of rows and of
	 *    columns as <b>matrix</b>.
	 * @see #identityDbl(int, int)
	 * @see #identityDbl(int)
	 * @see #identity(int[][])
	 */
	public static double[][] identity(double[][] matrix)
	{
		return identityDbl(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief Gets a new matrix of integers from a string.
	 * 
	 * @param matrixString String containing a matrix of integers.
	 * @return int[][] matrix of integers from this string.
	 * @see #dblFromString(String)
	 */
	public static int[][] intFromString(String matrixString)
	{
		String[] rows = matrixString.split(DELIMITER);
		int[][] matrix = new int[rows.length][];
		for ( int i = 0; i < rows.length; i++ )
			matrix[i] = Vector.intFromString(rows[i]);
		return matrix;
	}
	
	/**
	 * \brief Gets a new matrix of doubles from a string.
	 * 
	 * @param matrixString String containing a matrix of doubles.
	 * @return int[][] matrix of doubles from this string.
	 * @see #intFromString(String)
	 */
	public static double[][] dblFromString(String matrixString)
	{
		String[] rows = matrixString.split(DELIMITER);
		double[][] matrix = new double[rows.length][];
		for ( int i = 0; i < rows.length; i++ )
			matrix[i] = Vector.dblFromString(rows[i]);
		return matrix;
	}
	
	/**
	 * \brief Returns integer matrix in string format.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return String representation of this <b>matrix</b>.
	 */
	public static String toString(int[][] matrix)
	{
		StringBuffer out = new StringBuffer();
		toString(matrix, out);
		return out.toString();
	}
	
	/**
	 * \brief Returns double matrix in string format.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return String representation of this <b>matrix</b>.
	 */
	public static String toString(double[][] matrix)
	{
		StringBuffer out = new StringBuffer();
		toString(matrix, out);
		return out.toString();
	}

	public static String toString(double[][] matrix, String delimiter)
	{
		StringBuffer out = new StringBuffer();
		toString(matrix, out, delimiter);
		return out.toString();
	}
	
	/**
	 * \brief Converts the given <b>matrix</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(int[][] matrix, StringBuffer buffer)
	{
		int n = matrix.length - 1;
		for ( int i = 0; i < n; i++ )
		{
			Vector.toString(matrix[i], buffer);
			buffer.append(DELIMITER);
		}
		Vector.toString(matrix[n], buffer);
	}
	
	/**
	 * \brief Converts the given <b>matrix</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(double[][] matrix, StringBuffer buffer, String delimiter)
	{
		int n = matrix.length - 1;
		for ( int i = 0; i < n; i++ )
		{
			Vector.toString(matrix[i], buffer);
			buffer.append(delimiter);
		}
		Vector.toString(matrix[n], buffer);
	}

	public static void toString(double[][] matrix, StringBuffer buffer)
	{
		toString(matrix, buffer, DELIMITER);
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	/**
	 * \brief Number of rows in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} number of rows in the given <b>matrix</b>.
	 * @see #colDim(int[][])
	 * @see #maxDim(int[][])
	 * @see #minDim(int[][])
	 * @see #rowDim(double[][])
	 */
	public static int rowDim(int[][] matrix)
	{
		return matrix.length;
	}
	
	/**
	 * \brief Number of rows in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code int} number of rows in the given <b>matrix</b>.
	 * @see #colDim(double[][])
	 * @see #maxDim(double[][])
	 * @see #minDim(double[][])
	 * @see #rowDim(int[][])
	 */
	public static int rowDim(double[][] matrix)
	{
		return matrix.length;
	}
	
	/**
	 * \brief Number of columns in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code int} number of columns in the given <b>matrix</b>.
	 * @see #rowDim(double[][])
	 * @see #maxDim(int[][])
	 * @see #minDim(int[][])
	 * @see #colDim(int[][])
	 */
	public static int colDim(double[][] matrix)
	{
		return matrix[0].length;
	}
	
	/**
	 * \brief Number of columns in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} number of columns in the given <b>matrix</b>.
	 * @see #rowDim(int[][])
	 * @see #maxDim(double[][])
	 * @see #minDim(double[][])
	 * @see #colDim(double[][])
	 */
	public static int colDim(int[][] matrix)
	{
		return matrix[0].length;
	}
	
	/**
	 * \brief Returns the size of the largest of the two dimensions (# rows or
	 * # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return Size of the largest dimension of the given <b>matrix</b>.
	 * @see #colDim(int[][])
	 * @see #rowDim(int[][])
	 * @see #minDim(int[][])
	 * @see #maxDim(double[][])
	 */
	public static int maxDim(int[][] matrix)
	{
		return Math.max(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief Returns the size of the largest of the two dimensions (# rows or
	 * # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return Size of the largest dimension of the given <b>matrix</b>.
	 * @see #colDim(double[][])
	 * @see #rowDim(double[][])
	 * @see #minDim(double[][])
	 * @see #maxDim(int[][])
	 */
	public static int maxDim(double[][] matrix)
	{
		return Math.max(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief Returns the size of the smallest of the two dimensions (# rows
	 * or # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return Size of the smallest dimension of the given <b>matrix</b>.
	 * @see #colDim(int[][])
	 * @see #rowDim(int[][])
	 * @see #maxDim(int[][])
	 * @see #minDim(double[][])
	 */
	public static int minDim(int[][] matrix)
	{
		return Math.min(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief Returns the size of the smallest of the two dimensions (# rows
	 * or # columns) of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return Size of the smallest dimension of the given <b>matrix</b>.
	 * @see #colDim(double[][])
	 * @see #rowDim(double[][])
	 * @see #maxDim(double[][])
	 * @see #minDim(int[][])
	 */
	public static int minDim(double[][] matrix)
	{
		return Math.min(rowDim(matrix), colDim(matrix));
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
			Vector.copyTo(destination[i], matrix[i]);
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
		int[][] out = new int[matrix.length][];
		for ( int i = 0; i < matrix.length; i++ )
			out[i] = Vector.copy(matrix[i]);
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
			Vector.copyTo(destination[i], matrix[i]);
	}
	
	public static void copyTo(Double[][] destination, Double[][] matrix)
	{
		for ( int i = 0; i < matrix.length; i++ )
			Vector.copyTo(destination[i], matrix[i]);
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
		double[][] out = new double[matrix.length][];
		for ( int i = 0; i < matrix.length; i++ )
			out[i] = Vector.copy(matrix[i]);
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
		for ( int[] row : matrix )
			Vector.setAll(row, value);
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
		for ( double[] row : matrix )
			Vector.setAll(row, value);
	}
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to zero.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 */
	public static void reset(int[][] matrix)
	{
		setAll(matrix, 0);
	}
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to zero.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 */
	public static void reset(double[][] matrix)
	{
		setAll(matrix, 0.0);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>matrix</b>.
	 */
	public static void restrictMinimum(int[][] matrix, int newMinimum)
	{
		for ( int[] row : matrix )
			Vector.restrictMinimum(row, newMinimum);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>matrix</b>.
	 */
	public static void restrictMinimum(double[][] matrix, double newMinimum)
	{
		for ( double[] row : matrix )
			Vector.restrictMinimum(row, newMinimum);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix</b> to take a value less
	 * than or equal to <b>newMaximum</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>matrix</b>.
	 */
	public static void restrictMaximum(int[][] matrix, int newMaximum)
	{
		for ( int[] row : matrix )
			Vector.restrictMaximum(row, newMaximum);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix</b> to take a value less
	 * than or equal to <b>newMaximum</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>matrix</b>.
	 */
	public static void restrictMaximum(double[][] matrix, double newMaximum)
	{
		for ( double[] row : matrix )
			Vector.restrictMaximum(row, newMaximum);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix/b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 */
	public static void makeNonnegative(int[][] matrix)
	{
		restrictMinimum(matrix, 0);
	}
	
	/**
	 * \brief Force all elements in this <b>matrix/b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 */
	public static void makeNonnegative(double[][] matrix)
	{
		restrictMinimum(matrix, 0.0);
	}
	
	/**
	 * \brief Set all elements in the column of a given <b>matrix</b> to a new
	 * value.
	 * 
	 * @param matrix Two-dimensional array of integers (overwritten).
	 * @param index {@code int} index of the column required.
	 * @param value Fill the column with this integer value.
	 */
	public static void setColumnTo(int[][] matrix, int index, int value)
	{
		for ( int[] row : matrix )
			row[index] = value;
	}
	
	/**
	 * \brief Set all elements in the column of a given <b>matrix</b> to a new
	 * value.
	 * 
	 * @param matrix Two-dimensional array of doubles (overwritten).
	 * @param index {@code int} index of the column required.
	 * @param value Fill the column with this double value.
	 */
	public static void setColumnTo(double[][] matrix, int index, double value)
	{
		for ( double[] row : matrix )
			row[index] = value;
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
	
	/* Two or three matrices */
	
	/**
	 * \brief Check that the two matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Matrix dimensions must agree.
	 * @see #checkDimensionsSame(int[][], int[][], int[][])
	 * @see #checkDimensions(int[][])
	 * @see #checkSquare(int[][])
	 * @see #checkDimensionsSame(double[][], double[][])
	 */
	public static void checkDimensionsSame(int[][] a, int[][] b)
	{
		if ( rowDim(a) != rowDim(b) || colDim(a) != colDim(b) )
		{
			throw new 
				IllegalArgumentException("Matrix dimensions must agree.");
		}
	}
	
	/**
	 * \brief Check that the three matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @param c Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 * @see #checkDimensionsSame(int[][], int[][])
	 * @see #checkDimensions(int[][])
	 * @see #checkSquare(int[][])
	 * @see #checkDimensionsSame(double[][], double[][], double[][])
	 */
	public static void checkDimensionsSame(int[][] a, int[][] b, int[][] c)
	{
		checkDimensionsSame(a, b);
		checkDimensionsSame(a, c);
	}
	
	/**
	 * \brief Check that the two matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 * @see #checkDimensions(double[][])
	 * @see #checkDimensionsSame(double[][], double[][], double[][])
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
	
	/**
	 * \brief Check that the three matrices given have the same dimensions.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @param c Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Matrix row dimensions must agree.
	 * @see #checkDimensionsSame(double[][], double[][])
	 * @see #checkDimensions(double[][])
	 * @see #checkSquare(double[][])
	 * @see #checkDimensionsSame(int[][], int[][], int[][])
	 */
	public static void checkDimensionsSame(double[][] a, double[][] b, double[][] c)
	{
		checkDimensionsSame(a, b);
		checkDimensionsSame(a, c);
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
	
	/**
	 * \brief Check if the given <b>matrix</b> is composed of zeros.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean} showing whether <b>matrix</b> is all zeros
	 * (true) or contains a non-zero (false).
	 * @see #isNonnegative(int[][])
	 * @see #isZero(double[][])
	 * @see #isZero(double[][], double)
	 */
	public static boolean isZero(int[][] matrix)
	{
		for ( int[] row : matrix )
			if ( ! Vector.isZero(row) )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if the given <b>matrix</b> is composed of zeros.
	 * 
	 * <p>Note that this method is vulnerable to numerical issues, i.e. values
	 * that are extremely close to zero but not equal because of rounding.
	 * Consider using {@link #isZero(double[][] matrix, double tolerance)}
	 * instead.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved). 
	 * @return {@code boolean} showing whether <b>matrix</b> is all zeros 
	 * (true) or contains a non-zero (false).
	 * @see #isZero(double[][], double)
	 * @see #isNonnegative(double[][])
	 * @see #isZero(int[][])
	 */
	public static boolean isZero(double[][] matrix)
	{
		for ( double[] row : matrix )
			if ( ! Vector.isZero(row) )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if the given <b>matrix</b> is composed of zeros.
	 * 
	 * <p>If confident that numerical issues can be ignored, consider using
	 * {@link #isZero(double[][] matrix)} instead (slightly faster).</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param tolerance {@code double} value for the absolute tolerance, i.e.
	 * <i>|x<sub>i,j</sub>|</i> <= tolerance will be accepted as close enough
	 * to zero (helps avoid numerical issues). 
	 * @return {@code boolean} showing whether <b>matrix</b> is all zeros 
	 * (true) or contains a non-zero (false).
	 * @see #isZero(double[][])
	 * @see #isNonnegative(double[][])
	 * @see #isZero(int[][])
	 */
	public static boolean isZero(double[][] matrix, double tolerance)
	{
		for ( double[] row : matrix )
			if ( ! Vector.isZero(row, tolerance) )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if all the elements of the given <b>matrix</b> are
	 * non-negative, i.e. greater than or equal to zero.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean} showing whether all elements or <b>vector</b> 
	 * are >= 0 (true) or at least one is < 0 (false).
	 * @see #isZero(int[][])
	 * @see #isNonnegative(double[][])
	 */
	public static boolean isNonnegative(int[][] matrix)
	{
		for ( int[] row : matrix )
			if ( ! Vector.isNonnegative(row) )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if all elements in a <b>matrix</b> are positive or zero.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code boolean} reporting whether the <b>vector</b> contains 
	 * negative elements (false) or if all elements are greater than or equal
	 * to zero (true).
	 * @see #isZero(double[][])
	 * @see #isZero(double[][], double)
	 * @see #isNonnegative(int[][])
	 */
	public static boolean isNonnegative(double[][] matrix)
	{
		for ( double[] row : matrix )
			if ( ! Vector.isNonnegative(row) )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the two given matrices have the same elements, in the same
	 * order.
	 * 
	 * @param a Two-dimensional array of {@code int}s (preserved).
	 * @param b Two-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean}: true if they are the same, false if at least
	 * one element-element pair differs.
	 * @see #areSame(double[][], double[][])
	 */
	public static boolean areSame(int[][] a, int[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			if ( ! Vector.areSame(a[i], b[i]) )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the two given vectors have the same elements, in the same
	 * order.
	 * 
	 * <p>Note that this method is vulnerable to numerical issues, i.e. values
	 * that are extremely close together but not equal because of rounding.
	 * Consider using 
	 * {@link #areSame(double[][] a, double[][] b, double tolerance)}
	 * instead.</p>
	 * 
	 * @param a Two-dimensional array of {@code double}s (preserved).
	 * @param b Two-dimensional array of {@code double}s (preserved).
	 * @return {@code boolean}: true if they are the same, false if at least
	 * one element-element pair differs.
	 * @see #areSame(double[][], double[][], double)
	 * @see #areSame(int[][], int[][])
	 */
	public static boolean areSame(double[][] a, double[][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			if ( ! Vector.areSame(a[i], b[i]) )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the two given vectors have the same elements, in the same
	 * order.
	 * 
	 * <p>If confident that numerical issues can be ignored, consider using
	 * {@link #areSame(double[][] a, double[][] b)} instead (slightly faster).
	 * </p>
	 * 
	 * @param a Two-dimensional array of {@code double}s (preserved).
	 * @param b Two-dimensional array of {@code double}s (preserved).
	 * @param tolerance {@code double} value for the absolute tolerance, i.e.
	 * <i>|a<sub>i,j</sub> - b<sub>i,j</sub>|</i> <= tolerance will be accepted
	 * as close enough to zero (helps avoid numerical issues). 
	 * @return {@code boolean}: true if they are the same, false if at least
	 * one element-element pair differs.
	 * @see #areSame(double[][], double[][])
	 * @see #areSame(int[][], int[][])
	 */
	public static boolean areSame(double[][] a, double[][] b, double tolerance)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			if ( ! Vector.areSame(a[i], b[i], tolerance) )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the given matrix is symmetric, i.e. it is equal to its
	 * transpose.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code true} if <b>matrix</b> is symmetric, {@code false} if it
	 * is asymmetric.
	 */
	public static boolean isSymmetric(int[][] matrix)
	{
		checkDimensions(matrix);
		if ( ! isSquare(matrix) )
			return false;
		int n = rowDim(matrix);
		for ( int i = 0; i < n - 1; i++ )
			for ( int j = i + 1; j < n; j++ )
				if ( matrix[i][j] != matrix[j][i] )
					return false;
		return true;
	}
	
	/**
	 * \brief See if the given matrix is symmetric, i.e. it is equal to its
	 * transpose.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code true} if <b>matrix</b> is symmetric, {@code false} if it
	 * is asymmetric.
	 */
	public static boolean isSymmetric(double[][] matrix)
	{
		checkDimensions(matrix);
		if ( ! isSquare(matrix) )
			return false;
		int n = rowDim(matrix);
		for ( int i = 0; i < n - 1; i++ )
			for ( int j = i + 1; j < n; j++ )
				if ( matrix[i][j] != matrix[j][i] )
					return false;
		return true;
	}
	
	/**
	 * \brief See if the given matrix is symmetric, i.e. it is equal to its
	 * transpose.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param tolerance {@code double} value for the absolute tolerance, i.e.
	 * |a<sub>i,j</sub> - a<sub>j,i</sub>| <= tolerance will be accepted
	 * as close enough to zero (helps avoid numerical issues). 
	 * @return {@code true} if <b>matrix</b> is symmetric, {@code false} if it
	 * is asymmetric.
	 */
	public static boolean isSymmetric(double[][] matrix, double absTol)
	{
		checkDimensions(matrix);
		if ( ! isSquare(matrix) )
			return false;
		int n = rowDim(matrix);
		for ( int i = 0; i < n - 1; i++ )
			for ( int j = i + 1; j < n; j++ )
				if ( ! ExtraMath.areEqual(matrix[i][j], matrix[j][i], absTol) )
					return false;
		return true;
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
		checkDimensionsSame(destination, matrix);
		for ( int i = 0; i < rowDim(matrix); i++ )
			Vector.addTo(destination[i], matrix[i], value);
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
	 * <p>Both input matrices must have the same dimensions.</p>
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
		checkDimensionsSame(destination, matrix);
		for ( int i = 0; i < rowDim(matrix); i++ )
			Vector.addTo(destination[i], matrix[i], value);
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
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.addTo(destination[i], a[i], b[i]);
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
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.addTo(destination[i], a[i], b[i]);
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
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> - <b>b</b></p>
	 * 
	 * @param destination Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #minus(int[][], int[][])
	 * @see #minusEquals(int[][], int[][])
	 * @see #addTo(int[][], int[][], int)
	 * @see #minusTo(double[][], double[][], double[][]) 
	 */
	public static void minusTo(int[][] destination, int[][] a, int[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.minusTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into a new int[][].
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @return new int[][] array of <b>a</b> - <b>b</b>.
	 * @see #minusTo(int[][], int[][], int[][])
	 * @see #minusEquals(int[][], int[][])
	 * @see #addTo(int[][], int[][], int)
	 * @see #minus(double[][], double[][]) 
	 */
	public static int[][] minus(int[][] a, int[][] b)
	{
		int[][] out = new int[a.length][a[0].length];
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into <b>a</b>.
	 * 
	 * <p><b>a</b> is overwritten with <b>a</b> - <b>b</b>.</p>
	 * 
	 * @param a Two-dimensional array of integers (overwritten).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #minusTo(int[][], int[][], int[][])
	 * @see #minus(int[][], int[][])
	 * @see #addEquals(int[][], int)
	 * @see #minusEquals(double[][], double[][])
	 */
	public static void minusEquals(int[][] a, int[][] b)
	{
		minusTo(a, a, b);
	}
	
	/**
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> - <b>b</b></p>
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #minus(double[][], double[][])
	 * @see #minusEquals(double[][], double[][])
	 * @see #addTo(double[][], double[][], double)
	 * @see #minusTo(int[][], int[][], int[][]) 
	 */
	public static void minusTo(double[][] destination, double[][] a, double[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.minusTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into a new double[][].
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @return new double[][] array of <b>a</b> - <b>b</b>.
	 * @see #minusTo(double[][], double[][], double[][])
	 * @see #minusEquals(double[][], double[][])
	 * @see #addTo(double[][], double[][], double)
	 * @see #minus(int[][], int[][]) 
	 */
	public static double[][] minus(double[][] a, double[][] b)
	{
		double[][] out = new double[a.length][a[0].length];
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract matrix <b>b</b> from matrix <b>a</b>, element-by-element,
	 * writing the result into <b>a</b>.
	 * 
	 * <p><b>a</b> is overwritten with <b>a</b> - <b>b</b>.</p>
	 * 
	 * @param a Two-dimensional array of doubles (overwritten).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #minusTo(double[][], double[][], double[][])
	 * @see #minus(double[][], double[][])
	 * @see #addEquals(double[][], double)
	 * @see #minusEquals(int[][], int[][])
	 */
	public static void minusEquals(double[][] a, double[][] b)
	{
		minusTo(a, a, b);
	}
	
	/* Multiplication */
	
	/**
	 * \brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>,, writing the result into <b>destination</b>.
	 * 
	 * <p>Both input matrices must have the same dimensions.</p>
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
		checkDimensionsSame(destination, matrix);
		for ( int i = 0; i < rowDim(matrix); i++ )
			Vector.timesTo(destination[i], matrix[i], value);
	}
	
	/**
	 *\brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>,, returning the result as a new int[][].
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
	 * \brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>, overwriting the old values of <b>matrix</b>.
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
	 * \brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>, writing the result into <b>destination</b>.
	 * 
	 * <p>Both input matrices must have the same dimensions.</p>
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
		checkDimensionsSame(destination, matrix);
		for ( int i = 0; i < rowDim(matrix); i++ )
			Vector.timesTo(destination[i], matrix[i], value);
	}
	
	/**
	 * \brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>, returning the result as a new double[][].
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
	 * \brief Multiply every element of a <b>matrix</b> by a scalar
	 * <b>value</b>, overwriting the old values of <b>matrix</b>.
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
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * <p>All three inputs must have the same dimensions.</p>
	 * 
	 * @param destination Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #elemTimes(int[][], int[][])
	 * @see #elemTimesEquals(int[][], int[][])
	 * @see #timesTo(int[][], int[][], int[][])
	 * @see #elemTimesTo(double[][], double[][], double[][])
	 */
	public static void elemTimesTo(int[][] destination, int[][] a, int[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.timesTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * writing the result into a new int[][].
	 * 
	 * <p>Both inputs must have the same dimensions.</p>
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @return int[][] array of <b>a</b> times <b>b</b> element-wise.
	 * @see #elemTimesTo(int[][], int[][], int[][])
	 * @see #elemTimesEquals(int[][], int[][])
	 * @see #times(int[][], int[][])
	 * @see #elemTimes(double[][], double[][])
	 */
	public static int[][] elemTimes(int[][] a, int[][] b)
	{
		int[][] out = new int[a.length][a[0].length];
		elemTimesTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * overwriting <b>a</b> with the result.
	 * 
	 * <p>Both inputs must have the same dimensions.</p>
	 * 
	 * @param a Two-dimensional array of integers (overwritten).
	 * @param b Two-dimensional array of integers (preserved).
	 * @see #elemTimesTo(int[][], int[][], int[][])
	 * @see #elemTimes(int[][], int[][])
	 * @see #timesEquals(int[][], int[][])
	 * @see #elemTimesEquals(double[][], double[][])
	 */
	public static void elemTimesEquals(int[][] a, int[][] b)
	{
		elemTimesTo(a, a, b);
	}
	
	/**
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * writing the result into matrix <b>destination</b>.
	 * 
	 * <p>All three inputs must have the same dimensions.</p>
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #elemTimes(double[][], double[][])
	 * @see #elemTimesEquals(double[][], double[][])
	 * @see #timesTo(double[][], double[][], double[][])
	 * @see #elemTimesTo(int[][], int[][], int[][])
	 */
	public static void elemTimesTo(double[][] destination, double[][] a, double[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.timesTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * writing the result into a new double[][].
	 * 
	 * <p>Both inputs must have the same dimensions.</p>
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @return double[][] array of <b>a</b> times <b>b</b> element-wise.
	 * @see #elemTimesTo(double[][], double[][], double[][])
	 * @see #elemTimesEquals(double[][], double[][])
	 * @see #times(double[][], double[][])
	 * @see #elemTimes(int[][], int[][])
	 */
	public static double[][] elemTimes(double[][] a, double[][] b)
	{
		double[][] out = new double[a.length][a[0].length];
		elemTimesTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Element-by-element, multiply matrix <b>a</b> by matrix <b>b</b>,
	 * overwriting <b>a</b> with the result.
	 * 
	 * <p>Both inputs must have the same dimensions.</p>
	 * 
	 * @param a Two-dimensional array of doubles (overwritten).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @see #elemTimesTo(double[][], double[][], double[][])
	 * @see #elemTimes(double[][], double[][])
	 * @see #timesEquals(double[][], double[][])
	 * @see #elemTimesEquals(int[][], int[][])
	 */
	public static void elemTimesEquals(double[][] a, double[][] b)
	{
		elemTimesTo(a, a, b);
	}
	
	/**
	 * \brief Using matrix multiplication, multiply matrix <b>a</b> by matrix 
	 * <b>b</b>, writing the result into matrix <b>destination</b>.
	 * 
	 * <ul>
	 * <li><b>destination</b> must have the same number of rows as <b>a</b>.</li>
	 * <li><b>destination</b> must have the same number of columns as <b>b</b>.</li>
	 * <li><b>a</b> must have as many columns as <b>b</b> has rows.</li>
	 * </ul>
	 * 
	 * @param destination Two-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Matrix outer row dimensions must
	 * agree.
	 * @exception IllegalArgumentException Matrix inner row dimensions must
	 * agree.
	 * @see #times(int[][], int[][])
	 * @see #elemTimesTo(int[][], int[][], int[][])
	 * @see #timesTo(double[][], double[][], double[][])
	 */
	public static void timesTo(int[][] destination, int[][] a, int[][] b)
	{
		if ( rowDim(destination) != rowDim(a) ||
											colDim(destination) != colDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix outer row dimensions must agree.");
		}
		if ( colDim(a) != rowDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		int[] aRow;
		int[] bCol;
		for ( int j = 0; j < colDim(b); j++ )
		{
			bCol = getColumn(b, j);
			for ( int i = 0; i < rowDim(a); i++ )
			{
				aRow = getRowAsColumn(a, i);
				destination[i][j] = Vector.dotProduct(aRow, bCol);
			}
		}
	}
	
	/**
	 * \brief Using matrix multiplication, multiply matrix <b>a</b> by matrix 
	 * <b>b</b>, writing the result into a new int[][].
	 * 
	 * <p><b>a</b> must have as many columns as <b>b</b> has rows.</p>
	 * 
	 * @param a Two-dimensional array of integers (preserved).
	 * @param b Two-dimensional array of integers (preserved).
	 * @return new int[][] of the matrix multiplication <b>a</b> * <b>b</b>.
	 * @see #timesTo(int[][], int[][], int[][])
	 * @see #elemTimes( int[][], int[][])
	 * @see #times(double[][], double[][])
	 */
	public static int[][] times(int[][] a, int[][] b)
	{
		int[][] out = new int[rowDim(a)][colDim(b)];
		timesTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Using matrix multiplication, multiply matrix <b>a</b> by matrix 
	 * <b>b</b>, writing the result into matrix <b>destination</b>.
	 * 
	 * <ul>
	 * <li><b>destination</b> must have the same number of rows as <b>a</b>.</li>
	 * <li><b>destination</b> must have the same number of columns as <b>b</b>.</li>
	 * <li><b>a</b> must have as many columns as <b>b</b> has rows.</li>
	 * </ul>
	 * 
	 * @param destination Two-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Matrix outer row dimensions must
	 * agree.
	 * @exception IllegalArgumentException Matrix inner row dimensions must
	 * agree.
	 * @see #times(double[][], double[][])
	 * @see #elemTimesTo(double[][], double[][], double[][])
	 * @see #timesTo(int[][], int[][], int[][])
	 */
	public static void timesTo(double[][] destination,
								double[][] a, double[][] b)
	{
		if ( rowDim(destination) != rowDim(a) ||
											colDim(destination) != colDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix outer row dimensions must agree.");
		}
		if ( colDim(a) != rowDim(b) )
		{
			throw new IllegalArgumentException(
								"Matrix inner row dimensions must agree.");
		}
		double[] aRow;
		double[] bCol;
		for ( int j = 0; j < colDim(b); j++ )
		{
			bCol = getColumn(b, j);
			for ( int i = 0; i < rowDim(a); i++ )
			{
				aRow = getRowAsColumn(a, i);
				destination[i][j] = Vector.dotProduct(aRow, bCol);
			}
		}
	}
	
	/**
	 * \brief Using matrix multiplication, multiply matrix <b>a</b> by matrix 
	 * <b>b</b>, writing the result into a new double[][].
	 * 
	 * <p><b>a</b> must have as many columns as <b>b</b> has rows.</p>
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @return new double[][] of the matrix multiplication <b>a</b> * <b>b</b>.
	 * @see #timesTo(double[][], double[][], double[][])
	 * @see #elemTimes(double[][], double[][])
	 * @see #times(int[][], int[][])
	 */
	public static double[][] times(double[][] a, double[][] b)
	{
		double[][] out = new double[rowDim(a)][colDim(b)];
		timesTo(out, a, b);
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
	 * \brief For each element of a matrix <b>a</b>, divide by the
	 * corresponding element of matrix <b>b</b>, and write the result into
	 * the <b>destination</b> matrix.
	 * 
	 * @param destination Two-dimensional array of doubles (overwritten).
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 */
	public static void elemDivideTo(
			double[][] destination, double[][] a, double[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.divideTo(destination[i], a[i], b[i]);
	}

	public static void elemRatioTo(
			double[][] destination, double[][] a, double[][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < rowDim(a); i++ )
			Vector.ratioTo(destination[i], a[i], b[i]);
	}

	
	/**
	 * \brief For each element of a matrix <b>a</b>, divide by the
	 * corresponding element of matrix <b>b</b>, and write the result into
	 * a new matrix.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (preserved).
	 * @return New two-dimensional array of doubles.
	 */
	public static double[][] elemDivide(double[][] a, double[][] b)
	{
		double[][] out = zeros(a);
		elemDivideTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief For each element of a matrix <b>a</b>, divide by the
	 * corresponding element of matrix <b>b</b>, overwriting the element of
	 * <b>a</b> with the result.
	 * 
	 * @param a Two-dimensional array of doubles (overwritten).
	 * @param b Two-dimensional array of doubles (preserved).
	 */
	public static void elemDivideEqualsA(double[][] a, double[][] b)
	{
		elemDivideTo(a, a, b);
	}

	/**
	 * \brief For each element of a matrix <b>a</b>, divide by the
	 * corresponding element of matrix <b>b</b>, overwriting the element of
	 * <b>b</b> with the result.
	 * 
	 * @param a Two-dimensional array of doubles (preserved).
	 * @param b Two-dimensional array of doubles (overwritten).
	 */
	public static void elemDivideEqualsB(double[][] a, double[][] b)
	{
		elemDivideTo(b, a, b);
	}
	
	/*************************************************************************
	 * SUBMATRICES
	 ************************************************************************/
	
	/**
	 * \brief Extract a subsection of the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @param rows int[] array of row indices to include, in the order given.
	 * @param cols int[] array of column indices to include, in the order
	 * given.
	 * @return Two-dimensional array of integers selectively copied from
	 * <b>matrix</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check submatrix indices.
	 */
	public static int[][] submatrix(int[][] matrix, int[] rows, int[] cols)
	{
		int[][] out = new int[rows.length][];
		try
		{
			for ( int i = 0; i < rows.length; i++ )
				out[i] = Vector.subset(matrix[rows[i]], cols);
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
		double[][] out = new double[rows.length][];
		try
		{
			for ( int i = 0; i < rows.length; i++ )
				out[i] = Vector.subset(matrix[rows[i]], cols);
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
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @param rStart int row index at which to start (inclusive).
	 * @param rStop int row index at which to stop (exclusive).
	 * @param cStart int column index at which to start (inclusive).
	 * @param cStop int column index at which to stop (exclusive).
	 * @return Two-dimensional array of integers selectively copied from
	 * <b>matrix</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check submatrix indices.
	 */
	public static int[][] submatrix(int[][] matrix, int rStart,
											int rStop, int cStart, int cStop)
	{
		int[][] out = new int[rStop-rStart][cStop-cStart];
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
	
	/**
	 * brief Extract a subsection of the given <b>matrix</b>.
	 * 
	 * <p>For example, if <b>matrix</b> = <br>1, 2, 3, 4;<br>5, 6, 7, 8;<br>
	 * 9, 10, 11, 12;<br> then <i>submatrix(</i><b>matrix</b><i>, 0, 2, 1,
	 * 4)</i> returns a new matrix<br>2, 3, 4;<br>6, 7, 8;</p>
	 * 
	 * <p>Note that <b>matrix</b> will be unaffected by this method.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
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
	
	/**
	 * \brief TODO
	 * 
	 * @param destination
	 * @param source
	 * @param pivot
	 */
	public static void reorderRowsTo(
			int[][] destination, int[][] source, int[] pivot)
	{
		checkDimensionsSame(destination, source);
		if ( source.length != pivot.length )
		{
			throw new IllegalArgumentException(
					"Pivot must have as many elements ar matrix has rows");
		}
		for ( int i = 0; i < pivot.length; i++ )
			Vector.copyTo(destination[pivot[i]], source[i]);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param destination
	 * @param source
	 * @param pivot
	 */
	public static void reorderRowsTo(
			double[][] destination, double[][] source, int[] pivot)
	{
		checkDimensionsSame(destination, source);
		if ( source.length != pivot.length )
		{
			throw new IllegalArgumentException(
					"Pivot must have as many elements ar matrix has rows");
		}
		for ( int i = 0; i < pivot.length; i++ )
			Vector.copyTo(destination[pivot[i]], source[i]);
	}
	
	/*************************************************************************
	 * MATRIX-VECTOR CONVERSIONS
	 ************************************************************************/
	
	/**
	 * \brief Transpose a column <b>vector</b> to a row vector.
	 * 
	 * <p>Use {@link #toVector(int[][])} to reverse this.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return new {@code int[1][]} array with the same elements as
	 *    <b>vector</b>.
	 * @see #toVector(int[][])
	 * @see #transpose(int[][])
	 * @see #transpose(double[])
	 */
	public static int[][] transpose(int[] vector)
	{
		int[][] out = new int[1][vector.length];
		out[0] = Vector.copy(vector);
		return out;
	}
	
	/**
	 * \brief Transpose a column <b>vector</b> to a row vector.
	 * 
	 * <p>Use {@link #toVector(double[][])} to reverse this.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return new double[1][] array with the same elements as <b>vector</b>.
	 * @see #toVector(double[][])
	 * @see #transpose(double[][])
	 * @see #transpose(int[])
	 */
	public static double[][] transpose(double[] vector)
	{
		double[][] out = new double[1][vector.length];
		out[0] = Vector.copy(vector);
		return out;
	}
	
	/**
	 * \brief Transpose a <b>matrix</b>, i.e. flip it over its diagonal.
	 * 
	 * <p>For example, the matrix <i>(1, 2; 3, 4)</i> transposed is
	 *  <i>(1, 3; 2, 4)</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return new {@code int[][]} array of the given <b>matrix</b> transposed.
	 * @see #transpose(int[])
	 * @see #transpose(double[][])
	 */
	public static int[][] transpose(int[][] matrix)
	{
		checkDimensions(matrix);
		int[][] out = new int[colDim(matrix)][rowDim(matrix)];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	/**
	 * \brief Transpose a <b>matrix</b>, i.e. flip it over its diagonal.
	 * 
	 * <p>For example, the matrix <i>(1.0, 2.0; 3.0, 4.0)</i> transposed is
	 *  <i>(1.0, 3.0; 2.0, 4.0)</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return new {@code double[][]} array of the given <b>matrix</b>
	 * transposed.
	 * @see #transpose(double[])
	 * @see #transpose(int[][])
	 */
	public static double[][] transpose(double[][] matrix)
	{
		checkDimensions(matrix);
		double[][] out = new double[matrix[0].length][matrix.length];
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				out[j][i] = matrix[i][j];
		return out;
	}
	
	/**
	 * \brief Extracts the required row as a column vector.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @param index {@code int} index of the row required.
	 * @return new {@code int[]} array of the required row.
	 */
	public static int[] getRowAsColumn(int[][] matrix, int index)
	{
		return matrix[index];
	}
	
	/**
	 * \brief Extracts the required row as a column vector.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param index {@code int} index of the row required.
	 * @return new {@code double[]} array of the required row.
	 */
	public static double[] getRowAsColumn(double[][] matrix, int index)
	{
		return matrix[index];
	}
	
	/**
	 * \brief Extracts the required column as a vector.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @param index {@code int} index of the column required.
	 * @return new {@code int[]} vector of the required column.
	 */
	public static int[] getColumn(int[][] matrix, int index)
	{
		int nRow = rowDim(matrix);
		int[] out = new int[nRow];
		for ( int i = 0; i < nRow; i++ )
			out[i] = matrix[i][index];
		return out;
	}
	
	/**
	 * \brief Extracts the required column as a vector, overwriting 
	 * <b>destination</b> with the result.
	 * 
	 * @param destination One-dimensional array of {@code double}s
	 *    (overwritten).
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param index {@code int} index of the column required.
	 * @throws IllegalArgumentException Destination must have as many rows as
	 *    matrix.
	 */
	public static void getColumnTo(double[] destination, double[][] matrix,
																	int index)
	{
		if ( destination.length != rowDim(matrix) )
		{
			throw new IllegalArgumentException(
							"Destination must have as many rows as matrix.");
		}
		for ( int i = 0; i < rowDim(matrix); i++ )
			destination[i] = matrix[i][index];
	}
	
	/**
	 * \brief Extracts the required column as a new vector.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param index {@code int} index of the column required.
	 * @return new {@code double[]} array of the required column.
	 */
	public static double[] getColumn(double[][] matrix, int index)
	{
		double[] out = new double[rowDim(matrix)];
		getColumnTo(out, matrix, index);
		return out;
	}
	
	/**
	 * \brief Converts a row vector to a column vector.
	 * 
	 * @param rowVector {@code int[1][]} array of integer values (preserved).
	 * @return New one-dimensional array of {@code int}s.
	 * @exception IllegalArgumentException Matrix must have only one row.
	 */
	public static int[] asVector(int[][] rowVector)
	{
		if ( rowDim(rowVector) != 1 )
		{
			throw new 
				IllegalArgumentException("Matrix must have only one row.");
		}
		return Vector.copy(rowVector[0]);
	}

	/**
	 * \brief Converts a row vector to a column vector.
	 * 
	 * @param rowVector {@code double[1][]} array of real values (preserved).
	 * @return One-dimensional array of {@code double}s.
	 * @exception IllegalArgumentException Matrix must have only one row.
	 */
	public static double[] asVector(double[][] rowVector)
	{
		if ( rowDim(rowVector) != 1 )
		{
			throw new 
				IllegalArgumentException("Matrix must have only one row.");
		}
		return Vector.copy(rowVector[0]);
	}
	
	/**
	 * \brief Converts a column vector to a diagonal matrix.
	 * 
	 * <p>For example, if <b>vector</b> = <br>1<br>2<br>3<br>then
	 * <i>asDiagonal(</i><b>vector</b><i>)</i>) =<br>1, 0, 0;<br>0, 2, 0;<br>
	 * 0, 0, 3;</p> 
	 * 
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @return Two-dimensional array of {@code int}s with the elements of
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
	 * \brief Converts a column vector to a diagonal matrix.
	 * 
	 * <p>For example, if <b>vector</b> = <br>1<br>2<br>3<br>then
	 * <i>asDiagonal(</i><b>vector</b><i>)</i>) =<br>1, 0, 0;<br>0, 2, 0;<br>
	 * 0, 0, 3;</p> 
	 * 
	 * @param vector One-dimensional array of {@code double}s.
	 * @return New two-dimensional array of {@code double}s with the elements
	 * of <b>vector</b> along the main diagonal and zeros elsewhere. 
	 */
	public static double[][] asDiagonal(double[] vector)
	{
		double[][] out = zerosDbl(vector.length);
		for ( int i = 0; i < vector.length; i++ )
			out[i][i] = vector[i];
		return out;
	}
	
	/*************************************************************************
	 * SCALARS FROM MATRICES
	 ************************************************************************/
	
	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * <p>The trace is the sum of all elements on the main diagonal.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} sum of elements in the diagonal.
	 * @see #trace(double[][])
	 * @see #asDiagonal(int[])
	 */
	public static int trace(int[][] matrix)
	{
		int out = 0;
		int min = minDim(matrix);
		for ( int i = 0; i < min; i++ )
	         out += matrix[i][i];
	    return out;
	}

	/**
	 * \brief Trace of the <b>matrix</b> given.
	 * 
	 * <p>The trace is the sum of all elements on the main diagonal.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} sum of elements in the diagonal.
	 * @see #trace(int[][])
	 * @see #asDiagonal(double[])
	 */
	public static double trace(double[][] matrix)
	{
		double out = 0;
		int min = minDim(matrix);
		for ( int i = 0; i < min; i++ )
	         out += matrix[i][i];
	    return out;
	}
	
	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. maximum of the matrix <i>(1, -3; 2, 0)</i> is <i>2</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the greatest element in the <b>matrix</b>.
	 * @see #min(int[][])
	 * @see #maxColumnSum(int[][])
	 * @see #maxRowSum(int[][])
	 * @see #maxDim(int[][])
	 * @see #max(double[][])
	 */
	public static int max(int[][] matrix)
	{
		int out = Integer.MIN_VALUE;
		for ( int[] row : matrix )
			out = Math.max(out, Vector.max(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. maximum of the matrix <i>(1.0, -3.0; 2.0, 0.0)</i> is
	 * <i>2.0</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the greatest element in the 
	 *    <b>matrix</b>.
	 * @see #min(double[][])
	 * @see #maxColumnSum(double[][])
	 * @see #maxRowSum(double[][])
	 * @see #maxDim(double[][])
	 * @see #max(int[][])
	 */
	public static double max(double[][] matrix)
	{
		double out = Double.MIN_VALUE;
		for ( double[] row : matrix )
			out = Math.max(out, Vector.max(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. minimum of the matrix <i>(1, -3; 2, 0)</i> is <i>-3</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the least element in the <b>matrix</b>.
	 * @see #max(int[][])
	 * @see #minColumnSum(int[][])
	 * @see #minRowSum(int[][])
	 * @see #minDim(int[][])
	 * @see #min(double[][])
	 */
	public static int min(int[][] matrix)
	{
		int out = Integer.MAX_VALUE;
		for ( int[] row : matrix )
			out = Math.min(out, Vector.min(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>matrix</b>.
	 * 
	 * <p>E.g. minimum of the matrix <i>(1.0, -3.0; 2.0, 0.0)</i> is
	 * <i>-3.0</i>.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the least element in the <b>matrix</b>.
	 * @see #max(double[][])
	 * @see #minColumnSum(double[][])
	 * @see #minRowSum(double[][])
	 * @see #minDim(double[][])
	 * @see #min(int[][])
	 */
	public static double min(double[][] matrix)
	{
		double out = Double.MAX_VALUE;
		for ( double[] row : matrix )
			out = Math.min(out, Vector.min(row));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>3</i> (left) and of <i>7</i> (right). The maximum 
	 * of these is 7.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the greatest sum of all columns in the
	 *    given <b>matrix</b>.
	 */
	public static int maxColumnSum(int[][] matrix)
	{
		int out = Integer.MAX_VALUE;
		int temp;
		/* Do this the long-hand way to avoid copying columns to vectors. */
		for ( int i = 0; i < rowDim(matrix); i++ )
		{
			temp = 0;
			for ( int j = 0; j < colDim(matrix); j++ )
				temp += matrix[i][j];
			out = Math.max(out, temp);
		}		
		return out;
	}
	
	/**
	 * \brief Greatest sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>3.0</i> (left) and of <i>7.0</i> (right). The maximum 
	 * of these is 7.0.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the greatest sum of all columns in the
	 *    given <b>matrix</b>.
	 */
	public static double maxColumnSum(double[][] matrix)
	{
		double out = Double.MIN_VALUE;
		double temp;
		/* Do this the long-hand way to avoid copying columns to vectors. */
		for ( int i = 0; i < rowDim(matrix); i++ )
		{
			temp = 0.0;
			for ( int j = 0; j < colDim(matrix); j++ )
				temp += matrix[i][j];
			out = Math.max(out, temp);
		}		
		return out;
	}
	
	/**
	 * \brief Least sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>3</i> (left) and of <i>7</i> (right). The minimum 
	 * of these is 3.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the least sum of all columns in the given
	 *    <b>matrix</b>.
	 */
	public static int minColumnSum(int[][] matrix)
	{
		int out = Integer.MAX_VALUE;
		int temp;
		/* Do this the long-hand way to avoid copying columns to vectors. */
		for ( int i = 0; i < rowDim(matrix); i++ )
		{
			temp = 0;
			for ( int j = 0; j < colDim(matrix); j++ )
				temp += matrix[i][j];
			out = Math.min(out, temp);
		}		
		return out;
	}
	
	/**
	 * \brief Least sum of all columns in the given <b>matrix</b>.
	 * 
	 * <p>For example, the columns of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>3.0</i> (left) and of <i>7.0</i> (right). The minimum 
	 * of these is 3.0.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the least sum of all columns in the
	 *    given <b>matrix</b>.
	 */
	public static double minColumnSum(double[][] matrix)
	{
		double out = Double.MAX_VALUE;
		double temp;
		/* Do this the long-hand way to avoid copying columns to vectors. */
		for ( int i = 0; i < rowDim(matrix); i++ )
		{
			temp = 0.0;
			for ( int j = 0; j < colDim(matrix); j++ )
				temp += matrix[i][j];
			out = Math.min(out, temp);
		}		
		return out;
	}
	
	/**
	 * \brief Greatest sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1, 3; 2, 4)</i>
	 * have sums of <i>4</i> (top) and of <i>6</i> (bottom). The maximum 
	 * of these is 6.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the greatest sum of all rows in the given
	 *    <b>matrix</b>.
	 */
	public static int maxRowSum(int[][] matrix)
	{
		int out = Integer.MIN_VALUE;
		for ( int[] row : matrix )
			out = Math.max(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Greatest sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>4.0</i> (top) and of <i>6.0</i> (bottom). The maximum 
	 * of these is 6.0.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the greatest sum of all rows in the
	 *    given <b>matrix</b>.
	 */
	public static double maxRowSum(double[][] matrix)
	{
		double out = Double.MIN_VALUE;
		for ( double[] row : matrix )
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
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code int} value of the least sum of all rows in the given
	 * <b>matrix</b>.
	 */
	public static int minRowSum(int[][] matrix)
	{
		int out = Integer.MAX_VALUE;
		for ( int[] row : matrix )
			out = Math.min(out, Vector.sum(row));
		return out;
	}
	
	/**
	 * \brief Least sum of all rows in the given <b>matrix</b>.
	 * 
	 * <p>For example, the rows of the matrix <i>(1.0, 3.0; 2.0, 4.0)</i>
	 * have sums of <i>4.0</i> (top) and of <i>6.0</i> (bottom). The minimum 
	 * of these is 4.0.</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} value of the least sum of all rows in the given
	 *    <b>matrix</b>.
	 */
	public static double minRowSum(double[][] matrix)
	{
		double out = Double.MAX_VALUE;
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
	 * @param matrix Two-dimensional array of {@code int}s (preserved).
	 * @return {@code double} square root of the sum of all elements squared.
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
		for ( int[] row : matrix )
			for ( int elem : row )
				out = Math.hypot(out, elem);
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
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return {@code double} square root of the sum of all elements squared.
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
	 * \brief Calculates the sum of all elements in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of integers (preserved).
	 * @return int sum of all elements in the matrix.
	 * @see #sum(double[][])
	 */
	public static int sum(int[][] matrix)
	{
		int out = 0;
		for ( int[] row : matrix )
			out += Vector.sum(row);
		return out;
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>matrix</b>.
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return double sum of all elements in the matrix.
	 * @see #sum(int[][])
	 */
	public static double sum(double[][] matrix)
	{
		double out = 0.0;
		for ( double[] row : matrix )
			out += Vector.sum(row);
		return out;
	}
	
	/**
	 * \brief Calculates the arithmetic mean average element in the given 
	 * <b>matrix</b>.
	 * 
	 * <p>Only includes finite elements of <b>matrix</b>. If there are none,
	 * returns {@code Vector.UNDEFINED_AVERAGE}.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return double value of arithmetic mean of elements in <b>matrix</b>.
	 * @see #meanGeo(double[][] matrix)
	 * @see #meanHar(double[][] matrix)
	 */
	public static double meanArith(double[][] matrix)
	{
		double out = 0.0;
		double n = 0.0;
		for ( double[] row : matrix )
			for ( double elem : row )
				if ( Double.isFinite(elem) )
				{
					out += elem;
					n++;
				}
		/*
		 * Check the array contains valid entries before trying to divide by
		 * zero.
		 */
		if ( n == 0.0 )
			return Vector.UNDEFINED_AVERAGE;
		return out/n;
	}
	
	/*************************************************************************
	 * NEW RANDOM MATRICES
	 ************************************************************************/
	
	/**
	 * \brief A new <b>m</b>-by-<b>n</b> {@code int} matrix, where each element
	 * is randomly chosen from a uniform distribution in [min, max).
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param min Lower bound of random numbers (inclusive).
	 * @param max Upper bound of random numbers (exclusive).
	 * @return Two-dimensional array of integers, with elements drawn from a 
	 * uniform distribution between <b>min</b> (inclusive) and <b>max</b>
	 * (exclusive).
	 */
	public static int[][] randomInts(int m, int n, int min, int max)
	{
		int[][] out = new int[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.randomInts(n, min, max);
		return out;
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
	 * @see #randomZeroOne(int)
	 * @see #randomZeroOne(double[][])
	 */
	public static double[][] randomZeroOne(int m, int n)
	{
		double[][] out = new double[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.randomZeroOne(n);
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
	 * @see #randomZeroOne(int, int)
	 * @see #randomZeroOne(double[][])
	 */
	public static double[][] randomZeroOne(int mn)
	{
		return randomZeroOne(mn, mn);
	}
	
	/**
	 * \brief Create a new matrix with random elements.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return Two-dimensional array of random {@code double}s with the same
	 *    number of rows and of columns as <b>matrix</b>.
	 * @see #randomZeroOne(int, int)
	 * @see #randomZeroOne(int)
	 */
	public static double[][] randomZeroOne(double[][] matrix)
	{
		return randomZeroOne(rowDim(matrix), colDim(matrix));
	}
	
	/**
	 * \brief A new <b>m</b>-by-<b>n</b> matrix, where each element is
	 * randomly chosen from a uniform distribution in (-1.0, 1.0).
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @return Two-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus one and plus one 
	 * (exclusive).
	 */
	public static double[][] randomPlusMinus(int m, int n)
	{
		double[][] out = new double[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.randomPlusMinus(n);
		return out;
	}
	
	/**
	 * \brief A new square matrix, where each element is randomly chosen from a
	 * uniform distribution in (-1.0, 1.0).
	 * 
	 * @param mn Number of rows = number of columns.
	 * @return Two-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus one and plus one 
	 * (exclusive).
	 */
	public static double[][] randomPlusMinus(int mn)
	{
		return randomPlusMinus(mn, mn);
	}
	
	/**
	 * \brief A new <b>m</b>-by-<b>n</b> matrix, where each element is
	 * randomly chosen from a uniform distribution in (-<b>scale</b>, 
	 * <b>scale</b>).
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param scale Magnitude of largest number possible in a matrix element.
	 * @return Two-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus <b>scale</b> and 
	 * <b>scale</b> (exclusive).
	 */
	public static double[][] randomPlusMinus(int m, int n, double scale)
	{
		double[][] out = randomPlusMinus(m, n);
		timesEquals(out, scale);
		return out;
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 ************************************************************************/
	
	/**
	 * \brief Recast a {@code double[][]} as an {@code int[][]}.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved). 
	 * @return new {@code int[][]} array where each element is the recast
	 * {@code double} in the corresponding position of <b>matrix</b>.
	 * @see #round(double[][])
	 * @see #floor(double[][])
	 * @see #ceil(double[][])
	 * @see #toDbl(int[][])
	 */
	public static int[][] toInt(double[][] matrix)
	{
		int m = rowDim(matrix);
		int[][] out = new int[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.toInt(matrix[i]);
		return out;
	}
	
	/**
	 * \brief Round a {@code double[][]} as an {@code int[][]}.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return new {@code int[][]} array where each element is the rounded
	 * {@code double} in the corresponding position of <b>matrix</b>.
	 * @see #toInt(double[][])
	 * @see #floor(double[][])
	 * @see #ceil(double[][])
	 * @see #toDbl(int[][])
	 */
	public static int[][] round(double[][] matrix)
	{
		int m = rowDim(matrix);
		int[][] out = new int[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.round(matrix[i]);
		return out;
	}
	
	/**
	 * \brief Floor a {@code double[][]} as an {@code int[][]}.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return new {@code int[][]} array where each element is the floored
	 * {@code double} in the corresponding position of <b>matrix</b>.
	 * @see #toInt(double[][])
	 * @see #round(double[][])
	 * @see #ceil(double[][])
	 * @see #toDbl(int[][])
	 */
	public static int[][] floor(double[][] matrix)
	{
		int m = rowDim(matrix);
		int[][] out = new int[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.floor(matrix[i]);
		return out;
	}
	
	/**
	 * \brief Ceiling a {@code double[][]} as an {@code int[][]}.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @return new {@code int[][]} array where each element is the ceilinged
	 * {@code double} in the corresponding position of <b>matrix</b>.
	 * @see #toInt(double[][])
	 * @see #round(double[][])
	 * @see #floor(double[][])
	 * @see #toDbl(int[][])
	 */
	public static int[][] ceil(double[][] matrix)
	{
		int m = rowDim(matrix);
		int[][] out = new int[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.ceil(matrix[i]);
		return out;
	}
	
	/**
	 * \brief Recast an {@code int[][]} as a {@code double[][]}.
	 * 
	 * @param matrix Two-dimensional array of {@code int}s. 
	 * @return New {@code double[][]} array where each element is the recast
	 * {@code int} in the corresponding position of <b>matrix</b>.
	 * @see #toInt(double[][])
	 * @see #round(double[][])
	 * @see #floor(double[][])
	 * @see #ceil(double[][])
	 */
	public static double[][] toDbl(int[][] matrix)
	{
		int m = rowDim(matrix);
		double[][] out = new double[m][];
		for ( int i = 0; i < m; i++ )
			out[i] = Vector.toDbl(matrix[i]);
		return out;
	}
	
	/*************************************************************************
	 * ADVANCED METHODS
	 ************************************************************************/
	
	/**
	 * \brief Calculate the determinant of the given <b>matrix</b>.
	 * 
	 * <p>Note that <b>matrix</b> must be square.</p>
	 * 
	 * @param matrix Two-dimensional array of doubles (preserved).
	 * @return The {@code double} value of det(<b>matrix</b>).
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
	 * \brief Solve the system of linear equations represented by three
	 * matrices, where <b>a</b>*x = <b>b</b> and x is the matrix to be found.
	 * 
	 * <p>Neither input matrix will be affected by this method and both
	 * matrices must have the same number of rows (i.e. m<sub>a</sub> =
	 * m<sub>b</sub>). The output matrix, x, will be a new
	 * n<sub>a</sub>-by-n<sub>b</sub> matrix.</p>
	 * 
	 * @param a Two-dimensional array of {@code double}s (preserved).
	 * @param b Two-dimensional array of {@code double}s (preserved).
	 *  @return New two-dimensional array of {@code double}s, x, such that
	 *    <b>matrix</b> * x = <b>vector</b>.
	 */
	public static double[][] solve(double[][] a, double[][] b)
	{
		if ( isSquare(a) )
			return (new LUDecomposition(a)).solve(b);
		else
			return (new QRDecomposition(a)).solve(b);
	}
	
	/**
	 * \brief Solve <b>matrix</b> * x = <b>vector</b> and write the 
	 * result, x, into a <b>destination</b>.
	 * 
	 * TODO JAMA code uses QRDecomposition if matrices are non-square.
	 * 
	 * @param destination One-dimensional array of {@code double}s 
	 *    (overwritten).
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 */
	public static void solveTo(double[] destination, double[][] matrix,
															double[] vector)
	{
		(new LUDecomposition(matrix)).solveTo(destination, vector);
	}
	
	/**
	 * \brief Solve <b>matrix</b> * x = <b>vector</b> and write the 
	 * result, x, into a new {@code double[]}.
	 * 
	 * <p>Both inputs must have the same number of rows 
	 * (i.e. m<sub>matrix</sub> = length<sub>vector</sub>). The output vector
	 * will be a new vector of length n<sub>matrix</sub>.</p>
	 * 
	 * TODO JAMA code uses QRDecomposition if matrices are non-square.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @return New one-dimensional array of {@code double}s, x, such that
	 *    <b>matrix</b> * x = <b>vector</b>. 
	 */
	public static double[] solve(double[][] matrix, double[] vector)
	{
		return (new LUDecomposition(matrix)).solve(vector);
	}
	
	/**
	 * \brief Solve <b>matrix</b> * x = <b>vector</b> and write the 
	 * result, x, into the given <b>vector</b>.
	 * 
	 * <p>Both inputs must have the same number of rows 
	 * (i.e. m<sub>matrix</sub> = length<sub>vector</sub>).</p>
	 * 
	 * TODO JAMA code uses QRDecomposition if matrices are non-square.
	 * 
	 * @param matrix Two-dimensional array of {@code double}s (preserved).
	 * @param vector One-dimensional array of {@code double}s (overwritten).
	 * @see #solveTo(double[][], double[][], double[])
	 * @see #solve(double[][], double[])
	 */
	public static void solveEquals(double[][] matrix, double[] vector)
	{
		(new LUDecomposition(matrix)).solveEquals(vector);
	}
}