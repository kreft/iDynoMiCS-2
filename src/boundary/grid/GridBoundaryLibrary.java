/**
 * 
 */
package boundary.grid;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import boundary.grid.GridBoundary;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import modelBuilder.InputSetter;
import modelBuilder.ParameterSetter;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GridBoundaryLibrary
{

	/**
	 * \brief Grid boundary method where the variable is kept at a constant
	 * value.
	 * 
	 * <p>This value is zero by default.</p>
	 */
	public static class ConstantDirichlet extends GridBoundary
	{
		private double _value = 0.0;
		
		public void init(Element xmlNode)
		{
			//this._value = TODO;
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			return GridBoundary.calcFlux(_value, 
					grid.getValueAtCurrent(ArrayType.CONCN),
					grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
					grid.getNbhSharedSurfaceArea());
		}
		
		/* Unique methods (for testing) */
		
		// TODO replace uses of this with acceptInput("value", value)
		public void setValue(double value)
		{
			this._value = value;
		}
		
		@Override
		public String getXml()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			out.add(new ParameterSetter("value", this, "Double", 0.0));
			return out;
		}
		
		public void acceptInput(String name, Object input)
		{
			if ( name.equals("value") && input instanceof Double )
				this._value = (Double) input;
		}
	}

	/**
	 * \brief Grid boundary method where the variable gradient is kept at a
	 * constant value.
	 * 
	 * <p>This value is zero by default.</p>
	 */
	public static class ConstantNeumann extends GridBoundary
	{
		private double _gradient = 0.0;
		
		@Override
		public void init(Element xmlElem)
		{
			//this._gradient = TODO;
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			// TODO we probably need to get the voxel-voxel details here.
			return this._gradient;
		}
		
		// TODO replace uses of this with acceptInput("gradient", gradient)
		public void setGradient(double gradient)
		{
			this._gradient = gradient;
		}
		
		@Override
		public String getXml()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			out.add(new ParameterSetter("gradient", this, "Double", 0.0));
			return out;
		}
		
		public void acceptInput(String name, Object input)
		{
			if ( name.equals("gradient") && input instanceof Double )
				this._gradient = (Double) input;
		}
	}
	
	
	public static class ZeroFlux extends GridBoundary
	{
		public void init(Element xmlNode)
		{
			
		}
		
		public double getBoundaryFlux(SpatialGrid grid)
		{
			return 0.0;
		}
		
		@Override
		public String getXml()
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
}
