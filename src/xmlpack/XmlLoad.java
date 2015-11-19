package xmlpack;

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
import utility.Vector;
import agent.Agent;
import agent.body.Body;
import agent.body.Point;

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
	
	public static Agent loadAgentPrimaries(Agent aAgent, Node agentNode)
	{
		Element xmlAgent = (Element) agentNode;
		
		NodeList stateNodes = xmlAgent.getElementsByTagName("state");
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element stateElement = (Element) stateNodes.item(j);
			
			// state node with just attributes	
			if (! stateElement.hasChildNodes())
			{
				switch (stateElement.getAttribute("type")) 
				{
					case "boolean" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Boolean.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "int" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Integer.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "double" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Double.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "String" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), stateElement.getAttribute("value"));
	                	break;
				}
			}
			// state node with attributes and child nodes 
			else
			{
				switch (stateElement.getAttribute("type")) 
				{
					case "body" :
						//FIXME: not finished only accounts for simple coccoid cells
						List<Point> pointList = new LinkedList<Point>();
						NodeList pointNodes = stateElement.getElementsByTagName("point");
						for (int k = 0; k < pointNodes.getLength(); k++) 
						{
							Element point = (Element) pointNodes.item(k);
							pointList.add(new Point(Vector.vectorFromString(point.getAttribute("position"))));
						}
						aAgent.setPrimary("body",new Body(pointList));
						break;
					case "reactions" :
						List<Reaction> reactionList = new LinkedList<Reaction>();
						NodeList reactionNodes = stateElement.getElementsByTagName("reaction");
						for (int k = 0; k < reactionNodes.getLength(); k++) 
						{
							Element reaction = (Element) reactionNodes.item(k);
							reactionList.add(new Reaction(reaction.getAttribute("whateverisneededforthisconstructor")));
						}
						aAgent.setPrimary("reactions",reactionList);
						break;
				}
			}
		}
		return aAgent;
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
