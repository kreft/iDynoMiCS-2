package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import analysis.quantitative.Raster;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import spatialRegistry.SpatialMap;
import surface.Point;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief agent attachment based scaled to distance from 'bulk area' at distance
 * 'regiondepth' from the biofilm. More exposed areas are more likeley to
 * receive attachers
 * 
 * TODO receive attachers
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AgentAttachment extends ProcessManager
{
	private String RASTER_SCALE = AspectRef.rasterScale;
	private String VERBOSE = AspectRef.verbose;
	private String REGION_DEPTH = AspectRef.regionDepth;
	/** 
	 * verbose raster output for debuggin purposes 
	 */
	private boolean _verbose = false;
	/**
	 * Raster used to create euclidean distance maps
	 */
	private Raster _raster;
	/**
	 * size of the voxels of the raster.
	 */
	private double _rasterScale;
	/**
	 * 'bulk' region distance, defined as a distance which is further away from
	 * any type of agent then _regionDepth.
	 */
	private int _regionDepth;
	
	@Override
	public void init( Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._verbose = Helper.setIfNone( this.getBoolean(VERBOSE), false );
		
		this._raster = new Raster( agents, _verbose );
		
		this._rasterScale = Helper.setIfNone( 
				this.getDouble( RASTER_SCALE ), 0.2 );
		
		this._regionDepth = Helper.setIfNone( 
				this.getInt( REGION_DEPTH ), 10 );
	}
	
	@Override
	protected void internalStep()
	{
		/* generate a raster */
 		this._raster.rasterize( _rasterScale );
 		
 		/* TODO receive incoming agents */
 		List<Agent> attachers = null;
		
 		/* setup agent distance map and the region distance map */
		SpatialMap<Integer> agentDistMap = this._raster.agentDistanceMap();
		SpatialMap<Double> regionMap = 
				this._raster.regionMap( this._regionDepth );
		
		/* local variables */
		SpatialMap<Double> likeleyhoodMap = new SpatialMap<Double>(); 
		double sumLikeleyhood = 0.0;
		
		/* iterate over the voxel and assign an attachment likeleyhood to the 
		 * biofilm edge voxels  */
		for( int[] vox : agentDistMap.keySetNumeric() )
		{
			/* if edge voxel */
			if (agentDistMap.get( vox ).equals( 1 ) )
			{
				/* e scaled to the distance from the 'bulk' region, scaling 
				 * factor is always <= 1 */
				double e = Math.exp( ( - this.getTimeStepSize() *
						( _regionDepth / regionMap.get( vox ) ) ) ); 
				
				/* save values */
				likeleyhoodMap.put(vox, e);
				sumLikeleyhood += e;
			}
		}
		
		/* iterate over all agents until all have been attached */
		for (Agent a : attachers)
		{
			/* internal variable counting cumulative likeleyhood */
			double t = 0.0;
			/* generate random number for attachment position */
			double q = ExtraMath.getNormRand() * sumLikeleyhood ;
			
			/* iterate over all voxels until the randomly chosen raster voxel is
			 * encountered. */
			for( int[] vox : likeleyhoodMap.keySetNumeric() )
			{
				/* center position of voxel */
				double[] c =  _raster.voxelCenter( vox );
				/* internal variable counting cumulative likeleyhood */
				t += likeleyhoodMap.get( vox );
				if ( t > q )
				{
					/* when we encounter our randomly chosen voxel locate the 
					 * agents body at the center of this voxel */
					Body b = (Body) a.get(AspectRef.agentBody);
					for( Point p : b.getPoints() )
					{
						/* small shift for numerical niceness */
						double[] shift = 
								Vector.randomPlusMinus( vox.length, 1.0E-10 );
						p.setPosition( Vector.add( c , shift) );
					}
					/* add the agent to the compartment */
					this._agents.addAgent( a );
					/* once we have added break the for loop and continue to the
					 * next agent. */
					break;
				}
			}
		}
	}
}
