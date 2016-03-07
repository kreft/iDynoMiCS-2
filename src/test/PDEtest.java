/**
 * 
 */
package test;

import org.w3c.dom.Node;

import boundary.*;
import grid.GridBoundary;
import grid.GridBoundary.*;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Simulator;
import linearAlgebra.Vector;
import processManager.SolveDiffusionTransient;
import shape.Shape;
import shape.ShapeLibrary;
import shape.ShapeConventions.DimName;

public class PDEtest
{
	public static double D = 1.0;
	
	public static void main(String[] args)
	{
		Simulator aSimulator = new Simulator();
		aSimulator.timer.setTimeStepSize(1.0);
		aSimulator.timer.setEndOfSimulation(10.0);
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
		aSimulator.run();
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
		
		// TODO Bas [10.12.15] why do 1D and 2D compartments need to have 3 side
		// lengths? This seems to define the amount of voxels in each direction
		// how do we set the actual (metric) size of the domain?
		Compartment aCompartment = aSim.addCompartment("oneDimRiseFall");
		Shape aShape = (Shape) Shape.getNewInstance("Line");
		aShape.setDimensionLengths(new double[] {4.0, 1.0, 1.0});
		aCompartment.setShape(aShape);
		
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
		aCompartment.addBoundary(DimName.X, 0, xmin);
		Boundary xmax = new BoundaryFixed();
		ConstantDirichlet riseXmax = new ConstantDirichlet();
		riseXmax.setValue(1.0);
		xmin.setGridMethod("rise", riseXmax);
		aCompartment.addBoundary(DimName.X, 1, xmax);
		//TODO diffusivities
		aCompartment.init();
		/*
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(aSim.timer.getTimeStepSize());
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
		Compartment aCompartment = aSim.addCompartment("twoDimRandInitDiagBndrs");
		Shape aShape = (Shape) Shape.getNewInstance("Rectangle");
		aShape.setDimensionLengths(new double[] {3.0, 3.0, 1.0});
		aCompartment.setShape(aShape);
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
		for ( DimName dim : aCompartment.getShape().getDimensionNames() )
			for ( int i = 0; i < 2; i++ )
			{
				Boundary bndry = new Boundary();
				bndry.setGridMethod("solute", aGridMethod);
				aCompartment.addBoundary(dim, i, bndry);
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
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(aSim.timer.getTimeStepSize());
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
		
		Compartment aCompartment = aSim.addCompartment("twoDimRandInitCyclBndrs");
		Shape aShape = (Shape) Shape.getNewInstance("Rectangle");
		aShape.setDimensionLengths(new double[] {3.0, 3.0, 1.0});
		aCompartment.setShape(aShape);
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
		aCompartment.addBoundary(DimName.X, 0, new BoundaryFixed(0.0));
		aCompartment.addBoundary(DimName.X, 1, new BoundaryFixed(1.0));
		aCompartment.getShape().getDimension(DimName.Y).setCyclic();
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
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(aSim.timer.getTimeStepSize());
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
		Shape aShape = new ShapeLibrary.Rectangle();
		aShape.setDimensionLengths(length);
		EnvironmentContainer environment = new EnvironmentContainer(aShape);
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
			sg.newArray(ArrayType.WELLMIXED);
			sg.setAllTo(ArrayType.WELLMIXED, 1.0);
			sg.newArray(ArrayType.PRODUCTIONRATE);
			sg.newArray(ArrayType.DIFFPRODUCTIONRATE);
			coords[0] = 1;
			coords[1] = 1;
			sg.setValueAt(ArrayType.CONCN, coords, 1.0);
			sg.setValueAt(ArrayType.WELLMIXED, coords, 0.0);
		}
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer("Dimensionless");
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
		/* Stefan [11Feb2016]: assumes CartesianGrid (same nVoxel in all dims)*/
		int[] dims = Vector.copy(sg.getCurrentNVoxel());
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