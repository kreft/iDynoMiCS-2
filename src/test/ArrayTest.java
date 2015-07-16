package test;

import array.*;

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
		
		int[] v = Vector.zerosInt(3);
		v[0] = 1;
		v[1] = -3;
		v[2] = 2;
		for ( int elem : v )
			System.out.println(elem);
		Vector.times(v, 2);
		for ( int elem : v )
			System.out.println(elem);
	}

}
