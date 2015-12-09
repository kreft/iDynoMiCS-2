package dataIO;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import agent.Agent;
import idynomics.Compartment;
import utility.ExtraMath;

public class XmlTest {

	public static void main(String[] args)
	{
		// Make sure the random number generator is up and running.
		ExtraMath.initialiseRandomNumberGenerator();

		// Load the xml document
		Element doc = XmlLoad.loadDocument("testdata.xml");

		
		// Display document element's general info
		XmlLoad.displayWithAttributes(null, doc, null);
		
		System.out.println("--------------------- ");
		
		// display all child nodes (in tree structure, from the element doc, 
		// with attributes)
		XmlLoad.displayAllChildNodes("-",doc,true);
		
		// filter
		System.out.println("\nA filter for agents with species 'ecoli': ");
		NodeList agentNodes = doc.getElementsByTagName("agent"); 
		for (int i = 0; i < agentNodes.getLength(); i++) 
		{
			XmlLoad.displayIfAttribute((Element) agentNodes.item(i),
					"species","ecoli");
		} 
		
		// load data into model
		System.out.println("\nLoad data into model: ");
		
		// iterate trough all agents in xml file and add them including their
		// attributes
		Compartment comp = new Compartment("CUBOID");
		for (int i = 0; i < agentNodes.getLength(); i++) 
		{
			Agent anAgent = new Agent();
			NamedNodeMap att = ((Element) agentNodes.item(i)).getAttributes();
			for(int j = 0; j< att.getLength(); j++)
			{
				anAgent.setPrimary(att.item(j).getNodeName(), 
						att.item(j).getNodeValue());
			}
			comp.addAgent(anAgent);
		}
		
		System.out.println("Created: " + comp.agents.getNumAllAgents() + " agents");

		//
		System.out.println("\nThe end");
	}
}
