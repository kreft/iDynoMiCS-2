package processManager.library;

import java.util.LinkedList;

import agent.Agent;
import boundary.Boundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import processManager.ProcessDeparture;
import utility.Helper;


/**
 * A departure process that removes agents that are less than the boundary layer
 * thickness away from a biofilm boundary layer. Note any compartment that uses
 * this process manager should contain a biofilm boundary layer.
 * @author Tim Foster
 */

public class AgentsInBoundaryLayerDepart extends ProcessDeparture {

	private BiofilmBoundaryLayer _boundary;
	
	
	@Override
	protected LinkedList<Agent> agentsDepart()
	{
		
		LinkedList<Agent> out = new LinkedList<Agent>();
		
		if (Helper.isNullOrEmpty(this._boundary))
		{
			for (Boundary b : Idynomics.simulator.getCompartment(
					this._compartmentName).getShape().getAllBoundaries())
			{
				if (b instanceof BiofilmBoundaryLayer)
				{
					this._boundary = (BiofilmBoundaryLayer) b;
				}
			}
		}
		
		if (Helper.isNullOrEmpty(this._boundary))
		{
			if (Log.shouldWrite(Tier.NORMAL))
				Log.out(Tier.NORMAL, "Departure process " + this._name + 
						" requires a BiofilmBoundaryLayer in compartment " +
						this._compartmentName + ". Returning empty departures"
						+ "list.");
			return out;
		}
		
		/*
		 * Find all agents who are less than layerThickness away.
		 */
		out.addAll(this._agents.treeSearch(
				this._boundary, this._boundary.getLayerThickness()));
		/*
		 * Find all agents who are unattached to others or to a boundary,
		 * and who are on this side of the biofilm (in, e.g., the case of a
		 * floating granule).
		 */
		// TODO
		return out;

		
	}

}
