package dataIO;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import reaction.Reaction;
import surface.Point;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import agent.Agent;
import agent.AspectReg;
import agent.Body;
import agent.Species;
import agent.SpeciesLib;
import agent.AspectRegistry;
import agent.event.EventLoader;
import agent.state.StateLoader;
import dataIO.Feedback.LogLevel;
import generalInterfaces.AspectInterface;
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
	
	/////////////////////////////
	// Interfaces
	/////////////////////////////
	
	public interface nodeOperation {
		void action(Node node);
	}
	
	////////////////////////////
	// Local classes
	////////////////////////////
	
	////////////////////////////
	// Methods
	////////////////////////////
	
	public static Element loadDocument(String document)
	{
		try {
			File fXmlFile = new File(document);
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc.getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object newInstance(String inPackage, String inClass)
	{
		Class<?> c;
		try {
			c = Class.forName(inPackage + "." + inClass);
			return c.newInstance();
		} catch (ClassNotFoundException e ){
			Feedback.out(LogLevel.QUIET,"ERROR: the class " + inClass + " "
					+ "could not be found. Check the " + inPackage
					+ "package for the existence of this class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException e)  {
			Feedback.out(LogLevel.QUIET,"ERROR: the class " + inClass + " "
					+ "could not be accesed or instantieated. Check whether the"
					+ " called class is valid.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * TODO: ProcessManager has no xml node constructor, quick fix
	 */
	public static void constuctProcessManager(Node processNode, 
			Compartment compartment)
	{
		Element p = (Element) processNode;
		
		ProcessManager process = (ProcessManager) newInstance("processManager", 
				p.getAttribute("manager"));
		process.setName(p.getAttribute("name"));
		process.setPriority(Integer.valueOf(p.getAttribute("priority")));
		process.setTimeForNextStep(Double.valueOf(p.getAttribute("firstStep")));
		process.setTimeStepSize(Timer.getTimeStepSize());
		compartment.addProcessManager(process);
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
		
		comp.setSideLengths( Vector.dblFromString( obtainAttribute( 
				loadUnique(xmlCompartment, "sideLengths"), "value")));
		
		// solutes, grids
		// TODO boundaries?? other stuff
		
		comp.init();
		
		/**
		 * Load agents and agent container
		 */
		Element agents = loadUnique(Param.xmlDoc,"agents");
		NodeList agentNodes = agents.getElementsByTagName("agent");
		for (int j = 0; j < agentNodes.getLength(); j++) 
			comp.addAgent(new Agent(agentNodes.item(j)));
		
		/**
		 * Process managers
		 */
		Element processManagers = loadUnique(Param.xmlDoc,"processManagers");
		NodeList processNodes = processManagers.getElementsByTagName("process");
		for (int j = 0; j < processNodes.getLength(); j++) 
			constuctProcessManager(processNodes.item(j), comp);
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
		
		Idynomics.simulator.speciesLibrary.setAll(loadUnique(Param.xmlDoc, "speciesLib"));
		
		// cycle trough all compartments
		NodeList compartmentNodes = 
				Param.xmlDoc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			constructCompartment(compartmentNodes.item(i));
		}
	}

	/**
	 * Perform iterative operation for all nodes in nodeList
	 * @param nodeList
	 * @param operation
	 */
	public static void forAllNodes(NodeList nodeList, nodeOperation operation) 
	{
	    for (int i = 0; i < nodeList.getLength(); i++) 
		     operation.action(nodeList.item(i));
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
								StateLoader.getSecondary(s.getAttribute("value")
										, s.getAttribute("input")));
	                	break;
					case "event" :
						aspectReg.add(s.getAttribute("name"), 
								EventLoader.getEvent(s.getAttribute("value"), 
										s.getAttribute("input")));
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
		Param.xmlDoc = loadDocument(Param.protocolFile);
		Element sim = loadUnique((Element) Param.xmlDoc, "simulation");
		Param.simulationName = obtainAttribute(sim, "name");
		Param.outputRoot = obtainAttribute(sim, "outputfolder");
		Param.outputLocation = Param.outputRoot + "/" + Param.simulationName + "/";
		Feedback.set(obtainAttribute(sim,"log"));
		
		Param.simulationComment = gatherAttribute(sim,"comment");
	}
	
	/**
	 * Checks for unique node exists and whether it is unique, than returns it.
	 * @param xmlElement
	 * @param tagName
	 * @return
	 */
	public static Element loadUnique(Element xmlElement, String tagName)
	{
		NodeList nodes =  Param.xmlDoc.getElementsByTagName(tagName);
		if (nodes.getLength() > 1)
		{
			System.err.println("Warning: document contains more than 1"
					+ tagName + " nodes, loading first simulation node...");
		}
		else if (nodes.getLength() == 0)
		{
			System.err.println("Error: could not identify " + tagName + "node, "
					+ "make sure your file contains all required elements."
					+ "Aborting...");
			System.exit(0);
		}
		return (Element) nodes.item(0);
	}
	
	/**
	 * This method gets an attribute from an element, if the element does not
	 * have this attribute it will ask the user.
	 */
	public static String obtainAttribute(Element xmlElement, String attribute)
	{
		if(xmlElement.hasAttribute(attribute))
			return  xmlElement.getAttribute(attribute);
		else
		{
		@SuppressWarnings("resource")
		Scanner user_input = new Scanner( System.in );
		System.out.print(xmlElement.getNodeName() + " misses the "
				+ "attribute: \"" + attribute + "\", please enter a value: " );
		return user_input.next( );
		}
	}
	
	/**
	 * Gathers non critical attributes, returns "" if the attribute is not
	 * defined. This method does not ask the user for any information.
	 */
	public static String gatherAttribute(Element xmlElement, String attribute)
	{
		if(xmlElement.hasAttribute(attribute))
			return xmlElement.getAttribute(attribute);
		else
			return "";
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
	
	/*************************************************************************
	 * DISPLAYING
	 ************************************************************************/
	
	public static void display(Node node)
	{
		display(null, node);
	}
	
	public static void display(String prefix,Node node)
	{
		if (node.getNodeType() == Node.ELEMENT_NODE) 
			display(prefix, (Element) node);
	}
	
	public static void display(Element element)
	{
		display(null, element);
	}
	
	public static void display(String prefix, Element element)
	{
		String ln = " " + element.getTagName() + " " 
				+ element.getAttribute("name");
		if (prefix == null) 
			System.out.println(ln);
		else
			System.out.println(prefix + ln);
	}
	
	public static void displayWithAttributes(String prefix, Node node, 
			String[] attributes)
	{
		if (node.getNodeType() == Node.ELEMENT_NODE) 
			displayWithAttributes(prefix, (Element) node, attributes);
	}
	
	public static void displayWithAttributes(String prefix, Element element, 
			String[] attributes)
	{
		display(prefix, element);
		NamedNodeMap a = element.getAttributes();
		for (int i = 0; i < a.getLength(); i++) {
			String ln = " |" + a.item(i).getNodeName() + " : " 
					+ a.item(i).getNodeValue();
			if (prefix == null) 
				System.out.println(ln);
			else
				System.out.println(prefix + ln);
		}
	}
	
	public static void displayIfAttribute(Element element, String attribute, 
			String value)
	{
		if(element.getAttribute(attribute).toString().equals(value))
		{
			displayWithAttributes(null, element, null);
		}
	}
	
	public static void displayAllChildNodes(Element element)
	{
		displayAllChildNodes(null, element);	
	}
	
	public static void displayAllChildNodes(String prefix, Element element)
	{
		displayAllChildNodes(null, element, false);
	}
	
	public static void displayAllChildNodes(String prefix, Element element, 
			Boolean attributes)
	{
		if (element.hasChildNodes()) 
		{
			NodeList childNodes = element.getChildNodes();
			forAllNodes(childNodes, new nodeOperation() { 
				public void action(Node node) 
				{ 
					if (attributes)
						displayWithAttributes(prefix, node, null);
					else
						display(prefix, node); 
					if (node.getNodeType() == Node.ELEMENT_NODE) 
					{
						if (prefix == null) 
							displayAllChildNodes(null, (Element) node, 
									attributes);
						else 
							displayAllChildNodes(prefix + "-", 
									(Element) node, attributes );
					}
				}
			} );
		}			
	}


}
