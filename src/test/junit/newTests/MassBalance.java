package test.junit.newTests;

import java.util.Collection;
import java.util.LinkedList;

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


/**
 * \brief: Unit test for conservation of mass 

 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class MassBalance implements Testable {
	
	private double _initialMass = 0.4*10;
	
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
		Log.set(Tier.NORMAL);
		Tester.println("mass balance", mode);
		Compartment chemostat = Idynomics.simulator.getCompartment("chemostat");
		Compartment biofilm = Idynomics.simulator.getCompartment("biofilm");
		
		double dt = Idynomics.simulator.timer.getTimeStepSize();
		int i=0;
		String sol = "glucose";
		double[] vol = { chemostat.environment.getShape().getTotalVolume(), 
				biofilm.environment.getShape().getTotalVolume() };
		
		double[] flow = new double[2];	
		double[] mflow = new double[2];
		
		double[] con = new double[2];
		double conInit = 0.0;
		
		double[] dM = new double[3];
		double dMq = 0.0;
		
		double dPq = 0.0;
		double dF = 0.0;
		
		/* amount of solute (mass) in the system previously (init with t=0) */
		double[] mPre = { chemostat.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[0], 
			biofilm.environment.getSoluteGrid(sol).getAverage(
				ArrayType.CONCN)*vol[1] };
		
		Filter agents = FilterLogic.filterFromString("mass", biofilm);
		LinkedList<AspectInterface> toCount = new LinkedList<AspectInterface>();
		for (Agent a : biofilm.agents.getAllAgents())
			toCount.add((AspectInterface) a);
		double biomass = _initialMass = Counter.count(agents, toCount)[0];
		
		Tester.print( "in -\t" +
				"out -\t" +
				"cons =\t" +
				"accu\t" +
				"solver", mode);

		/* run the simulator manually and gather relevant system properties
		 * in the mean while. */
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Tester.println("\n # " + i++, mode);
			double[] in = { 0.0, 0.0, 0.0 };
			double[] out = { 0.0, 0.0, 0.0 };
			Idynomics.simulator.step();
			
			con[0] = chemostat.environment.getSoluteGrid(sol).
					getAverage(ArrayType.CONCN);
			con[1] = biofilm.environment.getSoluteGrid(sol).
					getAverage(ArrayType.CONCN);
			if (conInit == 0.0)
				conInit = con[0];
			
			/* look at all chemostat in and out flows */
			Collection<Boundary> boundaries = 
					chemostat.getShape().getAllBoundaries();
			for( Boundary e : boundaries)
			{
				flow[0] = e.getVolumeFlowRate();	
				mflow[0] = e.getMassFlowRate(sol);

				if( mflow[0] > 0.0)
				{
					in[0] += mflow[0] * dt;
					double cons = ((ConstantConcentrationToChemostat) e).
							getConcentration(sol);
					in[2] += flow[0] * cons * dt;
				}
				else
				{
					out[0] -= mflow[0] * dt;
					out[2] -= flow[0] * con[0] * dt;
				}
			}
			Tester.println("chemostat: in - out = acc, solved", mode);
			dM[0] = in[0]-out[0];
			
			Tester.println(
					String.format("%- 10.3f -", in[0]) +
					String.format("%- 20.3f =", out[0]) +
					String.format("%- 10.3f", 	dM[0]) +
					String.format("f%- 10.3g", 	(con[0]*vol[0])-mPre[0]), mode);
			
			dM[2] = in[2]-out[2];
			dF += dM[2];
			
			Tester.println("excluding biofilm diffusion "
					+ "(net in/out for overall system)", mode);
			Tester.println(
					String.format("%- 10.3g -", in[2]) +
					String.format("%- 20.3g =", out[2]) +
					String.format("%- 20.3g ", 	dM[2]), mode);

			/* look at all biofilm in and out flows */
			boundaries = biofilm.getShape().getAllBoundaries();
			for( Boundary e : boundaries)
			{
				flow[1] = e.getVolumeFlowRate();	
				mflow[1] = e.getMassFlowRate(sol);
				if( mflow[1] > 0.0)
					in[1] += mflow[1] * dt;
				else
					out[1] -= mflow[1] * dt;
			}

			toCount = new LinkedList<AspectInterface>();
			for (Agent a : biofilm.agents.getAllAgents())
				toCount.add((AspectInterface) a);
			
			double dBiomass = -(biomass - Counter.count(agents, toCount)[0]);
			biomass = Counter.count(agents, toCount)[0];
			
			dM[1] = in[1]-out[1]-(2.63*(dBiomass));

			Tester.println("biofilm: in - out - consumed = acc, solver", mode);
			Tester.println(
					String.format("%- 10.3f -", in[1]) +
					String.format("%- 10.3f -", out[1]) +
					String.format("%- 8.3f =", 	(2.63*dBiomass)) +
					String.format("%- 10.3f", 	dM[1]) +
					String.format("f%- 10.3g", 	(con[1]*vol[1])-mPre[1]), mode);

			/* look at the overall system */
			double dMtot = dM[0]+dM[1];
			double mPretot = 	((con[0] * vol[0]) - mPre[0]) +
								((con[1] * vol[1]) - mPre[1]);
			
			Tester.println("Mass, Mass solved, difference", mode);
			Tester.println( 
					String.format("%- 10.3f ", 	dMtot) +
					String.format(" %- 10.3f ", mPretot) +
					String.format(" %- 10.3f ", (mPretot-dMtot) ) +
					String.format("%30s", "Delta Step"), mode);
			
			dMq += dMtot;
			dPq += mPretot;
			
			Tester.println( 
					String.format("%- 10.3f ", 	dMq) +
					String.format(" %- 10.3f ", dPq) +
					String.format(" %- 10.3f ", (dPq-dMq) ) +
					String.format("%30s", "Delta Cummulative"), mode);
			mPre[0] = con[0]*vol[0];
			mPre[1] = con[1]*vol[1];
		}
		
		/* report final flows, consumption and discrepancies */
		biomass = Counter.count(agents, toCount)[0];
		Tester.println( "\n delta gluc from start (total mass): " + 
				( ( (con[0]-conInit) * vol[0]) + ( (con[1]-conInit) * 
				vol[1]) ), mode);
		Tester.println( "\n delta gluc from start tracked cumalative: " + 
				dPq, mode);
		Tester.println( "\n final substrate converted to biomass sim: " + 
				2.63*(biomass-_initialMass), mode);
		Tester.println( "\n delta mass chemostat in/out flows: " + 
				dF, mode);
		Tester.println( "\n total mass gained/lossed by solver: " + 
				(dF - 2.63*(biomass-_initialMass) - dPq), mode);
		
		/* the mass balance should close, but a small error can be permitted
		 * eg. < 1% of the biomass 
		 * 
		 * dF: net glucose added/removed from chemostat by in and outflows
		 * 2.63*(biomass-0.4): amount of glucose converted to biomass, init: 0.4
		 * dPq: total increase/decrease of glucose in system
		 * 
		 * in - out - consumption - accumulation = 0 */ 
		Tester.assess(0.0, (dF - 2.63*( biomass-_initialMass ) - dPq), 
				2.63*(biomass-_initialMass)*0.01, mode);
	}

}
