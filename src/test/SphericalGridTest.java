package test;

import java.util.Arrays;

import boundary.Boundary;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;

public class SphericalGridTest {

	public static void main(String[] args) {
		
	    long mem_start = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	    long t_start = System.currentTimeMillis();
		
		// TODO Auto-generated method stub
		SphericalGrid gridp = new SphericalGrid(new int[]{5,360,180},new double[]{1,1,1});
//	    CylindricalGrid gridp = new CylindricalGrid(new int[]{400,360,180},new double[]{1,1,1});
//	    SpatialGrid gridp = new CartesianGrid(new int[]{100,100,4000},1);
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		
		gridp.addBoundary(BoundarySide.CIRCUMFERENCE, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.INTERNAL, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.YMAX, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.YMIN, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.ZMAX, Boundary.constantDirichlet(0.0));
		gridp.addBoundary(BoundarySide.ZMIN, Boundary.constantDirichlet(0.0));
		
		System.out.println("time needed to create grid: "
				+(System.currentTimeMillis()-t_start)
				+" ms");
		
		System.out.println("Memory usage in byte: "+
				((Runtime.getRuntime().totalMemory() 
						- Runtime.getRuntime().freeMemory()
				)-mem_start)/1e6
				+ " MB");
		
//		System.out.println(gridp.arrayAsText(type));
//		System.out.println();
		
//		t_start = System.currentTimeMillis();
//		
//		int[] current, current_prev=null;
//		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
//				current = gridp.iteratorNext())
//		{
////			System.out.println("current: "+Arrays.toString(current)+
////			"\torigin: "+Arrays.toString(gridp.getVoxelOrigin(Vector.copy(current)))
////			+"\tcoord: "+Arrays.toString(gridp.getCoords(Vector.copy(gridp.getVoxelOrigin(Vector.copy(current))))));
////
////			System.out.println("current: "+Arrays.toString(current)+"\tindex: "+gridp.coord2idx(current)
////			+"\tcoord: "+Arrays.toString(gridp.idx2coord(gridp.coord2idx(current),null)));
////			
////			if(current_prev!=null && current_prev[0]==current[0]&& current_prev[1]==current[1]&& current_prev[2]==current[2]) break;
////			if (current[0]<0 || current[1]<0||current[2]<0) break;
////			current_prev=Vector.copy(current);
////			System.out.println();
//		}
//		
//		System.out.println("time needed to iterate through grid: "+(System.currentTimeMillis()-t_start)+" ms");
//		
//		System.out.println("number of grid elements: "+gridp.length());
		
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,true);
		System.out.println("press enter to start iterator");
		CylindricalGridTest.keyboard.nextLine();
//        plot.startIterator();
        plot.runIterator();
        CylindricalGridTest.keyboard.close();
	}

}
