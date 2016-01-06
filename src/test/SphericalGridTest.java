package test;

import java.io.IOException;
import java.util.Arrays;

import boundary.Boundary;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;
import grid.SphericalGrid;

public class SphericalGridTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SphericalGrid gridp = new SphericalGrid(new int[]{4,360,180},new double[]{1,1,1});
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
		
//		int[] current;
//		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
//				current = gridp.iteratorNext())
//		{
//			System.out.println("current: "+Arrays.toString(current)+
//			"\torigin: "+Arrays.toString(gridp.getVoxelOrigin(Vector.copy(current)))
//			+"\tcoord: "+Arrays.toString(gridp.getCoords(Vector.copy(gridp.getVoxelOrigin(Vector.copy(current))))));
//
//			System.out.println("current: "+Arrays.toString(current)+"\tindex: "+gridp.coord2idx(current)
//			+"\tcoord: "+Arrays.toString(gridp.idx2coord(gridp.coord2idx(current),null)));
//			
//			System.out.println();
//		}
//		
//		System.out.println(gridp.length());
		
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,true);
		System.out.println("press enter to start iterator");
		CylindricalGridTest.keyboard.nextLine();
//        plot.startIterator();
        plot.runIterator();
        CylindricalGridTest.keyboard.close();
	}

}
