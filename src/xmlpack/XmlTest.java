package xmlpack;

import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;



public class XmlTest {

	public static void main(String[] args) {

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
		
		////// example class
		class TestAgent {
			HashMap<String, Object> _states = new HashMap<String, Object>();
			public void setState(String name, Object state)
			{
				_states.put(name, state);
			}
		}
		
		// iterate trough all agents in xml file and add them including their
		// attributes
		LinkedList<TestAgent> Agents = new LinkedList<TestAgent>();
		for (int i = 0; i < agentNodes.getLength(); i++) 
		{
			TestAgent agent = new TestAgent();
			NamedNodeMap att = ((Element) agentNodes.item(i)).getAttributes();
			for(int j = 0; j< att.getLength(); j++)
			{
				agent.setState(att.item(j).getNodeName(), 
						att.item(j).getNodeValue());
			}
			Agents.add(agent);
		}
		
		System.out.println("Created: " + Agents.size() + " agents");

		//
		System.out.println("\nThe end");
	}
}
