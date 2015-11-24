package test;

import utility.PovExport;
import xmlpack.XmlLoad;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Species;
import agent.SpeciesLib;
import idynomics.Compartment;
import idynomics.Simulator;

public class AgentTest {
	
	static Simulator sim = new Simulator();

	public static void main(String[] args) {
		
		// load xml doc
		Element doc = XmlLoad.loadDocument("testagents.xml");
		
		// Display document element's general info
		XmlLoad.displayWithAttributes(null, doc, null);
		System.out.println("-------------------------------------------------");
		//XmlLoad.displayAllChildNodes("-",doc,true);
		
		// cycle trough all species and add them to the species Lib
		NodeList speciesNodes = doc.getElementsByTagName("species");
		for (int i = 0; i < speciesNodes.getLength(); i++) 
		{
			Element xmlSpecies = (Element) speciesNodes.item(i);
			SpeciesLib.set(xmlSpecies.getAttribute("name"), 
					new Species(speciesNodes.item(i)));
		}
		
		// cycle trough all compartments
		NodeList compartmentNodes = doc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			Element xmlCompartment = (Element) compartmentNodes.item(i);
			Compartment comp = sim.addCompartment(
					xmlCompartment.getAttribute("name"), 
					xmlCompartment.getAttribute("shape"));
			
			// Check the agent container
			if (xmlCompartment.getElementsByTagName("agents").getLength() > 1)
				System.out.println("more than 1 agentcontainer!!!");

			// cycle trough all agents in the agent container
			NodeList agentNodes = ((Element) xmlCompartment.
					getElementsByTagName("agents").item(0)).
					getElementsByTagName("agent");
			
			for (int j = 0; j < agentNodes.getLength(); j++) 
				new Agent(agentNodes.item(j), comp);

			System.out.println("writing output for compartment: " + comp.name);			
			PovExport.writepov(comp.name, comp.agents.getAllLocatedAgents());
		}
	}
}
