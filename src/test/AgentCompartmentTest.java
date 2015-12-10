package test;

import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;
import processManager.PrepareSoluteGrids;
import processManager.SolveDiffusionTransient;
import utility.ExtraMath;
import agent.Agent;
import agent.body.Body;
import agent.body.Point;
import agent.state.secondary.*;
import boundary.Boundary;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SpatialGrid.GridMethod;
import idynomics.Compartment;
import idynomics.Simulator;
import idynomics.Timer;

public class AgentCompartmentTest {

	public static void main(String[] args) {
		Timer.setTimeStepSize(1.0);
		Timer.setEndOfSimulation(10.0);
		
		Simulator aSim = new Simulator();
		
		Compartment aCompartment = aSim.addCompartment("myCompartment", "rectangle");
		aCompartment.setSideLengths(new double[] {9.0, 9.0, 1.0});
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

		// set 4 periodic boundaries
		for ( String side : new String[] {"xmin", "xmax", "ymin", "ymax"})
		{
			Boundary bndry = new Boundary();
			bndry.setGridMethod("solute", Boundary.cyclic());
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
			sg.setValueAt(ArrayType.CONCN, coords, ExtraMath.getUniRandDbl());
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
		
		Agent ezAgent = new Agent();
		ezAgent.set("mass",0.1);
		ezAgent.set("density", 0.2);
		ezAgent.set("volume",new SimpleVolumeState());
		ezAgent.set("radius", new CoccoidRadius());
		ezAgent.set("isLocated", true);
		ezAgent.set("lowerBoundingBox", new LowerBoundingBox());
		ezAgent.set("dimensionsBoundingBox", new DimensionsBoundingBox());
		
		List<Point> pts = new LinkedList<Point>();
		pts.add(new Point(2));
		ezAgent.set("body", new Body(pts));
		
		aCompartment.addAgent(ezAgent);

		//TODO twoDimIncompleteDomain(nStep, stepSize);
		/*
		 * Launch the simulation.
		 */
		aSim.launch();
		/*
		 * Print the results.
		 */
		aSim.printAll();
	}

}
