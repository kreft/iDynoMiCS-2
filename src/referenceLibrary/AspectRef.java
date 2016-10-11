package referenceLibrary;

import java.lang.reflect.Field;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief Aspect name references.
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
	 */
	public final static String defaultBiomassGrid = "biomass";
	
	/**
	 * List of names of events each agent should perform during a process
	 * manager step.
	 */
	public static String agentEventNames = "eventNames";
	
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
	public final static String agentPigment = "pigment";

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
}
