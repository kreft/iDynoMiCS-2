package surface.collision;

import linearAlgebra.Vector;

/**
 * Class passes variables between collision methods
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class CollisionVariables
{
	public CollisionVariables( int nDim, double margin )
	{
		this.interactionVector = Vector.zerosDbl(nDim);
		this.margin = margin;
	}
	
	public void setMargin(double margin )
	{
		this.margin = margin;
	}
	
	/*
	 * Vector that represents the direction and scale between: 
	 * point-point, line-point segment and line segment-line segment.
	 * 
	 * The vector is determined and overwritten by the distance methods
	 * (point-point, line-point, point-plane, etc.) either directly within 
	 * the method for planes or by the called method {@link 
	 * #setPeriodicDistanceVector(double[], double[])} and subsequently used
	 * to give the force vector its direction.
	 * 
	 * Since there are no duplicate methods in opposed order for line-point 
	 * (point-line), plane-point (point-plane) the order of the surface 
	 * input arguments is flipped if the this is required for the method as
	 * a result the direction vector dP also needs to be flipped before the 
	 * force is applied to the mass-points.
	 */
	public double[] interactionVector;
	
	/*
	 * Represents the closest point on the first line segment expressed as a
	 * fraction of the line segment.
	 */
	public double s;
	
	/*
	 * Represents the closest point on the second line segment expressed as 
	 * a fraction of the line segment.
	 */
	public double t;
	
	/*
	 * Flip if the force needs to be applied in the opposite direction to 
	 * the default.
	 * 
	 * <p>This is set in {@link #distance(Surface, Surface)} and used in
	 * {@link #collision(Collection, Collection, double)}.</p>
	 */
	public boolean flip = false;
			
	/*
	 * Internal variable used for passing a distance at with surfaces become
	 * attractive.
	 * 
	 * <p>This is set in {@link #collision(Collection, Collection, double)}
	 * only.</p>
	 */
	public double margin;
	
	/*
	 * calculated distance between two objects.
	 */
	private double distance;

	/*
	 * used to track max overlap
	 */
	private double maxOverlap = 0;
	
	/*
	 * Effective radius (required for Herz model).
	 */
	public double radiusEffective;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
		if( distance < maxOverlap )
			maxOverlap = distance;
	}

	public void resetOverlap() {
		this.maxOverlap = 0.0;
	}

	public double maxOverlap() 	{
		return this.maxOverlap;
	}
}