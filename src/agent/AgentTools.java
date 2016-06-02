/**
 * 
 */
package agent;

import static dataIO.Log.Tier.BULK;
import static dataIO.Log.Tier.DEBUG;

import java.util.List;
import java.util.function.Predicate;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.SubvoxelPoint;
import surface.Collision;
import surface.Surface;

/**
 * \brief Collection of useful methods for interacting with {@code Agent}s.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public final class AgentTools
{
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	protected final static Predicate<Agent> NO_REAC_FILTER = 
			(a -> ! a.isAspect(NameRef.agentReactions));
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have relevant components of a body.
	 */
	protected final static Predicate<Agent> NO_BODY_FILTER = 
			(a -> (! a.isAspect(NameRef.surfaceList)) ||
					( ! a.isAspect(NameRef.bodyRadius)));
	/**
	 * When choosing an appropriate sub-voxel resolution for building agents'
	 * {@code coordinateMap}s, the smallest agent radius is multiplied by this
	 * factor to ensure it is fine enough.
	 */
	// NOTE the value of a quarter is chosen arbitrarily
	private static double SUBGRID_FACTOR = 0.25;
	/**
	 * Aspect name for the {@code coordinateMap} used for establishing which
	 * voxels a located {@code Agent} covers.
	 */
	private static final String VD_TAG = NameRef.agentVolumeDistributionMap;
	
	
	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @param agents The agents of a {@code Compartment}.
	 */
	// TODO Consider moving this to AgentContainer instead?
	@SuppressWarnings("unchecked")
	public static void setupAgentDistributionMaps(AgentContainer agents)
	{
		Log.out(DEBUG, "Setting up agent distribution maps");
		Tier level = BULK;
		/*
		 * Reset the agent biomass distribution maps.
		 */
		CoordinateMap distributionMap;
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			distributionMap = new CoordinateMap();
			a.set(VD_TAG, distributionMap);
		}
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		Shape shape = agents.getShape();
		int nDim = agents.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		double[] sides;
		List<SubvoxelPoint> svPoints;
		List<Agent> nhbs;
		List<Surface> surfaces;
		double[] pLoc;
		Collision collision = new Collision(null, shape);
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			/* Find all agents that overlap with this voxel. */
			// TODO a method for getting a voxel's bounding box directly?
			location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
			shape.getVoxelSideLengthsTo(dimension, coord);
			sides = Vector.subset(dimension, nDim);
			/* NOTE the agent tree is always the amount of actual dimension */
			nhbs = agents.treeSearch(location, sides);
			/* Filter the agents for those with reactions, radius & surface. */
			nhbs.removeIf(NO_REAC_FILTER);
			nhbs.removeIf(NO_BODY_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( nhbs.isEmpty() )
				continue;
			Log.out(level, "  "+nhbs.size()+" agents overlap with coord "+
					Vector.toString(coord));
			/* 
			 * Find the sub-voxel resolution from the smallest agent, and
			 * get the list of sub-voxel points.
			 */
			double minRad = Vector.min(sides);
			double radius;
			for ( Agent a : nhbs )
			{
				radius = a.getDouble(NameRef.bodyRadius);
				Log.out(level, "   agent "+a.identity()+" has radius "+radius);
				minRad = Math.min(radius, minRad);
			}
			minRad *= SUBGRID_FACTOR;
			Log.out(level, "  using a min radius of "+minRad);
			svPoints = shape.getCurrentSubvoxelPoints(minRad);
			Log.out(level, "  gives "+svPoints.size()+" sub-voxel points");
			/* Get the sub-voxel points and query the agents. */
			for ( Agent a : nhbs )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(NameRef.agentReactions) )
					continue;
				if ( ! a.isAspect(NameRef.surfaceList) )
					continue;
				surfaces = (List<Surface>) a.get(NameRef.surfaceList);
				Log.out(level, "  "+"   agent "+a.identity()+" has "+
						surfaces.size()+" surfaces");
				distributionMap = (CoordinateMap) a.getValue(VD_TAG);
				sgLoop: for ( SubvoxelPoint p : svPoints )
				{
					/* Only give location in significant dimensions. */
					pLoc = p.getRealLocation(nDim);
					for ( Surface s : surfaces )
						if ( collision.distance(s, pLoc) < 0.0 )
						{
							distributionMap.increase(coord, p.volume);
							/*
							 * We only want to count this point once, even
							 * if other surfaces of the same agent hit it.
							 */
							continue sgLoop;
						}
				}
			}
		}
		Log.out(DEBUG, "Finished setting up agent distribution maps");
	}
}
