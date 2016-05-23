package test;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Simulator;
import processManager.ProcessManager;
import processManager.library.AgentGrowth;
import processManager.library.AgentRelaxation;
import processManager.library.RefreshMassGrids;
import processManager.library.SolveDiffusionTransient;
import shape.Shape;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

public class AgentCompartmentTest
{
	public static void main(String[] args)
	{
		Idynomics.simulator = new Simulator();
		Idynomics.simulator.timer.setTimeStepSize(1.0);
		Idynomics.simulator.timer.setEndOfSimulation(25.0);
		Log.set(Tier.DEBUG);
		Idynomics.global.simulationName = "test";
		Idynomics.global.outputLocation = "../results/test";
		
		Compartment aCompartment = 
					Idynomics.simulator.addCompartment("myCompartment");
		Shape aShape = (Shape) Shape.getNewInstance("rectangle");
		aShape.setDimensionLengths(new double[] {9.0, 9.0, 1.0});
		aCompartment.setShape(aShape);
		/*
		 * Set the boundary methods and initialise the compartment.
		 */
		// set 4 periodic boundaries
		for ( DimName dim : new DimName[]{DimName.X, DimName.Y} )
			aCompartment.getShape().getDimension(dim).setCyclic();
		
		/*
		 * 
		 */
		String[] soluteNames = new String[2];
		soluteNames[0] = "solute";
		soluteNames[1] = "biomass";
		for ( String aSoluteName : soluteNames )
			aCompartment.addSolute(aSoluteName);
		
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
		
		SpatialGrid bm = aCompartment.getSolute("biomass");
		for ( int[] coords = bm.resetIterator() ; bm.isIteratorValid();
												coords = bm.iteratorNext() )
		{
			bm.setValueAt(ArrayType.CONCN, coords, 0.0);
		}
		/*
		 * Set up the transient diffusion-reaction solver.
		 */
		SolveDiffusionTransient aProcess = new SolveDiffusionTransient();
		aProcess.init(new String[]{"solute"});
		aProcess.setTimeForNextStep(0.0);
		aProcess.setTimeStepSize(Idynomics.simulator.timer.getTimeStepSize());
		aCompartment.addProcessManager(aProcess);
		
		Agent ezAgent = new Agent();
		ezAgent.set("mass",0.1);
		ezAgent.set("density", 0.2);
		
		ProcessManager agentMassGrid = new RefreshMassGrids();
		agentMassGrid.setTimeForNextStep(0.0);
		agentMassGrid.setTimeStepSize(Idynomics.simulator.timer.getTimeStepSize());
		aCompartment.addProcessManager(agentMassGrid);
		
		ezAgent.init();
		aCompartment.addAgent(ezAgent);


		ProcessManager agentGrowth = new AgentGrowth();
		agentGrowth.setName("agentGrowth");
		//agentGrowth.debugMode();
		agentGrowth.setPriority(0);
		agentGrowth.setTimeForNextStep(0.0);
		agentGrowth.setTimeStepSize(Idynomics.simulator.timer.getTimeStepSize());
		aCompartment.addProcessManager(agentGrowth);
		
		ProcessManager agentRelax = new AgentRelaxation();
		agentRelax.setName("agentRelax");
		agentRelax.setPriority(1);
		agentRelax.setTimeForNextStep(0.0);
		agentRelax.setTimeStepSize(Idynomics.simulator.timer.getTimeStepSize());
		aCompartment.addProcessManager(agentRelax);
		

		//TODO twoDimIncompleteDomain(nStep, stepSize);
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
