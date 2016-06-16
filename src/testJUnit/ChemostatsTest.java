package testJUnit;

import static org.junit.Assert.assertTrue;
import static testJUnit.AllTests.TOLERANCE;

import org.junit.Test;

import boundary.library.ChemostatToChemostat;
import grid.ArrayType;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveChemostat;
import shape.ShapeLibrary.Dimensionless;
import utility.ExtraMath;

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
		double feedConcn = 1.0;
		/*
		 * Set up the Simulator and the Timer.
		 */
		AllTests.setupSimulatorForTest(tStep, tMax, "singleChemostat");
		/*
		 * The feed compartment.
		 */
		Compartment feed = Idynomics.simulator.addCompartment("feed");
		feed.setShape("dimensionless");
		feed.environment.addSolute(soluteName, feedConcn);
		/*
		 * The main compartment.
		 */
		Compartment chemo = Idynomics.simulator.addCompartment("chemostat");
		Dimensionless shape = new Dimensionless();
		shape.setVolume(1.0);
		chemo.setShape(shape);
		chemo.environment.addSolute(soluteName);
		SolveChemostat p1 = new SolveChemostat();
		p1.setName("SolveChemostat");
		p1.init(new String[]{soluteName});
		p1.setTimeStepSize(tStep);
		chemo.addProcessManager(p1);
		/*
		 * The waste compartment.
		 */
		Compartment waste = Idynomics.simulator.addCompartment("waste");
		waste.setShape("dimensionless");
		waste.environment.addSolute(soluteName);
		/* 
		 * Boundary connection from feed into chemostat.
		 */
		ChemostatToChemostat cIn;
		cIn = new ChemostatToChemostat();
		cIn.setFlowRate(flowRate);
		chemo.addBoundary(cIn);
		cIn.setPartnerCompartment(feed);
		/* 
		 * Boundary connection from chemostat into waste.
		 */
		ChemostatToChemostat cOut = new ChemostatToChemostat();
		cOut.setFlowRate( - flowRate);
		chemo.addBoundary(cOut);
		cOut.setPartnerCompartment(waste);
		/*
		 * Check for the asymptotic increase of solute in the chemostat.
		 */
		double s, t, S;
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			s = chemo.getSolute(soluteName).getAverage(ArrayType.CONCN);
			t = Idynomics.simulator.timer.getCurrentTime();
			S = (1 - Math.exp(-flowRate*t)) * feedConcn;
			assertTrue(ExtraMath.areEqual(s, S, TOLERANCE));
		}
	}
}
