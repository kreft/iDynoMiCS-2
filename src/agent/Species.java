package agent;

import org.w3c.dom.Node;

import dataIO.XmlLoad;
import generalInterfaces.AspectInterface;

/**
 * 
 * @author baco
 *
 */
public class Species implements AspectInterface
{
	
	public AspectReg<Object> aspectRegistry = new AspectReg<Object>();

    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Species() {
	}
	
	public Species(Node xmlNode) {
		XmlLoad.loadStates(this, xmlNode);
	}

	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	public AspectReg<?> registry() {
		return aspectRegistry;
	}
}