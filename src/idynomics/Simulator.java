package idynomics;

import java.util.HashMap;

public class Simulator
{
	
	protected HashMap<String, Compartment> _compartments;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Simulator()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step()
	{
		/*
		 * Loop through all compartments, calling their internal steps. 
		 */
		this._compartments.forEach((s,c) -> {c.step();});
		/*
		 * Once this is done loop through all again, this time exchanging
		 * cells that have tried to cross connected boundaries. 
		 */
		this._compartments.forEach((s,c) -> {c.pushAllOutboundAgents();});
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
}
