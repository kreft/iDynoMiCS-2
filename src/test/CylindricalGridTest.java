package test;

import java.util.Arrays;
import java.util.Scanner;

import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;

public class CylindricalGridTest {
	public static Scanner keyboard = new Scanner(System.in);

	public static void main(String[] args) {
//		CylindricalGrid gridp = new CylindricalGrid(
//				new int[]{5,360,3},
//				new double[][]{{1,.5,.5,.5,.5},{1},{.5,2,1}});
		CylindricalGrid gridp = new CylindricalGrid(
				new int[]{5,360,3},
				new double[]{1,1,1});
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
//		System.out.println(gridp.arrayAsText(type));
//		System.out.println();
		
		
//		CartesianGrid gridc=gridp.toCartesianGrid(type);
//		System.out.println(gridc.arrayAsText(type));
//		System.out.println();
		
		
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
		
//		int[] current;
//		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
//				current = gridp.iteratorNext())
//		{
//			System.out.println(Arrays.toString(current));
//			System.out.println(Arrays.toString(gridp.getVoxelOrigin(Vector.copy(current))));
//			System.out.println(Arrays.toString(gridp.getCoords(Vector.copy(gridp.getVoxelOrigin(Vector.copy(current))))));
//			System.out.println();
//		}
				
//		System.out.println();
//		final PolarGridPlot demo = new PolarGridPlot("Polar Chart Demo",gridp);
//        demo.pack();
//        RefineryUtilities.centerFrameOnScreen(demo);
//        demo.setVisible(true);
//        demo.start();
        
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,false);
		System.out.println("press enter to start iterator");
		keyboard.nextLine();
        plot.startIterator();
        keyboard.close();
	}

}
