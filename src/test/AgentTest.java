package test;

import utility.PovExport;
import xmlpack.XmlLoad;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import idynomics.Compartment;
import idynomics.Simulator;

public class AgentTest {
	
	static Simulator sim = new Simulator();

	public static void main(String[] args) {
		
		// load xml doc
		Element doc = XmlLoad.loadDocument("testagents.xml");
		
		// Display document element's general info
		XmlLoad.displayWithAttributes(null, doc, null);
		System.out.println("-----------");
		XmlLoad.displayAllChildNodes("-",doc,true);
		
		// cycle trough all compartments
		NodeList compartmentNodes = doc.getElementsByTagName("compartment");
		for (int i = 0; i < compartmentNodes.getLength(); i++) 
		{
			Element xmlCompartment = (Element) compartmentNodes.item(i);
			Compartment aCompartment = sim.addCompartment(
					xmlCompartment.getAttribute("name"), 
					xmlCompartment.getAttribute("shape"));
			
			// Check the agent container
			NodeList agentcontainerNodes = xmlCompartment.getElementsByTagName("agents");
			if (agentcontainerNodes.getLength() > 1)
				System.out.println("more than 1 agentcontainer!!!");
			Element agentcontainer = (Element) agentcontainerNodes.item(0);
			
			// cycle trough all agents in the agent container
			NodeList agentNodes = agentcontainer.getElementsByTagName("agent");
			for (int j = 0; j < agentNodes.getLength(); j++) 
				aCompartment.addAgent(new Agent(agentNodes.item(j)));

			System.out.println("writing output for compartment: " + aCompartment.name);			
			PovExport.writepov(aCompartment.name, aCompartment.agents.getAllLocatedAgents());
		}
	}
}
