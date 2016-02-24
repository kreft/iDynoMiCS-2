package dataIO;

/**
 * Single class that holds the naming of all xml tags and attributes, one
 * structured place to add or make changes.
 * @author baco
 *
 */
public class XmlLabel
{
	
	/* Nodes  */
	////////////
	
	/**
	 * aspect loaded by aspect interface
	 */
	public static String aspect = "aspect";
	
	/**
	 * and item in list or hashmap
	 */
	public static String item = "item";
	
	/**
	 * agent node
	 */
	public static String agent = "agent";
	
	/**
	 * process manager node
	 */
	public static String process = "process";
	
	/**
	 * assigns species module to be part of species description
	 */
	public static String speciesModule = "speciesModule";
	
	/**
	 * parameter node, used to set general parameters
	 */
	public static String parameter = "param";
	
	/**
	 * indicates surface.Point object
	 */
	public static String point = "point";
	
	/**
	 * indicates constant in expression
	 */
	public static String constant = "constant";
	
	/**
	 * indicates a solute
	 */
	public static String solute = "solute";
	
	/**
	 * indicates a resolution element of a solute.
	 */
	public static String resolution = "resolution";
	
	/**
	 * indicates an expression.
	 */
	public static String expression = "expression";
	
	/**
	 * indicates a voxel
	 */
	public static String voxel = "vox";
	
	/**
	 * indicates a reaction
	 */
	public static String reaction = "reaction";
	
	/* Container Nodes */
	/////////////////////
	
	/**
	 * encapsulates the entire simulation
	 */
	public static String simulation = "simulation";
	
	/**
	 * encapsulates all simulation wide parameters
	 */
	// TODO remove?
	public static String generalParams = "general";
	
	/**
	 * encapsulates all species definitions for simulation
	 */
	public static String speciesLibrary = "speciesLib";
	
	/**
	 * encapsulates all child nodes defining the compartment shape
	 */
	public static String compartmentShape = "shape";
	
	/**
	 * encapsulates all content associated with a single compartment
	 */
	public static String compartment = "compartment";
	
	/**
	 * The agent species
	 */
	public static String species = "species";
	
	/**
	 * encapsulates all reactions in the compartment
	 */
	public static String reactions = "reactions";

	/**
	 * encapsulates all agents for one compartment
	 */
	public static String agents = "agents";
	
	/**
	 * encapsulates all process managers for one compartment
	 */
	public static String processManagers = "processManagers";
	
	/* Attributes */
	////////////////
	
	/**
	 * general name attribute
	 */
	public static String nameAttribute = "name";
	
	/**
	 * general value attribute
	 */
	public static String valueAttribute = "value";
	
	/**
	 * type indicates object type for aspect nodes
	 */
	public static String typeAttribute = "type";
	
	/**
	 * indicates XMLable java class
	 */
	public static String classAttribute = "class";
	
	/**
	 * indicates package of XMLable java class
	 */
	public static String packageAttribute = "package";
	
	/**
	 * indicates key for hashmap
	 */
	public static String keyAttribute = "key";
	
	/**
	 * indicates object type for the hashmap key
	 */
	public static String keyTypeAttribute = "keyType";
	
	/**
	 * attribute can hold a comment (has no simulation effects)
	 */
	public static String commentAttribute = "comment";
	
	/**
	 * attribute can hold (up to three) dimension names 
	 */
	public static String dimensionNamesAttribute = "dimensions";
	
	/**
	 * attribute can hold a target resolution as double
	 */
	public static String targetResolutionAttribute = "targetResolution";
	
	/**
	 * indicates output folder (set as simulation attribute)
	 */
	public static String outputFolder = "outputfolder";
	
	/**
	 * verbosity of log messages
	 */
	public static String logLevel = "log";
	
	/**
	 * The size of time step that the global {@code Timer} will take.
	 */
	public static String timerStepSize = "stepSize";
	
	/**
	 * The time point at which the simulation will end.
	 */
	public static String endOfSimulation = "endOfSimulation";
	
	/**
	 * comma separated string of doubles that indicates a specific position in
	 * the compartment
	 */
	public static String position = "position";
	
	/**
	 * comma separated string of ints that indicates a specific position in
	 * the compartment
	 */
	public static String coordinates = "coord";

	/**
	 * indicates film layer thickness for diffusion of chemical species
	 */
	public static String layerThickness = "layerThickness";
	
	/**
	 * "default" diffusivity in the bulk/ solute
	 */
	public static String defaultDiffusivity = "defaultDiffusivity";
	
	/**
	 * diffusivity in the biofilm
	 */
	public static String biofilmDiffusivity = "biofilmDiffusivity";
	
	/**
	 * indicates a threshold double value
	 */
	public static String threshold = "threshold";
	
	/**
	 * indicates a concentration
	 */
	public static String concentration = "concentration";

}
