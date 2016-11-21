package testJUnit;

import org.junit.Test;

import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveDiffusionSteadyState;
import shape.Shape;
import shape.Dimension.DimName;
import shape.resolution.ResolutionCalculator.UniformResolution;

public class PDEgaussseidelTests
{
	@Test
	public void gaussseidelConvergesForSimpleLine()
	{
		/*
		 * Simulation parameters.
		 */
		double tStep = 1.0;
		double tMax = 5.0;
		int nVoxel = 4;
		String soluteName = "solute";
		/* Set up the simulator and log output. */
		AllTests.setupSimulatorForTest(tStep, tMax, 
					"gaussseidelConvergesForSimpleLine");
		/*
		 * Set up the simulation with a single compartment: a line with
		 * periodic boundaries.
		 */
		Compartment comp = Idynomics.simulator.addCompartment("line");
		comp.setShape("line");
		Shape shape = comp.getShape();
		shape.getDimension(DimName.X).setLength(nVoxel);
		UniformResolution resCalc = new UniformResolution();
		resCalc.setExtremes(0, nVoxel);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		//shape.makeCyclic(DimName.X);
		/* Add the solute and diffusivity */
		comp.environment.addSolute(new SpatialGrid(soluteName, 0.0, comp.environment));
		SpatialGrid sg = comp.getSolute(soluteName);
		for ( int i = 0; i < nVoxel; i++ )
		{
			sg.setValueAt(ArrayType.CONCN, new int[] {i, 0, 0}, i / (1.0*nVoxel));
		}
		/*
		 * Set up the diffusion solver.
		 */
		SolveDiffusionSteadyState pm = new SolveDiffusionSteadyState();
		pm.setName("DR solver");
		pm.init(new String[]{soluteName}, comp.environment, 
				comp.agents, comp.getName());
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(tStep);
		pm.setPriority(1);
		comp.addProcessManager(pm);
		
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
		}
	}
}
