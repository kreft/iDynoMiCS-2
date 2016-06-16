/**
 * 
 */
package grid.diffusivitySetter;

import grid.SpatialGrid;
import static grid.ArrayType.DIFFUSIVITY;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.XmlHandler;
import dataIO.XmlRef;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

/**
 * \brief Basic diffusivity setter that sets the same diffusivity across the
 * entire {@code SpatialGrid}.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class AllSameDiffuse implements IsDiffusivitySetter
{
	/**
	 * The diffusivity for every voxel.
	 */
	protected double _diffusivity;
	
	// NOTE temporary constructor
	public AllSameDiffuse(double diffusivity)
	{
		this._diffusivity = diffusivity;
	}
	
	/**
	 * \brief Initiation from XML node.
	 * 
	 * @param xmlNode
	 */
	public void init(Node xmlNode)
	{
		Element e = (Element) xmlNode;
		String s = XmlHandler.obtainAttribute(e, XmlRef.defaultDiffusivity);
		this._diffusivity = Double.valueOf(s);
	}
	
	@Override
	public void updateDiffusivity(SpatialGrid diffusivityGrid,
			EnvironmentContainer env, AgentContainer agents)
	{
		diffusivityGrid.newArray(DIFFUSIVITY, this._diffusivity);
	}
}
