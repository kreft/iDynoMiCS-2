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
import idynomics.Idynomics;
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
	public final static String COOL_DOWN_PERIOD = "cool_down";
	
	/**
	 * List of plasmids for which conjugation and segregation functions are called.
	 */
	protected static List<Object> _plasmidList;
	
	/**
	 * Hashmap of all agents and the set of plasmids each contains.
	 */
	private HashMap<Agent, Map<String, Double>> _plasmidAgents = new HashMap<Agent, Map<String, Double>>();

	/**
	 * Hashmap of agents and the time at which they last underwent conjugation
	 */
	private HashMap<Agent, Double> _previousConjugated = new HashMap<Agent, Double>();
	
	/**
	 * List of aspects to be copied from donor to recipient during conjugative transfer.
	 */
	private Set<String> _aspectsToCopy = new HashSet<String>();
	
	/**
	 * Current Simulation Time
	 */
	private Double _currentTime;
	
	/**
	 * Speed of pilus extension, taken to be 40 nm/sec = 144 um/hr
	 * See: https://doi.org/10.1073/pnas.0806786105
	 */
	private Double _piliExtensionSpeed = 144.0;
	
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
					if (this._plasmidAgents.containsKey(agent)) {
						this._plasmidAgents.get(agent).put(plsmd, _currentTime);
					}
					else {
						Map<String, Double> temp = new HashMap<String, Double>();
						temp.put(plsmd, _currentTime);
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
	protected boolean conjugate(Agent a, List<Agent> neighbours, String plasmid, Double tPlasmid) {
		if (neighbours.isEmpty())
			return false;
		HashMap<Object, Object> newPlasmid = (HashMap<Object, Object>) a.get(plasmid);
		double cool_down = (Double) newPlasmid.get(COOL_DOWN_PERIOD);
		int numTransfers = (int) Math.floor(_timeStepSize/cool_down);
		if ((this._currentTime - _timeStepSize) % cool_down == 0)
			numTransfers++;
		Log.out(Tier.DEBUG, "Num transfers: "+numTransfers);
		if (!this._previousConjugated.isEmpty() && this._previousConjugated.containsKey(a)) {
			if((this._currentTime + _timeStepSize) >= (this._previousConjugated.get(a) + cool_down)) {
				tPlasmid = this._previousConjugated.get(a) + cool_down;
				this._previousConjugated.remove(a);
			}
			else {
				return false;
			}
		}
		double transfer_probability = (Double) newPlasmid.get(TRANS_PROB);
		double maxPiliLength = (Double) newPlasmid.get(PILUS_LENGTH);
		double transfer_frequency = (Double) newPlasmid.get(TRANS_FREQ);
		
		String[] aspects_transfer = (String[]) newPlasmid.get(ASPECTS_TRANS);
		this._aspectsToCopy.addAll(Arrays.asList(aspects_transfer));
//		double copy_number = (Double) newPlasmid.get(COPY_NUM);
		Body aBody = (Body) a.getValue(this.BODY);
		List<Surface> aBodSurfaces = aBody.getSurfaces();
		Compartment comp = a.getCompartment();
		
		this._aspectsToCopy.addAll(Arrays.asList(plasmid));
		
		double currentPiliLength = this._piliExtensionSpeed * (this._currentTime - tPlasmid);
		if (currentPiliLength > maxPiliLength) {
			currentPiliLength = maxPiliLength;
		}
		if (currentPiliLength < 0) {
			currentPiliLength = 0;
		}
		
		if (IsLocated.isLocated(a) && !comp.isDimensionless()) {
			//biofilm
			boolean pilusAttached = false;
			Shape compartmentShape = comp.getShape();
			Collision iter = new Collision(compartmentShape);
			double numLoops = currentPiliLength * 100.0;
			int transferTry = 0;
			for (int n = 0; n <= numLoops; n++) {
				double minDist = n/100.0;
				if (numLoops - n < 1.0)
					minDist = minDist+(numLoops-n);
				Log.out(Tier.DEBUG, "min Dist: "+minDist);
				Predicate<Collection<Surface>> collisionCheck = new AreColliding<Collection<Surface>>(aBodSurfaces, iter, minDist);
				for (Agent nbr: neighbours) {
					Body nbrBody = (Body) nbr.getValue(this.BODY);
					List<Surface> bBodSurfaces = nbrBody.getSurfaces();
					if (collisionCheck.test(bBodSurfaces)) {
						pilusAttached = true;
						transferTry++;
						double probCheck = ExtraMath.getUniRandDbl();
						if (probCheck < transfer_probability) {
							double sendTime = (minDist/this._piliExtensionSpeed) + this._currentTime;
							sendPlasmid(a, nbr, plasmid, sendTime);
							this._previousConjugated.put(a, sendTime);
							if (transferTry >= numTransfers)
								return true;
						}
						else if (transferTry >= numTransfers)
							return false;
						else
							pilusAttached = false;
					}
				}
			}
			if (pilusAttached)
				return false;
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
			if (numTransfers > 1) {
				numCellsScreen = numCellsScreen * numTransfers;
			}
			if (numCellsScreen < neighbours.size()) {
				Random randomSelector = new Random();
				for (int i = 0; i < numCellsScreen; i++) {
					Agent nbr = neighbours.get(randomSelector.nextInt(neighbours.size()));
					sendPlasmid(a, nbr, plasmid, _currentTime);
					neighbours.remove(nbr);
				}
				return true;
			}
			else {
				for (Agent nbr : neighbours) {
					sendPlasmid(a, nbr, plasmid, _currentTime);
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
	private void sendPlasmid(Agent dnr, Agent nbr, String plasmid, Double localTime) {
		Iterator<String> aspectToCopy = this._aspectsToCopy.iterator();
		while (aspectToCopy.hasNext()) {
			String aspectName = aspectToCopy.next().toString();
			nbr.set(aspectName, dnr.get(aspectName));
		}
		if (this._plasmidAgents.containsKey(nbr))
			this._plasmidAgents.get(nbr).put(plasmid, localTime);
		else {
			Map<String, Double> temp = new HashMap<String, Double>();
			temp.put(plasmid, localTime);
			this._plasmidAgents.put(nbr, temp);
		}
		this._previousConjugated.put(dnr, localTime);
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
		this._currentTime = Idynomics.simulator.timer.getCurrentTime();
		this._aspectsToCopy.clear();
		HashMap<Agent, Map<String, Double>> currentPlasmidAgents = 
				new HashMap<Agent, Map<String, Double>>(); 
		this._plasmidAgents.forEach(currentPlasmidAgents::putIfAbsent);
		for (Map.Entry<Agent, Map<String, Double>> plasmidAgent : currentPlasmidAgents.entrySet()) {
			Agent donor = plasmidAgent.getKey();
			HashMap<String, Double> plasmidsInDonor = new HashMap<String, Double>(plasmidAgent.getValue());
			Compartment c = donor.getCompartment();
			
			List<Agent> neighbours = new LinkedList<Agent>();
			if (IsLocated.isLocated(donor)) {
				neighbours = c.agents.getAllLocatedAgents();
			}
			else {
				neighbours = c.agents.getAllUnlocatedAgents();
			}
			for (String plsmd : plasmidsInDonor.keySet()) {
				Predicate<Agent> hasPlasmid = new HasAspect(plsmd);
				neighbours.removeIf(hasPlasmid);
				this.conjugate(donor, neighbours, plsmd, plasmidsInDonor.get(plsmd));
				boolean conjugation = false;
				if (conjugation) {
					Log.out(Tier.DEBUG, "Plasmid sent!");
				}
			}
		}
	}
}