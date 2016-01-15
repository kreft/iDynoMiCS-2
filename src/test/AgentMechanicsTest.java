package test;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Species;
import agent.SpeciesLib;
import dataIO.PovExport;
import dataIO.SvgExport;
import dataIO.XmlLoad;
import processManager.*;
import idynomics.Compartment;
import idynomics.Simulator;

public class AgentMechanicsTest {

	public static void main(String[] args)
	{
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
		
		for (int i = 0; i < speciesNodes.getLength(); i++) 
		{
			Element xmlSpecies = (Element) speciesNodes.item(i);
			XmlLoad.loadSpeciesModules(SpeciesLib.get(xmlSpecies.getAttribute("name")),speciesNodes.item(i)); 
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
		
		double stepSize = 1;
		int nStep, mStep;
		nStep = mStep = 4*24;
		ProcessManager agentRelax = new AgentRelaxation();
		agentRelax.setTimeForNextStep(0.0);
		agentRelax.setTimeStepSize(stepSize);
		
		ProcessManager agentGrowth = new AgentGrowth();
		agentGrowth.setTimeForNextStep(0.0);
		agentGrowth.setTimeStepSize(stepSize);

		/**
		 * Very unfinished!!!
		 */
//		PovExport pov = new PovExport();
		SvgExport svg = new SvgExport();

		System.out.println("Time: "+agentRelax.getTimeForNextStep());
		// write initial state
//		pov.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
		svg.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
		for ( ; nStep > 0; nStep-- )
		{
			// step the process manager
			agentGrowth.step(testcompartment._environment, testcompartment.agents);
			agentRelax.step(testcompartment._environment, testcompartment.agents);
			// write output
//			pov.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
			svg.writepov(testcompartment.name, testcompartment.agents.getAllLocatedAgents());
			System.out.println(mStep-nStep +" Time: "+agentRelax.getTimeForNextStep());
		}
		System.out.println("finished");
	}
}
