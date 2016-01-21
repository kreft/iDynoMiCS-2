package test;

import java.util.LinkedList;
import java.util.List;

import processManager.AgentGrowth;
import processManager.AgentRelaxation;
import processManager.PrepareSoluteGrids;
import processManager.ProcessManager;
import processManager.SolveDiffusionTransient;
import utility.ExtraMath;
import agent.Agent;
import agent.body.Body;
import agent.body.Point;
import agent.event.EventLoader;
import agent.state.StateLoader;
import agent.state.secondary.*;
import boundary.Boundary;
import grid.GridBoundary;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment;
import idynomics.Simulator;
import idynomics.Timer;

public class AgentCompartmentTest {

	public static void main(String[] args) {
		Timer.setTimeStepSize(1.0);
		Timer.setEndOfSimulation(20.0);
		
		Simulator aSim = new Simulator();
		
		Compartment aCompartment = aSim.addCompartment("myCompartment", "Rectangle");
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
			// bndry.setGridMethod("solute", GridBoundary.Cyclic());
			//FIXME: how does this work with the new gridMethods?
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
		aPrep.setTimeForNextStep(0.0);
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
		ezAgent.set("volume", StateLoader.getSecondary("SimpleVolumeState","mass,density"));
		ezAgent.set("radius",  StateLoader.getSecondary("CoccoidRadius","volume"));
		ezAgent.set("growthRate", 0.2);
		ezAgent.set("#isLocated", true);		
		List<Point> pts = new LinkedList<Point>();
		pts.add(new Point(new double[]{1.0, 1.0}));
		ezAgent.set("body", new Body(pts));

		ezAgent.set("joints", StateLoader.getSecondary("JointsState","volume"));
		ezAgent.set("#boundingLower", StateLoader.getSecondary("LowerBoundingBox","body,radius"));
		ezAgent.set("#boundingSides", StateLoader.getSecondary("DimensionsBoundingBox","body,radius"));
		
		ezAgent.set("growth", EventLoader.getEvent("SimpleGrowth","mass,growthRate"));
		ezAgent.set("divide", EventLoader.getEvent("CoccoidDivision","mass,radius,body"));
		
		ezAgent.init();
		aCompartment.addAgent(ezAgent);

		ProcessManager agentRelax = new AgentRelaxation();
		agentRelax.setTimeForNextStep(0.0);
		agentRelax.setTimeStepSize(Timer.getTimeStepSize());
		aCompartment.addProcessManager(agentRelax);
		
		ProcessManager agentGrowth = new AgentGrowth();
		agentGrowth.setTimeForNextStep(0.0);
		agentGrowth.setTimeStepSize(Timer.getTimeStepSize());
		aCompartment.addProcessManager(agentGrowth);

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
