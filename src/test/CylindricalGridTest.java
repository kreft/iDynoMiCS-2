package test;

import java.util.Arrays;
import java.util.Scanner;

import boundary.Boundary;
import boundary.BoundaryFixed;
import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;
import grid.SpatialGrid.GridMethod;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;

public class CylindricalGridTest {
	public static Scanner keyboard = new Scanner(System.in);

	public static void main(String[] args) {
//		CylindricalGrid gridp = new CylindricalGrid(
//				new int[]{5,360,3},
//				new double[][]{{1,.5,.5,.5,.5},{1},{.5,2,1}});
		CylindricalGrid gridp = new CylindricalGrid(
				new int[]{30,360,1},
				new double[]{1,1,1});
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		gridp.addBoundary(BoundarySide.CIRCUMFERENCE, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.INTERNAL, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.YMAX, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.YMIN, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.ZMAX, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.ZMIN, Boundary.constantDirichlet(0.0));
		
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
        
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,true);
		System.out.println("press enter to start iterator");
		keyboard.nextLine();
//        plot.startIterator();
        plot.runIterator();
        keyboard.close();
		
//		System.out.println(Arrays.toString(gridp.cyclicTransform(new int[]{0,-1,0})));
	}

}
