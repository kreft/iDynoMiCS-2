package idynomics;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryConnected;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.*;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;
import processManager.ProcessComparator;
import processManager.ProcessManager;
import reaction.Reaction;
import shape.Shape;
import shape.ShapeConventions.DimName;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 */
public class Compartment implements CanPrelaunchCheck, IsSubmodel, XMLable
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
	protected double _localTime = Idynomics.simulator.timer.getCurrentTime();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment()
	{
		
	}
	
	public Compartment(Shape aShape)
	{
		this.setShape(aShape);
	}
	
	public Compartment(String aShapeName)
	{
		this((Shape) Shape.getNewInstance(aShapeName));
	}
	
	/**
	 * \brief
	 * 
	 * TODO This should go back to being private once tests are based on XML
	 * protocols.
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
	 * \brief Initialise this {@code Compartment} from an XML node. 
	 * 
	 * @param xmlElem An XML element from a protocol file.
	 */
	public void init(Element xmlElem)
	{
		Element elem;
		String str;
		/*
		 * Set up the shape.
		 */
		elem = XmlHandler.loadUnique(xmlElem, XmlLabel.compartmentShape);
		str = XmlHandler.gatherAttribute(elem, XmlLabel.classAttribute);
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
	}
	
	@Override
	public String getXml() {
		String out = "<" + XmlLabel.compartment + " " + XmlLabel.nameAttribute +
				"=\"" + this.name + "\">\n";
		out = out + this._shape.getXml();
		
		/* solutes, reactions */
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
		this._shape.setSurfaces();
		this._environment.init();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
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
	 * \brief Iterate over the process managers until the local time would
	 * exceed the global time step.
	 */
	public void step()
	{
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
		for ( BoundaryConnected b : this._shape.getConnectedBoundaries() )
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
	
	public Map<String, Class<?>> getParameters()
	{
		Map<String, Class<?>> out = new LinkedHashMap<String, Class<?>>();
		out.put(XmlLabel.nameAttribute, String.class);
		return out;
	}
	
	public void setParameter(String name, String value)
	{
		if ( name.equals(XmlLabel.nameAttribute) )
			this.name = value;
	}
	
	public List<SubmodelMaker> getSubmodelMakers()
	{
		List<SubmodelMaker> out = new LinkedList<SubmodelMaker>();
		/* We must have exactly one Shape. */
		out.add(new ShapeMaker(Requirement.EXACTLY_ONE));
		/* Any number of process managers is allowed, including none. */
		out.add(new ProcessMaker(Requirement.ZERO_TO_MANY));
		// TODO agents, solutes, diffusivity, reactions
		return out;
	}
	
	public class ShapeMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = 1486068039985317593L;
		
		public ShapeMaker(Requirement req)
		{
			super("shape", req);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String shapeName;
			if ( e == null )
				shapeName = "";
			else
				shapeName = e.getActionCommand();
			_shape = Shape.getNewInstance(shapeName);
			this.setLastMadeSubmodel(_shape);
		}
		
		public String[] getClassNameOptions()
		{
			return Shape.getAllOptions();
		}
	}
	
	public class ProcessMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -126858198160234919L;
		
		public ProcessMaker(Requirement req)
		{
			super("process manager", req);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ProcessManager newProcess =
					ProcessManager.getNewInstance(e.getActionCommand());
			
			_processes.add(newProcess);
			this.setLastMadeSubmodel(newProcess);
		}
	}
}
