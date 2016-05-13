package linearAlgebra;

import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * <p>Note on terminology: 
 * <ul><li>Rows are 2D horizontal "slices" through the array, with their
 * "normal vector" pointing down.</li><li>Columns are 2D vertical "slices"
 * through the array, with their "normal vector" pointing right.</li><li>
 * Stacks are 2D vertical "slices" through the array, with their "normal
 * vector" pointing into the screen.</li></ul></p>
 * 
 * <p>The layout of this class is:<p>
 * <ul>
 *   <li>standard new arrays</i>
 *   <li>copying and setting</i>
 *   <li>checking methods (isZero, etc)</li>
 *   <li>basic arithmetic (+, -, *, /)</li>
 *   <li>subsets and reordering of vectors</li>
 *   <li>scalars from arrays (sum, dot product, etc)</i>
 *   <li>new random arrays</li>
 *   <li>converting between integer and double</li>
 *   <li>rescaling arrays</li>
 * </ul>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class Array
{
	/**
	 * Character that separates matrices of an array in {@code String} format.
	 */
	public final static String DELIMITER = "|";
	
	/*************************************************************************
	 * STANDARD NEW ARRAYS
	 ************************************************************************/
	
	/**
	 * \brief Helper method to check input arguments.
	 * 
	 * @param size A 3-vector of positive integers... in theory.
	 * @throws IllegalArgumentException "Size must be three positive numbers"
	 */
	private static void checkSize(int[] size)
	{
		if ( size == null || size.length != 3 || Vector.min(size) < 1 )
		{
			throw new IllegalArgumentException(
								"Size must be three positive numbers");
		}
	}
	
	/**
	 * \brief A new n<sub>i</sub>-by-n<sub>j</sub>-by-n<sub>k</sub> array of
	 * integers.
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @param value Fill the array with this integer value.
	 * @return New 3D array of integers, all of <b>value</b> given.
	 */
	public static int[][][] array(int ni, int nj, int nk, int value)
	{
		int[][][] out = new int[ni][nj][nk];
		return setAll(out, value);
	}
	
	/**
	 * \brief A new cubic array of integers.
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @param value Fill the array with this integer value.
	 * @return New 3D array of integers, all of <b>value</b> given.
	 */
	public static int[][][] array(int nijk, int value)
	{
		return array(nijk, nijk, nijk, value);
	}
	
	/**
	 * \brief A new array of integers, with number of rows, columns, and
	 * stacks, given by <b>size</b>.
	 * 
	 * @param size A 3-vector of positive integers.
	 * @param value Fill the array with this integer value.
	 * @return New 3D array of integers, all of <b>value</b> given.
	 * @throws IllegalArgumentException "Size must be three positive numbers"
	 */
	public static int[][][] array(int[] size, int value)
	{
		checkSize(size);
		return array(size[0], size[1], size[2], value);
	}
	
	/**
	 * \brief A new n<sub>i</sub>-by-n<sub>j</sub>-by-n<sub>k</sub> array of
	 * {@code double}s.
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @param value Fill the array with this double value.
	 * @return New 3D array of {@code double}s, all of <b>value</b> given.
	 */
	public static double[][][] array(int ni, int nj, int nk, double value)
	{
		double[][][] out = new double[ni][nj][nk];
		return setAll(out, value);
	}

	/**
	 * \brief A new cubic array of {@code double}s.
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @param value Fill the array with this {@code double} value.
	 * @return New 3D array of {@code double}s, all of <b>value</b> given.
	 */
	public static double[][][] array(int nijk, double value)
	{
		return array(nijk, nijk, nijk, value);
	}
	
	/**
	 * \brief A new array of {@code double}s, with number of rows, columns, and
	 * stacks, given by <b>size</b>.
	 * 
	 * @param size A 3-vector of positive integers.
	 * @param value Fill the array with this {@code double} value.
	 * @return New 3D array of {@code double}s, all of <b>value</b> given.
	 * @throws IllegalArgumentException "Size must be three positive numbers"
	 */
	public static double[][][] array(int[] size, double value)
	{
		checkSize(size);
		return array(size[0], size[1], size[2], value);
	}
	
	/**
	 * \brief A new n<sub>i</sub>-by-n<sub>j</sub>-by-n<sub>k</sub> array of
	 * integer zeros.
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @return New 3D array of integer zeros.
	 */
	public static int[][][] zerosInt(int ni, int nj, int nk)
	{
		return array(ni, nj, nk, 0);
	}
	
	/**
	 * \brief A new cubic array of integer zeros.
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @return New 3D array of integer zeros.
	 */
	public static int[][][] zerosInt(int nijk)
	{
		return array(nijk, 0);
	}

	/**
	 * \brief A new array of integer zeros, with number of rows, columns, and
	 * stacks, given by <b>size</b>.
	 * 
	 * @param size A 3-vector of positive integers.
	 * @return New 3D array of integer zeros.
	 * @throws IllegalArgumentException "Size must be three positive numbers"
	 */
	public static int[][][] zerosInt(int[] size)
	{
		checkSize(size);
		return zerosInt(size[0], size[1], size[2]);
	}
	
	/**
	 * \brief A new array of integer zeros.
	 * 
	 * @param array Three-dimensional array of integers (preserved).
	 * @return New 3D array of integer zeros, with the same size as the
	 * given <b>array</b>.
	 */
	public static int[][][] zeros(int[][][] array)
	{
		return zerosInt(height(array), width(array), depth(array));
	}
	
	/**
	 * \brief A new n<sub>i</sub>-by-n<sub>j</sub>-by-n<sub>k</sub> array of
	 * {@code double} zeros.
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @return New 3D array of {@code double} zeros.
	 */
	public static double[][][] zerosDbl(int ni, int nj, int nk)
	{
		return array(ni, nj, nk, 0.0);
	}
	
	/**
	 * \brief A new cubic array of {@code double} zeros.
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @return New 3D array of {@code double} zeros.
	 */
	public static double[][][] zerosDbl(int nijk)
	{
		return array(nijk, 0.0);
	}
	
	/**
	 * \brief A new array of {@code double} zeros, with number of rows,
	 * columns, and stacks, given by <b>size</b>.
	 * 
	 * @param size A 3-vector of positive integers.
	 * @return New 3D array of {@code double} zeros.
	 * @throws IllegalArgumentException "Size must be three positive numbers"
	 */
	public static double[][][] zerosDbl(int[] size)
	{
		checkSize(size);
		return zerosDbl(size[0], size[1], size[2]);
	}
	
	/**
	 * \brief A new array of {@code double} zeros.
	 * 
	 * @param array Three-dimensional array of doubles (preserved).
	 * @return New 3D array of {@code double} zeros, with the same size as
	 * the given <b>array</b>.
	 */
	public static double[][][] zeros(double[][][] array)
	{
		return zerosDbl(height(array), width(array), depth(array));
	}

	/*************************************************************************
	 * COPYING AND SETTING
	 ************************************************************************/
	
	/**
	 * \brief Make a deep copy of the given <b>array</b>, placing the result
	 * into a <b>destination</b>.
	 * 
	 * @param destination 3D array of integers (overwritten).
	 * @param array 3D array of integers (preserved).
	 */
	public static void copyTo(int[][][] destination, int[][][] array)
	{
		for ( int i = 0 ; i < array.length; i++ )
			Matrix.copyTo(destination[i], array[i]);
	}
	
	/**
	 * \brief Make a deep copy of the given <b>array</b>.
	 * 
	 * @param array 3D array of integers (preserved).
	 * @return New array which is an exact copy of that given.
	 */
	public static int[][][] copy(int[][][] array)
	{
		int[][][] out = new int[array.length][][];
		for ( int i = 0; i < array.length; i++ )
			out[i] = Matrix.copy(array[i]);
		return out;
	}
	
	/**
	 * \brief Make a deep copy of the given <b>array</b>, placing the result
	 * into a <b>destination</b>.
	 * 
	 * @param destination 3D array of {@code double}s (overwritten).
	 * @param array 3D array of {@code double}s (preserved).
	 */
	public static void copyTo(double[][][] destination, double[][][] array)
	{
		for ( int i = 0 ; i < array.length; i++ )
			Matrix.copyTo(destination[i], array[i]);
	}
	
	/**
	 * \brief Make a deep copy of the given <b>array</b>.
	 * 
	 * @param array 3D array of {@code double}s (preserved).
	 * @return New array which is an exact copy of that given.
	 */
	public static double[][][] copy(double[][][] array)
	{
		double[][][] out = new double[array.length][][];
		for ( int i = 0; i < array.length; i++ )
			out[i] = Matrix.copy(array[i]);
		return out;
	}
	
	/**
	 * \brief Set all elements of the given <b>array</b> to the integer
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>setAll(copy(<b>array</b>), <b>value</b>)</i> or
	 * <i>newInt(height(<b>array</b>), width(<b>array</b>),
	 * depth(<b>array</b>), <b>value</b>)</i> to preserve the original state
	 * of <b>array</b>.</p>
	 * 
	 * @param array 3D array of integers (overwritten).
	 * @param value Fill the <b>array</b> with this integer value.
	 * @return Given <b>array</b> with all elements set to <b>value</b>.
	 */
	public static int[][][] setAll(int[][][] array, int value)
	{
		for ( int[][] matrix : array )
			Matrix.setAll(matrix, value);
		return array;
	}
	
	/**
	 * \brief Set all elements of the given <b>array</b> to the {@code double}
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>setAll(copy(<b>array</b>), <b>value</b>)</i> or
	 * <i>newDbl(height(<b>array</b>), width(<b>array</b>),
	 * depth(<b>array</b>), <b>value</b>)</i> to preserve the original state
	 * of <b>array</b>.</p>
	 * 
	 * @param array 3D array of {@code double}s (overwritten).
	 * @param value Fill the <b>array</b> with this integer value.
	 * @return Given <b>array</b> with all elements set to <b>value</b>.
	 */
	public static double[][][] setAll(double[][][] array, double value)
	{
		for ( double[][] matrix : array )
			Matrix.setAll(matrix, value);
		return array;
	}
	
	/**
	 * \brief Set all elements of the given <b>array</b> to zero.
	 * 
	 * @param array 3D array of integers (overwritten).
	 */
	public static void reset(int[][][] array)
	{
		setAll(array, 0);
	}
	
	/**
	 * \brief Set all elements of the given <b>array</b> to zero.
	 * 
	 * @param array 3D array of {@code double}s (overwritten).
	 */
	public static void reset(double[][][] array)
	{
		setAll(array, 0);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param array 3D array of integers (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>array</b>.
	 */
	public static void restrictMinimum(int[][][] array, int newMinimum)
	{
		for ( int[][] matrix : array )
			Matrix.restrictMinimum(matrix, newMinimum);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param array 3D array of {@code double}s (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>array</b>.
	 */
	public static void restrictMinimum(double[][][] array, double newMinimum)
	{
		for ( double[][] matrix : array )
			Matrix.restrictMinimum(matrix, newMinimum);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value less
	 * than or equal to <b>newMaximum</b>.
	 * 
	 * @param array 3D array of integers (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>array</b>.
	 */
	public static void restrictMaximum(int[][][] array, int newMaximum)
	{
		for ( int[][] matrix : array )
			Matrix.restrictMaximum(matrix, newMaximum);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value less
	 * than or equal to <b>newMaximum</b>.
	 * 
	 * @param array 3D array of {@code double}s (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>array</b>.
	 */
	public static void restrictMaximum(double[][][] array, double newMaximum)
	{
		for ( double[][] matrix : array )
			Matrix.restrictMaximum(matrix, newMaximum);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param array 3D array of integers (overwritten).
	 */
	public static void makeNonnegative(int[][][] array)
	{
		restrictMinimum(array, 0);
	}
	
	/**
	 * \brief Force all elements in this <b>array</b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param array 3D array of {@code double}s (overwritten).
	 */
	public static void makeNonnegative(double[][][] array)
	{
		restrictMinimum(array, 0.0);
	}
	
	/*************************************************************************
	 * CHECKING METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check the consistency of the row widths and column depths in a
	 * given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @exception IllegalArgumentException All rows must have the same width.
	 * @exception IllegalArgumentException All columns must have the same
	 * depth.
	 */
	public static void checkDimensions(int[][][] array)
	{
		for ( int i = 1; i < array.length; i++ )
		{
			if ( array[i].length != array[0].length )
			{
				throw new IllegalArgumentException(
									"All rows must have the same width.");
			}
			for ( int j = 1; j < array[0].length; j++ )
				if ( array[i][j].length != array[0][0].length )
				{
					throw new IllegalArgumentException(
									"All columns must have the same depth.");
				}
		}
	}
	
	/**
	 * \brief Check the consistency of the row widths and column depths in a
	 * given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @exception IllegalArgumentException All rows must have the same width.
	 * @exception IllegalArgumentException All columns must have the same
	 * depth.
	 */
	public static void checkDimensions(double[][][] array)
	{
		for ( int i = 1; i < array.length; i++ )
		{
			if ( array[i].length != array[0].length )
			{
				throw new IllegalArgumentException(
									"All rows must have the same width.");
			}
			for ( int j = 1; j < array[0].length; j++ )
				if ( array[i][j].length != array[0][0].length )
				{
					throw new IllegalArgumentException(
									"All columns must have the same depth.");
				}
		}
	}
	
	/**
	 * \brief Check that the two arrays given have the same dimensions.
	 * 
	 * <p>Note that the arrays will be unaffected by this method.</p>
	 * 
	 * @param a Three-dimensional array of integers.
	 * @param b Three-dimensional array of integers.
	 * @exception IllegalArgumentException Array dimensions must agree.
	 */
	public static void checkDimensionsSame(int[][][] a, int[][][] b)
	{
		if ( height(a) != height(b) || width(a) != width(b)
											|| depth(a) != depth(b))
		{
			throw new 
				IllegalArgumentException("Array dimensions must agree.");
		}
	}
	
	/**
	 * \brief Check that the two arrays given have the same dimensions.
	 * 
	 * <p>Note that the arrays will be unaffected by this method.</p>
	 * 
	 * @param a Three-dimensional array of doubles.
	 * @param b Three-dimensional array of doubles.
	 * @exception IllegalArgumentException Array dimensions must agree.
	 */
	public static void checkDimensionsSame(double[][][] a, double[][][] b)
	{
		if ( height(a) != height(b) || width(a) != width(b)
											|| depth(a) != depth(b))
		{
			throw new 
				IllegalArgumentException("Array dimensions must agree.");
		}
	}
	
	/**
	 * \brief Number of rows in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return int number of rows in the given <b>array</b>.
	 */
	public static int height(int[][][] array)
	{
		return array.length;
	}
	
	/**
	 * \brief Number of rows in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return int number of rows in the given <b>array</b>.
	 */
	public static int height(double[][][] array)
	{
		return array.length;
	}
	
	/**
	 * \brief Number of columns in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return int number of columns in the given <b>array</b>.
	 */
	public static int width(int[][][] array)
	{
		return array[0].length;
	}
	
	/**
	 * \brief Number of columns in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return int number of columns in the given <b>array</b>.
	 */
	public static int width(double[][][] array)
	{
		return array[0].length;
	}
	
	/**
	 * \brief Number of stacks in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return int number of stacks in the given <b>array</b>.
	 */
	public static int depth(int[][][] array)
	{
		return array[0][0].length;
	}
	
	/**
	 * \brief Number of stacks in the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return int number of stacks in the given <b>array</b>.
	 */
	public static int depth(double[][][] array)
	{
		return array[0][0].length;
	}
	
	/**
	 * \brief Reports if the <b>array</b> has as many rows as columns and
	 * stacks.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return boolean reporting whether the <b>matrix</b> is cubic (true) or
	 * not (false).
	 */
	public static boolean isCubic(int[][][] array)
	{
		return (height(array)==width(array)) && (height(array)==depth(array));
	}
	
	/**
	 * \brief Reports if the <b>array</b> has as many rows as columns and
	 * stacks.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return boolean reporting whether the <b>matrix</b> is cubic (true) or
	 * not (false).
	 */
	public static boolean isCubic(double[][][] array)
	{
		return (height(array)==width(array)) && (height(array)==depth(array));
	}
	
	/**
	 * \brief Checks that the given <b>array</b> is cubic, throwing an error
	 * if not.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @exception IllegalArgumentException Array must be cubic.
	 */
	public static void checkCubic(int[][][] array)
	{
		if ( ! isCubic(array) )
			throw new IllegalArgumentException("Array must be cubic.");
	}
	
	/**
	 * \brief Checks that the given <b>array</b> is cubic, throwing an error
	 * if not.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @exception IllegalArgumentException Array must be cubic.
	 */
	public static void checkCubic(double[][][] array)
	{
		if ( ! isCubic(array) )
			throw new IllegalArgumentException("Array must be cubic.");
	}
	
	/**
	 * \brief Returns the size of the largest of the three dimensions (# rows,
	 * # columns, or # stacks) of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return Size of the largest dimension of the given <b>array</b>.
	 */
	public static int maxDim(int[][][] array)
	{
		return Math.max(Math.max(height(array), width(array)), depth(array));
	}

	/**
	 * \brief Returns the size of the largest of the three dimensions (# rows,
	 * # columns, or # stacks) of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return Size of the largest dimension of the given <b>array</b>.
	 */
	public static int maxDim(double[][][] array)
	{
		return Math.max(Math.max(height(array), width(array)), depth(array));
	}
	
	/**
	 * \brief Returns the size of the smallest of the three dimensions (# rows,
	 * # columns, or # stacks) of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return Size of the smallest dimension of the given <b>array</b>.
	 */
	public static int minDim(int[][][] array)
	{
		return Math.min(Math.min(height(array), width(array)), depth(array));
	}
	
	/**
	 * \brief Returns the size of the smallest of the three dimensions (# rows,
	 * # columns, or # stacks) of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return Size of the smallest dimension of the given <b>array</b>.
	 */
	public static int minDim(double[][][] array)
	{
		return Math.min(Math.min(height(array), width(array)), depth(array));
	}
	
	/*************************************************************************
	 * BASIC ARTHIMETIC
	 ************************************************************************/
	
	/* Addition. */
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>add(copy(<b>array</b>), <b>value</b>)</i> to preserve the original
	 * state of <b>array</b>.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @param value Increase every element of the <b>array</b> by this
	 * integer value.
	 * @return Given <b>array</b> with all elements increased by
	 * <b>value</b>.
	 */
	public static int[][][] add(int[][][] array, int value)
	{
		for ( int i = 0; i < height(array); i++ )
			for ( int j = 0; j < width(array); j++ )
				for ( int k = 0; k < depth(array); k++ )
					array[i][j][k] += value;
		return array;
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>add(copy(<b>array</b>), <b>value</b>)</i> to preserve the original
	 * state of <b>array</b>.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @param value Increase every element of the <b>array</b> by this
	 * double value.
	 * @return Given <b>array</b> with all elements increased by
	 * <b>value</b>.
	 */
	public static double[][][] add(double[][][] array, double value)
	{
		for ( int i = 0; i < array.length; i++ )
			for ( int j = 0; j < array[i].length; j++ )
				for ( int k = 0; k < array[i][j].length; k++ )
					array[i][j][k] += value;
		return array;
	}

	/**
	 * \brief Add one array to another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(int[][][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Three-dimensional array of integers.
	 * @param b Three-dimensional array of integers.
	 * @return int[][][] array of <b>a</b>+<b>b</b>.
	 */
	public static int[][][] add(int[][][] a, int[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < height(a); i++ )
			for ( int j = 0; j < width(a); j++ )
				for ( int k = 0; k < depth(a); k++ )
					a[i][j][k] += b[i][j][k];
		return a;
	}
	
	/**
	 * \brief Add one array to another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(double[][][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Three-dimensional array of doubles.
	 * @param b Three-dimensional array of doubles.
	 * @return double[][][] array of <b>a</b>+<b>b</b>.
	 */
	public static double[][][] add(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] += b[i][j][k];
		return a;
	}
	
	/* Subtraction. */
	

	/**
	 * \brief Subtract one array from another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>subtract({@link #copy(int[][][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Three-dimensional array of integers.
	 * @param b Three-dimensional array of integers.
	 * @return int[][][] array of <b>a</b>-<b>b</b>.
	 */
	public static int[][][] subtract(int[][][] a, int[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] -= b[i][j][k];
		return a;
	}
	
	/**
	 * \brief Subtract one array from another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>subtract({@link #copy(double[][][] a)}, <b>b</b>)</i> to preserve
	 * the original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Three-dimensional array of doubles.
	 * @param b Three-dimensional array of doubles.
	 * @return int[][][] array of <b>a</b>-<b>b</b>.
	 */
	public static double[][][] subtract(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] -= b[i][j][k];
		return a;
	}
	
	
	/* Multiplication. */
	
	/**
	 * \brief Multiply all elements in a given <b>array</b> by a given
	 * <b>value</b>.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>times(copy(<b>array</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>array</b>.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @param value Multiply every element of the <b>array</b> by this
	 * integer value.
	 * @return Given <b>array</b> with all elements multiplied by
	 * <b>value</b>.
	 */
	public static int[][][] times(int[][][] array, int value)
	{
		for ( int i = 0; i < height(array); i++ )
			for ( int j = 0; j < width(array); j++ )
				for ( int k = 0; k < depth(array); k++ )
					array[i][j][k] *= value;
		return array;
	}

	/**
	 * \brief Multiply all elements in a given <b>array</b> by a given
	 * <b>value</b>.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>times(copy(<b>array</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>array</b>.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @param value Multiply every element of the <b>array</b> by this
	 * double value.
	 * @return Given <b>array</b> with all elements multiplied by
	 * <b>value</b>.
	 */
	public static double[][][] times(double[][][] array, double value)
	{
		for ( int i = 0; i < array.length; i++ )
			for ( int j = 0; j < array[i].length; j++ )
				for ( int k = 0; k < array[i][j].length; k++ )
					array[i][j][k] *= value;
		return array;
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>elemTimes({@link #copy(int[][][] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a Three-dimensional array of integers.
	 * @param b Three-dimensional array of integers.
	 * @return int[][][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static int[][][] elemTimes(int[][][] a, int[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] *= b[i][j][k];
		return a;
	}
	
	/**
	 * \brief Multiply one array from another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return double[][][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static double[][][] elemTimes(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] *= b[i][j][k];
		return a;
	}
	
	/* Division. */

	/**
	 * \brief Multiply one array by another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * @param a Three-dimensional array of integers (overwritten).
	 * @param b Three-dimensional array of integers (preserved).
	 * @return int[][][] array of <b>a</b> divided by <b>b</b> element-wise.
	 */
	public static int[][][] elemDivide(int[][][] a, int[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] /= b[i][j][k];
		return a;
	}
	
	/**
	 * \brief Multiply one array from another, element-by-element.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return double[][][] array of <b>a</b> divided by <b>b</b>
	 * element-wise.
	 */
	public static double[][][] elemDivide(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					a[i][j][k] /= b[i][j][k];
		return a;
	}
	
	/*************************************************************************
	 * SUBSETS AND REORDERING
	 ************************************************************************/
	
	/**
	 * \brief Extract a subsection of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @param rows int[] array of row indices to include, in the order given.
	 * @param cols int[] array of column indices to include, in the order
	 * given.
	 * @param stks int[] array of stack indices to include, in the order
	 * given.
	 * @return Three-dimensional array of integers selectively copied from
	 * <b>array</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check subarray indices.
	 */
	public static int[][][] subarray(int[][][] array,
										int[] rows, int[] cols, int[] stks)
	{
		int[][][] out = new int[rows.length][cols.length][stks.length];
		try
		{
			for ( int i = 0; i < height(array); i++ )
				for ( int j = 0; j < width(array); j++ )
					for ( int k = 0; k < depth(array); k++ )
						out[i][j][k] = array[rows[i]][cols[j]][stks[k]];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check subarray indices");
		}
		return out;
	}

	/**
	 * \brief Extract a subsection of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @param rows int[] array of row indices to include, in the order given.
	 * @param cols int[] array of column indices to include, in the order
	 * given.
	 * @param stks int[] array of stack indices to include, in the order
	 * given.
	 * @return Three-dimensional array of doubles selectively copied from
	 * <b>array</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check subarray indices.
	 */
	public static double[][][] subarray(double[][][] array,
										int[] rows, int[] cols, int[] stks)
	{
		double[][][] out = new double[rows.length][cols.length][stks.length];
		try
		{
			for ( int i = 0; i < height(array); i++ )
				for ( int j = 0; j < width(array); j++ )
					for ( int k = 0; k < depth(array); k++ )
						out[i][j][k] = array[rows[i]][cols[j]][stks[k]];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check subarray indices");
		}
		return out;
	}
	
	/**
	 * \brief Extract a subsection of the given <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @param iStart int row index at which to start (inclusive).
	 * @param iStop int row index at which to stop (exclusive).
	 * @param jStart int column index at which to start (inclusive).
	 * @param jStop int column index at which to stop (exclusive).
	 * @param kStart int stack index at which to start (inclusive).
	 * @param kStop int stack index at which to stop (exclusive).
	 * @return Three-dimensional array of doubles selectively copied from
	 * <b>array</b>.
	 * @exception  ArrayIndexOutOfBoundsException Check subarray indices.
	 */
	public static double[][][] subarray(double[][][] array, int iStart,
					int iStop, int jStart, int jStop, int kStart, int kStop)
	{
		double[][][] out = new
				double[iStop - iStart][jStop - jStart][kStop - kStart];
		try
		{
			for ( int i = iStart; i < iStop; i++ )
				for ( int j = jStart; j < jStop; j++ )
					for ( int k = kStart; k < kStop; k++ )
						out[i][j][k] = array[i][j][k];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new
					ArrayIndexOutOfBoundsException("Check subarray indices");
		}
		return out;
	}
	
	/*************************************************************************
	 * SCALARS FROM ARRAYS
	 * Any input arrays should be unaffected.
	 ************************************************************************/
	
	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return int value of the greatest element in the <b>array</b>.
	 */
	public static int max(int[][][] array)
	{
		int out = array[0][0][0];
		for ( int[][] row : array )
			out = Math.max(out, Matrix.max(row));
		return out;
	}

	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return double value of the greatest element in the <b>array</b>.
	 */
	public static double max(double[][][] array)
	{
		double out = array[0][0][0];
		for ( double[][] row : array )
			out = Math.max(out, Matrix.max(row));
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return int value of the least element in the <b>array</b>.
	 */
	public static int min(int[][][] array)
	{
		int out = array[0][0][0];
		for ( int[][] row : array )
			out = Math.max(out, Matrix.min(row));
		return out;
	}

	/**
	 * \brief Finds the value of the least element in the given
	 * <b>array</b>.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return double value of the least element in the <b>array</b>.
	 */
	public static double min(double[][][] array)
	{
		double out = array[0][0][0];
		for ( double[][] row : array )
			out = Math.min(out, Matrix.min(row));
		return out;
	}
	
	/**
	 * \brief Norm of a given <b>array</b>.
	 * 
	 * TODO is this a Euclidean norm? Frobenius norm?
	 * 
	 * <p>The original state of <b>array</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param array Three-dimensional array of integers.
	 * @return Square root of the sum of all elements squared.
	 */
	public static double norm(int[][][] array)
	{
		double out = 0.0;
		for ( int[][] row : array )
			for ( int[] colV : row )
				for ( int elem : colV )
					out = Math.hypot(out, elem);
		return out;
	}

	/**
	 * \brief TODO
	 * 
	 * @param array
	 * @return
	 */
	public static double meanArith(double[][][] array)
	{
		double total = 0.0;
		double n = 0.0;
		for ( double[][] row : array )
			for ( double[] colV : row )
				for ( double elem : colV )
				{
					total += elem;
					n++;
				}
		return (n == 0.0) ? 0.0 : total/n;
	}
	
	/**
	 * \brief Norm of a given <b>array</b>.
	 * 
	 * TODO is this a Euclidean norm? Frobenius norm?
	 * 
	 * <p>The original state of <b>array</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return Square root of the sum of all elements squared.
	 */
	public static double norm(double[][][] array)
	{
		double out = 0.0;
		for ( double[][] row : array )
			for ( double[] colV : row )
				for ( double elem : colV )
					out = Math.hypot(out, elem);
		return out;
	}
	
	/**
	 * \brief Find the total of the absolute pairwise differences between
	 * elements of two arrays.
	 * 
	 * <p>Arrays must have same dimensions.</p>
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return Total of the absolute pairwise differences.
	 */
	public static double totalAbsDifference(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			for ( int j = 0; j < a[i].length; j++ )
				for ( int k = 0; k < a[i][j].length; k++ )
					out += Math.abs(a[i][j][k] - b[i][j][k]);
		return out;
	}
	
	/*************************************************************************
	 * NEW RANDOM ARRAYS
	 * Any input should be unaffected.
	 ************************************************************************/
	
	/**
	 * \brief A new ni-by-nj-by-nk array of random doubles.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @return Three-dimensional array of doubles with elements drawn from a 
	 * uniform distribution.
	 */
	public static double[][][] random(int ni, int nj, int nk)
	{
		double[][][] out = new double[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = ExtraMath.getUniRandDbl();
		return out;
	}
	
	/**
	 * \brief Create a new cubic array with random elements.
	 * 
	 * <p>Random numbers are drawn from a uniform distribution over
	 * [0, 1).</p>
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @return Three-dimensional cubic array of doubles with elements drawn
	 * from a  uniform distribution.
	 */
	public static double[][][] random(int nijk)
	{
		return random(nijk, nijk, nijk);
	}
	
	/**
	 * \brief A new ni-by-nj-by-nk array of random doubles.
	 * 
	 * <p>Note that <b>array</b> will be unaffected by this method.</p>
	 * 
	 * @param array Three-dimensional array of doubles.
	 * @return Three-dimensional array of doubles with elements drawn from a 
	 * uniform distribution, and of same size as <b>array</b>.
	 */
	public static double[][][] random(double[][][] array)
	{
		return random(height(array), width(array), depth(array));
	}
	
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 * Recasting should not affect the input array.
	 ************************************************************************/
	
	/**
	 * \brief Recast a double[][][] as an int[][][].
	 * 
	 * <p>Note that any digits after the decimal point are simply discarded.
	 * See {@link #round(double[][][])}, etc for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>array</b> will be unaffected.</p>
	 * 
	 * @param array Three-dimensional array of doubles. 
	 * @return	int[][][] array where each element is the recast double in the
	 * corresponding position of <b>array</b>.
	 */
	public static int[][][] toInt(double[][][] array)
	{
		int ni = height(array);
		int nj = width(array);
		int nk = depth(array);
		int[][][] out = new int[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = (int) array[i][j][k];
		return out;
	}
	
	/**
	 * \brief Round a double[][][] as an int[][][].
	 * 
	 * <p>Note that elements of <b>array</b> are rounded as in
	 * <i>Math.round(double x)</i>. See {@link #toInt(double[][][])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>array</b> will be unaffected.</p>
	 * 
	 * @param array Three-dimensional array of doubles. 
	 * @return	int[][][] array where each element is the rounded double in
	 * the corresponding position of <b>array</b>.
	 */
	public static int[][][] round(double[][][] array)
	{
		int ni = height(array);
		int nj = width(array);
		int nk = depth(array);
		int[][][] out = new int[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = (int) Math.round(array[i][j][k]);
		return out;
	}
	
	/**
	 * \brief Floor a double[][][] as an int[][][].
	 * 
	 * <p>Note that elements of <b>array</b> are floored as in
	 * <i>Math.floor(double x)</i>. See {@link #toInt(double[][][])}, etc
	 * for alternate methods. This method should give identical output to
	 * <i>toInt()</i> when all elements of <b>array</b> are positive.</p>
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>array</b> will be unaffected.</p>
	 * 
	 * @param array Three-dimensional array of doubles. 
	 * @return	int[][][] array where each element is the floored double in
	 * the corresponding position of <b>array</b>.
	 */
	public static int[][][] floor(double[][][] array)
	{
		int ni = height(array);
		int nj = width(array);
		int nk = depth(array);
		int[][][] out = new int[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = (int) Math.floor(array[i][j][k]);
		return out;
	}
	
	/**
	 * \brief Ceiling a double[][][] as an int[][][].
	 * 
	 * <p>Note that elements of <b>array</b> are ceilinged as in
	 * <i>Math.ceil(double x)</i>. See {@link #toInt(double[][][])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>array</b> will be unaffected.</p>
	 * 
	 * @param array Three-dimensional array of doubles. 
	 * @return	int[][][] array where each element is the ceilinged double in
	 * the corresponding position of <b>array</b>.
	 */
	public static int[][][] ceil(double[][][] array)
	{
		int ni = height(array);
		int nj = width(array);
		int nk = depth(array);
		int[][][] out = new int[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = (int) Math.ceil(array[i][j][k]);
		return out;
	}
	
	/**
	 * \brief Recast an int[][][] as a double[][][].
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>array</b> will be unaffected.</p>
	 * 
	 * @param array Three-dimensional array of doubles. 
	 * @return	double[][][] array where each element is the recast int in the
	 * corresponding position of <b>array</b>.
	 */
	public static double[][][] toDbl(int[][][] array)
	{
		int ni = height(array);
		int nj = width(array);
		int nk = depth(array);
		double[][][] out = new double[ni][nj][nk];
		for ( int i = 0; i < ni; i++ )
			for ( int j = 0; j < nj; j++ )
				for ( int k = 0; k < nk; k++ )
					out[i][j][k] = (double) array[i][j][k];
		return out;
	}
	
	/*************************************************************************
	 * RESCALING ARRAYS
	 ************************************************************************/
	
	
	
	
	
	
	
	/**
	 * \brief Gets a new array of integers from a string.
	 * 
	 * @param arrayString String containing a array of integers.
	 * @return int[][][] array of integers from this string.
	 * @see #dblFromString(String)
	 */
	public static int[][][] intFromString(String arrayString)
	{
		String[] matrices = arrayString.split(DELIMITER);
		int[][][] array = new int[matrices.length][][];
		for ( int i = 0; i < matrices.length; i++ )
			array[i] = Matrix.intFromString(matrices[i]);
		return array;
	}
	
	/**
	 * \brief Gets a new matrix of doubles from a string.
	 * 
	 * @param arrayString String containing a matrix of doubles.
	 * @return int[][] matrix of doubles from this string.
	 * @see #intFromString(String)
	 */
	public static double[][][] dblFromString(String arrayString)
	{
		String[] matrices = arrayString.split(DELIMITER);
		double[][][] array = new double[matrices.length][][];
		for ( int i = 0; i < matrices.length; i++ )
			array[i] = Matrix.dblFromString(matrices[i]);
		return array;
	}
	
	/**
	 * \brief Returns integer array in string format.
	 * 
	 * @param array Three-dimensional array of integers (preserved).
	 * @return String representation of this <b>matrix</b>.
	 */
	public static String toString(int[][][] array)
	{
		StringBuffer out = new StringBuffer();
		toString(array, out);
		return out.toString();
	}
	
	/**
	 * \brief Returns double array in string format.
	 * 
	 * @param array Three-dimensional array of doubles (preserved).
	 * @return String representation of this <b>matrix</b>.
	 */
	public static String toString(double[][][] array)
	{
		StringBuffer out = new StringBuffer();
		toString(array, out);
		return out.toString();
	}
	
	/**
	 * \brief Converts the given <b>array</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param array Three-dimensional array of integers (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(int[][][] array, StringBuffer buffer)
	{
		int n = array.length - 1;
		for ( int i = 0; i < n; i++ )
		{
			Matrix.toString(array[i], buffer);
			buffer.append(DELIMITER);
		}
		Matrix.toString(array[n], buffer);
	}
	
	/**
	 * \brief Converts the given <b>array</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param array Three-dimensional array of doubles (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(double[][][] array, StringBuffer buffer)
	{
		int n = array.length - 1;
		for ( int i = 0; i < n; i++ )
		{
			Matrix.toString(array[i], buffer);
			buffer.append(DELIMITER);
		}
		Matrix.toString(array[n], buffer);
	}
}