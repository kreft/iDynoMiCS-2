/**
 * 
 */
package grid.domainSetter;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.LogFile;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import linearAlgebra.Vector;

/**
 * \brief A domain setter that includes voxels on the domain array of the given
 * grid that are within a set distance of any agents.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @since January 2016
 */
public class BoundaryLayer implements IsDomainSetter
{
	/**
	 * Value to set all voxels of the domain array of the given grid.
	 */
	protected double _value = 1.0;
	
	protected double _layerThickness;
	
	public void init(Node xmlNode)
	{
		// TODO Check this, maybe making use of XMLable interface
		Element elem = (Element) xmlNode;
		if ( elem.hasAttribute("value") )
			this._value = Double.parseDouble(elem.getAttribute("value"));
		if ( elem.hasAttribute("layerThickness") )
		{
			this._layerThickness = 
			Double.parseDouble(elem.getAttribute("layerThickness"));
		}
		else
		{
			LogFile.shoutLog("Boundary layer thickness must be set!");
			System.exit(-1);
		}
	}
	
	@Override
	public void updateDomain(SpatialGrid aGrid, AgentContainer agents)
	{
		/*
		 * Reset the domain array.
		 */
		aGrid.newArray(ArrayType.DOMAIN);
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		int[] coords = aGrid.resetIterator();
		double[] location;
		double[] searchOrigin = new double[agents.getNumDims()];
		double[] searchSize = Vector.vector(agents.getNumDims(),
											this._layerThickness * 2);
		List<Agent> neighbors;
		while ( aGrid.isIteratorValid() )
		{
			location = aGrid.getVoxelCentre(coords);
			Vector.addTo(searchOrigin, location, -this._layerThickness);
			neighbors = agents._agentTree.search(searchOrigin, searchSize);
			for ( Agent anAgent : neighbors )
				if ( true )
				{
					// TODO if this agent's surface is <= layerThickness from
					// location, set the domain to 1 and break.
					aGrid.setValueAt(ArrayType.DOMAIN, coords, this._value);
					break;
				}
			coords = aGrid.iteratorNext();
		}
	}
}
