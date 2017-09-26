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
	
	/**
	 * Aspects of the agents.
	 */
	public String PLASMID = AspectRef.plasmidList;
	public String FITNESS_COST = AspectRef.agentFitnessCost;
	public String BODY = AspectRef.agentBody;
	public String PIGMENT = AspectRef.agentPigment;
	public String PLASMID_LOSS = AspectRef.agentPlasmidLoss;
	
	/**
	 * Plasmid hashmap items.
	 */
	public String TRANS_PROB = AspectRef.transferProbability;
	public String LOSS_PROB = AspectRef.lossProbability;
	public String COPY_NUM = AspectRef.copyNumber;
	public String PILUS_LENGTH = AspectRef.pilusLength;
	public String TRANS_FREQ = AspectRef.transferFrequency;
	public String ASPECTS_TRANS = AspectRef.aspectsToTransfer;
	public String COOL_DOWN_PERIOD = AspectRef.coolDownPeriod;
	
	/**
	 * List of plasmids for which conjugation and segregation functions are called.
	 */
	protected static List<Object> _plasmidList;
	
	/**
	 * Hashmap of all agents and with the map of plasmids each contains.
	 */
	private HashMap<Agent, Map<String, Double>> _plasmidAgents = 
			new HashMap<Agent, Map<String, Double>>();

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
	private Double _currentTime = this.getTimeForNextStep();
	
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
				}
			}
		}
		Log.out(Tier.DEBUG, "Plasmid operations initialised");
	}
	
	/*************************************************************************
	 * CONJUGATION
	 ************************************************************************/
	
	/**
	 * \brief Plasmid transfer via conjugation. At each time step we check the pilus length
	 * and determine the number of transfer that can happen.
	 * 
	 * @param agent a: Donor agent
	 * @param Collection<Agent>: neighbours of the donor
	 * @param plasmid: plasmid undergoing conjugation
	 */
	@SuppressWarnings("unchecked")
	protected boolean conjugate(Agent a, List<Agent> neighbours, String plasmid, Double tPlasmid) {
		/*
		 * No need to proceed if there are no neighbours to receive the plasmid.
		 */
		if (neighbours.isEmpty())
			return false;
		
		/*
		 * Retrieve the plasmid hashmap from the donor agent. 
		 */
		HashMap<Object, Object> newPlasmid = (HashMap<Object, Object>) a.get(plasmid);
		
		/*
		 * Cool down period for the plasmid after conjugation.
		 */
		double cool_down = (Double) newPlasmid.get(COOL_DOWN_PERIOD);
		
		/*
		 * Number of times the donor can attempt conjugation between two timesteps.
		 */
		int numTransfers = (int) Math.floor(this.getTimeStepSize()/cool_down);
		if ((this._currentTime - _timeStepSize) % cool_down == 0)
			numTransfers++;
		
		/*
		 * check if the donor has conjugated before. 
		 */
		if (!this._previousConjugated.isEmpty() && this._previousConjugated.containsKey(a)) {
			/*
			 * check if enough time has passed for the donor to be able to conjugate again
			 */
			if((this._currentTime + _timeStepSize) >= (this._previousConjugated.get(a) + cool_down)) {
				tPlasmid = this._previousConjugated.get(a) + cool_down;
				this._previousConjugated.remove(a);
			}
			/*
			 * If it has undergone conjugation within cool down time, then return. 
			 */
			else {
				return false;
			}
		}
		
		double transfer_probability = (Double) newPlasmid.get(TRANS_PROB);
		double maxPiliLength = (Double) newPlasmid.get(PILUS_LENGTH);
		double transfer_frequency = (Double) newPlasmid.get(TRANS_FREQ);
		String[] aspects_transfer = (String[]) newPlasmid.get(ASPECTS_TRANS);
		this._aspectsToCopy.addAll(Arrays.asList(aspects_transfer));
		
		Body aBody = (Body) a.getValue(this.BODY);
		List<Surface> aBodSurfaces = aBody.getSurfaces();
		Compartment comp = a.getCompartment();
		
		//Add the plasmid name to the list of aspects to be copied to recipient.
		this._aspectsToCopy.addAll(Arrays.asList(plasmid));
		
		//Calculate pili length from the extension speed. Should be between 0 and provided max.
		double currentPiliLength = this._piliExtensionSpeed * (this._currentTime - tPlasmid);
		if (currentPiliLength > maxPiliLength) {
			currentPiliLength = maxPiliLength;
		}
		if (currentPiliLength < 0) {
			currentPiliLength = 0;
		}
		
		//Check if compartment is dimensionless or not and agents are located.
		if (IsLocated.isLocated(a) && !comp.isDimensionless()) {
			/**
			 * Biofilm scenario.
			 */
			
			boolean pilusAttached = false;
			Shape compartmentShape = comp.getShape();
			Collision iter = new Collision(compartmentShape);
			
			/*
			 * We check for neighbours at every 0.01 distance from donor surface
			 * until current pilus length is reached. 
			 */
			double numLoops = currentPiliLength * 100.0;
			int transferTry = 0;
			for (int n = 0; n <= numLoops; n++) {
				double minDist = n/100.0;
				if (numLoops - n < 1.0)
					minDist = minDist+(numLoops-n);
				Predicate<Collection<Surface>> collisionCheck = 
						new AreColliding<Collection<Surface>>(aBodSurfaces, iter, minDist);
				for (Agent nbr: neighbours) {
					Body nbrBody = (Body) nbr.getValue(this.BODY);
					List<Surface> bBodSurfaces = nbrBody.getSurfaces();
					
					if (collisionCheck.test(bBodSurfaces)) {
						pilusAttached = true;
						transferTry++;
						double probCheck = ExtraMath.getUniRandDbl();
						if (probCheck < transfer_probability) {
							double sendTime = (minDist/this._piliExtensionSpeed) + 
									this._currentTime - this.getTimeStepSize();
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
			/**
			 * Chemostat scenario
			 */
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
		/** 
		 * The internal step for plasmid dynamics, which will search for all agents with plasmids.
		 * The plasmids need to listed under the process manager as an item of the linked list "plasmids"
		 * Retrieves all agents with those plasmids and searches for their neighbours within distance of pilus length
		 * which is determined by the time of simulation and speed of plasmid - currently fixed at 144 micrometer/hr
		 */
		
		Log.out(Tier.DEBUG, "Plasmid Dynamics internal step starting");
		this._currentTime = this.getTimeForNextStep();
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
				boolean conjugation = false;
				conjugation = this.conjugate(donor, neighbours, plsmd, plasmidsInDonor.get(plsmd));
				if (conjugation) {
					Log.out(Tier.DEBUG, "Plasmid sent!");
				}
			}
		}
	}
}