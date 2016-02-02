/**
 * 
 */
package test;

import org.w3c.dom.Node;

import boundary.*;
import grid.CartesianGrid;
import grid.GridBoundary;
import grid.GridBoundary.*;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Simulator;
import idynomics.Timer;
import linearAlgebra.Vector;
import processManager.PrepareSoluteGrids;
import processManager.SolveDiffusionTransient;
import shape.ShapeLibrary;

public class PDEtest
{
	public static double D = 1.0;
	
	public static void main(String[] args)
	{
		Timer.setTimeStepSize(1.0);
		Timer.setEndOfSimulation(10.0);
		
		Simulator aSimulator = new Simulator();
		/*
		 * Add the test compartments.
		 */
		oneDimRiseFallComp(aSimulator);
		twoDimRandInitDiagBndrs(aSimulator);
		twoDimRandInitCyclBndrs(aSimulator);
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
		Compartment aCompartment = aSim.addCompartment("oneDimRiseFall", "Line");
		
		// TODO Bas [10.12.15] why do 1D and 2D compartments need to have 3 side
		// lengths? This seems to define the amount of voxels in each direction
		// how do we set the actual (metric) size of the domain?
		aCompartment.setSideLengths(new double[] {4.0, 1.0, 1.0});
		/*
		 * Add the solutes and boundary conditions.
		 */
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		Boundary xmin = new BoundaryFixed();
		ConstantDirichlet fallXmin = new ConstantDirichlet();
		fallXmin.setValue(1.0);
		xmin.setGridMethod("fall", fallXmin);
		aCompartment.addBoundary("xmin", xmin);
		Boundary xmax = new BoundaryFixed();
		ConstantDirichlet riseXmax = new ConstantDirichlet();
		riseXmax.setValue(1.0);
		xmin.setGridMethod("rise", riseXmax);
		aCompartment.addBoundary("xmax", xmax);
		//TODO diffusivities
		aCompartment.init();
		/*
		 * The solute grids will need prepping before the solver can get to work.
		 */
		PrepareSoluteGrids aPrep = new PrepareSoluteGrids();
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
	
	private static void twoDimRandInitDiagBndrs(Simulator aSim)
	{
		System.out.println("###############################################");
		System.out.println("COMPARTMENT: twoDimRandInitDiagBndrs");
		System.out.println("Testing 2D domain for one solute:");
		System.out.println("\tRandom starting concentrations");
		System.out.println("\tDirichlet boundaries fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear along diagonal");
		System.out.println("###############################################");
		Compartment aCompartment = aSim.addCompartment(
									"twoDimRandInitDiagBndrs", "Rectangle");
		aCompartment.setSideLengths(new double[] {3.0, 3.0, 1.0});
		/*
		 * 
		 */
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		/*
		 * Set the boundary methods and initialise the compartment.
		 */
		
		GridMethod aGridMethod = new GridMethod()
		{
			public void init(Node xmlNode)
			{
				
			}
			
			@Override
			public double getBoundaryFlux(SpatialGrid grid)
			{
				int[] current = grid.iteratorCurrent();
				return GridBoundary.calcFlux(
								Vector.sum(current)/4.0, 
								grid.getValueAtCurrent(ArrayType.CONCN),
								grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
								grid.getNbhSharedSurfaceArea());
			}
			
		};
		
		aGridMethod.getClass();
		for ( String side : new String[] {"xmin", "xmax", "ymin", "ymax"})
		{
			Boundary bndry = new Boundary();
			bndry.setGridMethod("solute", aGridMethod);
			aCompartment.addBoundary(side, bndry);
		}
		//TODO diffusivities
		aCompartment.init();
		/*
		 * Initialise the concentration array with random values.
		 */
		SpatialGrid sg = aCompartment.getSolute("solute");
		for ( int[] coords = sg.resetIterator() ; sg.isIteratorValid();
												coords = sg.iteratorNext() )
		{
			sg.setValueAt(ArrayType.CONCN, coords, Math.random());
		}
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
	
	private static void twoDimRandInitCyclBndrs(Simulator aSim)
	{
		System.out.println("###############################################");
		System.out.println("COMPARTMENT: twoDimRandInitCyclBndrs");
		System.out.println("Testing 2D domain for one solute:");
		System.out.println("\tRandom starting concentrations");
		System.out.println("\tDirichlet boundaries fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear along diagonal");
		System.out.println("###############################################");
		Compartment aCompartment = aSim.addCompartment(
									"twoDimRandInitCyclBndrs", "Rectangle");
		aCompartment.setSideLengths(new double[] {3.0, 3.0, 1.0});
		/*
		 * 
		 */
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		/*
		 * Set the boundary methods and initialise the compartment.
		 */
		Boundary xmin = new BoundaryFixed(0.0);
		aCompartment.addBoundary("xmin", xmin);
		Boundary xmax = new BoundaryFixed(1.0);
		aCompartment.addBoundary("xmax", xmax);
		BoundaryCyclic ymin = new BoundaryCyclic();
		BoundaryCyclic ymax = new BoundaryCyclic();
		ymin.setPartnerBoundary(ymax);
		ymax.setPartnerBoundary(ymin);
		aCompartment.addBoundary("ymin", ymin);
		aCompartment.addBoundary("ymax", ymax);
		//TODO diffusivities
		aCompartment.init();
		/*
		 * Initialise the concentration array with random values.
		 */
		SpatialGrid sg = aCompartment.getSolute("solute");
		for ( int[] coords = sg.resetIterator() ; sg.isIteratorValid();
				coords = sg.iteratorNext() )
		{
			sg.setValueAt(ArrayType.CONCN, coords, Math.random());
		}
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
	
	private static void twoDimIncompleteDomain(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing 2D domain for one solute:");
		System.out.println("\tCentre of domain excluded");
		System.out.println("\tBoundaries fixed at one");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear");
		System.out.println("###############################################");
		
		
		double[] length = Vector.vector(3, 1.0);
		length[0] = length[1] = 3;
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		EnvironmentContainer environment =
				new EnvironmentContainer(new ShapeLibrary.Rectangle());
		environment.setSize(length, 1.0);
		SpatialGrid sg;
		int[] coords = Vector.vector(3, 0);
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			environment.addSolute(name);
			sg = environment.getSoluteGrid(name);
			for ( int j = 0; j < length[0]; j++ )
			{
				coords[0] = j;
				coords[1] = -1;
				sg.setValueAt(ArrayType.CONCN, coords, 0.0);
				coords[1] = 3;
				sg.setValueAt(ArrayType.CONCN, coords, 0.0);
				coords[1] = j;
				coords[0] = -1;
				sg.setValueAt(ArrayType.CONCN, coords, 0.0);
				coords[0] = 3;
				sg.setValueAt(ArrayType.CONCN, coords, 0.0);
			}
			sg.newArray(ArrayType.DIFFUSIVITY);
			sg.setAllTo(ArrayType.DIFFUSIVITY, D);
			sg.newArray(ArrayType.DOMAIN);
			sg.setAllTo(ArrayType.DOMAIN, 1.0);
			sg.newArray(ArrayType.PRODUCTIONRATE);
			sg.newArray(ArrayType.DIFFPRODUCTIONRATE);
			coords[0] = 1;
			coords[1] = 1;
			sg.setValueAt(ArrayType.CONCN, coords, 1.0);
			sg.setValueAt(ArrayType.DOMAIN, coords, 0.0);
		}
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer();
		SolveDiffusionTransient process = new SolveDiffusionTransient();
		process.init(soluteNames);
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);
		System.out.println("Time: "+process.getTimeForNextStep());
		printSoluteGrid(environment.getSoluteGrid("solute"));
		for ( ; nStep > 0; nStep-- )
		{
			process.step(environment, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.println(name+": ");
				printSoluteGrid(environment.getSoluteGrid(name));
			}
		}
		System.out.println("\n");
	}
	
	private static void printSoluteGrid(SpatialGrid sg)
	{
		int[] dims = Vector.copy(sg.getNumVoxels());
		int[] start = Vector.zeros(dims);
		boolean[] sig = sg.getSignificantAxes();
		int[] coords = Vector.zeros(dims);
		for ( int i = 0; i < 3; i++ )
			if ( sig[i] )
				start[i]--;
		for ( int i = start[0]; i < dims[0]; i++ )
		{
			coords[0] = i;
			for ( int j = start[1]; j < dims[1]; j++ )
			{
				coords[1] = j;
				for ( int k = start[2]; k < dims[2]; k++ )
				{
					coords[2] = k;
					System.out.printf("%.5f, ", sg.getValueAt(ArrayType.CONCN, coords));
				}
			}
			System.out.println("");
		}
		
		
	}
}