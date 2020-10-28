package referenceLibrary;

import java.lang.reflect.Field;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief Aspect name references.
 * 
 * NOTE: If we want to allow overwriting the AspectRef's cannot be final.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AspectRef
{


	public static String[] getAllOptions()
	{
		Field[] fields = AspectRef.class.getFields();
		String[] options = new String[fields.length];
		int i = 0;

		for ( Field f : fields )
			try {
				options[i++] = (String) f.get(new String());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "problem in ObjectRef field declaretion"
						+ "\n can not obtain all options");
				e.printStackTrace();
			}
		return options;
	}
	
	/**
	 * Agent state references
	 */

	/**
	 * The time when an agent was born.
	 */
	public final static String birthday = "birthday";
	
	/**
	 * The time at which an agent died.
	 */
	public final static String deathday = "deathday";
	
	/**
	 * TODO
	 */
	public final static String agentBody = "body";

	/**
	 * TODO
	 */
	public final static String agentPulldistance = "pullDistance";
	
	/**
	 * TODO
	 */
	public final static String agentPullStrength = "pullStrength";
	
	/**
	 * TODO
	 */
	public final static String bodyRadius = "radius";

	/*
	 * TODO
	 */
	public final static String bodyLength = "#bodyLength";
	
	/**
	 * TODO
	 */
	public final static String isLocated = "#isLocated";
	
	/**
	 * TODO
	 */
	public final static String bodyUpdate = "updateBody";
	
	/**
	 * NOTE: linker springs will be reworked later, subject to change.
	 */
	public final static String filialLinker = "filialLinker";

	/**
	 * List with all surface objects associated with the object
	 */
	public final static String surfaceList = "surfaces";

	/**
	 * the solute name for the default BiomassGrid (the grid in which all 
	 * biomass/biofilm is represented
	 * 
	 * FIXME: consider renaming to something less generic than "biomass" (also
	 * update the test protocols if done so).
	 */
	public final static String defaultBiomassGrid = "biomass";
	
	/**
	 * List of names of events each agent should perform during a process
	 * manager step.
	 */
	public final static String agentEventNames = "eventNames";

	/**
	 * generic spine function { @Link aspect.calculated.ComponentExpression } 
	 * for rods in compartment that do not have a individual spine function
	 * defined
	 */
	public final static String genreicSpineFunction = "genreicSpineFunction";
	
	/**
	 * list with reactions owned by the agent
	 */
	// FIXME what is the difference between this and XmlLabel.reactions?
	public final static String agentReactions = "reactions";

	/**
	 * TODO
	 */
	public final static String agentMass = "mass";

	/**
	 * Agent mass that should trigger division.
	 */
	public final static String divisionMass = "divisionMass";
	/**
	 * 
	 */
	public final static String mumMassFrac = "mumMassFrac";
	/**
	 * 
	 */
	public final static String mumMassFracCV = "mumMassFracCV";
	
	/**
	 * TODO
	 */
	public final static String agentLinks = "linkedAgents";

	/**
	 * TODO
	 */
	public final static String linkerDistance = "linkerDist";

	/**
	 * TODO
	 */
	public final static String agentUpdateBody = "updateBody";

	/**
	 * TODO
	 */
	public final static String agentDivide = "divide";

	/**
	 * TODO
	 */
	public final static String agentVolumeDistributionMap = "volumeDistribution";

	/**
	 * TODO
	 */
	public final static String agentDensity = "density";
	
	/**
	 * TODO
	 */
	public static final String agentRepresentedDensity = "representedDensity";

	/**
	 * TODO
	 */
	public final static String agentVolume = "volume";

	/**
	 * TODO
	 */
	public final static String internalProducts = "internalProducts";

	/**
	 * TODO
	 */
	public final static String internalProduction = "produce";
	
	/**
	 * TODO
	 */
	public final static String productEPS = "eps";

	/**
	 * TODO
	 */
	public final static String maxInternalEPS = "maxInternalEPS";

	/**
	 * TODO
	 */
	public final static String epsSpecies = "epsSpecies";

	/**
	 * TODO
	 */
	public final static String internalProductionRate = "internalProduction";

	/**
	 * Reference tag for the growth event.
	 */
	// NOTE This may be merged with internalProduction.
	public final static String growth = "growth";
	
	/**
	 * TODO
	 */
	public final static String growthRate = "specGrowthRate";

	/**
	 * TODO
	 */
	public final static String agentPreferencedistance = "prefDist";

	/**
	 * TODO
	 */
	public final static String agentPreferenceIdentifier = "prefIdentifier";

	/**
	 * TODO
	 */
	public final static String agentAttachmentPreference = "preference";

	/**
	 * TODO
	 */
	public final static String agentCurrentPulldistance = "#curPullDist";

	/**
	 * TODO
	 */
	public final static String agentStochasticStep = "stochasticStep";

	/**
	 * TODO
	 */
	public final static String agentStochasticDirection = "stochasticDirection";

	/**
	 * TODO
	 */
	public final static String agentStochasticPause = "stochasticPause";

	/**
	 * TODO
	 */
	public final static String agentStochasticDistance = "stochasticDistance";

	/**
	 * TODO
	 */
	public final static String agentDivision = "divide";

	/**
	 * 
	 */
	public final static String collisionSearchDistance = "searchDist";

	/**
	 * TODO
	 */
	public final static String collisionPullEvaluation = "evaluatePull";

	/**
	 * TODO
	 */
	public final static String collisionCurrentPullDistance = "#curPullDist";

	/**
	 * TODO
	 */
	public final static String collisionBaseDT = "dtBase";

	/**
	 * TODO
	 */
	public final static String collisionMaxMOvement = "maxMovement";

	/**
	 * TODO
	 */
	public final static String agentStochasticMove = "stochasticMove";

	/**
	 * TODO
	 */
	public final static String collisionRelaxationMethod = "relaxationMethod";

	/**
	 * TODO
	 */
	public final static String agentExcreteEps = "epsExcretion";

	/**
	 * Used by RefreshMassGrids, calls event
	 */
	public final static String massToGrid = "massToGrid";

	/**
	 * TODO
	 */
	public final static String biomass = "biomass";

	/**
	 * TODO
	 */
	public final static String soluteNames = "soluteNames";

	/**
	 * TODO
	 */
	public final static String solver = "solver";

	/**
	 * TODO
	 */
	public final static String solverhMax = "hMax";

	/**
	 * TODO
	 */
	public final static String solverTolerance = "tolerance";
	
	/**
	 * TODO
	 */
	public final static String solverAbsTolerance = "absoluteTolerance";
	
	/**
	 * TODO
	 */
	public final static String solverRelTolerance = "relativeTolerance";
	
	/**
	 * TODO
	 */
	public final static String agentPigment = "pigment";
	
	/**
	 * TODO
	 */
	public final static String redComponent = "redComponent";
	
	/**
	 * TODO
	 */
	public final static String greenComponent = "greenComponent";
	
	/**
	 * TODO
	 */
	public final static String blueComponent = "blueComponent";

	/**
	 * TODO
	 */
	public final static String gridArrayType = "arrayType";

	/**
	 * TODO
	 */
	public final static String visualOutMaxValue = "maxConcentration";

	/**
	 * TODO
	 */
	public final static String soluteName= "solute";

	/**
	 * TODO
	 */
	public final static String filePrefix = "prefix";

	/**
	 * TODO
	 */
	public final static String graphicalOutputWriter = "outputWriter";
	
	/**
	 * Agent can't move
	 */
	public final static String staticAgent = "staticAgent";

	/**
	 * 
	 */
	public static final String tableSpecification = "tableSpecification";

	/**
	 * Threshold on which the system may be considered fully relaxed
	 */
	public static final String stressThreshold = "stressThreshold";

	/**
	 * Rod agent spine stiffness
	 */
	public static final String spineStiffness = "spineStiffness";

	/**
	 * testing some gravity buoyancy implementation
	 */
	public static final String gravity_testing = "gravity_testing";
	
	/**
	 * option to limit duration of biofilm compression due to grav
	 */
	public static final String LimitCompressionDuration = "LimitCompressionDuration";
	
	/**
	 * List of plasmids for which the conjugation and segregation loss are
	 * applicable.
	 */
	public static final String plasmidList= "plasmids";
	
	/**
	 * Fitness cost to the growth rate of the agent with plasmids.
	 */
	public static final String agentFitnessCost = "fitness_cost";
	
	/**
	 * Plasmid loss due to segregation event name.
	 */
	public static final String agentPlasmidLoss = "plasmidLoss";
	
	//Plasmid related aspects: These are defined as items in each plasmid aspect.	
	/**
	 * Transfer probability of the plasmid.
	 */
	public final static String transferProbability = "transfer_probability";
	
	/**
	 * Loss probability due to segregation at cell division.
	 */
	public final static String lossProbability = "loss_probability";
	
	/**
	 * Copy number of the plasmid.
	 */
	public final static String copyNumber = "copy";
	
	/**
	 * Pilus length of the plasmid.
	 */
	public final static String pilusLength = "pili_length";
	
	/**
	 * Transfer frequency of the plasmid in a well-mixed environment.
	 */
	public final static String transferFrequency = "transfer_frequency";
	
	/**
	 * Which of the aspects from the donot agent will be transfered to
	 * recipient with the plasmid
	 */
	public final static String aspectsToTransfer = "aspects_change";
	
	/**
	 * Cool down period for the agent before it can undergo conjugation again.
	 */
	public final static String coolDownPeriod = "cool_down";
	
	/**
	 * Pilus extension speed.
	 */
	public final static String extensionSpeed = "extension_speed";
	
	/**
	 * Pilus retraction speed.
	 */
	public final static String retractionSpeed = "retraction_speed";

	/**
	 * Process manager output filename
	 */
	public static final String fileName = "fileName";

	/**
	 * Include header in csv out.
	 */
	public static final String includeHeader = "includeHeader";

	/**
	 * 
	 */
	public static final String wetDryRatio = "WetDryRatio";
	/**
	 * 
	 */
	public static final String detachmentRate = "detachmentRate" ;
	/**
	 * 
	 */
	public static final String attachmentRate = "attachmentRate";
	
	/**
	 * 
	 */
	public static final String rasterScale = "rasterScale";

	/**
	 * Toggle running processManager in verbose mode generating additional plots
	 * and console output for debugging and understanding of the process 
	 */
	public static final String verbose = "verbose";

	/**
	 * Depth of detachment region eucledian distanceMap.
	 * 
	 */
	public static final String regionDepth = "regionDepth";

	/**
	 * Maximum thickness of a biofilm.
	 * 
	 */
	public static final String maxThickness = "maxThickness";

	/**
	 * Increase speed of agent relaxation by scaling to object with highest
	 * velocity
	 */
	public static final String fastAgentRelaxation = "fastRelaxation";

	/**
	 * force static dt in agent relaxation thereby ignoring max movement,
	 * time leaping and stress threshold (and thus also does not quantify
	 * related variables).
	 */
	public static final String staticAgentTimeStep = "staticAgentTimeStep";

	/**
	 * Maximum number of iterations per agent relaxation timestep.
	 */
	public static final String maxIterations = "maxIterations";

	/**
	 * Map with available agent transitions
	 */
	public static final String differentiationMap = "differentiationMap";

	/**
	 * agent differentiations
	 */
	public static final String agentDifferentiation = "differentiate";

	/**
	 * Initial point for distributed spawner class
	 */
	public static final String spawnerOrient = "orient";

	/**
	 * Agent spacing for distributed spawner class 
	 */
	public static final String spawnerSpacing = "spacing";

	public static final String collisionFunction = "collisionFunction";

	public static final String attractionFunction = "attractionFunction";
	
	public static final String agentDecompression = "agentDecompression";

	public static final String decompressionCellLength = "decompressionCellLength";

	public static final String decompressionThreshold = "decompressionThreshold";

	public static final String traversingFraction = "traversingFraction";

	public static final String vCycles = "vCycles";

	public static final String preSteps = "preSteps";

	public static final String coarseSteps = "coarseSteps";

	public static final String postSteps = "postSteps";

	public static final String disableBulkDynamics = "disableBulkDynamics";

	public static final String dampingFactor = "dampingFactor";

	public static final String filter = "filter";

	public static final String torsionFunction = "torsionFunction";
}
