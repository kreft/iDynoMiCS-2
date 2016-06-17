package idynomics;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.Instantiatable;
import grid.*;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import processManager.ProcessComparator;
import processManager.ProcessManager;
import reaction.Reaction;
import shape.Shape;
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
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	 * 
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
	 * @param xmlElem An XML element from a protocol file.
	 */
	public void init(Element xmlElem)
	{
		Tier level = Tier.EXPRESSIVE;
		Element elem;
		String str = null;
		/*
		 * Set up the shape.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlRef.compartmentShape);
		str = XmlHandler.obtainAttribute(elem, XmlRef.classAttribute);
		this.setShape( (Shape) Shape.getNewInstance(str) );
		this._shape.init( elem );
		
		/*
		 * Give it solutes.
		 * NOTE: wouldn't we want to pass initial grid values to? It would also
		 * be possible for the grids to be Xmlable
		 */
		Log.out(level, "Compartment reading in solutes");
		NodeList solutes = XmlHandler.getAll(xmlElem, XmlRef.solute);
		for ( int i = 0; i < solutes.getLength(); i++)
		{
			Element soluteE = (Element) solutes.item(i);
			String soluteName = XmlHandler.obtainAttribute(soluteE, 
					XmlRef.nameAttribute);
			String conc = XmlHandler.obtainAttribute((Element) solutes.item(i), 
					XmlRef.concentration);
			this.addSolute(soluteName);
			this.getSolute(soluteName).setTo(ArrayType.CONCN, conc);
			

			SpatialGrid myGrid = this.getSolute(soluteName);
			NodeList voxelvalues = XmlHandler.getAll(solutes.item(i), 
					XmlRef.voxel);
			for (int j = 0; j < voxelvalues.getLength(); j++)
			{
				myGrid.setValueAt(ArrayType.CONCN, Vector.intFromString(
						XmlHandler.obtainAttribute((Element) voxelvalues.item(j)
						, XmlRef.coordinates) ) , Double.valueOf( XmlHandler
						.obtainAttribute((Element) voxelvalues.item(j), 
						XmlRef.valueAttribute) ));
			}
		}
			
			// TODO diffusivity
			// TODO initial value
		
		/*
		 * Give it extracellular reactions.
		 */
		Log.out(level, "Compartment reading in (environmental) reactions");
		elem = XmlHandler.loadUnique(xmlElem, XmlRef.reactions);
		Element rElem;
		Reaction reac;
		if ( elem != null )
		{
			NodeList reactions = XmlHandler.getAll(elem, XmlRef.reaction);
			for ( int i = 0; i < reactions.getLength(); i++ )
			{
				rElem = (Element) reactions.item(i);
				/* Construct and intialise the reaction. */
				reac = (Reaction) Reaction.getNewInstance(rElem);
				reac.init(rElem);
				/* Add it to the environment. */
				this.environment.addReaction(reac);
			}
				
		}
		/*
		 * Read in agents.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlRef.agents);
		if ( elem == null )
		{
			Log.out(Tier.EXPRESSIVE,
					"Compartment "+this.name+" initialised without agents");
		}
		else
		{
			NodeList agents = elem.getElementsByTagName(XmlRef.agent);
			this.agents.readAgents(agents, this);
			this.agents.setAllAgentsCompartment(this);
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+
							" initialised with "+agents.getLength()+" agents");
			
		}
		/*
		 * Read in process managers.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlRef.processManagers);
		Element procElem;
		if ( elem == null )
		{
			Log.out(Tier.CRITICAL, "Compartment "+this.name+
									" initialised without process managers");
		}
		else
		{
			ProcessManager pm;
			NodeList processNodes = elem.getElementsByTagName(XmlRef.process);
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+
									" initialised with process managers:");
			for ( int i = 0; i < processNodes.getLength(); i++ )
			{
				procElem = (Element) processNodes.item(i);
				str = XmlHandler.gatherAttribute(procElem,
													XmlRef.nameAttribute);
				Log.out(Tier.EXPRESSIVE, "\t"+str);
				pm = ProcessManager.getNewInstance(procElem, this.environment, 
						this.agents, this.getName());
				this.addProcessManager(pm);
			}
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
//		aProcessManager.setCompartment(this);
		this._processes.add(aProcessManager);
		// TODO Rob [18Apr2016]: Check if the process's next time step is 
		// earlier than the current time.
		Collections.sort(this._processes, this._procComp);
	}
	
	/**
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this.environment.addSolute(soluteName);
	}	
	
	/**
	 * 
	 * @param soluteName
	 */
	// TODO not used, consider deletion
	public void addSolute(String soluteName, double initialConcentration)
	{
		this.environment.addSolute(soluteName, initialConcentration);
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
	
	/*************************************************************************
	 * Model Node factory
	 ************************************************************************/
	
	@Override
	public ModelNode getNode()
	{
		/* The compartment node. */
		ModelNode modelNode = new ModelNode(XmlRef.compartment, this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;
		/* Set title for GUI. */
		if ( this.getName() != null )
			modelNode.title = this.getName();
		/* Add the name attribute. */
		modelNode.add( new ModelAttribute(XmlRef.nameAttribute, 
				this.getName(), null, true ) );
		/* Add the shape if it exists. */
		if ( this._shape != null )
			modelNode.add( this._shape.getNode() );
		/* Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  */
		// NOTE Rob [9June2016]: Surely we only need to do this if 
		// this._shape == null? Or have I completely misunderstood?
		modelNode.childConstructors.put(Shape.getNewInstance("Dimensionless"), 
				Requirements.EXACTLY_ONE);
		/* Add the solutes node. */
		modelNode.add( this.getSolutesNode() );
		/* Add the agents node. */
		modelNode.add( this.getAgentsNode() );
		/* Add the process managers node. */
		modelNode.add( this.getProcessNode() );
		return modelNode;	
	}
	
	/**
	 * \brief Helper method for {@link #getNode()}.
	 * 
	 * @return Model node for the <b>agents</b>.
	 */
	private ModelNode getAgentsNode()
	{
		/* The agents node. */
		ModelNode modelNode = new ModelNode( XmlRef.agents, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		/* Add the agent childConstrutor for adding of additional agents. */
		modelNode.childConstructors.put( new Agent(this), 
				Requirements.ZERO_TO_MANY );
		/* If there are agents, add them as child nodes. */
		if ( this.agents != null )
			for ( Agent a : this.agents.getAllAgents() )
				modelNode.add( a.getNode() );
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
		ModelNode modelNode = new ModelNode(XmlRef.processManagers, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		/* 
		 * Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  
		 */
		modelNode.childConstructors.put( ProcessManager.getNewInstance(
				"AgentGrowth"), Requirements.ZERO_TO_MANY);
		/* Add existing process managers as child nodes. */
		for ( ProcessManager p : this._processes )
			modelNode.add( p.getNode() );
		return modelNode;
	}
	
	/**
	 * \brief Helper method for {@link #getNode()}.
	 * 
	 * @return Model node for the <b>solutes</b>.
	 */
	private ModelNode getSolutesNode()
	{
		/* The solutes node. */
		ModelNode modelNode = new ModelNode(XmlRef.solutes, this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;
		/* 
		 * add solute nodes, yet only if the environment has been initiated, when
		 * creating a new compartment solutes can be added later 
		 */
		if ( this.environment != null )
			for ( String sol : this.environment.getSoluteNames() )
				modelNode.add( this.getSolute(sol).getNode() );
		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) 
	{
		/* Set the modelNode for compartment. */
		if ( node.tag == this.defaultXmlTag() )
		{
			/* Update the name. */
			this.name = node.getAttribute( XmlRef.nameAttribute ).value;
			/* Set the child nodes. */
			for( ModelNode n : node.childNodes )
				n.constructor.setNode(n);
		}
		else
		{
			/* 
			 * Agents, process managers and solutes are container nodes: only
			 * child nodes need to be set here.
			 */
			for ( ModelNode n : node.childNodes )
				n.constructor.setNode(n);
		}
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
}
