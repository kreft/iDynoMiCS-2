package surface;

import java.util.LinkedList;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.HasBoundingBox;
import settable.Module;
import shape.Shape;
import surface.BoundingBox;
import utility.Helper;
import utility.StandardizedImportMethods;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Voxel extends Surface implements HasBoundingBox {
	
	/**
	 * Lower corner at position 0, voxel dimensions at position 1
	 */
	private Point[] _points = new Point[2];

    public Voxel(double[] lower, double[] dimensions)
    {
    	this.setLower(lower);
    	this.setDimensions(dimensions);
    }
	
	public Voxel(LinkedList<double[]> points)
	{
		//TODO
	}
	
	public Voxel(Element xmlElem)
	{
		if( !Helper.isNullOrEmpty( xmlElem ))
		{
			this._points = StandardizedImportMethods.
					pointImport(xmlElem, this, 2);
		}
	}
	
	public Module appendToModule(Module modelNode) 
	{
		for (Point p : _points )
			modelNode.add(p.getModule() );
		return modelNode;
	}

	public Type type() {
		return Surface.Type.VOXEL;
	}

	@Override
	public int dimensions() 
	{
		return this._points[0].nDim();
	}
	
	protected BoundingBox boundingBox = new BoundingBox();
	
	/**
	 * Currently we assume every voxel is on grid en thus never periodic. Also
	 * we do not allow margin
	 */
	public BoundingBox boundingBox(double margin, Shape shape)
	{
		if ( margin != 0.0 && Log.shouldWrite(Tier.CRITICAL) )
			Log.out(Tier.CRITICAL, "WARNING: attempt to create voxel "
					+ "boundingbox with margin, this is currently not "
					+ "supporting, returning voxelnwithout margin.");
		return this.boundingBox(shape);
	}

	public BoundingBox boundingBox(Shape shape)
	{
		return boundingBox.get(getDimensions(),getLower());
	}

	public double[] getLower() 
	{
		return _points[0].getPosition();
	}

	public void setLower(double[] _lower) 
	{
		this._points[0] = new Point(_lower);
	}

	public double[] getDimensions() 
	{
		return _points[1].getPosition();
	}

	public void setDimensions(double[] _dimensions) 
	{
		this._points[0] = new Point(_dimensions);
	}
}