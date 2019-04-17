package referenceLibrary;

import java.lang.reflect.Field;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief Single class that holds the naming of all XML tags and attributes,
 * one structured place to add or make changes.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 */
public class XmlRef
{
	public static String[] getAllOptions()
	{
		Field[] fields = XmlRef.class.getFields();
		String[] options = new String[fields.length];
		int i = 0;

		for ( Field f : fields )
			try {
				options[i++] = (String) f.get(new String());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "problem in ObjectRef field declaration"
						+ "\n cannot obtain all options");
				e.printStackTrace();
			}
		return options;
	}
	/* Nodes  */
	////////////
	
	/**
	 * Aspect loaded by aspect interface.
	 */
	public final static String aspect = "aspect";
	
	/**
	 * An item in a list or a hashmap.
	 */
	public final static String item = "item";
	
	/**
	 * Agent node.
	 */
	public final static String agent = "agent";
	
	/**
	 * templateAgent node, used by spawner.
	 */
	public final static String templateAgent = "templateAgent";
	
	/**
	 * Process manager node.
	 */
	public final static String process = "process";
	
	/**
	 * Assigns species module to be part of species description.
	 */
	public final static String speciesModule = "speciesModule";
	
	/**
	 * Parameter node, used to set general parameters.
	 */
	public final static String parameter = "param";
	
	/**
	 * Indicates {@code surface.Point} object.
	 */
	public final static String point = "point";
	
	/**
	 * Indicates constant in expression.
	 */
	public final static String constant = "constant";
	
	/**
	 * Indicates constant in expression.
	 */
	public final static String constants = "constants";
	
	/**
	 * Indicates a solute.
	 */
	public final static String solute = "solute";
	
	/**
	 * Method for setting the diffusivity array of a solute's spatial grid.
	 */
	public final static String diffusivitySetter = "diffusivitySetter";
	
	/**
	 * Indicates an mathematical expression.
	 */
	public final static String expression = "expression";
	
	/**
	 * Indicates a grid voxel.
	 */
	public final static String voxel = "vox";
	
	/**
	 * Indicates a reaction: could be environmental or agent-based.
	 */
	public final static String reaction = "reaction";
	
	/**
	 * Tag for the stoichiometry of a reaction.
	 */
	public final static String stoichiometric = "stoichiometric";
	
	/**
	 * Tag for the stoichiometry of a reaction.
	 */
	public final static String stoichiometry = "stoichiometry";
	
	/**
	 * Tag for a component of a reaction.
	 */
	public final static String component = "component";
	
	/**
	 * Tag for a stoichiometric constant of a component of a reaction.
	 */
	public final static String coefficient = "coefficient";
	
	/* Container Nodes */
	/////////////////////
	
	/**
	 * Encapsulates the entire simulation.
	 */
	public final static String simulation = "simulation";
	
	/**
	 * Timer node.
	 */
	public final static String timer = "timer";
	
	/**
	 * Encapsulates all simulation-wide parameters.
	 */
	// TODO remove?
	@Deprecated
	public final static String generalParams = "general";
	
	/**
	 * Encapsulates all species definitions for a simulation.
	 */
	public final static String speciesLibrary = "speciesLib";
	
	/**
	 * Encapsulates all common environmental reactions for a simulation.
	 */
	public final static String reactionLibrary = "reactionLib";
	
	/**
	 * An agent species.
	 */
	public final static String species = "species";
	
	/**
	 * Encapsulates all content associated with a single compartment.
	 */
	public final static String compartment = "compartment";
	
	/**
	 * Encapsulates the child node defining the shape of a compartment.
	 */
	public final static String compartmentShape = "shape";
	
	/**
	 * Encapsulates the child node for each dimension of a shape.
	 */
	public final static String shapeDimension = "dimension";
	
	/**
	 * Tag for the ResolutionCalculator class that should be used for a
	 * dimension.
	 */
	public final static String resolutionCalculator = "resolutionCalculator";
	
	/**
	 * Tag for the boolean denoting whether a dimension is cyclic (true) or
	 * not (false).
	 */
	public final static String isCyclic = "isCyclic";
	
	/**
	 * Encapsulates the child node for a dimension boundary.
	 */
	public final static String dimensionBoundary = "boundary";
	
	/**
	 * Tag for all extra-cellular reactions in the compartment, i.e. those
	 * that are not controlled by an {@code Agent}.
	 */
	public final static String reactions = "reactions";

	/**
	 * Encapsulates all agents for one compartment.
	 */
	public final static String agents = "agents";

	/**
	 * Encapsulates the environment (solutes, environmental reactions, etc) for
	 * one compartment.
	 */
	public final static String environment = "environment";
	
	/**
	 * Encapsulates all process managers for one compartment.
	 */
	public final static String processManagers = "processManagers";
	
	/* Attributes */
	////////////////
	
	/**
	 * General name attribute.
	 */
	public final static String nameAttribute = "name";
	
	/**
	 * General value attribute.
	 */
	public final static String valueAttribute = "value";
	
	/**
	 * Type indicates object type for aspect nodes.
	 */
	public final static String typeAttribute = "type";
	
	/**
	 * Indicates XMLable java class.
	 */
	public final static String classAttribute = "class";
	
	/**
	 * Indicates package of XMLable java class.
	 */
	public final static String packageAttribute = "package";
	
	/**
	 * Indicates key for hashmap.
	 */
	public final static String keyAttribute = "key";
	
	/**
	 * Indicates object type for the hashmap key.
	 */
	public final static String keyClassAttribute = "keyType";
	
	/**
	 * Attribute can hold a comment (has no simulation effects).
	 */
	public final static String commentAttribute = "comment";
	
	/**
	 * Attribute can hold (up to three) dimension names. 
	 */
	// TODO check this fits in with current model building practice
	public final static String dimensionNamesAttribute = "dimensions";
	
	/**
	 * Attribute can hold a target resolution as a {@code double}.
	 */
	public final static String targetResolutionAttribute = "targetResolution";
	
	/**
	 * Indicates output folder (set as simulation attribute).
	 */
	// TODO change to "outputFolder"?
	public final static String outputFolder = "outputfolder";
	
	/**
	 * Verbosity of log messages.
	 */
	public final static String logLevel = "log";
	
	/**
	 * The size of time step that the global {@code Timer} will take.
	 */
	public final static String timerStepSize = "stepSize";
	
	/**
	 * The time point at which the simulation will end.
	 */
	public final static String endOfSimulation = "endOfSimulation";
	
	/**
	 * Comma separated string of {@code double}s that indicates a spatial
	 * point position in the compartment.
	 */
	public final static String position = "position";
	
	/**
	 * Comma separated string of {@code int}s that indicates a grid voxel in
	 * the compartment.
	 */
	public final static String coordinates = "coord";

	/**
	 * Indicates film layer thickness for diffusion of chemical species.
	 */
	public final static String layerThickness = "layerThickness";
	
	/**
	 * The "default" diffusivity in the bulk/solute.
	 */
	public final static String defaultDiffusivity = "defaultDiffusivity";
	
	/**
	 * Diffusivity in the biofilm.
	 */
	public final static String biofilmDiffusivity = "biofilmDiffusivity";
	
	/**
	 * Indicates a threshold double value.
	 */
	public final static String threshold = "threshold";
	
	/**
	 * Indicates a solute concentration.
	 */
	public final static String concentration = "concentration";
	
	//////////// NOT sorted yet
	
	/**
	 * Priority of a process manager.
	 */
	public final static String processPriority = "priority";
	
	/**
	 * Priority of any given element.
	 * TODO merge with former (processPriority)
	 */
	public final static String priority = "priority";

	/**
	 * Time for the first timestep of a process manager.
	 */
	public final static String processFirstStep = "firstStep";

	/**
	 * Time step size for a process manager.
	 */
	public final static String processTimeStepSize = "timerStepSize";
	
	/**
	 * TODO
	 */
	public final static String inputAttribute = "input";
	
	/**
	 * Fields that can be set by the user.
	 */
	public final static String fields = "fields";

	/**
	 * extreme min
	 */
	public final static String min = "min";
	
	/**
	 * extreme max
	 */
	public final static String max = "max";

	/**
	 * The class of ResolutionCalculator that a Shape should use.
	 */
	public final static String resCalcClass = "resolutionCalculator";
	
	/**
	 * TODO
	 */
	public final static String solutes = "solutes";

	/**
	 * Seed for the random number generator.
	 */
	public final static String seed = "randomSeed";

	/**
	 * Object identity number.
	 */
	public final static String identity = "identity";

	/**
	 * Name of the compartment for a boundary's partner boundary.
	 */
	public final static String partnerCompartment = "partnerCompartment";

	/**
	 * TODO
	 */
	public final static String gridMethod = "gridMethod";

	/**
	 * TODO
	 */
	public final static String variable = "variable";

	/**
	 * TODO
	 */
	public final static String spawnNode = "spawn";

	/**
	 * Tag for the (integer) number of agents to create new.
	 */
	public final static String numberOfAgents = "number";

	/**
	 * Tag for the region of space in which to spawn new agents.
	 */
	public final static String spawnDomain = "domain";
	/**
	 * Force scalar used to scale the collision algorithm force distance 
	 * functions
	 */
	public final static String forceScalar = "forceScalar";

	/**
	 * 
	 */
	public static final String InstantiableMapLable = "map";

	/**
	 * species modules wrapper
	 */
	public static final String modules = "modules";

	/**
	 * Timer current time (Now)
	 */
	public static final String currentTime = "currentTime";

	/**
	 * Pile node label attribute
	 */
	public static final String nodeLabel = "nodeLabel";

	/**
	 * Pile entry class attribute
	 */
	public static final String entryClassAttribute = "entryClass";

	/**
	 * search distance for nearby eps particles
	 */
	public static final String epsDist = "epsDist";

	/**
	 * agent tree type
	 */
	public static final String tree = "tree";

	/**
	 * 0 or 1 refering to min or max boundary
	 */
	public static final String extreme = "extreme";

	/**
	 * Referring to instantiatable list node
	 */
	public static final String list = "list";

	/**
	 * Referring to instantiatable map node
	 */
	public static final String map = "map";

	public static final String currentIter = "currentIter";

	/**
	 * number of joints for random spawn agents
	 */
	public static final String numberOfJoints = "numberOfJoints";

	/**
	 * number of points to spawn
	 */
	public static final String points = "points";
	
	/**
	 * General range attribute.
	 */
	public final static String rangeAttribute = "range";
	
	/**
	 * Range applicable to this attribute.
	 */
	public final static String rangeForAttribute = "rangeFor";

	/**
	 * volume (for nonspatial compartment)
	 */
	public static final String volume = "volume";

	/**
	 * chemostat volume flowrate
	 */
	public static final String volumeFlowRate = "volumeFlowRate";
	
	/**
	 * transferCoefficient
	 */
	public static final String transferCoefficient = "transferCoefficient";
	
	/**
	 * define transfer coefficient to be volume specific
	 */
	public static final String volumeSpecific = "volumeSpecific";
	
	/**
	 * chemostat volume flowrate for constant chemostat volume
	 */
	public static final String constantVolume = "constantVolume";

	/**
	 * Toggle boundary agent removal on or of
	 */
	public static final String agentRemoval = "agentRemoval";
	
	/**
	 * Indicates sub folder (set for SA or GA).
	 */
	public final static String subFolder = "subfolder";

	/**
	 * Number of global time steps to skip for next xml out
	 */
	public static final String outputskip = "outputskip";
	
	/**
	 * Scaling factor determined by the real to modelled ratio
	 */
	public final static String compartmentScale = "scale";
	
	/**
	 * Actual size of the dimension
	 */
	public final static String realMax = "realMax";
	
	/**
	 * Actual size of the dimension
	 */
	public final static String realMin = "realMin";

	/**
	 * Additional required configuration files to be loaded.
	 */
	public static final String configuration = "configuration";

	public static final String chemicalLibrary = "chemicalLib";

	public static final String chemical = "chemical";

	public static final String formationGibbs = "formationGibbs";

	public static final String composition = "composition";

	public static final String oxidationState = "oxidationState";

	public static final String halfReaction = "halfReaction";

	public static final String metabolicReaction = "metabolicReaction";


	/**
	 * allows for setting exponential (exp) range scaling in sampler
	 */
	public static final String rangeScaleAttribute = "scaling";

	
	/**
	 *  All spawners in the compartment
	 */
	public static final String spawners = "spawners";
	
	
	public static final String spawner = "spawner";
	
	
	public static final String layerShape = "layerShape";
	
	
	public static final String cellShape = "cellShape";
	

	public static final String morphology = "morphology";

	
	public final static String agentBody = "body";

	/**
	 * refers to linAlg orientation object (unit Vector)
	 */
	public static final String orientation = "orientation";
}
