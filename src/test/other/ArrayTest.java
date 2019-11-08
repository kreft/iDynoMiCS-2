package test.other;

import linearAlgebra.Matrix;

public class ArrayTest
{

	public static void main(String[] args)
	{
		/*int[][] id = Matrix.identityInt(4, 3);
		for ( int[] row : id )
		{
			for ( int elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}*/
		/*
		int[] v = Vector.zerosInt(3);
		v[0] = 1;
		v[1] = -3;
		v[2] = 2;
		for ( int elem : v )
			System.out.println(elem);
		Vector.times(v, 2);
		for ( int elem : v )
			System.out.println(elem);
		*/
		
		
		/*
		 * Solve a tridiagonal matrix.
		 * 
		boolean periodic = true;
		int m = 5;
		double[][] a = Matrix.times(Matrix.identityDbl(m), 6.0);
		for ( int i = 0; i < m -1; i++ )
		{
			a[i][i+1] = 1.3;
			a[i+1][i] = 1.8;
		}
		if ( periodic )
		{
			a[0][m-1] = 0.7;
			a[m-1][0] = 3.2;
		}
		double[] vector = Vector.add(Vector.toDbl(Vector.range(m)),0.0);
		*
		 * Solve it the brute force way.
		 *
		double[] vsolveold = Matrix.solve(a, vector);
		double[] vcheckold = Matrix.times(a, vsolveold);
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < m; j++ )
				System.out.print(a[i][j]+" ");
			System.out.print("   "+vsolveold[i]);
			System.out.print("   "+vector[i]);
			System.out.print("   "+vcheckold[i]);
			System.out.println("");
		}
		System.out.println("");
		
		*
		 * Solve it the better way.
		 *
		double[][] td;
		if ( periodic )
			td = TriDiagonal.getTriDiagPeriodic(a);
		else
			td = TriDiagonal.getTriDiag(a);
		double[] vsolvenew = Vector.copy(vector);
		if ( periodic )
			TriDiagonal.solvePeriodic(td, vsolvenew);
		else
			TriDiagonal.solve(td, vsolvenew);
		double[] vchecknew = Matrix.times(a, vsolvenew);
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < m; j++ )
				System.out.print(a[i][j]+" ");
			System.out.print("\t"+vsolvenew[i]);
			System.out.print("\t"+vector[i]);
			System.out.print("\t"+vchecknew[i]);
			System.out.println("");
		}*/
		
		/*
		 * Check it's ok to have int[] arrays in a HashMap as an Object.
		 *
		int n = 5;
		HashMap<String, Object> hash = new HashMap<String, Object>();
		int[] test = Vector.range(n);
		hash.put("test", test);
		for ( int i = 0; i < n; i++ )
			System.out.println(((int[]) hash.get("test"))[i]);
		*/
		
		/* Test matrix */
		double[][] a = Matrix.zerosDbl(3);
		a[0][0] = 1.0; a[0][1] = 2.0; a[0][2] = 3.0;
		a[1][0] = 0.0; a[1][1] = 1.0; a[1][2] = 4.0;
		a[2][0] = 5.0; a[2][1] = 6.0; a[2][2] = 0.0;
		for ( double[] row : a )
		{
			for ( double elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}
		System.out.println("");
		/* Analytic solution */
		double[][] b = Matrix.zerosDbl(3);
		b[0][0] = -24.0; b[0][1] =  18.0; b[0][2] =  5.0;
		b[1][0] =  20.0; b[1][1] = -15.0; b[1][2] = -4.0;
		b[2][0] = - 5.0; b[2][1] =   4.0; b[2][2] = 1.0;
		for ( double[] row : b )
		{
			for ( double elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}
		System.out.println("");
		/* Assert */
		double[][] invert = Matrix.invert(a);
		for ( double[] row : invert )
		{
			for ( double elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}
		System.out.println("");
		
	}

}
