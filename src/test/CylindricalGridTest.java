package test;

import java.util.Arrays;

import grid.CartesianGrid;
import grid.CylindricalGrid;
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
			for ( nbh = gridp.resetNbhIterator(); 
					gridp.isNbhIteratorValid(); nbh = gridp.nbhIteratorNext() )
			{
				System.out.println("\tnbh: "+Arrays.toString(nbh));
			}
		}
		System.out.println();
//		int[] coords=gridp.getCoords(new double[]{2,2,0});
		int[] coords=gridp.getCoords(gridp.getVoxelOrigin(new int[]{3,41,7}));
		System.out.println(coords[0]+" "+coords[1]+" "+coords[2]);
		
		
		gridp.iteratorCurrent()[0]=1;
		gridp.iteratorCurrent()[1]=1;
		gridp.iteratorCurrent()[2]=0;
		for ( nbh = gridp.resetNbhIterator(); 
				gridp.isNbhIteratorValid(); nbh = gridp.nbhIteratorNext() )
		{
			System.out.println("\tnbh: "+Arrays.toString(nbh));
		}
	}

}
