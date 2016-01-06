/**
 * 
 */
package grid;

import org.w3c.dom.Node;

import grid.SpatialGrid.ArrayType;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class GridBoundary
{
	/**
	 * Interface detailing what should be done at a boundary. Typical examples
	 * include Dirichlet and Neumann boundary conditions. 
	 */
	public interface GridMethod
	{
		void init(Node xmlNode);
		
		double getBoundaryFlux(SpatialGrid grid);
	}
	
	/*************************************************************************
	 * USEFUL SUBMETHODS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * TODO Rob [10Dec2015]: This method is doubtless wrong! 
	 * 
	 * @param bndryConcn
	 * @param gridConcn
	 * @param diffusivity
	 * @param surfaceArea
	 * @return
	 */
	public static double calcFlux(double bndryConcn, double gridConcn,
									double diffusivity, double surfaceArea)
	{
		return (bndryConcn - gridConcn) * diffusivity * surfaceArea;
	}
	
	
	/*************************************************************************
	 * COMMON GRIDMETHODS
	 ************************************************************************/
	
	/**
	 * \brief Grid boundary method where the variable is kept at a constant
	 * value.
	 * 
	 * <p>This value is zero by default.</p>
	 */
	public static class ConstantDirichlet implements GridMethod
	{
		private double _value = 0.0;
		
		public void init(Node xmlNode)
		{
			//this._value = TODO;
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			return calcFlux(_value, 
					grid.getValueAtCurrent(ArrayType.CONCN),
					grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
					grid.getNbhSharedSurfaceArea());
		}
		
		/* Unique methods (for testing) */
		
		public void setValue(double value)
		{
			this._value = value;
		}
	}
	
	/**
	 * \brief Grid boundary method where the variable gradient is kept at a
	 * constant value.
	 * 
	 * <p>This value is zero by default.</p>
	 */
	public static class ConstantNeumann implements GridMethod
	{
		private double _gradient = 0.0;
		
		public void init(Node xmlNode)
		{
			//this._gradient = TODO;
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			// TODO we probably need to get the voxel-voxel details here.
			return this._gradient;
		}
		
		/* Unique methods (for testing) */
		
		public void setGradient(double gradient)
		{
			this._gradient = gradient;
		}
	}
	
	public static class ZeroFlux implements GridMethod
	{
		public void init(Node xmlNode)
		{
			
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			return 0.0;
		}
	}
	
	public static class Cyclic implements GridMethod
	{
		public void init(Node xmlNode)
		{
			
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			int[] nbh = grid.cyclicTransform(grid.neighborCurrent());
			double d = 0.5*(grid.getValueAtCurrent(ArrayType.DIFFUSIVITY)+
							 grid.getValueAt(ArrayType.DIFFUSIVITY, nbh));
			return calcFlux(grid.getValueAt(ArrayType.CONCN, nbh),
							grid.getValueAtCurrent(ArrayType.CONCN),
							d, grid.getNbhSharedSurfaceArea());
		}
	}
}
