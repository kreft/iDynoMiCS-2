/**
 * 
 */
package test;

import java.util.HashMap;

import grid.SpatialGrid;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import processManager.SolveDiffusionTransient;

public class PDEtest
{
	public static double D = 0.1;
	
	public static void main(String[] args)
	{
		double stepSize = 10.0;
		int nStep = 5;
		
		//oneDimRiseFall(nStep, stepSize);
		//twoDimRandInit(nStep, stepSize);
		twoDimIncompleteDomain(nStep, stepSize);
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
		
		int[] padding = Vector.vector(3, 0);
		padding[0] = 1;
		
		double resolution = 1.0;
		
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";

		HashMap<String, SpatialGrid> solutes = 
				new HashMap<String, SpatialGrid>();
		SpatialGrid sg;
		int[] coords = Vector.vector(3, 0);
		double value;
		double k = 1.0;
		double fudge = Math.exp(-k*(nVoxel[0]+1.0));
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			sg = new SpatialGrid(nVoxel, padding, resolution);
			sg.newArray(SpatialGrid.concn);
			for ( int j = -1; j < nVoxel[0]+1; j++ )
			{
				value = i + ((int)Math.pow(-1,i))*(Math.exp(-k*(j+1.0))-fudge)/(1.0-fudge);
				coords[0] = j;
				sg.addValueAt(SpatialGrid.concn, coords, value);
			}
			sg.newArray(SpatialGrid.diff);
			sg.setAllTo(SpatialGrid.diff, D, true);
			sg.newArray(SpatialGrid.domain);
			sg.setAllTo(SpatialGrid.domain, 1.0, true);
			sg.newArray(SpatialGrid.reac);
			sg.newArray(SpatialGrid.dReac);
			solutes.put(name, sg);
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
			printSoluteGrid(solutes.get(name));
		}
		for ( ; nStep > 0; nStep-- )
		{
			process.step(solutes, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.println(name+": ");
				printSoluteGrid(solutes.get(name));
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
		
		int[] padding = Vector.vector(3, 0);
		padding[0] = padding[1] = 1;
		
		double resolution = 1.0;
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		
		HashMap<String, SpatialGrid> solutes = 
				new HashMap<String, SpatialGrid>();
		SpatialGrid sg;
		int[] coords = Vector.vector(3, 0);
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			sg = new SpatialGrid(nVoxel, padding, resolution);
			sg.newArray(SpatialGrid.concn);
			for ( int j = -padding[0]; j < nVoxel[0]+padding[0]; j++ )
			{
				coords[0] = j;
				coords[1] = -1;
				sg.setValueAt(SpatialGrid.concn, coords, (j+1.0)/8.0);
				coords[1] = 3;
				sg.setValueAt(SpatialGrid.concn, coords, (5.0+j)/8.0);
				coords[1] = j;
				coords[0] = -1;
				sg.setValueAt(SpatialGrid.concn, coords, (j+1.0)/8.0);
				coords[0] = 3;
				sg.setValueAt(SpatialGrid.concn, coords, (5.0+j)/8.0);
			}
			for ( coords = sg.resetIterator() ; sg.isIteratorValid();
												coords = sg.iteratorNext() )
			{
				sg.setValueAt(SpatialGrid.concn, coords, Math.random());
			}
			sg.newArray(SpatialGrid.diff);
			sg.setAllTo(SpatialGrid.diff, D, true);
			sg.newArray(SpatialGrid.domain);
			sg.setAllTo(SpatialGrid.domain, 1.0, true);
			sg.newArray(SpatialGrid.reac);
			sg.newArray(SpatialGrid.dReac);
			solutes.put(name, sg);
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
		printSoluteGrid(solutes.get("solute"));
		for ( ; nStep > 0; nStep-- )
		{
			process.step(solutes, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.println(name+": ");
				printSoluteGrid(solutes.get(name));
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
		
		int[] padding = Vector.vector(3, 0);
		padding[0] = padding[1] = 1;
		
		double resolution = 1.0;
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "solute";
		
		HashMap<String, SpatialGrid> solutes = 
				new HashMap<String, SpatialGrid>();
		SpatialGrid sg;
		int[] coords = Vector.vector(3, 0);
		for ( int i = 0; i < soluteNames.length; i++ )
		{
			String name = soluteNames[i];
			sg = new SpatialGrid(nVoxel, padding, resolution);
			sg.newArray(SpatialGrid.concn);
			for ( int j = -padding[0]; j < nVoxel[0]+padding[0]; j++ )
			{
				coords[0] = j;
				coords[1] = -1;
				sg.setValueAt(SpatialGrid.concn, coords, 0.0);
				coords[1] = 3;
				sg.setValueAt(SpatialGrid.concn, coords, 0.0);
				coords[1] = j;
				coords[0] = -1;
				sg.setValueAt(SpatialGrid.concn, coords, 0.0);
				coords[0] = 3;
				sg.setValueAt(SpatialGrid.concn, coords, 0.0);
			}
			sg.newArray(SpatialGrid.diff);
			sg.setAllTo(SpatialGrid.diff, D, true);
			sg.newArray(SpatialGrid.domain);
			sg.setAllTo(SpatialGrid.domain, 1.0, true);
			sg.newArray(SpatialGrid.reac);
			sg.newArray(SpatialGrid.dReac);
			coords[0] = 1;
			coords[1] = 1;
			sg.setValueAt(SpatialGrid.concn, coords, 1.0);
			sg.setValueAt(SpatialGrid.domain, coords, 0.0);
			solutes.put(name, sg);
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
		printSoluteGrid(solutes.get("solute"));
		for ( ; nStep > 0; nStep-- )
		{
			process.step(solutes, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.println(name+": ");
				printSoluteGrid(solutes.get(name));
			}
		}
		System.out.println("\n");
	}
	
	private static void printSoluteGrid(SpatialGrid sg)
	{
		int[] dims = Vector.add(sg.getNumVoxels(), sg.getPadding());
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
					System.out.printf("%.5f, ", sg.getValueAt(SpatialGrid.concn, coords));
				}
			}
			System.out.println("");
		}
		
		
	}
}