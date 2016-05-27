package testJUnit;

import org.junit.Test;

import boundary.BoundaryLibrary.ChemostatOutflow;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveChemostat;
import shape.ShapeLibrary.Dimensionless;

/**
 * \brief Unit test class to check that a simple chemostat systems behave
 * with regards to solutes and agents.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ChemostatsTest
{
	@Test
	public void singleChemostat()
	{
		double flowRate = 1.0;
		double tStep = 1.0;
		double tMax = 10.0;
		String soluteName = "solute";
		/*
		 * Set up the Simulator and the Timer.
		 */
		AllTests.setupSimulatorForTest(tStep, tMax, "singleChemostat");
		/* Compartment. */
		Compartment comp;
		/* Boundary connections. */
		ChemostatOutflow cOut;
		/*
		 * The waste compartment.
		 */
		comp = Idynomics.simulator.addCompartment("waste");
		comp.setShape("dimensionless");
		comp._environment.addSolute(soluteName);
		/*
		 * 
		 */
		cOut = new ChemostatOutflow();
		cOut.setFlowRate(flowRate);
		cOut.setPartnerCompartment(comp);
		comp = Idynomics.simulator.addCompartment("chemostat");
		Dimensionless shape = new Dimensionless();
		shape.setVolume(1.0);
		shape.addOtherBoundary(cOut);
		comp.setShape(shape);
		comp._environment.addSolute(soluteName);
		SolveChemostat p1 = new SolveChemostat();
		p1.init();
		p1.setTimeStepSize(tStep);
		comp.addProcessManager(p1);
		/*
		 * The feed compartment.
		 */
		cOut = new ChemostatOutflow();
		cOut.setFlowRate(flowRate);
		comp = Idynomics.simulator.addCompartment("feed");
		comp.setShape("dimensionless");
		comp._environment.addSolute(soluteName);
		
	}
	
	
	
	
}
