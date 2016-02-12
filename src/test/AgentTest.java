package test;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.PovExport;
import dataIO.XmlHandler;
import agent.Agent;
import agent.Species;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.Simulator;
import utility.ExtraMath;

public class AgentTest {
	
	static Simulator sim = new Simulator();

	public static void main(String[] args)
	{
		// Make sure the random number generator is up and running.
		ExtraMath.initialiseRandomNumberGenerator();
		
		// load xml doc
		Element doc = XmlHandler.loadDocument("testagents.xml");
		
		// Display document element's general info
		XmlHandler.displayWithAttributes(null, doc, null);
		System.out.println("-------------------------------------------------");
		//XmlLoad.displayAllChildNodes("-",doc,true);
		
		// cycle trough all species and add them to the species Lib
		NodeList speciesNodes = doc.getElementsByTagName("species");
		for (int i = 0; i < speciesNodes.getLength(); i++) 
		{
			Element xmlSpecies = (Element) speciesNodes.item(i);
			Idynomics.simulator.speciesLibrary.set(xmlSpecies.getAttribute("name"), 
					new Species(speciesNodes.item(i)));
		}
		
		// cycle trough all compartments
		NodeList compartmentNodes = doc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			Element xmlCompartment = (Element) compartmentNodes.item(i);
			Compartment comp = sim.addCompartment(
										xmlCompartment.getAttribute("name"));
			comp.init(xmlCompartment);
			// Check the agent container
			if (xmlCompartment.getElementsByTagName("agents").getLength() > 1)
				System.out.println("more than 1 agentcontainer!!!");

			// cycle trough all agents in the agent container
			NodeList agentNodes = ((Element) xmlCompartment.
					getElementsByTagName("agents").item(0)).
					getElementsByTagName("agent");
			
			for (int j = 0; j < agentNodes.getLength(); j++) 
				comp.addAgent(new Agent(agentNodes.item(j)));
			
			System.out.println("writing output for compartment: " + comp.name);	
			
			new PovExport().writepov(comp.name, comp.agents.getAllLocatedAgents());
		}
	}
}
