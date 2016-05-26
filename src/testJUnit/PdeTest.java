package testJUnit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import static testJUnit.AllTests.TOLERANCE;

import boundary.BoundaryLibrary.SolidBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveDiffusionTransient;
import shape.Shape;
import shape.Dimension.DimName;
import shape.resolution.ResolutionCalculator.UniformResolution;
import utility.ExtraMath;

/**
 * \brief Test checking that the Partial Differential Equation (PDE) solvers
 * behave as they should.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PdeTest
{
	@Test
	public void checkMassBalanceWithoutReactions()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 0.1;
		double tMax = 1.0;
		int nVoxel = 3;
		/*
		 * Set up the simulation with a single compartment: a line, with both
		 * boundaries solid.
		 */
		AllTests.setupSimulatorForTest(tStep, tMax, "checkMassBalance");
		Compartment comp = Idynomics.simulator.addCompartment("oneDim");
		comp.setShape("line");
		comp.addBoundary(DimName.X, 0, new SolidBoundary());
		comp.addBoundary(DimName.X, 1, new SolidBoundary());
		Shape shape = comp.getShape();
		UniformResolution resCalc = new UniformResolution();
		resCalc.setLength(1.0 * nVoxel);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		/* Add the solute and fill it with random values. */
		String soluteName = "solute";
		comp.addSolute(soluteName);
		SpatialGrid sG = comp.getSolute(soluteName);
		double concn = 0.0;
		for ( int[] c = shape.resetIterator(); 
				shape.isIteratorValid(); 
				shape.iteratorNext() )
		{
			//sG.setValueAt(ArrayType.CONCN, c, 2.0 * ExtraMath.getUniRandDbl());
			sG.setValueAt(ArrayType.CONCN, c, concn);
			//concn += 1.5;
			concn += ExtraMath.getUniRandDbl();
		}
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionTransient pm = new SolveDiffusionTransient();
		pm.setName("DR solver");
		pm.init(new String[]{soluteName});
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		/*
		 * Run the simulation, checking at each time step that there is as much
		 * solute after as there was before (mass balance).
		 */
		double oldTotal = sG.getTotal(ArrayType.CONCN);
		double newTotal;
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			newTotal = sG.getTotal(ArrayType.CONCN);
			Log.out(Tier.NORMAL, "Total solute was "+oldTotal+" before, "+newTotal+" after");
			assertTrue(ExtraMath.areEqual(oldTotal, newTotal, TOLERANCE));
			oldTotal = newTotal;
		}
		// FIXME this unit test currently passes when compartmentLength = 2.0,
		// but fails when compartmentLength = 3.0;
	}
}
