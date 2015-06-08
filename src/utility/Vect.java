package utility;

public final class Vect {
	
	/**
	 * @param vector
	 * @return Euclidean norm
	 */
	public static double normE(Double[] vector) {
		double temp = 0.0;
		for(int i=0; i<vector.length; i++)
			temp += vector[i] * vector[i];
		return Math.sqrt(temp);
	}
	
	/**
	 * @param vector
	 * @return Squared Euclidean norm
	 */
	public static double normSquare(Double[] vector) {
		double temp = 0.0;
		for(int i=0; i<vector.length; i++)
			temp += vector[i] * vector[i];
		return temp;
	}
	
	/**
	 * \brief creates a new vector that represents a field by field subtraction 
	 * of vector[field] - second[field]. Does not overwrite 'vector'.
	 * @param vector
	 * @param second
	 * @return field by field subtraction vector[field] - second[field]
	 */
	public static Double[] minus(Double[] vector, Double[] second) {
		Double[] c = copy(vector);
		for(int i=0; i<c.length; i++) 
			c[i] -= second[i];
		return c;	
	}
	
	/**
	 * \brief divides every field of 'vector' by the euclidean norm of 'vector'.
	 * Does not overwrite 'vector'
	 * @param vector
	 * @return normalized vector
	 */
	public static Double[] normalize(Double[] vector) {
		Double[] c = copy(vector);
		double norm = normE(vector);
		for(int i=0; i<vector.length; i++)
			vector[i] /= norm;
		return c;
	}
	
	/**
	 * \brief adds the Double 'second' to every field of 'vector'. Overwrites
	 * 'vector'
	 * @param vector
	 * @param second
	 */
	public static void add(Double[] vector, Double second) {
		for(int i=0; i<vector.length; i++) 
			vector[i] += second;
	}
	
	/**
	 * \brief field by field addition of vector[field] + second[field].
	 * Overwrites 'vector'
	 * @param vector
	 * @param second
	 */
	public static void add(Double[] vector, Double[] second) {
		for(int i=0; i<vector.length; i++) 
			vector[i] += second[i];
	}
	
	/**
	 * \brief creates a new vector that represents a field by field addition of 
	 * vector[field] + second[field]. Does not overwrite 'vector'
	 * @param vector
	 * @param second
	 * @return the sum of vector and second
	 */
	public static Double[] sum(Double[] vector, Double[] second) {
		Double[] c = copy(vector);
		add(c,second);
		return c;
	}
	
	/**
	 * \brief the sum of all fields in 'vector'.
	 * @param vector
	 * @return the sum of all fields of the vector
	 */
	public static Double sum(Double[] vector) {
		Double c = 0.0;
		for(int i=0; i<vector.length; i++) 
			c += vector[i];
		return c;
	}
	
	/**
	 * \brief field by field addition of second to vector, even if second is of
	 * different length! 
	 * @param vector
	 * @param second
	 */
	public static Double[] addIffy(Double[] vector, Double[] second) {
		Double[] c = copy(vector);
		int l = Math.min(c.length, second.length);
		for(int i=0; i<l; i++) 
			c[i] += second[i];
		return c;
	}
	
	/**
	 * \brief multiplies all fields of 'vector' by 'scalar'. Does not overwrite
	 * 'vector'
	 * @param vector
	 * @param scalar
	 * @return vector multiplied by scalar
	 */
	public static Double[] product(Double[] vector, double scalar) {
		Double[] c = copy(vector);
		for (int i = 0; i < c.length; i++)
			c[i] *= scalar;
		return c;
	}
	
	/**
	 * \brief creates a new vector that represents a field by field 
	 * multiplication of vector[field] * second[field]. Does not overwrite 
	 * 'vector'
	 * @param vector
	 * @param scalar
	 * @return vector multiplied by scalar
	 */
	public static Double[] product(Double[] vector, Double[] second) {
		Double[] c = copy(vector);
		for (int i = 0; i < c.length; i++)
			c[i] *= second[i];
		return c;
	}
	
	/**
	 * \brief the dot product is the sum of a field by field multiplication of
	 * 'vector' and 'second'
	 * @param vector
	 * @param second
	 * @return the dot product of vector and second
	 */
	public static double dot(Double[] vector, Double[] second) {
		double dot = 0.0;
		for (int i = 0; i < vector.length; i++)
			dot += vector[i] * second[i];	
		return dot;
	}

	/**
	 * \brief replaces all fields of vector by 0.0.
	 * @param vector
	 */
	public static void reset(Double[] vector) {
		for(int i=0; i<vector.length; i++) 
			vector[i] = 0.0;
	}
	
	/**
	 * \brief creates a new vector of random numbers between -1.0 and 1.0 of 
	 * length 'n'.
	 * @param n
	 * @return a vector of random numbers between -1.0 and 1.0 of length 'n'.
	 */
	public static Double[] randomDirection(int n) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = (Math.random()-0.5)*2.0;
		return v;
	}
	
	/**
	 * \brief creates a new vector of random numbers between -1.0 and 1.0 of 
	 * length 'n' scaled by 'scalar'.
	 * @param n
	 * @param scalar
	 * @return vector of random numbers between -scalar and scalar of length n
	 */
	public static Double[] randomDirection(int n, double scalar) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = scalar*(Math.random()-0.5)*2.0;
		return v;
	}
	
	/**
 	 * \brief creates a new vector of zero's (0.0) of length 'n'.
	 * @param n
	 * @return vector of length n with 0.0 in each field
	 */
	public static Double[] zeros(int n) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = 0.0;
		return v;
	}
	
	/**
	 * \brief returns a new vector that is in oposite direction of 'vector'
	 * @param vector
	 * @return vector in oposing direction
	 */
	public static Double[] inverse(Double[] vector) {
		Double[] v = new Double[vector.length];
		for(int i = 0; i < vector.length; i++)
			v[i] = 0.0 - vector[i];
		return v;
	}
	
	/**
	 * \brief creates a new field by field copy of 'vector'.
	 * @param vector
	 * @return a copy of vector
	 */
	public static Double[] copy(Double[] vector) {
		Double[] v = new Double[vector.length];
		for(int i = 0; i < vector.length; i++)
			v[i] = vector[i];
		return v;
	}
	
	/**
	 * \brief creates a new vector that represents a point between 'vector' and
	 * 'second'. This point can by shifted by 'shift' and randomized by
	 * 'randomfactor'. Used in rod-cell cell division. 
	 * TODO: this is the quick and (very) dirty approach, clean-up
	 * @param vector
	 * @param second
	 * @param shift
	 * @param randomfactor
	 * @return exact point between v0 and v1 shifted by 'shift' randomized by
	 * 'randomfactor'.
	 */
	public static Double[] between(Double[] vector, Double[] second, 
			double shift, double randomfactor) 
	{
		Double[] h 		= minus(vector,second);
		h 				= normalize(h);
		double distance	= normE(h);
		add(h,randomfactor*2*(Math.random()-0.5));
		return minus(vector,product(h,(distance/2.0)+shift)); 
	}

}
