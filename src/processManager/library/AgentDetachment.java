package processManager.library;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import analysis.quantitative.Raster;
import bookkeeper.KeeperEntry.EventType;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import spatialRegistry.SpatialMap;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief agent detachment based scaled to distance from 'bulk area' at distance
 * 'regiondepth' from the biofilm.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AgentDetachment extends ProcessManager
{
	
	private String DETACHMENT_RATE = AspectRef.detachmentRate;
	private String RASTER_SCALE = AspectRef.rasterScale;
	private String VERBOSE = AspectRef.verbose;
	private String REGION_DEPTH = AspectRef.regionDepth;
	
	private boolean _verbose = false;
	private Raster _raster;
	private double _detachmentRate;
	private double _rasterScale;
	private int _regionDepth;
	
	@Override
	public void init( Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._verbose = Helper.setIfNone( this.getBoolean(VERBOSE), false );
		
		this._raster = new Raster( agents, _verbose );
		
		/* FIXME replace with obtain value as they are case dependent */
		this._detachmentRate = Helper.setIfNone( 
				this.getDouble( DETACHMENT_RATE ), 0.1 );
		
		this._rasterScale = Helper.setIfNone( 
				this.getDouble( RASTER_SCALE ), 0.2 );
		
		this._regionDepth = Helper.setIfNone( 
				this.getInt( REGION_DEPTH ), 10 );
	}
	
	/**
	 * 
	 * With agent removal over time t dictated by rate r the number of remaining
	 * Agents A can be expressed as:
	 * 
	 * dA/dt = -rA -> A(t) = A(0)*exp(-rt)
	 * 
	 * We only consider removal from the biofilm surface and refer to this as 
	 * detachment.
	 */
	@Override
	protected void internalStep()
	{
 		this._raster.rasterize( _rasterScale );
		
		SpatialMap<Integer> agentDistMap = this._raster.agentDistanceMap();
		SpatialMap<Double> regionMap = 
				this._raster.regionMap( this._regionDepth );
		
		List<Agent> agentList;
		LinkedList<Agent> handledAgents = new LinkedList<Agent>();
		for( int[] vox : agentDistMap.keySetNumeric() )
		{
			if (agentDistMap.get( vox ).equals( 1 ) )
			{
				agentList = _raster.getAgentRaster().get( vox );
				double e = Math.exp( ( - this.getTimeStepSize() *
						this._detachmentRate*_regionDepth / regionMap.get( vox ) ) ); 
				for ( Agent a : agentList )
				{
					if( !handledAgents.contains( a ) )
					{
						handledAgents.add( a );
						if( ExtraMath.getUniRandDbl() > e )
						{
							/* FIXME DEPARTURE! */
							this._agents.registerRemoveAgent( a , EventType.REMOVED, "detach", null);
						}
					}
						
				}
			}
		}
		
		
		
	}

}
