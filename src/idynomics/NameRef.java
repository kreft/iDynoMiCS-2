package idynomics;

public class NameRef {
	
	/**
	 * General xml name references
	 */
	
	/**
	 * Process manager references
	 */
	public static String processPriority = "priority";
	
	/**
	 * Process manager initial step
	 */
	public static String initialStep = "firstStep";

	/**
	 * Agent state references
	 */
	
	/**
	 * 
	 */
	public static String agentBody = "body";

	/**
	 * 
	 */
	public static String agentPulldistance = "pullDistance";
	
	/**
	 * 
	 */
	public static String agentPullStrength = "pullStrength";
	
	/**
	 * 
	 */
	public static String bodyRadius = "radius";

	/**
	 * 
	 */
	public static String bodyLength = "#bodyLength";
	
	/**
	 * 
	 */
	public static String isLocated = "#isLocated";
	
	/**
	 * 
	 */
	public static String bodyUpdate = "updateBody";
	
	/**
	 * NOTE: linker springs will be reworked later, subject to change.
	 */
	public static String fillialLinker = "filialLinker";

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
	public static String agentReactions = "reactions";
}
