package dataIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.security.CodeSource;
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

import idynomics.Idynomics;
import idynomics.Param;
import dataIO.Log.Tier;
import utility.Helper;

/**
 * \brief Helper class for working with XML files.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class XmlHandler
{
	/**
	 * \brief Make a new {@code Document}.
	 * 
	 * @return
	 */
	public static Document newDocument()
	{
		try 
		{
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			return doc;
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * \brief Returns specified document as xml Element
	 * 
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
		} catch ( ParserConfigurationException | IOException e) {
			Log.printToScreen("Error while loading: " + document + "\n"
					+ "error message: " + e.getMessage(), true);
			document = Helper.obtainInput("", "Atempt to re-obtain document",
					true);
			return loadDocument(document);
		} catch ( SAXException e ) {
			Log.printToScreen("Error while loading: " + document + "\n"
				+ "error message: " + e.getMessage(), true);
			return null;
		}			
		
	}
	
	/**
	 * Load resource from within .jar file
	 * @param resource
	 * @return
	 */
	public static Element loadResource(String resource)
	{
		try {
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc;
			InputStream input = String.class.getResourceAsStream(resource);
			doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			return doc.getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("Error while loading: " + resource + "\n"
					+ "error message: " + e.getMessage());
			resource = Helper.obtainInput("", "Atempt to re-obtain document",
					true);
			return null;
		}
	}
	
	/**
	 * quick method to allow appending the current path in front of the resource
	 * before reading the document.
	 * @param document
	 */
	public static Element loadDocumentCanonicial(String document)
	{
		try {
			String current = new java.io.File( "" ).getCanonicalPath();
			File fXmlFile = new File(current + "/" + Idynomics.global.idynomicsRoot + "/" + document);
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc.getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("Error while loading: " + document + "\n"
					+ "error message: " + e.getMessage());
			document = Helper.obtainInput("", "Atempt to re-obtain document",
					true);
			return loadResource(document);
		}
	}
	
	/**
	 * FIXME TESTING
	 * TODO find a way to get the jar path in both Linux, windows and mac
	 * @return
	 * @throws Exception
	 */
	public static String getJarContainingFolder() throws Exception {
		  CodeSource codeSource = Idynomics.class.getProtectionDomain().getCodeSource();

		  File jarFile;

		  if (codeSource.getLocation() != null) {
		    jarFile = new File(codeSource.getLocation().toURI());
		  }
		  else {
		    String path = Idynomics.class.getResource(Idynomics.class.getSimpleName() + ".class").getPath();
		    String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
		    jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
		    jarFile = new File(jarFilePath);
		  }
		  return jarFile.getParentFile().getAbsolutePath();
		}

	/**
	 * \brief Gathers non critical attributes, returns "" if the attribute is not
	 * defined. This method does not ask the user for any information.
	 */
	public static String gatherAttribute(Element xmlElement, String attribute)
	{
		if ( xmlElement.hasAttribute(attribute) )
			return xmlElement.getAttribute(attribute);
		else
			return "";
	}
	
	/**
	 * \brief Gathers non critical attributes, returns "" if the attribute is not
	 * defined. This method does not ask the user for any information.
	 * @param xmlElement
	 * @param attribute
	 * @return
	 */
	public static String gatherAttribute(Node xmlElement, String attribute)
	{
		return gatherAttribute((Element) xmlElement, attribute);
	}

	/**
	 * \brief This method gets an attribute from an element, if the element does not
	 * have this attribute it will ask the user.
	 */
	public static String obtainAttribute(Element xmlElement, String attribute)
	{
		if(xmlElement.hasAttribute(attribute))
			return  xmlElement.getAttribute(attribute);
		else
		{
			return Helper.obtainInput(null, "Required " + attribute +
					" from missing xml node: " + xmlElement.getLocalName());
		}
	}
	
	/**
	 * \brief Enquires attributes from parent's child nodes identified by tag.
	 * @param parent
	 * @param tag
	 * @param attributes
	 * @return
	 */
	public static List<String[]> gatherAtributesFrom(Element parent, String tag, 
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
	 * \brief returning all child nodes identified by tag from parent node or element
	 */
	public static NodeList getAll(Node parent, String tag)
	{
		return getAll((Element) parent, tag);
	}
	
	/**
	 * \brief returning all child nodes identified by tag from parent node or element
	 */
	public static NodeList getAll(Element parent, String tag)
	{
		return parent.getElementsByTagName(tag);
	}
	
	/**
	 * \brief Directly writes attributes from xml node to static field
	 */
	public static void setStaticField(Class<?> c, Element element)
	{
		try {
			Field f = c.getDeclaredField(
					element.getAttribute(XmlLabel.nameAttribute));
			f.set(c, element.getAttribute(XmlLabel.valueAttribute));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// NOTE: do not log this since this may occur before the log
			// is initiated (the log it self is start from a general 
			// param
			System.err.println("Warning: attempting to set non existend"
					+ " general paramater: " + 
					element.getAttribute(XmlLabel.nameAttribute) );
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * \brief Checks for unique node exists and whether it is unique, than returns it.
	 * 
	 * @param xmlElement
	 * @param tagName
	 * @return
	 */
	public static Element loadUnique(Element xmlElement, String tagName)
	{
		NodeList nodes =  xmlElement.getElementsByTagName(tagName);
		if (nodes.getLength() > 1)
		{
			Log.out(Tier.NORMAL,"Warning: document contains more than 1"
					+ tagName + " nodes, loading first simulation node...");
		}
		else if (nodes.getLength() == 0)
		{
			Log.out(Tier.NORMAL,"Warning: could not identify " + tagName + 
					" node, make sure your file contains all required elements."
					+ " Attempt to continue with 'null' node.");
			return null;
		}
		return (Element) nodes.item(0);
	}		
	
	/**
	 * returns true if a node tagName exists as childNode of xmlElement
	 * @param xmlElement
	 * @param tagName
	 * @return
	 */
	public static boolean hasNode(Element xmlElement, String tagName)
	{
		NodeList nodes = xmlElement.getElementsByTagName(tagName);
		return ( nodes.getLength() > 0 );
	}
	
	/**
	 * \brief Loads attribute from a unique node
	 * @param xmlElement
	 * @param tagName
	 * @param attribute
	 * @return
	 */
	public static String attributeFromUniqueNode(Element xmlElement, String tagName, 
			String attribute)
	{
		Element e = loadUnique(xmlElement, tagName);
		if(e == null)
		{
			return Helper.obtainInput(null, "Required " + attribute +
					" from missing xml node: " + tagName);
		}
		else
			return obtainAttribute(e, attribute);
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
				+ element.getAttribute(XmlLabel.nameAttribute);
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
		for ( int i = 0; i < a.getLength(); i++ )
		{
			String ln = " | " + a.item(i).getNodeName() + " : " 
					+ a.item(i).getNodeValue();
			if ( prefix == null )
				Log.printToScreen(ln, false);
			else
				Log.printToScreen(prefix + ln, false);
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
		if ( element.hasChildNodes() ) 
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
	
}
