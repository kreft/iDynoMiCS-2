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

import boundary.Boundary;
import boundary.BoundaryCyclic;
import boundary.BoundaryFixed;
import grid.CylindricalGrid;
import grid.PolarGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import grid.GridBoundary.ConstantDirichlet;
import idynomics.Compartment;
import idynomics.Simulator;
import idynomics.Timer;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import processManager.PrepareSoluteGrids;
import processManager.SolveDiffusionTransient;
import test.plotting.PolarGridPlot3D;

public class PolarGridTest {
	public static Scanner keyboard = new Scanner(System.in);
	public static double D = 1.0;
	
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

	public static void main(String[] args) {
		/* Questions:
		 *  - discuss getNumVoxel(),
		 *  - what happens for getCoords() and getLocation() if outside bounds? 
		 */
		
		/**********************************************************************/
		/********************* CHOOSE ARRAY TYPE HERE *************************/
		/**********************************************************************/
		
//		SphericalGrid grid = new SphericalGrid(new double[]{3,90,90},1);
		SphericalGrid grid = new SphericalGrid(new double[]{3,90,90},
				new double[]{1,1,1});
		
//	    CylindricalGrid grid = new CylindricalGrid(new double[]{3,360,1},1);
//		CylindricalGrid grid = new CylindricalGrid(
//				new double[]{3,360,1},new double[]{1,1,1});
		
//	    CartesianGrid gridp = new CartesianGrid(new int[]{100,100,4000},1);
		
		Timer.setTimeStepSize(1.0);
		Timer.setEndOfSimulation(10.0);
		
		Simulator aSimulator = new Simulator();
		
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
//		 * do | do not plot grid cell polygons
//		 */
//		createGraphics(grid,true,true,true); 
		
//		plotVoxelVolumes(grid);
		oneDimRiseFallComp(aSimulator);
		
		/**********************************************************************/
		/***************************** SIMULATION *****************************/
		/**********************************************************************/
		
		//TODO twoDimIncompleteDomain(nStep, stepSize);
		/*
		 * Launch the simulation.
		 */
		aSimulator.launch();
		/*
		 * Print the results.
		 */
		aSimulator.printAll();
		
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
	
	private static void oneDimRiseFallComp(Simulator aSim)
	{
		System.out.println("###############################################");
		System.out.println("COMPARTMENT: oneDimRiseFall");
		System.out.println("Testing 1D domain for two solutes:");
		System.out.println("\tLeft & right fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear");
		System.out.println("###############################################");
		Compartment aCompartment = aSim.addCompartment("oneDimRiseFall", "disk");
		aCompartment.setSideLengths(new double[] {3.0, 360.0, 1.0});
		/*
		 * Add the solutes and boundary conditions.
		 */
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		
//		Boundary inter = new BoundaryCyclic();
//		ConstantDirichlet fallInter = new ConstantDirichlet();
//		fallInter.setValue(1.0);
//		inter.setGridMethod("fall", fallInter);
//		aCompartment.addBoundary("INTERNAL", inter);
//		
		Boundary circ = new BoundaryFixed();
		ConstantDirichlet riseCirc = new ConstantDirichlet();
		riseCirc.setValue(1.0);
		circ.setGridMethod("rise", riseCirc);
		aCompartment.addBoundary("CIRCUMFERENCE", circ);
		
		Boundary xmin = new BoundaryFixed();
		ConstantDirichlet fallXmin = new ConstantDirichlet();
		fallXmin.setValue(1.0);
		xmin.setGridMethod("fall", fallXmin);
		aCompartment.addBoundary("XMIN", xmin);
		
		Boundary xmax = new BoundaryFixed();
		ConstantDirichlet riseXmax = new ConstantDirichlet();
		riseXmax.setValue(1.0);
		xmax.setGridMethod("rise", riseXmax);
		aCompartment.addBoundary("XMAX", xmax);
		
		Boundary ymin = new BoundaryFixed();
		ConstantDirichlet fallYmin = new ConstantDirichlet();
		fallYmin.setValue(1.0);
		ymin.setGridMethod("fall", fallYmin);
		aCompartment.addBoundary("YMIN", ymin);
		
		Boundary ymax = new BoundaryFixed();
		ConstantDirichlet riseYmax = new ConstantDirichlet();
		riseYmax.setValue(1.0);
		ymax.setGridMethod("rise", riseYmax);
		aCompartment.addBoundary("YMAX", ymax);
		
		Boundary zmin = new BoundaryFixed();
		ConstantDirichlet fallZmin = new ConstantDirichlet();
		fallZmin.setValue(1.0);
		zmin.setGridMethod("fall", fallZmin);
		aCompartment.addBoundary("ZMIN", zmin);
		
		Boundary zmax = new BoundaryFixed();
		ConstantDirichlet riseZmax = new ConstantDirichlet();
		riseZmax.setValue(1.0);
		zmax.setGridMethod("rise", riseZmax);
		aCompartment.addBoundary("ZMAX", zmax);
		
		//TODO diffusivities
		aCompartment.init();
		/*
		 * The solute grids will need prepping before the solver can get to work.
		 */
		PrepareSoluteGrids aPrep = new PrepareSoluteGrids();
		aPrep.setTimeStepSize(Double.MAX_VALUE);
		aCompartment.addProcessManager(aPrep);
		/*
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(Timer.getTimeStepSize());
		aCompartment.addProcessManager(aProcess);
	}
}
