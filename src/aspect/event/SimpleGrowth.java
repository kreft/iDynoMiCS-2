package aspect.event;

import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;

/**
 * \brief Agent growth where the agent has only one kind of biomass.
 * 
 * <p>For more complex growth, consider using the event InternalProduction
 * instead.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SimpleGrowth extends Event
{
	public String MASS = AspectRef.agentMass;
	public String GROWTH_RATE = AspectRef.growthRate;

	@Override
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		/*
		 * Get the agent's mass and growth rate as scalars (for vectors,
		 * consider using the event InternalProduction instead).
		 */
		Double mass = initiator.getDouble(this.MASS);
		Double growthRate = initiator.getDouble(this.GROWTH_RATE);
		/*
		 * If either of these are undefined, or there is no growth, then there
		 * is nothing more to do.
		 */
		if ( mass == null || growthRate == null || growthRate == 0.0 )
			return;
		/*
		 * Assume the growth rate to be representative of the whole time step,
		 * and apply the change in mass. Now that this growth rate has been
		 * applied, remove the aspect so that it does not get applied again.
		 */
		initiator.set(this.MASS, mass + (growthRate * timeStep));
		initiator.reg().remove(this.GROWTH_RATE);
	}
}
