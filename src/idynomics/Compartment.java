package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryConnected;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.*;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import reaction.Reaction;
import shape.Shape;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 */
public class Compartment implements CanPrelaunchCheck, XMLable
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
	protected double _localTime = Timer.getCurrentTime();
	
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
	 * @param xmlNode An XML node from a protocol file.
	 */
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;

		this.setShape((Shape) Shape.getNewInstance(
				XmlHandler.attributeFromUniqueNode(elem, 
				XmlLabel.compartmentShape, XmlLabel.classAttribute)));
		this._shape.init(XmlHandler.loadUnique(elem, 
				XmlLabel.compartmentShape));
		
		/*
		 * Give it solutes.
		 * NOTE: wouldn't we want to pass initial grid values to? It would also
		 * be possible for the grids to be Xmlable
		 */
		NodeList solutes = XmlHandler.getAll(elem, XmlLabel.solute);
		for ( int i = 0; i < solutes.getLength(); i++)
		{
			String soluteName = XmlHandler.obtainAttribute((Element) 
					solutes.item(i), XmlLabel.nameAttribute);
			this.addSolute(soluteName, Double.valueOf(
					XmlHandler.obtainAttribute((Element) solutes.item(i), 
					XmlLabel.concentration)));
			
			// FIXME please provide standard methods to load entire solute grids
			SpatialGrid myGrid = this.getSolute(soluteName);
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
		Element reactionsElem = XmlHandler.loadUnique(elem, XmlLabel.reactions);
		if (reactionsElem != null)
		{
			NodeList reactions = XmlHandler.getAll(reactionsElem, 
					XmlLabel.reaction);
			for ( int i = 0; i < reactions.getLength(); i++ )
				this._environment.addReaction((Reaction) Reaction.getNewInstance(
						reactions.item(i)),XmlHandler.obtainAttribute(
						(Element) reactions.item(i), XmlLabel.nameAttribute));
		}
		/*
		 * Finally, finish off the initialisation as standard.
		 */
		this.init();
	}
	
	public void init()
	{
		/*
		 * NOTE: Bas [06.02.16] this may be set elsewhere as long as it is after
		 * the Dimensions and sideLengths are set.
		 * 
		 * NOTE: Rob [8Feb2016] here is fine (_environment also needs
		 * sideLengths, etc).
		 */
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
		this._environment.addSolute(soluteName);
	}
	
	/**
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName, double initialConcentration)
	{
		this._environment.addSolute(soluteName, initialConcentration);
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
	
	public SpatialGrid getSolute(String soluteName)
	{
		return this._environment.getSoluteGrid(soluteName);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * 
	 */
	public void step()
	{
		ProcessManager currentProcess = this._processes.getFirst();
		while ( (this._localTime = currentProcess.getTimeForNextStep()) < 
											Timer.getEndOfCurrentIteration() )
		{
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
	 * \brief Helper for ordering {@code ProcessManager}s 
	 */
	protected static class ProcessComparator 
										implements Comparator<ProcessManager>
	{
		@Override
		public int compare(ProcessManager pm1, ProcessManager pm2) 
		{
			Double temp = pm1.getTimeForNextStep() - pm2.getTimeForNextStep();
			if ( ExtraMath.areEqual(temp, 0.0, 1.0E-10) )
				return pm1.getPriority() - pm2.getPriority();
			else
				return temp.intValue();
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
			Log.out(tier.CRITICAL, "Compartment shape is undefined!");
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
}
