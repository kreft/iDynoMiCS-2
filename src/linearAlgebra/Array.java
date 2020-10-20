package linearAlgebra;

/**
 * \brief Library of useful array functions.
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
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public final class Array
{
	/**
	 * Character that separates matrices of an array in {@code String} format.
	 */
	// NOTE arrayString.split(DELIMITER) does not work when we use "|" here!
	// This can change, but check it works with testJUnit.LinearAlgebraTest
	public final static String DELIMITER = "%";

	public final static String PRINT_DELIMITER = "% \n";

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
	 * \brief Check that the three arrays given have the same dimensions.
	 * 
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 * @param c Three-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Array dimensions must agree.
	 */
	public static void checkDimensionsSame(
			int[][][] a, int[][][] b, int[][][] c)
	{
		checkDimensionsSame(a, b);
		checkDimensionsSame(a, c);
	}

	/**
	 * \brief Check that the three arrays given have the same dimensions.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @param c Three-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Array dimensions must agree.
	 */
	public static void checkDimensionsSame(
			double[][][] a, double[][][] b, double[][][] c)
	{
		checkDimensionsSame(a, b);
		checkDimensionsSame(a, c);
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
	
	/**
	 * \brief See if the two given arrays have the same elements, in the same
	 * order.
	 * 
	 * @param a Three-dimensional array of {@code int}s (preserved).
	 * @param b Three-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean}: true if they are the same, false if at least
	 * one element-element pair differs.
	 * @see #areSame(double[][][], double[][][])
	 */
	public static boolean areSame(int[][][] a, int[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			if ( ! Matrix.areSame(a[i], b[i]) )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the two given arrays have the same elements, in the same
	 * order.
	 * 
	 * @param a Three-dimensional array of {@code double}s (preserved).
	 * @param b Three-dimensional array of {@code double}s (preserved).
	 * @return {@code boolean}: true if they are the same, false if at least
	 * one element-element pair differs.
	 * @see #areSame(int[][][], int[][][])
	 */
	public static boolean areSame(double[][][] a, double[][][] b)
	{
		checkDimensionsSame(a, b);
		for ( int i = 0; i < a.length; i++ )
			if ( ! Matrix.areSame(a[i], b[i]) )
				return false;
		return true;
	}
	
	/*************************************************************************
	 * BASIC ARTHIMETIC
	 ************************************************************************/
	
	/* Addition. */
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, writing the result into <b>destination</b>.
	 * 
	 * @param destination Three-dimensional array of integers (overwritten).
	 * @param array Three-dimensional array of integers (preserved)
	 * @param value Increase every element by this value (may be negative).
	 */
	public static void addTo(int[][][] destination, int[][][] array, int value)
	{
		checkDimensionsSame(destination, array);
		for ( int i = 0; i < array.length; i++ )
			Matrix.addTo(destination[i], array[i], value);
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, returning the result as a new array.
	 * 
	 * @param array Three-dimensional array of integers (preserved).
	 * @param value Increase every element by this value (may be negative).
	 * @return New array that is a copy of <b>array</b>, but with all elements
	 * increased by <b>value</b>.
	 */
	public static int[][][] add(int[][][] array, int value)
	{
		int[][][] out = zeros(array);
		addTo(out, array, value);
		return out;
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, overwriting it with the result.
	 * 
	 * @param array Three-dimensional array of integers (overwritten).
	 * @param value Increase every element of the <b>array</b> by this
	 * integer value (may be negative).
	 */
	public static void addEquals(int[][][] array, int value)
	{
		addTo(array, array, value);
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, writing the result doubleo <b>destination</b>.
	 * 
	 * @param destination Three-dimensional array of doubles (overwritten).
	 * @param array Three-dimensional array of doubles (preserved)
	 * @param value Increase every element by this value (may be negative).
	 */
	public static void addTo(
			double[][][] destination, double[][][] array, double value)
	{
		checkDimensionsSame(destination, array);
		for ( int i = 0; i < array.length; i++ )
			Matrix.addTo(destination[i], array[i], value);
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, returning the result as a new array.
	 * 
	 * @param array Three-dimensional array of doubles (preserved).
	 * @param value Increase every element by this value (may be negative).
	 * @return New array that is a copy of <b>array</b>, but with all elements
	 * increased by <b>value</b>.
	 */
	public static double[][][] add(double[][][] array, double value)
	{
		double[][][] out = zeros(array);
		addTo(out, array, value);
		return out;
	}
	
	/**
	 * \brief Add a given <b>value</b> to all elements in a given
	 * <b>array</b>, overwriting it with the result.
	 * 
	 * @param array Three-dimensional array of doubles (overwritten).
	 * @param value Increase every element of the <b>array</b> by this
	 * double value (may be negative).
	 */
	public static void addEquals(double[][][] array, double value)
	{
		addTo(array, array, value);
	}
	
	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * into <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> + <b>b</b>.</p>
	 * 
	 * @param destination Three-dimensional array of integers (overwritten)
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void addTo(int[][][] destination, int[][][] a, int[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.addTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * into a new array.
	 * 
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 * @return new int[][][] array of <b>a</b>+<b>b</b>.
	 */
	public static int[][][] add(int[][][] a, int[][][] b)
	{
		int[][][] out = zeros(a);
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * into the first array given (<b>a</b>).
	 * 
	 * @param a Three-dimensional array of integers (overwritten).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void addEquals(int[][][] a, int[][][] b)
	{
		addTo(a, a, b);
	}

	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * doubleo <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> + <b>b</b>.</p>
	 * 
	 * @param destination Three-dimensional array of doubles (overwritten)
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void addTo(
			double[][][] destination, double[][][] a, double[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.addTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * doubleo a new array.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return new double[][][] array of <b>a</b>+<b>b</b>.
	 */
	public static double[][][] add(double[][][] a, double[][][] b)
	{
		double[][][] out = zeros(a);
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add one array to another, element-by-element, writing the result
	 * doubleo the first array given (<b>a</b>).
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void addEquals(double[][][] a, double[][][] b)
	{
		addTo(a, a, b);
	}
	
	/* Subtraction. */
	
	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> - <b>b</b>.</p>
	 * 
	 * @param destination Three-dimensional array of integers (overwritten)
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void minusTo(int[][][] destination, int[][][] a, int[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.minusTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into a new array.
	 * 
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 * @return new int[][][] array of <b>a</b> - <b>b</b>.
	 */
	public static int[][][] minus(int[][][] a, int[][][] b)
	{
		int[][][] out = zeros(a);
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into the first array given (<b>a</b>).
	 * 
	 * @param a Three-dimensional array of integers (overwritten).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void minusEquals(int[][][] a, int[][][] b)
	{
		minusTo(a, a, b);
	}

	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p><b>destination</b> = <b>a</b> - <b>b</b>.</p>
	 * 
	 * @param destination Three-dimensional array of doubles (overwritten)
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void minusTo(
			double[][][] destination, double[][][] a, double[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.minusTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into a new array.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return new double[][][] array of <b>a</b> - <b>b</b>.
	 */
	public static double[][][] minus(double[][][] a, double[][][] b)
	{
		double[][][] out = zeros(a);
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract one array from another, element-by-element, writing the
	 * result into the first array given (<b>a</b>).
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void minusEquals(double[][][] a, double[][][] b)
	{
		minusTo(a, a, b);
	}
	
	/* Multiplication. */
	
	/**
	 * \brief Multiply every element of an <b>array</b> by a scalar
	 * <b>value</b>,, writing the result into <b>destination</b>.
	 * 
	 * <p>Both input arrays must have the same dimensions.</p>
	 * 
	 * @param destination Three-dimensional array of integers to be filled with 
	 * the result (overwritten).
	 * @param array Three-dimensional array of integers from which to take 
	 * pre-existing values (preserved).
	 * @param value integer value to times to all elements.
	 */
	public static void timesTo(
						int[][][] destination, int[][][] array, int value)
	{
		checkDimensionsSame(destination, array);
		for ( int i = 0; i < array.length; i++ )
			Matrix.timesTo(destination[i], array[i], value);
	}
	
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
		int[][][] out = zeros(array);
		timesTo(out, array, value);
		return out;
	}
	
	/**
	 * \brief Multiply every element of an <b>array</b> by a scalar
	 * <b>value</b>, overwriting the old values of <b>array</b>.
	 * 
	 * @param array Three-dimensional array of integers (overwritten).
	 * @param value integer value to times to all elements.
	 */
	public static void timesEquals(int[][][] array, int value)
	{
		timesTo(array, array, value);
	}
	/**
	 * \brief Multiply every element of an <b>array</b> by a scalar
	 * <b>value</b>, writing the result into <b>destination</b>.
	 * 
	 * <p>Both input arrays must have the same dimensions.</p>
	 * 
	 * @param destination Three-dimensional array of doubles to be filled with 
	 * the result (overwritten).
	 * @param array Three-dimensional array of doubles from which to take 
	 * pre-existing values (preserved).
	 * @param value double value to times to all elements.
	 */
	public static void timesTo(
						double[][][] destination, double[][][] array, double value)
	{
		checkDimensionsSame(destination, array);
		for ( int i = 0; i < array.length; i++ )
			Matrix.timesTo(destination[i], array[i], value);
	}
	
	/**
	 * \brief Multiply all elements in a given <b>array</b> by a given
	 * <b>value</b>.
	 * 
	 * <p>Note that <b>array</b> will be overwritten; use
	 * <i>times(copy(<b>array</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>array</b>.</p>
	 * 
	 * @param array Three-dimensional array of doublees.
	 * @param value Multiply every element of the <b>array</b> by this
	 * double value.
	 * @return Given <b>array</b> with all elements multiplied by
	 * <b>value</b>.
	 */
	public static double[][][] times(double[][][] array, double value)
	{
		double[][][] out = zeros(array);
		timesTo(out, array, value);
		return out;
	}
	
	/**
	 * \brief Multiply every element of an <b>array</b> by a scalar
	 * <b>value</b>, overwriting the old values of <b>array</b>.
	 * 
	 * @param array Three-dimensional array of doubles (overwritten).
	 * @param value double value to times to all elements.
	 */
	public static void timesEquals(double[][][] array, double value)
	{
		timesTo(array, array, value);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into <b>destination</b>.
	 * 
	 * @param destination Three-dimensional array of integers (overwritten).
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void elemTimesTo(
			int[][][] destination, int[][][] a, int[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.elemTimesTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element.
	 * 
	 * @param a Three-dimensional array of integers (preserved).
	 * @param b Three-dimensional array of integers (preserved).
	 * @return New int[][][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static int[][][] elemTimes(int[][][] a, int[][][] b)
	{
		int[][][] out = zeros(a);
		elemTimesTo(out, a, b);
		return a;
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into the first array given.
	 * 
	 * @param a Three-dimensional array of integers (overwritten).
	 * @param b Three-dimensional array of integers (preserved).
	 */
	public static void elemTimesEquals(int[][][] a, int[][][] b)
	{
		elemTimesTo(a, a, b);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into <b>destination</b>.
	 * 
	 * @param destination Three-dimensional array of doubles (overwritten).
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void elemTimesTo(
			double[][][] destination, double[][][] a, double[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.elemTimesTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return New double[][][] array of <b>a</b> times <b>b</b> element-wise.
	 */
	public static double[][][] elemTimes(double[][][] a, double[][][] b)
	{
		double[][][] out = zeros(a);
		elemTimesTo(out, a, b);
		return a;
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into the first array given.
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void elemTimesEquals(double[][][] a, double[][][] b)
	{
		elemTimesTo(a, a, b);
	}
	
	/* Division. */
	
	/* (Note that this is ambiguous in integers, and so left for now!) */
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into <b>destination</b>.
	 * 
	 * @param destination Three-dimensional array of doubles (overwritten).
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void elemDivideTo(
			double[][][] destination, double[][][] a, double[][][] b)
	{
		checkDimensionsSame(destination, a, b);
		for ( int i = 0; i < a.length; i++ )
			Matrix.elemDivideTo(destination[i], a[i], b[i]);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (preserved).
	 * @return New double[][][] array of <b>a</b> divide <b>b</b> element-wise.
	 */
	public static double[][][] elemDivide(double[][][] a, double[][][] b)
	{
		double[][][] out = zeros(a);
		elemDivideTo(out, a, b);
		return a;
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into the first array given.
	 * 
	 * @param a Three-dimensional array of doubles (overwritten).
	 * @param b Three-dimensional array of doubles (preserved).
	 */
	public static void elemDivideEqualsA(double[][][] a, double[][][] b)
	{
		elemDivideTo(a, a, b);
	}
	
	/**
	 * \brief Multiply one array by another, element-by-element, writing the
	 * result into the second array given.
	 * 
	 * @param a Three-dimensional array of doubles (preserved).
	 * @param b Three-dimensional array of doubles (overwritten).
	 */
	public static void elemDivideEqualsB(double[][][] a, double[][][] b)
	{
		elemDivideTo(b, a, b);
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
		double[][][] out = new double[iStop+1 - iStart][jStop+1 - jStart][kStop+1 - kStart];
		for ( int i = iStart; i <= iStop; i++ )
			for ( int j = jStart; j <= jStop; j++ )
				for ( int k = kStart; k <= kStop; k++ )
					out[i - iStart][j - jStart][k - kStart] = array[i][j][k];
		return out;
	}
	
	/**
	 * \brief TODO
	 * @param out
	 * @param iStart
	 * @param iStop
	 * @param jStart
	 * @param jStop
	 * @param kStart
	 * @param kStop
	 */
	public static int[][][] subarray(int[][][] array, int iStart, int iStop, 
			int jStart, int jStop, int kStart, int kStop) 
	{
		int[][][] out = new int[iStop+1 - iStart][jStop+1 - jStart][kStop+1 - kStart];
		for ( int i = iStart; i <= iStop; i++ )
			for ( int j = jStart; j <= jStop; j++ )
				for ( int k = kStart; k <= kStop; k++ )
					out[i - iStart][j - jStart][k - kStart] = array[i][j][k];
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
	 * \brief Calculates the arithmetic mean average element in the given 
	 * <b>array</b>.
	 * 
	 * <p>Only includes finite elements of <b>array</b>. If there are none,
	 * returns Double.NaN</p>
	 * 
	 * @param matrix Three-dimensional array of doubles (preserved).
	 * @return double value of arithmetic mean of elements in <b>array</b>.
	 * @see #meanGeo(double[][][])
	 * @see #meanHar(double[][][])
	 */
	public static double meanArith(double[][][] array)
	{
		double total = 0.0;
		double n = 0.0;
		for ( double[][] stack : array )
			for ( double[] row : stack )
				for ( double elem : row )
					if ( Double.isFinite(elem) )
					{
						total += elem;
						n++;
					}
		return (n == 0.0) ? Vector.UNDEFINED_AVERAGE : total/n;
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
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>array</b>.
	 * 
	 * @param array Three-dimensional array of integers (preserved).
	 * @return int sum of all elements in the array.
	 * @see #sum(double[][][])
	 */
	public static int sum(int[][][] array)
	{
		int out = 0;
		for ( int[][] matrix : array )
			out += Matrix.sum(matrix);
		return out;
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>array</b>.
	 * 
	 * @param array Two-dimensional array of doubles (preserved).
	 * @return double sum of all elements in the array.
	 * @see #sum(int[][][])
	 */
	public static double sum(double[][][] array)
	{
		double out = 0.0;
		for ( double[][] matrix : array )
			out += Matrix.sum(matrix);
		return out;
	}
	
	/*************************************************************************
	 * NEW RANDOM ARRAYS
	 * Any input should be unaffected.
	 ************************************************************************/
	
	/**
	 * \brief A new ni-by-nj-by-nk {@code int} array, where each element
	 * is randomly chosen from a uniform distribution in [min, max).
	 * 
	 * @param m Number of rows.
	 * @param n Number of columns.
	 * @param min Lower bound of random numbers (inclusive).
	 * @param max Upper bound of random numbers (exclusive).
	 * @return Three-dimensional array of integers, with elements drawn from a 
	 * uniform distribution between <b>min</b> (inclusive) and <b>max</b>
	 * (exclusive).
	 */
	public static int[][][] randomInts(int ni, int nj, int nk, int min, int max)
	{
		int[][][] out = new int[ni][][];
		for ( int i = 0; i < ni; i++ )
			out[i] = Matrix.randomInts(nj, nk, min, max);
		return out;
	}
	
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
	public static double[][][] randomZeroOne(int ni, int nj, int nk)
	{
		double[][][] out = new double[ni][][];
		for ( int i = 0; i < ni; i++ )
			out[i] = Matrix.randomZeroOne(nj, nk);
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
	public static double[][][] randomZeroOne(int nijk)
	{
		return randomZeroOne(nijk, nijk, nijk);
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
	public static double[][][] randomZeroOne(double[][][] array)
	{
		return randomZeroOne(height(array), width(array), depth(array));
	}
	
	/**
	 * \brief A new ni-by-nj-by-nk array, where each element is
	 * randomly chosen from a uniform distribution in (-1.0, 1.0).
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @return Three-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus one and plus one 
	 * (exclusive).
	 */
	public static double[][][] randomPlusMinus(int ni, int nj, int nk)
	{
		double[][][] out = new double[ni][][];
		for ( int i = 0; i < ni; i++ )
			out[i] = Matrix.randomPlusMinus(nj, nk);
		return out;
	}
	
	/**
	 * \brief A new cubic array, where each element is
	 * randomly chosen from a uniform distribution in (-1.0, 1.0).
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @return Three-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus one and plus one 
	 * (exclusive).
	 */
	public static double[][][] randomPlusMinus(int nijk)
	{
		return randomPlusMinus(nijk, nijk, nijk);
	}
	
	/**
	 * \brief A new ni-by-nj-by-nk array, where each element is randomly chosen
	 * from a uniform distribution in (-b>scale</b>, <b>scale</b>).
	 * 
	 * @param ni Number of rows.
	 * @param nj Number of columns.
	 * @param nk Number of stacks.
	 * @return Three-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus <b>scale</b> and 
	 * <b>scale</b> (exclusive).
	 */
	public static double[][][] randomPlusMinus(
									int ni, int nj, int nk, double scale)
	{
		double[][][] out = randomPlusMinus(ni, nj, nk);
		timesEquals(out, scale);
		return out;
	}
	
	/**
	 * \brief A new cubic array, where each element is randomly chosen from a
	 * uniform distribution in (-b>scale</b>, <b>scale</b>).
	 * 
	 * @param nijk Number of rows = number of columns = number of stacks.
	 * @return Three-dimensional array of doubles, with all elements randomly
	 * chosen from a uniform distribution between minus <b>scale</b> and 
	 * <b>scale</b> (exclusive).
	 */
	public static double[][][] randomPlusMinus(int nijk, double scale)
	{
		return randomPlusMinus(nijk, nijk, nijk, scale);
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
	 * TO/FROM STRING
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
			buffer.append(PRINT_DELIMITER);
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
			buffer.append(PRINT_DELIMITER);
		}
		Matrix.toString(array[n], buffer);
	}
}