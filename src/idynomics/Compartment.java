package idynomics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import agent.Agent;
import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.Instantiatable;
import grid.*;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import processManager.ProcessComparator;
import processManager.ProcessManager;
import reaction.Reaction;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import spatialRegistry.TreeType;
import utility.Helper;
import shape.Dimension.DimName;

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
 */
public class Compartment implements CanPrelaunchCheck, Instantiatable, NodeConstructor
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
	 * ProcessComparator orders Process Managers by their time priority.
	 */
	protected ProcessComparator _procComp = new ProcessComparator();
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
	private NodeConstructor _parentNode;
	
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
	
	public NodeConstructor newBlank()
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
		Log.out(Tier.EXPRESSIVE, "Compartment \""+this.name+
				"\" taking shape \""+aShape.getName()+"\"");
		this._shape = aShape;
		this.environment = new EnvironmentContainer(this._shape);
		this.agents = new AgentContainer(this._shape);
	}
	
	/**
	 * \brief TODO
	 * FIXME only used by unit tests
	 * @param shapeName
	 */
	public void setShape(String shapeName)
	{
		Shape aShape = Shape.getNewInstance(shapeName);
		this.setShape(aShape);
	}
	
	/**
	 * \brief Initialise this {@code Compartment} from an XML node. 
	 * 
	 * TODO diffusivity
	 * @param xmlElem An XML element from a protocol file.
	 */
	public void init(Element xmlElem, NodeConstructor parent)
	{
		Tier level = Tier.EXPRESSIVE;
		/*
		 * Compartment initiation
		 */
		this.name = XmlHandler.obtainAttribute(
				xmlElem, XmlRef.nameAttribute, XmlRef.compartment);
		Idynomics.simulator.addCompartment(this);
		/*
		 * Set up the shape.
		 */
		Element elem = XmlHandler.loadUnique(xmlElem, XmlRef.compartmentShape);
		String str = XmlHandler.gatherAttribute(elem, XmlRef.classAttribute);
		this.setShape( (Shape) Shape.getNewInstance(
				str, elem, (NodeConstructor) this) );	
		/*
		 * set container parentNodes
		 */
		agents.setParent(this);
		environment.setParent(this);
		/*
		 * setup tree
		 */
		str = XmlHandler.gatherAttribute(xmlElem, XmlRef.tree);
		str = Helper.setIfNone(str, String.valueOf(TreeType.RTREE));
		this.agents.setSpatialTree(TreeType.valueOf(str));
		/*
		 * Load solutes.
		 */
		Log.out(level, "Compartment reading in solutes");
		for ( Element e : XmlHandler.getElements(xmlElem, XmlRef.solute))
			this.environment.addSolute( new SpatialGrid( e, this.environment) );
		/*
		 * Load extra-cellular reactions.
		 */
		Log.out(level, "Compartment reading in (environmental) reactions");
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.reaction) )
			this.environment.addReaction( new Reaction(	e, this.environment) );	
		/*
		 * Read in agents.
		 */
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.agent) )
			this.addAgent(new Agent( e, this ));
		Log.out(level, "Compartment "+this.name+" initialised with "+ 
				this.agents.getNumAllAgents()+" agents");
		/*
		 * Read in process managers.
		 */
		Log.out(level,"Compartment "+this.name+ " loading "+XmlHandler.
				getElements(xmlElem, XmlRef.process).size()+" processManagers");
		for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.process) )
			this.addProcessManager( (ProcessManager) 
					Instantiatable.getNewInstance( XmlHandler.obtainAttribute(
					e, XmlRef.classAttribute, XmlRef.classAttribute), e, this));
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
		aBoundary.init(this.environment, this.agents, this.name);
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
		this._processes.add(aProcessManager);
		aProcessManager.init(this.environment, this.agents, this.name);
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
		agent.setCompartment(null);
		this.agents.registerRemoveAgent(agent);
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
	 * STEPPING
	 * **********************************************************************/
	
	/**
	 * 
	 * @param compartments
	 */
	// TODO temporary work, still in progress!
	public void checkBoundaryConnections(List<Compartment> compartments)
	{
		for ( Boundary b : this._shape.getDisconnectedBoundaries() )
		{
			String name = b.getPartnerCompartmentName();
			Compartment comp = null;
			for ( Compartment c : compartments )
				if ( c.getName().equals(name) )
				{
					comp = c;
					break;
				}
			if ( comp == null )
			{
				// TODO safety
			}
			else
			{
				Boundary partner = b.makePartnerBoundary();
				comp.getShape().addOtherBoundary(partner);
			}
		}
	}
	
	/**
	 * \brief Do all inbound agent & solute transfers.
	 */
	public void preStep()
	{
		/*
		 * Ask all Agents waiting in boundary arrivals lounges to enter the
		 * compartment now.
		 */
		this.agents.agentsArrive();
		/*
		 * Ask all boundaries to update their solute concentrations.
		 */
		this.environment.updateSoluteBoundaries();
	}
	
	/**
	 * \brief Iterate over the process managers until the local time would
	 * exceed the global time step.
	 */
	public void step()
	{
		// TODO temporary fix, reassess
		this._localTime = Idynomics.simulator.timer.getCurrentTime();
		
		if ( this._processes.isEmpty() )
			return;
		ProcessManager currentProcess = this._processes.getFirst();
		while ( (this._localTime = currentProcess.getTimeForNextStep() ) 
					< Idynomics.simulator.timer.getEndOfCurrentIteration() )
		{
			Log.out(Tier.EXPRESSIVE, "");
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+
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
			Collections.sort(this._processes, this._procComp);
			/*
			 * Choose the new first process for the next iteration.
			 */
			currentProcess = this._processes.getFirst();
		}
	}
	
	/**
	 * \brief Do all outbound agent & solute transfers.
	 */
	public void postStep()
	{
		/*
		 * Boundaries grab the agents they want, settling any conflicts between
		 * boundaries.
		 */
		this.agents.boundariesGrabAgents();
		/*
		 * Tell all agents queued to leave the compartment to move now.
		 */
		this.agents.agentsDepart();
		/*
		 * 
		 */
		// TODO update concentrations again?
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
			out.put(pm.getName(), pm.getRealTimeTaken());
		return out;
	}
	
	/* ***********************************************************************
	 * Model Node factory
	 * **********************************************************************/
	
	@Override
	public ModelNode getNode()
	{
		/* The compartment node. */
		ModelNode modelNode = new ModelNode(XmlRef.compartment, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		/* Set title for GUI. */
		if ( this.getName() != null )
			modelNode.setTitle(this.getName());
		/* Add the name attribute. */
		modelNode.add( new ModelAttribute(XmlRef.nameAttribute, 
				this.getName(), null, true ) );
		/* Add the shape if it exists. */
		if ( this._shape != null )
			modelNode.add( this._shape.getNode() );
		/* Add the Environment node. */
		modelNode.add( this.environment.getNode() );
		/* Add the Agents node. */
		modelNode.add( this.agents.getNode() );
		/* Add the process managers node. */
		modelNode.add( this.getProcessNode() );
				
		/* spatial registry NOTE we are handling this here since the agent
		 * container does not have the proper init infrastructure */
		modelNode.add( new ModelAttribute(XmlRef.tree, 
				String.valueOf( this.agents.getSpatialTree() ) , 
				Helper.enumToStringArray( TreeType.class ), false ) );

		return modelNode;	
	}
	
	/**
	 * \brief Helper method for {@link #getNode()}.
	 * 
	 * @return Model node for the <b>process managers</b>.
	 */
	private ModelNode getProcessNode()
	{
		/* The process managers node. */
		ModelNode modelNode = new ModelNode( XmlRef.processManagers, this );
		modelNode.setRequirements( Requirements.EXACTLY_ONE );
		/* 
		 * Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  
		 */
		modelNode.addConstructable( ClassRef.processManager, 
				Helper.collectionToArray( ProcessManager.getAllOptions() ), 
				ModelNode.Requirements.ZERO_TO_MANY );
		
		/* Add existing process managers as child nodes. */
		for ( ProcessManager p : this._processes )
			modelNode.add( p.getNode() );
		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) 
	{
		/* Set the modelNode for compartment. */
		if ( node.isTag(this.defaultXmlTag()) )
		{
			/* Update the name. */
			this.name = node.getAttribute( XmlRef.nameAttribute ).getValue();
			
			/* set the tree type */
			String tree = node.getAttribute( XmlRef.tree ).getValue();
			if ( ! Helper.isNone( tree ) )
				this.agents.setSpatialTree( TreeType.valueOf( tree ) );
		}
		/* 
		 * Set the child nodes.
		 * Agents, process managers and solutes are container nodes: only
		 * child nodes need to be set here.
		 */
		NodeConstructor.super.setNode(node);
	}
	
	public void removeNode(String specifier)
	{
		Idynomics.simulator.removeChildNode(this);
	}
	
	public void removeChildNode(NodeConstructor child)
	{
		if (child instanceof Shape)
		{
			this.setShape( (Shape) Shape.getNewInstance(
					null, null, (NodeConstructor) this) );
			// FIXME also remove solutes, spatial grids would be incompatible 
			// with a new shape
		}
		if (child instanceof ProcessManager)
			this._processes.remove((ProcessManager) child);
	}
	
	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		/* Set the shape. */
		if ( childObject instanceof Shape)
			this.setShape( (Shape) childObject); 
		/* Add processManagers. */
		if ( childObject instanceof ProcessManager)
			this.addProcessManager( (ProcessManager) childObject); 
		/*
		 * NOTE Agents register themselves to the compartment (register birth).
		 */
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.compartment;
	}

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public NodeConstructor getParent() 
	{
		return this._parentNode;
	}
}
