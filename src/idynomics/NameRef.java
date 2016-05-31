package idynomics;

/**
 * \brief Aspect name references.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class NameRef
{
	/**
	 * Agent state references
	 */
	
	/**
	 * TODO
	 */
	public static String agentBody = "body";

	/**
	 * TODO
	 */
	public static String agentPulldistance = "pullDistance";
	
	/**
	 * TODO
	 */
	public static String agentPullStrength = "pullStrength";
	
	/**
	 * TODO
	 */
	public static String bodyRadius = "radius";

	/*
	 * TODO
	 */
	public static String bodyLength = "#bodyLength";
	
	/**
	 * TODO
	 */
	public static String isLocated = "#isLocated";
	
	/**
	 * TODO
	 */
	public static String bodyUpdate = "updateBody";
	
	/**
	 * NOTE: linker springs will be reworked later, subject to change.
	 */
	public static String filialLinker = "filialLinker";

	/**
	 * List with all surface objects associated with the object
	 */
	public static String surfaceList = "surfaces";

	/**
	 * the solute name for the default BiomassGrid (the grid in which all 
	 * biomass/biofilm is represented
	 */
	public static String defaultBiomassGrid = "biomass";
	
	/**
	 * list with reactions owned by the agent
	 */
	// FIXME what is the difference between this and XmlLabel.reactions?
	public static String agentReactions = "reactions";

	/**
	 * TODO
	 */
	public static String agentMass = "mass";
	
	/**
	 * Agent mass that should trigger division.
	 */
	public static String divisionMass = "divisionMass";
	/**
	 * 
	 */
	public static String mumMassFrac = "mumMassFrac";
	/**
	 * 
	 */
	public static String mumMassFracCV = "mumMassFracCV";
	
	/**
	 * TODO
	 */
	public static String agentLinks = "linkedAgents";

	/**
	 * TODO
	 */
	public static String linkerDistance = "linkerDist";

	/**
	 * TODO
	 */
	public static String agentUpdateBody = "updateBody";

	/**
	 * TODO
	 */
	public static String agentDivide = "divide";

	/**
	 * TODO
	 */
	public static String agentVolumeDistributionMap = "volumeDistribution";

	/**
	 * TODO
	 */
	public static String agentDensity = "density";

	/**
	 * TODO
	 */
	public static String agentVolume = "volume";

	/**
	 * TODO
	 */
	public static String internalProducts = "internalProducts";

	/**
	 * TODO
	 */
	public static String productEPS = "eps";

	/**
	 * TODO
	 */
	public static String maxInternalEPS = "maxInternalEPS";

	/**
	 * TODO
	 */
	public static String epsSpecies = "epsSpecies";

	/**
	 * TODO
	 */
	public static String internalProduction = "internalProduction";

	/**
	 * Reference tag for the growth event.
	 */
	// NOTE This may be merged with internalProduction.
	public static String growth = "growth";
	
	/**
	 * TODO
	 */
	public static String growthRate = "specGrowthRate";

	/**
	 * TODO
	 */
	public static String agentPreferencedistance = "prefDist";

	/**
	 * TODO
	 */
	public static String agentPreferenceIdentifier = "prefIdentifier";

	/**
	 * TODO
	 */
	public static String agentAttachmentPreference = "preference";

	/**
	 * TODO
	 */
	public static String agentCurrentPulldistance = "#curPullDist";

	/**
	 * TODO
	 */
	public static String agentStochasticStep = "stochasticStep";

	/**
	 * TODO
	 */
	public static String agentStochasticDirection = "stochasticDirection";

	/**
	 * TODO
	 */
	public static String agentStochasticPause = "stochasticPause";

	/**
	 * TODO
	 */
	public static String agentStochasticDistance = "stochasticDistance";
}
