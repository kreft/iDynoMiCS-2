package test;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.vecmath.Color3f;

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
import boundary.BoundaryFixed;
import boundary.grid.GridMethodLibrary;
import boundary.grid.GridMethodLibrary.ConstantDirichlet;
import grid.CylindricalGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment;
import idynomics.Simulator;
import linearAlgebra.Vector;
import processManager.library.SolveDiffusionTransient;
import shape.Shape;
import shape.ShapeConventions.DimName;
import test.plotting.SpatialGridPlot3D;
import test.plotting.SpatialGridPlot3D.Branch;
import test.plotting.SpatialGridPlot3D.VoxelTarget;

public class PolarGridTest
{
	public static Scanner keyboard = new Scanner(System.in);
	
	/**
	 * Diffusivity
	 */
	public static double D = 1.0;

	public static void main(String[] args)
	{
		/**********************************************************************/
		/********************* CHOOSE ARRAY TYPE HERE *************************/
		/**********************************************************************/
		
		/* standard constructors */
		
		double[] totalLength = new double[]{20, 2 * Math.PI , 1};
//		
//	    CartesianGrid grid = new CartesianGrid(totalLength);		
	    CylindricalGrid grid = new CylindricalGrid(totalLength);
//		SphericalGrid grid = new SphericalGrid(totalLength);

		
		/* resolution objects */
		
//		Object[] res_funs = new Object[]{
//			new double[]{3, 0.5, 2, 1}, 
//			(DoubleFunction<Double>) j -> (0.1 + Math.abs(Math.sin(j))), 
//			0.5
//		};
		
//		Class<?>[] res_classes = new Class[]{
//				ResolutionCalculator.SimpleVaryingResolution.class,
//				ResolutionCalculator.ResolutionFunction.class,
//				ResolutionCalculator.MultiGrid.class
//		};
		
//		ResCalc[] rC = ResCalcFactory.createResCalcForCube(
//				new double[]{5,5,5}, 
//				res_funs,
//				res_classes
//				);
		
//		ResCalc[][] rC = ResCalcFactory.createResCalcForCylinder(
//				new double[]{3,2*Math.PI,1}, 
//				res_funs,
//				res_classes
//				);
		
//		ResCalc[][][] rC = ResCalcFactory.createResCalcForSphere(
//				totalLength, 
//				res_funs,
//				res_classes
//				);
		
		
//		CartesianGrid grid = new CartesianGrid(rC);
//		CylindricalGrid grid = new CylindricalGrid(rC);
//		SphericalGrid grid = new SphericalGrid(rC);
		
				
		/**********************************************************************/
		/********************** SOME INITIALIZING *****************************/
		/**********************************************************************/			
		/*
		 * create the array
		 */
		ArrayType type=ArrayType.CONCN;
		grid.newArray(type, 0);
		/*
		 * add boundaries
		 */
//		for ( DimName dim : grid.getDimensionNames() )
//			for ( int i = 0; i < 2; i++ )
//				grid.addBoundary(dim, i, new ConstantDirichlet());
		
//		grid.setValueAt(type, new int[]{1,1,1},1);
//		grid.setValueAt(type, new int[]{2,2,1},0.5);
//		grid.setValueAt(type, new int[]{2,3,1},0.5);
//		grid.setValueAt(type, new int[]{2,4,1},0.5);
//		grid.setValueAt(type, new int[]{2,0,0},0.5);
		
		/**********************************************************************/
		/******************** CHOOSE TEST METHOD HERE *************************/
		/**********************************************************************/
		
//		testMemoryAndIteratorSpeed(grid);
//		testIterator(grid);
//		testNbhIterator(grid);
//		SpatialGridPlot3D plot = createGraphics(grid,null,ArrayType.CONCN, 
//													true, Vector.vector(3, 0.5));
//		plotVoxelVolumes(grid);
		twoDimRisePDETest();

		keyboard.close();
	}
	
	/**************************************************************************/
	/******************************** METHODS *********************************/
	/**************************************************************************/
	
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

	public static void testIterator(SpatialGrid grid)
	{
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
	
	public static void testNbhIterator(SpatialGrid grid)
	{
		int[] current;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			System.out.println("grid size: "
					+Arrays.toString(grid.getCurrentNVoxel()));
			int[] nbh;
			System.out.println("current: "+Arrays.toString(current));
			for ( nbh = grid.resetNbhIterator(); 
					grid.isNbhIteratorValid(); 
					nbh = grid.nbhIteratorNext() )
			{
				System.out.print("\tnbh: "+Arrays.toString(nbh));
				System.out.println(
						",\tshared area: "+grid.getNbhSharedSurfaceArea());
			}
			System.out.println();
		}
	}
	
	/**
	 * \brief Creates a graphical visualization of a spatial grid's array. 
	 * 
	 * @param grid the spatial grid.
	 * @param loc The world position of that grid (can be null).
	 * @param type The ArrayType.
	 * @param create_iterator Start an interactive iterator visualization.
	 * @param in_voxel_location Plot points into the voxels 
	 * 				(i.e. new double[]{0.5, 0.5, 0.5} for center points),
	 * 				can be null (no points).
	 * @return The SpatialGridPlot reference.
	 */
	public static SpatialGridPlot3D createGraphics(SpatialGrid grid, double[] loc,
		ArrayType type, boolean create_iterator, double[] in_voxel_location)
	{
		SpatialGridPlot3D plot = new SpatialGridPlot3D();
		
		plot.setWorldPosition(grid, type, loc);
		plot.autoSetCamera();
		if (in_voxel_location != null){
			plot.addPoints(grid, type, in_voxel_location);
		}
		if (create_iterator){
			System.out.println("press enter to start iterator");
			keyboard.nextLine();
			plot.startIterator(grid, type);
			keyboard.close();
		}
		return plot;
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
	
	private static void twoDimRisePDETest()
	{
		Simulator aSim = new Simulator();
		aSim.timer.setTimeStepSize(1.0);
		aSim.timer.setEndOfSimulation(50);

		Compartment aCompartment = aSim.addCompartment("twoDimRise");
		Shape aShape = (Shape) Shape.getNewInstance("circle");
		aShape.setDimensionLengths(new double[] {50.0, 2 * Math.PI, 1.0});
		aCompartment.setShape(aShape);
		/*
		 * Add the solutes and boundary conditions.
		 */
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
			
		Boundary rmin = new BoundaryFixed();
		GridMethodLibrary.ConstantDirichlet fallRMin = new GridMethodLibrary.ConstantDirichlet();
		fallRMin.setValue(1.0);
		rmin.setGridMethod("fall", fallRMin);
		aCompartment.addBoundary(DimName.R, 0, rmin);
	
		Boundary rmax = new BoundaryFixed();
		GridMethodLibrary.ConstantDirichlet riseRMax = new GridMethodLibrary.ConstantDirichlet();
		riseRMax.setValue(1.0);
		rmax.setGridMethod("rise", riseRMax);
		aCompartment.addBoundary(DimName.R, 1, rmax);
		
		aCompartment.init();
		/*
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeStepSize(aSim.timer.getTimeStepSize());
		aCompartment.addProcessManager(aProcess);
		
		//TODO twoDimIncompleteDomain(nStep, stepSize);
		SpatialGrid riseGrid = (SpatialGrid) aCompartment.getSolute("rise");
		SpatialGridPlot3D plot = createGraphics(riseGrid, Vector.zerosDbl(3), 
												ArrayType.CONCN, false, null);
		plot.setTransparency(VoxelTarget.ALL, riseGrid, ArrayType.CONCN, 0);
		plot.setPolygonMode(VoxelTarget.ALL, riseGrid, ArrayType.CONCN, true);
		plot.setColor(Branch.Voxels, VoxelTarget.ALL, riseGrid, ArrayType.CONCN, 
														new Color3f(0f,0f,1f));
		long t;
		System.out.println("press enter to start PDE");
		keyboard.nextLine();
		while ( aSim.timer.isRunning() )
		{
			t = System.currentTimeMillis();
			System.out.print("solving PDE step...");
			aSim.step();
			System.out.println(" done!");
			System.out.print(" plotting concentrations...");
			plot.plotCurrentConcentrations(riseGrid);
			System.out.println(" done!");
		}
		/*
		 * Print the results.
		 */
		aSim.printAll();
	}
}
