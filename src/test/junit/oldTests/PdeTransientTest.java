package test.junit.oldTests;

import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import java.util.Arrays;

import org.junit.Test;

import boundary.spatialLibrary.FixedBoundary;
import boundary.spatialLibrary.SolidBoundary;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import processManager.library.SolveDiffusionTransient;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.UniformResolution;
import test.OldTests;
import utility.ExtraMath;

/**
 * \brief Test checking that the Partial Differential Equation (PDE) solvers
 * behave as they should.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class PdeTransientTest
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
		OldTests.setupSimulatorForTest(tStep, tMax, "checkMassBalance");
		Compartment comp = Idynomics.simulator.addCompartment("oneDim");
		Shape shape = OldTests.GetShape("Line");
		comp.setShape(shape);
		Dimension x = shape.getDimension(DimName.X);
		x.setLength(nVoxel);
		for ( int extreme = 0; extreme < 2; extreme++ )
		{
			SolidBoundary sB = new SolidBoundary();
			sB.setParent(x);
			sB.setExtreme(extreme);
			comp.addBoundary(sB);
		}
		UniformResolution resCalc = new UniformResolution(x);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		/* Add the solute and fill it with random values. */
		String soluteName = "solute";
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
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
		pm.init(null, comp.environment, comp.agents, comp.getName());
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
	
	@Test
	public void checkDiffusionIndifferentForRadiusInCircle()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 0.1;
		double tMax = 1.0;
		int nVoxelR = 3;
		String soluteName = "solute";
		/* Set up the simulator and log output. */
		OldTests.setupSimulatorForTest(tStep, tMax,
				"checkDiffusionIndifferentForRadiusInCircle");
		/*
		 * Set up the simulation with a single compartment: a circle, with
		 * solid rmin and fixed rmax boundary, theta cyclic.
		 */
		Compartment comp = Idynomics.simulator.addCompartment("circle");
		Shape shape = OldTests.GetShape("Circle");
		comp.setShape(shape);
		Dimension r = shape.getDimension(DimName.R);
		FixedBoundary rMax = new FixedBoundary();
		rMax.instantiate(OldTests.getSpatialBoundaryElement(1), r);
		rMax.setConcentration("solute", 2.0);
		comp.addBoundary(rMax);
		Dimension radial = shape.getDimension(DimName.R);
		radial.setLength(nVoxelR);
		UniformResolution resCalc = new UniformResolution(radial);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.R, resCalc);
		Dimension theta = shape.getDimension(DimName.THETA);
		theta.setLength(2 * Math.PI);
		resCalc = new UniformResolution(theta);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.THETA, resCalc);
		/* Add the solute (will be initialised with zero concn). */
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
		SpatialGrid sG = comp.getSolute(soluteName);
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionTransient pm = new SolveDiffusionTransient();
		pm.setName("DR solver");
		pm.init(null, comp.environment, 
				comp.agents, comp.getName());
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		/*
		 * Run the simulation, checking at each time step that all voxels in a 
		 * ring have the same concentration.
		 */
		double last_concn = Double.NaN, cur_concn;
		double[] conc_diff = new double[nVoxelR];
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			int ringIndex = -1;
			for ( shape.resetIterator();
					shape.isIteratorValid(); shape.iteratorNext() )
			{
				if ( shape.iteratorCurrent()[0] != ringIndex )
				{
					ringIndex = shape.iteratorCurrent()[0];
					last_concn = sG.getValueAtCurrent(ArrayType.CONCN);
				}
				cur_concn = sG.getValueAtCurrent(ArrayType.CONCN);
				conc_diff[ringIndex] += Math.abs(last_concn - cur_concn);
				last_concn = cur_concn;
			}
			Log.out(Tier.DEBUG, "Differences along radii for step " 
						+ Idynomics.simulator.timer.getCurrentTime()+": "
											+ Arrays.toString(conc_diff));
			assertTrue(ExtraMath.areEqual(Vector.sum(conc_diff),
					0, TOLERANCE));
		}
	}
	
	@Test
	public void checkDiffusionReachesEquilibriumInCylinder()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 0.1;
		double tMax = 100.0;
		int nVoxelR = 7;
		String soluteName = "solute";
		/* Set up the simulator and log output. */
		OldTests.setupSimulatorForTest(tStep, tMax,
				"checkDiffusionReachesEquilibriumInCylinder");
		/*
		 * Set up the simulation with a single compartment: a cylinder, with
		 * solid rmin and z and fixed rmax boundary, theta cyclic.
		 */
		Compartment comp = Idynomics.simulator.addCompartment("cylinder");
		Shape shape = OldTests.GetShape("Cylinder");
		comp.setShape(shape);
		Dimension r = shape.getDimension(DimName.R);
		FixedBoundary rMax = new FixedBoundary();
		rMax.instantiate(OldTests.getSpatialBoundaryElement(1), r);
		rMax.setConcentration("solute", 2.0);
		comp.addBoundary(rMax);
		Dimension radial = shape.getDimension(DimName.R);
		radial.setLength(nVoxelR);
		UniformResolution resCalc = new UniformResolution(radial);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.R, resCalc);
		Dimension theta = shape.getDimension(DimName.THETA);
		theta.setLength((2.0/3.0) * Math.PI); 
		resCalc = new UniformResolution(theta);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.THETA, resCalc);
		Dimension z = shape.getDimension(DimName.Z);
		z.setLength(3.0);
		resCalc = new UniformResolution(z);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.Z, resCalc);
		/* Add the solute (will be initialised with zero concn). */
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
		SpatialGrid sG = comp.getSolute(soluteName);
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionTransient pm = new SolveDiffusionTransient();
		pm.setName("DR solver");
		pm.init(null, comp.environment, 
				comp.agents, comp.getName());
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		/*
		 * Run the simulation, computing the mean concentration for
		 * each time step, stop if mean concentration is equal for  
		 * two time steps or after 100 time steps. 
		 */
		double mean_conc = 0, conc_diff = 0, last_conc = 0;
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			int voxel_counter = 0;
			mean_conc = 0;
			for ( shape.resetIterator();
					shape.isIteratorValid(); shape.iteratorNext() )
			{
				mean_conc += sG.getValueAtCurrent(ArrayType.CONCN);
				voxel_counter++;
			}
			mean_conc /= voxel_counter - 1;
			conc_diff = Math.abs(last_conc - mean_conc);
			last_conc = mean_conc;
			Log.out(Tier.DEBUG, "Mean concentration for time step " 
					+ Idynomics.simulator.timer.getCurrentTime()+": "
					+ mean_conc + " has difference "+conc_diff
					+" to last mean concentration");
			if (ExtraMath.areEqual(conc_diff, 0, TOLERANCE)){
				Log.out(Tier.DEBUG, "reached equilibrium at time step "
						+ Idynomics.simulator.timer.getCurrentTime()
						+" with mean concentration " + mean_conc);
				break;
			}
		}
		assertTrue(mean_conc > 0 && ExtraMath.areEqual(conc_diff, 0, TOLERANCE));
	}
	
	@Test
	public void checkDiffusionReachesEquilibriumInSphere()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 0.1;
		double tMax = 100;
		int nVoxelR = 7;
		String soluteName = "solute";
		/* Set up the simulator and log output. */
		OldTests.setupSimulatorForTest(tStep, tMax,
				"checkDiffusionReachesEquilibriumInSphere");
		/*
		 * Set up the simulation with a single compartment: a cylinder, with
		 * solid rmin and z and fixed rmax boundary, theta cyclic.
		 */
		Compartment comp = Idynomics.simulator.addCompartment("sphere");
		Shape shape = OldTests.GetShape("Sphere");
		comp.setShape(shape);
		Dimension r = shape.getDimension(DimName.R);
		FixedBoundary rMax = new FixedBoundary();
		rMax.instantiate(OldTests.getSpatialBoundaryElement(1), r);
		rMax.setConcentration("solute", 2.0);
		comp.addBoundary(rMax);
		Dimension radial = shape.getDimension(DimName.R);
		radial.setLength(nVoxelR);
		UniformResolution resCalc = new UniformResolution(radial);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.R, resCalc);
		Dimension phi = shape.getDimension(DimName.PHI);
		phi.setLength(0.5 * Math.PI);
		resCalc = new UniformResolution(phi);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.PHI, resCalc);
		Dimension theta = shape.getDimension(DimName.THETA);
		theta.setLength(0.5 * Math.PI);
		resCalc = new UniformResolution(theta);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.THETA, resCalc);
		/* Add the solute (will be initialised with zero concn). */
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
		SpatialGrid sG = comp.getSolute(soluteName);
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionTransient pm = new SolveDiffusionTransient();
		pm.setName("DR solver");
		pm.init(null, comp.environment, 
				comp.agents, comp.getName());
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		/*
		 * Run the simulation, computing the mean concentration for
		 * each time step, stop if mean concentration is equal for  
		 * two time steps or max time step reached. 
		 */
		double mean_conc = 0, conc_diff = 0, last_conc = 0;
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			int voxel_counter = 0;
			mean_conc = 0;
			for ( shape.resetIterator();
					shape.isIteratorValid(); shape.iteratorNext() )
			{
				mean_conc += sG.getValueAtCurrent(ArrayType.CONCN);
				voxel_counter++;
			}
			mean_conc /= voxel_counter - 1;
			conc_diff = Math.abs(last_conc - mean_conc);
			last_conc = mean_conc;
			Log.out(Tier.DEBUG, "Mean concentration for time step " 
					+ Idynomics.simulator.timer.getCurrentTime()+": "
					+ mean_conc + " has difference "+conc_diff
					+" to last mean concentration");
			if (ExtraMath.areEqual(conc_diff, 0, TOLERANCE)){
				Log.out(Tier.DEBUG, "reached equilibrium at time step "
						+ Idynomics.simulator.timer.getCurrentTime()
						+" with mean concentration " + mean_conc);
				break;
			}
		}
		assertTrue(mean_conc > 0 && ExtraMath.areEqual(conc_diff, 0, TOLERANCE));
	}
	
	@Test
	public void checkDiffusionIndifferentForRadiusInSphere()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 0.1;
		double tMax = 1.0;
		int nVoxelR = 10;
		String soluteName = "solute";
		/* Set up the simulator and log output. */
		OldTests.setupSimulatorForTest(tStep, tMax,
				"checkDiffusionIndifferentForRadiusInSphere");
		/*
		 * Set up the simulation with a single compartment: a cylinder, with
		 * solid rmin and z and fixed rmax boundary, theta cyclic.
		 */
		Compartment comp = Idynomics.simulator.addCompartment("sphere");
		Shape shape = OldTests.GetShape("Sphere");
		comp.setShape(shape);
		Dimension r = shape.getDimension(DimName.R);
		FixedBoundary rMax = new FixedBoundary();
		rMax.instantiate(OldTests.getSpatialBoundaryElement(1), r);
		rMax.setConcentration("solute", 2.0);
		comp.addBoundary(rMax);
		Dimension radial = shape.getDimension(DimName.R);
		radial.setLength(nVoxelR);
		UniformResolution resCalc = new UniformResolution(radial);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.R, resCalc);
		Dimension phi = shape.getDimension(DimName.PHI);
		phi.setLength(0.5 * Math.PI);
		resCalc = new UniformResolution(phi);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.PHI, resCalc);
		Dimension theta = shape.getDimension(DimName.THETA);
		theta.setLength(0.5 * Math.PI);
		resCalc = new UniformResolution(theta);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.THETA, resCalc);
		/* Add the solute (will be initialised with zero concn). */
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
		SpatialGrid sG = comp.getSolute(soluteName);
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionTransient pm = new SolveDiffusionTransient();
		pm.setName("DR solver");
		pm.init(null, comp.environment, 
				comp.agents, comp.getName());
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		/*
		 * Run the simulation, checking at each time step that all voxels in a 
		 * ring have the same concentration.
		 */
		double last_concn = Double.NaN, cur_concn;
		double[] conc_diff = new double[nVoxelR];
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			int ringIndex = -1;
			for ( shape.resetIterator();
					shape.isIteratorValid(); shape.iteratorNext() )
			{
				if ( shape.iteratorCurrent()[0] != ringIndex )
				{
					ringIndex = shape.iteratorCurrent()[0];
					last_concn = sG.getValueAtCurrent(ArrayType.CONCN);
				}
				cur_concn = sG.getValueAtCurrent(ArrayType.CONCN);
				conc_diff[ringIndex] += Math.abs(last_concn - cur_concn);
				last_concn = cur_concn;
			}
			Log.out(Tier.DEBUG, "Differences along radii for step " 
						+ Idynomics.simulator.timer.getCurrentTime()+": "
											+ Arrays.toString(conc_diff));
			assertTrue(ExtraMath.areEqual(Vector.sum(conc_diff),
					0, TOLERANCE));
		}
	}
}