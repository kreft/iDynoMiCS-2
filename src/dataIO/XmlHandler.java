package dataIO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Collection;
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

import idynomics.Idynomics;
import referenceLibrary.XmlRef;
import sensitivityAnalysis.XmlCreate;
import dataIO.Log.Tier;
import utility.Helper;

/**
 * \brief Helper class for working with XML files.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class XmlHandler
{
	/**
	 * \brief Make a new {@code Document}.
	 * 
	 * @return A new empty document.
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
					false);
			return loadDocument(document);
		} catch ( SAXException e ) {
			Log.printToScreen("Error while loading: " + document + "\n"
				+ "error message: " + e.getMessage(), true);
			return null;
		}			
		
	}
	
	/**
	 * \brief Checks if there is an argument provided and loads the input XML file 
	 * provided in the argument. If no argument provided, asks for file name.
	 * @throws IOException 
	 */
	public static Document xmlLoad(String filePath) throws IOException {
		if ( filePath == null )
		{
			Log.printToScreen("No XML File given!", true);
			throw new IOException();
		}
		Log.printToScreen("Reading XML file: " + filePath + 
			"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
			+ "~~~~~~~~~~~~~~~~~~~~~~~~\n", false);
		
		return XmlHandler.getDocument(filePath);
	}
	
	/**
	 * \brief Load the input XML file provided in the argument
	 */
	public static Document getDocument(String filePath) {
		try {
			File fXmlFile = new File(filePath);
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch ( ParserConfigurationException | IOException e) {
			Log.printToScreen("Error while loading: " + filePath + "\n"
					+ "error message: " + e.getMessage(), true);
			filePath = Helper.obtainInput("", "Atempt to re-obtain document",
					false);
			return getDocument(filePath);
		} catch ( SAXException e ) {
			Log.printToScreen("Error while loading: " + filePath + "\n"
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
					false);
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
					false);
			return loadResource(document);
		}
	}
	
	/**
	 * FIXME TESTING
	 * TODO find a way to get the jar path in both Linux, windows and mac
	 * @return
	 * @throws Exception
	 */
	public static String getJarContainingFolder() throws Exception
	{
		CodeSource codeSource =
				Idynomics.class.getProtectionDomain().getCodeSource();
		File jarFile;
		if (codeSource.getLocation() == null)
		{
			String name = Idynomics.class.getSimpleName() + ".class";
			String path = Idynomics.class.getResource(name).getPath();
			int start = path.indexOf(":") + 1;
			int end = path.indexOf("!");
			String jarFilePath = path.substring(start, end);
			jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
			jarFile = new File(jarFilePath);
		}
		else
		{
			jarFile = new File(codeSource.getLocation().toURI());
		}
		return jarFile.getParentFile().getAbsolutePath();
	}

	/**
	 * \brief Gathers non critical attributes, returns "" if the attribute is not
	 * defined. This method does not ask the user for any information.
	 */
	public static String gatherAttribute(Element xmlElement, String attribute)
	{
		if ( xmlElement != null && xmlElement.hasAttribute(attribute) )
			return xmlElement.getAttribute(attribute);
		else
			return null;
	}
	
	/**
	 * \brief Gathers non critical attributes
	 * 
	 * <p>Returns "" if the attribute is not defined. This method does not ask
	 * the user for any information.</p>
	 * 
	 * @param xmlElement
	 * @param attribute
	 * @return
	 */
	public static String gatherAttribute(Node xmlElement, String attribute)
	{
		return gatherAttribute((Element) xmlElement, attribute);
	}

	/**
	 * \brief This method gets an attribute from an element: if the element
	 * does not have this attribute it will ask the user.
	 */
	public static String obtainAttribute(Element xmlElement,
			String attribute, String tag)
	{
		if ( xmlElement != null && xmlElement.hasAttribute(attribute) )
			return xmlElement.getAttribute(attribute);
		else
		{
			return Helper.obtainInput(null,
					"Required " + attribute +" for node: " + tag );
		}
	}
	
	/**
	 * obtain boolean input
	 * @param xmlElement
	 * @param attribute
	 * @param tag
	 * @return
	 */
	public static boolean obtainBoolean(Element xmlElement, String attribute, String tag)
	{
		if ( xmlElement != null && xmlElement.hasAttribute(attribute) )
			return Boolean.valueOf(xmlElement.getAttribute(attribute));
		else
		{
			return Helper.obtainInput("Required " + attribute +
					" for node: "  + tag, true );
		}
	}
	
	/**
	 * \brief Gets an attribute from an XML node: if the attribute cannot be
	 * found, asks the user. 
	 * 
	 * @param xmlNode Node from an XML protocol file.
	 * @param attribute Name of the attribute sought.
	 * @return String representation of the attribute required.
	 */
	public static String obtainAttribute(Node xmlNode, String attribute, String tag)
	{
		return obtainAttribute((Element) xmlNode, attribute, tag);
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
	 * \brief Gets all child nodes identified by tag from parent element.
	 * 
	 * @param parent TODO
	 * @param tag
	 */
	public static NodeList getAll(Element parent, String tag)
	{
		if (parent == null)
			return null;
		return parent.getElementsByTagName(tag);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param parent
	 * @param tag
	 * @return
	 */
	public static Collection<Element> getElements(Element parent, String tag)
	{
		LinkedList<Element> out = new LinkedList<Element>();
		NodeList list = getAll(parent, tag);
		if ( list != null )
		{
			for ( int i = 0; i < list.getLength(); i++)
				out.add((Element) list.item(i));
		}
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param parent
	 * @param tag
	 * @param attribute
	 * @param value
	 * @return
	 */
	public static Node getSpecific(Element parent, String tag,
			String attribute, String value)
	{
		if ( parent == null )
			return null;
		String v;
		NodeList list = parent.getElementsByTagName(tag);
		for ( int i = 0; i < list.getLength(); i++ )
		{
			v = XmlHandler.gatherAttribute(list.item(i), attribute);
			if ( v.equals(value) )
				return list.item(i);
		}
		return null;
	}

	/**
	 * \brief Checks that a child XML element belongs to that given and is
	 * unique.
	 * 
	 * <p>If the XML element is {@code null}, this method returns {@code null}
	 * without warning.</p>
	 * 
	 * <p>If the given element does not own a child element with the given
	 * name, this method prints a warning and returns {@code null}.</p>
	 * 
	 * <p>If there are multiple child elements with the give tag, this method
	 * prints a warning and returns the first.</p>
	 * 
	 * @param xmlElement Element of an XML document.
	 * @param tagName 
	 * @return The child XML element.
	 */
	public static Element findUniqueChild(Element xmlElement, String tagName)
	{
		if ( xmlElement == null )
			return null;
		
		NodeList nodes = xmlElement.getElementsByTagName(tagName);
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
	 * \brief Check if an XML element has a child element with the given tag.
	 * 
	 * @param xmlElement Element of an XML document.
	 * @param tagName
	 * @return True if at least one child element with the given tag exists
	 * belongs xmlElement.
	 */
	public static boolean hasChild(Element xmlElement, String tagName)
	{
		NodeList nodes = xmlElement.getElementsByTagName(tagName);
		return ( nodes.getLength() > 0 );
	}
	
	/**
	 * \brief Loads attribute from a unique node
	 * 
	 * @param xmlElement Element of an XML document.
	 * @param tagName
	 * @param attribute
	 * @return
	 */
	public static String attributeFromUniqueNode(Element xmlElement, String tagName, 
			String attribute)
	{
		Element e = findUniqueChild(xmlElement, tagName);
		if(e == null)
		{
			return Helper.obtainInput(null, "Required " + attribute +
					" from missing xml node: " + tagName);
		}
		else
			return obtainAttribute(e, attribute, tagName);
	}
	
	/**
	 * Construct xml element from string
	 * based on: http://stackoverflow.com/questions/729621/convert-string-xml-fragment-to-document-node-in-java
	 */
	public static Element elementFromString(String xmlSnippet)
	{
		Element node = null;
		try {
			node =  DocumentBuilderFactory
				    .newInstance()
				    .newDocumentBuilder()
				    .parse(new ByteArrayInputStream(xmlSnippet.getBytes()))
				    .getDocumentElement();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
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
				+ element.getAttribute(XmlRef.nameAttribute);
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

	public static boolean hasAttribute(Element p, String attribute) {
		if (p == null)
			return false;
		else
			return p.hasAttribute(attribute);
	}
	
}
