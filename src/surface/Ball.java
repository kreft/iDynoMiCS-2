package surface;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import generalInterfaces.Copyable;
import generalInterfaces.HasBoundingBox;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import utility.Helper;
import utility.StandardizedImportMethods;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Ball extends Surface implements HasBoundingBox, Copyable
{
	/**
	 * Location of the center of this sphere.
	 */
	public Point _point;
	/**
	 * Radius of this sphere.
	 */
	public double _radius;

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public Ball(Point point, double radius)
	{
		this._point = point;
		this._radius = radius;
	}


	/**
	 * \brief Copy constructor.
	 * 
	 * @param sphere
	 */
	public Ball(Ball sphere)
	{
		this._point = (Point) ObjectFactory.copy(sphere._point);
		this._radius = (double) ObjectFactory.copy(sphere._radius);
	}

	/**
	 * 
	 * @param center
	 * @param radius
	 */
	public Ball(double[] center, double radius)
	{
		this._point = new Point(center);
		this._radius = radius;
	}

	/**
	 * \brief Construct a ball with zero radius.
	 * 
	 * @param center
	 */
	public Ball(double[] center)
	{
		this(center, 0.0);
	}
	
	public Ball(Element xmlElem)
	{
		if( !Helper.isNullOrEmpty( xmlElem ))
		{
			this._point = StandardizedImportMethods.
					pointImport(xmlElem, this, 1)[0];
			this._radius = Double.valueOf( xmlElem.getAttribute(XmlRef.radius));
		}
	}

	@Override
	public Object copy()
	{
		Point p = (Point) this._point.copy();
		double r = (double) ObjectFactory.copy(this._radius);
		return new Ball(p, r);
	}
	

	
	/*************************************************************************
	 * SIMPLE GETTERS & SETTERS
	 ************************************************************************/

	public Type type()
	{
		return Surface.Type.SPHERE;
	}

	/**
	 * @return Location vector of the center of this sphere.
	 */
	public double[] getCenter()
	{
		return this._point.getPosition();
	}

	/**
	 * 
	 * @param position
	 */
	public void setCenter(double[] position)
	{
		this._point.setPosition(position);
	}

	public double getRadius()
	{
		return this._radius;
	}

	public void setRadius(double radius)
	{
		this._radius = radius;
	}
	
	public Module appendToModule(Module modelNode) 
	{
		modelNode.add(new Attribute(XmlRef.radius, 
				String.valueOf(this._radius), null, false ));

		modelNode.add(_point.getModule() );
		return modelNode;
	}

	/*************************************************************************
	 * BOUNDING BOX
	 ************************************************************************/
	
	protected BoundingBox boundingBox = new BoundingBox();
	
	public BoundingBox boundingBox(double margin, Shape shape)
	{
		return boundingBox.get(this.getCenter(), this._radius, margin);
	}

	public BoundingBox boundingBox(Shape shape)
	{
		return boundingBox.get(this.getCenter(), this._radius);
	}

	@Override
	public int dimensions() 
	{
		return this._point.nDim();
	}
}