package test.junit.oldTests;

import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import org.junit.Test;

import boundary.library.ConstantConcentrationToChemostat;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Idynomics;
import processManager.library.ChemostatSolver;
import shape.ShapeLibrary.Dimensionless;
import test.OldTests;
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
		OldTests.setupSimulatorForTest(tStep, tMax, "singleChemostat");
		/*
		 * The main compartment.
		 */
		Compartment chemo = Idynomics.simulator.addCompartment("chemostat");
		Dimensionless shape = new Dimensionless();
		shape.setTotalVolume(1.0);
		chemo.setShape(shape);
		chemo.environment.addSolute(new SpatialGrid(soluteName, 0.0, chemo.environment));
		ChemostatSolver p1 = new ChemostatSolver();
		p1.setName("SolveChemostat");
		p1.init(null, chemo.environment, 
				chemo.agents, chemo.getName());
		p1.setTimeStepSize(tStep);
		chemo.addProcessManager(p1);
		/* 
		 * Boundary connection from feed into chemostat.
		 */
		ConstantConcentrationToChemostat cInNew = new ConstantConcentrationToChemostat();
		cInNew.setVolumeFlowRate(flowRate);
		cInNew.setConcentration(soluteName, feedConcn);
		chemo.addBoundary(cInNew);
		/* 
		 * Boundary connection from chemostat into waste.
		 */
		ConstantConcentrationToChemostat cOutNew = new ConstantConcentrationToChemostat();
		cOutNew.setVolumeFlowRate( - flowRate);
		chemo.addBoundary(cOutNew);
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
			Log.out(Tier.DEBUG, "solute is "+s+", should be "+S);
			assertTrue(ExtraMath.areEqual(s, S, TOLERANCE));
		}
	}
}
