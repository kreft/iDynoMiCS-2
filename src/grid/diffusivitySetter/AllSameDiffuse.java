/**
 * 
 */
package grid.diffusivitySetter;

import grid.SpatialGrid;
import static grid.ArrayType.DIFFUSIVITY;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.XmlHandler;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;

/**
 * \brief Basic diffusivity setter that sets the same diffusivity across the
 * entire {@code SpatialGrid}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class AllSameDiffuse implements IsDiffusivitySetter
{
	/**
	 * The diffusivity for every voxel.
	 */
	protected double _diffusivity;
	
	public AllSameDiffuse()
	{
		/* NOTE empty constructor to become Instantiatable, 
		 * Class.forName(className).newInstance(); has to work otherwise we
		 * cannot create a new instance.
		 */
	}
	
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
	public void init(Element xmlElem, NodeConstructor parent)
	{
		String s = XmlHandler.obtainAttribute(xmlElem, XmlRef.defaultDiffusivity, "PARENT NODE");
		this._diffusivity = Double.valueOf(s);
	}
	
	@Override
	public void updateDiffusivity(SpatialGrid diffusivityGrid,
			EnvironmentContainer env, AgentContainer agents)
	{
		diffusivityGrid.newArray(DIFFUSIVITY, this._diffusivity);
	}
}
