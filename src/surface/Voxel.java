package surface;

import java.util.LinkedList;

import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.HasBoundingBox;
import shape.Shape;
import surface.BoundingBox;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Voxel extends Surface implements HasBoundingBox {
	
	/**
	 * TODO
	 */
	protected double[] _dimensions;

	/**
	 * TODO
	 */
	protected double[] _lower;
    
    public Voxel(double[] lower, double[] dimensions)
    {
    	this._lower = lower;
    	this._dimensions = dimensions;
    }
	
	public Voxel(LinkedList<double[]> points)
	{
		//TODO
	}

	public Type type() {
		return Surface.Type.VOXEL;
	}

	@Override
	public int dimensions() 
	{
		return this._dimensions.length;
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
		return boundingBox.get(_dimensions,_lower);
	}
}