package aspect.event;

import surface.Point;
import utility.ExtraMath;
import linearAlgebra.Vector;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;

/**
 * Cell death class for bacteria disinfection process
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author .
 */
public class DeathEvent extends Event
{
	/**
	 * The Agent's mass.
	 */
	public String MASS = NameRef.agentMass;
	/**
	 * If the Agent's mass is above this value, trigger division.
	 */
	public String THRESHOLD_MASS = NameRef.divisionMass;
	/**
	 * The fraction of mass that is kept by the mother Agent: 1 - this value
	 * will be inherited by th daughter cell. Must be between 0 and 1
	 * exclusive.
	 */
	public String MUM_MASS_FRAC = NameRef.mumMassFrac;
	/**
	 * Amount of stochastic variation used in {@code mumMassFrac}.
	 */
	public String MUM_MASS_FRAC_CV = NameRef.mumMassFracCV;
	/**
	 * The Agent's body.
	 */
	public String BODY = NameRef.agentBody;
	/**
	 * The radius of the Agent's body.
	 */
	public String RADIUS = NameRef.bodyRadius;
	/**
	 * TODO
	 */
	public String LINKED = NameRef.agentLinks;
	/**
	 * TODO
	 */
	public String LINKER_DIST = NameRef.linkerDistance;
	/**
	 * Name of the update body event that both agents should call after
	 * division. 
	 */
	public String UPDATE_BODY = NameRef.agentUpdateBody;
	/**
	 * Name of the division event that both agents should call at the end of
	 * this event, in case agents have grown a lot between time steps.
	 */
	public String DIVIDE = NameRef.agentDivide;
	
	// TODO delete?
	
	public DeathEvent()
	{
		setInput("mass,radius,body");
	}

	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Agent mother = (Agent) initiator;
		
		EnvironmentContainer env = mother.getCompartment()._environment;
		SpatialGrid toxin = env.getSoluteGrid("chlorine");
		
		Body momBody = (Body) mother.get(BODY);
	
		List<Point> points = momBody.getPoints();
		
		double[] firstLocation = points.get(0).getPosition();
		
	 //get chlorine concentration around a cell 
		double concn = toxin.getValueAt(ArrayType.CONCN,firstLocation);
		
	// we need to get max chlroine concentration 
		double maxconcentration = ???
				
	// Compare local chlorine concentration to chlorine threshold concentration for cell death (lysis)  			
		if ( this.maxconcentration > 0.5 )	
	// Cell dies
	
		   Body mother = (Body) mother.get(0);

		
		// TODO Joints state will be removed
		double[] originalPos = momBody.getJoints().get(0);
		double[] shift = Vector.randomPlusMinus(originalPos.length,(double) mother.get(0));		
			
			
		
		
		else
		{
			Log.out(Tier.BULK, "Agent "+mother.identity()+
					" does not have a body to shift after CoccoidDivision");
		}
		/* Update filial links, if appropriate. */
		if ( mother.isAspect(LINKER_DIST) )
			this.updateLinkers(mother, daughter);
		else
		{
			Log.out(Tier.BULK, "Agent "+mother.identity()+
					" does not create fillial links");
		}
		/* Register the mother's death in the compartment they belong to. */
		/* The bodies of both cells may now need updating. */
		mother.event(UPDATE_BODY);
	}
	
	/**
	 * \brief Check if the given agent should divide now.
	 * 
	 * @param anAgent An agent.
	 * @param {@code true} if the agent should divide now, {@code false} if it
	 * should wait.
	 */
	// TODO generalise this so that the user can set the variable which
	// triggers division, and the value of this variable it should use.
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
		/*
		 * Find the threshold that triggers division.
		 */
		double threshold = 0.2;
		if ( anAgent.isAspect(this.THRESHOLD_MASS) )
			threshold = anAgent.getDouble(this.THRESHOLD_MASS);
		/*
		 * Compare the two values.
		 */
		return (variable > threshold);
	}
	
	/**
	 * \brief Transfer mass from <b>mother</b> to <b>daughter</b>.
	 * 
	 * <p>By default, half the mass if transferred, but this can be overridden
	 * if the mother has <i>mumMassFrac</i> (and <i>mumMassFracCV</i>) set.</p>
	 * 
	 * @param mother Agent with too much mass.
	 * @param daughter Agent with no mass.
	 */
	private void transferMass(Agent mother, Agent daughter)
	{
		
		 * Transfer the mass from mother to daughter, using mumMassFrac.
		 */
		Object mumMass = mother.get(this.MASS);
		if ( mumMass instanceof Double )
		{
			double motherMass = (Double) mumMass;
			mother.set(this.MASS, motherMass * mumMassFrac);
			daughter.set(this.MASS, motherMass * (1.0 - mumMassFrac));
		}
		else if ( mumMass instanceof double[] )
		{
			double[] motherMass = (double[]) mumMass;
			double[] daughterMass = Vector.times(motherMass, 1 - mumMassFrac);
			Vector.timesEquals(motherMass, mumMassFrac);
			mother.set(this.MASS, motherMass);
			daughter.set(this.MASS, daughterMass);
		}
		// TODO handle more potential types of mass aspect, e.g. HashMap
		else
		{
			Log.out(Tier.CRITICAL, "Agent "+mother.identity()+
					" has an unrecognised mass type: "+
					mumMass.getClass().toString());
		}
	}
	
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param mother An agent.
	 * @param daughter Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	private void shiftBodies(Agent mother, Agent daughter)
	{
		Body momBody = (Body) mother.get(BODY);
		Body daughterBody = (Body) daughter.get(BODY);
		
		// TODO Joints state will be removed
		double[] originalPos = momBody.getJoints().get(0);
		double[] shift = Vector.randomPlusMinus(originalPos.length, 
				0.5*mother.getDouble(RADIUS));
		
		Point p = momBody.getPoints().get(0);
		p.setPosition(Vector.add(originalPos, shift));
		Point q = daughterBody.getPoints().get(0);
		q.setPosition(Vector.minus(originalPos, shift));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param mother
	 * @param daughter
	 */
	//TODO work in progress, currently testing filial links
	@SuppressWarnings("unchecked")
	private void updateLinkers(Agent mother, Agent daughter)
	{
		/*
		 * If this mother can link, add the daughter to her list of
		 * links and update.
		 */
		LinkedList<Integer> linkers;
		if ( mother.isAspect(LINKED) )
			linkers = (LinkedList<Integer>) mother.getValue(LINKED);
		else
			linkers = new LinkedList<Integer>();
		linkers.add(daughter.identity());
		// TODO add the mother to the daughter's links?
		// TODO presumably, the daughter's linkers were copied directly
		// from the mother... is this appropriate?
		mother.set(LINKED, linkers);
	}
}