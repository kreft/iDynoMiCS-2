package agent;

import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;

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
	public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
	
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
		loadAspects(xmlNode);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public AspectReg<?> reg()
	{
		return aspectRegistry;
	}
}