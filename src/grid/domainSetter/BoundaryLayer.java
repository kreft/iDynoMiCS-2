/**
 * 
 */
package grid.domainSetter;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.tier;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import surface.Ball;
import surface.Collision;
import surface.Surface;

/**
 * \brief A domain setter that includes voxels on the domain array of the given
 * grid that are within a set distance of any agents.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author baco
 * @since January 2016
 */
public class BoundaryLayer implements IsDomainSetter
{
	/**
	 * Value to set all voxels of the domain array of the given grid.
	 */
	protected double _value = 1.0;
	
	/**
	 * the thickness of the pure diffusion film (sorry no flow currently)
	 */
	protected double _layerThickness;
	
	/**
	 * initiation from xml node attributes: value, layerthickness
	 * @param xmlNode
	 */
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
			Log.out(tier.CRITICAL,"Boundary layer thickness must be set!");
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
		List<Agent> neighbors;
		while ( aGrid.isIteratorValid() )
		{
			Ball gridSphere = new Ball(aGrid.getVoxelCentre(coords), 
					this._layerThickness);
			gridSphere.init(new Collision(null, agents.getShape()));
			/**
			 * find all closeby agents
			 */
			neighbors = agents._agentTree.cyclicsearch(gridSphere.boundingBox());
			for ( Agent a : neighbors )
				for ( Surface s : (List<Surface>) a.get(NameRef.surfaceList))
					
					/**
					 * Set the grid to _value if an agent is within the grid's
					 * sphere
					 */
					if ( gridSphere.distanceTo(s) < 0.0 )
						{
							aGrid.setValueAt(ArrayType.DOMAIN, coords, 
									this._value);
							break;
						}
			coords = aGrid.iteratorNext();
		}
	}
}
