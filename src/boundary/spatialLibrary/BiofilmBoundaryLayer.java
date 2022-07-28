package boundary.spatialLibrary;

import static grid.ArrayType.WELLMIXED;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import boundary.WellMixedBoundary;
import boundary.library.ChemostatToBoundaryLayer;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import debugTools.SegmentTimer;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import spatialRegistry.SpatialRegistry;
import spatialRegistry.splitTree.SplitTree;
import surface.Ball;
import surface.BoundingBox;
import surface.Surface;
import surface.Voxel;
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

	public void updateWellMixedArrayOld()
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
			Voxel innerBox = new Voxel(Vector.vector(numDim,0.0),
					Vector.vector(numDim,0.0));

			/* naming parent loop so that we can break
			out of it instantly when we hit and agent */
			shapeloop : while ( aShape.isIteratorValid() )
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
								coords = aShape.iteratorNext();
								continue shapeloop;
							}
				coords = aShape.iteratorNext();
			}
		}
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
		double[] min = Vector.zerosDbl(	aShape.getNumberOfDimensions() );


		LinkedList<int[]> reiterate = new LinkedList<int[]>();

		int[] coords = aShape.resetIterator();
		while ( aShape.isIteratorValid() )
		{

			double[] voxelOrigin = aShape.getVoxelOrigin(coords);
			double[] voxelUpper = aShape.getVoxelUpperCorner(coords);
			Voxel box = new Voxel( Vector.subset(voxelOrigin, aShape.getNumberOfDimensions()),
					Vector.subset(voxelUpper, aShape.getNumberOfDimensions()) );
			if ( ! this._agents.treeSearch(box.boundingBox(aShape)).isEmpty() )
			{
				grid.setValueAt(WELLMIXED, coords,
						WellMixedConstants.NOT_MIXED);
				reiterate.add(Vector.copy(coords));
			}
			else
			{
				grid.setValueAt(WELLMIXED, coords,
						Double.MAX_VALUE);
			}
			coords = aShape.iteratorNext();

		}

		if (this._layerThickness > 0.0 )
		{
			iterateDiffusionRegion( reiterate, (this._layerThickness / aShape.getVoxelSideLengths(coords)[0]) );

			coords = aShape.resetIterator();
			while ( aShape.isIteratorValid() ) {
				//NOTE assuming voxel is cube
				if( grid.getValueAt(WELLMIXED, coords) < (this._layerThickness / aShape.getVoxelSideLengths(coords)[0]) )
					grid.setValueAt(WELLMIXED, coords, WellMixedConstants.NOT_MIXED );
				coords = aShape.iteratorNext();
			}
		}
	}

	private void iterateDiffusionRegion(LinkedList<int[]> outerBound, Double treshold)
	{
		LinkedList<int[]> reiterate = new LinkedList<int[]>();
		SpatialGrid grid = this._environment.getCommonGrid();
		int[] dims = Vector.dimensions( grid.getArray(WELLMIXED) );
		boolean[] periodic = this._environment.getShape().getIsCyclicNaturalOrder();

		double sqrt2 = 1.41421356237;

		for( int[] coords : outerBound )
		{
			Double current = grid.getValueAt(WELLMIXED, coords);
			if ( current > treshold )
				break;
			int[] temp;
			boolean valid;
			for( int i = 0; i < dims.length; i++)
			{
				temp = Vector.copy(coords);
				// down
				temp[i] -= 1;
				valid = push(temp, dims, periodic, current + 1.0, grid, reiterate);

				// down neighbors (x+ gets: x+ z+, x+ z-. y+ gets: y+ x-, y+ x+ etc.)
				if( valid && i > 0)
				{
					temp[i-1] -= 1;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
					temp[i-1] += 2;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
				}
				else
				{
					temp[dims.length-1] -= 1;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
					temp[dims.length-1] += 2;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
				}

				// up
				temp = Vector.copy(coords);
				temp[i] += 1;
				valid = push(temp, dims, periodic, current + 1.0, grid, reiterate);

				// up neighbors
				if( valid && i > 0)
				{
					temp[i-1] -= 1;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
					temp[i-1] += 2;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
				}
				else
				{
					temp[dims.length-1] -= 1;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
					temp[dims.length-1] += 2;
					push(temp, dims, periodic, current + sqrt2, grid, reiterate);
				}

			}
		}
		if( !reiterate.isEmpty() )
			iterateDiffusionRegion( reiterate , treshold);
	}

	/** updates diffusion distance of potential neighbor to step. Returns true if neighbor is valid
	 * false if neighbor is invalid
	 *
	 * @param vocal
	 * @param dims
	 * @param periodic
	 * @param step
	 * @param grid
	 * @param reiterate
	 * @return
	 */
	public boolean push( int[] vocal, int[] dims, boolean[] periodic, double step,
					  SpatialGrid grid, LinkedList<int[]> reiterate )
	{
		vocal = periodicCoord(vocal, dims, periodic);
		if( vocal != null ) {
			Double neighbour = grid.getValueAt(WELLMIXED, vocal);
			if (neighbour > step) {
				grid.setValueAt(WELLMIXED, vocal, step);
				reiterate.add(Vector.copy(vocal));
			}
		}
		else {
			return false;
		}
		return true;
	}

	/**
	 * returns Periodic coord, returns the same value if coord is already within bounds,
	 * returns null if coord is out of spec
	 *
	 * @param coords
	 * @param dims
	 * @param periodic
	 * @return
	 */
	public int[] periodicCoord( int[] coords, int[] dims, boolean[] periodic )
	{
		int[] out = new int[coords.length];
		for( int i = 0; i < coords.length; i++ )
		{
			if( coords[i] >= 0 && coords[i] < dims[i] )
				out[i] = coords[i];
			else if ( !periodic[i] )
			{
				// not in range and not periodic
				return null;
			}
			else {
				if( coords[i] == -1 )
					out[i] = dims[i]-1;
				else if( coords[i] == dims[i] )
					out[i] = 0;
				else
					// periodic but jumps more than one step (somehow)
					return null;
			}
		}
		return out;
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
