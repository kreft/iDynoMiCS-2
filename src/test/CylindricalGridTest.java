package test;

import java.util.Arrays;

import org.jfree.ui.RefineryUtilities;

import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;
import test.plotting.PolarGridPlot;

public class CylindricalGridTest {

	public static void main(String[] args) {
		CylindricalGrid gridp = new CylindricalGrid(new int[]{21,360,1},1);
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		System.out.println(gridp.arrayAsText(type));
		System.out.println();
		
		
		CartesianGrid gridc=gridp.toCartesianGrid(type);
		System.out.println(gridc.arrayAsText(type));
		System.out.println();
		
		
//		System.out.println("grid size: "+Arrays.toString(gridp.getNumVoxels()));
//		int[] current, nbh;
//		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
//				  current = gridp.iteratorNext())
//		{
//			System.out.println("current: "+Arrays.toString(current));
//			for ( nbh = gridp.resetNbhIterator(); 
//					gridp.isNbhIteratorValid(); nbh = gridp.nbhIteratorNext() )
//			{
//				System.out.println("\tnbh: "+Arrays.toString(nbh));
//			}
//		}
//		System.out.println();
//		int[] coords=gridp.getCoords(gridp.getVoxelOrigin(new int[]{3,41,7}));
//		System.out.println(coords[0]+" "+coords[1]+" "+coords[2]);
				
		System.out.println();
		final PolarGridPlot demo = new PolarGridPlot("Polar Chart Demo",gridp);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        demo.start();
	}

}
