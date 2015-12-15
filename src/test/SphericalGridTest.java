package test;

import java.io.IOException;
import java.util.Arrays;

import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;
import grid.SphericalGrid;

public class SphericalGridTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SphericalGrid gridp = new SphericalGrid(new int[]{3,360,180},new double[]{1,1,1});
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		
//		System.out.println(gridp.arrayAsText(type));
//		System.out.println();
		
		int[] current;
		int z=0;
		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
				current = gridp.iteratorNext())
		{
			System.out.println(Arrays.toString(current));
			System.out.println(Arrays.toString(gridp.getVoxelOrigin(Vector.copy(current))));
			System.out.println(Arrays.toString(gridp.getCoords(Vector.copy(gridp.getVoxelOrigin(Vector.copy(current))))));
			System.out.println();
			z++;
		}
		
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true); // false=origin, true=centre
//		System.out.println();
		System.out.println(gridp.length());
		System.out.println("press enter to start iterator");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        plot.start();
	}

}
