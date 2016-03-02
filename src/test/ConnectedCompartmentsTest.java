package test;

import agent.Agent;
import boundary.ChemostatConnection;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Compartment;
import idynomics.Simulator;
import idynomics.Timer;
import processManager.SolveChemostat;
import shape.ShapeLibrary.Dimensionless;

public class ConnectedCompartmentsTest
{
	public static void main(String[] args)
	{
		/*
		 * Set up the Simulator and the Timer.
		 */
		Timer.setTimeStepSize(1.0);
		Timer.setEndOfSimulation(10.0);
		Simulator sim = new Simulator();
		Log.set(Tier.EXPRESSIVE);
		/*
		 * The connection between the two Compartments.
		 */
		ChemostatConnection b1 = new ChemostatConnection();
		ChemostatConnection b2 = new ChemostatConnection();
		b1.setPartnerBoundary(b2);
		b2.setPartnerBoundary(b1);
		/* 
		 * This is the rate at which agents and solutes should flow from the
		 * first Compartment to the second.
		 */
		// TODO automate this somehow
		b1.setFlowRate(-1.0);
		b2.setFlowRate(1.0);
		/*
		 * First compartment.
		 */
		Compartment c1 = sim.addCompartment("first");
		Dimensionless s1 = new Dimensionless();
		s1.setVolume(1.0);
		s1.addOtherBoundary(b1);
		c1.setShape(s1);
		SolveChemostat p1 = new SolveChemostat();
		p1.init();
		p1.setTimeStepSize(1.0);
		c1.addProcessManager(p1);
		/*
		 * Second compartment.
		 */
		Compartment c2 = sim.addCompartment("second");
		Dimensionless s2 = new Dimensionless();
		s2.setVolume(1.0);
		s2.addOtherBoundary(b2);
		c2.setShape(s2);
		SolveChemostat p2 = new SolveChemostat();
		p2.init();
		p2.setTimeStepSize(1.0);
		c2.addProcessManager(p2);
		/*
		 * Fill the First compartment with Agents.
		 */
		Agent anAgent;
		for ( int i = 0; i < 100; i++ )
		{
			anAgent = new Agent();
			c1.agents.addAgent(anAgent);
		}
		/*
		 * 
		 */
		sim.run();
	}
}