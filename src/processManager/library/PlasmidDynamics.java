/**
 * 
 */
package processManager.library;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
	public String PLASMID_LOSS = AspectRef.agentPlasmidLoss;
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
	public final static String PILUS_LENGTH = "pili_length";
	public final static String TRANS_FREQ = "transfer_frequency";
	public final static String ASPECTS_TRANS = "aspects_change";
	
	/**
	 * List of plasmids for which conjugation and segregation functions are called.
	 */
	protected static List<Object> _plasmidList;
	
	/**
	 * Hashmap of all agents with plasmids it contains.
	 */
	private HashMap<Agent, Set<String>> _plasmidAgents = new HashMap<Agent, Set<String>>();

	/**
	 * Hashmap of agents and the time at which have undergone conjugation
	 */
	private HashMap<Agent, Double> _previousConjugated = new HashMap<Agent, Double>();
	
	/**
	 * List of aspects to be copied from donor to recipient during conjugative transfer.
	 */
	private Set<String> _aspectsToCopy = new HashSet<String>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		PlasmidDynamics._plasmidList = (LinkedList<Object>) this.getOr(PLASMID, null);
		Iterator<Object> itr = PlasmidDynamics._plasmidList.iterator();
		while (itr.hasNext()) {
			String plsmd = itr.next().toString();
			for (Agent agent: this._agents.getAllAgents())
			{
				if (agent.isAspect(plsmd))
				{
					if (this._plasmidAgents.containsKey(agent))
						this._plasmidAgents.get(agent).add(plsmd);
					else {
						Set<String> temp = new HashSet<String>();
						temp.add(plsmd);
						this._plasmidAgents.put(agent, temp);
					}
//					if (!agent.isAspect(PILUS_LENGTH))
//						agent.set(this.PILUS_LENGTH, 6.0);
					Double simple_growth = agent.getDouble(this.GROWTH_RATE);
					Map<String,Double> internal_production = (HashMap<String,Double>)
							agent.getOr(this.PRODUCTION_RATE, null);
					Double fitness_cost = agent.getDouble(this.FITNESS_COST);
					if (simple_growth != null && !(simple_growth.isNaN()))
						agent.set(this.GROWTH_RATE, simple_growth*(1.0-fitness_cost));
					if (internal_production != null) {
						internal_production.replaceAll((k,v) -> v*(1.0-fitness_cost));
						agent.set(this.PRODUCTION_RATE, internal_production);
					}
				}
			}
		}
		Log.out(Tier.DEBUG, "Plasmid operations initialised");
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
	protected boolean conjugate(Agent a, List<Agent> neighbours, String plasmid) {
		if (neighbours.isEmpty())
			return false;
		HashMap<Object, Object> newPlasmid = (HashMap<Object, Object>) a.get(plasmid);
		double transfer_probability = (Double) newPlasmid.get(TRANS_PROB);
		double pilusLength = (Double) newPlasmid.get(PILUS_LENGTH);
		double transfer_frequency = (Double) newPlasmid.get(TRANS_FREQ);
		String[] aspects_transfer = (String[]) newPlasmid.get(ASPECTS_TRANS);
		this._aspectsToCopy.addAll(Arrays.asList(aspects_transfer));
//		double copy_number = (Double) newPlasmid.get(COPY_NUM);
		Body aBody = (Body) a.getValue(this.BODY);
		List<Surface> aBodSurfaces = aBody.getSurfaces();
		Compartment comp = a.getCompartment();
		
		this._aspectsToCopy.addAll(Arrays.asList(plasmid, this.FITNESS_COST, this.COOL_DOWN_PERIOD));
		
		if (IsLocated.isLocated(a) && !comp.isDimensionless()) {
			//biofilm
			boolean nbrFound = false;
			Shape compartmentShape = comp.getShape();
			Collision iter = new Collision(compartmentShape);
			double numLoops = pilusLength/0.25;
			for (int n = 0; n <= numLoops; n++) {
				double minDist = n*0.25;
				if (numLoops - n < 1.0)
					minDist = minDist+(numLoops-n);
				Predicate<Collection<Surface>> collisionCheck = new AreColliding<Collection<Surface>>(aBodSurfaces, iter, minDist);
				for (Agent nbr: neighbours) {
					Body nbrBody = (Body) nbr.getValue(this.BODY);
					List<Surface> bBodSurfaces = nbrBody.getSurfaces();
					double probCheck = ExtraMath.getUniRandDbl();
					if (collisionCheck.test(bBodSurfaces)) {
						nbrFound = true;
						if (probCheck < transfer_probability) {
							sendPlasmid(a, nbr, plasmid);
							return true;
						}
					}
				}
				if (nbrFound)
					return false;
			}
		}
		else {
			//chemostat
			int numNeighbours = neighbours.size();
			double probToScreen = transfer_frequency * numNeighbours * _timeStepSize / (numNeighbours + 1);
			int numCellsScreen = (int) Math.floor(probToScreen);
			double remainder = probToScreen - numCellsScreen;
			double rndDbl = ExtraMath.getUniRandDbl();
			if (rndDbl < remainder) {
				numCellsScreen++;
			}
			if (numCellsScreen < neighbours.size()) {
				Random randomSelector = new Random();
				for (int i = 0; i < numCellsScreen; i++) {
					Agent nbr = neighbours.get(randomSelector.nextInt(neighbours.size()));
					sendPlasmid(a, nbr, plasmid);
					neighbours.remove(nbr);
				}
				return true;
			}
			else {
				for (Agent nbr : neighbours) {
					sendPlasmid(a, nbr, plasmid);
				}
				return true;
			}
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
	 * @return true or false indicating whether the transfer was successful or not
	 */
	private void sendPlasmid(Agent dnr, Agent nbr, String plasmid) {
		Iterator<String> aspectToCopy = this._aspectsToCopy.iterator();
		while (aspectToCopy.hasNext()) {
			String aspectName = aspectToCopy.next().toString();
			nbr.set(aspectName, dnr.get(aspectName));
		}
		if (this._plasmidAgents.containsKey(nbr))
			this._plasmidAgents.get(nbr).add(plasmid);
		else {
			Set<String> temp = new HashSet<String>();
			temp.add(plasmid);
			this._plasmidAgents.put(nbr, temp);
		}
	}
	
	/**
	 * \brief Get hashmap of agents with plasmids
	 * 
	 */
	public static List<Object> getAllPlasmids() {
		return _plasmidList;
	}

	@Override
	protected void internalStep() {
		// TODO Auto-generated method stub
		Log.out(Tier.DEBUG, "Plasmid Dynamics internal step starting");
		this._aspectsToCopy.clear();
		double currentTime = _timeForNextStep - _timeStepSize;
		HashMap<Agent, Set<String>> currentPlasmidAgents = new HashMap<Agent, Set<String>>(); 
		this._plasmidAgents.forEach(currentPlasmidAgents::putIfAbsent);
		for (Map.Entry<Agent, Set<String>> plasmidAgent : currentPlasmidAgents.entrySet()) {
			Agent donor = plasmidAgent.getKey();
			Compartment c = donor.getCompartment();
			
			List<Agent> neighbours = new LinkedList<Agent>();
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
