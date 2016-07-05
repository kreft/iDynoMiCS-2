package referenceLibrary;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;
import nodeFactory.primarySetters.PileEntry;

/**
 * 
 * One of the biggest perks of this reference library is that it allows for 
 * refactoring without having to update any assignments
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ClassRef
{
	/* ************************************************************************
	 * Helper methods
	 */

	/**
	 * Get all class references from the library including the their path
	 * @return String[]
	 */
	public static String[] getAllOptionsFullPath()
	{
		Field[] fields = ClassRef.class.getFields();
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
	 * get all class references from the library excluding their path
	 * @return String[]
	 */
	public static String[] getAllOptions()
	{
		Field[] fields = ClassRef.class.getFields();
		String[] options = new String[fields.length];
		int i = 0;

		for ( Field f : fields )
			try {
				options[i++] = simplify((String) f.get(new String()));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "problem in ObjectRef field declaretion"
						+ "\n can not obtain all options");
				e.printStackTrace();
			}
		return options;
	}
	
	/**
	 * Get all class references from the library that are part of the specified
	 * package
	 * 
	 * @param classPackage
	 * @return List<String>
	 */
	public static List<String> getAllOptions(String classPackage)
	{
		Field[] fields = ClassRef.class.getFields();
		LinkedList<String> options = new LinkedList<String>();
		for ( Field f : fields )
			try {
				String name = ( (String) f.get( new String() ) );
				if ( name.split( simplify( name ) )[0].equals(classPackage + ".") )
					options.add( simplify( name ) );
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "problem in ObjectRef field declaretion"
						+ "\n can not obtain all options");
				e.printStackTrace();
			}
		return options;
	}
	
	/**
	 * Get the simplified name from a class full path specification String
	 * @param String class including path
	 * @return String
	 */
	public static String simplify(String name)
	{
		String[] parts = name.split("\\.");
		return parts[parts.length-1];
	}
	
	public static String path(String name)
	{
		String[] parts = name.split("\\.");
		String[] path = name.split(parts[parts.length-1]);
		return path[0];
	}

	
	/* ************************************************************************
	 * Class reference library : Aspects - Calculated
	 */
	
	/**
	 * agent surfaces aspect
	 */
	public final static String agentSurfaces =
			new aspect.calculated.AgentSurfaces().getClass().getName();
	
	/**
	 * coccoid radius aspect
	 */
	public final static String coccoidRadius =
			new aspect.calculated.CoccoidRadius().getClass().getName();
	
	/**
	 * component volume state
	 */
	public final static String componentVolumeState =
			new aspect.calculated.ComponentVolumeState().getClass().getName();
	
	/**
	 * simple volume state
	 */
	public final static String simpleVolumeState =
			new aspect.calculated.SimpleVolumeState().getClass().getName();
	
	/**
	 * class reference to the StateExpression
	 */
	public final static String expressionAspect = 
			new aspect.calculated.StateExpression().getClass().getName();
	
	/**
	 * the voxel distribution map
	 */
	public final static String voxelDistributionMap =
			new aspect.calculated.VoxelDistributionMap().getClass().getName();
	
	/* ************************************************************************
	 * Class reference library : Aspects - Event
	 */

	/**
	 * the coccoid division event
	 */
	public final static String coccoidDivision =
			new aspect.event.CoccoidDivision().getClass().getName();
	
	/**
	 * the detect local solute event
	 */
	public final static String detectLocalSolute =
			new aspect.event.DetectLocalSolute().getClass().getName();
	
	/**
	 * the excrete eps event (non-cumulative)
	 */
	public final static String excreteEPS =
			new aspect.event.ExcreteEPS().getClass().getName();
	
	/**
	 * the excrete eps cumulative event
	 */
	public final static String excreteEPSCumulative =
			new aspect.event.ExcreteEPSCumulative().getClass().getName();
	
	/**
	 * the internal production event
	 */
	public final static String internalProduction =
			new aspect.event.InternalProduction().getClass().getName();
	
	/**
	 * the resolve interaction distance event
	 */
	public final static String resolveInteractionDistance =
			new aspect.event.ResolveInteractionDistance().getClass().getName();
	
	/**
	 * the rod division event
	 */
	public final static String rodDivision =
			new aspect.event.RodDivision().getClass().getName();
	
	/**
	 * the simple growth event
	 */
	public final static String simpleGrowth =
			new aspect.event.SimpleGrowth().getClass().getName();
	
	/**
	 * the stochastic move event
	 */
	public final static String stochasticMove =
			new aspect.event.StochasticMove().getClass().getName();
	
	/**
	 * the update body event
	 */
	public final static String updateBody =
			new aspect.event.UpdateBody().getClass().getName();
	
	/* ************************************************************************
	 * Agents, Species and aspect modules
	 */
	
	/**
	 * the agent class
	 */
	public final static String agent =
			new agent.Agent().getClass().getName();
	
	/**
	 * the agent class
	 */
	public final static String species =
			new agent.Species().getClass().getName();
	
	/**
	 * the agent class
	 */
	public final static String aspect =
			new aspect.Aspect().getClass().getName();
	
	/**
	 * the body class
	 */
	public final static String body =
			new agent.Body(new double[] { 0.0 }, 0.0).getClass().getName();
	
	/* ************************************************************************
	 * Process managers
	 */
	
	public final static String processManager =
			"processManager.ProcessManager";
	
	/**
	 * the agent events process manager
	 */
	public final static String agentEvents =
			new processManager.library.AgentEvents().getClass().getName();
	
	/**
	 * basic agent growth process manager
	 */
	public final static String agentGrowth =
			new processManager.library.AgentGrowth().getClass().getName();
	
	/**
	 * agent introduction process manager
	 */
	public final static String agentIntroduction =
			new processManager.library.AgentIntroduction().getClass().getName();
	
	/**
	 * agent mechanical relaxation process manager
	 */
	public final static String agentRelaxation =
			new processManager.library.AgentRelaxation().getClass().getName();
	
	/**
	 * agent stochastiv move manager
	 */
	public final static String agentStochasticMove =
			new processManager.library.AgentStochasticMove().getClass().getName();
	/**
	 * graphical output process manager
	 */
	public final static String GraphicalOutput =
			new processManager.library.GraphicalOutput().getClass().getName();
	
	/**
	 * refresh mass grids process manager
	 */
	public final static String refreshMassGrids =
			new processManager.library.RefreshMassGrids().getClass().getName();
	
	/**
	 * solve chemostat process manager
	 */
	public final static String solveChemostat =
			new processManager.library.SolveChemostat().getClass().getName();
	
	/**
	 * solve transient diffusion process manager
	 */
	public final static String solveDiffusionTransient =
			new processManager.library.SolveDiffusionTransient().getClass().getName();
	
	/**
	 * write xml output process manager
	 */
	public final static String writeXmlOutput =
			new processManager.library.WriteXmlOutput().getClass().getName();
	
	/* ************************************************************************
	 * IdynoMiCS main classes
	 */
	
	/** 
	 * the compartment class
	 */
	public final static String compartment =
			new idynomics.Compartment().getClass().getName();
	
	public final static String speciesLibrary =
			new agent.SpeciesLib().getClass().getName();
	
	/* ************************************************************************
	 * miscellaneous
	 */
	
	/**
	 * the svg exporter class
	 */
	public final static String svgExport =
			new dataIO.SvgExport().getClass().getName();
	
	/**
	 * the pov ray exporter class
	 */
	public final static String povExport =
			new dataIO.PovExport().getClass().getName();
	
	/**
	 * 
	 */	
	public final static String spatialGrid =
			new grid.SpatialGrid().getClass().getName();
	
	/**
	 * 
	 */
	public static final String reaction =
			new reaction.Reaction().getClass().getName();
	
	public static final String pileEntry =
			nodeFactory.primarySetters.PileEntry.class.getName();
	
	/* ************************************************************************
	 * java classes
	 */
	
	/**
	 * the LinkedList class
	 */
	public final static String linkedList =
			new java.util.LinkedList().getClass().getName();
	
	/**
	 * the HashMap class
	 */
	public final static String hashMap =
			new java.util.HashMap().getClass().getName();

}
