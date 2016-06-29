package testJUnit;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import agent.Agent;
import aspect.AspectRef;
import idynomics.Compartment;
import idynomics.Idynomics;
import processManager.library.SolveChemostat;
import reaction.Reaction;
import shape.Shape;
import shape.ShapeLibrary.Dimensionless;

public class MassAsHashMapTest
{
	private final static double T_STEP = 1.0;
	private final static double T_MAX = 10.0;
	private final static String COMP_NAME = "comp";
	private final static String SOLUTE_NAME = "solute";
	private final static double INITIAL_CONCN = 100.0;
	private final static Shape SHAPE = new Dimensionless(10.0);
	
	private final static double START_MASS = 0.1;
	
	@Test
	public void massAsDouble()
	{
		AllTests.setupSimulatorForTest(T_STEP, T_MAX, "massAsDouble");
		
		Agent agent = getAgent();
		agent.set(AspectRef.agentMass, START_MASS);
		List<Reaction> reactions = new LinkedList<Reaction>();
		reactions.add(new Reaction(AspectRef.agentMass, 1.0, "1.0", "reac"));
		agent.set(AspectRef.agentReactions, reactions);
		
		Compartment comp = getCompartment();
		comp.addAgent(agent);
		
		Idynomics.simulator.run();
	}
	
	
	private static Compartment getCompartment()
	{
		Compartment comp = Idynomics.simulator.addCompartment(COMP_NAME);
		comp.setShape(SHAPE);
		comp.environment.addSolute(SOLUTE_NAME, INITIAL_CONCN);
		
		SolveChemostat pm = new SolveChemostat();
		pm.setName("chemostat solver");
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(T_STEP);
		pm.set(AspectRef.soluteNames, new String[]{SOLUTE_NAME});
		
		comp.addProcessManager(pm);
		
		return comp;
	}
	
	private static Agent getAgent()
	{
		Agent agent = new Agent();
		
		return agent;
	}
	
}
