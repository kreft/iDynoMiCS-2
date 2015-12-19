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
		SphericalGrid gridp = new SphericalGrid(new int[]{5,360,180},new double[]{1,1,1});
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		
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
		
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,false);
		System.out.println("press enter to start iterator");
		CylindricalGridTest.keyboard.nextLine();
        plot.startIterator();
        CylindricalGridTest.keyboard.close();
	}

}
