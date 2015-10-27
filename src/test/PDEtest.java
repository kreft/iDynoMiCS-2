/**
 * 
 */
package test;

import boundary.*;
import grid.CartesianGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Timer;
import linearAlgebra.Vector;
import processManager.SolveDiffusionTransient;

public class PDEtest
{
	public static double D = 1.0;
	
	public static void main(String[] args)
	{
		double stepSize = 10.0;
		int nStep = 5;
		
		oneDimRiseFallNew(nStep, stepSize);
		//oneDimRiseFall(nStep, stepSize);
		//twoDimRandInit(nStep, stepSize);
		//twoDimIncompleteDomain(nStep, stepSize);
	}
	
	private static void oneDimRiseFallNew(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing 1D domain for two solutes:");
		System.out.println("\tLeft & right fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear");
		System.out.println("###############################################");
		
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";
		
		Compartment aCompartment = new Compartment();
		aCompartment.setShape("line");
		aCompartment.addBoundary("xmin", new BoundaryFixed(0.0));
		aCompartment.addBoundary("xmax", new BoundaryFixed(1.0));
		aCompartment.setSideLengths(new double[] {3.0, 1.0, 1.0});
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(stepSize);
		aCompartment.addProcessManager(aProcess);
		
		Timer aTimer = new Timer();
		aTimer.setTimeStepSize(stepSize);
		
		
	}
	
	private static void oneDimRiseFall(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing 1D domain for two solutes:");
		System.out.println("\tLeft & right fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear");
		System.out.println("###############################################");
		
		int[] nVoxel = Vector.vector(3, 1);
		nVoxel[0] = 3;
		
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";

		EnvironmentContainer environment = new EnvironmentContainer();
		environment.init(nVoxel, 1.0);
		CartesianGrid sg;
		int[] coords = Vector.vector(3, 0);
		double value;
		double k = 1.0;
		double fudge = Math.exp(-k*(nVoxel[0]+1.0));
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			environment.addSolute(name);
			sg = environment.getSoluteGrid(name);
			for ( int j = -1; j < nVoxel[0]+1; j++ )
			{
				value = i + ((int)Math.pow(-1,i))*(Math.exp(-k*(j+1.0))-fudge)/(1.0-fudge);
				coords[0] = j;
				sg.addValueAt(ArrayType.CONCN, coords, value);
			}
			sg.newArray(ArrayType.DIFFUSIVITY);
			sg.setAllTo(ArrayType.DIFFUSIVITY, D);
			sg.newArray(ArrayType.DOMAIN);
			sg.setAllTo(ArrayType.DOMAIN, 1.0);
			sg.newArray(ArrayType.PRODUCTIONRATE);
			sg.newArray(ArrayType.DIFFPRODUCTIONRATE);
		}
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer();
		/*
		 * 
		 */
		SolveDiffusionTransient process = new SolveDiffusionTransient();
		process.init(soluteNames);
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);
		System.out.println("Time: "+process.getTimeForNextStep());
		for ( String name : soluteNames )
		{
			System.out.println(name+": ");
			printSoluteGrid(environment.getSoluteGrid(name));
		}
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
	
	private static void twoDimRandInit(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing 2D domain for one solute:");
		System.out.println("\tRandom starting concentrations");
		System.out.println("\tBoundaries fixed");
		System.out.println("\tD = "+D);
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards linear");
		System.out.println("###############################################");
		
		
		int[] nVoxel = Vector.vector(3, 1);
		nVoxel[0] = nVoxel[1] = 3;
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		
		EnvironmentContainer environment = new EnvironmentContainer();
		environment.init(nVoxel, 1.0);
		CartesianGrid sg;
		int[] coords = Vector.vector(3, 0);
		for ( String name : soluteNames )
		{
			environment.addSolute(name);
			sg = environment.getSoluteGrid(name);
			for ( int j = 0; j < nVoxel[0]; j++ )
			{
				coords[0] = j;
				coords[1] = -1;
				sg.setValueAt(ArrayType.CONCN, coords, (j+1.0)/8.0);
				coords[1] = 3;
				sg.setValueAt(ArrayType.CONCN, coords, (5.0+j)/8.0);
				coords[1] = j;
				coords[0] = -1;
				sg.setValueAt(ArrayType.CONCN, coords, (j+1.0)/8.0);
				coords[0] = 3;
				sg.setValueAt(ArrayType.CONCN, coords, (5.0+j)/8.0);
			}
			for ( coords = sg.resetIterator() ; sg.isIteratorValid();
												coords = sg.iteratorNext() )
			{
				sg.setValueAt(ArrayType.CONCN, coords, Math.random());
			}
			sg.newArray(ArrayType.DIFFUSIVITY);
			sg.setAllTo(ArrayType.DIFFUSIVITY, D);
			sg.newArray(ArrayType.DOMAIN);
			sg.setAllTo(ArrayType.DOMAIN, 1.0);
			sg.newArray(ArrayType.PRODUCTIONRATE);
			sg.newArray(ArrayType.DIFFPRODUCTIONRATE);
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
		
		
		int[] nVoxel = Vector.vector(3, 1);
		nVoxel[0] = nVoxel[1] = 3;
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		
		EnvironmentContainer environment = new EnvironmentContainer();
		environment.init(nVoxel, 1.0);
		CartesianGrid sg;
		int[] coords = Vector.vector(3, 0);
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			environment.addSolute(name);
			sg = environment.getSoluteGrid(name);
			for ( int j = 0; j < nVoxel[0]; j++ )
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
	
	private static void printSoluteGrid(CartesianGrid sg)
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