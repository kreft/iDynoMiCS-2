package dataIO;

import java.io.File;
import java.io.IOException;
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

import idynomics.Param;

/**
 * handles xml files
 * @author baco
 *
 */
public class XmlHandler {

	/**
	 * Returns specified document as xml Element
	 * @param document
	 * @return
	 */
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
	
	public static String gatherAttribute(Node xmlElement, String attribute)
	{
		return gatherAttribute((Element) xmlElement, attribute);
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
	 * Enquires attributes from parent's child nodes identified by tag.
	 * @param parent
	 * @param tag
	 * @param attributes
	 * @return
	 */
	public static List<String[]> gatherAtributes(Element parent, String tag, 
			String[] attributes)
	{
		NodeList nodes = getAll(parent, tag);
		List<String[]> values = new LinkedList<String[]>();
		for(int i = 0; nodes.getLength() > i; i++)
		{
			String[] temp = new String[attributes.length];
			for(int j = 0; attributes.length > j; j++)
				temp[j] = gatherAttribute(((Element) nodes.item(i)), 
						attributes[j]);
			values.add(temp);
		}
		return values;
	}
	
	/**
	 * returning all child nodes identified by tag from parent node or element
	 */
	public static NodeList getAll(Node parent, String tag)
	{
		return getAll((Element) parent, tag);
	}
	
	public static NodeList getAll(Element parent, String tag)
	{
		return parent.getElementsByTagName(tag);
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
			for (int i = 0; i < childNodes.getLength(); i++) 
			{
				Node node = childNodes.item(i);
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
		} 
	}

	/**
	 * Checks for unique node exists and whether it is unique, than returns it.
	 * @param xmlElement
	 * @param tagName
	 * @return
	 */
	public static Element loadUnique(Element xmlElement, String tagName)
	{
		NodeList nodes =  xmlElement.getElementsByTagName(tagName);
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
}