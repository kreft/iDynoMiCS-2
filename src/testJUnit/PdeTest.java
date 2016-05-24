package testJUnit;

import org.junit.Test;

import boundary.BoundaryLibrary.SolidBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveDiffusionTransient;
import shape.Shape;
import shape.ShapeConventions.DimName;
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
	public void flattenRandom()
	{
		double tStep = 1.0;
		double tMax = 10.0;
		double compartmentLength = 2.0;
		
		AllTests.setupSimulatorForTest(tStep, tMax, "flattenRandom");
		
		Compartment comp = Idynomics.simulator.addCompartment("oneDim");
		comp.setShape("line");
		//comp.setSideLengths(new double[]{compartmentLength});
		/* Make both boundaries solid. */
		comp.addBoundary(DimName.X, 0, new SolidBoundary());
		comp.addBoundary(DimName.X, 1, new SolidBoundary());
		
		Shape shape = comp.getShape();
		UniformResolution resCalc = new UniformResolution();
		resCalc.setLength(compartmentLength);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		/*
		 * Add the solute and fill it with random values.
		 */
		String soluteName = "solute";
		comp.addSolute(soluteName);
		SpatialGrid sG = comp.getSolute(soluteName);
		for ( int[] c = shape.resetIterator(); 
				shape.isIteratorValid(); 
				shape.iteratorNext() )
		{
			sG.setValueAt(ArrayType.CONCN, c, 2.0 * ExtraMath.getUniRandDbl());
		}
		double average = sG.getAverage(ArrayType.CONCN);
		Log.out(Tier.DEBUG, "Average concn is "+average+" to start");
		
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
		
		double[][][] oldConcns, newConcns;
		
		oldConcns = sG.getArray(ArrayType.CONCN);
		Idynomics.simulator.step();
		newConcns = sG.getArray(ArrayType.CONCN);
		Log.out(Tier.DEBUG, "New average: "+sG.getAverage(ArrayType.CONCN));
	}
}
