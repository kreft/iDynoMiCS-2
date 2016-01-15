package test;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import boundary.BoundaryFixed;
import grid.PolarGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;

public class PolarGridTest {
	public static Scanner keyboard = new Scanner(System.in);
	
	/****** CARE: 
	 * switch of array coordinates in the spherical grid array:
	 * (r,p,t):
	 * 		- coordinates in the spherical array
	 * 		- inside[] for getLocation()
	 * (r,t,p):
	 * 		- cylindrical array coordinates
	 *  	- locations in space 
	 *  	- _ires
	 *  	- _res
	 *  	- _nVoxel
	 */
	
	/* SUGGESTED IMPROVEMENTS:
	 *  - store a cumulative sum of the resoultions to speed computation
	 *  	of locations up (where we compute it on every call)
	 *  - 
	 */

	public static void main(String[] args) {
		
		/**********************************************************************/
		/********************* CHOOSE ARRAY TYPE HERE *************************/
		/**********************************************************************/
		
		SphericalGrid grid = new SphericalGrid(
				new int[]{20,360,180},new double[]{1,1,0.5});
		
//	    CylindricalGrid grid = new CylindricalGrid(
//				new int[]{40,360,1},new double[]{1,1,1});
		
//	    CartesianGrid gridp = new CartesianGrid(new int[]{100,100,4000},1);
		
		/*
		 * create the array
		 */
		ArrayType type=ArrayType.CONCN;
		grid.newArray(type, 0);
		/*
		 * add boundaries
		 */
		for (BoundarySide bs : BoundarySide.values()){
			grid.addBoundary(bs, new BoundaryFixed().getGridMethod(""));
		}
		
		/**********************************************************************/
		/******************** CHOOSE TEST METHOD HERE *************************/
		/**********************************************************************/
		
//		testMemoryAndIteratorSpeed(grid);
//		testIterator(grid);
//		testNbhIterator(grid);
//		/*
//		 * booleans:
//		 * step manual | automatic
//		 * plot centre | origin
//		 * plot | do not plot grid
//		 */
//		createGraphics(grid,true,true,true); 
		
		plotVoxelVolumes(grid);
		
	}
	
	public static void testMemoryAndIteratorSpeed(SpatialGrid grid){
		long t_start = System.currentTimeMillis();
		long mem_start = (Runtime.getRuntime().totalMemory() 
				- Runtime.getRuntime().freeMemory());

		System.out.println("time needed to create grid: "
				+(System.currentTimeMillis()-t_start)
				+" ms");

		System.out.println("Memory usage of grid array: "+
				((Runtime.getRuntime().totalMemory() 
						- Runtime.getRuntime().freeMemory()
						)-mem_start)/1e6 + " MB");
		
		t_start = System.currentTimeMillis();
//		int last_idx=0;
		while (grid.isIteratorValid()) grid.iteratorNext();
		System.out.println("time needed to iterate through grid: "
				+(System.currentTimeMillis()-t_start)+" ms");	
	}

	public static void testIterator(SpatialGrid grid){
		int[] current;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			System.out.println("current: "+Arrays.toString(current)+
					"\torigin: "+Arrays.toString(
							grid.getVoxelOrigin(Vector.copy(current)))
					+"\tcoord: "+Arrays.toString(
							grid.getCoords(Vector.copy(
							grid.getVoxelOrigin(Vector.copy(current)))))
					+"\tvolume: "+grid.getVoxelVolume(current)
			);
			System.out.println();
		}
	}
	
	public static void testNbhIterator(SpatialGrid grid){
		int[] current;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			
			System.out.println("grid size: "
					+Arrays.toString(grid.getNumVoxels()));
			int[] nbh;
			for ( current = grid.resetIterator(); grid.isIteratorValid();
					current = grid.iteratorNext())
			{
				System.out.println("current: "+Arrays.toString(current));
				for ( nbh = grid.resetNbhIterator(); 
						grid.isNbhIteratorValid(); 
						nbh = grid.nbhIteratorNext() )
				{
					System.out.println("\tnbh: "+Arrays.toString(nbh));
				}
			}
			System.out.println();
			int[] coords=grid.getCoords(
					grid.getVoxelOrigin(new int[]{3,41,7}));
			System.out.println(coords[0]+" "+coords[1]+" "+coords[2]);
		}
	}
	
	public static void createGraphics(PolarGrid grid, boolean step_manual,
			boolean plot_centre, boolean plot_grid){
		/*
		 * PolarGrid only atm because of getLocation(..) method
		 */
		PolarGridPlot3D plot = new PolarGridPlot3D(grid,plot_centre,plot_grid);
		System.out.println("press enter to start iterator");
		keyboard.nextLine();
        if (step_manual) plot.startIterator();  
        else plot.runIterator();     
        keyboard.close();
	}

	public static void plotVoxelVolumes(SpatialGrid grid){
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries vol = new XYSeries("Volume");
		
		int[] current;
		int x=0;
		double val;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			val=grid.getVoxelVolume(current);
			vol.add(new XYDataItem(x, val));
			x++;
		}
		
		dataset.addSeries(vol);
		JFreeChart chart = ChartFactory.createXYStepChart(
				"Line Chart Demo", "X", "Y", dataset);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotHeight(4);
		renderer.setDotWidth(3);
		plot.setRenderer(renderer);
		
		/*
		 * set tick unit manually 
		 * (because it sometimes sets the tick unit too small to display)
		 */
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setTickUnit(new NumberTickUnit(0.1));
		
		ChartPanel chartPanel = new ChartPanel(chart);
//		JPanel panel = new JPanel();
//		panel.add(chartPanel);
		JFrame frame = new JFrame("Voxel Volumes");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.add(chartPanel);
	}
}
