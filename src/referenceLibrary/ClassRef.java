package referenceLibrary;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;

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
		if (name.contains("$"))
		{
			return name.split("\\$")[1];
		}
		String[] parts = name.split("\\.");
		return parts[parts.length-1];
	}
	
	public static String path(String name)
	{
		if (name.contains("$"))
		{
			return name.split("\\$")[0] + "$";
		}
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
			aspect.calculated.AgentSurfaces.class.getName();
	
	/**
	 * coccoid radius aspect
	 */
	public final static String coccoidRadius =
			aspect.calculated.CoccoidRadius.class.getName();
	
	/**
	 * component volume state
	 */
	public final static String componentVolumeState =
			aspect.calculated.ComponentVolumeState.class.getName();
	
	/**
	 * simple volume state
	 */
	public final static String simpleVolumeState =
			aspect.calculated.SimpleVolumeState.class.getName();
	
	/**
	 * class reference to the StateExpression
	 */
	public final static String expressionAspect = 
			aspect.calculated.StateExpression.class.getName();
	
	/**
	 * class reference to the ComponentExpression
	 */
	public final static String componentExpressionAspect = 
			aspect.calculated.ComponentExpression.class.getName();
	
	/**
	 * the voxel distribution map
	 */
	public final static String voxelDistributionMap =
			aspect.calculated.VoxelDistributionMap.class.getName();
	
	/* ************************************************************************
	 * Class reference library : Aspects - Event
	 */

	/**
	 * the coccoid division event
	 */
	public final static String coccoidDivision =
			aspect.event.CoccoidDivision.class.getName();
	
	/**
	 * the detect local solute event
	 */
	public final static String detectLocalSolute =
			aspect.event.DetectLocalSolute.class.getName();
	
	/**
	 * the excrete eps event (non-cumulative)
	 */
	public final static String excreteEPS =
			aspect.event.ExcreteEPS.class.getName();
	
	/**
	 * the excrete eps cumulative event
	 */
	public final static String excreteEPSCumulative =
			aspect.event.ExcreteEPSCumulative.class.getName();
	
	/**
	 * the internal production event
	 */
	public final static String internalProduction =
			aspect.event.InternalProduction.class.getName();
	
	/**
	 * the resolve interaction distance event
	 */
	public final static String resolveInteractionDistance =
			aspect.event.ResolveInteractionDistance.class.getName();
	
	/**
	 * the rod division event
	 */
	public final static String rodDivision =
			aspect.event.RodDivision.class.getName();
	
	/**
	 * the simple growth event
	 */
	public final static String simpleGrowth =
			aspect.event.SimpleGrowth.class.getName();
	
	/**
	 * the stochastic move event
	 */
	public final static String stochasticMove =
			aspect.event.StochasticMove.class.getName();
	
	/**
	 * the update body event
	 */
	public final static String updateBody =
			aspect.event.UpdateBody.class.getName();
	
	/* ************************************************************************
	 * Agents, Species and aspect modules
	 */
	
	/**
	 * the agent class
	 */
	public final static String agent =
			agent.Agent.class.getName();
	
	/**
	 * the agent class
	 */
	public final static String species =
			agent.Species.class.getName();
	
	/**
	 * the agent class
	 */
	public final static String aspect =
			aspect.Aspect.class.getName();
	
	/**
	 * the body class
	 */
	public final static String body =
			agent.Body.class.getName();
	
	/* ************************************************************************
	 * Process managers
	 */
	
	/**
	 * the generic processManager class
	 */
	public final static String processManager =
			processManager.ProcessManager.class.getName();
	
	/**
	 * the agent events process manager
	 */
	public final static String agentEvents =
			processManager.library.AgentEvents.class.getName();
	
	/**
	 * basic agent growth process manager
	 */
	public final static String agentGrowth =
			processManager.library.AgentGrowth.class.getName();
	
	/**
	 * agent introduction process manager
	 */
	public final static String agentIntroduction =
			processManager.library.AgentIntroduction.class.getName();
	
	/**
	 * agent mechanical relaxation process manager
	 */
	public final static String agentRelaxation =
			processManager.library.AgentRelaxation.class.getName();
	
	/**
	 * agent stochastiv move manager
	 */
	public final static String agentStochasticMove =
			processManager.library.AgentStochasticMove.class.getName();
	/**
	 * graphical output process manager
	 */
	public final static String GraphicalOutput =
			processManager.library.GraphicalOutput.class.getName();
	
	/**
	 * refresh mass grids process manager
	 */
	public final static String refreshMassGrids =
			processManager.library.RefreshMassGrids.class.getName();
	
	/**
	 * solve chemostat process manager
	 */
	public final static String solveChemostat =
			processManager.library.SolveChemostat.class.getName();
	
	/**
	 * solve transient diffusion process manager
	 */
	public final static String solveDiffusionTransient =
			processManager.library.SolveDiffusionTransient.class.getName();
	
	public final static String solveDiffusionSteadyState =
			processManager.library.SolveDiffusionSteadyState.class.getName();
	/**
	 * write xml output process manager
	 */
	public final static String writeXmlOutput =
			processManager.library.WriteXmlOutput.class.getName();
	
	public final static String summary =
			processManager.library.Summary.class.getName();
	
	/* ************************************************************************
	 * IdynoMiCS main classes
	 */
	
	/** 
	 * the compartment class
	 */
	public final static String compartment =
			idynomics.Compartment.class.getName();
	
	/**
	 * TODO
	 */
	public final static String speciesLibrary =
			agent.SpeciesLib.class.getName();
	
	
	/* ************************************************************************
	 * Grid classes
	 */
	
	/**
	 * TODO
	 */
	public final static String allSameDiffuse =
			grid.diffusivitySetter.AllSameDiffuse.class.getName();
	
	/**
	 * TODO
	 */
	public final static String scaledIfBiomassPresent =
			grid.diffusivitySetter.ScaledIfBiomassPresent.class.getName();
	
	/* ************************************************************************
	 * boundaries
	 */
	
	/**
	 * TODO
	 */
	public final static String fixedBoundary =
			boundary.spatialLibrary.FixedBoundary.class.getName();
	
	/* ************************************************************************
	 * shape classes
	 */
	
	/**
	 * TODO
	 */
	public final static String dimensionless =
			shape.ShapeLibrary.Dimensionless.class.getName();
	
	/**
	 * TODO
	 */
	public final static String circle =
			shape.ShapeLibrary.Circle.class.getName();
	
	/**
	 * TODO
	 */
	public final static String cuboid =
			shape.ShapeLibrary.Cuboid.class.getName();
	
	/**
	 * TODO
	 */
	public final static String cylinder =
			shape.ShapeLibrary.Cylinder.class.getName();
	
	/**
	 * TODO
	 */
	public final static String line =
			shape.ShapeLibrary.Line.class.getName();
	
	/**
	 * TODO
	 */
	public final static String rectangle =
			shape.ShapeLibrary.Rectangle.class.getName();
	
	/**
	 * TODO
	 */
	public final static String sphere =
			shape.ShapeLibrary.Sphere.class.getName();
	
	
	/* ************************************************************************
	 * miscellaneous
	 */
	
	/**
	 * the svg exporter class
	 */
	public final static String svgExport =
			dataIO.SvgExport.class.getName();
	
	/**
	 * the pov ray exporter class
	 */
	public final static String povExport =
			dataIO.PovExport.class.getName();
	
	/**
	 * TODO
	 */	
	public final static String spatialGrid =
			grid.SpatialGrid.class.getName();
	
	/**
	 * TODO
	 */
	public static final String reaction =
			reaction.Reaction.class.getName();
	
	/**
	 * TODO
	 */
	public static final String pileEntry =
			instantiable.object.ListEntry.class.getName();
	
	/**
	 * TODO
	 */
	public static final String instantiableList =
			instantiable.object.InstantiableList.class.getName();
	
	/**
	 * 
	 */
	public static final String instantiableMap = 
			instantiable.object.InstantiableMap.class.getName();
	
	/* ************************************************************************
	 * java classes
	 */
	
	/**
	 * the LinkedList class
	 */
	public final static String linkedList =
			java.util.LinkedList.class.getName();
	
	/**
	 * the HashMap class
	 */
	public final static String hashMap =
			java.util.HashMap.class.getName();

	/**
	 * String
	 */
	public final static String string =
			String.class.getName();
}
