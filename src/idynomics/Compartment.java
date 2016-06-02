package idynomics;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.*;
import grid.SpatialGrid.ArrayType;
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
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 */
public class Compartment implements CanPrelaunchCheck, XMLable, NodeConstructor
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
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
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
		Element elem;
		String str = null;
		/*
		 * Set up the shape.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlLabel.compartmentShape);
		str = XmlHandler.obtainAttribute(elem, XmlLabel.classAttribute);
		this.setShape( (Shape) Shape.getNewInstance(str) );
		this._shape.init( elem );
		
		/*
		 * Give it solutes.
		 * NOTE: wouldn't we want to pass initial grid values to? It would also
		 * be possible for the grids to be Xmlable
		 */
		NodeList solutes = XmlHandler.getAll(xmlElem, XmlLabel.solute);
		
		for ( int i = 0; i < solutes.getLength(); i++)
		{
			Element soluteE = (Element) solutes.item(i);
			String soluteName = XmlHandler.obtainAttribute(soluteE, 
					XmlLabel.nameAttribute);
			String conc = XmlHandler.obtainAttribute((Element) solutes.item(i), 
					XmlLabel.concentration);
			this.addSolute(soluteName);
			this.getSolute(soluteName).setTo(ArrayType.CONCN, conc);
			

			SpatialGrid myGrid = this.getSolute(str);
			NodeList voxelvalues = XmlHandler.getAll(solutes.item(i), 
					XmlLabel.voxel);
			for (int j = 0; j < voxelvalues.getLength(); j++)
			{
				myGrid.setValueAt(ArrayType.CONCN, Vector.intFromString(
						XmlHandler.obtainAttribute((Element) voxelvalues.item(j)
						, XmlLabel.coordinates) ) , Double.valueOf( XmlHandler
						.obtainAttribute((Element) voxelvalues.item(j), 
						XmlLabel.valueAttribute) ));
			}
		}
			
			// TODO diffusivity
			// TODO initial value
		
		/*
		 * Give it extracellular reactions.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlLabel.reactions);
		Element rElem;
		Reaction reac;
		if ( elem != null )
		{
			NodeList reactions = XmlHandler.getAll(elem, XmlLabel.reaction);
			for ( int i = 0; i < reactions.getLength(); i++ )
			{
				rElem = (Element) reactions.item(i);
				/* Name of the solute, e.g. glucose */
				str = XmlHandler.obtainAttribute(rElem, XmlLabel.nameAttribute);
				/* Construct and intialise the reaction. */
				reac = (Reaction) Reaction.getNewInstance(rElem);
				reac.init(rElem);
				/* Add it to the environment. */
				this.environment.addReaction(reac, str);
			}
				
		}
		/*
		 * Read in agents.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlLabel.agents);
		if ( elem == null )
		{
			Log.out(Tier.EXPRESSIVE,
					"Compartment "+this.name+" initialised without agents");
		}
		else
		{
			NodeList agents = elem.getElementsByTagName(XmlLabel.agent);
			this.agents.readAgents(agents, this);
			this.agents.setAllAgentsCompartment(this);
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+
							" initialised with "+agents.getLength()+" agents");
			
		}
		/*
		 * Read in process managers.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlLabel.processManagers);
		Element procElem;
		if ( elem == null )
		{
			Log.out(Tier.CRITICAL, "Compartment "+this.name+
									" initialised without process managers");
		}
		else
		{
			
			NodeList processNodes = elem.getElementsByTagName(XmlLabel.process);
			Log.out(Tier.EXPRESSIVE, "Compartment "+this.name+
									" initialised with process managers:");
			for ( int i = 0; i < processNodes.getLength(); i++ )
			{
				procElem = (Element) processNodes.item(i);
				str = XmlHandler.gatherAttribute(procElem,
													XmlLabel.nameAttribute);
				Log.out(Tier.EXPRESSIVE, "\t"+str);
				this.addProcessManager(ProcessManager.getNewInstance(procElem) );
			}
		}
	}
		
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
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
	 * \brief TODO
	 * 
	 * @param dim
	 * @param index
	 * @param aBoundary
	 */
	public void addBoundary(DimName dim, int index, Boundary aBoundary)
	{
		this._shape.setBoundary(dim, index, aBoundary);
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
	public void addSolute(String soluteName, double initialConcentration)
	{
		this.environment.addSolute(soluteName, initialConcentration);
	}
	
	/**
	 * 
	 * @param Agent
	 */
	public void addAgent(Agent agent)
	{
		this.agents.addAgent(agent);
		agent.setCompartment(this);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agent
	 */
	public void killAgent(Agent agent)
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
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
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
	
	public void agentsArrive()
	{
		this.agents.agentsArrive();
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
			currentProcess.step(this.environment, this.agents);
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
	 * \brief Tell all agents queued to leave the {@code Compartment} to move
	 * now.
	 */
	public void pushAllOutboundAgents()
	{
		this.agents.agentsDepart();
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
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
	
	/**
	 * retrieve the current model node
	 */
	public ModelNode getNode()
	{

		/* the compartment node */
		ModelNode modelNode = new ModelNode(XmlLabel.compartment, this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;

		/* set title for gui interface */
		if (this.getName() != null)
			modelNode.title = this.getName();
		
		/* add the name attribute */
		modelNode.add( new ModelAttribute(XmlLabel.nameAttribute, 
				this.getName(), null, true ) );
		
		/* add the shape if it exists */
		if ( this._shape != null )
			modelNode.add( _shape.getNode() );
		
		/* Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  */
		modelNode.childConstructors.put(Shape.getNewInstance("Dimensionless"), 
				ModelNode.Requirements.EXACTLY_ONE);
		
		/* add the solutes node */
		modelNode.add(this.getSolutesNode() );
		
		/* add the agents node */
		modelNode.add(this.getAgentsNode() );
		
		/* add the process managers node */
		modelNode.add(this.getProcessNode() );
		
		return modelNode;	
	}
	
	/**
	 * retrieve the agents node, handled by compartment
	 * @return
	 */
	public ModelNode getAgentsNode()
	{
		/* the agents node */
		ModelNode modelNode = new ModelNode( XmlLabel.agents, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		
		/* add the agent childConstrutor, allows adding of additional agents */
		modelNode.childConstructors.put( new Agent(this), 
				ModelNode.Requirements.ZERO_TO_MANY );
		
		/* if there are agents, add them as child nodes */
		if( this.agents != null )
			for ( Agent a : this.agents.getAllAgents() )
				modelNode.add( a.getNode() );
		
		return modelNode;
	}
	
	/**
	 * retrieve the process manager node, handled by compartment
	 * @return
	 */
	public ModelNode getProcessNode()
	{
		/* the process managers node */
		ModelNode modelNode = new ModelNode(XmlLabel.processManagers, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		
		/* 
		 * Work around: we need an object in order to call the newBlank method
		 * from TODO investigate a cleaner way of doing this  
		 */
		modelNode.childConstructors.put( ProcessManager.getNewInstance(
				"AgentGrowth"), ModelNode.Requirements.ZERO_TO_MANY);
		
		/* add existing process managers as child nodes */
		for ( ProcessManager p : this._processes )
			modelNode.add(p.getNode() );
		
		return modelNode;
	}
	
	/**
	 * retrieve the solutes node, handled by compartment
	 * @return
	 */
	public ModelNode getSolutesNode()
	{
		/* the solutes node */
		ModelNode modelNode = new ModelNode(XmlLabel.solutes, this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;

		/* 
		 * add solute nodes, yet only if the environment has been initiated, when
		 * creating a new compartment solutes can be added later 
		 */
		if ( this.environment != null )
			for ( String sol : this.environment.getSoluteNames() )
				modelNode.add(this.getSolute(sol).getNode() );
		
		return modelNode;
	}

	/**
	 * update any values that are newly set or modified
	 */
	public void setNode(ModelNode node) 
	{
		/* set the modelNode for compartment */
		if ( node.tag == defaultXmlTag() )
		{
			/* update the name */
			this.name = node.getAttribute( XmlLabel.nameAttribute ).value;
			
			/* set the child nodes */
			for( ModelNode n : node.childNodes )
				n.constructor.setNode(n);
		}
		else 
		{
			/* 
			 * agents, process managers and solutes are container nodes, only
			 * child nodes need to be set here
			 */
			for( ModelNode n : node.childNodes )
				n.constructor.setNode(n);
		}
	}
	
	/**
	 * Handle object added via the gui
	 */
	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		/* set the shape */
		if( childObject instanceof Shape)
			this.setShape( (Shape) childObject); 
		
		/* add processManagers */
		if( childObject instanceof ProcessManager)
			this.addProcessManager( (ProcessManager) childObject); 
		
		/* NOTE agents register themselves to the compartment (register birth) 
		*/
	}


	/**
	 * return the xml tag of this node constructor (compartment)
	 */
	@Override
	public String defaultXmlTag() 
	{
		return XmlLabel.compartment;
	}

	@Override
	public String getXml() 
	{
		return this.getNode().getXML();
	}
}
