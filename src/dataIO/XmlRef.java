package dataIO;

/**
 * \brief Single class that holds the naming of all XML tags and attributes,
 * one structured place to add or make changes.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class XmlRef
{
	
	/* Nodes  */
	////////////
	
	/**
	 * Aspect loaded by aspect interface.
	 */
	public static String aspect = "aspect";
	
	/**
	 * An item in a list or a hashmap.
	 */
	public static String item = "item";
	
	/**
	 * Agent node.
	 */
	public static String agent = "agent";
	
	/**
	 * Process manager node.
	 */
	public static String process = "process";
	
	/**
	 * Assigns species module to be part of species description.
	 */
	public static String speciesModule = "speciesModule";
	
	/**
	 * Parameter node, used to set general parameters.
	 */
	public static String parameter = "param";
	
	/**
	 * Indicates {@code surface.Point} object.
	 */
	public static String point = "point";
	
	/**
	 * Indicates constant in expression.
	 */
	public static String constant = "constant";
	
	/**
	 * Indicates a solute.
	 */
	public static String solute = "solute";
	
	/**
	 * Indicates an mathematical expression.
	 */
	public static String expression = "expression";
	
	/**
	 * Indicates a grid voxel.
	 */
	public static String voxel = "vox";
	
	/**
	 * Indicates a reaction: could be environmental or agent-based.
	 */
	public static String reaction = "reaction";
	
	/**
	 * Tag for the stoichiometry of a reaction.
	 */
	public static String stoichiometry = "stoichiometric";
	
	/**
	 * Tag for a component of a reaction.
	 */
	public static String component = "component";
	
	/**
	 * Tag for a stoichiometric constant of a component of a reaction.
	 */
	public static String coefficient = "coefficient";
	
	/* Container Nodes */
	/////////////////////
	
	/**
	 * Encapsulates the entire simulation.
	 */
	public static String simulation = "simulation";
	
	/**
	 * Timer node.
	 */
	public static String timer = "timer";
	
	/**
	 * Encapsulates all simulation-wide parameters.
	 */
	// TODO remove?
	@Deprecated
	public static String generalParams = "general";
	
	/**
	 * Encapsulates all species definitions for a simulation.
	 */
	public static String speciesLibrary = "speciesLib";
	
	/**
	 * Encapsulates all common environmental reactions for a simulation.
	 */
	public static String reactionLibrary = "reactionLib";
	
	/**
	 * An agent species.
	 */
	public static String species = "species";
	
	/**
	 * Encapsulates all content associated with a single compartment.
	 */
	public static String compartment = "compartment";
	
	/**
	 * Encapsulates the child node defining the shape of a compartment.
	 */
	public static String compartmentShape = "shape";
	
	/**
	 * Encapsulates the child node for each dimension of a shape.
	 */
	public static String shapeDimension = "dimension";
	
	/**
	 * Tag for the boolean denoting whether a dimension is cyclic (true) or
	 * not (false).
	 */
	public static String isCyclic = "isCyclic";
	
	/**
	 * Encapsulates the child node for a dimension boundary.
	 */
	public static String dimensionBoundary = "boundary";
	
	/**
	 * Tag for all extra-cellular reactions in the compartment, i.e. those
	 * that are not controlled by an {@code Agent}.
	 */
	public static String reactions = "reactions";

	/**
	 * Encapsulates all agents for one compartment.
	 */
	public static String agents = "agents";

	/**
	 * Encapsulates the environment (solutes, environmental reactions, etc) for
	 * one compartment.
	 */
	public static String environment = "environment";
	
	/**
	 * Encapsulates all process managers for one compartment.
	 */
	public static String processManagers = "processManagers";
	
	/* Attributes */
	////////////////
	
	/**
	 * General name attribute.
	 */
	public static String nameAttribute = "name";
	
	/**
	 * General value attribute.
	 */
	public static String valueAttribute = "value";
	
	/**
	 * Type indicates object type for aspect nodes.
	 */
	public static String typeAttribute = "type";
	
	/**
	 * Indicates XMLable java class.
	 */
	public static String classAttribute = "class";
	
	/**
	 * Indicates package of XMLable java class.
	 */
	public static String packageAttribute = "package";
	
	/**
	 * Indicates key for hashmap.
	 */
	public static String keyAttribute = "key";
	
	/**
	 * Indicates object type for the hashmap key.
	 */
	public static String keyTypeAttribute = "keyType";
	
	/**
	 * Attribute can hold a comment (has no simulation effects).
	 */
	public static String commentAttribute = "comment";
	
	/**
	 * Attribute can hold (up to three) dimension names. 
	 */
	// TODO check this fits in with current model building practice
	public static String dimensionNamesAttribute = "dimensions";
	
	/**
	 * Attribute can hold a target resolution as a {@code double}.
	 */
	public static String targetResolutionAttribute = "targetResolution";
	
	/**
	 * Indicates output folder (set as simulation attribute).
	 */
	// TODO change to "outputFolder"?
	public static String outputFolder = "outputfolder";
	
	/**
	 * Verbosity of log messages.
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
	 * Comma separated string of {@code double}s that indicates a spatial
	 * point position in the compartment.
	 */
	public static String position = "position";
	
	/**
	 * Comma separated string of {@code int}s that indicates a grid voxel in
	 * the compartment.
	 */
	public static String coordinates = "coord";

	/**
	 * Indicates film layer thickness for diffusion of chemical species.
	 */
	public static String layerThickness = "layerThickness";
	
	/**
	 * The "default" diffusivity in the bulk/solute.
	 */
	public static String defaultDiffusivity = "defaultDiffusivity";
	
	/**
	 * Diffusivity in the biofilm.
	 */
	public static String biofilmDiffusivity = "biofilmDiffusivity";
	
	/**
	 * Indicates a threshold double value.
	 */
	public static String threshold = "threshold";
	
	/**
	 * Indicates a solute concentration.
	 */
	public static String concentration = "concentration";
	
	//////////// NOT sorted yet
	
	/**
	 * Priority of a process manager.
	 */
	public static String processPriority = "priority";

	/**
	 * Time for the first timestep of a process manager.
	 */
	public static String processFirstStep = "firstStep";

	/**
	 * Time step size for a process manager.
	 */
	public static String processTimeStepSize = "timerStepSize";
	
	/**
	 * TODO
	 */
	public static String inputAttribute = "input";
	
	/**
	 * Fields that can be set by the user.
	 */
	public static String fields = "fields";

	/**
	 * extreme min
	 */
	public static String min = "min";
	
	/**
	 * extreme max
	 */
	public static String max = "max";

	/**
	 * TODO
	 */
	public static String solutes = "solutes";

	/**
	 * Seed for the random number generator.
	 */
	public static String seed = "randomSeed";

	/**
	 * Object identity number.
	 */
	public static String identity = "identity";

	/**
	 * TODO
	 */
	public static String boundaryPartner = "partner";

	/**
	 * TODO
	 */
	public static String gridMethod = "gridMethod";

	/**
	 * TODO
	 */
	public static String variable = "variable";

	/**
	 * TODO
	 */
	public static String spawnNode = "spawn";

	/**
	 * Tag for the (integer) number of agents to create new.
	 */
	public static String numberOfAgents = "number";

	/**
	 * Tag for the region of space in which to spawn new agents.
	 */
	public static String spawnDomain = "domain";

}
