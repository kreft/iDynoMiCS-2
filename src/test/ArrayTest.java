package test;

import array.Matrix;
import array.Vector;

public class ArrayTest
{

	public static void main(String[] args)
	{
		int[][] id = Matrix.identityInt(4, 3);
		for ( int[] row : id )
		{
			for ( int elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}
		
		int[] v = Vector.zeroInt(3);
		v[0] = 1;
		v[1] = -3;
		v[2] = 2;
		System.out.println(Vector.max(v));
	}

}
