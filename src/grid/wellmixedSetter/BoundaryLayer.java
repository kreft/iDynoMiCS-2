/**
 * 
 */
package grid.wellmixedSetter;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import grid.SpatialGrid;
import static grid.SpatialGrid.ArrayType.*;
import idynomics.AgentContainer;
import idynomics.NameRef;
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
public class BoundaryLayer implements IsWellmixedSetter
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
		String temp;
		if ( elem.hasAttribute(XmlLabel.valueAttribute) )
		{
			
			this._value = Double.parseDouble(elem.getAttribute(
					XmlLabel.valueAttribute));
		}
		if ( elem.hasAttribute(XmlLabel.layerThickness) )
		{
			this._layerThickness = 
			Double.parseDouble(elem.getAttribute(XmlLabel.layerThickness));
		}
		else
		{
			Log.out(tier.CRITICAL,"Boundary layer thickness must be set!");
			System.exit(-1);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateWellmixed(SpatialGrid aGrid, AgentContainer agents)
	{
		/*
		 * Reset the domain array.
		 */
		aGrid.newArray(WELLMIXED);
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		int[] coords = aGrid.resetIterator();
		List<Agent> neighbors;
		Collision collision = new Collision(null, agents.getShape());
		while ( aGrid.isIteratorValid() )
		{
			Ball gridSphere = new Ball(aGrid.getVoxelCentre(coords), 
														this._layerThickness);
			gridSphere.init(collision);
			/*
			 * Find all nearby agents. Set the grid to _value if an agent is
			 * within the grid's sphere
			 */
			neighbors = 
					agents._agentTree.cyclicsearch(gridSphere.boundingBox());
			for ( Agent a : neighbors )
				for ( Surface s : (List<Surface>) a.get(NameRef.surfaceList) )
					if ( gridSphere.distanceTo(s) < 0.0 )
						{
							aGrid.setValueAt(WELLMIXED, coords, this._value);
							break;
						}
			coords = aGrid.iteratorNext();
		}
	}
}
