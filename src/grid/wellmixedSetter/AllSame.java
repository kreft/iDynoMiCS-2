/**
 * 
 */
package grid.wellmixedSetter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.XmlLabel;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;

/**
 * \brief A domain setter that sets all voxels on the domain array of the given
 * grid to the same value.
 * 
 * <p>By default, this value is one.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @since January 2016
 */
public class AllSame implements IsWellmixedSetter
{
	/**
	 * Value to set all voxels of the domain array of the given grid.
	 */
	protected double _value = 1.0;
	
	public void init(Node xmlNode)
	{
		// TODO Check this, maybe making use of XMLable interface
		Element elem = (Element) xmlNode;
		if ( elem.hasAttribute(XmlLabel.valueAttribute) )
			this._value = Double.parseDouble(elem.getAttribute(
					XmlLabel.valueAttribute));
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO remove once 
	 * 
	 * @param value
	 */
	public void setValue(double value)
	{
		this._value = value;
	}
	
	@Override
	public void updateWellmixed(SpatialGrid aGrid, AgentContainer agents)
	{
		aGrid.newArray(ArrayType.WELLMIXED, this._value);
	}
}