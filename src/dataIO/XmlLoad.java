package dataIO;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import processManager.ProcessManager;
import utility.Helper;
import agent.Agent;
import aspect.AspectInterface;
import dataIO.Log.Tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Param;

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
	@Deprecated
	public static void constructCompartment(Node compartmentNode)
	{
		// NOTE: misses construction from xml, quick fix
		
		Element xmlCompartment = (Element) compartmentNode;
		/*
		 * Find the name of this compartment and its shape.
		 */
		String compName = xmlCompartment.getAttribute(XmlLabel.nameAttribute);
		compName = Helper.obtainInput(compName, "comparment name");
		Compartment comp = Idynomics.simulator.addCompartment(compName);
		comp.init(xmlCompartment);
		
		/**
		 * Load agents and agent container
		 */
		Element agents = XmlHandler.loadUnique(xmlCompartment,XmlLabel.agents);
		if(agents != null)
		{
			NodeList agentNodes = agents.getElementsByTagName(XmlLabel.agent);
			for (int j = 0; j < agentNodes.getLength(); j++) 
				comp.addAgent(new Agent(agentNodes.item(j),comp));
		}
		else
			Log.out(Tier.NORMAL, "Warning: starting simulation without agents");
		
		/**
		 * Process managers
		 */
		Element processManagers = XmlHandler.loadUnique(
				xmlCompartment,XmlLabel.processManagers);
		if(processManagers != null)
		{
			NodeList pNodes = processManagers.getElementsByTagName(
					XmlLabel.process);
			for (int j = 0; j < pNodes.getLength(); j++) 
			{
				comp.addProcessManager( ProcessManager.getNewInstance(
						pNodes.item(j)));
			}
		}
		else
		{
			Log.out(Tier.CRITICAL, "Warning: attempt to start simulation"
					+ "without process managers, aborting..");
			Helper.abort(3000);
		}
	}
	
	/**
	 * build up simulation from xml file.
	 * NOTE: if you want to make changes to the iDynomics documents setup this
	 * is probably your starting point
	 */
	@Deprecated
	public static void constructSimulation()
	{
		loadGeneralParameters();
		
		// NOTE: misses construction from xml, quick fix
		Idynomics.simulator.timer.setTimeStepSize(
				Double.valueOf( Helper.obtainInput( 
						Param.timeStepSize,"Timer time step size")));
		Idynomics.simulator.timer.setEndOfSimulation(
				Double.valueOf( Helper.obtainInput(
						Param.endOfSimulation,"End of simulation")));

		// NOTE: simulator now made by Idynomics class, may be changed later.
		
		if (XmlHandler.hasNode(Param.xmlDoc, XmlLabel.speciesLibrary))
			Idynomics.simulator.speciesLibrary.init( XmlHandler.loadUnique(
					Param.xmlDoc, XmlLabel.speciesLibrary));
		
		// cycle trough all compartments
		NodeList compartmentNodes = 
				Param.xmlDoc.getElementsByTagName(XmlLabel.compartment);
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
	@Deprecated
	public static void loadSpeciesModules(AspectInterface species, Node xmlNode)
	{
		Element xmlSpecies = (Element) xmlNode;
		
		NodeList nodes = xmlSpecies.getElementsByTagName(
				XmlLabel.speciesModule);
		for (int j = 0; j < nodes.getLength(); j++) 
		{
			Element s = (Element) nodes.item(j);
			
			/**
			 * add a species module to be incorporated in this species
			 * FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
			 * species here.
			 * @param name
			 */
			species.reg().addSubModule(s.getAttribute(XmlLabel.nameAttribute), 
					Idynomics.simulator.speciesLibrary);
		}
	}
	
	/**
	 * Used to initiate simulation from protocol file, sets the xml document and
	 * loads the first essential information
	 * @param xmlNode
	 */
	@Deprecated
	public static void xmlInit(String document)
	{
		/*
		 * This method contains System.err in stead of normal logging since it
		 * is called before logging is initiated.
		 */
		Param.xmlDoc = XmlHandler.loadDocument(Param.protocolFile);
		Element sim = XmlHandler.loadUnique((Element) Param.xmlDoc, 
				XmlLabel.simulation);
		Param.simulationName = XmlHandler.obtainAttribute(sim, 
				XmlLabel.nameAttribute);
		Param.outputRoot = XmlHandler.obtainAttribute(sim, 
				XmlLabel.outputFolder);
		Param.outputLocation = Param.outputRoot + "/" + Param.simulationName + 
				"/";
		Tier t = null;
		while (t == null) 
		{
			try
			{
				t = Tier.valueOf(XmlHandler.obtainAttribute(sim,
						XmlLabel.logLevel));
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("log level not recognized, use: " + 
						Helper.enumToString(Tier.class));
			}
		}
		if( ! Log.isSet() )
			Log.set(t);
		Param.simulationComment = XmlHandler.gatherAttribute(sim,
				XmlLabel.commentAttribute);
	}
	
	/**
	 * loads all general parameters
	 */
	@Deprecated
	public static void loadGeneralParameters()
	{
		NodeList general = XmlHandler.getAll(Param.xmlDoc,
				XmlLabel.generalParams);
		for (int i = 0; i < general.getLength(); i++) 
		{
			NodeList paramNodes = XmlHandler.getAll(general.item(i),
					XmlLabel.parameter);
			for (int j = 0; j < paramNodes.getLength(); j++) 
			{
				Element s = (Element) paramNodes.item(j);
				XmlHandler.setStaticField(Param.class, s);
			}
		}
	}
}
