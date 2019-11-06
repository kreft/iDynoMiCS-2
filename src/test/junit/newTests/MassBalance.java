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
		
		double[] dM = new double[3];
		double[] mPre = { chemostat.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[0], 
			biofilm.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[1] };
		
		double dMq = 0.0;
		double dPq = 0.0;
		
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
			
			for( Boundary e : boundaries)
			{
//				System.out.println(e.getClass().getSimpleName());
				flow[0] = e.getVolumeFlowRate();	
				mflow[0] = e.getMassFlowRate(sol);

				if( mflow[0] > 0.0)
				{
					in[0] += mflow[0];
					Map<String,Double> cons = 
							((ConstantConcentrationToChemostat) e)._concns;
					in[2] += flow[0] * cons.get(sol);
				}
				else
				{
					out[0] -= mflow[0];
					out[2] -= flow[0]*con[0];
				}
			}
			
			dM[0] = in[0]-out[0];
			System.out.println(
					String.format("%- 10.2f -", in[0]) +
					String.format("%- 20.2f =", out[0]) +
					String.format("%- 10.2f", dM[0]) +
					String.format("f%- 10.2g", (con[0]*vol[0])-mPre[0]) +
					String.format("%50s", "chemostat mass flows, f: deltaBackCalc"));
			
			dM[2] = in[2]-out[2];
			System.out.println(
					String.format("%- 10.2g -", in[2]) +
					String.format("%- 20.2g =", out[2]) +
					String.format("%- 20.2g ", dM[2]) +
					String.format("%50s", "chemostat volume flows * concentration"));

			boundaries = biofilm.getShape().getAllBoundaries();
			
			for( Boundary e : boundaries)
			{
//				System.out.println(e.getClass().getSimpleName());
				flow[1] = e.getVolumeFlowRate();	
				mflow[1] = e.getMassFlowRate(sol);
				
				
				if( mflow[1] > 0.0)
					in[1] += mflow[1];
				else
					out[1] -= mflow[1];
			}

			subjects = new LinkedList<AspectInterface>();
			for (Agent a : biofilm.agents.getAllAgents())
				subjects.add((AspectInterface) a);
			double dBiomass = -(biomass - Counter.count(agents, subjects)[0]);
			biomass = Counter.count(agents, subjects)[0];
			
			dM[1] = in[1]-out[1]-(2.63*dBiomass);
			System.out.println(
					String.format("%- 10.2f -", in[1]) +
					String.format("%- 10.2f -", out[1]) +
					String.format("%- 8.2f =", (2.63*dBiomass)) +
					String.format("%- 10.2f", dM[1]) +
					String.format("f%- 10.2g", (con[1]*vol[1])-mPre[1]) +
					String.format("%50s", "chemostat mass flows, : deltaBackCalc"));

			double dMtot = dM[0]+dM[1];
			double mPretot = ((con[0]*vol[0])-mPre[0])+((con[1]*vol[1])-mPre[1]);
			System.out.println( 
					String.format("%- 10.2f ", dMtot) +
					String.format(" %- 10.2f ", mPretot) +
					String.format(" %- 10.2f ", (mPretot-dMtot) ) +
					String.format("%70s", "deltaTot, deltaBackCalcTot, difference") );
			
			dMq += dMtot;
			dPq += mPretot;
			
			System.out.println( 
					String.format("%- 10.2f ", dMq) +
					String.format(" %- 10.2f ", dPq) +
					String.format(" %- 10.2f ", (dPq-dMq) ) +
					String.format("%70s", "Cummulative deltas")  );
			mPre[0] = con[0]*vol[0];
			mPre[1] = con[1]*vol[1];
		}
		
		biomass = Counter.count(agents, subjects)[0];
		System.out.println( "\ntotal mass gained/lossed by solver: " + dPq);
		System.out.println( "\nfinal biomass sim: " + biomass);
		
		/* the mass balance should close, but a small error can be permitted
		 * eg. < 1% of the biomass */
		assertEquals(0.0, dPq, biomass*0.01);
	}

}
