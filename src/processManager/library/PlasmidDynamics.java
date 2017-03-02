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
import agent.predicate.IsLocated;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
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
//	public String PILUS_LENGTH = AspectRef.pilusLength;
	public String BODY = AspectRef.agentBody;
	public String PIGMENT = AspectRef.agentPigment;
	public String COOL_DOWN_PERIOD = AspectRef.coolDownTime;
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
	public final static String LOSS_PIGMENT = "loss_pigment";
	public final static String PILUS_LENGTH = "pili_length";
	
	/**
	 * List of plasmids for which conjugation and segregation functions are called.
	 */
	private List<Object> _plasmidList;
	
	/**
	 * Hashmap of all agents with plasmids it contains.
	 */
	private HashMap<Agent, List<String>> _plasmidAgents = new HashMap<Agent, List<String>>();

	/**
	 * Hashmap of agents and the time at which have undergone conjugation
	 */
	private HashMap<Agent, Double> _previousConjugated = new HashMap<Agent, Double>();
	
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
//					if (!agent.isAspect(PILUS_LENGTH))
//						agent.set(this.PILUS_LENGTH, 6.0);
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
	 * @param agent a: Donor agent
	 * @param Collection<Agent>: neighbours of the donor
	 * @param plasmid: plasmid undergoing conjugation
	 */
	@SuppressWarnings("unchecked")
	protected boolean conjugate(Agent a, Collection<Agent> neighbours, String plasmid) {
		if (neighbours.isEmpty())
			return false;
		HashMap<Object, Object> newPlasmid = (HashMap<Object, Object>) a.get(plasmid);
		double transfer_probability = (Double) newPlasmid.get(TRANS_PROB);
		double pilusLength = (Double) newPlasmid.get(PILUS_LENGTH);
//		double copy_number = (Double) newPlasmid.get(COPY_NUM);
		Body aBody = (Body) a.getValue(this.BODY);
		List<Surface> aBodSurfaces = aBody.getSurfaces();
		
		Shape compartmentShape = a.getCompartment().getShape();
		
		if (IsLocated.isLocated(a)) {
			//biofilm
			Collision iter = new Collision(compartmentShape);
			for (int n = 0; n <= pilusLength/0.25; n++) {
				double minDist = n*0.25;
				Predicate<Collection<Surface>> collisionCheck = new AreColliding<Collection<Surface>>(aBodSurfaces, iter, minDist);
				for (Agent nbr: neighbours) {
					Body nbrBody = (Body) nbr.getValue(this.BODY);
					List<Surface> bBodSurfaces = nbrBody.getSurfaces();
					if (collisionCheck.test(bBodSurfaces)) {
						boolean conjTest = sendPlasmid(transfer_probability, nbr, a, plasmid);
						if (conjTest)
							return true;
					}
				}
			}
		}
		else {
			//chemostat
			
		}
		return false;
	}
	
	/**
	 * Function to send plasmid to neighbour.
	 * 
	 * @param transfer_probability: Proabability that the donor plasmid will undrgo conjugation
	 * @param nbr: Neighbour agent who will receive the plasmid
	 * @param dnr: Donor agent with plasmid
	 * @param plasmid: The plasmid to be transferred
	 * @return
	 */
	private boolean sendPlasmid(double transfer_probability, Agent nbr, Agent dnr, String plasmid) {
		double probCheck = ExtraMath.getUniRandDbl();
		if (probCheck < transfer_probability) {
			nbr.set(plasmid, dnr.get(plasmid));
			nbr.set(this.PIGMENT, dnr.get(this.PIGMENT));
			nbr.set(this.FITNESS_COST, dnr.get(this.FITNESS_COST));
			nbr.set(this.COOL_DOWN_PERIOD, dnr.get(this.COOL_DOWN_PERIOD));
//			nbr.set(this.PILUS_LENGTH, a.get(this.PILUS_LENGTH));
			if (this._plasmidAgents.containsKey(nbr))
				this._plasmidAgents.get(nbr).add(plasmid);
			else
				this._plasmidAgents.put(nbr, Arrays.asList(plasmid));
			return true;
		}
		return false;
	}
	
	/**
	 * \brief Get hashmap of agents with plasmids
	 * 
	 */
	public HashMap<Agent, List<String>> getAllAgentsWithPlasmids() {
		return _plasmidAgents;
	}

	@Override
	protected void internalStep() {
		// TODO Auto-generated method stub
		double currentTime = _timeForNextStep - _timeStepSize;
		HashMap<Agent, List<String>> currentPlasmidAgents = new HashMap<Agent, List<String>>(); 
		this._plasmidAgents.forEach(currentPlasmidAgents::putIfAbsent);
		for (Map.Entry<Agent, List<String>> plasmidAgent : currentPlasmidAgents.entrySet()) {
			Agent donor = plasmidAgent.getKey();
			Compartment c = donor.getCompartment();
			
			Collection<Agent> neighbours = new LinkedList<Agent>();
			if (IsLocated.isLocated(donor)) {
				neighbours = c.agents.getAllLocatedAgents();
			}
			else {
				neighbours = c.agents.getAllUnlocatedAgents();
			}
			for (String plsmd : plasmidAgent.getValue()) {
				Predicate<Agent> hasPlasmid = new HasAspect(plsmd);
				neighbours.removeIf(hasPlasmid);
				boolean conjugation = false;
				if (this._previousConjugated.isEmpty() || !this._previousConjugated.containsKey(donor)) {
					Log.out(Tier.DEBUG, "Conjugate Function called.");
					conjugation = this.conjugate(donor, neighbours, plsmd);
				}
				else if(currentTime >= (this._previousConjugated.get(donor)+donor.getDouble(this.COOL_DOWN_PERIOD))) {
					this._previousConjugated.remove(donor);
				}
				if (conjugation) {
					this._previousConjugated.put(donor, currentTime);
				}
			}
		}
	}

}
