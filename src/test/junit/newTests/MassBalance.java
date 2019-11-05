package test.junit.newTests;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import agent.Agent;
import analysis.Counter;
import analysis.FilterLogic;
import analysis.filter.Filter;
import aspect.AspectInterface;
import boundary.Boundary;
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
		
		
		double[] in = { 0.0, 0.0 };
		double[] out = { 0.0, 0.0 };
		double[] dM = new double[2];
		
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
					in[0] += mflow[0];
				else
					out[0] -= mflow[0];
			}
			
			dM[0] = in[0]-out[0];
			System.out.print(
					String.format("%.2f-\t", in[0]) +
					String.format("%.2f=\t\t", out[0]) +
					String.format("%.2f\t", dM[0]));
			System.out.println(String.format(" f: %.4g", (con[0]*vol[0])-mPre[0]));

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
			System.out.print(
					String.format("%.2f-\t", in[1]) +
					String.format("%.2f-\t", out[1]) +
					String.format("%.2f=\t", dBiomass) +
					String.format("%.2f\t", dM[1]));
			System.out.println(String.format(" f: %.4g", (con[1]*vol[1])-mPre[1]));

			double dMtot = dM[0]+dM[1];
			double mPretot = ((con[0]*vol[0])-mPre[0])+((con[1]*vol[1])-mPre[1]);
			System.out.println( String.format("%.2f\t", dMtot) + " " + 
					String.format("%.2f\t", mPretot) + " " + 
					String.format("%.2f\t", (mPretot-dMtot) ) );
			
			dMq += dMtot;
			dPq += mPretot;
			
			System.out.println( String.format("%.2f\t", dMq) + " " + 
					String.format("%.2f\t", dPq) + " " + 
					String.format("%.2f\t", (dPq-dMq) ) );
			mPre[0] = con[0]*vol[0];
			mPre[1] = con[1]*vol[1];
		}
		System.out.println( "\ntotal mass gained/lossed by solver: " + dPq);
		System.out.println( "\nfinal biomass sim: " + Counter.count(agents, subjects)[0]);
		 assertEquals(0.0, dPq, 0.1);
	}

}
