package processManager.library;

import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import processManager.ProcessDeparture;
import referenceLibrary.XmlRef;
import utility.ExtraMath;

public class DepartureByFlow extends ProcessDeparture {

	/**
	 * Volume flowing out of this compartment. Can be given either as a negative
	 * or positive number. The absolute value will be used
	 */
	private double _flowRate;
	private String FLOWRATE = XmlRef.volumeFlowRate;
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._flowRate = Math.abs(XmlHandler.obtainDouble(
				xmlElem, FLOWRATE, this.defaultXmlTag()));
	}

	
	/**
	 * The number of agents at time t can be found using a simple 
	 * differential equation:
	 * 
	 * dA/dt = rA
	 * A(t) = A(0) * e^(rt)
	 * 
	 * TeX:
	 * $$\frac{dA}{dt} = -rA$$
	 * $$A(t) = A(0)*e^{-rt} $$
	 * $$r = \frac{detachment.rate*h}{surface.distance}$$
	 * 
	 * here r is the removal rate and A is the number of agents.
	 * Translating this to agent based we can say the chance of any
	 * agent being removed over time t equals e^(rt).
	 */
	@Override
	protected LinkedList<Agent> agentsDepart() 
	{
		int nAllAgents = this._agents.getNumAllAgents();
		LinkedList<Agent> removals = new LinkedList<Agent>();
		if ( (nAllAgents > 0) && (this._flowRate < 0.0) )
		{
			/* calculate (1 - removal chance) */
			double e = Math.exp( ( 
					- this._flowRate/this._agents.getShape().getTotalVolume() * 
					Idynomics.simulator.timer.getTimeStepSize() ) ); 
			
			for ( int i = 0; i < nAllAgents; i++ )
			{
				if( ExtraMath.getUniRandDbl() > e )
					removals.add( this._agents.chooseAgent(i) );
			}
		}
		return removals;
	}
	
}
