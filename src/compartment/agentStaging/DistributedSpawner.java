package compartment.agentStaging;

import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import utility.Helper;


/**
 * \Brief Spawner places agents on grid with equidistant spacing.
 * 
 * Dimensions with spacing interval of 0.0 will only receive agents at the 
 * orient layer.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * 
 *
 */
public class DistributedSpawner extends Spawner {
	
	public String ORIENT = AspectRef.spawnerOrient;
	public String SPACING = AspectRef.spawnerSpacing;
	
	private double[] _spacing;
	private double[] _orient;
	private double[] _max;
	
	/**
	 * default class initiation
	 */
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		super.init(xmlElem, agents, compartmentName);
		this._max = this.getCompartment().getShape().getDimensionLengths();
		this._spacing = (double[]) this.getValue(SPACING);
		this._orient = (double[]) this.getValue(ORIENT);
	}

	/**
	 * Spawn agents on user defined grid
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void spawn() 
	{
		if ( Helper.isNullOrEmpty(_spacing) || Helper.isNullOrEmpty(_orient) )
		{
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() + " required"
					+ "parameters missing: " + ORIENT + " and/or " + SPACING );
		}
		LinkedList<double[]> locations = new LinkedList<double[]>();
		for( double d : positions(_orient[0],_spacing[0], _max[0]))
			locations.add( new double[] {d} );
		
		for(int i = 1; i < _orient.length; i++)
		{
			LinkedList<double[]> temp = new LinkedList<double[]>();
			for( double d : positions(_orient[i],_spacing[i], _max[i]))
			{
				for( double[] loc : locations )
				{
					double[] dloc = Vector.copy( loc );
					temp.add( Vector.append( dloc , d ) );
				}
			}
			locations = (LinkedList<double[]>) temp.clone();
		}
		int i = 0;
		for ( double[] loc : locations )
		{
			/* break if the target number is reached */
			if( i >= this._numberOfAgents)
				break;
			this.spawnAgent(loc);
			i++;
		}
	}
	
	/**
	 * A list with all equally spaced positions within linear space starting
	 * start and stopping before max.
	 * @param start
	 * @param space
	 * @param max
	 * @return
	 */
	private LinkedList<Double> positions(double start, double space, double max)
	{
		LinkedList<Double> out = new LinkedList<Double>();
		if ( space == 0.0 || start+space > max)
		{
			out.add(start);
		} else if ( start > max ) {
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() + " orient "
					+ "outside domain intervals." );
		} else {
			for(double position = start; position < max; position += space)
				out.add(position);
		}
		return out;
	}
	
	/**
	 * Spawn agent at location.
	 * @param location
	 */
	private void spawnAgent(double[] location)
	{
		/* use copy constructor */
		Agent newAgent = new Agent(this.getTemplate());
		newAgent.set(AspectRef.agentBody, 
				new Body( this.getMorphology(), location, 0.0, 0.0 ) );
		newAgent.setCompartment( this.getCompartment() );
		newAgent.registerBirth();
	}
}
