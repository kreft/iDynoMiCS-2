package utility;

public final class Vector
{
	/*************************************************************************
	 * VOID METHODS
	 ************************************************************************/
	
	/**
	 * \brief Replaces all fields of vector with zeros.
	 * 
	 * @param vector Double[] to be reset.
	 */
	public static void reset(Double[] vector)
	{
		for ( int i = 0; i < vector.length; i++ )
			vector[i] = 0.0;
	}
	
	/**
	 * \brief Adds scalar value to each field in the given vector.
	 * 
	 * @param vector
	 * @param scalar
	 */
	public static void addTo(Double[] vector, Double scalar)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] += scalar;
	}
	
	/**
	 * \brief Adds vector2 to vector1 on a field-by-field basis.
	 * 
	 * vector1 is changed, but vector2 is not.
	 * 
	 * @param vector1
	 * @param vector2
	 */
	public static void addTo(Double[] vector1, Double[] vector2)
	{
		for ( int i = 0; i< vector1.length; i++ ) 
			vector1[i] += vector2[i];
	}
	
	/*************************************************************************
	 * METHODS RETURNING VECTORS
	 ************************************************************************/
	
	/**
	 * 
	 * TODO This is a duplicate of ExtraMath.newDoubleArray
	 * 
	 * @param n	Length of the vector required.
	 * @return Vector of length n with zero in each field.
	 */
	public static Double[] zeros(int n)
	{
		Double[] v = new Double[n];
		for ( int i = 0; i < n; i++ )
			v[i] = 0.0;
		return v;
	}
	
	/**
	 * \brief 
	 * 
	 * @param Double[] vector to be copied.
	 * @return A copy of the vector given.
	 */
	public static Double[] copy(Double[] vector)
	{
		Double[] out = new Double[vector.length];
		for(int i = 0; i < vector.length; i++)
			out[i] = vector[i];
		return out;
	}

	/**
	 * 
	 * @param vector
	 * @param scalar
	 * @return vector multiplied by scalar
	 */
	public static Double[] scaleCopy(Double[] vector, Double scalar)
	{
		Double[] out = copy(vector);
		for ( int i = 0; i < out.length; i++ )
			out[i] *= scalar;
		return out;
	}
	
	/**
	 * 
	 * @param vector
	 * @return vector in opposing direction
	 */
	public static Double[] reverseCopy(Double[] vector)
	{
		return scaleCopy(vector, -1.0);
	}
	
	/**
	 * 
	 * @param vector1
	 * @param vector2
	 * @return the sum of vector and second
	 */
	public static Double[] addCopy(Double[] vector1, Double[] vector2)
	{
		Double[] c = copy(vector1);
		for ( int i = 0; i < c.length; i++ ) 
			c[i] += vector2[i];
		return c;
	}
	
	/**
	 * 
	 * Field-by-field subtraction: vector1[field] - vector2[field]
	 * 
	 * @param vector1
	 * @param vector2
	 * @return 
	 */
	public static Double[] minusCopy(Double[] vector1, Double[] vector2)
	{
		Double[] out = copy(vector1);
		for ( int i = 0; i < out.length; i++ ) 
			out[i] -= vector2[i];
		return out;
	}
	
	/**
	 * 
	 * @param vector
	 * @return normalized vector
	 * 
	 */
	public static Double[] normalizeCopy(Double[] vector)
	{
		Double[] c = copy(vector);
		Double norm = normEuclid(vector);
		for ( int i = 0; i < vector.length; i++ )
			vector[i] /= norm;
		return c;
	}
	
	/**
	 * 
	 * TODO Change Math.random() to 
	 * 
	 * @param n	Length of the vector required.
	 * @param scalar
	 * @return Vector of random numbers between -scalar and scalar of length n
	 */
	public static Double[] randomDirection(int n, Double scalar)
	{
		Double[] v = new Double[n];
		for ( int i = 0; i < n; i++ )
			v[i] = 2.0 * scalar * (Math.random() - 0.5);
		return v;
	}
	
	/**
	 * 
	 * @param n	Length of the vector required.
	 * @return a vector of random numbers between -1.0 and 1.0 of length n
	 */
	public static Double[] randomDirection(int n)
	{
		return randomDirection(n, 1.0);
	}
	
	
	/*************************************************************************
	 * METHODS RETURNING SCALARS
	 ************************************************************************/
	
	/**
	 * 
	 * @param vector
	 * @return the sum of all fields of the vector
	 */
	public static Double sum(Double[] vector)
	{
		Double sum = 0.0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * 
	 * TODO Safety checking.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return Dot product of the two vectors.
	 */
	public static Double dotProduct(Double[] vector1, Double[] vector2)
	{
		Double out = 0.0;
		for ( int i = 0; i < vector1.length; i++ )
			out += vector1[i] * vector2[i];	
		return out;
	}
	
	/**
	 * 
	 * @param vector
	 * @return Squared Euclidean norm
	 */
	public static Double normSquare(Double[] vector)
	{
		return dotProduct(vector, vector);
	}
	
	/**
	 * 
	 * @param vector
	 * @return Euclidean norm
	 */
	public static Double normEuclid(Double[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/*************************************************************************
	 * METHODS THAT NEED CHECKING
	 ************************************************************************/
	
	/**
	 * \brief Field-by-field addition of second to vector, even if second is of
	 * different length!
	 * @param vector1
	 * @param vector2
	 */
	public static void addIffy(Double[] vector1, Double[] vector2)
	{
		int l = Math.min(vector1.length, vector2.length);
		for(int i=0; i<l; i++) 
			vector1[i] += vector2[i];
	}
		
	/**
	 * TODO: this is the quick and (very) dirty approach, clean-up
	 * 
	 * TODO Rob 1July2015: What is this for???
	 * 
	 * @param v0
	 * @param v1
	 * @param shift
	 * @param randomfactor
	 * @return exact point between v0 and v1 shifted by shift randomized by
	 * randomfactor
	 */
	public static Double[] between(Double[] v0, Double[] v1, double shift, double randomfactor)
	{
		Double[] h 		= minusCopy(v0, v1);
		h 				= normalizeCopy(h);
		Double distance	= normEuclid(h);
		addTo(h,randomfactor*2*(Math.random()-0.5));
		return minusCopy(v0,scaleCopy(h,(distance/2.0)+shift)); 
	}

}
