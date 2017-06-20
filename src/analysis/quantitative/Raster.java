package analysis.quantitative;

import java.util.LinkedList;
import java.util.List;

import idynomics.Compartment;
import instantiable.Instance;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import shape.CartesianShape;
import shape.Shape;
import spatialRegistry.SpatialMap;
import surface.Surface;
import surface.Voxel;
import utility.Helper;
import agent.Agent;
import agent.Body;
import dataIO.GraphicalExporter;
import dataIO.Log;
import dataIO.Log.Tier;

/**
 * Raster object used for numerical analysis
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Raster {

	private SpatialMap<List<Agent>> _agentMatrix = new SpatialMap<List<Agent>>();
	private Compartment _compartment;
	private Shape _shape;
	private GraphicalExporter _graphics;
	
	public Raster(Compartment compartment)
	{
		this._compartment = compartment;
	}
	
	/**
	 * Rasterize the compartments current state
	 */
	public void rasterize(double voxelLength) 
	{
		this._shape = _compartment.getShape();
		/* domain dimension lengths */
		double[] dimLengths = _shape.getDimensionLengths();
		
		/* discrete size, amount of voxels per dimension */
		int[] size = Vector.zerosInt( dimLengths.length );
		for ( int c = 0; c < dimLengths.length; c++)
			size[c] = (int) Math.ceil( dimLengths[c] / voxelLength );
		
		/* standard voxel, used for agent voxel collision */
		double[] vox = Vector.setAll( dimLengths, voxelLength);
		
		/* All available voxel positions */
		LinkedList<int[]> coords = coordinates(size);
		
		/* Register all located agents to their respected voxels locations in
		 * the AgentMatrix (raster). */
		for ( int[] c : coords )
		{
			/* create collision voxel and find potential agents TODO we could 
			 * set this voxel instead of creating a new one. */
			Voxel v = new Voxel( convert(c, voxelLength), vox );
			List<Agent> agents = _compartment.agents.treeSearch( 
					v.boundingBox(null) );
			
			/* iterate over all surfaces of all potential colliders and only
			 * store actual colliders in the agent matrix */
			LinkedList<Agent> colliders = new LinkedList<Agent>();
			for ( Agent a : agents )
				for ( Surface s : 
					( (Body) a.get( AspectRef.agentBody) ).getSurfaces() )
				{
					if ( _compartment.getShape().getCollision().
							areColliding( s, v, 0.0 ) &! colliders.contains(a) )
					{
						colliders.add(a);
					}
				}
			this._agentMatrix.put( c, colliders );
		}
		int max = 14;
		int[][][] passOne = this.distanceMap(size, this.agentMap());
		SpatialMap<Integer> distance = new SpatialMap<Integer>();
		for( int i = 0; i < size[0]; i++)
		{
			for ( int j = 0; j < size[1]; j++ )
			{
				System.out.print(Vector.toString(passOne[i][j]) + "\t");
				if (size(passOne[i][j]) > max )
					distance.put(new int[] { i,  j}, max ); 
				else
					distance.put(new int[] { i,  j}, 
							( (Double) size(passOne[i][j]) ).intValue() ); 
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		this.plot(distance, 1, "distance", Helper.giveMeAGradient(max+1));
		
	}
	
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i = 0; i < stop-start; i++)
	      result[i] = start+i;

	   return result;
	}
	
	public void plot(SpatialMap<Integer> raster, double scale, String fileName)
	{
		plot(raster, scale, fileName, Helper.DIFFERENTIATING_PALETE);
	}
	
	public void plot(SpatialMap<Integer> raster, double scale, String fileName, String[] colors)
	{
		/* get instance of appropriate output writer */
		if ( Helper.isNone(_graphics))
			this._graphics = (GraphicalExporter) Instance.getNew(
					null, null, ClassRef.svgExport );
		
		/* Initiate new file. */
		if (fileName == null)
			fileName = Helper.obtainInput("", "enter filename.");
		_graphics.createCustomFile(fileName);
		
		/* Verify the shape type */
		if ( ! ( _shape instanceof CartesianShape) )
		{
			Log.out(Tier.NORMAL, "attempt to " + this.getClass().getSimpleName()
					+ ".plot unsuported shape, skipping.");
			return;
		}
		
		for ( int[] c : raster.keySetNumeric() )
			_graphics.rectangle( convert(c, scale), 
					new double[] { scale, scale }, 
					colors[ raster.get(c) ] );

		/* Close the file */
		_graphics.closeFile();
	}
	
	public SpatialMap<Integer> agentMap() 
	{
		SpatialMap<Integer> out = new SpatialMap<Integer>();
		for ( int[] c : this._agentMatrix.keySetNumeric() )
			if ( this._agentMatrix.get( c ).isEmpty() )
				out.put(c, 0);
			else
				out.put(c, 1);
		return out;
	}
	
	public int[][][] distanceMap(int[] dimension, 
			SpatialMap<Integer> agentMap)
	{
		return distanceMap(dimension, agentMap, this._shape.getIsCyclicNaturalOrder());
	}
	
	/* note 2d only 
	 * P.E. Danielsson, Euclidean distance mapping, computer graphics and image processing 14, 227-248 (1980)
	 * */
	public int[][][] distanceMap(int[] dimension, 
			SpatialMap<Integer> agentMap, boolean[] periodic)
	{
		int[] p, q, step, max_value, zeros;
		int d, e;
		max_value = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
		zeros = new int[] { 0, 0 };
		
		d = ( periodic[0] ? dimension[0] : dimension[0] + 1);
		e = ( periodic[1] ? dimension[1] : dimension[1] + 1);
			
		int[][][] out = new int[d][e][2];
		Array.setAll(out, Integer.MAX_VALUE);
		
		for ( int[] key : agentMap.keySetNumeric())
		{
			if (agentMap.get(key) == 1 )
				out[ ( periodic[0] ? key[0] : key[0] + 1 ) ]
						[ ( periodic[1] ? key[1] : key[1] + 1 ) ] = max_value;
			else
				out[ ( periodic[0] ? key[0] : key[0] + 1 ) ]
						[ ( periodic[1] ? key[1] : key[1] + 1 ) ] = zeros;
		}
		
		/* iterations */
		for ( int j = 0; j < e; j++)
		{
			step = new int[] { 0, 1 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				
				/* handle boundaries */	
				if ( j == 0 )
					if ( periodic[1] )
						q = out[i][e-1];
					else
						q = max_value;
				else
					q = out[i][j-1];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
			step = new int[] { 1, 0 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];

				/* handle boundaries */	
				if ( i == 0 )
					if ( periodic[0] )
						q = out[d-1][j];
					else
						q = max_value;
				else
					q = out[i-1][j];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
			for ( int i = d-1; i > 0; i--)
			{
				p = out[i][j];
				
				/* handle boundaries */	
				if ( i == d-1 )
					if ( periodic[0] )
						q = out[0][j];
					else
						q = max_value;
				else
					q = out[i+1][j];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
		}
		for ( int j = e-1; j > 0; j--)
		{
			step = new int[] { 0, 1 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				
				/* handle boundaries */	
				if ( j == e-1 )
					if ( periodic[1] )
						q = out[i][0];
					else
						q = max_value;
				else
					q = out[i][j+1];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
			step = new int[] { 1, 0 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				
				/* handle boundaries */	
				if ( i == 0 )
					if ( periodic[0] )
						q = out[d-1][j];
					else
						q = max_value;
				else
					q = out[i-1][j];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
			for ( int i = d-1; i > 0; i--)
			{
				p = out[i][j];
				
				/* handle boundaries */	
				if ( i == d-1 )
					if ( periodic[0] )
						q = out[0][j];
					else
						q = max_value;
				else
					q = out[i+1][j];
				
				if( size( Vector.add( q, step ) ) < size( p ) )
					out[i][j] = Vector.add( q, step );
			}
		}
		
		/* remove margin for non-periodic dimensions and return if none just 
		 * return out */
		for ( boolean b : periodic )
			if ( !b )
				return Array.subarray(out, ( periodic[0] ? 0 : 1 ), d-1, 
						( periodic[1] ? 0 : 1 ), e-1, 0, 1);
		return out;
	}

	private double size(int[] vector)
	{
		double out = 0.0;
		for (int i : vector)
			out += (double) i * (double) i;
		return Math.sqrt(out);
	}
	/*
	 * source: http://cs.brown.edu/~pff/dt/
	 */
	public double[]  distanceTransform(double[] f, int n)
	{
		double[] d = new double[n];
		int[] v = new int[n];
		double[] z = new double[n+1];
		int k = 0;
		
		v[0] = 0;
		z[0] = Double.MIN_VALUE;
		z[1] = Double.MAX_VALUE;
		
		for (int q = 1; q <= n-1; q++)
		{
			double s = ( ( f[q] + Math.pow( q, 2 ) ) -
					( f[ v[k] ] + Math.pow( v[k], 2 ) ) ) / 
					( 2 * q - 2 * v[k] );
			
		    while ( s <= z[k] ) {
		        k--;
		        s  = ( ( f[q] + Math.pow( q, 2 ) ) -
		        		( f[ v[k] ] + Math.pow( v[k], 2 ) ) ) /
		        		( 2 * q - 2 * v[k] );
		      }
		      k++;
		      v[k] = q;
		      z[k] = s;
		      z[k+1] = Double.MAX_VALUE;
		    }

		    k = 0;
		    for (int q = 0; q <= n-1; q++) {
		      while ( z[k+1] < q )
		        k++;
		      d[q] = Math.pow( v[k], 2 ) + f[ v[k] ];
		    }
		    return d;
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

		for (int[] c : coordinates(new int[]{ 60, 60,}))
		{
			System.out.println(Vector.toString(c));
		}
	}
}
