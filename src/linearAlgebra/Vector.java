package linearAlgebra;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;
import idynomics.Idynomics;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Library of useful vector functions.
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
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
*/
public final class Vector
{
	/**
	 * Character that separates elements of a vector in {@code String} format.
	 */
	public final static String DELIMITER = ",";
	
	/**
	 * The value that is returned whenever the average value of a vector,
	 * matrix or array cannot be defined.
	 */
	public final static double UNDEFINED_AVERAGE = Double.NaN;

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
	 * @see #vector(int, double)
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
	 * @see #vector(int, int)
	 */
	public static double[] vector(int n, double value)
	{
		double[] vector = new double[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief Create a double vector from an existing Double[] (capital D)
	 * @param object
	 * @return double[] array obtained from Double[] object
	 */
	public static double[] vector(Double[] object)
	{
		double[] vector = new double[object.length];
		for(int i = 0; i < object.length; i++)
			vector[i] = object[i];
		return vector;
	}
	
	/**
	 * \brief Create a double vector from an existing list<Double> (capital D)
	 * @param object
	 * @return double[] array obtained from list<Double> object
	 */
	public static double[] vector(List<Double> object)
	{
		double[] vector = new double[object.size()];
		for(int i = 0; i < object.size(); i++)
			vector[i] = object.get(i);
		return vector;
	}
	
	/**
	 * \brief Create a double vector from an existing Collection<Double> (capital D)
	 * @param object
	 * @return double[] array obtained from list<Double> object
	 */
	public static double[] vector(Collection<Double> object)
	{
		double[] vector = new double[object.size()];
		int i = 0;
		for(Double d : object)
		{
			vector[i] = d;
			i++;
		}
		return vector;
	}
	
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return int[] array of length <b>n</b>, with all elements set to zero.
	 * @see #zeros(int[])
	 * @see #onesInt(int)
	 * @see #zerosDbl(int)
	 */
	public static int[] zerosInt(int n)
	{
		return vector(n, 0);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * zero.
	 * @see #zeros(double[])
	 * @see #onesDbl(int)
	 * @see #zerosInt(int)
	 */
	public static double[] zerosDbl(int n)
	{
		return vector(n, 0.0);
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
	 * @see #zerosInt(int)
	 * @see #zeros(double[])
	 */
	public static int[] zeros(int[] vector)
	{
		return zerosInt(vector.length);
	}
	
	/**
	 * \brief A new double vector of same length as <b>vector</b>, and all
	 * elements set to zero.
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[] array of same length as <b>vector</b>, with all
	 * elements set to zero.
	 * @see #zerosDbl(int)
	 * @see #zeros(int[])
	 */
	public static double[] zeros(double[] vector)
	{
		return zerosDbl(vector.length);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * one.
	 * 
	 * @param n	Length of the vector to create.
	 * @return int[] array of length <b>n</b>, with all elements set to one.
	 * @see #zerosInt(int)
	 * @see #onesDbl(int)
	 */
	public static int[] onesInt(int n)
	{
		return vector(n, 1);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * one.
	 * 
	 * @param n	Length of the vector to create.
	 * @return double[] array of length <b>n</b>, with all elements set to one.
	 * @see #zerosDbl(int)
	 * @see #onesInt(int)
	 */
	public static double[] onesDbl(int n)
	{
		return vector(n, 1.0);
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
	 * @see #dblFromString(String)
	 */
	public static int[] intFromString(String vectorString)
	{
		vectorString = Helper.removeWhitespace(vectorString);
		String[] fields = vectorString.split(DELIMITER);
		int[] vector = new int[fields.length];
		for ( int i = 0; i < fields.length; i++ )		
			vector[i] = Integer.valueOf(fields[i]);
		return vector;
	}
	
	/**
	 * \brief Gets a new vector of doubles from a string.
	 * 
	 * @param vectorString String containing a vector of doubles.
	 * @return double[] vector of doubles from this string.
	 * @see intFromString(String)
	 */
	public static double[] dblFromString(String vectorString)
	{
		vectorString = Helper.removeWhitespace(vectorString);
		String[] fields = vectorString.split(DELIMITER);
		double[] vector = new double[fields.length];
		for ( int i = 0; i < fields.length; i++ )	
		{
			try
			{
				vector[i] = Double.parseDouble(fields[i]);
			}
			catch (NumberFormatException e)
			{
				vector[i] = 
					new Expression( fields[i] ).format( Idynomics.unitSystem );
			}
		}
		return vector;
	}
	
	public static float[] fltFromString(String vectorString)
	{
		vectorString = Helper.removeWhitespace(vectorString);
		String[] fields = vectorString.split(DELIMITER);
		float[] vector = new float[fields.length];
		for ( int i = 0; i < fields.length; i++ )	
		{
			vector[i] = (float) Double.parseDouble(fields[i]);
		}
		return vector;
	}
	
	/**
	 * \brief Returns integer vector in string format.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return String representation of this <b>vector</b>.
	 */
	public static String toString(int[] vector)
	{
		StringBuffer out = new StringBuffer();
		toString(vector, out);
		return out.toString();
	}
	
	/**
	 * \brief Returns double vector in string format.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return String representation of this <b>vector</b>.
	 */
	public static String toString(double[] vector)
	{
		StringBuffer out = new StringBuffer();
		toString(vector, out);
		return out.toString();
	}
	
	/**
	 * \brief Converts the given <b>vector</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(int[] vector, StringBuffer buffer)
	{
		int n = vector.length - 1;
		for ( int i = 0; i < n; i++ )
		{
			buffer.append(vector[i]);
			buffer.append(DELIMITER);
		}
		buffer.append(vector[n]);
	}
	
	/**
	 * \brief Converts the given <b>vector</b> to {@code String}
	 * format, and appends it to the given <b>buffer</b>.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param buffer String buffer (faster than String).
	 */
	public static void toString(double[] vector, StringBuffer buffer)
	{
		int n = vector.length - 1;
		if ( n < 0 )
			return;
		for ( int i = 0; i < n; i++ )
		{
			buffer.append(vector[i]);
			buffer.append(DELIMITER);
		}
		buffer.append(vector[n]);
	}
	
	/*************************************************************************
	 * COPYING AND SETTING
	 ************************************************************************/
	 
	/**
	 * \brief Copy the values of <b>source</b> into <b>destination</b>.
	 * 
	 * NOTE: If source.length > destination.length tailing values are omitted in
	 * destination. If destination.length > source.length tailing values of 
	 * original destination will be maintained. If this behavior is unwanted use
	 * java default destination = source.clone();
	 * 
	 * @param destination int[] to be overwritten with the values of
	 * <b>source</b>.
	 * @param source int[] to be copied from (preserved).
	 * @return <b>destination</b>
	 * @see #copy(int[] vector)
	 * @see #copyTo(double[] destination, double[] source)
	 */
	public static void copyTo(int[] destination, int[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i];
	}
	
	/**
	 * \brief Copy the values of <b>source</b> into <b>destination</b>.
	 * 
	 * NOTE: If source.length > destination.length tailing values are omitted in
	 * destination. If destination.length > source.length tailing values of 
	 * original destination will be maintained. If this behavior is unwanted use
	 * java default destination = source.clone();
	 * 
	 * @param destination double[] to be overwritten with the values of
	 * <b>source</b>.
	 * @param source double[] to be copied from (preserved).
	 * @return <b>destination</b>
	 * @see #copy(double[] vector)
	 * @see #copyTo(int[] destination, int[] source)
	 */
	public static void copyTo(double[] destination, double[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i];
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
		int[] out = new int[vector.length];
		copyTo(out, vector);
		return out;
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
		double[] out = new double[vector.length];
		copyTo(out, vector);
		return out;
	}
	
	public static double[] replace(int field, double value, double[] source)
	{
		double[] out = new double[source.length];
		for ( int i = 0; i < source.length; i++ )
		{
			if ( i == field)
				out[i] = value;
			else		
				out[i] = source[i];
		}
		return out;
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new boolean[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of booleans (preserved).
	 * @return	boolean[] that is a copy of <b>vector</b>.
	 * @see #copyTo(boolean[] destination, boolean[] source)
	 */
	public static boolean[] copy(boolean[] vector)
	{
		boolean[] out = new boolean[vector.length];
		copyTo(out, vector);
		return out;
	}
	
	/**
	 * \brief Copy the values of <b>source</b> into <b>destination</b>.
	 * 
	 * NOTE: If source.length > destination.length tailing values are omitted in
	 * destination. If destination.length > source.length tailing values of 
	 * original destination will be maintained. If this behavior is unwanted use
	 * java default destination = source.clone();
	 * 
	 * @param destination boolean[] to be overwritten with the values of
	 * <b>source</b>.
	 * @param source boolean[] to be copied from (preserved).
	 * @return <b>destination</b>
	 * @see #copy(boolean[] vector)
	 */
	public static void copyTo(boolean[] destination, boolean[] source)
	{
		for ( int i = 0; i < destination.length; i++ )
			destination[i] = source[i];
	}
	
	/**
	 * returns true if all fields in input are equal to value
	 * @param input
	 * @param value
	 * @return
	 */
	public static boolean allOfValue(boolean[] input, boolean value)
	{
		if ( input == null )
			return false;
		for ( boolean b : input)
			if ( b != value)
				return false;
		return true;
	}
	
	/**
	 * returns true if all fields in input are equal to value
	 * @param input
	 * @param value
	 * @return
	 */
	public static boolean allOfValue(double[] input, double value)
	{
		if ( input == null )
			return false;
		for ( double b : input)
			if ( b != value)
				return false;
		return true;
	}
	
	/**
	 * returns true if all fields in input are equal to value
	 * @param input
	 * @param value
	 * @return
	 */
	public static boolean allOfValue(int[] input, int value)
	{
		if ( input == null )
			return false;
		for ( int b : input)
			if ( b != value)
				return false;
		return true;
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
	 * \brief Set all elements of the given <b>vector</b> to the boolean
	 * <b>value</b> given.
	 * 
	 * @param vector One-dimensional array of booleans (overwritten).
	 * @param value	boolean value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
	 */
	public static boolean[] setAll(boolean[] vector, boolean value)
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
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param vector One-dimensional array of {@code int}s (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>vector</b>.
	 * @see #restrictMaximum(int[], int)
	 */
	public static void restrictMinimum(int[] vector, int newMinimum)
	{
		for ( int i = 0; i < vector.length; i++ )
			if ( vector[i] < newMinimum )
				vector[i] = newMinimum;
	}
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value greater
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param vector One-dimensional array of {@code double}s (overwritten).
	 * @param newMinimum New minimum value for all elements in <b>vector</b>.
	 * @see #restrictMaximum(double[], double)
	 * @see #restrictMinimum(int[], int)
	 */
	public static void restrictMinimum(double[] vector, double newMinimum)
	{
		for ( int i = 0; i < vector.length; i++ )
			if ( vector[i] < newMinimum )
				vector[i] = newMinimum;
	}
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value less
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param vector One-dimensional array of {@code int}s (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>vector</b>.
	 * @see #restrictMinimum(int[], int)
	 */
	public static void restrictMaximum(int[] vector, int newMaximum)
	{
		for ( int i = 0; i < vector.length; i++ )
			if ( vector[i] > newMaximum )
				vector[i] = newMaximum;
	}
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value less
	 * than or equal to <b>newMinimum</b>.
	 * 
	 * @param vector One-dimensional array of {@code double}s (overwritten).
	 * @param newMaximum New maximum value for all elements in <b>vector</b>.
	 * @see #restrictMinimum(double[], double)
	 * @see #restrictMaximum(int[], int)
	 */
	public static void restrictMaximum(double[] vector, double newMaximum)
	{
		for ( int i = 0; i < vector.length; i++ )
			if ( vector[i] > newMaximum )
				vector[i] = newMaximum;
	}
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param vector One-dimensional array of {@code int}s (overwritten).
	 * @see #restrictMinimum(int[], int)
	 * @see #restrictMaximum(int[], int)
	 */
	public static void makeNonnegative(int[] vector)
	{
		restrictMinimum(vector, 0);
	}
	
	/**
	 * \brief Force all elements in this <b>vector</b> to take a value greater
	 * than or equal to zero.
	 * 
	 * @param vector One-dimensional array of {@code double}s (overwritten).
	 * @see #restrictMinimum(double[], double)
	 * @see #restrictMaximum(double[], double)
	 * @see #makeNonnegative(int[])
	 */
	public static void makeNonnegative(double[] vector)
	{
		restrictMinimum(vector, 0.0);
	}
	
	/*************************************************************************
	 * CHECKING METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean} showing whether <b>vector</b> is all zeros
	 * (true) or contains a non-zero (false).
	 * @see #isNonnegative(int[])
	 * @see #isZero(double[])
	 * @see #isZero(double[], double)
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
	 * @param vector One-dimensional array of {@code double}s (preserved). 
	 * @return {@code boolean} showing whether <b>vector</b> is all zeros
	 * (true) or contains a non-zero (false).
	 * @see #isZero(double[], double)
	 * @see #isNonnegative(double[])
	 * @see #isZero(int[])
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
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @param tolerance {@code double} value for the absolute tolerance, i.e.
	 * <i>|x<sub>i</sub>|</i> <= tolerance will be accepted as close enough to
	 * zero (helps avoid numerical issues). 
	 * @return {@code boolean} showing whether <b>vector</b> is all zeros
	 * (true) or contains a non-zero (false).
	 * @see #isZero(double[])
	 * @see #isNonnegative(double[])
	 * @see #isZero(int[])
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
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @return {@code boolean} showing whether all elements or <b>vector</b>
	 * are >= 0 (true) or at least one is < 0 (false).
	 * @see #isZero(int[])
	 * @see #isNonnegative(double[])
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
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @return {@code boolean} reporting whether the <b>vector</b> contains
	 * negative elements (false) or if all elements are greater than or equal
	 * to zero (true).
	 * @see #isZero(double[])
	 * @see #isZero(double[], double)
	 * @see #isNonnegative(int[])
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
	 * @see #checkLengths(int[], int[], int[])
	 * @see #checkLengths(double[], double[])
	 * @see #checkLengths(double[], int[])
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
	 * @see #checkLengths(int[], int[])
	 * @see #checkLengths(double[], int[])
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
	 * @see #checkLengths(int[], int[])
	 * @see #checkLengths(double[], double[])
	 */
	public static void checkLengths(double[] a, int[] b)
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
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @param c One-dimensional array of integers (preserved).
	 * @exception IllegalArgumentException Null vector given.
	 * @exception IllegalArgumentException Vectors must be the same length.
	 * @see #checkLengths(int[], int[])
	 * @see #checkLengths(double[], double[], double[])
	 */
	public static void checkLengths(int[] a, int[] b, int[] c)
	{
		checkLengths(a, b);
		checkLengths(b, c);
	}
	
	/**
	 * \brief Check that the two given vectors are not null, and have the same
	 * length.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @param c One-dimensional array of doubles (preserved).
	 * @exception IllegalArgumentException Null vector given.
	 * @exception IllegalArgumentException Vectors must be the same length.
	 * @see #checkLengths(double[], double[])
	 * @see #checkLengths(int[], int[], int[])
	 */
	public static void checkLengths(double[] a, double[] b, double[] c)
	{
		checkLengths(a, b);
		checkLengths(b, c);
	}
	
	/**
	 * \brief See if the two given vectors have the same elements, in the same
	 * order.
	 * 
	 * @param a One-dimensional array of integers (preserved).
	 * @param b One-dimensional array of integers (preserved).
	 * @return boolean: true if they are the same, false if at least one
	 * element-element pair differs.
	 * @see #areSame(double[], double[])
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
	 * @see #areSame(int[], int[])
	 */
	public static boolean areSame(double a[], double[] b)
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
	 * @see #areSame(int[], int[])
	 */
	public static boolean areSame(double a[], double[] b, double absTol)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ )
			if ( ! ExtraMath.areEqual(a[i], b[i], absTol) )
				return false;
		return true;
	}
	
	/**
	 * \brief Check if the given <b>set</b> of vectors contains the
	 * <b>vector</b> given.
	 * 
	 * <p>Note that this method is necessary because testing equivalence of 
	 * vectors with {@code ==} is unreliable.</p>
	 * 
	 * @param set Unordered collection of integer vectors (preserved).
	 * @param vector One-dimensional array of integers (preserved).
	 * @return {@code true} if the set contains vector, {@code false} if it
	 * does not.
	 */
	public static boolean contains(Set<int[]> set, int[] vector)
	{
		for ( int[] v : set )
			if ( areSame(v, vector) )
				return true;
		return false;
	}
	
	/**
	 * \brief See if the given {@code HashMap} contains the given {@code int[]}
	 * vector among its keys.
	 * 
	 * @param hm
	 * @param vector
	 * @return
	 */
	public static boolean containsKey(HashMap<int[], ?> hm, int[] vector)
	{
		return contains(hm.keySet(), vector);
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
		checkLengths(destination, a, b);
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
		for ( int i = 0; i < a.length; i++ ) 
		{
			destination[i] = a[i] + b[i];
		}
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
		checkLengths(destination, a, b);
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
	 * TODO
	 * @param a
	 * @param value
	 * @return
	 */
	public static double[] minus(double[] a, double value) {
		double[] out = new double[a.length];
		for ( int i = 0; i < a.length; i++ ) 
			out[i] = a[i] - value;
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
	 * @param destination The vector to be filled with the result (overwritten).
	 * @param vector One-dimensional array of doubles (preserved).
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
		checkLengths(destination, a, b);
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
		checkLengths(destination, a, b);
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
	
	/* Division */
	
	/**
	 * \brief For each element of a vector <b>a</b>, divide by the
	 * corresponding element of vector <b>b</b>, and write the result into
	 * the <b>destination</b> vector.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 */
	public static void divideTo(double[] destination, double[] a, double[] b)
	{
		checkLengths(destination, a, b);
		for ( int i = 0; i < a.length; i++ ) 
			destination[i] = a[i] / b[i];
	}
	
	/**
	 * \brief divide destination vector elements by divisor.
	 * @param destination
	 * @param divisor
	 */
	public static double[] divideEqualsA(double[] destination, double divisor)
	{
		for ( int i = 0; i < destination.length; i++ ) 
			destination[i] = destination[i] / divisor;
		return destination;
	}
	
	/**
	 * \brief For each element of a vector <b>a</b>, divide by the
	 * corresponding element of vector <b>b</b>, and write the result into
	 * a new vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return New one-dimensional array of doubles.
	 */
	public static double[] divide(double[] a, double[] b)
	{
		double[] out = new double[a.length];
		divideTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief For each element of a vector <b>a</b>, divide by the
	 * corresponding element of vector <b>b</b>, overwriting the element of
	 * <b>a</b> with the result.
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 */
	public static void divideEqualsA(double[]a, double[]b)
	{
		divideTo(a, a, b);
	}
	
	/**
	 * \brief For each element of a vector <b>a</b>, divide by the
	 * corresponding element of vector <b>b</b>, overwriting the element of
	 * <b>b</b> with the result.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (overwritten).
	 */
	public static void divideEqualsB(double[]a, double[]b)
	{
		divideTo(b, a, b);
	}
	
	/*************************************************************************
	 * SUBSETS AND REORDERING
	 ************************************************************************/
	
	/* Subset */
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of {@code int}s (overwritten).
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @param indices {@code int[]} array of indices to use.
	 */
	public static void subsetTo(int[] destination, int[] vector, int[] indices)
	{
		checkLengths(destination, indices);
		for ( int i = 0; i < indices.length; i++ )
			destination[i] = vector[indices[i]];
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param indices int[] array of indices to use.
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
	 * @param vector One-dimensional array of integers (preserved).
	 * @param indices int[] array of indices to use.
	 * @return integer[] array subset of <b>vector</b>.
	 */
	public static int[] subset(int[] vector, int[] indices)
	{
		int[] out = new int[indices.length];
		subsetTo(out, vector, indices);
		return out;
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
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of {@code int}s (overwritten).
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 */
	public static void subsetTo(int[] destination, int[] vector,
														int start, int stop)
	{
		for ( int i = start; i < stop; i++ )
			destination[i-start] = vector[i];
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of {@code double}s (overwritten).
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 */
	public static void subsetTo(double[] destination, double[] vector,
														int start, int stop)
	{
		for ( int i = start; i < stop; i++ )
			destination[i-start] = vector[i];
	}
	
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
		subsetTo(out, vector, start, stop);
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
		subsetTo(out, vector, start, stop);
		return out;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of {@code int}s (overwritten).
	 * @param vector One-dimensional array of {@code int}s (preserved).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 */
	public static void subsetTo(int[] destination, int[] vector, int stop)
	{
		subsetTo(destination, vector, 0, stop);
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and write the result
	 * into <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of {@code double}s (overwritten).
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * 
	 */
	public static void subsetTo(double[] destination, double[] vector, int stop)
	{
		subsetTo(destination, vector, 0, stop);
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return New double array of <b>vector</b>[0, ..., stop-1].
	 * @see #subsetTo(int[], int[], int)
	 * @see #subset(int[], int[])
	 * @see #subset(int[], int, int)
	 * @see #subset(double[], int)
	 */
	public static int[] subset(int[] vector, int stop)
	{
		return subset(vector, 0, stop);
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return New double array of <b>vector</b>[0, ..., stop-1].
	 * @see #subsetTo(double[], double[], int)
	 * @see #subset(double[], double[])
	 * @see #subset(double[], double, double)
	 * @see #subset(int[], double)
	 */
	public static double[] subset(double[] vector, int stop)
	{
		return subset(vector, 0, stop);
	}
	
	/**
	 * \brief Append a value to the end of a vector, writing the result into a
	 * new vector.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param value New number to append to the end of this vector.
	 * @return New one-dimensional array of integers.
	 */
	public static int[] append(int[] vector, int value)
	{
		int[] out = new int[vector.length+1];
		for (int i = 0; i < vector.length; i++)
			out[i] = vector[i];
		out[vector.length] = value;
		return out;
	}
	
	/**
	 * \brief Append a value to the end of a vector, writing the result into a
	 * new vector.
	 * 
	 * For appending entire vectors @See {@link #appendAll(double[], double[]) 
	 * appendAll}
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param value New number to append to the end of this vector.
	 * @return New one-dimensional array of doubles.
	 */
	public static double[] append(double[] vector, double value)
	{
		double[] out = new double[vector.length+1];
		for (int i = 0; i < vector.length; i++)
			out[i] = vector[i];
		out[vector.length] = value;
		return out;
	}
	
	/**
	 * \brief Append a second vector to the end of a vector (returning a new 
	 * double[] object including both.
	 * 
	 * For single value appending @See {@link #append(double[], double) append}
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param second One-dimensional array of doubles to append (preserved).
	 * @return New one-dimensional array of doubles.
	 */
	public static double[] appendAll(double[] vector, double[] second)
	{
		double[] out = new double[vector.length+second.length];
		for (int i = 0; i < vector.length; i++)
			out[i] = vector[i];
		for (int i = 0; i < second.length; i++)
			out[i+vector.length] = second[i];
		return out;
	}
	
	/* Flip */
	
	/**
	 * \brief Reverse the order of a given vector <b>source</b>, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p>For example, (1, 2, 3) would be flipped to (3, 2, 1).</p>
	 * 
	 * @param destination One-dimensional array of integers (overwritten).
	 * @param source One-dimensional array of integers (preserved).
	 * @see #flip(int[])
	 * @see #flipEquals(int[])
	 * @see #flipTo(double[])
	 * @see #reverseTo(int[] vector)
	 */
	public static void flipTo(int[] destination, int[] source)
	{
		checkLengths(destination, source);
		int i = source.length;
		for ( int element : source )
			destination[--i] = element;
	}
	
	/**
	 * \brief Make a copy of the given <b>vector</b>, except with the order
	 * flipped.
	 * 
	 * <p>For example, (1, 2, 3) would be flipped to (3, 2, 1).</p>
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @return New int[] array with the same elements as <b>vector</b>, but in
	 * the opposite order. 
	 * @see #flipTo(int[], int[])
	 * @see #flipEquals(int[])
	 * @see #flip(double[])
	 * @see #reverse(int[] vector)
	 */
	public static int[] flip(int[] vector)
	{
		int[] out = new int[vector.length];
		flipTo(out, vector);
		return out;
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>For example, (1, 2, 3) would be flipped to (3, 2, 1).</p>
	 * 
	 * @param vector One-dimensional array of integers (overwritten).
	 * @see #flipTo(int[], int[])
	 * @see #flip(int[])
	 * @see #flipEquals(double[])
	 * @see #reverseEquals(int[])
	 */
	public static void flipEquals(int[] vector)
	{
		int j = vector.length;
		int n = (int) (0.5 * j);
		int temp;
		for ( int i = 0; i < n; i++ )
		{
			temp = vector[i];
			vector[i] = vector[--j];
			vector[j] = temp;
		}
	}
	
	/**
	 * \brief Reverse the order of a given vector <b>source</b>, writing the
	 * result into <b>destination</b>.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param destination One-dimensional array of {@code double}s
	 * (overwritten).
	 * @param source One-dimensional array of {@code double}s (preserved).
	 * @see #flip(double[])
	 * @see #flipEquals(double[])
	 * @see #flipTo(int[], int[])
	 * @see #reverseTo(double[], double[])
	 */
	public static void flipTo(double[] destination, double[] source)
	{
		int i = source.length;
		for ( double element : source )
			destination[--i] = element;
	}
	
	/**
	 * \brief Make a copy of the given <b>vector</b>, except with the order
	 * flipped.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param vector One-dimensional array of {@code double}s (preserved).
	 * @return New {@code double[]} array with the same elements as
	 * <b>vector</b>, but in the opposite order. 
	 * @see #flipTo(double[], double[])
	 * @see #flipEquals(double[])
	 * @see #flip(int[])
	 * @see #reverse(double[])
	 */
	public static double[] flip(double[] vector)
	{
		double[] out = new double[vector.length];
		flipTo(out, vector);
		return out;
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>For example, (1.0, 2.0, 3.0) would be flipped to (3.0, 2.0, 1.0).</p>
	 * 
	 * @param vector One-dimensional array of {@code double}s (overwritten).
	 * @see #flipTo(double[], double[])
	 * @see #flip(double[])
	 * @see #flipEquals(int[])
	 * @see #reverseEquals(double[])
	 */
	public static void flipEquals(double[] vector)
	{
		int j = vector.length;
		int n = (int) (0.5 * j);
		double temp;
		for ( int i = 0; i < n; i++ )
		{
			temp = vector[i];
			vector[i] = vector[--j];
			vector[j] = temp;
		}
	}
	
	/*************************************************************************
	 * SCALARS FROM VECTORS
	 * Any input vectors should be unaffected.
	 ************************************************************************/
	
	/**
	 * \brief Count the number of times a given <b>value</b> appears in a
	 * <b>vector</b>.
	 * 
	 * @param vector One-dimensional array of integers (preserved).
	 * @param value Integer value to search for.
	 * @return Number of times this value appears in the vector. This will be
	 * zero if it does not appear.
	 */
	public static int countInstances(int[] vector, int value)
	{
		int out = 0;
		for ( int elem : vector )
			if ( elem == value )
				out++;
		return out;
	}
	
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
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b> and writes the result to out.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @Param out re-usable double
	 * @return double sum of all elements in <b>vector</b>.
	 * @see #normEuclid(double[] vector)
	 * @see #normSquare(int[] vector)
	 */
	public static double normSquareTo(double out, double[] vector)
	{
		return dotProductTo(out, vector, vector);
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
	
	public static double normEuclidTo(double out, double[] vector)
	{
		out = Math.sqrt(normSquareTo(out, vector));
		return out;
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
		for ( int element : vector )
			sum += element;
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
		for ( double element : vector )
			sum += element;
		return sum;
	}
	
	/**
	 * \brief Calculates the absolute sum of all elements in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the absolute sum of the vector <i>(a, b)</i> is
	 * <i>|a| + |b|</i>. Note that this may be different from the absolute of
	 * the sum, i.e. <i>|a + b|</i>.</p>
	 * 
	 * @param vector One-dimensional array of integer (preserved).
	 * @return integer absolute sum of all elements in the vector.
	 * @see #sum(int[] vector)
	 * @see #sumAbs(double[] vector)
	 */
	public static int sumAbs(int[] vector)
	{
		int sum = 0;
		for ( int element : vector )
			sum += Math.abs(element);
		return sum;
	}
	
	/**
	 * \brief Calculates the absolute sum of all elements in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the absolute sum of the vector <i>(a, b)</i> is
	 * <i>|a| + |b|</i>. Note that this may be different from the absolute of
	 * the sum, i.e. <i>|a + b|</i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double absolute sum of all elements in the vector.
	 * @see #sum(double[] vector)
	 * @see #sumAbs(int[] vector)
	 */
	public static double sumAbs(double[] vector)
	{
		double sum = 0.0;
		for ( double element : vector )
			sum += Math.abs(element);
		return sum;
	}
	
	/**
	 * \brief Calculates the arithmetic mean average element in the given 
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns {@link #UNDEFINED_AVERAGE}.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of arithmetic mean of elements in <b>vector</b>.
	 * @see #meanGeo(double[])
	 * @see #meanHar(double[])
	 */
	public static double meanArith(double[] vector)
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
	 * returns {@link #UNDEFINED_AVERAGE}.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of geometric mean of elements in <b>vector</b>.
	 * @see #meanArith(double[])
	 * @see #meanHar(double[])
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
			return UNDEFINED_AVERAGE;
		return Math.pow(out, 1/n);
	}
	
	/**
	 * \brief Calculates the harmonic mean average element in the given 
	 * <b>vector</b>.
	 * 
	 * <p>Only includes finite elements of <b>vector</b>. If there are none,
	 * returns {@link #UNDEFINED_AVERAGE}.</p>
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @return double value of harmonic mean of elements in <b>vector</b>.
	 * @see #meanArith(double[])
	 * @see #meanGeo(double[])
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
			return UNDEFINED_AVERAGE;
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
		double mean = meanArith(vector);
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
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given and writes the
	 * result to out.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * @Param out re-usable double
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 * @see #dotQuotient(double[] a, double[] b)
	 * @see #dotProduct(int[] a, int[] b)
	 */
	public static double dotProductTo(double out, double[] a, double[] b)
	{
		out = 0.0;
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
	 * \brief A new {@code int} vector of length <b>n</b>, where each element
	 * is randomly and independently chosen from a uniform distribution in
	 * [min, max).
	 * 
	 * @param n Length of the vector to create.
	 * @param min Lower bound of random numbers (inclusive).
	 * @param max Upper bound of random numbers (exclusive).
	 * @return {@code int[]} array of length <b>n</b>, with all elements
	 * randomly chosen from a uniform distribution between <b>min</b>
	 * (inclusive) and <b>max</b> (exclusive).
	 */
	public static int[] randomInts(int n, int min, int max)
	{
		int[] out = new int[n];
		for ( int i = 0; i < n; i++ )
			out[i] = ExtraMath.getUniRandInt(min, max);
		return out;
	}
	
	/**
	 * \brief A new {@code int} vector of length <b>n</b>, where each element
	 * is randomly chosen from chosen from a uniform distribution in [min, max)
	 * without replacement.
	 * 
	 * <p><i>Without replacement</i> means that every element is unique, i.e.
	 * no number appears twice.</p>
	 * 
	 * @param n Length of the vector to create.
	 * @param min Lower bound of random numbers (inclusive).
	 * @param max Upper bound of random numbers (exclusive).
	 * @return {@code int[]} array of length <b>n</b>, with all elements
	 * randomly chosen from a uniform distribution between <b>min</b>
	 * (inclusive) and <b>max</b> (exclusive).
	 * @throws IllegalArgumentException Cannot sample more times than there are
	 * possibilities.
	 */
	public static int[] randomIntsNoReplacement(int n, int min, int max)
	{
		if ( n > (max - min) )
		{
			throw new IllegalArgumentException(
					"Cannot sample more times than there are possibilities.");
		}
		int[] out = new int[n];
		boolean resample;
		for ( int i = 0; i < n; i++ )
		{
			do 
			{
				out[i] = ExtraMath.getUniRandInt(min, max);
				resample = false;
				for ( int j = 0; j < i; j++ )
					if ( out[i] == out[j] )
						resample = true;
			} while ( resample );
		}
		return out;
	}
	
	
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
	
	/**
	 * \brief Recast an double[] as a float[].
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	float[] array where each element is the recast double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static float[] toFloat(double[] vector)
	{
		float[] out = new float[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (float) vector[i];
		return out;
	}
	
	
	/*************************************************************************
	 * RESCALING VECTORS
	 ************************************************************************/
	
	/**
	 * \brief Scale the <b>source</b> vector, so that it has different
	 * direction but new Euclidean norm, writing the result into
	 * <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param source One-dimensional array of doubles (preserved).
	 * @param newNorm Intended Euclidean norm of <b>destination</b>.
	 */
	public static void normaliseEuclidTo(double[] destination, double[] source, 
			double newNorm)
	{
		double oldNorm = normEuclid(source);
		if ( oldNorm != 0.0 )
			timesTo(destination, source, newNorm/oldNorm);
	}
	
	/**
	 * \brief Scale the <b>source</b> vector, so that it has different
	 * direction but a Euclidean norm of one, writing the result into
	 * <b>destination</b>.
	 * 
	 * @param destination One-dimensional array of doubles (overwritten).
	 * @param source One-dimensional array of doubles (preserved).
	 */
	public static void normaliseEuclidTo(double[] destination, double[] source)
	{
		normaliseEuclidTo(destination, source, 1.0);
	}
	
	/**
	 * \brief Scale the <b>source</b> vector, so that it has different
	 * direction but new Euclidean norm, writing the result into
	 * a new vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 * @param newNorm Intended Euclidean norm of <b>destination</b>.
	 */
	public static double[] normaliseEuclid(double[] vector, double newNorm)
	{
		double[] destination = new double[vector.length];
		normaliseEuclidTo(destination, vector, newNorm);
		return destination;
	}
	
	/**
	 * \brief Scale the <b>source</b> vector, so that it has different
	 * direction but a Euclidean norm of one, writing the result into
	 * a new vector.
	 * 
	 * @param vector One-dimensional array of doubles (preserved).
	 */
	public static double[] normaliseEuclid(double[] vector)
	{
		return normaliseEuclid(vector, 1.0);
	}
	
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
	public static void normaliseEuclidEquals(double[] vector, double newNorm)
	{
		double oldNorm = normEuclid(vector);
		if ( oldNorm != 0.0 )
			timesEquals(vector, newNorm/oldNorm);
		else 
		{
			Log.out("zero division");
		}
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 
	 * <b>newNorm</b> NOTE: does not check for division by 0.
	 * 
	 * <p>Note that if the <b>vector</b> is composed of all zeros, this
	 * method will simply exit with the <b>vector</b> unchanged.</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten).
	 * @param newNorm double value for the new Euclidean norm of <b>vector</i>.
	 */
	public static void normaliseEuclidEqualsUnchecked(double[] vector, double newNorm)
	{
		if(normEuclid(vector) == 0.0)
		{
			Log.out(Tier.CRITICAL,"zero division not allowed in: Vector");
			return;
		}
		timesEquals(vector, newNorm/normEuclid(vector));
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
	public static void normaliseEuclidEquals(double[] vector)
	{
		normaliseEuclidEquals(vector, 1.0);
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
	 * @see #angle(double[], double[])
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
	 * @see #cosAngle(double[], double[])
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
	
	/**
	 * \brief Find the mid-point of two vectors, writing the result into 
	 * <b>destination</b>.
	 * 
	 * @param destination  One-dimensional array of doubles to be overwritten.
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @see #midPoint(double[], double[])
	 */
	public static void midPointTo(double[] destination, double[] a, double[] b)
	{
		checkLengths(destination, a, b);
		minusTo(destination, a, b);
		timesEquals(destination, 0.5);
		addEquals(destination, b);
	}
	
	/**
	 * \brief Find the mid-point of two vectors, giving the result as a new
	 * vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return New one-dimensional array of doubles with the mid-point of
	 * <b>a</b> and <b>b</b>.
	 */
	public static double[] midPoint(double[] a, double[] b)
	{
		double[] out = new double[a.length];
		midPointTo(out, a, b);
		return out;
	}
	
	
	
	/**
	 * \brief Convert the given vector, in Cartesian coordinates, to
	 * spherical coordinates: write the result into <b>destination</b>.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @param cartesian One-dimensional array of doubles, assumed to be in
	 * Cartesian coordinates (preserved).
	 */
	public static void spherifyTo(double[] destination, double[] cartesian)
	{
		switch ( cartesian.length ) 
		{
		case 1 :
		{
			destination[0] = cartesian[0];
			return;
		}
		case 2 :
		{
			double radius = normEuclid(cartesian);
			double angle = Math.atan2(cartesian[1], cartesian[0]);
			destination[0] = radius;
			destination[1] = angle;
			return;
		}
		case 3 : 
		{
			double radius = normEuclid(cartesian);
			double theta = Math.atan2(cartesian[1], cartesian[0]);
			double phi = Math.acos(cartesian[2]/radius);
			destination[0] = radius;
			destination[2] = phi;
			destination[1] = theta;
		}
		}
	}
	
	/**
	 * \brief Convert the given vector, in <b>cartesian</b> coordinates, to
	 * spherical coordinates: write the result into a new vector.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @return	New vector of {@code double}s, with the position of
	 * <b>cartesian</b> converted to spherical coordinates.
	 */
	public static double[] spherify(double[] cartesian)
	{
		double[] spherical = new double[cartesian.length];
		spherifyTo(spherical, cartesian);
		return spherical;
	}
	
	/**
	 * \brief Convert the given vector, in <b>cartesian</b> coordinates, to
	 *  spherical coordinates: write the result into a new vector.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten),
	 * assumed originally to be in Cartesian coordinates but then in
	 * spherical coordinates.
	 */
	public static void spherifyEquals(double[] vector)
	{
		spherifyTo(vector, vector);
	}
	
	/**
	 * \brief Convert the given vector, in <b>spherical</b> coordinates, to
	 * Cartesian coordinates: write the result into <b>destination</b>.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @param spherical One-dimensional array of doubles, assumed to be in
	 * spherical coordinates (preserved).
	 */
	public static void unspherifyTo(double[] destination, double[] spherical)
	{
		checkLengths(destination, spherical);
		/* 
		 * Store angles & radius first, then set, so we can use
		 * this method for unspherifyEquals.
		 */
		switch ( spherical.length )
		{
		case 1 :
		{
			destination[0] = spherical[0];
			return;
		}
		case 2 :
		{
			double radius = spherical[0];
			double angle = spherical[1];
			destination[0] = radius * Math.cos(angle);
			destination[1] = radius * Math.sin(angle);
			return;
		}
		case 3 :
		{
			double radius = spherical[0];
			double phi = spherical[2];
			double theta = spherical[1];
			destination[0] = radius * Math.cos(theta) * Math.sin(phi);
			destination[1] = radius * Math.sin(theta) * Math.sin(phi);
			destination[2] = radius * Math.cos(phi);
		}
		}
	}
	
	/**
	 * \brief Convert the given vector, in <b>spherical</b> coordinates, to
	 * Cartesian coordinates: write the result into a new vector.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param spherical One-dimensional array of doubles, assumed to be in
	 * spherical coordinates (preserved).
	 * @return	New vector of {@code double}s, with the position of
	 * <b>spherical</b> converted to Cartesian coordinates.
	 */
	public static double[] unspherify(double[] spherical)
	{
		double[] cartesian = new double[spherical.length];
		unspherifyTo(cartesian, spherical);
		return cartesian;
	}
	
	/**
	 * \brief Convert the given vector, in spherical coordinates, to
	 * Cartesian coordinates: write the result into a new vector.
	 * 
	 * <p><b>Note</b>: the coordinate scheme used here is the same as that in
	 * {@code SphericalShape}, i.e., (r, phi, theta) rather than the more
	 * commonly used (r, theta, phi).</p>
	 * 
	 * @param vector One-dimensional array of doubles (overwritten),
	 * assumed originally to be in spherical coordinates but then in
	 * Cartesian coordinates.
	 */
	public static void unspherifyEquals(double[] vector)
	{
		unspherifyTo(vector, vector);
	}
	
	/**
	 * \brief Convert the given vector, in Cartesian coordinates, to
	 * cylindrical coordinates: write the result into <b>destination</b>.
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @param cartesian One-dimensional array of doubles, assumed to be in
	 * Cartesian coordinates (preserved).
	 */
	public static void cylindrifyTo(double[] destination, double[] cartesian)
	{
		checkLengths(destination, cartesian);
		switch ( cartesian.length ) 
		{
			case 3 :
				destination[2] = cartesian[2];
			case 2 : 
			{
				/* 
				 * Calculate angle & radius first, then set, so we can use
				 * this method for cylindrfyEquals.
				 */
				double angle = Math.atan2(cartesian[1], cartesian[0]);
				double radius = Math.hypot(cartesian[1], cartesian[0]);
				destination[1] = angle;
				destination[0] = radius;
				break;
			}
			case 1 :
				destination[0] = cartesian[0];
		}
	}
	
	/**
	 * \brief Convert the given vector, in <b>cartesian</b> coordinates, to
	 * cylindrical coordinates: write the result into a new vector.
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @return	New vector of {@code double}s, with the position of
	 * <b>cartesian</b> converted to cylindrical coordinates.
	 */
	public static double[] cylindrify(double[] cartesian)
	{
		double[] cylindrical = new double[cartesian.length];
		cylindrifyTo(cylindrical, cartesian);
		return cylindrical;
	}
	
	/**
	 * \brief Convert the given vector, in cartesian coordinates, to
	 *  cylindrical coordinates: write the result into a new vector.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten),
	 * assumed originally to be in Cartesian coordinates but then in
	 * cylindrical coordinates.
	 */
	public static void cylindrifyEquals(double[] vector)
	{
		cylindrifyTo(vector, vector);
	}
	
	/**
	 * \brief Convert the given vector, in <b>cylindrical</b> coordinates, to
	 * Cartesian coordinates: write the result into <b>destination</b>.
	 * 
	 * @param destination The vector to be filled with the result (overwritten).
	 * @param cylindrical One-dimensional array of doubles, assumed to be in
	 * cylindrical coordinates (preserved).
	 */
	public static void uncylindrifyTo(double[] destination, double[] cylindrical)
	{
		checkLengths(destination, cylindrical);
		switch ( cylindrical.length ) 
		{
			case 3 :
				destination[2] = cylindrical[2];
			case 2 : 
			{
				/* 
				 * Calculate angle & radius first, then set, so we can use
				 * this method for uncylindrfyEquals.
				 */
				double angle = cylindrical[1];
				double radius = cylindrical[0];
				destination[1] = radius * Math.sin(angle);
				destination[0] = radius * Math.cos(angle);
				break;
			}
			case 1 :
				destination[0] = cylindrical[0];
		}
	}
	
	/**
	 * \brief Convert the given vector, in <b>cylindrical</b> coordinates, to
	 * Cartesian coordinates: write the result into a new vector.
	 * 
	 * @param cylindrical One-dimensional array of doubles, assumed to be in
	 * cylindrical coordinates (preserved).
	 * @return	New vector of {@code double}s, with the position of
	 * <b>cylindrical</b> converted to Cartesian coordinates.
	 */
	public static double[] uncylindrify(double[] cylindrical)
	{
		double[] cartesian = new double[cylindrical.length];
		uncylindrifyTo(cartesian, cylindrical);
		return cartesian;
	}
	
	/**
	 * \brief Convert the given vector, in cylindrical coordinates, to
	 * Cartesian coordinates: write the result into a new vector.
	 * 
	 * @param vector One-dimensional array of doubles (overwritten),
	 * assumed originally to be in cylindrical coordinates but then in
	 * Cartesian coordinates.
	 */
	public static void uncylindrifyEquals(double[] vector)
	{
		uncylindrifyTo(vector, vector);
	}
	
	/**
	 * \brief Find the outer-product of two vectors, <b>a</b>
	 * and <b>b</b>, and write the result into <b>destination</b>.
	 * 
	 * @param destination  Two-dimensional array of doubles to be overwritten
	 * with <b>a</b> ⓧ <b>b</b> with number of rows equal to length of <b>a</b> 
	 * and number of columns equal to length of <b>b</b>.
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 */
	public static void outerProductTo(double[][] destination, double[] a,
																double[] b)
	{
		for ( int i = 0; i < a.length; i++ ) {
			for (int j = 0; j < b.length; j++) {
				destination[i][j] = a[i]*b[j];
			}
		}
	}
	
	/**
	 * \brief Find the outer-product of two vectors, <b>a</b>
	 * and <b>b</b>, and write the result into a new vector.
	 * 
	 * @param a One-dimensional array of doubles (preserved).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return new double[a.length][b.length] with the outer-product <b>a</b> ⓧ <b>b</b>.
	 */
	public static double[][] outerProduct(double[] a, double[] b)
	{
		double[][] out = new double[a.length][b.length];
		outerProductTo(out, a, b);
		return out;
	}

	public static int[] translate(double[] location, double[] resolution) 
	{
		int out[] = new int[3];
		for(int i = 0; i < resolution.length; i++)
			if( i < location.length)
				out[i] = (int) Math.floor(location[i]/resolution[i]);
			else
				out[i] = 0;
		return out;
	}

	public static boolean equals(double[] a, double[] b) {
		if(a.length != b.length)
			return false;
		for( int i = 0; i < a.length; i++)
			if( a[i] != b[i] )
				return false;
		return true;
	}


}
