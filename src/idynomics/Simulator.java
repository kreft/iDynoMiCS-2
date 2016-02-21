package idynomics;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.SpeciesLib;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.tier;
import generalInterfaces.CanPrelaunchCheck;
import utility.*;


public class Simulator implements CanPrelaunchCheck, Runnable
{

	protected HashMap<String, Compartment> _compartments = 
										   new HashMap<String, Compartment>();
	
	/*
	 * contains all species for this simulation
	 */
	public SpeciesLib speciesLibrary = new SpeciesLib();

	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Simulator()
	{
		//TODO fully implement MTRandom
		ExtraMath.initialiseRandomNumberGenerator();
	}
	
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		String str;
		NodeList children;
		Element child;
		
		children = XmlHandler.getAll(elem, "compartment");
		if ( children.getLength() == 0 )
		{
			// TODO
		}
		for ( int i = 0; i < children.getLength(); i++ )
		{
			child = (Element) children.item(i);
			str = XmlHandler.attributeFromUniqueNode(child, "name", "string");
			str = Helper.obtainInput(str, "compartment name");
			Compartment aCompartment = this.addCompartment(str);
			aCompartment.init(child);
		}
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Compartment addCompartment(String name)
	{
		if ( this._compartments.containsKey(name) )
			Log.out(tier.CRITICAL, "Warning: overwriting compartment "+name);
		Compartment aCompartment = new Compartment();
		aCompartment.name = name;
		this._compartments.put(name, aCompartment);
		return aCompartment;
	}
	
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
		
		/*
		 * 
		 */
		Timer.step();

	}
	
	public void run()
	{
		/**
		 * Start timing just before simulation starts.
		 */
		double tic = System.currentTimeMillis();
		
		if ( ! isReadyForLaunch() )
		{
			Log.out(tier.CRITICAL, "Simulator not ready to launch!");
			return;
		}
		while ( Timer.isRunning() )
		{
			this.step();
		}
		
		/**
		 * print the simulation results
		 */
		Idynomics.simulator.printAll();
		
		/**
		 * report simulation time
		 */
		Log.out(tier.QUIET, "Simulation finished in: " + 
				(System.currentTimeMillis() - tic) * 0.001 + " seconds");
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printAll()
	{
		this._compartments.forEach((s,c) -> 
		{
			Log.out(tier.QUIET,"COMPARTMENT: " + s);
			c.printAllSoluteGrids();
		});
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		/* Check the log file is initialised. */
		// TODO
		/* Check the random number generator is initialised. */
		if ( ExtraMath.random == null )
		{
			Log.out(tier.CRITICAL,"Random number generator not initialised!");
			return false;
		}
		/* Check we have at least one compartment. */
		if ( this._compartments.isEmpty() )
		{
			Log.out(tier.CRITICAL,"No compartment(s) specified!");
			return false;
		}
		/* If any compartments are not ready, then stop. */
		for ( Compartment c : this._compartments.values() )
		{
			if ( ! c.isReadyForLaunch() )
			{
				Log.out(tier.CRITICAL,"Compartment " + c.name + " not ready for"
						+ " launch!");
				return false;
			}
		}
		
		
		return true;
	}

}
