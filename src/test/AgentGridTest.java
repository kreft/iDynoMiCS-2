/**
 * 
 */
package test;

import boundary.spatialLibrary.FixedBoundary;
import boundary.spatialLibrary.SolidBoundary;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Simulator;
import processManager.library.SolveDiffusionTransient;
import shape.Shape;
import shape.Dimension.DimName;

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
		 * Make a simulator, set the timestep and total simulation time, and
		 * give it one 9x9 compartment.
		 */
		Idynomics.simulator = new Simulator();
		Idynomics.simulator.timer.setTimeStepSize(tStep);
		Idynomics.simulator.timer.setEndOfSimulation(endTime);
		Compartment aCompartment = 
					Idynomics.simulator.addCompartment("myCompartment");
		Shape aShape = (Shape) Shape.getNewInstance("rectangle");
		aShape.setDimensionLengths(new double[] {9.0, 9.0, 1.0});
		aCompartment.setShape(aShape);
		/*
		 * Set the boundary methods.
		 */
		aCompartment.addBoundary(DimName.X, 0, new SolidBoundary(DimName.X, 0));
		aCompartment.addBoundary(DimName.Y, 0, new SolidBoundary(DimName.Y, 0));
		aCompartment.addBoundary(DimName.Y, 1, new SolidBoundary(DimName.Y, 1));
		FixedBoundary top = new FixedBoundary(DimName.X, 1);
		top.setConcentration("solute", topConcn);
		aCompartment.addBoundary(DimName.X, 1, top);
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
		Idynomics.simulator.run();
		/*
		 * Print the results.
		 */
		Idynomics.simulator.printAll();
	}
}
