/**
 * 
 */
package test;

import java.util.HashMap;

import boundary.ChemostatConnection;
import dataIO.Log;
import dataIO.Log.tier;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import processManager.SolveChemostat;
import shape.ShapeLibrary;
import shape.ShapeLibrary.Dimensionless;
import utility.ExtraMath;

public class ODETest
{
	public static void main(String[] args)
	{
		// Make sure the random number generator is up and running.
		ExtraMath.initialiseRandomNumberGenerator();
		Log.set(tier.DEBUG);
		/*
		 * Setting a time step of ln(2) means that the difference between the
		 * chemostat concentrations and the respective inflow concentrations
		 * should half every timestep. 
		 */
		double timeStep = Math.log(2.0);
		int nStep = 5;
		
		oneChemostatRise(nStep, timeStep);
		//oneChemostatFall(nStep, timeStep);
		//twoChemostatBasic(nStep, timeStep);
		//compartment();
	}
	
	private static void oneChemostatRise(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing chemostat for one solute:");
		System.out.println("\tSin = 1.0");
		System.out.println("\tS0 = 0.0");
		System.out.println("\tD = 1.0");
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should tend towards one");
		System.out.println("###############################################");
		
		/*
		 * 
		 */
		String[] soluteNames = new String[]{"rise"};
		HashMap<String, Double> inflowConcn = new HashMap<String, Double>();
		inflowConcn.put(soluteNames[0], 1.0);
		/*
		 * Set up the environment, with its shape and solute.
		 */
		Dimensionless myShape = new ShapeLibrary.Dimensionless();
		myShape.setVolume(1.0);
		EnvironmentContainer environment = new EnvironmentContainer(myShape);
		for ( String name : soluteNames )
			environment.addSolute(name, 0.0);
		/*
		 * Add the inflow and outflow.
		 */
		ChemostatConnection ccIn = new ChemostatConnection();
		ccIn.setFlowRate(1.0);
		ccIn.setConcentrations(inflowConcn);
		myShape.addOtherBoundary(ccIn);
		ChemostatConnection ccOut = new ChemostatConnection();
		ccOut.setFlowRate(-1.0);
		myShape.addOtherBoundary(ccOut);
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer("Dimensionless");
		/*
		 * Set up the process manager
		 */
		SolveChemostat process = new SolveChemostat();
		//process.aspectRegistry.set("solver", "heun");
		process.init(soluteNames);
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);
		
		System.out.println("Time: "+process.getTimeForNextStep());
		for ( String name : soluteNames )
		{
			System.out.println("\t"+name+": "+
					environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
		}
		for ( ; nStep > 0; nStep-- )
		{
			process.step(environment, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
			{
				System.out.println("\t"+name+": "+
						environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
			}
		}
		System.out.println("\n");
	}
	
	private static void oneChemostatFall(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing chemostat for one solute:");
		System.out.println("\tSin = 0.0");
		System.out.println("\tS0 = 1.0");
		System.out.println("\tD = 1.0");
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should halve each timestep");
		System.out.println("###############################################");
		
		
		String[] soluteNames = new String[1];
		soluteNames[0] = "fall";
		
		HashMap<String, Double> initialConcn = new HashMap<String, Double>();
		initialConcn.put(soluteNames[0], 1.0);
		
		HashMap<String, Double> inflowConcn = new HashMap<String, Double>();
		inflowConcn.put(soluteNames[0], 0.0);
		
		/*
		 * 
		 */
		EnvironmentContainer environment = 
				new EnvironmentContainer(new ShapeLibrary.Dimensionless());
		for ( String name : soluteNames )
			environment.addSolute(name, initialConcn.get(name));
		
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer(new ShapeLibrary.Dimensionless());
		/*
		 * Set up the process manager
		 */
		SolveChemostat process = new SolveChemostat();
		process.init(soluteNames);
		//process.setInflow(inflowConcn);
		//process.setDilution(1.0);
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);
		
		System.out.println("Time: "+process.getTimeForNextStep());
		for ( String name : soluteNames )
			System.out.println("\t"+name+": "+environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
		for ( ; nStep > 0; nStep-- )
		{
			process.step(environment, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
				System.out.println("\t"+name+": "+environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
		}
		System.out.println("\n");
	}
	
	/**
	 * Basic chemostat test:
	 * 
	 * "rise" starts with a concn of 0 and inflow concn of 1
	 * "fall" starts with a concn of 1 and inflow concn of 0
	 * 
	 * Since dS/dt = D(Sin - S), S = Sin + (S0 - Sin)*e^(-Dt)
	 * 
	 * The two solutes should have no effect on each other
	 * 
	 * So for "rise", S = 1 - e^(-t)
	 * and for "fall", S = e^(-t) 
	 */
	private static void twoChemostatBasic(int nStep, double stepSize)
	{
		System.out.println("###############################################");
		System.out.println("Testing chemostat for two solutes:");
		System.out.println("\tSin = 1.0, 0.0");
		System.out.println("\tS0 = 0.0, 1.0");
		System.out.println("\tD = 1.0");
		System.out.println("\tNo agents or reactions");
		System.out.println("Solutes should not interfere with each other");
		System.out.println("###############################################");
		String[] soluteNames = new String[2];
		soluteNames[0] = "rise";
		soluteNames[1] = "fall";
		
		HashMap<String, Double> initialConcn = new HashMap<String, Double>();
		initialConcn.put(soluteNames[0], 0.0);
		initialConcn.put(soluteNames[1], 1.0);
		
		HashMap<String, Double> inflowConcn = new HashMap<String, Double>();
		inflowConcn.put(soluteNames[0], 1.0);
		inflowConcn.put(soluteNames[1], 0.0);
		
		/*
		 * 
		 */
		EnvironmentContainer environment = 
				new EnvironmentContainer(new ShapeLibrary.Dimensionless());
		for ( String name : soluteNames )
			environment.addSolute(name, initialConcn.get(name));
		
		/*
		 * Dummy AgentContainer will be empty
		 */
		AgentContainer agents = new AgentContainer("Dimensionless");
		/*
		 * Set up the process manager
		 */
		SolveChemostat process = new SolveChemostat();
		process.init(soluteNames);
		//process.setInflow(inflowConcn);
		//process.setDilution(1.0);
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);
		
		System.out.println("Time: "+process.getTimeForNextStep());
		for ( String name : soluteNames )
			System.out.println("\t"+name+": "+environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
		for ( ; nStep > 0; nStep-- )
		{
			process.step(environment, agents);
			System.out.println("Time: "+process.getTimeForNextStep());
			for ( String name : soluteNames )
				System.out.println("\t"+name+": "+environment.getSoluteGrid(name).getMax(ArrayType.CONCN));
		}
		System.out.println("\n");
	}
	
	private static void compartment()
	{
		System.out.println("###############################################");
		System.out.println("Testing chemostat for one solute:");
		System.out.println("\tSin = 0.0");
		System.out.println("\tS0 = 1.0");
		System.out.println("\tD = 1.0");
		System.out.println("\tNo agents or reactions");
		System.out.println("Concentration should halve each timestep");
		System.out.println("###############################################");
		Compartment aCompartment = new Compartment("dimensionless");
		
		//aCompartment.addSolute("rise");
		
		SolveChemostat chemoSolver = new SolveChemostat();
		aCompartment.addProcessManager(chemoSolver);
		
		
	}
}