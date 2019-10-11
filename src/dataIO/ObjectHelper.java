package dataIO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import referenceLibrary.XmlRef;

/**
 * \brief Helper methods to create complex java.lang objects from xml or string.
 * Note: implements Settable, Instantiable, Copyable is always preferred.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ObjectHelper {
	
	/* ************************************************************************
	 * Lists from xml / String
	 * ***********************************************************************/

	/**
	 * Construct a LinkedList from an xml element
	 * @param s
	 * @return
	 */
	public static LinkedList<?> xmlList(Element s)
	{
		NodeList items;
		LinkedList<Object> temp = new LinkedList<Object>();
		items = XmlHandler.getAll(s, XmlRef.item);
		for ( int i = 0; i < items.getLength(); i++ )
			temp.add((Object) ObjectFactory.loadObject((Element) items.item(i)));
		return temp;
	}

	/**
	 * construct a LinkedList from a String formated xml element
	 * @param s
	 * @return
	 */
	public static LinkedList<?> xmlList(String s)
	{
		return xmlList((Element) ObjectHelper.stringToNode(s));
	}
	
	/* ************************************************************************
	 * Maps from xml / String
	 * ***********************************************************************/

	/**
	 * Construct a HashMap from xml element with default node and attribute 
	 * labeling.
	 * @param s
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(Element s)
	{
		return ObjectHelper.xmlHashMap(s, XmlRef.item);
	}

	/**
	 * construct a HahMap from a String formated xml element (default labeling).
	 * @param s
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(String s)
	{
		return xmlHashMap( (Element) ObjectHelper.stringToNode(s) );
	}

	/**
	 * Construct a HashMap from xml element with alternative item node label,
	 * but otherwise default.
	 * @param s
	 * @param itemNodeLabel
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(Element s, String itemNodeLabel)
	{
		return ObjectHelper.xmlHashMap(s, itemNodeLabel, XmlRef.keyClassAttribute, 
				XmlRef.keyAttribute, XmlRef.classAttribute, 
				XmlRef.valueAttribute);
	}

	/**
	 * Construct a HashMap from xml element allowing for fully custom labeling
	 * @param s
	 * @param itemNodeLabel
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(Element s, String itemNodeLabel, 
			String keyClass, String keyAttribute, String valueClass,
			String valueAttribute)
	{
		NodeList items;
		HashMap<Object,Object> hMap = new HashMap<Object,Object>();
		items = XmlHandler.getAll(s, itemNodeLabel);
		for ( int i = 0; i < items.getLength(); i++ )
		{
			hMap.put( (Object) ObjectFactory.loadObject( (Element) items.item(i), 
					keyAttribute, keyClass ), 
					(Object) ObjectFactory.loadObject( (Element) items.item(i), 
					valueAttribute, valueClass ) );
		}
		return hMap;
	}
	
	/* ************************************************************************
	 * Helper methods
	 * ***********************************************************************/

	/**
	 * Helper method that converts string to xml node for complex objects
	 * xml formatted input
	 * @param input
	 * @return
	 */
	static Node stringToNode(String input)
	{
		Node node = null;
		try {
			node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(input.getBytes()))
					.getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
}
