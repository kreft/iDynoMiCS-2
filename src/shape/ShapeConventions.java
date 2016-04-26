/**
 * 
 */
package shape;

import org.w3c.dom.Element;

import boundary.Boundary;
import boundary.grid.GridMethod;
import grid.SpatialGrid;
import shape.resolution.ResolutionCalculator.ResCalc;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class ShapeConventions
{
	/**
	 * 
	 * 
	 */
	public enum DimName
	{
		X,
		Y,
		Z,
		R,
		THETA,
		PHI;
	}
	
	/**
	 * \brief Dummy resolution calculator that always has exactly one voxel.
	 */
	// NOTE exploratory work, may not be used
	public static class SingleVoxel extends ResCalc
	{
		public SingleVoxel()
		{
			this._nVoxel = 1;
			this._length = 1.0;
		}
		
		@Override
		public void setLength(double length)
		{
			this._length = length;
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
			_defaultGridMethod = new CyclicGrid();
		}
	}
	
	public static class CyclicGrid extends GridMethod
	{
		@Override
		public void init(Element xmlNode)
		{
			/* Do nothing here. */ 
		}
		
		@Override
		public double getBoundaryFlux(SpatialGrid grid)
		{
			// TODO
			return 0;
		}
		
		@Override
		public String getXml()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
}
