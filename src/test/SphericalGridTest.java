package test;

import java.util.Arrays;

import grid.SpatialGrid.ArrayType;
import test.plotting.PolarGridPlot3D;
import grid.SphericalGrid;

public class SphericalGridTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SphericalGrid gridp = new SphericalGrid(new int[]{4,360,180},1);
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
			System.out.println(Arrays.toString(gridp.getVoxelOrigin(current)));
			System.out.println(Arrays.toString(gridp.getCoords(gridp.getVoxelOrigin(current))));
			System.out.println();
			z++;
		}
		new PolarGridPlot3D(gridp);
		System.out.println();
		System.out.println(gridp.length());
	}

}
