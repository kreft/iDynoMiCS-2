package test.junit.oldTests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import agent.Agent;
import aspect.event.CoccoidDivision;
import compartment.Compartment;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import shape.Shape;
import shape.ShapeLibrary.Dimensionless;
import test.OldTests;

public class AgentEventTest
{
	private final static double T_STEP = 1.0;
	private final static double T_MAX = 10.0;
	private final static String COMP_NAME = "comp";
	private final static Shape SHAPE = new Dimensionless(10.0);
	
	private final static double START_MASS = 0.3;
	private final static double THRESHOLD_MASS = 0.1;
	
	
	@Test
	public void divisionWithMassAsDouble()
	{
		OldTests.setupSimulatorForTest(T_STEP, T_MAX, "massAsDouble");
		
		Agent agent = getAgent();
		agent.set(AspectRef.agentMass, START_MASS);
		
		Compartment comp = getCompartment();
		comp.addAgent(agent);
		
		agent.event(AspectRef.agentDivide);
		
		int nAgent = comp.agents.getNumAllAgents();
		assertEquals(nAgent, calcTrueNumAgents());
	}
	
	@Test
	public void divisionWithMassAsMap()
	{
		OldTests.setupSimulatorForTest(T_STEP, T_MAX, "massAsDouble");
		
		Agent agent = getAgent();
		Map<String,Double> massMap = new HashMap<String,Double>();
		massMap.put(AspectRef.biomass, START_MASS);
		agent.set(AspectRef.agentMass, massMap);
		
		
		Compartment comp = getCompartment();
		comp.addAgent(agent);
		
		agent.event(AspectRef.agentDivide);
		
		int nAgent = comp.agents.getNumAllAgents();
		assertEquals(nAgent, calcTrueNumAgents());
	}
	
	private static Compartment getCompartment()
	{
		Compartment comp = Idynomics.simulator.addCompartment(COMP_NAME);
		comp.setShape(SHAPE);
		return comp;
	}
	
	private static Agent getAgent()
	{
		Agent agent = new Agent();
		agent.set(AspectRef.divisionMass, THRESHOLD_MASS);
		agent.set(AspectRef.agentDivide, new CoccoidDivision());
		return agent;
	}
	
	private static int calcTrueNumAgents()
	{
		int out = 1;
		double massPerAgent = START_MASS;
		while ( massPerAgent > THRESHOLD_MASS )
		{
			out *= 2;
			massPerAgent *= 0.5;
		}
		return out;
	}
}
