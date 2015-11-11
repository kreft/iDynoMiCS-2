package test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import reaction.Reaction;
import utility.Vector;
import xmlpack.XmlLoad;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.body.Body;
import agent.body.Point;
import agent.state.*;
import agent.state.secondary.VolumeState;
import idynomics.Compartment;

public class AgentTest {

	public static HashMap<String, Compartment> _compartments = new HashMap<String, Compartment>();
	
	public static Agent loadAgent(Node agentNode)
	{
		Element xmlAgent = (Element) agentNode;
		Agent aAgent = new Agent();
		
		NodeList stateNodes = xmlAgent.getElementsByTagName("state");
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element stateElement = (Element) stateNodes.item(j);
			
			// state node with just attributes	
			if (! stateElement.hasChildNodes())
			{
				switch (stateElement.getAttribute("type")) 
				{
					case "boolean" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Boolean.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "int" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Integer.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "double" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), Double.valueOf(stateElement.getAttribute("value")));
	                	break;
					case "String" : 
						aAgent.setPrimary(stateElement.getAttribute("name"), stateElement.getAttribute("value"));
	                	break;
				}
			}
			// state node with attributes and child nodes 
			else
			{
				switch (stateElement.getAttribute("type")) 
				{
					case "body" :
						//FIXME: not finished only accounts for simple coccoid cells
						List<Point> pointList = new LinkedList<Point>();
						NodeList pointNodes = stateElement.getElementsByTagName("point");
						for (int k = 0; k < pointNodes.getLength(); k++) 
						{
							Element point = (Element) pointNodes.item(k);
							pointList.add(new Point(Vector.vectorFromString(point.getAttribute("position"))));
						}
						aAgent.setPrimary("body",new Body(pointList));
						break;
					case "reactions" :
						List<Reaction> reactionList = new LinkedList<Reaction>();
						NodeList reactionNodes = stateElement.getElementsByTagName("reaction");
						for (int k = 0; k < reactionNodes.getLength(); k++) 
						{
							Element reaction = (Element) reactionNodes.item(k);
							reactionList.add(new Reaction(reaction.getAttribute("whateverisneededforthisconstructor")));
						}
						aAgent.setPrimary("reactions",reactionList);
						break;
				}
			}
		}
		return aAgent;	
	}

	public static void main(String[] args) {
		
		// our test agent
		Agent testagent = new Agent();

		// add a new state
		State mass = new PrimaryState();
		mass.init(testagent, 0.1);
		testagent.setState("mass",mass);
		
		// add a new state the automated way
		testagent.setPrimary("density", 0.2);
		
		// add a predefined secondary state
		State volume = new VolumeState();
		volume.init(testagent, null);
		testagent.setState("volume",volume);
		
		// add a secondary state that was not previously defined (anonymous class).
		State anonymous = new CalculatedState();
		anonymous.init(testagent, new CalculatedState.stateExpression() {
			
			@Override
			public Object calculate(Agent agent) {
				return (Double) agent.get("mass") / (Double) agent.get("density");
			}
		});
		testagent.setState("volume2",anonymous);
		
		System.out.println(testagent.get("mass"));
		System.out.println(testagent.getState("mass").getClass());
		System.out.println(testagent.get("density"));
		System.out.println(volume.get());
		System.out.println(testagent.get("volume"));
		System.out.println(anonymous.get());
		System.out.println(anonymous.getClass());
		System.out.println(testagent.get("volume2"));
		
		
		//////////////
		// now the same thing the ezway
		/////////////
		
		
		long tic = System.currentTimeMillis();
		int times = 1000000;
		for (int b = 0; b < times; b++)
		{
		// our test agent
		Agent ezagent = new Agent();

		// add a new state
		ezagent.set("mass",0.1);
		
		// add a new state again
		ezagent.set("density", 0.2);
		
		// add a predefined secondary state
		ezagent.set("volume",new VolumeState());
		
		// add a secondary state that was not previously defined (anonymous class).
		ezagent.set("volume2", new CalculatedState.stateExpression() {
			
			@Override
			public Object calculate(Agent agent) {
				return (Double) agent.get("mass") / (Double) agent.get("density");
			}
		});
		
		
		}
		System.out.println(times + " times in: " + (System.currentTimeMillis()-tic) + " milisecs");

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
			Compartment aCompartment = new 
							Compartment(xmlCompartment.getAttribute("shape"));
			aCompartment.init();
			
			// Check the agent container
			NodeList agentcontainerNodes = xmlCompartment.getElementsByTagName("agents");
			if (agentcontainerNodes.getLength() > 1)
				System.out.println("more than 1 agentcontainer!!!");
			Element agentcontainer = (Element) agentcontainerNodes.item(0);
			
			// cycle trough all agents in the agent container
			NodeList agentNodes = agentcontainer.getElementsByTagName("agent");
			for (int j = 0; j < agentNodes.getLength(); j++) 
				aCompartment.addAgent(loadAgent(agentNodes.item(j)));

			_compartments.put(xmlCompartment.getAttribute("name"), aCompartment);
		}
	}
}
