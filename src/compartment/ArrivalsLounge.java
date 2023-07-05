package compartment;

import java.util.LinkedList;

import org.w3c.dom.Element;
import agent.Agent;
import dataIO.XmlHandler;
import instantiable.Instantiable;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;

/**
 * The arrivals lounge contains newly arrived agents that have come from other
 * compartments. Agents should arrive in an arrivals lounge during a global 
 * post-step and be removed by an arrival process during the next global 
 * pre-step. Users should ensure compartments that are the destinations of 
 * departure processes have corresponding arrivals processes.
 * 
 * @author Tim Foster - trf896@student.bham.ac.uk
 */

public class ArrivalsLounge implements Instantiable, Settable {

	private Settable _parentNode;
	
	private String _origin;
	
	private LinkedList<Agent> _agents = new LinkedList<Agent>();
	
	public ArrivalsLounge(Element xmlElem, Compartment parent)
	{
		this.instantiate(xmlElem, parent);
	}
	
	public ArrivalsLounge(String origin, Compartment parent)
	{
		this._origin = origin;
		this._parentNode = parent;
	}
	
	
	@Override
	public void instantiate(Element xmlElem, Settable parent) {
		
		this._parentNode = parent;
		this._origin = xmlElem.getAttribute(XmlRef.originAttribute);
		
		if (XmlHandler.hasChild(xmlElem, XmlRef.agent))
		{
			for ( Element e : XmlHandler.getElements( xmlElem, XmlRef.agent) )
				this.addAgent(new Agent( e, (Compartment) this._parentNode ));
		}
	}
	
	/**
	 * An agent held in this arrivals lounge is inserted into the main agent
	 * container and leaves the arrivals lounge.
	 * @param agent
	 */
	public void addAgent(Agent agent)
	{
		this._agents.add(agent);
		agent.setCompartment((Compartment) this._parentNode);
	}
	
	public void addAgents(LinkedList<Agent> agents)
	{
		for (Agent a : agents)
			this.addAgent(a);
	}
	
	public void clear()
	{
		this._agents.clear();
	}
	

	@Override
	public String defaultXmlTag() {
		return XmlRef.arrivalsLounge;
	}

	@Override
	public void setParent(Settable parent) {
		this._parentNode = parent;
		
	}

	@Override
	public Settable getParent() {
		return this._parentNode;
	}
	
	public String getOrigin()
	{
		return this._origin;
	}
	
	public LinkedList<Agent> getAgents()
	{
		return this._agents;
	}

	@Override
	public Module getModule() {
		//The arrivals lounge node
		Module modelNode = new Module( XmlRef.arrivalsLounge, this);
		
		//A compartment may have several arrivals lounges if it accepts agents
		//from several other compartments.
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.add( new Attribute(XmlRef.originAttribute, 
				this.getOrigin(), null, false ) );
		
		/* Add the agent childConstrutor for adding of additional agents. */
		modelNode.addChildSpec( ClassRef.agent, 
				Module.Requirements.ZERO_TO_MANY);
		
		//Add agents as child nodes
		for ( Agent a : this.getAgents() )
			modelNode.add( a.getModule() );
		
		return modelNode;
		
	}

	
}
