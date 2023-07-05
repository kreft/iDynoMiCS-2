package test.other;

import static grid.ArrayType.DIFFUSIVITY;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import boundary.spatialLibrary.FixedBoundary;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.Idynomics;
import idynomics.Simulator;
import processManager.library.SolveDiffusionTransient;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.UniformResolution;
import test.OldTests;

public class PDEBenchmark {

	public static void main(String[] args) {
		try {
			cube();
			cylindrical();
			spherical();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void cube() throws IOException{
		/*
		 * Simulation parameters.
		 */
		Idynomics.global.outputRoot = "./Benchmarks";
		Idynomics.global.simulationName = "BenchmarkCartesianShape";
		Idynomics.global.updateSettings();
		Log.set(Tier.NORMAL);
		Log.setupFile();
		BufferedWriter bw = null;
		bw = new BufferedWriter(
				new FileWriter(Idynomics.global.outputRoot+"/rOutputCube.csv"));
		bw.write("# format: nVoxel, time taken, ministep size ");
		bw.newLine();
		for (double nVoxelX = 1; nVoxelX < 210; nVoxelX += 10){
			/* let the shape calculate the maximal time step */
			double tStep = 1000;
			double tMax = 10.0;
			long start = System.currentTimeMillis();
			String soluteName = "solute";
			/* Set up the simulator */
			Idynomics.simulator = new Simulator();
			Idynomics.simulator.timer.setTimeStepSize(tStep);
			Idynomics.simulator.timer.setEndOfSimulation(tMax);

			/*
			 * Set up the simulation with a single compartment: a cylinder, with
			 * solid rmin and z and fixed rmax boundary, theta cyclic.
			 */
			Compartment comp = Idynomics.simulator.addCompartment("cuboid");
			Shape shape = OldTests.GetShape("Cuboid");
			comp.setShape(shape);
			Dimension x = shape.getDimension(DimName.X);
			x.setLength(nVoxelX);
			FixedBoundary xMax = new FixedBoundary();
			xMax.instantiate(OldTests.getSpatialBoundaryElement(1), x);
			xMax.setConcentration("solute", 2.0);
			comp.addBoundary(xMax);
			UniformResolution resCalc = new UniformResolution(x);
			resCalc.setResolution(1.0);
			shape.setDimensionResolution(DimName.X, resCalc);
			Dimension y = shape.getDimension(DimName.Y);
			y.setLength(10.0);
			resCalc = new UniformResolution(y);
			resCalc.setResolution(1.0);
			shape.setDimensionResolution(DimName.Y, resCalc);
			Dimension z = shape.getDimension(DimName.Z);
			z.setLength(10.0);
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
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to setup the simulator");
			
			/*
			 * Run the simulation */
			int voxel_counter = 0;
			while ( Idynomics.simulator.timer.isRunning() )
			{
				Idynomics.simulator.step();
				voxel_counter = 0;
				for ( shape.resetIterator();
						shape.isIteratorValid(); shape.iteratorNext() )
				{
					voxel_counter++;
				}
			}
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to solve a "+voxel_counter+" voxel "
							+ "cuboid grid for "+tMax+" timesteps");
			/* maximal time step estimation copied from PDEExplicit.solve() */
			double inverseMaxT = sG.getMax(DIFFUSIVITY);
			inverseMaxT *= shape.getMaxFluxPotential();
			inverseMaxT *= shape.getNumberOfDimensions();
			double dt =  Math.min(tStep, 1 / inverseMaxT);
			bw.write(voxel_counter+", "
							+ (System.currentTimeMillis() - start) +", "
							+ dt);
			bw.newLine();
		}
		bw.close();
	}


	public static void cylindrical() throws IOException{
		/*
		 * Simulation parameters.
		 */
		Idynomics.global.outputRoot = "./Benchmarks";
		Idynomics.global.simulationName = "BenchmarkCylindricalShape";
		Idynomics.global.updateSettings();
		Log.set(Tier.NORMAL);
		Log.setupFile();
		BufferedWriter bw = null;
		bw = new BufferedWriter(
				new FileWriter(Idynomics.global.outputRoot+"/rOutputCylinder.csv"));
		bw.write("# format: nVoxel, time taken, ministep size ");
		bw.newLine();
		for (double nVoxelR = 1; nVoxelR < 25; nVoxelR+=2){
			/* let the shape calculate the maximal time step */
			double tStep = 10;
			double tMax = 10.0;
			long start = System.currentTimeMillis();
			String soluteName = "solute";
			/* Set up the simulator */
			Idynomics.simulator = new Simulator();
			
			Idynomics.simulator.timer.setTimeStepSize(tStep);
			Idynomics.simulator.timer.setEndOfSimulation(tMax);

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
			theta.setLength(2 * Math.PI);
			resCalc = new UniformResolution(theta);
			resCalc.setResolution(1.0);
			shape.setDimensionResolution(DimName.THETA, resCalc);
			Dimension z = shape.getDimension(DimName.Z);
			z.setLength(10.0);
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
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to setup the simulator");
			
			/*
			 * Run the simulation */
			int voxel_counter = 0;
			while ( Idynomics.simulator.timer.isRunning() )
			{
				Idynomics.simulator.step();
				voxel_counter = 0;
				for ( shape.resetIterator();
						shape.isIteratorValid(); shape.iteratorNext() )
				{
					voxel_counter++;
				}
			}
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to solve a "+voxel_counter+" voxel "
							+ "cylindrical grid for "+tMax+" timesteps");
			/* maximal time step estimation copied from PDEExplicit.solve() */
			double inverseMaxT = sG.getMax(DIFFUSIVITY);
			inverseMaxT *= shape.getMaxFluxPotential();
			inverseMaxT *= shape.getNumberOfDimensions();
			double dt =  Math.min(tStep, 1 / inverseMaxT);
			bw.write(voxel_counter+", "
							+ (System.currentTimeMillis() - start) +", "
							+ dt);
			bw.newLine();
		}
		bw.close();
	}
	
	public static void spherical() throws IOException{
		/*
		 * Simulation parameters.
		 */
		Idynomics.global.outputRoot = "./Benchmarks";
		Idynomics.global.simulationName = "BenchmarkSphericalShape";
		Idynomics.global.updateSettings();
		Log.set(Tier.NORMAL);
		Log.setupFile();
		BufferedWriter bw = null;
		bw = new BufferedWriter(
				new FileWriter(Idynomics.global.outputRoot+"/rOutputSphere.csv"));
		bw.write("# format: nVoxel, time taken, ministep size ");
		bw.newLine();
		for (double nVoxelR = 1; nVoxelR < 20; nVoxelR+=2){
			/* let the shape calculate the maximal time step */
			double tStep = 10;
			double tMax = 10.0;
			long start = System.currentTimeMillis();
			String soluteName = "solute";
			/* Set up the simulator */
			Idynomics.simulator = new Simulator();
			
			Idynomics.simulator.timer.setTimeStepSize(tStep);
			Idynomics.simulator.timer.setEndOfSimulation(tMax);

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
			phi.setLength(Math.PI);
			resCalc = new UniformResolution(phi);
			resCalc.setResolution(1.0);
			shape.setDimensionResolution(DimName.PHI, resCalc);
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
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to setup the simulator");
			
			/*
			 * Run the simulation */
			int voxel_counter = 0;
			while ( Idynomics.simulator.timer.isRunning() )
			{
				Idynomics.simulator.step();
				voxel_counter = 0;
				for ( shape.resetIterator();
						shape.isIteratorValid(); shape.iteratorNext() )
				{
					voxel_counter++;
				}
			}
			
			Log.out(Tier.NORMAL, "took "+ (System.currentTimeMillis() - start)
					+ " milliseconds to solve a "+voxel_counter+" voxel "
							+ "spherical grid for "+tMax+" timesteps");
			/* maximal time step estimation copied from PDEExplicit.solve() */
			double inverseMaxT = sG.getMax(DIFFUSIVITY);
			inverseMaxT *= shape.getMaxFluxPotential();
			inverseMaxT *= shape.getNumberOfDimensions();
			double dt =  Math.min(tStep, 1 / inverseMaxT);
			bw.write(voxel_counter+", "
							+ (System.currentTimeMillis() - start) +", "
							+ dt);
			bw.newLine();
		}
		bw.close();
	}
}
