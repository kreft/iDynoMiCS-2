package idynomics;

import java.util.LinkedList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.SpeciesLib;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import utility.*;

/**
 * \brief Simulator manages all compartments, making sure they synchronise at
 * the correct times. 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Simulator implements CanPrelaunchCheck, Runnable, XMLable
{
	/**
	 * \brief List of {@code Compartment}s in this {@code Simulator}.
	 * 
	 * Order is irrelevant, and each {@code Compartment} knows its own name.
	 */
	protected LinkedList<Compartment> _compartments = 
										   		new LinkedList<Compartment>();
	/**
	 * Contains information about all species for this simulation.
	 */
	public SpeciesLib speciesLibrary = new SpeciesLib();

	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Simulator()
	{
		//TODO fully implement MTRandom (reading in random seed)
		ExtraMath.initialiseRandomNumberGenerator();
	}
	
	public void init(Element xmlElem)
	{
		/*
		 * Set up the Timer.
		 */
		// TODO change protocol files accordingly
		Timer.init( XmlHandler.loadUnique(xmlElem, "timer") );
		/*
		 * Set up the compartments.
		 */
		Log.out(tier.NORMAL, "Loading compartments...");
		NodeList children;
		children = XmlHandler.getAll(xmlElem, "compartment");
		if ( children.getLength() == 0 )
		{
			Log.out(tier.CRITICAL, 
				   "Warning: Simulator initialised without any compartments!");
		}
		Element child;
		String str;
		for ( int i = 0; i < children.getLength(); i++ )
		{
			child = (Element) children.item(i);
			str = XmlHandler.gatherAttribute(child, "name");
			Log.out(tier.NORMAL, "\t\tMaking "+str);
			str = Helper.obtainInput(str, "compartment name");
			Compartment aCompartment = this.addCompartment(str);
			aCompartment.init(child);
		}
		Log.out(tier.NORMAL, "\tCompartments loaded");
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Add a {@code Compartment} with the given name, checking for
	 * uniqueness.
	 * 
	 * @param name {@code String} name for the {@code Compartment}.
	 * @return The new {@code Compartment} created.
	 */
	public Compartment addCompartment(String name)
	{
		if ( this.hasCompartment(name) )
		{
			Log.out(tier.CRITICAL, 
				"Warning: simulator already has a compartment called "+name);
		}
		Compartment aCompartment = new Compartment();
		aCompartment.name = name;
		this._compartments.add(aCompartment);
		return aCompartment;
	}
	
	/**
	 * \brief Check if this has a {@code Compartment} called by the given name.
	 * 
	 * @param name {@code String} name for the {@code Compartment}.
	 * @return {@code boolean} true if this has a {@code Compartment} with the
	 * given <b>name</b>, false if it does not.
	 */
	public boolean hasCompartment(String name)
	{
		for ( Compartment c : this._compartments )
			if ( name.equals(c.name) )
				return true;
		return false;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step()
	{
		/*
		 * Loop through all compartments, calling their internal steps. 
		 */
		for ( Compartment c : this._compartments )
			c.step();
		/*
		 * Once this is done loop through all again, this time exchanging
		 * cells that have tried to cross connected boundaries. 
		 */
		for ( Compartment c : this._compartments )
			c.pushAllOutboundAgents();
		/*
		 * 
		 */
		Timer.step();
		/* we should say something when an iter step is finished */
		Log.out(tier.NORMAL, "iter time: " + Timer.getCurrentTime());

	}
	
	public void run()
	{
		/*
		 * Start timing just before simulation starts.
		 */
		double tic = System.currentTimeMillis();
		while ( Timer.isRunning() )
			this.step();
		/*
		 * Print the simulation results.
		 */
		this.printAll();
		/*
		 * Report simulation time.
		 */
		tic = (System.currentTimeMillis() - tic) * 0.001;
		Log.out(tier.QUIET, "Simulation finished in " + tic + " seconds");
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printAll()
	{
		for ( Compartment c : this._compartments ) 
		{
			Log.out(tier.QUIET, "COMPARTMENT: " + c.name);
			c.printAllSoluteGrids();
			Log.out(tier.QUIET, c.agents.getNumAllAgents() + " agents");
		}
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
		for ( Compartment c : this._compartments )
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
