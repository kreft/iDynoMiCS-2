/**
 * 
 */
package grid.wellmixedSetter;

import static grid.SpatialGrid.ArrayType.WELLMIXED;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import shape.Shape;
import surface.Ball;
import surface.Collision;
import surface.Surface;

/**
 * \brief A domain setter that includes voxels on the domain array of the given
 * grid that are within a set distance of any agents.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
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
		if ( elem.hasAttribute(XmlRef.valueAttribute) )
		{
			temp = elem.getAttribute(XmlRef.valueAttribute);
			this._value = Double.parseDouble(temp);
		}
		if ( elem.hasAttribute(XmlRef.layerThickness) )
		{
			temp = elem.getAttribute(XmlRef.layerThickness);
			this._layerThickness = Double.parseDouble(temp);
		}
		else
		{
			Log.out(Tier.CRITICAL,"Boundary layer thickness must be set!");
			System.exit(-1);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateWellmixed(SpatialGrid aGrid, AgentContainer agents)
	{
		Shape aShape = aGrid.getShape();
		/*
		 * Reset the domain array.
		 */
		aGrid.newArray(WELLMIXED);
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		int[] coords = aShape.resetIterator();
		List<Agent> neighbors;
		Collision collision = new Collision(null, agents.getShape());
		while ( aShape.isIteratorValid() )
		{
			Ball gridSphere = new Ball(aShape.getVoxelCentre(coords), 
														this._layerThickness);
			gridSphere.init(collision);
			/*
			 * Find all nearby agents. Set the grid to _value if an agent is
			 * within the grid's sphere
			 */
			neighbors = 
					agents.treeSearch(gridSphere.boundingBox());
			for ( Agent a : neighbors )
				for ( Surface s : (List<Surface>) a.get(AspectRef.surfaceList) )
					if ( gridSphere.distanceTo(s) < 0.0 )
						{
							aGrid.setValueAt(WELLMIXED, coords, this._value);
							break;
						}
			coords = aShape.iteratorNext();
		}
	}
}
