package aspect.event;

import java.util.LinkedList;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Point;
import utility.Helper;

/**
 * FIXME MAP handling for this class needs updating, see CoccoidDivision as
 * example
 * rod division, taking into account periodic boundaries
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class RodDivision extends Event {
	
	public String MASS = AspectRef.agentMass;
	/**
	 * The Agent's mass.
	 */
	public String RADIUS = AspectRef.bodyRadius;
	public String BODY = AspectRef.agentBody;
	public String LINKED = AspectRef.agentLinks;
	public String LINKER_DIST = AspectRef.linkerDistance;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String DIVIDE = AspectRef.agentDivide;
	public String THRESHOLD_MASS = AspectRef.divisionMass;
	/**
	 * Name of the plasmid loss event that should be called for the daughter
	 * agent, if specified in the agent definition.
	 */
	public String PLASMID_LOSS = AspectRef.agentPlasmidLoss;

	/**
	 * Method that initiates the division
	 */
	@SuppressWarnings("unchecked")
	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep)
	{
		Agent mother = (Agent) initiator;

		Shape shape = mother.getCompartment().getShape();
		
		if ( ! this.shouldDivide(mother) )
			return;
		
		Body momBody = (Body) mother.get(BODY);

		Agent daughter = new Agent(mother); // the copy constructor

		/* FIXME maybe move this type of methods to a lib */
		CoccoidDivision.transferMass(mother, daughter);
		
		/*
		 * find the closest distance between the two mass points of the rod
		 * agent and assumes this is the correct length, preventing rods being
		 * stretched out over the entire domain
		 */
		double[] midPos = shape.periodicMidPoint(
				momBody.getPosition(0), 
				momBody.getPosition(1) );
		
		double[] shift = Vector.randomPlusMinus( midPos.length, 
				0.05*(double) mother.get(RADIUS) );
		
		Point p = momBody.getPoints().get(1);
		p.setPosition( shape.periodicMidPoint( 
				momBody.getPosition(0),
				Vector.add(midPos, shift) ) );
		
		Body daughterBody = (Body) daughter.get(BODY);
		Point q = daughterBody.getPoints().get(0);
		q.setPosition( shape.periodicMidPoint(
				daughterBody.getPosition(1),
				Vector.minus( midPos, shift ) ) );


		//TODO work in progress, currently testing fillial links
		if ( mother.isAspect(LINKER_DIST))
		{
			LinkedList<Integer> linkers = 
					(mother.isAspect(LINKED) ? (LinkedList
					<Integer>) mother.getValue(LINKED) :
					new LinkedList<Integer>());
			linkers.add(daughter.identity());
			mother.set(LINKED, linkers);
		}
		daughter.registerBirth();
		
		/* if either is still larger than the division size they need to divide 
		 * again */
		mother.event(UPDATE_BODY);
		daughter.event(UPDATE_BODY);
		/* Call the plasmid loss event */
		if (daughter.isAspect(PLASMID_LOSS))
			daughter.event(PLASMID_LOSS);
		mother.event(DIVIDE);
		daughter.event(DIVIDE);
	}
	
	
	/**
	 * \brief Check if the given agent should divide now.
	 * 
	 * @param anAgent An agent.
	 * @param {@code true} if the agent should divide now, {@code false} if it
	 * should wait.
	 */
	// TODO generalize this so that the user can set the variable which
	// triggers division, and the value of this variable it should use.
	@SuppressWarnings("unchecked")
	private boolean shouldDivide(Agent anAgent)
	{
		/*
		 * Find the agent-specific variable to test (mass, by default).
		 */
		double variable = 0.0;
		Object mumMass = anAgent.get(this.MASS);
		if ( mumMass instanceof Double )
			variable = (Double) mumMass;
		else if ( mumMass instanceof double[] )
			variable = Vector.sum((double[]) mumMass);
		else if ( mumMass instanceof Map )
			variable = Helper.totalValue((Map<String,Double>) mumMass);
		else
		{
			// TODO safety?
		}
		/*
		 * Find the threshold that triggers division.
		 */
		double threshold = Double.MAX_VALUE;
		if ( anAgent.isAspect(this.THRESHOLD_MASS) )
			threshold = anAgent.getDouble(this.THRESHOLD_MASS);
		/*
		 * Compare the two values.
		 */
		return (variable > threshold);
	}
}
