package idynomics;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.SpeciesLib;
import dataIO.Log;
import dataIO.XmlExport;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import utility.*;
import nodeFactory.*;
import nodeFactory.ModelNode.Requirements;

/**
 * \brief Simulator manages all compartments, making sure they synchronise at
 * the correct times. 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Simulator implements CanPrelaunchCheck, Runnable, XMLable, NodeConstructor
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
	
	/**
	 * The timer
	 */
	public Timer timer;
	
	/**
	 * Xml output writer
	 */
	private XmlExport _xmlOut;
	
	/**
	 * Simulator is the top node in iDynoMiCS and stores its own modelNode and 
	 * within that all child nodes, simulator is the exception to the rule not
	 * storing ModelNodes
	 */
	private ModelNode _modelNode;

	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
		
	public Simulator()
	{
		//TODO fully implement MTRandom (reading in random seed)
		ExtraMath.initialiseRandomNumberGenerator();
		this.timer = new Timer();
		this._xmlOut = new XmlExport();
	}

	/**
	 * return the name of the current simulation
	 * @return
	 */
	public String getName()
	{
		return (Idynomics.global.simulationName == null) ?
					XmlLabel.simulation : Idynomics.global.simulationName;
	}
	
	/**
	 * get the current seed, used to create intermediate restartable save points
	 * @return
	 */
	public long seed()
	{
		long currentSeed = ExtraMath.random.nextLong();
		ExtraMath.intialiseRandomNumberGenerator(currentSeed);
		return currentSeed;
	}
	
	/**
	 * Initiate random number generator with given seed
	 * @param seed
	 */
	public void seed(long seed)
	{
		ExtraMath.intialiseRandomNumberGenerator(seed);
	}
	
	public void init(Element xmlElem)
	{
		/* 
		 * retrieve seed from xml file and initiate random number generator with
		 * that seed
		 */
		String seed =XmlHandler.gatherAttribute(xmlElem, XmlLabel.seed);
		if (seed != "")
			ExtraMath.intialiseRandomNumberGenerator(Long.valueOf(seed));
		
		/*
		 * Set up the Timer.
		 */
		this.timer.init( XmlHandler.loadUnique( xmlElem, XmlLabel.timer ));
		/*
		 * Set up the species library.
		 */
		if (XmlHandler.hasNode(Idynomics.global.xmlDoc, XmlLabel.speciesLibrary))
				this.speciesLibrary.init( XmlHandler.loadUnique(xmlElem, 
						XmlLabel.speciesLibrary ));
		/*
		 * Set up the compartments.
		 */
		Log.out(Tier.NORMAL, "Compartments loading...");
		NodeList children;
		children = XmlHandler.getAll( xmlElem, XmlLabel.compartment );
		if ( children.getLength() == 0 )
		{
			Log.out(Tier.CRITICAL, 
				   "Warning: Simulator initialised without any compartments!");
		}
		Element child;
		String str;
		for ( int i = 0; i < children.getLength(); i++ )
		{
			child = (Element) children.item(i);
			str = XmlHandler.gatherAttribute(child, XmlLabel.nameAttribute);
			Log.out(Tier.NORMAL, "Making "+str);
			str = Helper.obtainInput(str, "compartment name");
			Compartment aCompartment = this.addCompartment(str);
			aCompartment.init(child);
		}
		Log.out(Tier.NORMAL, "Compartments loaded!\n");
		
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
			Log.out(Tier.CRITICAL, 
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
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public List<String> getCompartmentNames()
	{
		LinkedList<String> names = new LinkedList<String>();
		for ( Compartment c : this._compartments )
			names.add(c.name);
		return names;
	}
	
	/**
	 * \brief Check if this contains any {@code Compartment}s.
	 * 
	 * @return {@code boolean}: true if there is at least one
	 * {@code Compartment} in this {@code Simulator}, false if there is none.
	 */
	public boolean hasCompartments()
	{
		return ! this._compartments.isEmpty();
	}
	
	/**
	 * \brief Check if this contains any {@code Compartment}s with at least one
	 * spatial dimension.
	 * 
	 * @return {@code boolean}: true if there is at least one
	 * {@code Compartment} with at least spatial dimension in this
	 * {@code Simulator}, false if there are no {@code Compartment}s or if all
	 * {@code Compartment}s are dimensionless.
	 */
	public boolean hasSpatialCompartments()
	{
		for ( Compartment c : this._compartments )
			if ( ! c.isDimensionless() )
				return true;
		return false;
	}
	
	/**
	 * \brief Get the first {@code Compartment} in this {@code Simulator} that
	 * has at least one spatial dimension.
	 * 
	 * @return A {@code Compartment} if possible, {@code null} if not.
	 */
	public Compartment get1stSpatialCompartment()
	{
		for ( Compartment c : this._compartments )
			if ( ! c.isDimensionless() )
				return c;
		return null;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step()
	{
		/*
		 * TODO
		 */
		for ( Compartment c : this._compartments )
			c.agentsArrive();
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
		this.timer.step();
		
		/*
		 * write state to new xml file
		 */
		_xmlOut.writeFile();
		
		/* we should say something when an iter step is finished */
		Log.out(Tier.NORMAL, "iter time: " + this.timer.getCurrentTime());
		
		/*
		 * reporting agents
		 */
		for (Compartment c : this._compartments)
		{
			Log.out(Tier.QUIET,"COMPARTMENT: " + c.getName());
			Log.out(Tier.QUIET,c.agents.getAllAgents().size() + " agents");
		};

	}
	
	public void run()
	{
		Log.out(Tier.NORMAL, "Launching simulation!");
		/*
		 * Start timing just before simulation starts.
		 */
		double tic = System.currentTimeMillis();
		/* Check if any boundary connections need to be made. */
		for ( Compartment c : this._compartments )
			c.checkBoundaryConnections(this._compartments);
		/* Run the simulation. */
		while ( this.timer.isRunning() )
			this.step();
		/*
		 * Print the simulation results.
		 */
		this.printAll();
		/*
		 * Report simulation time.
		 */
		tic = (System.currentTimeMillis() - tic) * 0.001;
		Log.out(Tier.QUIET, "Simulation finished in " + tic + " seconds\n"+
				"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printAll()
	{
		for ( Compartment c : this._compartments ) 
		{
			Log.out(Tier.QUIET, "COMPARTMENT: " + c.name);
			c.printAllSoluteGrids();
			Log.out(Tier.QUIET, c.agents.getNumAllAgents() + " agents");
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
			Log.out(Tier.CRITICAL,"Random number generator not initialised!");
			return false;
		}
		
		/* Check we have at least one compartment. */
		if ( this._compartments.isEmpty() )
		{
			Log.out(Tier.CRITICAL,"No compartment(s) specified!");
			return false;
		}
		
		/* If any compartments are not ready, then stop. */
		for ( Compartment c : this._compartments )
		{
			if ( ! c.isReadyForLaunch() )
			{
				Log.out(Tier.CRITICAL,"Compartment " + c.name + " not ready for"
						+ " launch!");
				return false;
			}
		}
		return true;
	}

	/**
	 * return the model node with all current, up to date state parameters, used
	 * for xml output and gui fields.
	 */
	@Override
	public ModelNode getNode() {
		/* create simulation node */
		ModelNode modelNode = new ModelNode(XmlLabel.simulation, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		
		Param.init();
		if(! Log.isSet())
			Log.set(Tier.NORMAL);
		
		/* add attributes */
		/* the current random seed */
		modelNode.add( new ModelAttribute(XmlLabel.seed,
				String.valueOf(seed()), null, true));
		
		/* the simulation name */
		modelNode.add( new ModelAttribute(XmlLabel.nameAttribute, 
				Idynomics.global.simulationName, null, false ));
		
		/* the output folder */
		modelNode.add(new ModelAttribute(XmlLabel.outputFolder, 
				Idynomics.global.outputRoot, null, false ));
		
		/* the log level */
		modelNode.add(new ModelAttribute(XmlLabel.logLevel, Log.level(), 
				Helper.enumToString(Tier.class).split(" "), false ));
		
		/* the optional comment */
		modelNode.add(new ModelAttribute(XmlLabel.commentAttribute, 
				Idynomics.global.simulationComment, null, true ));
		
		/* add timer node */
		modelNode.add(timer.getNode());
		
		/* add species lib */
		modelNode.add(speciesLibrary.getNode());

		/* add compartment nodes */
		for ( Compartment c : this._compartments )
			modelNode.add(c.getNode());
		
		/* add child constructor (adds add compartment button to gui */
		modelNode.childConstructors.put(new Compartment(), 
				ModelNode.Requirements.ZERO_TO_FEW);

		/* Safe this modelNode locally for model run without having to have save 
		 * all button */
		this._modelNode = modelNode;
		
		/* return node */
		return modelNode;
	}
	
	/**
	 * Additional setNode method for simulation, allows for emediate simulation
	 * kick-off
	 */
	public void setNode()
	{
		setNode(this._modelNode);
	}
	
	/**
	 * update current value's with the value's from the modelNode
	 */
	public void setNode(ModelNode node)
	{
		/* set local node */
		this._modelNode = node;
		
		/* update simulation name */
		Idynomics.global.simulationName = 
				node.getAttribute(XmlLabel.nameAttribute).value;
		
		/* update output root folder */
		Idynomics.global.outputRoot = 
				node.getAttribute(XmlLabel.outputFolder).value;
		
		/* set output level */
		Log.set(node.getAttribute(XmlLabel.logLevel).value);
		
		/* set random seed */
		seed(Long.valueOf(node.getAttribute(XmlLabel.seed).value));
		
		/* set value's for all child nodes */
		for(ModelNode n : node.childNodes)
			n.constructor.setNode(n);
	}

	/**
	 * Method is called when "add" button is hit in gui, options are set in
	 * child constructor hashMap of model node.
	 */
	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		if (childObject instanceof Compartment)
			this._compartments.add((Compartment) childObject);
	}

	/**
	 * the default xml tag of this object.
	 */
	@Override
	public String defaultXmlTag() {
		return XmlLabel.simulation;
	}
	
	/**
	 * returns an "empty" Node constructor object.
	 */
	public NodeConstructor newBlank()
	{
		return new Simulator();
	}

	@Override
	public String getXml() 
	{
		return this._modelNode.getXML();
	}
}

