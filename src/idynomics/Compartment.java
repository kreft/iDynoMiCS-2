package idynomics;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import dataIO.Log;
import dataIO.ObjectRef;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.*;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.ParameterSetter;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import processManager.ProcessComparator;
import processManager.ProcessManager;
import reaction.Reaction;
import shape.Shape;
import shape.Shape.ShapeMaker;
import shape.ShapeConventions.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 */
public class Compartment implements CanPrelaunchCheck, IsSubmodel, XMLable, NodeConstructor
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
	public EnvironmentContainer _environment;
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
	
	public ModelNode modelNode;
	
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
		this._shape = aShape;
		this._environment = new EnvironmentContainer(this._shape);
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
			String soluteName = XmlHandler.obtainAttribute(soluteE, XmlLabel.nameAttribute);
			double conc = Double.valueOf(
					XmlHandler.obtainAttribute((Element) solutes.item(i), 
					XmlLabel.concentration));
			this.addSolute(soluteName, conc, soluteE);
			
			// FIXME please provide standard methods to load entire solute grids
			SpatialGrid myGrid = this.getSolute(str);
			NodeList voxelvalues = XmlHandler.getAll(solutes.item(i), 
					XmlLabel.voxel);
			for (int j = 0; j < voxelvalues.getLength(); j++)
			{
				myGrid.setValueAt(ArrayType.CONCN, Vector.intFromString(
						XmlHandler.obtainAttribute((Element) voxelvalues.item(j)
						, XmlLabel.coordinates)) , Double.valueOf( XmlHandler
						.obtainAttribute((Element) voxelvalues.item(j), 
						XmlLabel.valueAttribute)));
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
				this._environment.addReaction(reac, str);
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
				this.addProcessManager(ProcessManager.getNewInstance(procElem));
			}
		}
		/*
		 * Finally, finish off the initialisation as standard.
		 */
		this.init();
		
		test.PolarGridTest.testMemoryAndIteratorSpeed(this._shape);
	}
	
	@Override
	public String getXml() {
		String out = "<" + XmlLabel.compartment + " " + XmlLabel.nameAttribute +
				"=\"" + this.name + "\">\n";
		out = out + this._shape.getXml();
		
		/* TODO solutes, reactions */
		out = out + this.agents.getXml();
		
		out = out + "<" + XmlLabel.processManagers + ">";
		for(ProcessManager p : this._processes)
		{
			out = out + "<" + XmlLabel.process + " " + XmlLabel.nameAttribute +
					"=\"" + p.getName() + "\" " + XmlLabel.classAttribute + 
					"=\"" + p.getClass().getSimpleName() + "\" " +
					XmlLabel.processPriority + "=\"" + p.getPriority() + "\" " +
					XmlLabel.processFirstStep + "=\"" + p.getTimeForNextStep() +
					"\" " + XmlLabel.packageAttribute + "=\"processManager.\"" +
					">\n"
					+ p.reg().getXml() + "</" + XmlLabel.process + ">\n";
		}
		out = out + "</" + XmlLabel.processManagers + ">\n";
		
		out = out + "</" + XmlLabel.compartment + ">\n";
		return out;
	}
	
	public void init()
	{
		
		this._environment.init();
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
		this._environment.addSolute(soluteName, null);
	}	
	
	/**
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName, Element resolution)
	{
		this._environment.addSolute(soluteName, resolution);
	}
	
	/**
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName, double initialConcentration, Element resolution)
	{
		this._environment.addSolute(soluteName, initialConcentration, resolution);
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
	 * \brief Get the {@code SpatialGrid} for the given solute name.
	 * 
	 * @param soluteName {@code String} name of the solute required.
	 * @return The {@code SpatialGrid} for that solute, or {@code null} if it
	 * does not exist.
	 */
	public SpatialGrid getSolute(String soluteName)
	{
		return this._environment.getSoluteGrid(soluteName);
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
		while ( (this._localTime = currentProcess.getTimeForNextStep()) 
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
			currentProcess.step(this._environment, this.agents);
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
		for ( Boundary b : this._shape.getAllBoundaries() )
			b.pushAllOutboundAgents();
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
		this._environment.printSolute(soluteName);
	}
	
	public void printAllSoluteGrids()
	{
		this._environment.printAllSolutes();
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
	public List<InputSetter> getRequiredInputs()
	{
		List<InputSetter> out = new LinkedList<InputSetter>();
		out.add(new ParameterSetter(XmlLabel.nameAttribute,this,ObjectRef.STR));
		/* We must have exactly one Shape. */
		out.add(new ShapeMaker(Requirement.EXACTLY_ONE, this));
		/* Any number of process managers is allowed, including none. */
		// TODO temporarily removed, reinstate
		//out.add(new ProcessMaker(Requirement.ZERO_TO_MANY, this));
		// TODO agents, solutes, diffusivity, reactions
		return out;
	}
	
	public void acceptInput(String name, Object input)
	{
		/* Parameters */
		if ( name.equals(XmlLabel.nameAttribute) )
			this.name = (String) input;
		/* Sub-models */
		if ( input instanceof Shape )
			this._shape = (Shape) input;
		if ( input instanceof ProcessManager )
			this._processes.add((ProcessManager) input);
	}
	
	public static class CompartmentMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -6545954286337098173L;
		
		public CompartmentMaker(Requirement req, IsSubmodel target)
		{
			super(XmlLabel.compartment, req, target);
		}
		
		@Override
		public void doAction(ActionEvent e)
		{
			this.addSubmodel(new Compartment());
		}
		
		@Override
		public Object getOptions()
		{
			return null;
		}
	}
	
	public ModelNode getNode()
	{
		if (modelNode == null)
		{
		ModelNode myNode = new ModelNode(XmlLabel.compartment, this);
		myNode.requirement = Requirements.ZERO_TO_FEW;
		
		myNode.add(new ModelAttribute(XmlLabel.nameAttribute, 
				this.getName(), null, true ));
		
		if ( this._shape !=null )
			myNode.add(_shape.getNode());
		
		// Work around
		myNode.childConstructors.put(Shape.getNewInstance("Dimensionless"), ModelNode.Requirements.EXACTLY_ONE);
		
		modelNode = myNode;
		}
		
		return modelNode;
		
	}

	public void setNode(ModelNode node) {
		this.name = node.getAttribute(XmlLabel.nameAttribute).value;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		if( childObject instanceof Shape)
			this.setShape((Shape) childObject); 
	}

	@Override
	public String defaultXmlTag() {
		return XmlLabel.compartment;
	}
}
