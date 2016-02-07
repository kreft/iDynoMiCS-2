package dataIO;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import reaction.Reaction;
import surface.Point;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.AspectReg;
import aspect.Calculated;
import aspect.Event;
import dataIO.Log.tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Param;
import idynomics.Simulator;
import idynomics.Timer;

/**
 * 
 * @author baco
 *
 */
public class XmlLoad {
	
	////////////////////////////
	// Methods
	////////////////////////////
	
	public static Object newInstance(String inPackage, String inClass)
	{
		Class<?> c;
		try {
			c = Class.forName(inPackage + "." + inClass);
			return c.newInstance();
		} catch (ClassNotFoundException e ){
			Log.out(tier.QUIET,"ERROR: the class " + inClass + " "
					+ "could not be found. Check the " + inPackage
					+ "package for the existence of this class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException e)  {
			Log.out(tier.QUIET,"ERROR: the class " + inClass + " "
					+ "could not be accesed or instantieated. Check whether the"
					+ " called class is valid.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * TODO: ProcessManager has no xml node constructor, quick fix
	 */
	public static ProcessManager constuctProcessManager(Node processNode)
	{
		Element p = (Element) processNode;
		
		ProcessManager process = (ProcessManager) newInstance("processManager", 
				p.getAttribute("manager"));
		process.setName(p.getAttribute("name"));
		process.setPriority(Integer.valueOf(p.getAttribute("priority")));
		process.setTimeForNextStep(Double.valueOf(p.getAttribute("firstStep")));
		process.setTimeStepSize(Timer.getTimeStepSize());
		return process;
	}
	
	/**
	 * TODO: compartmend has no xml node constructor, quick fix
	 */
	public static void constructCompartment(Node compartmentNode)
	{
		Simulator mySim = Idynomics.simulator;
		// NOTE: misses construction from xml, quick fix
		
		Element xmlCompartment = (Element) compartmentNode;
		Compartment comp = mySim.addCompartment(
				xmlCompartment.getAttribute("name"), 
				xmlCompartment.getAttribute("shape"));
		
		comp.setSideLengths( Vector.dblFromString( XmlHandler.obtainAttribute( 
				XmlHandler.loadUnique(xmlCompartment, "sideLengths"), "value")));
		
		// solutes, grids
		// TODO boundaries?? other stuff
		
		comp.init();
		
		/**
		 * Load agents and agent container
		 */
		Element agents = XmlHandler.loadUnique(Param.xmlDoc,"agents");
		NodeList agentNodes = agents.getElementsByTagName("agent");
		for (int j = 0; j < agentNodes.getLength(); j++) 
			comp.addAgent(new Agent(agentNodes.item(j)));
		
		/**
		 * Process managers
		 */
		Element processManagers = XmlHandler.loadUnique(Param.xmlDoc,"processManagers");
		NodeList processNodes = processManagers.getElementsByTagName("process");
		for (int j = 0; j < processNodes.getLength(); j++) 
		{
			comp.addProcessManager(constuctProcessManager(processNodes.item(j)));
		}
	}
	
	
	/**
	 * 
	 */
	public static void constructSimulation()
	{
		loadGeneralParameters();
		
		// NOTE: misses construction from xml, quick fix
		Timer.setTimeStepSize(Double.valueOf(Param.timeStepSize));
		Timer.setEndOfSimulation(Double.valueOf(Param.endOfSimulation));

		// NOTE: simulator now made by Idynomics class, may be changed later.
		
		Idynomics.simulator.speciesLibrary.setAll(XmlHandler.loadUnique(Param.xmlDoc, "speciesLib"));
		
		// cycle trough all compartments
		NodeList compartmentNodes = 
				Param.xmlDoc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			constructCompartment(compartmentNodes.item(i));
		}
	}

	/**
	 * Loads all states from xmlNode into anything that implements the
	 * StateObject interface.
	 * @param aspectReg
	 * @param xmlNode
	 */
	public static void loadStates(AspectInterface aspectInterface, Node xmlNode)
	{
		Element xmlAgent = (Element) xmlNode;
		@SuppressWarnings("unchecked")
		AspectReg<Object> aspectReg = (AspectReg<Object>) aspectInterface.registry();
		
		NodeList stateNodes = xmlAgent.getElementsByTagName("state");
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			if (! s.hasChildNodes())	// state node with just attributes //
			{
				switch (s.getAttribute("type")) 
				{
					case "boolean" : 
						aspectReg.add(s.getAttribute("name"), 
								Boolean.valueOf(s.getAttribute("value")));
	                	break;
					case "int" : 
						aspectReg.add(s.getAttribute("name"), 
								Integer.valueOf(s.getAttribute("value")));
	                	break;
					case "double" : 
						aspectReg.add(s.getAttribute("name"), 
								Double.valueOf(s.getAttribute("value")));
	                	break;
					case "String" : 
						aspectReg.add(s.getAttribute("name"), 
								s.getAttribute("value"));
	                	break;
					case "secondary" : 
						aspectReg.add(s.getAttribute("name"), 
								Calculated.getNewInstance(s));
	                	break;
					case "event" :
						aspectReg.add(s.getAttribute("name"), 
								Event.getNewInstance(s));
				}
			}
			else	// state node with attributes and child nodes //
			{
				switch (s.getAttribute("type")) 
				{
					case "body" :
						//FIXME: not finished only accounts for simple coccoids
						List<Point> pointList = new LinkedList<Point>();
						NodeList pointNodes = s.getElementsByTagName("point");
						for (int k = 0; k < pointNodes.getLength(); k++) 
						{
							Element point = (Element) pointNodes.item(k);
							pointList.add(new Point(Vector.dblFromString(
									point.getAttribute("position"))));
						}
						// Bas [01.02.16] TODO: currently only agents can have a
						// body, look into this if other things alos need to be
						// able to have a body
						aspectReg.add("body", new Body(pointList));
						break;
					case "reactions" :
						List<Reaction> reactions = new LinkedList<Reaction>();
						NodeList rNodes = s.getElementsByTagName("reaction");
						for (int k = 0; k < rNodes.getLength(); k++) 
						{
							Element reaction = (Element) rNodes.item(k);
							reactions.add(new Reaction(
									reaction.getAttribute("somethingReact")));
						}
						aspectReg.add("reactions", reactions);
						break;
				}
			}
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
			species.registry().addSubModule(s.getAttribute("name"), Idynomics.simulator.speciesLibrary);
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
		Element sim = XmlHandler.loadUnique((Element) Param.xmlDoc, "simulation");
		Param.simulationName = XmlHandler.obtainAttribute(sim, "name");
		Param.outputRoot = XmlHandler.obtainAttribute(sim, "outputfolder");
		Param.outputLocation = Param.outputRoot + "/" + Param.simulationName + "/";
		Log.set(XmlHandler.obtainAttribute(sim,"log"));
		
		Param.simulationComment = XmlHandler.gatherAttribute(sim,"comment");
	}
	
	/**
	 * loads all general parameters
	 */
	public static void loadGeneralParameters()
	{
		NodeList general = Param.xmlDoc.getElementsByTagName("general");
		for (int i = 0; i < general.getLength(); i++) 
		{
			Element xmlgeneral = (Element) general.item(i);
			NodeList paramNodes = xmlgeneral.getElementsByTagName("param");
			for (int j = 0; j < paramNodes.getLength(); j++) 
			{
				Element s = (Element) paramNodes.item(j);
				try {
					Class<?> c = Param.class;
					Field f = c.getDeclaredField(s.getAttribute("name"));
					f.set(f, s.getAttribute("value"));
	
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// NOTE: do not log this since this may occur before the log
					// is initiated (the log it self is start from a general 
					// param
					System.err.println("Warning: attempting to set non existend"
							+ " general paramater: " + s.getAttribute("name") );
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	



}
