package aspect.event;

import java.util.HashMap;
import java.util.Map;

import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class InternalProduction  extends Event
{
	/**
	 * Map of product names to product values. Examples of products include
	 * lipids, ribosomes, etc.
	 */
	public String INTERNAL_PRODUCTS = AspectRef.internalProducts;
	/**
	 * Map of production rates for internal products.
	 */
	public String INTERNAL_PRODUCTION = AspectRef.internalProductionRate;

	@SuppressWarnings("unchecked")
	@Override
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		/*
		 * If this agent does not have any internal production values stored,
		 * then there is nothing more to do.
		 */
		if ( ! initiator.isAspect(this.INTERNAL_PRODUCTION) )
			return;
		/*
		 * Now that we know it exists, get the map from product name to amount
		 * of product made.
		 */
		Map<String,Double> internalProduction = (HashMap<String,Double>)
				initiator.getValue(this.INTERNAL_PRODUCTION);
		/*
		 * If the agent's internal products do not yet exist, then make them.
		 */
		Map<String,Double> internalProducts;
		if ( initiator.isAspect(this.INTERNAL_PRODUCTS) )
		{
			internalProducts = (HashMap<String,Double>)
					initiator.getValue(this.INTERNAL_PRODUCTS);
		}
		else
		{
			// TODO Rob [24May2016]: are we sure this is the right approach?
			internalProducts = new HashMap<String,Double>();
		}
		/*
		 * For each product in the production map, 
		 */
		// TODO Rob [24May2016]: check that multiplying by the timeStep here
		// makes sense.
		for ( String p : internalProduction.keySet() )
		{
			double product = (internalProducts.containsKey(p) ?
					internalProducts.get(p) : 0.0);
			double rate = internalProduction.get(p); 
			internalProducts.put(p, product + rate * timeStep);
			internalProduction.put(p, 0.0);
		}
		/*
		 * Store the updated maps back in the agent.
		 */
		initiator.set(this.INTERNAL_PRODUCTS, internalProducts);
		initiator.set(this.INTERNAL_PRODUCTION, internalProduction);
	}
}
