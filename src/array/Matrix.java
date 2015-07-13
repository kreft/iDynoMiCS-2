package array;

public final class Matrix
{
	/*************************************************************************
	 * SIMPLE INTEGER METHODS
	 ************************************************************************/
	
	/**
	 * \brief Set all elements of the given <b>matrix</b> to the integer
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>matrix</b> will be overwritten; use
	 * <i>setAll(copy(<b>matrix</b>), <b>value</b>)</i> or
	 * <i>newInt(<b>matrix</b>.length, <b>matrix</b>[0].length,
	 * <b>value</b>)</i> to preserve the original state of <b>matrix</b>.</p>
	 * 
	 * @param matrix int[][] array to use.
	 * @param value int value to use.
	 * @return Given <b>matrix</b> with all elements set to <b>value</b>.
	 */
	public static int[][] setAll(int[][] matrix, int value)
	{
		for ( int i = 0; i < matrix.length; i++ )
			for ( int j = 0; j < matrix[0].length; j++ )
				matrix[i][j] = value;
		return matrix;
	}
	
	public static int[][] newInt(int m, int n, int value)
	{
		int[][] out = new int[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = value;
		return out;
	}
	
	public static int[][] newInt(int mn, int value)
	{
		return newInt(mn, mn, value);
	}
	
	public static int[][] zerosInt(int m, int n)
	{
		return newInt(m, n, 0);
	}
	
	/**
	 * \brief Constructs a square matrix full of zeros.
	 * 
	 * @param mn	Number of rows (same as number of columns)
	 * @return	int[][] array composed of zeros.
	 */
	public static int[][] zerosInt(int mn)
	{
		return newInt(mn, 0);
	}
	
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
	
	
	/*************************************************************************
	 * SIMPLE DOUBLE METHODS
	 ************************************************************************/
	
	public static double[][] newDbl(int m, int n, double value)
	{
		double[][] out = new double[m][n];
		for ( int i = 0; i < m; i++ )
			for ( int j = 0; j < n; j++ )
				out[i][j] = value;
		return out;
	}
	
	public static double[][] newDbl(int mn, double value)
	{
		return newDbl(mn, mn, value);
	}
	
	public static double[][] zerosDbl(int m, int n)
	{
		return newDbl(m, n, 0.0);
	}
	
	/**
	 * \brief Constructs a square matrix full of double zeros.
	 * 
	 * @param mn	Number of rows (same as number of columns)
	 * @return	double[][] array composed of zeros.
	 */
	public static double[][] zerosDbl(int mn)
	{
		return newDbl(mn, 0.0);
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
	
	public static double[][] transpose(double[] vector)
	{
		double[][] out = new double[1][vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[0][i] = vector[i];
		return out;
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INTEGER AND DOUBLE
	 ************************************************************************/
	
	
	public static int[] toInt(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) vector[i];
		return out;
	}
	
	
	public static double[] toDbl(int[] vector)
	{
		double[] out = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (double) vector[i];
		return out;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
}