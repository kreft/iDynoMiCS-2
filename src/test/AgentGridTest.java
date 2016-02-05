/**
 * 
 */
package test;

import boundary.*;
import idynomics.Compartment;
import idynomics.Simulator;
import idynomics.Timer;
import processManager.SolveDiffusionTransient;
import shape.ShapeConventions.DimName;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentGridTest
{
	public static void main(String[] args)
	{
		double tStep = 0.1;
		double endTime = 10 * tStep;
		double topConcn = 1.0;
		/*
		 * Set the timestep and total simulation time.
		 */
		Timer.setTimeStepSize(tStep);
		Timer.setEndOfSimulation(endTime);
		/*
		 * Make a simulator and give it one 9x9 compartment.
		 */
		Simulator aSim = new Simulator();
		Compartment aCompartment = aSim.addCompartment("myCompartment",
																"rectangle");
		aCompartment.setSideLengths(new double[] {9.0, 9.0, 1.0});
		/*
		 * Set the boundary methods.
		 */
		aCompartment.addBoundary(DimName.X, 0, new BoundaryZeroFlux());
		aCompartment.addBoundary(DimName.X, 1, new BoundaryFixed(topConcn));
		aCompartment.addBoundary(DimName.Y, 0, new BoundaryZeroFlux());
		aCompartment.addBoundary(DimName.Y, 1, new BoundaryZeroFlux());
		/*
		 * We just have one solute, but need to give it to the process manager
		 * in an array.
		 */
		String[] soluteNames = new String[] {"solute"};
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		/*
		 * The process manager for solving the diffusion-reaction PDE.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(soluteNames);
		aProcess.setTimeStepSize(tStep);
		aCompartment.addProcessManager(aProcess);
		/*
		 * Launch the simulation.
		 */
		aCompartment.init();
		aSim.launch();
		/*
		 * Print the results.
		 */
		aSim.printAll();
	}
}
