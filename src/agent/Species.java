package agent;

import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.XmlLoad;

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

	public AspectReg<?> reg() {
		return aspectRegistry;
	}
}