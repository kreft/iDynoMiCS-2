package agent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.Log;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import idynomics.Idynomics;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Species implements AspectInterface
{
	/**
	 * TODO
	 */
	protected AspectReg<Object> _aspectRegistry = new AspectReg<Object>();
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public Species()
	{
		// Do nothing!
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param xmlNode
	 */
	public Species(Node xmlNode)
	{
		/* Load the primary aspects of this Species. */
		this.loadAspects(xmlNode);
	}
	
	public void loadSpeciesModules(Element xmlElem)
	{
		NodeList nodes = xmlElem.getElementsByTagName(XmlLabel.speciesModule);
		String name;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			Element s = (Element) nodes.item(i);
			name = s.getAttribute(XmlLabel.nameAttribute);
			Log.out(Tier.DEBUG, "Loading SpeciesModule \""+name+"\"");
			this._aspectRegistry.addSubModule(name, 
										Idynomics.simulator.speciesLibrary);
		}
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * Get this {@code Species}' aspect registry.
	 */
	public AspectReg<?> reg()
	{
		return this._aspectRegistry;
	}
}