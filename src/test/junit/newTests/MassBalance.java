package test.junit.newTests;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import agent.Agent;
import analysis.Counter;
import analysis.FilterLogic;
import analysis.filter.Filter;
import aspect.AspectInterface;
import boundary.Boundary;
import boundary.library.ConstantConcentrationToChemostat;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Tester;
import grid.ArrayType;
import debugTools.Testable;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import surface.*;
import surface.Rod;
import surface.Surface;
import surface.Voxel;

/**
 * \brief: Unit tests for surface intersection.
 * 
 * This test evaluates surface-surface intersection (collision) for scenarios 
 * with a known outcome, this test does NOT test surface to surface distance
 * calculation.
 * 
 * basic intersections that are tested for:
 *  sphere sphere
 *  rod sphere
 *  rod rod
 *  sphere plane
 *  rod plane
 *  voxel sphere (voxel are assumed to be never split be a periodic boundary)
 *  voxel rod
 *  
 * basic intersections that are NOT tested for:
 *  plane plane (planes are infinite so any non-parallel plane should intersect)
 *  voxel voxel (test is not implemented, but would be easy if required)
 *  voxel plane (test is not implemented, but would be easy if required)
 *  
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class MassBalance implements Testable {
	
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}
	
	public void test(TestMode mode)
	{
		/* basic simulator initiation */
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator("protocol/unit-tests/mass_balance.xml");

		Tester.println("mass balance", mode);
		Compartment chemostat = Idynomics.simulator.getCompartment("chemostat");
		Compartment biofilm = Idynomics.simulator.getCompartment("biofilm");
		int i=0;
		
		String sol = "glucose";
		double[] vol = { chemostat.environment.getShape().getTotalVolume(), 
				biofilm.environment.getShape().getTotalVolume() };
		
		double[] flow = new double[2];	
		double[] mflow = new double[2];
		double[] con = new double[2];
		double conInit = 0.0;
		
		double[] dM = new double[3];
		double[] mPre = { chemostat.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[0], 
			biofilm.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[1] };
		
		double dMq = 0.0;
		double dPq = 0.0;
		double dF = 0.0;
		
		Filter agents = FilterLogic.filterFromString("mass", biofilm);
		LinkedList<AspectInterface> subjects = new LinkedList<AspectInterface>();
		for (Agent a : biofilm.agents.getAllAgents())
			subjects.add((AspectInterface) a);
		double biomass = Counter.count(agents, subjects)[0];
		
		System.out.print( "in -\t" +
				"out -\t" +
				"cons =\t" +
				"accu\t" +
				"solver");

		while ( Idynomics.simulator.timer.isRunning() )
		{
			System.out.println("\n # " + i++);
			double[] in = { 0.0, 0.0, 0.0 };
			double[] out = { 0.0, 0.0, 0.0 };
			Idynomics.simulator.step();
			Collection<Boundary> boundaries = chemostat.getShape().getAllBoundaries();
			con[0] = chemostat.environment.getSoluteGrid(sol).getAverage(ArrayType.CONCN);
			con[1] = biofilm.environment.getSoluteGrid(sol).getAverage(ArrayType.CONCN);
			if (conInit == 0.0)
				conInit = con[0];
			
			for( Boundary e : boundaries)
			{
//				System.out.println(e.getClass().getSimpleName());
				flow[0] = e.getVolumeFlowRate();	
				mflow[0] = e.getMassFlowRate(sol);

				if( mflow[0] > 0.0)
				{
					in[0] += mflow[0] * Idynomics.simulator.timer.getTimeStepSize();
					Map<String,Double> cons = 
							((ConstantConcentrationToChemostat) e)._concns;
					in[2] += flow[0] * cons.get(sol) * Idynomics.simulator.timer.getTimeStepSize();
				}
				else
				{
					out[0] -= mflow[0] * Idynomics.simulator.timer.getTimeStepSize();
					out[2] -= flow[0]*con[0] * Idynomics.simulator.timer.getTimeStepSize();
				}
			}
			System.out.println("chemostat: in - out = acc, solved");
			dM[0] = in[0]-out[0];
			System.out.println(
					String.format("%- 10.3f -", in[0]) +
					String.format("%- 20.3f =", out[0]) +
					String.format("%- 10.3f", dM[0]) +
					String.format("f%- 10.3g", (con[0]*vol[0])-mPre[0]));
			
			dM[2] = in[2]-out[2];
			dF += dM[2];
			System.out.println("excluding biofilm diffusion (net change in overall system)");
			System.out.println(
					String.format("%- 10.3g -", in[2]) +
					String.format("%- 20.3g =", out[2]) +
					String.format("%- 20.3g ", dM[2]));

			boundaries = biofilm.getShape().getAllBoundaries();
			
			for( Boundary e : boundaries)
			{
//				System.out.println(e.getClass().getSimpleName());
				flow[1] = e.getVolumeFlowRate();	
				mflow[1] = e.getMassFlowRate(sol);
				
				
				if( mflow[1] > 0.0)
					in[1] += mflow[1] * Idynomics.simulator.timer.getTimeStepSize();
				else
					out[1] -= mflow[1] * Idynomics.simulator.timer.getTimeStepSize();
			}

			subjects = new LinkedList<AspectInterface>();
			for (Agent a : biofilm.agents.getAllAgents())
				subjects.add((AspectInterface) a);
			double dBiomass = -(biomass - Counter.count(agents, subjects)[0]);
			biomass = Counter.count(agents, subjects)[0];
			
			dM[1] = in[1]-out[1]-(2.63*(dBiomass));
			

			System.out.println("biofilm: in - out - consumed = acc, solver");
			System.out.println(
					String.format("%- 10.3f -", in[1]) +
					String.format("%- 10.3f -", out[1]) +
					String.format("%- 8.3f =", (2.63*dBiomass)) +
					String.format("%- 10.3f", dM[1]) +
					String.format("f%- 10.3g", (con[1]*vol[1])-mPre[1]));

			double dMtot = dM[0]+dM[1];
			double mPretot = ((con[0]*vol[0])-mPre[0])+((con[1]*vol[1])-mPre[1]);
			System.out.println("Mass, Mass solved, difference");
			System.out.println( 
					String.format("%- 10.3f ", dMtot) +
					String.format(" %- 10.3f ", mPretot) +
					String.format(" %- 10.3f ", (mPretot-dMtot) ) +
					String.format("%70s", "Delta Step") );
			
			dMq += dMtot;
			dPq += mPretot;
			
			System.out.println( 
					String.format("%- 10.3f ", dMq) +
					String.format(" %- 10.3f ", dPq) +
					String.format(" %- 10.3f ", (dPq-dMq) ) +
					String.format("%70s", "Delta Cummulative")  );
			mPre[0] = con[0]*vol[0];
			mPre[1] = con[1]*vol[1];
		}
		
		biomass = Counter.count(agents, subjects)[0];

		System.out.println( "\n delta gluc from start (total mass): " + (((con[0]-conInit) * vol[0]) + ((con[1]-conInit) * vol[1])));
		System.out.println( "\n delta gluc from start tracked cumalative: " + dPq);
		System.out.println( "\n final substrate converted to biomass sim: " + 2.63*(biomass-0.4));
		System.out.println( "\n delta mass chemostat in/out flows: " + dF);
		System.out.println( "\n total mass gained/lossed by solver: " + (dPq + 2.63*(biomass-0.4) + dF));
		
		/* the mass balance should close, but a small error can be permitted
		 * eg. < 1% of the biomass 
		 * 
		 * dPq: total increase/decrease of glucose in system
		 * 2.63*(biomass-0.4): amount of glucose converted to biomass (0.4 was initial biomass)
		 * dF: net glucose added/removed from chemostat by in and outflows
		 * */
		assertEquals(0.0, (dPq + 2.63*(biomass-0.4) + dF), biomass*0.01);
	}

}
