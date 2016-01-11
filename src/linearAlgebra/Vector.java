package linearAlgebra;

import utility.ExtraMath;

/**
 * \brief Abstract class of useful vector functions.
 * 
 * <p>By convention:<ul><li>a method <i>function(vector)</i> returns a new
 * vector,</li><li>the equivalent <i>functionEquals(vector)</i> overwrites the
 * values in the argument vector,</li><li> and <i>functionTo(destination,
 * source)</i> uses <i>source</i> as the input and writes the result into 
 * <i>destination</i>.</li></ul> If in doubt, the description of the method
 * should make it clear which arguments are preserved and which are
 * overwritten.</p>
 * 
 * <p>Both integer vectors (int[]) and real-number vectors (double[]) are
 * handled in this class. Splitting the class into two has been considered,
 * but this may cause confusion when converting from one to the other, or when
 * trying to use methods that take both as arguments.</p>
 * 
 * <p>The layout of this class is:<p>
 * <ul>
 *   <li>standard new vectors</i>
 *   <li>copying and setting</i>
 *   <li>checking methods (isZero, etc)</li>
 *   <li>basic arithmetic (+, -, *, /)</li>
 *   <li>subsets and reordering of vectors</li>
 *   <li>scalars from vectors (sum, dot product, etc)</i>
 *   <li>new random vectors</li>
 *   <li>converting between integer and double</li>
 *   <li>rescaling vectors</li>
 *   <li>geometry</li>
 * </ul>  
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
*/

public final class Vector
{
	/*************************************************************************
	 * STANDARD NEW VECTORS
	 ************************************************************************/
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	int value to use.
	 * @return	int[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 * @see #vector(int n, double value)
	 */
	public static int[] vector(int n, int value)
	{
		int[] vector = new int[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	double value to use.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 * @see #vector(int n, int value)
	 */
	public static double[] vector(int n, double value)
	{
		double[] vector = new double[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return int[] array of length <b>n</b>, with all elements set to zero.
	 * @see #zeros(int[] vector)
	 * @see #zerosDbl(int n)
	 */
	public static int[] zerosInt(int n)
	{
		return vector(n, 0);
	}
	
	/**
	 * \brief A new integer vector of same length as <b>vector</b>, and all
	 * elements set to zero.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return int[] array of same length as <b>vector</b>, with all elements
	 * set to zero.
	 * @see #zerosInt(int n)
	 * @see #zeros(double[] vector)
	 */
	public static int[] zeros(int[] vector)
	{
		return zerosInt(vector.length);
	}

	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * zero.
	 * @see #zeros(double[] vector)
	 * @see #zerosInt(int n)
	 */
	public static double[] zerosDbl(int n)
	{
		return vector(n, 0.0);
	}
	
	// FIXME nicify
	public static double[] onesDbl(int n)
	{
		return vector(n, 1.0);
	}
	
	/**
	 * \brief A new double vector of same length as <b>vector</b>, and all
	 * elements set to zero.
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[] array of same length as <b>vector</b>, with all
	 * elements set to zero.
	 * @see #zerosDbl(int n)
	 * @see #zeros(int[] vector)
	 */
	public static double[] zeros(double[] vector)
	{
		return zerosDbl(vector.length);
	}
	
	/**
	 * \brief A new integer vector with length <b>n</b> and values
	 * (0, 1, ..., n-1).
	 * 
	 * @param n int length of the output vector.
	 * @return int[] counting from 0 to n-1
	 */
	public static int[] range(int n)
	{
		int[] vector = new int[n];
		for ( int i = 0; i < n; i++ )
			vector[i] = i;
		return vector;
	}
	
	/**
	 * \brief Gets a new vector of integers from a string.
	 * 
	 * @param vectorString String containing a vector of integers.
	 * @return int[] vector of integers from this string.
	 * @see #dblFromString(String vectorString)
	 */
	public static int[] intFromString(String vectorString)
	{
		String[] fields = vectorString.split(",");
		int[] vector = new int[fields.length];
		for (int i = 0; i < fields.length; i++)		
			vector[i] = Integer.valueOf(fields[i]);
		return vector;
	}
	
	/**
	 * \brief Gets a new vector of doubles from a string.
	 * 
	 * @param vectorString String containing a vector of doubles.
	 * @return double[] vector of doubles from this string.
	 * @see intFromString(String vectorString)
	 */
	public static double[] dblFromString(String vectorString)
	{
		String[] fields = vectorString.split(",");
		double[] vector = new double[fields.length];
		for (int i = 0; i < fields.length; i++)		
			vector[i] = Double.valueOf(fields[i]);
		return vector;
	}
	
	/*************************************************************************
	 * COPYING AND SETTING
	 ************************************************************************/
	 
	/**
	 * \brief Copy the values of <b>source</b> into <b>destination</b>.
	 * 
	 * @param destination int[] to be overwritten with the values of
	 * <b>source</b>.
	 * @param source int[] to be copied from (preserved).
	 * @return <b>destination</b>
	 * @see #copy(int[] vector)
	 * @see #copyTo(double[] destination, double[] source)
	 */
	public static int[] copyTo(int[] destination, int[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i];
		return destination;
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new int[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return new int[] that is a copy of <b>vector</b>.
	 * @see #copyTo(int[] destination, int[] source)
	 * @see #copy(double[] vector)
	 */
	public static int[] copy(int[] vector)
	{
		return copyTo(new int[vector.length], vector);
	}
	
	/**
	 * \brief Copy the values of <b>source</b> into <b>destination</b>.
	 * 
	 * @param destination double[] to be overwritten with the values of
	 * <b>source</b>.
	 * @param source double[] to be copied from (preserved).
	 * @return <b>destination</b>
	 * @see #copy(double[] vector)
	 * @see #copyTo(int[] destination, int[] source)
	 */
	public static double[] copyTo(double[] destination, double[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i];
		return destination;
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new double[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return	double[] that is a copy of <b>vector</b>.
	 * @see #copyTo(double[] destination, double[] source)
	 * @see #copy(int[] vector)
	 */
	public static double[] copy(double[] vector)
	{
		return copyTo(new double[vector.length], vector);
	}
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the integer
	 * <b>value</b> given.
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @param value	int value to use.
	 * @return Given <b>vector</b>, with all elements set to <b>value</b>.
	 * @see #reset(int[] vector)
	 * @see #vector(int n, int value)
	 * @see #setAll(double[] vector, double value)
	 */
	public static int[] setAll(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ )
			vector[i] = value;
		return vector;
	}
	
	/**
	 * \brief Reset all elements of the given <b>vector</b> to zero.
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 * @see #setAll(int[] vector, int value)
	 * @see #zerosInt(int n)
	 * @see #reset(double[] vector)
	 */
	public static int[] reset(int[] vector)
	{
		return setAll(vector, 0);
	}
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the double
	 * <b>value</b> given.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @param value	double value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
	 * @see #reset(double[] vector)
	 * @see #vector(int n, double value)
	 * @see #setAll(int[] vector, int value)
	 */
	public static double[] setAll(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ )
			vector[i] = value;
		return vector;
	}
	
	/**
	 * \brief Reset all elements of the given <b>vector</b> to zero.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 * @see #setAll(double[] vector, double value)
	 * @see #zerosDbl(int n)
	 * @see #reset(int[] vector)
	 */
	public static double[] reset(double[] vector)
	{
		return setAll(vector, 0.0);
	}
	
	/*************************************************************************
	 * CHECKING METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 * @see #isNonnegative(int[] vector)
	 * @see #isZero(double[] vector)
	 * @see #isZero(double[] vector, double tolerance)
	 */
	public static boolean isZero(int[] vector)
	{
		for ( int element : vector )
			if ( element != 0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * <p>Note that this method is vulnerable to numerical issues, i.e. values
	 * that are extremely close to zero but not equal because of rounding.
	 * Consider using {@link #isZero(double[] vector, double tolerance)}
	 * instead.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved). 
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 * @see #isZero(double[] vector, double tolerance)
	 * @see #isNonnegative(double[] vector)
	 * @see #isZero(int[] vector)
	 */
	public static boolean isZero(double[] vector)
	{
		for ( double element : vector )
			if ( element != 0.0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * <p>If confident that numerical issues can be ignored, consider using
	 * {@link #isZero(double[] vector)} instead (slightly faster).</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param tolerance double value for the absolute tolerance, i.e.
	 * <i>|x<sub>i</sub>|</i> <= tolerance will be accepted as close enough to
	 * zero (helps avoid numerical issues). 
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 * @see #isZero(double[] vector)
	 * @see #isNonnegative(double[] vector)
	 * @see #isZero(int[] vector)
	 */
	public static boolean isZero(double[] vector, double tolerance)
	{
		for ( double element : vector )
			if ( Math.abs(element) > tolerance )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if all the elements of the given <b>vector</b> are
	 * non-negative, i.e. greater than or equal to zero.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return boolean showing whether all elements or <b>vector</b> are >= 0
	 * (true) or at least one is < 0 (false).
	 * @see #isZero(int[] vector)
	 * @see #isNonnegative(double[] vector)
	 */
	public static boolean isNonnegative(int[] vector)
	{
		for ( int element : vector )
			if ( element < 0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if all elements in a <b>vector</b> are positive or zero.
	 * 
	 * <p>Note that <b>vector</b> is unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return boolean reporting whether the <b>vector</b> contains negative
	 * elements (false) or if all elements are greater than or equal to zero
	 * (true).
	 * @see #isZero(double[] vector)
	 * @see #isZero(double[] vector, double tolerance)
	 * @see #isNonnegative(int[] vector)
	 */
	public static boolean isNonnegative(double[] vector)
	{
		for ( double element : vector )
			if ( element < 0.0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Check that the two given vectors are not null, and have the same
	 * length.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Null vector given.
	 * @exception IllegalArgumentException Vectors must be the same length.
	 * @see #checkLengths(double[] a, double[] b)
	 * @see #checkLengths(double[] a, int[] b)
	 */
	public static void checkLengths(int[] a, int[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	/**
	 * \brief Check that the two given vectors are not null, and have the same
	 * length.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Null vector given.
	 * @exception IllegalArgumentException Vectors must be the same length.
	 * @see #checkLengths(int[] a, int[] b)
	 * @see #checkLengths(double[] a, int[] b)
	 */
	public static void checkLengths(double[] a, double[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	/**
	 * \brief Check that the two given vectors are not null, and have the same
	 * length.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Null vector given.
	 * @exception IllegalArgumentException Vectors must be the same length.
	 * @see #checkLengths(int[] a, int[] b)
	 * @see #checkLengths(double[] a, double[] b)
	 */
	public static void checkLengths(double[] a, int[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	/**
	 * \brief See if the two given vectors have the same elements, in the same
	 * order.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return boolean: true if they are the same, false if at least one
	 * element-element pair differs.
	 * @see #areSame(double[] a, double[] b)
	 */
	public static boolean areSame(int[] a, int[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ )
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	
	/**
	 * \brief See if the two given vectors have the same elements, in the same
	 * order.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return boolean: true if they are the same, false if at least one
	 * element-element pair differs.
	 * @see #areSame(int[] a, int[] b)
	 */
	public static boolean areSame(double a[], double[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ )
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	
	/*************************************************************************
	 * BASIC ARTHIMETIC
	 ************************************************************************/
	
	/* Adding */
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>source</b>,
	 * placing the result in <b>destination</b>.
	 * 
	 * @param destination The int[] to be filled with the result (overwritten).
	 * @param source The int[] from which to take pre-existing values
	 * (preserved). 
	 * @param value int value to add to all elements.
	 * @see #add(int[] vector, int value)
	 * @see #addEquals(int[] vector, int value)
	 * @see #addTo(double[] destination, double[] source, double value)
	 */
	public static void addTo(int[] destination, int[] source, int value)
	{
		checkLengths(destination, source);
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i] + value;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>vector</b>,
	 * returning the result as a new vector. 
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param value int to add to every element of <b>vector</b>.
	 * @return new int[] with elements equal to those in <b>vector</b>, plus
	 * <b>value</b>.
	 * @see #addTo(int[] destination, int[] source, int value)
	 * @see #addEquals(int[] vector, int value)
	 * @see #add(double[] vector, double value)
	 */
	public static int[] add(int[] vector, int value)
	{
		int[] out = new int[vector.length];
		addTo(out, vector, value);
		return out;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>vector</b>,
	 * overwriting the old values of <b>vector</b>.
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @param value	int to add to every element of <b>vector</b>.
	 * @see #addTo(int[] destination, int[] source, int value)
	 * @see #add(int[] vector, int value)
	 * @see #addEquals(double[] vector, double value)
	 */
	public static void addEquals(int[] vector, int value)
	{
		addTo(vector, vector, value);
	}

	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>source</b>,
	 * placing the result in <b>destination</b>.
	 * 
	 * @param destination The double[] to be filled with the result
	 * (overwritten).
	 * @param source The double[] from which to take pre-existing values
	 * (preserved). 
	 * @param value double value to add to all elements.
	 * @see #add(double[] vector, double value)
	 * @see #addEquals(double[] vector, double value)
	 * @see #addTo(double[] destination, double[] source, double value)
	 */
	public static void addTo(double[] destination, double[] source, 
																double value)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i] + value;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>vector</b>,
	 * returning the result as a new vector. 
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param value double to add to every element of <b>vector</b>.
	 * @return new double[] with elements equal to those in <b>vector</b>,
	 * plus <b>value</b>.
	 * @see #addTo(double[] destination, double[] source, double value)
	 * @see #addEquals(double[] vector, double value)
	 * @see #add(int[] vector, int value)
	 */
	public static double[] add(double[] vector, double value)
	{
		double[] out = new double[vector.length];
		addTo(out, vector, value);
		return out;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of a <b>vector</b>,
	 * overwriting the old values of <b>vector</b>.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @param value	double to add to every element of <b>vector</b>.
	 * @see #addTo(double[] destination, double[] source, double value)
	 * @see #add(double[] vector, double value)
	 * @see #addEquals(int[] vector, int value)
	 */
	public static void addEquals(double[] vector, double value)
	{
		addTo(vector, vector, value);
	}
	
	/**
	 * \brief Add two vectors together, writing the result into the given
	 * <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of integers (overwritten).
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #add(int[] a, int[] b)
	 * @see #addEquals(int[] a, int[] b)
	 * @see #addTo(double[] destination, double[] a, double[] b)
	 */
	public static void addTo(int[] destination, int[] a, int[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] + b[i];
	}
	
	/**
	 * \brief Add two vectors together, writing the result into a new vector.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return new int[] array of <b>a</b> + <b>b</b>.
	 * @see #addTo(int[] destination, int[] a, int[] b)
	 * @see #addEquals(int[] a, int[] b)
	 * @see #add(double[] a, double[] b)
	 */
	public static int[] add(int[] a, int[] b)
	{
		int[] out = new int[a.length];
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add two vectors together, writing the result into <b>a</b>.
	 * 
	 * @param a One-dimensional array of integers (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #addTo(int[] destination, int[] a, int[] b)
	 * @see #add(int[] a, int[] b)
	 * @see #addEquals(double[] a, double[] b)
	 */
	public static void addEquals(int[] a, int[] b)
	{
		addTo(a, a, b);
	}
	
	/**
	 * \brief Add two vectors together, writing the result into the given
	 * <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #add(double[] a, double[] b)
	 * @see #addEquals(double[] a, double[] b)
	 * @see #addTo(int[] destination, int[] a, int[] b)
	 */
	public static void addTo(double[] destination, double[] a, double[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] + b[i];
	}
	
	/**
	 * \brief Add two vectors together, writing the result into a new vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return new int[] array of <b>a</b> + <b>b</b>.
	 * @see #addTo(double[] destination, double[] a, double[] b)
	 * @see #addEquals(double[] a, double[] b)
	 * @see #add(int[] a, int[] b)
	 */
	public static double[] add(double[] a, double[] b)
	{
		double[] out = new double[a.length];
		addTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Add two vectors together, writing the result into <b>a</b>.
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #addTo(double[] destination, double[] a, double[] b)
	 * @see #add(double[] a, double[] b)
	 * @see #addEquals(int[] a, int[] b)
	 */
	public static void addEquals(double[] a, double[] b)
	{
		addTo(a, a, b);
	}
	
	/* Subtracting (vectors only) */
	
	/**
	 * \brief Subtract one vector from another, writing the result into the
	 * given <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of integers (overwritten with
	 * <b>a</b> - <b>b</b>).
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #minus(int[] a, int[] b)
	 * @see #minusEquals(int[] a, int[] b)
	 * @see #minusTo(double[] destination, double[] a, double[] b)
	 */
	public static void minusTo(int[] destination, int[] a, int[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] - b[i];
	}
	
	/**
	 * \brief Subtract one vector from another, writing the result into a new
	 * vector.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return new int[] array of <b>a</b> - <b>b</b>.
	 * @see #addTo(int[] destination, int[] a, int[] b)
	 * @see #addEquals(int[] a, int[] b)
	 * @see #add(double[] a, double[] b)
	 */
	public static int[] minus(int[] a, int[] b)
	{
		int[] out = new int[a.length];
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract one vector from another, writing the result into <b>a</b>.
	 * 
	 * @param a One-dimensional array of integers (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #minusTo(int[] destination, int[] a, int[] b)
	 * @see #minus(int[] a, int[] b)
	 * @see #minusEquals(double[] a, double[] b)
	 */
	public static void minusEquals(int[] a, int[] b)
	{
		minusTo(a, a, b);
	}
	
	/**
	 * \brief Subtract one vector from another, writing the result into the
	 * given <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten with
	 * <b>a</b> - <b>b</b>).
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #minus(double[] a, double[] b)
	 * @see #minusEquals(double[] a, double[] b)
	 * @see #minusTo(int[] destination, int[] a, int[] b)
	 */
	public static void minusTo(double[] destination, double[] a, double[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] - b[i];
	}
	
	/**
	 * \brief Subtract one vector from another, writing the result into a new
	 * vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return new double[] array of <b>a</b> - <b>b</b>.
	 * @see #addTo(double[] destination, double[] a, double[] b)
	 * @see #addEquals(double[] a, double[] b)
	 * @see #add(int[] a, int[] b)
	 */
	public static double[] minus(double[] a, double[] b)
	{
		double[] out = new double[a.length];
		minusTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Subtract one vector from another, writing the result into <b>a</b>.
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #minusTo(double[] destination, double[] a, double[] b)
	 * @see #minus(double[] a, double[] b)
	 * @see #minusEquals(int[] a, int[] b)
	 */
	public static void minusEquals(double[] a, double[] b)
	{
		minusTo(a, a, b);
	}
	
	/* Multiplication */
	
	/**
	 * \brief Multiply every element of a <b>source</b> int[] by a scalar
	 * <b>value</b>, placing the result in <b>destination</b>.
	 * 
	 * @param destination The int[] to be filled with the result
	 * (overwritten).
	 * @param source The int[] from which to take pre-existing values
	 * (preserved). 
	 * @param value int value by which to multiply all elements.
	 * @see #times(int[] a, int[] b)
	 * @see #timesEquals(int[] a, int[] b)
	 * @see #timesTo(double[] destination, double[] a, double[] b)
	 */
	public static void timesTo(int[] destination, int[] source, int value)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i] * value;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be preserved and the result written
	 * into a new int[]. <ul><li>Use 
	 * {@link #timesEquals(int[] vector, int value)} to overwrite 
	 * <b>vector</b></li><li> or 
	 * {@link #timesTo(int[] destination, int[] vector, int value)} to write
	 * the result into a given int[].</li></ul></p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param value	int scalar by which to multiply <b>vector</b>.
	 * @return New int[] equal to <b>vector</b> * <b>value</b>.
	 */
	public static int[] times(int[] vector, int value)
	{
		int[] out = new int[vector.length];
		timesTo(out, vector, value);
		return out;
	}
	
	/**
	 * \brief multiply a <b>vector</b> by a scalar <b>value</b>,
	 * overwriting the old values of <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten. <ul><li>Use 
	 * {@link #times(int[] vector, int value)} to write the results into a new
	 * int[]</li><li> or 
	 * {@link #timesTo(int[] destination, int[] vector, int value)} to write
	 * the result into a given int[].</li></ul></p>
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @param value	int scalar by which to multiply <b>vector</b>.
	 * @return	Given <b>vector</b> multiplied by the given <b>value</b>.
	 */
	public static void timesEquals(int[] vector, int value)
	{
		timesTo(vector, vector, value);
	}
	
	/**
	 * \brief Multiply every element of a <b>source</b> double[] by a scalar
	 * <b>value</b>, placing the result in <b>destination</b>.
	 * 
	 * @param destination The double[] to be filled with the result
	 * (overwritten).
	 * @param source The double[] from which to take pre-existing values
	 * (preserved). 
	 * @param value double value to multiply with all elements.
	 * @see #times(double[] vector, double value)
	 * @see #timesEquals(double[] vector, double value)
	 * @see #timesTo(double[] destination, double[] source, double value)
	 */
	public static void timesTo(double[] destination, double[] source, 
																double value)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i] * value;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>, returning the
	 * result as a new vector. 
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param value double to multiply with every element of <b>vector</b>.
	 * @return new double[] with elements equal to those in <b>vector</b>,
	 * times <b>value</b>.
	 * @see #timesTo(double[] destination, double[] source, double value)
	 * @see #timesEquals(double[] vector, double value)
	 * @see #times(int[] vector, int value)
	 */
	public static double[] times(double[] vector, double value)
	{
		double[] out = new double[vector.length];
		timesTo(out, vector, value);
		return out;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>, overwriting
	 * the old values of <b>vector</b>.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @param value	double to multiply with every element of <b>vector</b>.
	 * @see #timesTo(double[] destination, double[] source, double value)
	 * @see #times(double[] vector, double value)
	 * @see #timesEquals(int[] vector, int value)
	 */
	public static void timesEquals(double[] vector, double value)
	{
		timesTo(vector, vector, value);
	}
	
	/**
	 * \brief Sets each element of <b>destination</b> to be the corresponding
	 * element of <b>source</b>, but with opposite sign (+/-).
	 * 
	 * <ul><li>Use {@link #reverseEquals(int[] vector, int value)} to
	 * overwrite <b>vector</b></li><li> or
	 * {@link #reverse(int[] vector, int value)} to write the results into a
	 * new int[].</li></ul>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static int[] reverseTo(int[] destination, int[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = -source[i];
		return destination;
	}
	
	/**
	 * \brief Returns a new int[] with elements corresponding to those in
	 * <b>vector</b>, but with opposite sign (+/-).
	 * 
	 * <p>Note that <b>vector</b> will be preserved and the result written
	 * into a new int[]. <ul><li>Use 
	 * {@link #reverseEquals(int[] vector, int value)} to overwrite 
	 * <b>vector</b></li><li> or 
	 * {@link #reverseTo(int[] destination, int[] vector, int value)} to write
	 * the result into a given int[].</li></ul></p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return new int[], identical to <b>vector</b> except that all elements
	 * have opposite sign (+/-).
	 */
	public static int[] reverse(int[] vector)
	{
		return reverseTo(new int[vector.length], vector);
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten. <ul><li>Use 
	 * {@link #reverse(int[] vector, int value)} to write the results into a
	 * new int[]</li><li> or 
	 * {@link #reverseTo(int[] destination, int[] vector, int value)} to write
	 * the result into a given int[].</li></ul></p>
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static int[] reverseEquals(int[] vector)
	{
		return reverseTo(vector, vector);
	}
	
	/**
	 * \brief Sets each element of <b>destination</b> to be the corresponding
	 * element of <b>source</b>, but with opposite sign (+/-).
	 * 
	 * <ul><li>Use {@link #reverseEquals(double[] vector, double value)} to
	 * overwrite <b>vector</b></li><li> or
	 * {@link #reverse(double[] vector, double value)} to write the results into a
	 * new double[].</li></ul>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static void reverseTo(double[] destination, double[] source)
	{
		timesTo(destination, source, -1.0);
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be preserved, as a new double[] is
	 * created. Use {@link #reverse(double[] vector)} to avoid garbage if
	 * <b>vector</b> may be overwritten.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	new copy of <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static double[] reverse(double[] vector)
	{
		double[] out = new double[vector.length];
		reverseTo(out, vector);
		return out;
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reverse(copy(<b>vector</b>))</i> to preserve the original state of
	 * <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static void reverseEquals(double[] vector)
	{
		reverseTo(vector, vector);
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into the given <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of integers (overwritten).
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #times(int[] a, int[] b)
	 * @see #timesEquals(int[] a, int[] b)
	 * @see #timesTo(double[] destination, double[] a, double[] b)
	 */
	public static void timesTo(int[] destination, int[] a, int[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] * b[i];
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into a new vector.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return new int[] array of <b>a</b> * <b>b</b>.
	 * @see #timesTo(int[] destination, int[] a, int[] b)
	 * @see #timesEquals(int[] a, int[] b)
	 * @see #times(double[] a, double[] b)
	 */
	public static int[] times(int[] a, int[] b)
	{
		int[] out = new int[a.length];
		timesTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into <b>a</b>.
	 * 
	 * @param a One-dimensional array of integers (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @see #timesTo(int[] destination, int[] a, int[] b)
	 * @see #times(int[] a, int[] b)
	 * @see #timesEquals(double[] a, double[] b)
	 */
	public static void timesEquals(int[] a, int[] b)
	{
		timesTo(a, a, b);
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into the given <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #times(double[] a, double[] b)
	 * @see #timesEquals(double[] a, double[] b)
	 * @see #timesTo(int[] destination, int[] a, int[] b)
	 */
	public static void timesTo(double[] destination, double[] a, double[] b)
	{
		checkLengths(a, b);
		checkLengths(a, destination);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] * b[i];
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into a new vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return new int[] array of <b>a</b> + <b>b</b>.
	 * @see #timesTo(double[] destination, double[] a, double[] b)
	 * @see #timesEquals(double[] a, double[] b)
	 * @see #times(int[] a, int[] b)
	 */
	public static double[] times(double[] a, double[] b)
	{
		double[] out = new double[a.length];
		timesTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Multiply two vectors together element-wise, writing the result
	 * into <b>a</b>.
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #timesTo(double[] destination, double[] a, double[] b)
	 * @see #times(double[] a, double[] b)
	 * @see #timesEquals(int[] a, int[] b)
	 */
	public static void timesEquals(double[] a, double[] b)
	{
		timesTo(a, a, b);
	}
	
	/*************************************************************************
	 * SUBSETS AND REORDERING
	 ************************************************************************/
	
	/* Subset */
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return int[] array subset of <b>vector</b>: [start, start+1, ...,
	 * stop-2, stop-1]
	 */
	public static int[] subset(int[] vector, int start, int stop)
	{
		int[] out = new int[stop-start];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[i + start];
		return out;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param indices int[] array of indices to use.
	 * @return integer[] array subset of <b>vector</b>.
	 */
	public static int[] subset(int[] vector, int[] indices)
	{
		int[] out = new int[indices.length];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[indices[i]];
		return out;
	}

	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return double[] array subset of <b>vector</b>: [start, start+1, ...,
	 * stop-2, stop-1]
	 */
	public static double[] subset(double[] vector, int start, int stop)
	{
		double[] out = new double[stop-start];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[i + start];
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param destination
	 * @param vector
	 * @param indices
	 */
	public static void subsetTo(double[] destination, double[] vector,
																int[] indices)
	{
		checkLengths(destination, indices);
		for ( int i = 0; i < indices.length; i++ )
			destination[i] = vector[indices[i]];
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param indices int[] array of indices to use.
	 * @return double[] array subset of <b>vector</b>.
	 */
	public static double[] subset(double[] vector, int[] indices)
	{
		double[] out = new double[indices.length];
		subsetTo(out, vector, indices);
		return out;
	}
	
	/* Flip */
	
	/**
	 * \brief Reverse the order of a given vector <b>source</b>, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param destination One-dimensional array of integers (overwritten).
	 * @param source One-dimensional array of integers (preserved).
	 * @return The int[] <b>destination</b> with the same elements as 
	 * <b>source</b>, but in the opposite order. 
	 */
	public static int[] flipTo(int[] destination, int[] source)
	{
		int i = source.length;
		for ( int element : source )
			destination[--i] = element;
		return destination;
	}
	
	/**
	 * \brief Make a copy of the given <b>vector</b>, except with the order
	 * flipped.
	 * 
	 * <p>For example, (1, 2, 3) would be flipped to (3, 2, 1).</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return int[] array with the same elements as <b>vector</b>, but in
	 * the opposite order. 
	 * @see #reverse(int[] vector)
	 */
	public static int[] flip(int[] vector)
	{
		return flipTo(new int[vector.length], vector);
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>For example, (1, 2, 3) would be flipped to (3, 2, 1).</p>
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @return The same int[] as input, but with the order of elements flipped.
	 */
	public static int[] flipEquals(int[] vector)
	{
		int n = (int) 0.5 * vector.length;
		int temp;
		int j = vector.length;
		for ( int i = 0; i < n; i++ )
		{
			temp = vector[i];
			vector[i] = vector[j--];
			vector[j] = temp;
		}
		return vector;
	}
	
	/**
	 * \brief Reverse the order of a given vector <b>source</b>, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param source One-dimensional array of doubles (preserved).
	 * @return The double[] <b>destination</b> with the same elements as 
	 * <b>source</b>, but in the opposite order. 
	 */
	public static double[] flipTo(double[] destination, double[] source)
	{
		int i = source.length;
		for ( double element : source )
			destination[--i] = element;
		return destination;
	}
	
	/**
	 * \brief Make a copy of the given <b>vector</b>, except with the order
	 * flipped.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * <p>See also {@link #reverse(double[] vector)}.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double[] array with the same elements as <b>vector</b>, but in
	 * the opposite order. 
	 */
	public static double[] flip(double[] vector)
	{
		return flipTo(new double[vector.length], vector);
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @return The same double[] as input, but with the order of elements
	 * flipped.
	 */
	public static double[] flipEquals(double[] vector)
	{
		int n = (int) 0.5 * vector.length;
		double temp;
		int j = vector.length;
		for ( int i = 0; i < n; i++ )
		{
			temp = vector[i];
			vector[i] = vector[j--];
			vector[j] = temp;
		}
		return vector;
	}
	
	/*************************************************************************
	 * SCALARS FROM VECTORS
	 * Any input vectors should be unaffected.
	 ************************************************************************/
	
	/* Maximum/minimum */
	
	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. maximum of the vector <i>(1, -3, 2)</i> is <i>2</i>.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return int value of the greatest element in the <b>vector</b>.
	 * @see #min(int[] vector)
	 * @see #max(double[] vector)
	 */
	public static int max(int[] vector)
	{
		int out = vector[0];
		for ( int i = 1; i < vector.length; i++ )
			out = Math.max(out, vector[i]);
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. minimum of the vector <i>(1, -3, 2)</i> is <i>-3</i>.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return int value of the least element in the <b>vector</b>.
	 * @see #max(int[] vector)
	 * @see #min(double[] vector)
	 */
	public static int min(int[] vector)
	{
		int out = vector[0];
		for ( int i = 1; i < vector.length; i++ )
			out = Math.min(out, vector[i]);
		return out;
	}

	/**
	 * \brief Finds the value of the greatest element in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. maximum of the vector <i>(1, -3, 2)</i> is <i>2</i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of the greatest element in the vector.
	 * @see #min(double[] vector)
	 * @see #max(int[] vector)
	 */
	public static double max(double[] vector)
	{
		double out = vector[0];
		for ( int i = 1; i < vector.length; i++ )
			out = Math.max(out, vector[i]);
		return out;
	}
	
	/**
	 * \brief Finds the value of the least element in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. minimum of the vector <i>(1.0, -3.0, 2.0)</i> is
	 * <i>-3.0</i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of the least element in the vector.
	 * @see #max(double[] vector)
	 * @see #min(int[] vector)
	 */
	public static double min(double[] vector)
	{
		double out = vector[0];
		for ( int i = 1; i < vector.length; i++ )
			out = Math.min(out, vector[i]);
		return out;
	}
	
	/* Norms */
	
	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return double sum of all elements in <b>vector</b>.
	 * @see #normEuclid(int[] vector)
	 * @see #normSquare(double[] vector)
	 */
	public static int normSquare(int[] vector)
	{
		return dotProduct(vector, vector);
	}
	
	/**
	 * \brief Euclidean norm of the given <b>vector</b>.
	 * 
	 * <p>E.g. the normEuclid of the vector <i>(a, b)</i> is
	 * <i>(a<sup>2</sup> + b<sup>2</sup> )<sup>1/2</sup></i>.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return double Euclidean norm of elements in <b>vector</b>.
	 * @see #normSquare(int[] vector)
	 * @see #normEuclid(double[] vector)
	 */
	public static double normEuclid(int[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}

	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double sum of all elements in <b>vector</b>.
	 * @see #normEuclid(double[] vector)
	 * @see #normSquare(int[] vector)
	 */
	public static double normSquare(double[] vector)
	{
		return dotProduct(vector, vector);
	}
	
	/**
	 * \brief Euclidean norm of the given <b>vector</b>.
	 * 
	 * <p>E.g. the normEuclid of the vector <i>(a, b)</i> is
	 * <i>(a<sup>2</sup> + b<sup>2</sup> )<sup>1/2</sup></i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return	double Euclidean norm of elements in <b>vector</b>.
	 * @see #normSquare(double[] vector)
	 * @see #normEuclid(int[] vector)
	 */
	public static double normEuclid(double[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/* Statistics */
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return int sum of all elements in the <b>vector</b>.
	 * @see #sum(double[] vector)
	 */
	public static int sum(int[] vector)
	{
		int sum = 0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double sum of all elements in the vector.
	 * @see #sum(int[] vector)
	 */
	public static double sum(double[] vector)
	{
		double sum = 0.0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the arithmetic mean average element in the given 
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns Double.NaN</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of arithmetic mean of elements in <b>vector</b>.
	 * @see #meanGeo(double[] vector)
	 * @see #meanHar(double[] vector)
	 */
	public static double meanAri(double[] vector)
	{
		double out = 0.0;
		double n = 0.0;
		for ( double elem : vector )
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
			return Double.NaN;
		return out/n;
	}
	
	/**
	 * \brief Calculates the geometric mean average element in the given 
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns zero</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of geometric mean of elements in <b>vector</b>.
	 * @see #meanAri(double[] vector)
	 * @see #meanHar(double[] vector)
	 */
	public static double meanGeo(double[] vector)
	{
		double out = 0.0;
		double n = 0.0;
		for ( double elem : vector )
			if ( Double.isFinite(elem) )
			{
				out *= elem;
				n++;
			}
		/*
		 * Check the array contains valid entries before trying to divide by
		 * zero.
		 */
		if ( n == 0.0 )
			return 0.0;
		return Math.pow(out, 1/n);
	}
	
	/**
	 * \brief Calculates the harmonic mean average element in the given 
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns positive infinity</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of harmonic mean of elements in <b>vector</b>.
	 * @see #meanAri(double[] vector)
	 * @see #meanGeo(double[] vector)
	 */
	public static double meanHar(double[] vector)
	{
		double out = 0.0;
		for ( double elem : vector )
		{
			if ( elem == 0.0 )
				return 0.0;
			if ( Double.isFinite(elem) )
				out += 1/elem;
		}
		/*
		 * Check the array contains valid entries before trying to divide by
		 * zero.
		 */
		if ( out == 0.0 )
			return Double.POSITIVE_INFINITY;
		return 1/out;
	}
	
	/**
	 * \brief Calculates the standard deviation of elements in the given
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns Double.NaN</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param fromSample boolean denoting whether to divide by n-1 (true) or n
	 * (false) 
	 * @return double value of standard deviation of elements in
	 * <b>vector</b>.
	 */
	public static double stdDev(double[] vector, boolean fromSample)
	{
		double mean = meanAri(vector);
		if ( mean == Double.NaN )
			return mean;
		double out = 0.0;
		double n = 0.0;
		for ( double elem : vector )
			if ( Double.isFinite(elem) )
			{
				out = Math.hypot(out, elem - mean);
				n++;
			}
		/*
		 * Check the array contains valid entries before trying to divide by
		 * zero.
		 */
		if ( n == 0.0 )
			return Double.NaN;
		/*
		 * If this is from a sample we divide by (n-1), not n.
		 */
		if ( fromSample && (n > 1.0) )
			n--;
		return out/Math.sqrt(n);
	}
	
	/* Dot functions */
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return int value of the dot product of <b>a</b> and <b>b</b>.
	 * @see #dotProduct(double[] a, double[] b)
	 */
	public static int dotProduct(int[] a, int[] b)
	{
		checkLengths(a, b);
		int out = 0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 * @see #dotQuotient(double[] a, double[] b)
	 * @see #dotProduct(int[] a, int[] b)
	 */
	public static double dotProduct(double[] a, double[] b)
	{
		checkLengths(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
	
	/**
	 * \brief Calculate the dot quotient of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> )./(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>/b<sub>1</sub> +
	 * a<sub>2</sub>/b<sub>2</sub></i></p>
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double value of the dot quotient of <b>a</b> and <b>b</b>.
	 * @see #dotProduct(double[] a, double[] b)
	 */
	public static double dotQuotient(double[] a, double[] b)
	{
		checkLengths(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] / b[i];	
		return out;
	}
	
	/* Distance */
	
	/**
	 * \brief Euclidean distance between two positions, given by vectors.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double Euclidean distance between the two vectors.
	 */
	public static double distanceEuclid(double[] a, double[] b)
	{
		checkLengths(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += ExtraMath.sq(a[i] - b[i]);
		return Math.sqrt(out);
	}
	
	/*************************************************************************
	 * NEW RANDOM VECTORS
	 * Any input should be unaffected.
	 ************************************************************************/
	
	/**
	 * \brief A new double vector of length <b>n</b>, where each element is
	 * randomly chosen from a uniform distribution in [0.0, 1.0).
	 * 
	 * @param n Length of the vector to create.
	 * @return double[] array of length <b>n</b>, with all elements randomly
	 * chosen from a uniform distribution between zero (inclusive) and one
	 * (exclusive).
	 */
	public static double[] randomZeroOne(int n)
	{
		double[] out = new double[n];
		for ( int i = 0; i < n; i++ )
			out[i] = ExtraMath.getUniRandDbl();
		return out;
	}
	
	/**
	 * \brief A new double vector of same length as <b>vector</b>, where each
	 * element is randomly chosen from a uniform distribution in [0.0, 1.0).
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double[] array of same length as <b>vector</b>, with all
	 * elements randomly chosen from a uniform distribution between zero
	 * (inclusive) and one (exclusive).
	 */
	public static double[] randomZeroOne(double[] vector)
	{
		return randomZeroOne(vector.length);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, where each element is
	 * randomly chosen from a uniform distribution in (-1.0, 1.0).
	 * 
	 * @param n Length of the vector to create.
	 * @return double[] array of length <b>n</b>, with all elements randomly
	 * chosen from a uniform distribution between minus one and plus one 
	 * (exclusive).
	 */
	public static double[] randomPlusMinus(int n)
	{
		double[] out = new double[n];
		for ( int i = 0; i < n; i++ )
		{
			out[i] = ExtraMath.getUniRandDbl();
			if ( ExtraMath.getRandBool() )
				out[i] *= -1;
		}
		return out;
	}
	
	/**
	 * \brief A new double vector of same length as <b>vector</b>, where each
	 * element is randomly chosen from a uniform distribution in (-1.0, 1.0).
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double[] array of same length as <b>vector</b>, with all
	 * elements randomly chosen from a uniform distribution between minus one 
	 * and plus one (exclusive).
	 */
	public static double[] randomPlusMinus(double[] vector)
	{
		return randomPlusMinus(vector.length);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, where each element is
	 * randomly chosen from a uniform distribution in (-<b>scale</b>, 
	 * <b>scale</b>).
	 * 
	 * @param n Length of the vector to create.
	 * @param scale Magnitude of largest number possible in a vector element.
	 * @return double[] array of length <b>n</b>, with all elements randomly
	 * chosen from a uniform distribution between minus <b>scale</b> and 
	 * <b>scale</b> (exclusive).
	 */
	public static double[] randomPlusMinus(int n, double scale)
	{
		double[] out =  randomPlusMinus(n);
		timesEquals(out, scale);
		return out; 
	}
	
	/**
	 * \brief A new double vector of same length as <b>vector</b>, where each
	 * element is randomly chosen from a uniform distribution in (-<b>scale</b>, 
	 * <b>scale</b>).
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[] array of same length as <b>vector</b>, with all
	 * elements randomly chosen from a uniform distribution between minus 
	 * <b>scale</b> and <b>scale</b> (exclusive).
	 */
	public static double[] randomPlusMinus(double[] vector, double scale)
	{
		return randomPlusMinus(vector.length, scale);
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 * Recasting should not affect the input vector.
	 ************************************************************************/
	
	/**
	 * \brief Recast a double[] as an int[].
	 * 
	 * <p>Note that any digits after the decimal point are simply discarded.
	 * See {@link #round(double[])}, etc for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the recast double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] toInt(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) vector[i];
		return out;
	}
	
	/**
	 * \brief Round a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are rounded as in
	 * <i>Math.round(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the rounded double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] round(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.round(vector[i]);
		return out;
	}
	
	/**
	 * \brief Floor a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are floored as in
	 * <i>Math.floor(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods. This method should give identical output to
	 * <i>recastToInt()</i> when all elements of <b>vector</b> are 
	 * positive.</p>
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the floored double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] floor(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.floor(vector[i]);
		return out;
	}
	
	/**
	 * \brief Ceiling a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are ceilinged as in
	 * <i>Math.ceil(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the ceilinged double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] ceil(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.ceil(vector[i]);
		return out;
	}
	
	/**
	 * \brief Recast an int[] as a double[].
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	double[] array where each element is the recast int in the
	 * corresponding position of <b>vector</b>.
	 */
	public static double[] toDbl(int[] vector)
	{
		double[] out = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (double) vector[i];
		return out;
	}
	
	
	/*************************************************************************
	 * RESCALING VECTORS
	 ************************************************************************/
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 
	 * <b>newNorm</b>.
	 * 
	 * <p>Note that if the <b>vector</b> is composed of all zeros, this
	 * method will simply exit with the <b>vector</b> unchanged.</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @param newNorm double value for the new Euclidean norm of <b>vector</i>.
	 */
	public static void normaliseEuclid(double[] vector, double newNorm)
	{
		double oldNorm = normEuclid(vector);
		if ( oldNorm != 0.0 )
			timesEquals(vector, newNorm/oldNorm);
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes one.
	 * 
	 * <p>Note that if the <b>vector</b> is composed of all zeros, this
	 * method will simply exit with the <b>vector</b> unchanged.</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 */
	public static void normaliseEuclid(double[] vector)
	{
		normaliseEuclid(vector, 1.0);
	}
	
	/*************************************************************************
	 * GEOMETRY
	 ************************************************************************/
	
	/**
	 * \brief Find the cosine of the angle between two vectors.
	 * 
	 * <p>Returns zero if either of the vectors is all zeros.</p>
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return Cosine of the angle between the two given vectors.
	 * @see #cosAngle(double[] a, double[] b)
	 */
	public static double cosAngle(double[] a, double[] b)
	{
		/*
		 * Returning 0.0 if the dot product is 0.0 removes the danger of
		 * dividing 0.0/0.0 and also speeds things up slightly if the vectors
		 * are orthogonal.
		 */
		double dot = dotProduct(a, b);
		return ( dot == 0.0 ) ? 0.0 : dot/(normEuclid(a) * normEuclid(b));
	}
	
	/**
	 * \brief Find the angle between two vectors.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return The angle between the two given vectors (in radians).
	 * @see #cosAngle(double[] a, double[] b)
	 */
	public static double angle(double[] a, double[] b)
	{
		return Math.acos(cosAngle(a, b));
	}
	
	/**
	 * \brief See if the two given vectors are at right-angles to one another.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return boolean: true if they are orthogonal, false if not.
	 * @see #areParallel(double a[], double[] b)
	 */
	public static boolean areOrthogonal(double a[], double[] b)
	{
		return dotProduct(a, b) == 0.0;
	}
	
	/**
	 * \brief See if the two given vectors are at parallel.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return boolean: true if they are parallel, false if not.
	 * @see #areOrthogonal(double a[], double[] b)
	 */
	public static boolean areParallel(double a[], double[] b)
	{
		return ( Math.abs(cosAngle(a, b)) == 1.0 );
	}
	
	/**
	 * \brief Find the cross-product of two 3-dimensional vectors, <b>a</b>
	 * and <b>b</b>, and write the result into <b>destination</b>.
	 * 
	 * <p>Currently only implemented for 3D vectors.</p>
	 * 
	 * @param destination  One-dimensional array of doubles to be overwritten
	 * with <b>a</b> x <b>b</b>.
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Vectors must be 3D.
	 */
	public static void crossProductTo(double[] destination, double[] a,
																double[] b)
	{
		if ( destination.length != 3 || a.length != 3 || b.length != 3 )
			throw new IllegalArgumentException("Vectors must be 3D.");
		for ( int i = 0; i < 3; i++ )
			destination[i] = a[(i+1)%3]*b[(i+2)%3] - a[(i+2)%3]*b[(i+1)%3];
	}
	
	/**
	 * \brief Find the cross-product of two 3-dimensional vectors, <b>a</b>
	 * and <b>b</b>, and write the result into a new vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return new double[3] with the cross-product.
	 * @exception IllegalArgumentException Vectors must be 3D.
	 */
	public static double[] crossProduct(double[] a, double[] b)
	{
		double[] out = new double[3];
		crossProductTo(out, a, b);
		return out;
	}
	
	// TODO document: handling polar/spherical coordinates
	/**
	 *	x = r cos(theta) sin(phi)
	 *	y = r sin(theta) sin(phi)
	 *	z = r cos(phi)
	 *	r = sqrt(x2+y2+z2)
	 *	theta = atan2(y, x)
	 *	phi = acos(z/r)
	 */
	
	public static double polarRadius(double[] cartesian)
	{
		return normEuclid(cartesian);
	}
	
	public static double[] toPolar(double[] cartesian)
	{
		double[] p = new double[cartesian.length];
		switch(cartesian.length) 
		{
			case 3 : p[2] = Math.acos(cartesian[2]/normEuclid(cartesian));
			case 2 : p[1] = Math.atan2(cartesian[1], cartesian[0]);
			case 1 : p[0] = normEuclid(cartesian);
		}
		return p;
	}
	
	public static double[] toCartesian(double[] polar)
	{
		double[] c = new double[polar.length];
		double phi = 0.0;
		switch(polar.length) 
		{
			case 1 : return polar;
			case 3 : c[2] = polar[0] * Math.cos(polar[2]);
					 phi  = polar[2];
			case 2 : c[0] = polar[0] * Math.cos(polar[1]) * Math.sin(phi);
					 c[1] = polar[0] * Math.sin(polar[1]) * Math.sin(phi);		
		}
		return c;
	}
}
