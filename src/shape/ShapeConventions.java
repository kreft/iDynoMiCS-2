/**
 * 
 */
package shape;

import boundary.SpatialBoundary;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import shape.Dimension.DimName;
import shape.resolution.ResolutionCalculator.SameRes;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class ShapeConventions
{

	/**
	 * \brief Dummy resolution calculator that always has exactly one voxel.
	 */
	// NOTE exploratory work, may not be used
	public static class SingleVoxel extends SameRes
	{
		public SingleVoxel()
		{
			this._nVoxel = 1;
			this._length = 1.0;
		}
		
		@Override
		public void init(double targetResolution, double totalLength)
		{
			this._nVoxel = 1;
			this._length = 1.0;
		}
		
		@Override
		public void setResolution(double targetResolution)
		{
			this._resolution = targetResolution;
		}

		@Override
		public double getMinResolution()
		{
			return this._length;
		}

		@Override
		public double getResolution(int voxelIndex)
		{
			return this._length;
		}

		@Override
		public double getCumulativeResolution(int voxelIndex)
		{
			return this._length;
		}

		@Override
		public int getVoxelIndex(double location)
		{
			return 0;
		}
	}
	
	/**
	 * \brief Dummy class for cyclic dimensions.
	 * 
	 * Should only be initialised by Dimension and never from protocol file.
	 */
	public static class BoundaryCyclic extends SpatialBoundary
	{
		public BoundaryCyclic(DimName dim, int extreme)
		{
			super(dim, extreme);
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			/* Do nothing! */
		}

		@Override
		protected Class<?> getPartnerClass()
		{
			return BoundaryCyclic.class;
		}

		@Override
		public void updateConcentrations(EnvironmentContainer environment)
		{
			/* Do nothing! */
		}

		@Override
		public double getFlux(SpatialGrid grid)
		{
			return 0.0;
		}

		@Override
		public boolean needsToUpdateWellMixed()
		{
			return false;
		}

		@Override
		public void updateWellMixedArray(
				SpatialGrid grid, AgentContainer agents)
		{
			/* Do nothing! */
		}
	}
}
