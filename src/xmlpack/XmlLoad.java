package xmlpack;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
