package boundary.spatialLibrary;

import static grid.ArrayType.WELLMIXED;

import java.util.List;

import agent.Agent;
import agent.Body;
import boundary.WellMixedBoundary;
import boundary.library.ChemostatToBoundaryLayer;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Ball;
import surface.BoundingBox;
import surface.Surface;
import surface.collision.Collision;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class BiofilmBoundaryLayer extends WellMixedBoundary
{
	/**
	 * Spherical surface object with radius equal to {@link #_layerThickness}.
	 * Used here for updating the well-mixed array.
	 */
	protected Ball _gridSphere;
	/**
	 * For the random walk after insertion, we assume that the agent has the
	 * stochastic move event.
	 */
	// NOTE This is not a permanent solution.
	public static String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	/**
	 * For the random walk after insertion, we assume that the agent has the
	 * pull distance aspect.
	 */
	// NOTE This is not a permanent solution.
	public static String CURRENT_PULL_DISTANCE = 
			AspectRef.collisionCurrentPullDistance;
	/**
	 * For the random walk after insertion, we use an arbitrary time step size.
	 */
	// NOTE This is not a permanent solution.
	public static double MOVE_TSTEP = 1.0;
	
	/* ***********************************************************************
	 * CONSTRUCTOR
	 * **********************************************************************/
	
	public BiofilmBoundaryLayer()
	{
		this._dominant = true;
	}
	
	@Override
	public void setContainers(
			EnvironmentContainer environment, AgentContainer agents)
	{
		super.setContainers(environment, agents);
		this.tryToCreateGridSphere();
	}
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! super.isReadyForLaunch() )
			return false;
		return (this._layerThickness >= 0.0);
	}

	private void tryToCreateGridSphere()
	{
		if ( this._agents == null || this._layerThickness <= 0.0 )
			return;
		
		Shape shape = this._agents.getShape();
		Collision collision = new Collision(null, null, shape);
		double[] zeros = Vector.zerosDbl(shape.getNumberOfDimensions());
		this._gridSphere = new Ball(zeros, this._layerThickness);
		this._gridSphere.init(collision);
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	@Override
	protected boolean needsLayerThickness()
	{
		return true;
	}

	@Override
	public void setLayerThickness(double thickness)
	{
		/*
		 * If the boundary layer thickness changes, we also need to change the 
		 * radius of the ball used in updating the well-mixed array.
		 * NOTE: One sets a Ball's radius, not diameter
		 */
		super.setLayerThickness(thickness);
		this.tryToCreateGridSphere();
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	protected double calcDiffusiveFlow(SpatialGrid grid)
	{
		double concn = this._concns.get(grid.getName());
		return this.calcDiffusiveFlowFixed(grid, concn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateWellMixedArray()
	{
		Shape aShape = this._environment.getShape();
		SpatialGrid grid = this._environment.getCommonGrid();
		int numDim = aShape.getNumberOfDimensions();
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		if (this._layerThickness == 0.0 )
		{
			int[] coords = aShape.resetIterator();
			BoundingBox box = new BoundingBox();
			List<Agent> neighbors;
			while ( aShape.isIteratorValid() )
			{
				double[] voxelOrigin = aShape.getVoxelOrigin(coords);
				double[] voxelUpper = aShape.getVoxelUpperCorner(coords);
				box.get(voxelOrigin, voxelUpper);
				neighbors = this._agents.treeSearch(box);
				if (neighbors.size() > 0)
				{
					grid.setValueAt(WELLMIXED, coords, 
							WellMixedConstants.NOT_MIXED);
				}
			coords = aShape.iteratorNext();
			}
		}
		else
		{
			int[] coords = aShape.resetIterator();
			double[] voxelCenter = aShape.getVoxelCentre(coords);
			double[] voxelCenterTrimmed = Vector.zerosDbl(numDim);
			List<Agent> neighbors;
			BoundingBox box;
			while ( aShape.isIteratorValid() )
			{
				aShape.voxelCentreTo(voxelCenter, coords);
				Vector.copyTo(voxelCenterTrimmed, voxelCenter);
				this._gridSphere.setCenter(voxelCenterTrimmed);
				/*
				 * Find all nearby agents. Set the grid to zero if an agent is
				 * within the grid's sphere
				 */
				box = this._gridSphere.boundingBox(this._agents.getShape());
				neighbors = this._agents.treeSearch(box);
				for ( Agent a : neighbors )
					for (Surface s : (List<Surface>) ((Body) 
							a.get( AspectRef.agentBody )).getSurfaces() )
						if ( this._gridSphere.distanceTo(s) < 0.0 )
							{
								grid.setValueAt(WELLMIXED, coords, 
										WellMixedConstants.NOT_MIXED);
								break;
							}
				coords = aShape.iteratorNext();
			}
		}
	}

	@Override
	public void additionalPartnerUpdate()
	{
		ChemostatToBoundaryLayer p = (ChemostatToBoundaryLayer) this._partner;
		for ( String soluteName : this._environment.getSoluteNames() )
			this._concns.put(soluteName, p.getSoluteConcentration(soluteName));
	}
	
	public boolean isSolid()
	{
		return false;
	}
}
