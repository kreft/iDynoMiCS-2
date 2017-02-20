/**
 * 
 */
package processManager.library;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import agent.predicate.HasAspect;
import aspect.AspectReg;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import surface.Collision;
import surface.Surface;
import surface.predicate.AreColliding;
import utility.ExtraMath;

/**
 * \brief Plasmid Operations - Conjugation, Segregation Loss and affect on growth
 * rate.
 * 
 * @author Sankalp @SankalpArya (stxsa33@nottingham.ac.uk), UoN, Nottingham
 */
public class PlasmidDynamics extends ProcessManager {
	
	public String PLASMID = AspectRef.plasmidList;
	public String FITNESS_COST = AspectRef.agentFitnessCost;
	public String PILUS_LENGTH = AspectRef.pilusLength;
	public String BODY = AspectRef.agentBody;
	public String PIGMENT = AspectRef.agentPigment;
	/**
	 * Map of production rates for internal products.
	 */
	public String PRODUCTION_RATE = AspectRef.internalProductionRate;
	/**
	 * Growth rate for Simple growth.
	 */
	public String GROWTH_RATE = AspectRef.growthRate;
	
	public final static String TRANS_PROB = "transfer_probability";
	public final static String LOSS_PROB = "loss_probability";
	public final static String COPY_NUM = "copy";
	public final static String PLASMID_PIGMENT = "pigment";
	
	/**
	 * List of plasmids for which conjugation and segregation functions are called.
	 */
	private List<Object> _plasmidList;
	
	/**
	 * Hashmap of all agents with plasmids it contains.
	 */
	private HashMap<Agent, List<String>> _plasmidAgents = new HashMap<Agent, List<String>>();
	
	private String _noPlasmidPigment = "BLUE";
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._plasmidList = (LinkedList<Object>) this.getOr(PLASMID, null);
		Iterator<Object> itr = this._plasmidList.iterator();
		while (itr.hasNext()) {
			String plsmd = itr.next().toString();
			for (Agent agent: this._agents.getAllLocatedAgents())
			{
				if (agent.isAspect(plsmd))
				{
					if (this._plasmidAgents.containsKey(agent))
						this._plasmidAgents.get(agent).add(plsmd);
					else
						this._plasmidAgents.put(agent, Arrays.asList(plsmd));
					if (!agent.isAspect(PILUS_LENGTH))
						agent.setPilusLength(6.0);
					Double simple_growth = agent.getDouble(this.GROWTH_RATE);
					Map<String,Double> internal_production = (HashMap<String,Double>)
							agent.getOr(this.PRODUCTION_RATE, null);
					Double fitness_cost = agent.getDouble(this.FITNESS_COST);
					if (simple_growth != null && !(simple_growth.isNaN()))
						agent.set(this.GROWTH_RATE, 
								simple_growth*(1.0-fitness_cost));
					if (internal_production != null) {
						internal_production.replaceAll(
								(k,v) -> v*(1.0-fitness_cost));
						agent.set(this.PRODUCTION_RATE, internal_production);
					}
				}
			}
		}
	}
	
	/*************************************************************************
	 * CONJUGATION
	 ************************************************************************/
	
	/**
	 * \brief Plasmid transfer via conjugation.
	 * 
	 * @param agents
	 */
	@SuppressWarnings("unchecked")
	protected boolean conjugate(Agent a, Agent b, String plasmid) {
		HashMap<Object, Object> newPlasmid = (HashMap<Object, Object>) a.get(plasmid);
		double transfer_probability = (Double) newPlasmid.get(TRANS_PROB);
		double copy_number = (Double) newPlasmid.get(COPY_NUM);
		Body aBody = (Body) a.getValue(this.BODY);
		List<Surface> aBodSurfaces = aBody.getSurfaces();
		
		Collision iter = new Collision(a.getCompartment().getShape());
		
		Predicate<Collection<Surface>> collisionCheck = new AreColliding<Collection<Surface>>(
				aBodSurfaces, iter, a.getDouble(this.PILUS_LENGTH));
		
		double rndNum = ExtraMath.getUniRandDbl();
		rndNum = (rndNum * copy_number)/(copy_number + rndNum);
		
		Body bBody = (Body) b.getValue(this.BODY);
		List<Surface> bBodSurfaces = bBody.getSurfaces();
		
		if (collisionCheck.test(bBodSurfaces) && rndNum < transfer_probability) {
			this._noPlasmidPigment = b.getString(PIGMENT);
			b.reg().duplicate(a);
			Log.out(Tier.DEBUG, "Conjugated agent: "+b.getXml());
			return true;
		}
		return false;
	}
	
	protected boolean checkPlasmids(AspectReg aspects) {
		if (this._plasmidList.isEmpty())
			return false;
		else {
			Iterator<Object> itrtr = this._plasmidList.iterator();
			while (itrtr.hasNext()) {
				String plasmidName = itrtr.next().toString();
				if (aspects.isLocalAspect(plasmidName))
					return true;
			}
			return false;
		}
	}
	
	public void segregationLoss(Agent lAgent, AspectReg lAspects) {
		if (checkPlasmids(lAspects)) {
			segregationLoss(lAgent);
		}
	}
	
	protected void segregationLoss(Agent lAgent) {
		Iterator<Object> itr = this._plasmidList.iterator();
		while (itr.hasNext()) {
			String plsmdName = itr.next().toString();
			if (lAgent.isAspect(plsmdName)) {
				@SuppressWarnings("unchecked")
				HashMap<Object, Object> lPlasmid = (HashMap<Object, Object>) lAgent.get(plsmdName);
				double loss_probability = (Double) lPlasmid.get(LOSS_PROB);
				
				double lRndNum = ExtraMath.getUniRandDbl();
				
				if (lRndNum < loss_probability) {
					lAgent.reg().remove(plsmdName);
					lAgent.reg().remove(PILUS_LENGTH);
					lAgent.reg().remove(FITNESS_COST);
					lAgent.set(PIGMENT, this._noPlasmidPigment);
					return;
				}
			}
			else {
				return;
			}
		}
	}

	@Override
	protected void internalStep() {
		// TODO Auto-generated method stub
		for (Map.Entry<Agent, List<String>> plasmidAgent : this._plasmidAgents.entrySet()) {
			Agent a = plasmidAgent.getKey();
			
			Compartment c = a.getCompartment();
			
			Collection<Agent> neighbours = c.agents.treeSearch(a, a.getDouble(this.PILUS_LENGTH));
			
			for (String plsmd : plasmidAgent.getValue()) {
				neighbours.removeIf(new HasAspect(plsmd));
				for (Agent nbr : neighbours) {
					boolean conjugation = this.conjugate(a, nbr, plsmd);
					if (conjugation)
						break;
				}
				neighbours = c.agents.treeSearch(a, a.getDouble(this.PILUS_LENGTH));
			}
		}
	}

}
