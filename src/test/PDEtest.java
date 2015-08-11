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
	public static void main(String[] args)
	{
		double stepSize = 5.0;
		int nStep = 10;
		
		oneDimRiseFall(nStep, stepSize);
	}
	
	private static void oneDimRiseFall(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing 1D domain for two solutes:");
		System.out.println("\tLeft & right fixed");
		System.out.println("\tD = 0.1");
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
			sg.setAllTo(SpatialGrid.diff, 0.1, true);
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
			System.out.print(name+": ");
			for ( int i = -1; i < nVoxel[0]; i++ )
			{
				coords[0] = i;
				System.out.print(solutes.get(name).getValueAt(SpatialGrid.concn, coords)+", ");
			}
			coords[0] = nVoxel[0];
			System.out.println(solutes.get(name).getValueAt(SpatialGrid.concn, coords));
		}
		for ( ; nStep > 0; nStep-- )
		{
			process.step(solutes, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.print(name+": ");
				for ( int i = -1; i < nVoxel[0]; i++ )
				{
					coords[0] = i;
					System.out.print(solutes.get(name).getValueAt(SpatialGrid.concn, coords)+", ");
				}
				coords[0] = nVoxel[0];
				System.out.println(solutes.get(name).getValueAt(SpatialGrid.concn, coords));
			}
		}
		System.out.println("\n");
	}
}