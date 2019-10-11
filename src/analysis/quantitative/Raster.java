package analysis.quantitative;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import analysis.Counter;
import analysis.FilterLogic;
import analysis.filter.Filter;
import analysis.toolset.LinearRegression;
import aspect.AspectInterface;
import compartment.AgentContainer;
import compartment.Compartment;
import dataIO.CsvExport;
import dataIO.GraphicalExporter;
import dataIO.Log;
import dataIO.Log.Tier;
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

/**
 * \Brief Raster is used to rasterize and quantify spatial properties of
 * biofilms.
 * 
 * NOTE: This class in an active state of development, please do not make
 * changes but give feedback as in-line comments instead.
 * 
 * TODO: extend to 3D biofilms
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Raster {

	private SpatialMap<List<Agent>> _agentRaster = 
			new SpatialMap<List<Agent>>();
	/**
	 * The associated agentContainer
	 */
	private AgentContainer _agentContainer;
	/**
	 * The compartment shape
	 */
	private Shape _shape;
	/**
	 * The svg exporter
	 */
	private GraphicalExporter _graphics;
	private boolean[] _periodic;
	/**
	 * Include additional debug and visual analysis output
	 */
	private boolean _verbose = false;
	
	/* raster sizes including margins (rX and rY) and actual size */
	private int rX, rY;
	private int[] size;
	
	/* default distance map positions */
	protected final int[] max_value = 
			new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
	
	protected final int[] zeros = 
			new int[] { 0, 0 };
	
	/* analysis */
	protected SpatialMap<Integer> _agentDistanceMap;
	protected SpatialMap<Integer> _edgeDistanceMap;
	private SpatialMap<Double> _edgeDistanceMapDbl;
	private double _voxelLength;
	
	/* header for default analysis output */
	protected final static String[] header = new String[]{
			"fractal dimension",
			"fractal std Err",
			"max diffusion distance",
			"average diffusion distance (biomass)",
			"fraction encapsulated void space"
	};

	private enum Region
	{
		GAP, EDGE, BOUNDARY, CUSTOM
	}
	
	/* ************************************************************************
	 * CONSTRUCTORS
	 * ***********************************************************************/
	
	/**
	 * default constructor
	 * @param compartment associated with this raster
	 */
	public Raster(AgentContainer agentContainer)
	{
		this(agentContainer, false);
	}
	
	/**
	 * default constructor
	 * @param compartment associated with this raster
	 */
	public Raster(AgentContainer agentContainer, boolean verbose)
	{
		this._agentContainer = agentContainer;
		this._shape = agentContainer.getShape();
		this._periodic = this._shape.getIsCyclicNaturalOrder();
		this._verbose = verbose;
	}
	
	/**
	 * verbose setting constructor
	 * @param compartment associated with this raster
	 * @param verbose boolean indicating additional output
	 */
	public Raster(Compartment compartment, boolean verbose)
	{
		this._shape = compartment.getShape();
		this._agentContainer = compartment.agents;
		this._periodic = this._shape.getIsCyclicNaturalOrder();
		this._verbose = verbose;
	}
	
	/**
	 * Rasterize the compartments current state
	 */
	public void rasterize( double voxelLength ) 
	{
		/* domain dimension lengths */
		double[] dimLengths = _shape.getDimensionLengths();
		
		this._voxelLength = voxelLength;
		
		/* discrete size, amount of voxels per dimension */
		this.size = Vector.zerosInt( dimLengths.length );
		for ( int c = 0; c < dimLengths.length; c++)
			this.size[c] = (int) Math.ceil( dimLengths[c] / voxelLength );
		
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
			Voxel v = new Voxel( toContinuous(c, voxelLength), Vector.add(toContinuous(c, voxelLength), vox) );
			List<Agent> agents = _agentContainer.treeSearch( 
					v.boundingBox(null) );
			
			/* iterate over all surfaces of all potential colliders and only
			 * store actual colliders in the agent matrix */
			LinkedList<Agent> colliders = new LinkedList<Agent>();
			for ( Agent a : agents )
				for ( Surface s : 
					( (Body) a.get( AspectRef.agentBody ) ).getSurfaces() )
				{
					if ( _shape.getCollision().
							intersect( s, v, 0.0 ) &! colliders.contains(a) )
					{
						colliders.add(a);
						break;
					}
				}
			this._agentRaster.put( c, colliders );
		}

		/* matrix dimension length (rX, rY) is +1 for non periodic */
		rX = ( _periodic[0] ? size[0] : size[0] + 1 );
		rY = ( _periodic[1] ? size[1] : size[1] + 1 );
			
		int[][][] agents = new int[rX][rY][2];
		Array.setAll(agents, Integer.MAX_VALUE);
		
		/* fill matrix, add spacers for non periodic. */
		agents = this.presenceMapToArray( agents, this.agentMap(), true, false);
		int[][][] edge = Array.copy( agents );
		
		agents = this.distanceMatrix( agents );
		long millis = System.currentTimeMillis();
		if ( this._verbose )
			this.plotArray(agents, "ag" + millis );
		edge = this.edgeMatrix( edge );
		if ( this._verbose )
			this.plotArray(edge, "alpha" + millis );
		edge = this.distanceMatrix( edge );
		if ( this._verbose )
			this.plotArray(edge, "beta" + millis );
		
		/* plot biofilm euclidean distance */
		this._agentDistanceMap = euclideanMap( agents );

		/* plot edge euclidean distance */
		this._edgeDistanceMap = euclideanMap( edge );
		
		this._edgeDistanceMapDbl = euclideanMapDbl( edge );
		
		/* NOTE for testing: looking for co-ocurance 
		traitLocalization("species=CanonicalAOB", "species=CanonicalNOB" ); */
	}
	

	public double[] voxelCenter(int[] vox) {
		double[] out = new double[vox.length];
		for( int i = 0; i < out.length; i++)
			out[i] = this._voxelLength * vox[i] + (0.5 * this._voxelLength);
		return out;
	}
	
	/**
	 * \brief euclidean distanceMap of biofilm region including boundary layer
	 * of int bound number of voxels. (Ignores encapsulated void spaces).
	 * 
	 * @param bound
	 * @return
	 */
	public SpatialMap<Double> regionMap( int bound )
	{
		int[][][] agents = new int[rX][rY][2];
		Array.setAll(agents, Integer.MAX_VALUE);
		
		/* fill matrix, add spacers for non periodic. */
		agents = this.presenceMapToArray( agents, this.agentMap(), true, false);
		int[][][] region = Array.copy( agents );

		LinkedList<int[]> custom = 
				regionDetect( region, Region.CUSTOM, bound*2, bound);
		
		for ( int[] pos : custom )
			region[pos[0]][pos[1]] = max_value;
		
		if(this._verbose)
		{
			long millis = System.currentTimeMillis();
			SpatialMap<Integer> dMap = euclideanMap( this.distanceMatrix( region ) );
			//FIXME colors
			this.plot( dMap, 1.0, "dMap" + millis,  Helper.DIFFERENTIATING_PALETTE ); //Helper.giveMeAGradient( 40 ) );
		}
				
		return euclideanMapDbl( this.distanceMatrix( region ) );
	}
	
	public String toString()
	{
		double[] out = new double[] { };
		
		out = Vector.appendAll( out, this.fractalDimension() );
		out = Vector.append( out, this.max( this.agentDistanceMap() ) );
		out = Vector.append( out, this.averageDiffusionDistance() );
		out = Vector.append( out, this.voidSpace() );
		
		return Vector.toString( out );
	}
	
	public static String getHeader()
	{

		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < header.length; i++)
		{
			builder.append( header[i] );
			builder.append( Vector.DELIMITER );
		}
		return builder.toString();
	}
	
	public int[] distanceVector( String filterA, String filterB )
	{
		SpatialMap<Integer> aMap = this.occuranceMap( filterA, 
				 this.agentMap() );
		
		int[][][] matrix = new int[rX][rY][2];
		Array.setAll(matrix, Integer.MAX_VALUE);
		matrix = presenceMapToArray(matrix, aMap, false, true);
		
		matrix = this.distanceMatrix( matrix );

		long millis = System.currentTimeMillis();
		if ( this._verbose )
			this.plotArray(matrix, "a1" + millis);
		
		SpatialMap<Integer> aDist = gradientMap(matrix);
		
		if ( this.max( aDist ) == Integer.MAX_VALUE )
			return null;
		
		SpatialMap<Integer> bMap = this.occuranceMap( filterB, 
				 this.agentMap() );
		
		int[] bToADist = Vector.zerosInt( this.max( aDist )+1  );
		for ( String key : bMap.keySet() )
			if ( bMap.get( key ) == 1 )
				bToADist[ aDist.get( key ) ]++;
		return bToADist;
	}
		
	public void traitLocalization( String filterA, String filterB )
	{

		long millis = System.currentTimeMillis();
		if ( this._verbose )
			plotPropertyAnalysis( filterA, "traitA" + millis, 
					this.agentMap() );
		
		SpatialMap<Integer> aMap = this.occuranceMap( filterA, 
				 this.agentMap() );
		
		int[][][] matrix = new int[rX][rY][2];
		Array.setAll(matrix, Integer.MAX_VALUE);
		matrix = presenceMapToArray(matrix, aMap, false, true);
		
		matrix = this.distanceMatrix( matrix );
		
		if ( this._verbose )
			this.plotArray(matrix, "a1" + millis);
		
		SpatialMap<Integer> aDist = gradientMap(matrix);
		
		if ( this.max( aDist ) == Integer.MAX_VALUE )
			return;
		
		SpatialMap<Integer> bMap = this.occuranceMap( filterB, 
				 this.agentMap() );
		
		int[] bToADist = Vector.zerosInt( this.max( aDist )+1  );
		for ( String key : bMap.keySet() )
			if ( bMap.get( key ) == 1 )
				bToADist[ aDist.get( key ) ]++;
		Log.out( Tier.EXPRESSIVE, "Distance of b from a" );
		Log.out( Tier.EXPRESSIVE, "Co-ocurence voxels: " + bToADist[0] );
		Log.out( Tier.EXPRESSIVE, "Average distance: " + averageDist( bToADist )
				+ " voxels" );		
		
		matrix = new int[rX][rY][2];
		Array.setAll(matrix, Integer.MAX_VALUE);
		matrix = presenceMapToArray(matrix, bMap, false, true);
		
		matrix = this.distanceMatrix( matrix );
		
		if ( this._verbose )
			this.plotArray(matrix, "n1" + millis);
		
		SpatialMap<Integer> bDist = gradientMap(matrix);

		
		int[] aToBDist = Vector.zerosInt( this.max( bDist )+1 );
		for ( String key : aMap.keySet() )
			if ( aMap.get( key ) == 1 )
				aToBDist[ bDist.get( key ) ]++;

		Log.out( Tier.EXPRESSIVE, "Distance of a from b" );
		Log.out( Tier.EXPRESSIVE, "Co-ocurence voxels: " + aToBDist[0] );
		Log.out( Tier.EXPRESSIVE, "Average distance: " + averageDist( aToBDist )
				+ " voxels" );		
		
		Log.out( Tier.EXPRESSIVE, "strict co-localization " + Vector.toString( 
				colocalization( aToBDist, bToADist, 1 ) ) );
		Log.out( Tier.EXPRESSIVE, "proximity co-localization ( < 6µm ) " + 
				Vector.toString( colocalization( aToBDist, bToADist, 12 ) ) );
		Log.out( Tier.EXPRESSIVE, Vector.toString( aToBDist ) );
		Log.out( Tier.EXPRESSIVE, Vector.toString( bToADist ) );
	}
	
	/**
	 * Returns the Manders coefficients of co-localization:
	 * 
	 * MANDERS, E. M. M., VERBEEK, F. J. & ATEN, J. A. Measurement of 
	 * co-localization of objects in dual-colour confocal images. J. Microsc. 
	 * 169, 375–382 (1993).
	 */
	public double[] colocalization( int[] distAtoB, int[] distBtoA, 
			int threshold)
	{
		int c = 0, d = 0;
		for ( int i = 0; i < Math.min(threshold, distAtoB.length); i++)
			c += distAtoB[i];
		for ( int i = 0; i < Math.min(threshold, distBtoA.length); i++)
			d += distBtoA[i];

		return new double[] { (double) c / (double) Vector.sum(distAtoB), 
				(double) d / (double) Vector.sum(distBtoA) };
	}

	public Integer max(SpatialMap<Integer> map)
	{
		Integer out = 0;
		for ( Integer n : map.values())
			if ( n > out )
				out = n;
		return out;
	}
	
	public double averageDist( int[] voxelDistCount )
	{
		double p = 0, q = 0;
		for ( int i = 0; i < voxelDistCount.length; i++ )
		{
			p += i * voxelDistCount[i];
			q += voxelDistCount[i];
		}
		return p/q;
	}
	
	public int[] voxelsPerDistance( SpatialMap<Integer> distanceMap )
	{
		if ( this.max( distanceMap ) == Integer.MAX_VALUE )
		{
			this.plot( distanceMap, 1, "catch", Helper.giveMeAGradient(255) );
			
			/* matrix dimension length (rX, rY) is +1 for non periodic */
			rX = ( _periodic[0] ? size[0] : size[0] + 1 );
			rY = ( _periodic[1] ? size[1] : size[1] + 1 );
				
			int[][][] agents = new int[rX][rY][2];
			Array.setAll(agents, Integer.MAX_VALUE);
			
			/* fill matrix, add spacers for non periodic. */
			agents = this.presenceMapToArray( agents, this.agentMap(), true, false);		
			agents = this.distanceMatrix( agents );
			CsvExport dump = new CsvExport();
			dump.createCustomFile("_debug");
			dump.writeLine( textArray( agents ) );
			dump.closeFile();
			
			return new int[] {0, 1};
		}
		int[] out = Vector.zerosInt( this.max( distanceMap ) + 1 );
		for ( String key : _agentDistanceMap.keySet() )
		{
			int p = _agentDistanceMap.get( key );
			if ( p != 0 )
				out[ p ]++;
		}
		
		return out;
	}
	
	public double voidSpace()
	{
		return voidSpace( this.agentDistanceMap(), this.agentMap() );
	}
	
	/**
	 * 
	 * @param space
	 * @param raw
	 * @return fraction encapsulated void space (if this fraction is high the
	 * domain should be considered foamy and thus not biofilm).
	 */
	public double voidSpace( SpatialMap<Integer> space, SpatialMap<Integer> raw)
	{
		int a = 0, b = 0;
		for ( String l : space.keySet() )
		{
			if ( space.get( l ) != 0 )
			{
				a++;
				if ( raw.get( l ) == 1 )
					b++;
			}
		}
		return ( 1.0 - ((double) b / (double) a));
	}
	
	public double averageDiffusionDistance()
	{
		int[] dist = voxelsPerDistance( this._agentDistanceMap );
		int o = 0, c = 0;
		for ( int i = 1; i < dist.length; i++) /* we skip 0 intentionally */
		{
			o += dist[i] * i;
			c += dist[i];
		}
		return (double)o / (double)c;
	}
	
	public double averageDiffusionDistance( String filter )
	{

		SpatialMap<Integer> bMap = this.occuranceMap( filter, 
				 this.agentMap() );
		
		int[] dist = Vector.zerosInt( this.max( this._agentDistanceMap )+1  );
		for ( String key : bMap.keySet() )
			if ( bMap.get( key ) == 1 )
				dist[ this._agentDistanceMap.get( key ) ]++;

		int o = 0, c = 0;
		for ( int i = 1; i < dist.length; i++) /* we skip 0 intentionally */
		{
			o += dist[i] * i;
			c += dist[i];
		}
		return (double)o / (double)c;
	}
	
	/**
	 * calculate fractal dimension from edge distance map
	 * @return
	 */
	public double[] fractalDimension()
	{
		if ( _edgeDistanceMap == null )
		{
			Log.out(Tier.NORMAL, "edge map not set, raseterization required" );
			return null;
		}
		/* fractal dimension and the std error. */
		return this.fractalDimension( _edgeDistanceMapDbl );
	}
	
	public double[] fractalDimension( SpatialMap<Double> edgeDistance )
	{
		return fractalDimension( edgeDistance, 
				new double[] { 1.0, 1.2, 1.4, 1.6, 1.8, 2.0 } );
	}
	
	/**
	 * calculate fractal dimension from edge distance map
	 * @param edgeDistance
	 * @return
	 */
	public double[] fractalDimension( SpatialMap<Double> edgeDistance, double[] steps )
	{
		double[] diam = new double[ steps.length ];
		double[] count = new double[ steps.length ];
		for ( int i = 0; i < steps.length; i++ )
		{
			count[i] = 0;
			for ( Double a : edgeDistance.values() )
				if ( a < Math.exp( steps[i] ) )
				{
					count[i]++;
					diam[i] = Math.exp( steps[i] );
				}
		}
		
		double[] lnPerimeter = new double[count.length];
		for ( int i = 0; i < count.length; i++ )
		{
			lnPerimeter[i] = Math.log( count[i] / Math.exp( steps[i] ) );
		}
		
		Log.out(Tier.DEBUG, Vector.toString( steps ) );
		Log.out(Tier.DEBUG, Vector.toString( diam ) );
		Log.out(Tier.DEBUG, Vector.toString( lnPerimeter ) );
		Log.out(Tier.DEBUG, Vector.toString( count ) );
		
		LinearRegression fractalReg = new LinearRegression(steps, lnPerimeter);
		Log.out(Tier.DEBUG, fractalReg.toString() );
		Log.out(Tier.EXPRESSIVE, String.format( "fractal dimension %.3f, stdErr"
				+ " %.3f", 1 - fractalReg.slope(), fractalReg.slopeStdErr() ) );
		return new double[] { 1.0 - fractalReg.slope(), fractalReg.slopeStdErr() };
	}
	
	/**
	 * generate int[] with all integers within range start - stop
	 * @param start
	 * @param stop
	 * @return
	 */
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i = 0; i < stop-start; i++)
	      result[i] = start+i;

	   return result;
	}
	
	/**
	 * plot to svg file with default (differentiating) palette 
	 * @param raster
	 * @param scale
	 * @param fileName
	 */
	public void plot( SpatialMap<Integer> raster, double scale, 
			String fileName )
	{
		plot( raster, scale, fileName, Helper.DIFFERENTIATING_PALETTE );
	}
	
	/**
	 * plot to svg file with supplied color palette ( String[] )
	 * @param raster
	 * @param scale
	 * @param fileName
	 */
	public void plot( SpatialMap<Integer> raster, double scale, 
			String fileName, String[] colors )
	{
		/* get instance of appropriate output writer */
		if ( Helper.isNullOrEmpty(_graphics) );
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
			_graphics.rectangle( toContinuous(c, scale), 
					new double[] { scale, scale }, 
					colors[ Math.min( raster.get(c), colors.length-1 ) ] );

		/* Close the file */
		_graphics.closeFile();
	}
	
	/**
	 * Obtain SpatialMap<Integer> from SpatialMap<List<Agent>> with 1: agent(s)
	 * and 0: no agents for fast subsequent numeric algorithms.
	 * 
	 * @return
	 */
	public SpatialMap<Integer> agentMap() 
	{
		SpatialMap<Integer> out = new SpatialMap<Integer>();
		for ( int[] c : this._agentRaster.keySetNumeric() )
			if ( this._agentRaster.get( c ).isEmpty() )
				out.put(c, 0);
			else
				out.put(c, 1);
		return out;
	}
	
	public SpatialMap<Integer> agentDistanceMap() 
	{
		return this._agentDistanceMap;
	}
	
	public SpatialMap<Integer> edgeDistanceMap() 
	{
		return this._edgeDistanceMap;
	}
	
	/**
	 * convert SpatialMap to int[][][] and if plugHoles = true, close 
	 * encapsulated voids.
	 * 
	 * @param array
	 * @param agentMap
	 * @param plugHoles
	 * @return
	 */
	private int[][][] presenceMapToArray( int[][][] array, 
			SpatialMap<Integer> agentMap, boolean plugHoles, boolean inverse )
	{
		/* fill matrix, add spacers for non periodic. */
		for ( int[] key : agentMap.keySetNumeric())
		{
			if ( ( inverse && agentMap.get(key) == 0 ) || 
					( !inverse && agentMap.get(key) >= 1 ) )
				array[ ( _periodic[0] ? key[0] : key[0] + 1 ) ]
						[ ( _periodic[1] ? key[1] : key[1] + 1 ) ] = max_value;
			else
				array[ ( _periodic[0] ? key[0] : key[0] + 1 ) ]
						[ ( _periodic[1] ? key[1] : key[1] + 1 ) ] = zeros;
		}
		
		int max = Array.max(array);
		SpatialMap<Integer> distance = new SpatialMap<Integer>();
		for( int i = 0; i < array.length; i++)
		{
			for ( int j = 0; j < array[0].length; j++ )
			{
				if (euclidean( array[i][j] ) > max )
					distance.put(new int[] { i, j }, max ); 
				else
					distance.put(new int[] { i, j }, 
							( (Double) euclidean( array[i][j] ) ).intValue() ); 
			}
		}
		

		long millis = System.currentTimeMillis();
		if ( this._verbose )
			plot( distance, 1, "raster" + millis, Helper.giveMeAGradient( max+1 ) );
		
		/* fill encapsulated void spaces */
		if ( plugHoles )
		{
			LinkedList<int[]> gaps = regionDetect(array, Region.GAP);
			for ( int[] pos : gaps )
				array[pos[0]][pos[1]] = max_value;
		}
		return array;
	}
	
	/**
	 * Create SpatialMap based on agent property occurrence per region, counts
	 * amount of agents that pass the supplied filter per voxel.
	 * 
	 * @param agentMap
	 * @param filter {@link String}
	 * @return
	 */
    public SpatialMap<double[]> propertyLocalisation( String filter, 
    		SpatialMap<Integer> intMap ) 
    {
    	Filter myFilter = FilterLogic.filterFromString( filter );
    	SpatialMap<double[]> propMap = propertyLocalisation( myFilter, intMap );
    	return propMap;
    }
	
	/**
	 * Create SpatialMap based on agent property occurrence per region, counts
	 * amount of agents that pass the supplied filter per voxel.
	 * 
	 * @param agentMap
	 * @param filter {@link analysis.filter.Filter}
	 * @return
	 */
	public SpatialMap<double[]> propertyLocalisation( Filter filter,
			 SpatialMap<Integer> agentMap )
	{
		SpatialMap<double[]> propertyMap = new SpatialMap<double[]>();
		for ( int[] spot : agentMap.keySetNumeric() )
		{
			List<AspectInterface> agents = new LinkedList<AspectInterface>( 
					this._agentRaster.get( spot ) );
			propertyMap.put( spot, Counter.count(filter, agents) );
		}
		return propertyMap;
	}
	
	/**
	 * From biomass in[][][] build a int[][][] where only edge voxels are set
	 * to 0,0 all other voxels are set to MAX_VALUE.
	 * 
	 * @param array
	 * @return
	 */
	private int[][][] edgeMatrix( int[][][] array )
	{
		int[][][] out = Array.array( array.length, array[0].length, 
				array[0][0].length, Integer.MAX_VALUE );
		LinkedList<int[]> edge = regionDetect( array, Region.EDGE );
		for ( int[] pos : edge )
			out[pos[0]][pos[1]] = zeros;
		return out;
	}
	
	/**
	 * Detect encapsulated voids or open surfaces. returns voxel coordinates
	 * that are assigned to the specified region: GAP for encapsulated voids up
	 * to 6 voxels wide (maximum purging depth of 20), EDGE for surface regions
	 * (non-biomass voxels that are neighboring biomass voxels) including 
	 * diagonal neighbors, BOUNDARY for surface regions excluding diagonal 
	 * neighbors.
	 * 
	 * @param array
	 * @param region
	 * @return
	 */
	private LinkedList<int[]> regionDetect( int[][][] array, Region region )
	{
		return this.regionDetect(array, region, 0, 0);
	}
	
	/**
	 * Detect encapsulated voids or open surfaces. returns voxel coordinates
	 * that are assigned to the specified region: GAP for encapsulated voids up
	 * to 6 voxels wide (maximum purging depth of 20), EDGE for surface regions
	 * (non-biomass voxels that are neighboring biomass voxels) including 
	 * diagonal neighbors, BOUNDARY for surface regions excluding diagonal 
	 * neighbors.
	 * 
	 * @param array
	 * @param region
	 * @return
	 */
	private LinkedList<int[]> regionDetect( int[][][] array, Region region, 
			int passLim, int purgeLim )
	{
		int passes = 0, purgeLimit = 0;
		int d = array.length, e = array[0].length;

		int[][][] temp = Array.copy( array );
		LinkedList<int[]> pass = new LinkedList<int[]>();
		
		switch ( region ) 
		{
		case GAP :
			passes = 3;
			purgeLimit = 20;
			break;
		case EDGE :
			passes = 1;
			purgeLimit = 0;
			break;
		case BOUNDARY :
			passes = 1;
			purgeLimit = 0;
			break;
		case CUSTOM :
			passes = passLim;
			purgeLimit = purgeLim;
			break;
		}
		
		/* filling */
		for ( int t = 0; t < passes; t++)
		{
			for ( int j = 0; j < e; j++)
			{
				for ( int i = 0; i < d; i++)
				{
					if ( Vector.areSame( temp[i][j], zeros) )
					{
						/* Just the vertical and horizontal ( cross ) */
						if ( j > 0 &&
								Vector.areSame( temp[i][j-1], max_value ) 
								&! ( !_periodic[1] && j < 2) )
							pass.add(new int[] { i, j });
						else if ( j < e-1 &&
								Vector.areSame( temp[i][j+1], max_value )
								&! ( !_periodic[1] && j > e-3 ) )
							pass.add(new int[] { i, j });
						else if ( i > 0 &&
								Vector.areSame( temp[i-1][j], max_value ) 
								&! ( !_periodic[0] && i < 2) )
							pass.add(new int[] { i, j });
						else if ( i < d-1 &&
								Vector.areSame( temp[i+1][j], max_value ) 
								&! ( !_periodic[0] && i > d-3 ) )
							pass.add(new int[] { i, j });
						else if ( region == Region.EDGE )
						{
							/* EDGE: also check for Diagonal neighbours */
							if ( j > 0 && i > 0 &&
									Vector.areSame( temp[i-1][j-1], max_value ) 
									&! ( !_periodic[1] && j < 2 )  
									&! ( !_periodic[0] && i < 2 ) )
								pass.add(new int[] { i, j });
							else if ( j < e-1 && i > 0 &&
									Vector.areSame( temp[i-1][j+1], max_value ) 
									&! ( !_periodic[1] && j > e-3 )  
									&! ( !_periodic[0] && i < 2 ) )
								pass.add(new int[] { i, j });
							else if ( i < d-1 && j > 0 &&
									Vector.areSame( temp[i+1][j-1], max_value ) 
									&! ( !_periodic[1] && j < 2 ) 
									&! ( !_periodic[0] && i > d-3 ) )
								pass.add(new int[] { i, j });
							else if ( i < d-1 && j < e-1 &&
									Vector.areSame( temp[i+1][j+1], max_value ) 
									&! ( !_periodic[0] && i > d-3 )  
									&! ( !_periodic[1] && j > e-3 ) )
								pass.add(new int[] { i, j });
						}
					}
				}
			}
			for ( int[] pos : pass )
				temp[pos[0]][pos[1]] = max_value;	
		}

		/* purge open holes */
		for ( int t = 0; t < purgeLimit; t++)
		{
			boolean cleared = true;
			for ( int[] pos : new LinkedList<int[]>(pass) )
			{
				if ( pos[1] > 0 && Vector.areSame( 
						temp[ pos[0] ][ pos[1]-1 ], zeros ) )
				{
					temp[ pos[0] ][ pos[1] ] = zeros;
					cleared = false;
					pass.remove(pos);
				}
				else if ( pos[1] < e-1 && Vector.areSame( 
						temp[ pos[0] ][ pos[1]+1 ], zeros ) )
				{
					temp[ pos[0] ][ pos[1] ] = zeros;
					cleared = false;
					pass.remove(pos);
				}
				else if ( pos[0] > 0 && Vector.areSame( 
						temp[ pos[0]-1 ][ pos[1] ], zeros )  )
				{
					temp[ pos[0] ][ pos[1] ] = zeros;
					cleared = false;
					pass.remove(pos);
				}
				else if ( pos[0] < d-1 && Vector.areSame(
						temp[ pos[0]+1 ][ pos[1] ], zeros ) )
				{
					temp[ pos[0] ][ pos[1] ] = zeros;
					cleared = false;
					pass.remove( pos );
				}
			}
			if ( cleared )
				break;
		}
		return pass;
	}
	
	/* 
	 * Extended 4SED approach for periodic boundaries and added padding for
	 * correct boundary handling in non-periodic situations.
	 * 
	 * FOUR-POINT SEQUENTIAL EUCLIDEAN DISTANCE MAPPING (4SED), NOTE 2d only
	 * P.E. Danielsson, Euclidean distance mapping, computer graphics and image 
	 * processing 14, 227-248 (1980)
	 */
	public int[][][] distanceMatrix(int[][][] out )
	{
		int[] p, q, step;
		int d = out.length;
		int e = out[0].length;
		
		/* iterations, first pass */
		for ( int j = 0; j < e; j++)
		{
			/* step 1 */
			step = new int[] { 0, 1 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				/* handle boundaries */	
				if ( j == 0 )
					if ( _periodic[1] )
						q = out[i][e-1];
					else
						q = max_value;
				else
					q = out[i][j-1];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
			/* step 2 */
			step = new int[] { 1, 0 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				/* handle boundaries */	
				if ( i == 0 )
					if ( _periodic[0] )
						q = out[d-1][j];
					else
						q = max_value;
				else
					q = out[i-1][j];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
			/* step 3 */
			for ( int i = d-1; i >= 0; i--)
			{
				p = out[i][j];
				/* handle boundaries */	
				if ( i == d-1 )
					if ( _periodic[0] )
						q = out[0][j];
					else
						q = max_value;
				else
					q = out[i+1][j];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
		}
		/* second pass */
		for ( int j = e-1; j >= 0; j--)
		{
			/* step 4 */
			step = new int[] { 0, 1 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				/* handle boundaries */	
				if ( j == e-1 )
					if ( _periodic[1] )
						q = out[i][0];
					else
						q = max_value;
				else
					q = out[i][j+1];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
			/* step 5 */
			step = new int[] { 1, 0 };
			for ( int i = 0; i < d; i++)
			{
				p = out[i][j];
				/* handle boundaries */	
				if ( i == 0 )
					if ( _periodic[0] )
						q = out[d-1][j];
					else
						q = max_value;
				else
					q = out[i-1][j];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
			/* step 6 */
			for ( int i = d-1; i > 0; i--)
			{
				p = out[i][j];	
				/* handle boundaries */	
				if ( i == d-1 )
					if ( _periodic[0] )
						q = out[0][j];
					else
						q = max_value;
				else
					q = out[i+1][j];
				if( euclidean( Vector.add( q, step ) ) < euclidean( p ) )
					out[i][j] = Vector.add( q, step );
			}
		}
		/* remove margin for non-periodic dimensions and return if none just 
		 * return out */
		for ( boolean b : _periodic )
			if ( !b )
				return Array.subarray(out, ( _periodic[0] ? 0 : 1 ), d-1, 
						( _periodic[1] ? 0 : 1 ), e-1, 0, 1);
		return out;
	}

	/**
	 * Calculates the euclidean distance from a int[] vector, distance is
	 * expressed in voxel lengths.
	 * @param vector
	 * @return
	 */
	private double euclidean(int[] vector)
	{
		double out = 0.0;
		for (int i : vector)
			out += (double) i * (double) i;
		return Math.sqrt(out);
	}
	
	public SpatialMap<Integer> euclideanMap( int[][][] matrix)
	{
		/* plot edge euclidean distance */
		int max = Array.max( matrix );
		SpatialMap<Integer> distance = new SpatialMap<Integer>();
		for( int i = 0; i < size[0]; i++)
		{
			for ( int j = 0; j < size[1]; j++ )
			{
				if ( euclidean( matrix[i][j] ) > max )
					distance.put( new int[] { i, j }, max ); 
				else
					distance.put( new int[] { i, j }, 
							( (Double) euclidean( matrix[i][j] ) ).intValue() ); 
			}
		}

		long millis = System.currentTimeMillis();
		if ( this._verbose )
			this.plot( distance, 1, "eucliMap" + millis, Helper.giveMeAGradient( max+1 ) );
		return distance;
	}
	
	public SpatialMap<Double> euclideanMapDbl( int[][][] matrix )
	{
		/* plot edge euclidean distance */
		int max = Array.max( matrix );
		SpatialMap<Double> distance = new SpatialMap<Double>();
		for( int i = 0; i < size[0]; i++)
		{
			for ( int j = 0; j < size[1]; j++ )
			{
				if (euclidean( matrix[i][j] ) > max )
					distance.put( new int[] { i, j }, Double.valueOf( max ) ); 
				else
					distance.put( new int[] { i, j }, 
							Double.valueOf( euclidean( matrix[i][j] ) ) ); 
			}
		}
		return distance;
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
				coords.addAll( coordinates( d+1, i, s, coord ) );
			}
		return coords;
	}
	
	/**
	 * convert an int[] into a double[] and use a scaler for appropriate length
	 * sizing
	 * @param c
	 * @param scalar
	 * @return
	 */
	private static double[] toContinuous(int[] c, double scalar)
	{
		double[] out = new double[c.length];
		for (int i = 0; i < c.length; ++i)
		    out[i] = (double) c[i] * scalar;
		return out;
	}
    
    /**
     * convert an array of integers ( int[][][] ) into a SpatialMap holding the
     * same values.
     * @param array
     * @return
     */
    private SpatialMap<Integer> gradientMap( int[][][] array )
    {
		SpatialMap<Integer> distance = new SpatialMap<Integer>();
		for( int i = 0; i < array.length; i++)
		{
			for ( int j = 0; j < array[0].length; j++ )
			{
				distance.put(new int[] { i, j }, 
						( (Double) euclidean( array[i][j] ) ).intValue() ); 
			}
		}
		return distance;
    }
    
    public Integer toInteger( Double in )
    {
    	return (Integer) ( in ).intValue();
    }
    
    /* ************************************************************************
     * Direct use
     * ***********************************************************************/
    
    /**
     * 
     * @param filter
     * @param fileName
     * @param intMap
     */
    public void plotPropertyAnalysis( String filter, String fileName, 
    		SpatialMap<Integer> intMap )
    {
    	this.plot( occuranceMap(filter, intMap), 1, fileName, 
    			Helper.DIFFERENTIATING_PALETTE );
    }
    
    public SpatialMap<Integer> occuranceMap( String filter )
    {
    	return occuranceMap( filter, this.agentMap() );
    }
        
    public SpatialMap<Integer> occuranceMap( String filter, 
    		SpatialMap<Integer> intMap ) 
    {
    	SpatialMap<Integer> myMap = new SpatialMap<Integer>();
    	SpatialMap<double[]> propMap = propertyLocalisation(filter, intMap);
    	for ( int[] spot : propMap.keySetNumeric() )
    		myMap.put(spot, toInteger( propMap.get( spot )[0]) );
    	return myMap;
    }
    
    public SpatialMap<List<Agent>> getAgentRaster()
    {
    	return this._agentRaster;
    }
    
    /* ************************************************************************
     * Debugging tools 
     * ***********************************************************************/

    /**
     * prints array to console
     * @param array
     */
	private String textArray(int[][][] array)
	{
		String out = "";
		for( int i = 0; i < array.length; i++ )
		{
			for ( int j = 0; j < array[0].length; j++ )
				out += ( Vector.toString( array[i][j] ) + "\t" );
			out += ("\n");
		}
		out += ("\n");
		return out;
	}
	
	/**
	 * plots array to svg file
	 * @param array
	 * @param name
	 */
	private void plotArray( int[][][] array, String name)
	{
		int max = Array.max(array);
		SpatialMap<Integer> distance = new SpatialMap<Integer>();
		for( int i = 0; i < array.length; i++)
		{
			for ( int j = 0; j < array[0].length; j++ )
			{
				if ( euclidean( array[i][j] ) > max )
					distance.put( new int[] { i,  j }, max ); 
				else
					distance.put( new int[] { i,  j }, 
							( (Double) euclidean( array[i][j] ) ).intValue() ); 
			}
		}
		this.plot(distance, 1, name, Helper.giveMeAGradient(max+1));
	}

}
