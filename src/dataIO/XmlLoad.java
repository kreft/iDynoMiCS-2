package dataIO;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import linearAlgebra.Vector;
import processManager.ProcessManager;
import utility.Helper;
import agent.Agent;
import aspect.AspectInterface;
import dataIO.Log.tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Param;
import idynomics.Timer;


/**
 * 
 * @author baco
 *
 */
public class XmlLoad
{
	
	////////////////////////////
	// Methods
	////////////////////////////
		
	/**
	 * TODO: compartment has no xml node constructor, quick fix
	 */
	public static void constructCompartment(Node compartmentNode)
	{
		// NOTE: misses construction from xml, quick fix
		
		Element xmlCompartment = (Element) compartmentNode;
		/*
		 * Find the name of this compartment and its shape.
		 */
		String compName = xmlCompartment.getAttribute("name");
		compName = Helper.obtainInput(compName, "comparment name");
		Compartment comp = Idynomics.simulator.addCompartment(compName);
		comp.init(xmlCompartment);
		
		/**
		 * Load agents and agent container
		 */
		Element agents = XmlHandler.loadUnique(Param.xmlDoc,"agents");
		if(agents != null)
		{
			NodeList agentNodes = agents.getElementsByTagName("agent");
			for (int j = 0; j < agentNodes.getLength(); j++) 
				comp.addAgent(new Agent(agentNodes.item(j)));
		}
		else
			Log.out(tier.NORMAL, "Warning: starting simulation without agents");
		
		/**
		 * Process managers
		 */
		Element processManagers = XmlHandler.loadUnique(
				Param.xmlDoc,"processManagers");
		if(processManagers != null)
		{
			NodeList pNodes = processManagers.getElementsByTagName("process");
			for (int j = 0; j < pNodes.getLength(); j++) 
			{
				comp.addProcessManager( ProcessManager.getNewInstance(
						pNodes.item(j)));
			}
		}
		else
		{
			Log.out(tier.CRITICAL, "Warning: attempt to start simulation"
					+ "without process managers, aborting..");
			Helper.abort(3000);
		}
	}
	
	
	/**
	 * build up simulation from xml file.
	 * NOTE: if you want to make changes to the iDynomics documents setup this
	 * is probably your starting point
	 */
	public static void constructSimulation()
	{
		loadGeneralParameters();
		
		// NOTE: misses construction from xml, quick fix
		Timer.setTimeStepSize(Double.valueOf( Helper.obtainInput( 
				Param.timeStepSize,"Timer time step size")));
		Timer.setEndOfSimulation( Double.valueOf( Helper.obtainInput(
				Param.endOfSimulation,"End of simulation")));

		// NOTE: simulator now made by Idynomics class, may be changed later.
		
		Idynomics.simulator.speciesLibrary.setAll( XmlHandler.loadUnique(
				Param.xmlDoc, "speciesLib"));
		
		// cycle trough all compartments
		NodeList compartmentNodes = 
				Param.xmlDoc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			constructCompartment(compartmentNodes.item(i));
		}
	}


	
	/**
	 * Load speciesModules is used to obtain all speciesModules from an XML node
	 * and load the corresponding speciesModules into the speciesModules List of
	 * the Species.
	 *  
	 * @param species
	 * @param xmlNode
	 */
	public static void loadSpeciesModules(AspectInterface species, Node xmlNode)
	{
		Element xmlSpecies = (Element) xmlNode;
		
		NodeList nodes = xmlSpecies.getElementsByTagName("speciesModule");
		for (int j = 0; j < nodes.getLength(); j++) 
		{
			Element s = (Element) nodes.item(j);
			
			/**
			 * add a species module to be incorporated in this species
			 * FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
			 * species here.
			 * @param name
			 */
			species.reg().addSubModule(s.getAttribute("name"), 
					Idynomics.simulator.speciesLibrary);
		}
	}
	
	/**
	 * Used to initiate simulation from protocol file, sets the xml document and
	 * loads the first essential information
	 * @param xmlNode
	 */
	public static void xmlInit(String document)
	{
		/*
		 * This method contains System.err in stead of normal logging since it
		 * is called before logging is initiated.
		 */
		Param.xmlDoc = XmlHandler.loadDocument(Param.protocolFile);
		Element sim = XmlHandler.loadUnique((Element) Param.xmlDoc, 
				"simulation");
		Param.simulationName = XmlHandler.obtainAttribute(sim, "name");
		Param.outputRoot = XmlHandler.obtainAttribute(sim, "outputfolder");
		Param.outputLocation = Param.outputRoot + "/" + Param.simulationName + 
				"/";
		Log.set(XmlHandler.obtainAttribute(sim,"log"));
		Param.simulationComment = XmlHandler.gatherAttribute(sim,"comment");
	}
	
	/**
	 * loads all general parameters
	 */
	public static void loadGeneralParameters()
	{
		NodeList general = XmlHandler.getAll(Param.xmlDoc,"general");
		for (int i = 0; i < general.getLength(); i++) 
		{
			NodeList paramNodes = XmlHandler.getAll(general.item(i),"param");
			for (int j = 0; j < paramNodes.getLength(); j++) 
			{
				Element s = (Element) paramNodes.item(j);
				XmlHandler.setStaticField(Param.class, s);
			}
		}
	}
}
