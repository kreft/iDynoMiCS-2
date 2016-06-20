/**
 * 
 */
package shape;

import boundary.Boundary;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
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
	public static class BoundaryCyclic extends Boundary
	{
		public BoundaryCyclic()
		{
			
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
	}
}
