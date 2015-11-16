package test;

import java.util.Arrays;

import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;

public class CylindricalGridTest {

	public static void main(String[] args) {
		CylindricalGrid gridp = new CylindricalGrid(new int[]{3,360,1},1);
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		System.out.println(gridp.arrayAsText(type));
		System.out.println();
		
		CartesianGrid gridc=gridp.toCartesianGrid(type);
		System.out.println(gridc.arrayAsText(type));
		System.out.println();
		
		System.out.println("grid size: "+Arrays.toString(gridp.getNumVoxels()));
		int[] current, nbh;
		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
				  current = gridp.iteratorNext())
		{
			System.out.println("current: "+Arrays.toString(current));
			for ( nbh = gridp.resetNbhIterator(false); 
					gridp.isNbhIteratorValid(); nbh = gridp.nbhIteratorNext() )
			{
				System.out.println("\tnbh: "+Arrays.toString(nbh));
			}
		}
	}

}
