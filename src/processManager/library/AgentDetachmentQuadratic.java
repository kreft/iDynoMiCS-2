package processManager.library;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import agent.Agent;
import analysis.quantitative.Raster;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDeparture;
import referenceLibrary.AspectRef;
import spatialRegistry.SpatialMap;
import utility.ExtraMath;

/**
 * \brief Agent detachment from the biofilm surface with a detachment speed
 * that scales with the square of the local biofilm thickness, as in the
 * quadratic detachment (DS_Quadratic) of iDynoMiCS 1.
 *
 * <p>The survival probability of an agent over a time step dt is
 * exp(-rate * dt), following dA/dt = -rate * A.</p>
 */
public class AgentDetachmentQuadratic extends ProcessDeparture
{

	private String DETACHMENT_RATE = AspectRef.detachmentRate;
	private String RASTER_SCALE = AspectRef.rasterScale;
	private String VERBOSE = AspectRef.verbose;
	private String REGION_DEPTH = AspectRef.regionDepth;

	/**
	 * Dimension index along which biofilm thickness is measured (Y).
	 */
	private static final int HEIGHT_DIM = 1;

	private boolean _verbose = false;
	private Raster _raster;
	/**
	 * kDet: erosion speed per squared thickness [1/(length * time)].
	 */
	private double _kDet;
	private double _rasterScale;
	private int _regionDepth;

	@Override
	public void init( Element xmlElem, EnvironmentContainer environment,
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		/* NOTE: requesting an aspect that is not set interrupts the
		 * simulation, so optional parameters are checked with isAspect()
		 * before they are read. */
		this._verbose = this.isAspect( VERBOSE ) &&
				this.getBoolean( VERBOSE );

		this._raster = new Raster( agents, _verbose );

		/* Required: there is no sensible case-independent default. */
		this._kDet = this.getDouble( DETACHMENT_RATE );

		this._rasterScale = this.isAspect( RASTER_SCALE ) ?
				this.getDouble( RASTER_SCALE ) : 0.2;

		this._regionDepth = this.isAspect( REGION_DEPTH ) ?
				this.getInt( REGION_DEPTH ) : 10;
	}

	/**
	 * \brief Select the agents that detach this time step.
	 *
	 * @return Agents leaving the compartment.
	 */
	@Override
	protected LinkedList<Agent> agentsDepart()
	{
		this._raster.rasterize( _rasterScale );

		SpatialMap<Integer> agentDistMap = this._raster.agentDistanceMap();
		SpatialMap<Double> regionMap =
				this._raster.regionMap( this._regionDepth );

		List<Agent> agentList;
		Set<Agent> handledAgents = new HashSet<Agent>();
		LinkedList<Agent> departingAgents = new LinkedList<Agent>();
		for( int[] vox : agentDistMap.keySetNumeric() )
		{
			/* Only surface voxels: biomass directly adjacent to liquid. */
			if (agentDistMap.get( vox ).equals( 1 ) )
			{
				agentList = _raster.getAgentRaster().get( vox );
				double e = Math.exp( - this.getTimeStepSize() *
						this.localRate( vox, regionMap.get( vox ) ) );
				for ( Agent a : agentList )
				{
					/* Set.add returns false if the agent was already handled,
					 * so each agent gets exactly one chance per step. */
					if( handledAgents.add( a ) )
					{
						if( ExtraMath.getUniRandDbl() > e )
						{
							departingAgents.add(a);
						}
					}
				}
			}
		}

		return departingAgents;
	}

	/**
	 * \brief Detachment rate (per unit time) for agents in a surface voxel.
	 *
	 * @param vox Surface voxel index.
	 * @param bulkDistance Distance (in voxels) from the voxel to the bulk
	 * region, from {@link Raster#regionMap(int)}.
	 * @return Local detachment rate [1/time].
	 */
	private double localRate( int[] vox, double bulkDistance )
	{
		/* Height of the voxel centre above the substratum. */
		double thickness = this._raster.voxelCenter( vox )[ HEIGHT_DIM ];
		/* Local erosion speed as in iDynoMiCS 1 (DS_Quadratic). */
		double erosionSpeed = this._kDet * thickness * thickness;
		/* Exposure to the bulk, as in AgentDetachment. */
		double exposure = this._regionDepth / bulkDistance;
		/* Convert the speed to a per-agent rate via the voxel length. */
		return ( erosionSpeed / this._rasterScale ) * exposure;
	}
}
