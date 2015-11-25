package test;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Species;
import agent.SpeciesLib;
import dataIO.PovExport;
import dataIO.XmlLoad;
import processManager.AgentRelaxation;
import idynomics.Compartment;
import idynomics.Simulator;

public class AgentMechanicsTest {

	public static void main(String[] args) {
		
		////////////////////////
		// Loading initial state from xml
		////////////////////////

		Simulator sim = new Simulator();
		Compartment testcompartment = null;
		
		Element doc = XmlLoad.loadDocument("testagents.xml");
		
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
			Compartment comp = testcompartment = sim.addCompartment(
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
				comp.addAgent(new Agent(agentNodes.item(j)));
			
		}
		
		////////////////////////
		// set parameters and initiate process manager
		////////////////////////
		
		double stepSize = 1.0;
		int nStep = 50;

		AgentRelaxation process = new AgentRelaxation();
		process.setTimeForNextStep(0.0);
		process.setTimeStepSize(stepSize);

		PovExport pov = new PovExport();
		System.out.println("Time: "+process.getTimeForNextStep());
		// write initial state
		pov.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
		for ( ; nStep > 0; nStep-- )
		{
			// step the process manager
			process.step(testcompartment._environment, testcompartment.agents);
			// write output
			pov.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
			System.out.println("Time: "+process.getTimeForNextStep());
		}
		System.out.println("finished");
	}
}
