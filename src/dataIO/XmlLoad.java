package dataIO;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import linearAlgebra.Vector;
import agent.StateObject;
import agent.body.Body;
import agent.body.Point;
import agent.state.StateLib;

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

	public static void forAllNodes(NodeList nodeList, nodeOperation operation) 
	{
	    for (int i = 0; i < nodeList.getLength(); i++) 
		     operation.action(nodeList.item(i));
	}
	
	/**
	 * Loads all states from xmlNode into anything that implements the
	 * StateObject interface.
	 * @param stateObject
	 * @param xmlNode
	 */
	public static void loadStates(StateObject stateObject, Node xmlNode)
	{
		Element xmlAgent = (Element) xmlNode;
		
		NodeList stateNodes = xmlAgent.getElementsByTagName("state");
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			if (! s.hasChildNodes())	// state node with just attributes //
			{
				switch (s.getAttribute("type")) 
				{
					case "boolean" : 
						stateObject.setPrimary(s.getAttribute("name"), 
								Boolean.valueOf(s.getAttribute("value")));
	                	break;
					case "int" : 
						stateObject.setPrimary(s.getAttribute("name"), 
								Integer.valueOf(s.getAttribute("value")));
	                	break;
					case "double" : 
						stateObject.setPrimary(s.getAttribute("name"), 
								Double.valueOf(s.getAttribute("value")));
	                	break;
					case "String" : 
						stateObject.setPrimary(s.getAttribute("name"), 
								s.getAttribute("value"));
	                	break;
					case "secondary" : 
						stateObject.setState(s.getAttribute("name"), 
								StateLib.get(s.getAttribute("value")));
	                	break;
				}
			}
			else	// state node with attributes and child nodes //
			{
				switch (s.getAttribute("type")) 
				{
					case "body" :
						//FIXME: not finished only accounts for simple coccoid cells
						List<Point> pointList = new LinkedList<Point>();
						NodeList pointNodes = s.getElementsByTagName("point");
						for (int k = 0; k < pointNodes.getLength(); k++) 
						{
							Element point = (Element) pointNodes.item(k);
							pointList.add(new Point(Vector.dblFromString(
									point.getAttribute("position"))));
						}
						stateObject.setPrimary("body", new Body(pointList));
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
						stateObject.setPrimary("reactions", reactions);
						break;
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
	
	public static void displayIfAttribute(Element element, String attribute, String value)
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
