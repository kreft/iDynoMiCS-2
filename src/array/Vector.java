package array;

/**
 * \brief Abstract class of useful vector functions.
 * 
 * <p>Both integer vectors (int[]) and real-number vectors (double[]) are
 * handled in this class. Splitting the class into two has been considered,
 * but this may cause confusion when converting from one to the other, or when
 * trying to use methods that take both as arguments.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class Vector
{
	/*************************************************************************
	 * SIMPLE INTEGER METHODS
	 ************************************************************************/
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the integer
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>setAll(copy(<b>vector</b>), <b>value</b>)</i> or
	 * <i>newInt(<b>vector</b>.length, <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector int[] array to use.
	 * @param value	int value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
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
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reset(copy(<b>vector</b>))</i> or
	 * <i>zerosInt(<b>vector</b>.length)</i> 
	 * to preserve the original state of <b>vector</b>.</p>
	 * 
	 * @param vector	int[] array to reset.
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 */
	public static int[] reset(int[] vector)
	{
		return setAll(vector, 0);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	int value to use.
	 * @return	int[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 */
	public static int[] vector(int n, int value)
	{
		int[] vector = new int[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	int[] array of length <b>n</b>, with all elements set to zero.
	 */
	public static int[] zerosInt(int n)
	{
		return vector(n, 0);
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new int[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector	int[] array to be copied.
	 * @return	int[] that is a copy of <b>vector</b>.
	 */
	public static int[] copy(int[] vector)
	{
		int[] v = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			v[i] = vector[i];
		return v;
	}
	
	/**
	 *  \brief Add a scalar <b>value</b> to every element of  a <b>vector</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>add(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector	int[] array to use.
	 * @param value	int to add to every element of <b>vector</b>.
	 * @return	Given <b>vector</b>, with all elements greater by
	 * <b>value</b>.
	 */
	public static int[] add(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] += value;
		return vector;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>times(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector	int[] array to be used.
	 * @param value	int scalar by which to multiply <b>vector</b>.
	 * @return	Given <b>vector</b> multiplied by the given <b>value</b>.
	 */
	public static int[] times(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] *= value;
		return vector;
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reverse(copy(<b>vector</b>))</i> to preserve the original state of
	 * <b>vector</b>.</p>
	 * 
	 * @param vector	int[] array to be reversed.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static int[] reverse(int[] vector)
	{
		return times(vector, -1);
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector	int[] array to be used.
	 * @return int sum of all elements in the vector.
	 */
	public static int sum(int[] vector)
	{
		int sum = 0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector int[] array to be used.
	 * @return double sum of all elements in the vector.
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
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector int[] array to be used.
	 * @return	double Euclidean norm of elements in <b>vector</b>.
	 */
	public static double normEuclid(int[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/*************************************************************************
	 * SIMPLE DOUBLE METHODS
	 ************************************************************************/
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the double
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>setAll(copy(<b>vector</b>), <b>value</b>)</i> or
	 * <i>newDbl(<b>vector</b>.length, <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector	double[] array to use.
	 * @param value	double value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
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
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reset(copy(<b>vector</b>))</i> or
	 * <i>zerosDbl(<b>vector</b>.length)</i> 
	 * to preserve the original state of <b>vector</b>.</p>
	 * 
	 * @param vector	double[] array to reset.
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 */
	public static double[] reset(double[] vector)
	{
		return setAll(vector, 0.0);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	double value to use.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 */
	public static double[] vector(int n, double value)
	{
		double[] vector = new double[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * zero.
	 */
	public static double[] zerosDbl(int n)
	{
		return vector(n, 0.0);
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new double[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector	double[] array to be copied.
	 * @return	double[] that is a copy of <b>vector</b>.
	 */
	public static double[] copy(double[] vector)
	{
		double[] v = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			v[i] = vector[i];
		return v;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of  a <b>vector</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>add(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector	double[] array to use.
	 * @param value	double to add to every element of <b>vector</b>.
	 * @return	Given <b>vector</b>, with all elements greater by
	 * <b>value</b>.
	 */
	public static double[] add(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] += value;
		return vector;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>times(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector	double[] array to be used.
	 * @param value	double scalar by which to multiply <b>vector</b>.
	 * @return	Given <b>vector</b> multiplied by the given <b>value</b>.
	 */
	public static double[] times(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] *= value;
		return vector;
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reverse(copy(<b>vector</b>))</i> to preserve the original state of
	 * <b>vector</b>.</p>
	 * 
	 * @param vector	double[] array to be reversed.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static double[] reverse(double[] vector)
	{
		return times(vector, -1.0);
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * @param vector double[] array to be used.
	 * @return double sum of all elements in the vector.
	 */
	public static double sum(double[] vector)
	{
		double sum = 0.0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * @param vector double[] array to be used.
	 * @return double sum of all elements in the vector.
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
	 * @param vector double[] array to be used.
	 * @return	double Euclidean norm of elements in <b>vector</b>.
	 */
	public static double normEuclid(double[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 
	 * <b>newNorm</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>normaliseEuclid(copy(<b>vector</b>), <b>value</b>)</i> to preserve
	 * the original state of <b>vector</b>.</p>
	 * 
	 * @param vector double[] array to be normalised.
	 * @param newNorm double value for the Euclidean norm of <b>vector</i>.
	 * @return double[] array of <b>vector</b>, where the elements have been
	 * scaled so that {@link #normEuclid(double[] vector)} = newNorm.
	 */
	public static double[] normaliseEuclid(double[] vector, double newNorm)
	{
		return times(vector, newNorm/normEuclid(vector));
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 1.0
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>normaliseEuclid(copy(<b>vector</b>))</i> to preserve the original
	 * state of <b>vector</b>.</p>
	 * 
	 * @param vector double[] array to be normalised.
	 * @return double[] array of <b>vector</b>, where the elements have been
	 * scaled so that {@link #normEuclid(double[] vector)} = 1.0
	 */
	public static double[] normaliseEuclid(double[] vector)
	{
		return normaliseEuclid(vector, 1.0);
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INT[] AND DOUBLE[]
	 ************************************************************************/
	
	/**
	 * \brief Recast a double[] as an int[].
	 * 
	 * <p>Note that any digits after the decimal point are simply discarded.
	 * See {@link #round(double[])}, etc for alternate methods.</p>  
	 * 
	 * @param vector	double[] array to be recast. 
	 * @return	int[] array where each element is the recast double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] toDbl(double[] vector)
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
	 * @param vector	double[] array to be recast. 
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
	 * @param vector	double[] array to be recast. 
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
	 * @param vector	double[] array to be recast. 
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
	 * @param vector	int[] array to be recast. 
	 * @return	double[] array where each element is the recast int in the
	 * corresponding position of <b>vector</b>.
	 */
	public static double[] toInt(int[] vector)
	{
		double[] out = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (double) vector[i];
		return out;
	}
	
	
	/*************************************************************************
	 * TWO VECTOR METHODS
	 ************************************************************************/
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a int[] array to be used (overwritten).
	 * @param b int[] array to be used (not overwritten).
	 * @return int[] array of <b>a</b> + <b>b</b>.
	 */
	public static int[] add(int[] a, int[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a double[] array to be used (overwritten).
	 * @param b double[] array to be used (not overwritten).
	 * @return double[] array of <b>a</b> + <b>b</b>.
	 */
	public static double[] add(double[] a, double[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * <p>Not also that <i>a + b = b + a</i>. The result is returned as a
	 * double. Returning an int can be done safely using: <i>(int)
	 * add(a, b)</i> or <i>add(</i>{@link #toInt(double[] a)}<i>, b)</i>.</p>
	 * 
	 * @param a double[] array to be used (overwritten).
	 * @param b int[] array to be used (not overwritten).
	 * @return double[] array of <b>a</b> + <b>b</b>.
	 */
	public static double[] add(double[] a, int[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a int[] array to be used (overwritten).
	 * @param b int[] array to be used (not overwritten).
	 * @return int[] array of <b>a</b> times <b>b</b>.
	 */
	public static int[] times(int[] a, int[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a double[] array to be used (overwritten).
	 * @param b double[] array to be used (not overwritten).
	 * @return double[] array of <b>a</b> times <b>b</b>.
	 */
	public static double[] times(double[] a, double[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * <p>furthermore, the result is returned as a double. Returning an
	 * int is potentially ambiguous: <i>(int) times(a, b)</i> will often
	 * be different to <i>times(</i>{@link #toInt(double[] a)}<i>, b)</i>!</p>
	 * 
	 * @param a double[] array to be used (overwritten).
	 * @param b int[] array to be used (not overwritten).
	 * @return double[] array of <b>a</b> times <b>b</b>.
	 */
	public static double[] times(double[] a, int[] b)
	{
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that the order they are entered as arguments is irrelevant.</p>
	 * 
	 * @param a	int[] array to be used.
	 * @param b	int[] array to be used.
	 * @return int value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static int dotProduct(int[] a, int[] b)
	{
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
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that the order they are entered as arguments is irrelevant.</p>
	 * 
	 * @param a	double[] array to be used.
	 * @param b	double[] array to be used.
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static double dotProduct(double[] a, double[] b)
	{
		double out = 0.0;
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
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that <i>a</i>.<i>b</i> = <i>b</i>.<i>a</i>.</p>
	 * 
	 * <p>Note also that the result is returned as a double. Returning an
	 * int is potentially ambiguous: <i>(int) dotProduct(a, b)</i> will often
	 * be different to <i>dotProduct(</i>{@link #toInt(double[] a)}<i>, b)</i>
	 * !</p>
	 * 
	 * @param a	double[] array to be used.
	 * @param b	int[] array to be used.
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static double dotProduct(double[] a, int[] b)
	{
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
}