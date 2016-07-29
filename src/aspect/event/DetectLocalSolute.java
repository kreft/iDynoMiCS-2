/**
 * 
 */
package aspect.event;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Compartment;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.subvoxel.CoordinateMap;

/**
 * \brief Testing/template event that detects the local solute concentrations
 * of an agent and writes them to log file.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class DetectLocalSolute extends Event
{
	
	private String VD_TAG = AspectRef.agentVolumeDistributionMap;
	
	public String SOLUTE_NAME = "glucose";
	
	@Override
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.NORMAL;
		/*
		 * This event assumes an Agent with a Compartment defined.
		 */
		Agent anAgent = (Agent) initiator;
		Compartment comp = anAgent.getCompartment();
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "DetectLocalSolute looking for the \""+this.SOLUTE_NAME+
				"\" concentrations around agent (ID: "+anAgent.identity()+
				") in compartment \""+comp.getName()+"\"");
		}
		/*
		 * Find the relevant solute grid, if it exists.
		 */
		if ( ! comp.environment.isSoluteName(SOLUTE_NAME) )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  cannot find concn of a solute "+SOLUTE_NAME+
					" that is not in the environment! Copmpartment "
					+comp.getName());
			}
			return;
		}
		SpatialGrid solute = comp.getSolute(SOLUTE_NAME);
		/*
		 * Find the volume distribution map, if it exists.
		 */
		if ( ! anAgent.isAspect(VD_TAG) )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  cannot find solute concn on an agent that "+
					" has no "+VD_TAG+" (agent ID: "+anAgent.identity()+")");
			}
			return;
		}
		CoordinateMap distribMap = (CoordinateMap) anAgent.getValue(VD_TAG);
		/*
		 * Loop over the coordinates, printing out the solute concentrations.
		 * This is the part that would be changed in any event using this as a
		 * template.
		 */
		for ( int[] coord : distribMap.keySet() )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  concn at "+Vector.toString(coord)+" is "+
					solute.getValueAt(ArrayType.CONCN, coord));
			}
		}
	}
}
