/**
 * 
 */
package shape;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import linearAlgebra.Array;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import shape.Dimension.DimName;
import shape.iterator.ShapeIterator;
import shape.resolution.ResolutionCalculator;

/**
 * \brief Collection of instanciable {@code Shape} classes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public final class ShapeLibrary
{
	
	/*************************************************************************
	 * DUMMY SHAPE (for chemostats, etc)
	 ************************************************************************/
	
	/**
	 * \brief A zero-dimensional shape, which only has a volume.
	 * 
	 * <p>Used by {@code Compartment}s without spatial structure, e.g. a
	 * chemostat.</p>
	 */
	public static class Dimensionless extends Shape
	{
		protected double _volume = 0.0;
		
		public Dimensionless()
		{
			super();
		}
		
		public Dimensionless(double volume)
		{
			super();
			this.setTotalVolume(volume);
		}

		@Override
		public double[][][] getNewArray(double initialValue) {
			return Array.array(1, initialValue);
		}
		
		@Override
		public void instantiate(Element xmlElem, Settable parent)
		{
			super.instantiate(xmlElem, parent);
			
			this._volume = XmlHandler.obtainDouble(
					xmlElem, XmlRef.volume, this.defaultXmlTag());
		}
		
		@Override
		public Module getModule()
		{
			Module node = super.getModule();
			node.add( new Attribute(XmlRef.volume, 
						String.valueOf(this.getTotalVolume()), null, true ));
			return node;
		}
		
		public void setModule(Module node)
		{
			super.setModule(node);
			this._volume = Double.parseDouble( 
					node.getAttribute(XmlRef.volume).getValue() );
		}
		
		/**
		 * \brief Set this dimensionless shape's volume.
		 * 
		 * @param volume New volume to use.
		 */
		public void setTotalVolume(double volume)
		{
			this._volume = volume;
		}
		
		public double getTotalVolume()
		{
			return this._volume;
		}
		
		@Override
		public double getTotalRealVolume() {
			return this._volume;
		}
				
		@Override
		public void getLocalPositionTo(double[] destination, double[] location)
		{
			/* Do nothing! */
		}
		
		@Override
		public void getGlobalLocationTo(double[] destination, double[] local)
		{
			/* Do nothing! */
		}
		
		@Override
		public ResolutionCalculator getResolutionCalculator(int[] coord, int axis)
		{
			return null;
		}
		
		@Override
		public void setDimensionResolution(DimName dName, ResolutionCalculator resC)
		{
			/* Do nothing! */
		}
		
		public void setSurfaces()
		{
			/* Do nothing! */
		}
		
		@Override
		public int getTotalNumberOfVoxels()
		{
			return 1;
		}
		
		@Override
		public double getVoxelVolume(double[] origin, double[] upper)
		{
			return this._volume;
		}
		
		@Override
		public int[] nbhIteratorNext()
		{
			return null;
		}
		
		public boolean isReadyForLaunch()
		{
			if ( ! super.isReadyForLaunch() )
				return false;
			if ( this._volume <= 0.0 )
			{
				Log.out(Tier.CRITICAL,
							"Dimensionless shape must have positive volume!");
				return false;
			}
			return true;
		}

		@Override
		public double nhbCurrDistance()
		{
			return 0.0;
		}

		@Override
		public double nhbCurrSharedArea()
		{
			return 0.0;
		}
		
		@Override
		public void moveAlongDimension(double[] loc, DimName dimN, double dist)
		{
			/* Do nothing! */
		}

		@Override
		public double getBoundarySurfaceArea(DimName dimN, int extreme)
		{
			return 0.0;
		}
		
		@Override
		public double getRealSurfaceArea(DimName dimN, int extreme) {
			// TODO Auto-generated method stub
			return 0.0;
		}

		@Override
		public ShapeIterator getNewIterator(int strideLength)
		{
			return null;
		}
		
		@Override
		public boolean canGenerateCoarserMultigridLayer()
		{
			return false;
		}
		
		@Override
		public Shape generateCoarserMultigridLayer()
		{
			return null;
		}

	}
	
	/*************************************************************************
	 * SHAPES WITH STRAIGHT EDGES
	 ************************************************************************/
	
	/**
	 * \brief One-dimensional, straight {@code Shape} class.
	 */
	public static class Line extends CartesianShape
	{
		public Line()
		{
			super();
			this.setSignificant(1);
		}
	}
	
	/**
	 * \brief Two-dimensional, straight {@code Shape} class.
	 */
	public static class Rectangle extends CartesianShape
	{
		public Rectangle()
		{
			super();
			this.setSignificant(2);
		}
	}
	
	/**
	 * \brief Three-dimensional, straight {@code Shape} class.
	 */
	public static class Cuboid extends CartesianShape
	{
		public Cuboid()
		{
			super();
			this.setSignificant(3);
		}
	}
	
	/*************************************************************************
	 * CYLINDRICAL SHAPES
	 ************************************************************************/
	
	/**
	 * \brief Two-dimensional, round {@code Shape} class with an assumed linear
	 * thickness.
	 */
	public static class Circle extends CylindricalShape
	{
		public Circle()
		{
			super();
			this.setSignificant(2);
		}
	}
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with a linear third
	 * dimension.
	 */
	public static class Cylinder extends Circle
	{
		public Cylinder()
		{
			super();
			this.setSignificant(3);
		}
	}
	
	/*************************************************************************
	 * SPHERICAL SHAPES
	 ************************************************************************/
	
	// TODO SphereRadius, SphereSlice?
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with both second and
	 * third dimensions angular.
	 */
	public static class Sphere extends SphericalShape
	{
		public Sphere()
		{
			super();
			this.setSignificant(3);
		}
	}
}
