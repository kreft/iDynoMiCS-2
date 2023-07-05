package surface;

import org.w3c.dom.Element;

import generalInterfaces.HasBoundingBox;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import shape.Shape;
import utility.Helper;
import utility.StandardizedImportMethods;

/**
 * The constant-normal form of the (infinite) plane
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Plane extends Surface implements HasBoundingBox 
{

	/**
	 * The normal vector of the plane.
	 */
	private Point _normal;
	
	/**
	 * The dot product of the plane's normal vector with a point on the plane.
	 */
	private double _d;

	private BoundingBox boundingBox = new BoundingBox();

	/**
	 * \brief Construct plane from its normal and the dot product of the
	 * plane's normal vector with a point on the plane.
	 * 
	 * @param normal
	 * @param d
	 */
	public Plane(double[] normal, double d)
	{
		this._normal = new Point(normal);
		this._d = d;
	}
	
	/**
	 * \brief Construct plane from its normal vector and a point on the plane.
	 * 
	 * @param normal
	 * @param point
	 */
	public Plane(double[] normal, double[] point)
	{
		this._normal = new Point(normal);
		this._d = Vector.dotProduct(normal, point);
	}
	
	/**
	 * \brief Construct plane from 3 points on the plane.
	 * 
	 * @param pointA
	 * @param pointB
	 * @param pointC
	 */
	public Plane(double[] pointA, double[] pointB, double[] pointC)
	{
		this._normal = new Point( Vector.normaliseEuclid(Vector.crossProduct(
				Vector.minus(pointB, pointA), Vector.minus( pointC, pointA ))));
		this._d = Vector.dotProduct(this._normal.getPosition(), pointA);
	}
	
	public Plane(Element xmlElem)
	{
		if( !Helper.isNullOrEmpty( xmlElem ))
		{
			this._normal = StandardizedImportMethods.
					pointImport(xmlElem, this, 1)[0];
			this._d = Double.valueOf( 
					xmlElem.getAttribute( XmlRef.valueAttribute ));
		}
	}
	
	public Module appendToModule(Module modelNode) 
	{
		modelNode.add(new Attribute(XmlRef.valueAttribute, 
				String.valueOf(this._d), null, false ));

		modelNode.add(_normal.getModule() );
		return modelNode;
	}
	
		@Override
	public Type type()
	{
		return Surface.Type.PLANE;
	}
		
	@Override
	public int dimensions() 
	{
		return this._normal.nDim();
	}

	@Override
	public BoundingBox boundingBox(Shape shape) {

		return this.boundingBox(0.0, shape);
	}

	@Override
	public BoundingBox boundingBox(double margin, Shape shape) {
		double[] lower = new double[_normal.nDim()];
		double[] upper = new double[_normal.nDim()];
		int n = 0;
		for ( int i = 0; i < _normal.nDim(); i++ )
		{
			/* 
			 * a dimension an infinite plane goes into the box will go into 
			 * infinitely as well
			 */
			if (_normal.getPosition()[i] == 0.0 )
			{
				lower[i] = -Double.MAX_VALUE;
				upper[i] = Double.MAX_VALUE;
			}
			/*
			 * if the infinite plane's normal is not right angled on two 
			 * dimensions the bounding box of the infinite plane will cover the 
			 * entire domain
			 */
			else if ( n > 0 )
			{
				return boundingBox.get(
						Vector.setAll(lower, -Double.MAX_VALUE),
						Vector.setAll(upper, Double.MAX_VALUE));
			}
			/*
			 * if the plane's normal is pointing in a single dimension we can
			 * have a bounding box that is not infinite in that dimension
			 */
			else
			{
				n++;
				lower[i] = _normal.getPosition()[i] * _d - margin;
				upper[i] = _normal.getPosition()[i] * _d + margin;
			}
		}
		return boundingBox .get(lower, upper);
	}

	public double[] getNormal() 
	{
		return this._normal.getPosition();
	}

	public double getD() 
	{
		return this._d;
	}
}
