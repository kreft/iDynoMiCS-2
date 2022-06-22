package compartment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import agent.Agent;

import bookkeeper.Bookkeeper;
import bookkeeper.KeeperEntry.EventType;
import boundary.Boundary;
import boundary.SpatialBoundary;
import compartment.agentStaging.Spawner;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import generalInterfaces.CanPrelaunchCheck;
import grid.SpatialGrid;
import idynomics.Global;
import idynomics.Idynomics;
import instantiable.Instance;
import instantiable.Instantiable;
import linearAlgebra.Orientation;
import linearAlgebra.Vector;
import physicalObject.PhysicalObject;
import processManager.ProcessArrival;
import processManager.ProcessDeparture;
import processManager.ProcessComparator;
import processManager.ProcessManager;
import reaction.RegularReaction;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.Dimension.DimName;
import shape.Shape;
import spatialRegistry.TreeType;
import utility.Helper;

/**
 * \brief TODO
 * 
 * <p>A compartment owns<ul>
 * <li>one shape</li>
 * <li>one environment container</li>
 * <li>one agent container</li>
 * <li>zero to many process managers</li></ul></p>
 * 
 * <p>The environment container and the agent container both have a reference
 * to the shape, but do not know about each other. Agent-environment
 * interactions must be mediated by a process manager. Each process manager has
 * a reference to the environment container and the agent container, and 
 * therefore can ask either of these about the compartment shape. It is
 * important though, that process managers do not have a reference to the
 * compartment they belong to: otherwise, a naive developer could have a
 * process manager call the {@code step()} method in {@code Compartment},
 * causing such chaos that even the thought of it keeps Rob awake at night.</p>
 * 
 * <p>In summary, the hierarchy of ownership is: shape -> agent/environment
 * containers -> process managers -> compartment. All the arrows point in the
 * same direction, meaning no entanglement of the kind iDynoMiCS 1 suffered.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */
public class Compartment implements CanPrelaunchCheck, Instantiable, Settable, Comparable<Compartment>
{
	/**
	 * This has a name for reporting purposes.
	 */
	public String name;
	/**
	 * Shape describes the geometry and size.
	 * 
	 * TODO also the resolution calculators?
	 */
	protected Shape _shape;
	/**
	 * Scaling factor determines the ratio of real to modelled size.
	 * All calculations for boundary movement will use this for scaling up from
	 * the modelled size to actual biofilm size.
	 */
	protected double _scalingFactor = 1.0;
	/**
	 * AgentContainer deals with all agents, whether they have spatial location
	 * or not.
	 */
	public AgentContainer agents;
	/**
	 * EnvironmentContainer deals with all solutes.
	 */
	public EnvironmentContainer environment;
	/**
	 * ProcessManagers handle the interactions between agents and solutes.
	 * The order of the list is important.
	 */
	protected LinkedList<ProcessManager> _processes = 
											new LinkedList<ProcessManager>();

	/**
	 * List of arrival processes, to be carried out of the beginning of a
	 * global timestep.
	 */
	protected LinkedList<ProcessManager> _arrivalProcesses =
			new LinkedList<ProcessManager>();

	/**
	 * List of departure processes, to be carried out at the end of a global
	 * timestep.
	 */
	protected LinkedList<ProcessManager> _departureProcesses =
			new LinkedList<ProcessManager>();

	/**
	 * ProcessComparator orders Process Managers by their time priority.
	 */
	protected ProcessComparator _procComp = new ProcessComparator();
	
	/**
	 * 
	 */
	public Bookkeeper _bookKeeper = new Bookkeeper();
	/**
	 * Local time should always be between {@code Timer.getCurrentTime()} and
	 * {@code Timer.getEndOfCurrentTime()}.
	 */	
	// TODO temporary fix, reassess
	//protected double _localTime = Idynomics.simulator.timer.getCurrentTime();
	protected double _localTime;
	
	/**
	 * the compartment parent node constructor (simulator)
	 */
	private Settable _parentNode;
	
	/**
	 * 
	 */
	private int _priority = Integer.MAX_VALUE;

	/**
	 * Collection of arrival nodes
	 */
	private LinkedList<ArrivalsLounge> _arrivals =
			new LinkedList<ArrivalsLounge>();

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public Compartment()
	{
		
	}
	
	public Compartment(String name)
	{
		this.name = name;
	}
	
	public void remove(Object object)
	{
		if ( object instanceof ProcessManager )
			this._processes.remove(object);
	}
	
	public Settable newBlank()
	{
		Compartment newComp = new Compartment();
		return newComp;
	}
	
	/**
	 * \brief
	 * 
	 * @param aShape
	 */
	public void setShape(Shape aShape)
	{
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, "Compartment \""+this.name+
					"\" taking shape \""+aShape.getName()+"\"");
		this._shape = aShape;
		this.environment = new EnvironmentContainer(this._shape);
		this.agents = new AgentContainer(this);
	}
	/**
	 * \brief Initialise this {@code Compartment} from an XML node. 
	 * 
	 * TODO diffusivity
	 * @param xmlElem An XML element from a protocol file.
	 */
	public void instantiate(Element xmlElem, Settable parent)
	{
		/*
		 * Compartment initiation
		 */
		this.name = XmlHandler.obtainAttribute(
				xmlElem, XmlRef.nameAttribute, XmlRef.compartment);
		if( XmlHandler.hasAttribute(xmlElem, XmlRef.priority))
			this._priority = Integer.valueOf( XmlHandler.gatherAttribute(
					xmlElem, XmlRef.priority) );
		Idynomics.simulator.addCompartment(this);
		/*
		 * Set up the shape.
		 */
		Element elem = XmlHandler.findUniqueChild(xmlElem, XmlRef.compartmentShape);
		String[] str = new String[] { XmlHandler.gatherAttribute(elem, XmlRef.classAttribute) };
		if ( str[0] == null )
			str = Shape.getAllOptions();
		this.setShape( (Shape) Instance.getNew(elem, this, str) );
		/* NOTE density scaling is done on individual basis {@Link 
		 * DensityScaling} so agents can maintain constant radius for 2D and 3D 
		 * cases. */
		
		double[] simulatedLengths = this.getShape().getDimensionLengths();
		// Check for scale attribute, specifying explicitly provided scale.
		String scalingFac = XmlHandler.gatherAttribute(xmlElem, XmlRef.compartmentScale);
		if ( !Helper.isNullOrEmpty(scalingFac) )
		{
			double scFac = Double.valueOf(scalingFac);
			this.setScalingFactor(scFac);
		}
		// Check for significant dimensions.
		else if (simulatedLengths.length != 0)
		{
			// Scaling factor not provided explicitly, calculate from realLengths
			Shape compShape = this.getShape();
			DimName dimN = compShape.getRealDimExtremeName();
			if ( Helper.isNullOrEmpty(compShape.getRealDimExtremeName()) )
			{
				double simulatedVolume = compShape.getTotalVolume();
				double realVolume = compShape.getTotalRealVolume();
				this.setScalingFactor(realVolume / simulatedVolume);
			}
			else
			{
				double simulatedArea = compShape.getBoundarySurfaceArea(dimN, 1);
				double realArea = compShape.getRealSurfaceArea(dimN, 1);
				this.setScalingFactor(realArea / simulatedArea);
			}
		}

		for( Boundary b : this._shape.getAllBoundaries())
		{
			b.setContainers(environment, agents);
			// FIXME trying to figure out how to get the well mixed region working,
			// quite funky investigate
//			if (b instanceof SpatialBoundary)
//				((SpatialBoundary) b).setLayerThickness(Double.valueOf(
//						XmlHandler.obtainAttribute(xmlElem, 
//						XmlRef.layerThickness, XmlRef.compartment)));
		}
		/*
		 * set container parentNodes
		 */
		agents.setParent(this);
		environment.setParent(this);
		/*
		 * setup tree
		 */
		String type = XmlHandler.gatherAttribute(xmlElem, XmlRef.tree);
		type = Helper.setIfNone(type, String.valueOf( agents.getSpatialTreeType() ));
		this.agents.setSpatialTreeType(TreeType.valueOf(type));
		/*
		 * Look for spawner elements
		 */
		Spawner spawner;
		TreeMap<Integer,Spawner> spawners = new TreeMap<Integer,Spawner>();
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.spawnNode) )
		{
			if ( e.hasAttribute( XmlRef.classAttribute ) )
			{
				spawner = (Spawner) Instance.getNew(e, this);
				/* check for duplicate priority */
				int priority = Helper.nextAvailableKey(
						spawner.getPriority(),spawners.keySet() );
				if (spawners.containsKey( spawner.getPriority() ))
				{
					if( Log.shouldWrite(Tier.EXPRESSIVE))
						Log.out(Tier.EXPRESSIVE, "WARNING: Spawner with "
								+ "duplicate priority next priority is picked "
								+ "by simulator.");
				}
				spawners.put(priority, spawner);
			}
		}
		/* verify whether this always returns in correct order (it should) */
		for( Spawner s : spawners.values() )
			s.spawn();
		
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, "Compartment " + this.name + 
					" initialised with " + this.agents.getNumAllAgents() +
					" agents.");

		/*
		 * Read in agents.
		 */
		Element agents = XmlHandler.findUniqueChild(xmlElem, XmlRef.agents);
		for ( Element e : XmlHandler.getElements( agents, XmlRef.agent) )
			this.addAgent(new Agent( e, this ));
		
		this.agents.update();
		
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+" initialised with "+ 
					this.agents.getNumAllAgents()+" agents");
		/*
		 * Load solutes.
		 */
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, "Compartment reading in solutes");
		Element solutes = XmlHandler.findUniqueChild(xmlElem, XmlRef.solutes);
		for ( Element e : XmlHandler.getElements(solutes, XmlRef.solute))
		{
			new SpatialGrid( e, this.environment);
		}
		/*
		 * Load extra-cellular reactions.
		 */
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, "Compartment reading in (environmental) reactions");
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.reaction) )
			new RegularReaction(e, this.environment);	
		/*
		 * Read in process managers.
		 */
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE,"Compartment "+this.name+ " loading "+XmlHandler.
					getElements(xmlElem, XmlRef.process).size()+" processManagers");
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.process) )
		{
			this.addProcessManager(
					(ProcessManager) Instance.getNew(e, this, (String[])null));
		}
		
		for ( Element e : XmlHandler.getElements(
				xmlElem, XmlRef.arrivalProcesses) )
		{
			this.addProcessManager(
					(ProcessManager) Instance.getNew(e, this, (String[])null));
		}

		for ( Element e : XmlHandler.getElements(
				xmlElem, XmlRef.departureProcesses) )
		{
			this.addProcessManager(
					(ProcessManager) Instance.getNew(e, this, (String[])null));
		}

		for ( Element e : XmlHandler.getElements(xmlElem,XmlRef.physicalObject))
		{
			this.addPhysicalObject( (PhysicalObject) Instance.getNew(e, this, 
							PhysicalObject.class.getName()));
		}
		
		/* NOTE: we fetch the class from the xml node */
		elem = XmlHandler.findUniqueChild(xmlElem, XmlRef.orientation);
		str = new String[] { XmlHandler.gatherAttribute(elem, XmlRef.variable) };
		if ( str[0] == null )
		{
			this.getShape().setOrientation( new Orientation( Vector.zerosDbl(
					this._shape.getNumberOfDimensions() ), this ) );
		} else {
			this.getShape().setOrientation(  (Orientation) Instance.getNew( elem, 
					this, Orientation.class.getName() ) );
		}

		/**
		 * Add the arrivals lounge(s)
		 */
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.arrivalsLounge) )
		{
			ArrivalsLounge a = new ArrivalsLounge(e, this);
			this._arrivals.add(a);
		}
	}
	
		
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	public String getName()
	{
		return this.name;
	}
	
	public Shape getShape()
	{
		return this._shape;
	}
	
	public boolean isDimensionless()
	{
		return this._shape.getNumberOfDimensions() == 0;
	}
	
	public int getNumDims()
	{
		return this._shape.getNumberOfDimensions();
	}
	
	public void setSideLengths(double[] sideLengths)
	{
		this._shape.setDimensionLengths(sideLengths);
	}
	
	/**
	 * \brief Add a boundary to this compartment's shape.
	 * 
	 * @param aBoundary Any boundary, whether spatial or non-spatial.
	 */
	// TODO move this spatial/non-spatial splitting to Shape?
	public void addBoundary(Boundary aBoundary)
	{
		aBoundary.setContainers(this.environment, this.agents);
		if ( aBoundary instanceof SpatialBoundary )
		{
			SpatialBoundary sB = (SpatialBoundary) aBoundary;
			DimName dim = sB.getDimName();
			int extreme = sB.getExtreme();
			this._shape.setBoundary(dim, extreme, sB);
		}
		else
			this._shape.addOtherBoundary(aBoundary);
	}
	
	/**
	 * \brief Add the given {@code ProcessManager} to the list, making sure
	 * that it is in the correct place.
	 * 
	 * @param aProcessManager
	 */
	public void addProcessManager(ProcessManager aProcessManager)
	{
		if (aProcessManager instanceof ProcessArrival)
		{
			this._arrivalProcesses.add((ProcessArrival) aProcessManager);
			Collections.sort(this._arrivalProcesses, this._procComp);
		}
		else if (aProcessManager instanceof ProcessDeparture)
		{
			this._departureProcesses.add((ProcessDeparture) aProcessManager);
			Collections.sort(this._departureProcesses, this._procComp);
		}
		else
		{
			this._processes.add(aProcessManager);
			Collections.sort(this._processes, this._procComp);
		}
		// TODO Rob [18Apr2016]: Check if the process's next time step is 
		// earlier than the current time.
		Collections.sort(this._processes, this._procComp);
	}
	
	/**
	 * \brief Add the given agent to this compartment.
	 * 
	 * @param Agent Agent to add.
	 */
	public void addAgent(Agent agent)
	{
		this.agents.addAgent(agent);
		agent.setCompartment(this);
		if( Global.bookkeeping )
			registerBook(EventType.ARRIVE, "Arrive", 
					String.valueOf(agent.identity()), null, agent);
	}
	
	public void addPhysicalObject(PhysicalObject p)
	{
		this.agents._physicalObjects.add(p);
	}
	
	public void addReaction(RegularReaction reaction)
	{
		this.environment.addReaction(reaction);
	}
	
	public void addSolute(SpatialGrid solute)
	{
		this.environment.addSolute(solute);
	}
	
	public void registerBook(EventType eventType, String event,
			String identity, String value, Settable storedSettable)
	{
		this._bookKeeper.register(eventType, event,
			identity, value, storedSettable);
	}
	
	/**
	 * \brief Remove the given agent from this compartment, registering its
	 * removal.
	 * 
	 * <p>This should be used only removal from the entire simulation, and not
	 * for transfer to another compartment. For example, cell lysis.</p>
	 * 
	 * @param agent Agent to remove.
	 */
	public void registerRemoveAgent(Agent agent)
	{
		// !!!! method only used from gui
		agent.setCompartment(null);
		this.agents.registerRemoveAgent(agent, EventType.REMOVED, "gui remove", null);
	}
	
	/**
	 * \brief Get the {@code SpatialGrid} for the given solute name.
	 * 
	 * @param soluteName {@code String} name of the solute required.
	 * @return The {@code SpatialGrid} for that solute, or {@code null} if it
	 * does not exist.
	 */
	public SpatialGrid getSolute(String soluteName)
	{
		return this.environment.getSoluteGrid(soluteName);
	}
	
	/* ***********************************************************************
	 * AGENT TRANSFER
	 * **********************************************************************/

	/**
	 * This method accepts new agents into an arrival lounge. It is called by a
	 * departure method.
	 * @param agents
	 */
	public void acceptAgents(String origin, LinkedList<Agent> agents)
	{
		//Loop through existing arrivals lounges. If a matching one is found,
		//agents will be added and the method will return.
		for (ArrivalsLounge a : this._arrivals)
		{
			if (a.getOrigin().contentEquals(origin))
			{
				a.addAgents(agents);
				return;
			}
		}

		//If the above loop found no arrivals lounge matching the origin string,
		//create a new one.
		ArrivalsLounge a = new ArrivalsLounge(origin, this);
		this._arrivals.add(a);
		a.addAgents(agents);
	}

	/**
	 * This method gets a list of arriving agents that originated in a
	 * particular compartment. It is typically called by arrival processes.
	 * @param originatingCompartment
	 * @return
	 */
	public LinkedList<Agent> getArrivals (String origin)
	{
		LinkedList<Agent> arrivals = new LinkedList<Agent>();
		for (ArrivalsLounge a : this._arrivals)
		{
			if (a.getOrigin().contentEquals(origin))
			{
				arrivals.addAll(a.getAgents());
				a.clear();
				return arrivals;
			}
		}

		//Return empty list if no departures have been received from the origin
		//in the last timestep.
		return arrivals;

	}



	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	public void runProcesses(LinkedList<ProcessManager> processes)
	{
		ProcessManager currentProcess = processes.getFirst();
		while ( (this._localTime = currentProcess.getTimeForNextStep() )
					< Idynomics.simulator.timer.getEndOfCurrentIteration() &&
					Idynomics.simulator.active() )
		{
			if( Log.shouldWrite(Tier.DEBUG) )
				Log.out(Tier.DEBUG, "Compartment "+this.name+
									" running process "+currentProcess.getName()+
									" at local time "+this._localTime);

			/*
			 * First process on the list does its thing. This should then
			 * increase its next step time.
			 */
			currentProcess.step();
			/*
			 * Reinsert this process at the appropriate position in the list.
			 */
			Collections.sort(processes, this._procComp);
			/*
			 * Choose the new first process for the next iteration.
			 */
			currentProcess = processes.getFirst();
		}
	}

	/**
	 * \brief Connects any disconnected boundaries to a new partner boundary on 
	 * the appropriate compartment.
	 * 
	 * Note that generation of spatial boundaries by this method is not yet
	 * possible. It is therefore necessary to specify spatial boundaries and to
	 * omit their partners in the protocol file.
	 * @param compartments List of compartments to choose from.
	 */
	public void checkBoundaryConnections(Collection<Compartment> compartments)
	{
		List<String> compartmentNames = new LinkedList<String>();
		for ( Compartment c : compartments )
			compartmentNames.add(c.getName());
		
		for ( Boundary b : this._shape.getDisconnectedBoundaries() )
		{
			String name = b.getPartnerCompartmentName();
			Compartment comp = findByName(compartments, name);
			while ( comp == null )
			{
				if( Log.shouldWrite(Tier.CRITICAL) )
					Log.out(Tier.CRITICAL, 
							"Cannot connect boundary " + b.getName() +
							" in compartment " + this.getName());
				name = Helper.obtainInput(compartmentNames, 
						"Please choose a compartment:", true);
				comp = findByName(compartments, name);
			}
			if( Log.shouldWrite(Tier.EXPRESSIVE) )
				Log.out(Tier.EXPRESSIVE, 
						"Connecting boundary " + b.getName() +
						" to a partner boundary of type " + 
						b.getPartnerClass().toString() + " in compartment " +
						comp.getName());
			Boundary partner = b.makePartnerBoundary();
			comp.addBoundary(partner);
		}
	}
	
	public void checkAgentDeparture()
	{
		if (this._shape.getNumberOfDimensions() > 0)
		{
			if (this._departureProcesses.isEmpty())
			{
				Log.out(Tier.CRITICAL, "Warning: no departure process set.");
//				ProcessDeparture defaultDepartureProcess = (ProcessDeparture)
//						Instance.getNew(ClassRef.floatingAgentDeparture, null);
//				defaultDepartureProcess.set(
//						AspectRef.collisionSearchDistance,
//						Global.default_attachment_pull_distance);
//				defaultDepartureProcess.setPriority(0);
//				defaultDepartureProcess.setDepartureType(
//						ProcessDeparture.DepartureType.REMOVAL);
//				defaultDepartureProcess.setShape(this._shape);
//				defaultDepartureProcess.setAgentContainer(this.agents);
//				defaultDepartureProcess.setTimeStep(
//						Idynomics.simulator.timer.getTimeStepSize());
//				this._departureProcesses.add(defaultDepartureProcess);
			}
		}
	}

	/**
	 * \brief Do all inbound agent & solute transfers.
	 */
	public void preStep()
	{
		this._bookKeeper.clear();

		this._localTime = Idynomics.simulator.timer.getCurrentTime();

		if( Log.shouldWrite(Tier.DEBUG) )
		{
			Log.out(Tier.DEBUG, "Compartment "+this.name+
					" at local time "+this._localTime);
		}

		if ( !this._arrivalProcesses.isEmpty() )
			this.runProcesses(this._arrivalProcesses);
		else
		{
			// is there any default way to accept arriving agents?
			// if not there should be at least a warning
		}
		
		this.agents.sortLocatedAgents();
	}
	
	/**
	 * \brief Iterate over the process managers until the local time would
	 * exceed the global time step.
	 */
	public void step()
	{
		this._localTime = Idynomics.simulator.timer.getCurrentTime();
		

		if (! this._processes.isEmpty() )
			this.runProcesses(this._processes);
	}
	
	/**
	 * \brief Do all outbound agent & solute transfers.
	 */
	public void postStep()
	{
		/*
		 * Tell all agents queued to leave the compartment to move now.
		 */
		if (!this._departureProcesses.isEmpty())
			this.runProcesses(this._departureProcesses);
		
		if( Global.csv_bookkeeping)
			this.writeEventLog();
	}

	/**
	 * For stepping non-timebased processes (eg post processing).
	 * @param process
	 */
	public void process(String process)
	{
		for ( ProcessManager p : this._processes )
			if( p.getName().equals(process) )
				p.step();
	}

	/* ***********************************************************************
	 * PRE-LAUNCH CHECK
	 * **********************************************************************/
	
	public boolean isReadyForLaunch()
	{
		if ( this._shape == null )
		{
			Log.out(Tier.CRITICAL, "Compartment shape is undefined!");
			return false;
		}
		if ( ! this._shape.isReadyForLaunch() )
			return false;
		return true;
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public void printSoluteGrid(String soluteName)
	{
		this.environment.printSolute(soluteName);
	}
	
	public void printAllSoluteGrids()
	{
		this.environment.printAllSolutes();
	}
	
	/**
	 * @return TODO
	 */
	public Map<String,Long> getRealTimeStats()
	{
		Map<String,Long> out = new HashMap<String,Long>();
		for ( ProcessManager pm : this._processes )
		{
			if (out.containsKey(pm.getName()))
				out.put(pm.getName(), out.get(pm.getName()) +
						pm.getRealTimeTaken());
			else
				out.put(pm.getName(), pm.getRealTimeTaken());
		}
		return out;
	}
	
	public void writeEventLog()
	{
		this._bookKeeper.toFile(this.getName());
	}
	
	/* ***********************************************************************
	 * Model Node factory
	 * **********************************************************************/
	
	@Override
	public Module getModule()
	{
		/* The compartment node. */
		Module modelNode = new Module(XmlRef.compartment, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		/* Set title for GUI. */
		if ( this.getName() != null )
			modelNode.setTitle(this.getName());

		/* Add the name attribute. */
		modelNode.add( new Attribute(XmlRef.nameAttribute, 
				this.getName(), null, true ) );
		modelNode.add( new Attribute(XmlRef.priority, 
				String.valueOf(this._priority), null, true ) );
		
		modelNode.add( new Attribute(XmlRef.compartmentScale,
                String.valueOf(this.getScalingFactor()), null, true ) );
		/* Add the shape if it exists. */
		if ( this._shape != null )
			modelNode.add( this._shape.getModule() );
		/* Add the Environment node. */
		modelNode.add( this.environment.getModule() );
		/* Add the Agents node. */
		modelNode.add( this.agents.getModule() );
		/* Add the arrivals lounge(s) */
		if (!this._arrivals.isEmpty())
		{
			for (ArrivalsLounge a : this._arrivals)
			{
				modelNode.add( a.getModule() );
			}
		}
		/* Add the process managers nodes. */
		for (Module m : this.getProcessNodes())
			modelNode.add(m);
		
		modelNode.add( getObjectNode() );
		
		/* FIXME performs slow, investigate, opted for csv export for now */
		if( Global.xml_bookkeeping )
			modelNode.add( this._bookKeeper.getModule() );
		
		/* spatial registry NOTE we are handling this here since the agent
		 * container does not have the proper init infrastructure */
		modelNode.add( new Attribute(XmlRef.tree, 
				String.valueOf( this.agents.getSpatialTreeType() ) , 
				Helper.enumToStringArray( TreeType.class ), false ) );

		return modelNode;	
	}
	
	/**
	 * \brief Helper method for {@link #getModule()}.
	 * 
	 * @return Model nodes for the <b>process managers</b>.
	 */
	private LinkedList<Module> getProcessNodes()
	{
		/* The process managers node. */
		Module processNode = new Module( XmlRef.processManagers, this );
		processNode.setRequirements( Requirements.EXACTLY_ONE );

		Module arrivalNode = new Module( XmlRef.arrivalProcesses, this );
		arrivalNode.setRequirements( Requirements.EXACTLY_ONE );

		Module departureNode = new Module( XmlRef.departureProcesses, this );
		departureNode.setRequirements( Requirements.EXACTLY_ONE );
		/* 
		 * Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  
		 */
		processNode.addChildSpec( ClassRef.processManager,
				Helper.collectionToArray( ProcessManager.getAllOptions() ),
				Module.Requirements.ZERO_TO_MANY );

		arrivalNode.addChildSpec( ClassRef.processArrival,
				Helper.collectionToArray( ProcessManager.getAllOptions() ),
				Module.Requirements.ZERO_TO_MANY );

		departureNode.addChildSpec( ClassRef.processDeparture,
				Helper.collectionToArray( ProcessManager.getAllOptions() ), 
				Module.Requirements.ZERO_TO_MANY );
		
		/* Add existing process managers as child nodes. */
		for (ProcessManager p : this._arrivalProcesses)
			arrivalNode.add( p.getModule() );
		for (ProcessManager p : this._departureProcesses)
			departureNode.add(p.getModule());
		for (ProcessManager p : this._processes)
			processNode.add(p.getModule());

		LinkedList<Module> modules = new LinkedList<Module>();
		modules.add(processNode);
		modules.add(arrivalNode);
		modules.add(departureNode);

		return modules;
	}
	
	/**
	 * \brief Helper method for {@link #getModule()}.
	 * 
	 * @return Model node for the <b>process managers</b>.
	 */
	private Module getObjectNode()
	{
		/* The process managers node. */
		Module modelNode = new Module( XmlRef.objects, this );
		modelNode.setRequirements( Requirements.EXACTLY_ONE );
		/* 
		 * TODO add Object child spec
		 */
		
		/* Add existing process managers as child nodes. */
		for (PhysicalObject p : this.agents._physicalObjects )
			modelNode.add( p.getModule() );
		return modelNode;
	}

	@Override
	public void setModule(Module node) 
	{
		/* Set the modelNode for compartment. */
		if ( node.getTag().equals(this.defaultXmlTag()) )
		{
			/* Update the name. */
			this.name = node.getAttribute( XmlRef.nameAttribute ).getValue();
			this._priority = Integer.valueOf(node.getAttribute( 
					XmlRef.priority ).getValue() );
			
			/* set the tree type */
			String tree = node.getAttribute( XmlRef.tree ).getValue();
			if ( ! Helper.isNullOrEmpty( tree ) )
				this.agents.setSpatialTreeType( TreeType.valueOf( tree ) );
		}
		/* 
		 * Set the child nodes.
		 * Agents, process managers and solutes are container nodes: only
		 * child nodes need to be set here.
		 */
		Settable.super.setModule(node);
	}
	
	public void removeModule(String specifier)
	{
		Idynomics.simulator.removeCompartment(this);
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.compartment;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
	
	/* ***********************************************************************
	 * Helper methods
	 * **********************************************************************/
	
	public static Compartment findByName(
			Collection<Compartment> compartments, String name)
	{
		for ( Compartment c : compartments )
			if ( c.getName().equals(name) )
				return c;
		return null;
	}
	
	public void setScalingFactor(double scFac)
	{
		this._scalingFactor = scFac;
	}
	
	public double getScalingFactor()
	{
		return this._scalingFactor;
	}

	@Override
	public int compareTo(Compartment o) 
	{
		int temp = this._priority - o._priority;
		if ( temp != 0 )
			return temp;
		else
			return this.name.compareTo(o.name);
	}
}
