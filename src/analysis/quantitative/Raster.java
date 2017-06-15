package analysis.quantitative;

import java.util.LinkedList;
import java.util.List;

import idynomics.Compartment;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import spatialRegistry.SpatialMap;
import surface.Surface;
import surface.Voxel;
import agent.Agent;
import agent.Body;

/**
 * Raster object used for numerical analysis
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Raster {

	private SpatialMap<List<Agent>> _agentMatrix = new SpatialMap<List<Agent>>();
	
	private Compartment compartment;
	
	/**
	 * Rasterize the compartments current state
	 */
	public void rasterize(double voxelLength) 
	{
		/* domain dimension lengths */
		double[] dimLengths = compartment.getShape().getDimensionLengths();
		
		/* discrete size, amount of voxels per dimension */
		int[] size = Vector.zerosInt(dimLengths.length);
		for ( int c = 0; c < dimLengths.length; c++)
			size[c] = (int) Math.ceil( dimLengths[c] / voxelLength );
		
		/* standard voxel, used for agent voxel collision */
		double[] vox = Vector.setAll(dimLengths.clone(), voxelLength);
		
		/* All available voxel positions */
		LinkedList<int[]> coords = coordinates(size);
		
		/* Register all located agents to their respected voxels locations in
		 * the AgentMatrix (raster). */
		for ( int[] c : coords )
		{
			/* create collision voxel and find potential agents TODO we could 
			 * set this voxel instead of creating a new one. */
			Voxel v = new Voxel( convert(c, voxelLength), vox );
			List<Agent> agents = compartment.agents.treeSearch( 
					v.boundingBox(null) );
			
			/* iterate over all surfaces of all potential colliders and only
			 * store actual colliders in the agent matrix */
			LinkedList<Agent> colliders = new LinkedList<Agent>();
			for ( Agent a : agents )
				for ( Surface s : 
					( (Body) a.get( AspectRef.agentBody) ).getSurfaces() )
				{
					if ( compartment.getShape().getCollision().
							areColliding( v, s, 0.0 ) &! colliders.contains(a) )
					{
						colliders.add(a);
					}
				}
			this._agentMatrix.put( c, colliders );
		}

	}
	
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i = 0; i < stop-start; i++)
	      result[i] = start+i;

	   return result;
	}
	
	/**
	 * \brief returns a linked list with all possible coordinate positions from
	 *  zeros to the provided range.
	 *  
	 * @param size - integer array, each integer represents a dimension length.
	 * @return a LinkedList with all possible coordinate position within the
	 * given domain
	 */
	public static LinkedList<int[]> coordinates(int[] size)
	{
		return coordinates(0, 1, size, Vector.zeros(size));
	}
	
	private static double[] convert(int[] c, double scalar)
	{
		double[] out = new double[c.length];
		for (int i = 0; i < c.length; ++i)
		    out[i] = (double) c[i] * scalar;
		return out;
	}
	
	/**
	 *  \brief returns a linked list with all possible coordinate positions from
	 *  zeros to the provided range.
	 * 
	 */
	private static LinkedList<int[]> coordinates(int d, int p, int[] s, int[] c)
	{
		LinkedList<int[]> coords = new LinkedList<int[]>();
		
		/* if the received position is not the null position add it to the list.
		 * This prevents doubles in the list. */
		if (p > 0)
			coords.add(c);
		
		/* if the current dimension is within the range of number of dimensions
		 * iterate over all positions in this dimension.
		 */
		if ( d < s.length )
			for (int i = 0; i < s[d]; i++) 
			{ 
				/* clone the 0 position and add 1 until n in this dimension */
				int[] coord = c.clone();
				coord[d] = i; 
				/* call self, this will add the current position and will add 
				 * all values of the higher dimensions (if any).
				 */
				coords.addAll( coordinates(d+1, i, s, coord ) );
			}
		return coords;
	}
	
	/**
	 * For quick testing
	 * @param args
	 */
	public static void main(String[] args) {

		for (int[] c : coordinates(new int[]{ 3, 3, 3, 3 }))
		{
			System.out.println(Vector.toString(c));
		}
	}
}
