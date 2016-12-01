package render;

import java.util.List;

import linearAlgebra.Vector;
import shape.Shape;

public class GLUtil {
	/**
	 * Appends a third position value of zero if <b>pos</b> is 2-dimensional. 
	 * If the input is 3-dimensional, the original vector will be returned.
	 * 
	 * @param p - A 2D or 3D position in space.
	 */
	public static double[] make3D(double[] pos){
		if (pos.length == 3)
			return pos;
		return new double[]{ pos[0], pos[1], 0.0 };
	}
	
	public static double[] searchClosestCyclicShadowPoint(Shape shape,
			double[] posA, double[] posB){
		// FIXME think of something more robust
		/*
		 * find the closest distance between the two mass points of the rod
		 * agent and assumes this is the correct length, preventing rods being
		 * stretched out over the entire domain
		 * 
		 * Here we assume that posB will stay fixed, so we are looking for a
		 * candidate position for the "A" end of the cylinder.
		 */
		List<double[]> cyclicPoints = shape.getCyclicPoints(posA);
		double[] c = cyclicPoints.get(0);

		/* distance between the two mass points */
		double dist = Vector.distanceEuclid(posB, c);
		double dDist;
		/* 
		 * find the closest 'shadow' point, use the original point if all
		 * alternative point are further.
		 */
		for ( double[] d : cyclicPoints )
		{
			dDist = Vector.distanceEuclid( posB, d);
			if ( dDist < dist)
			{
				c = d;
				dist = dDist;
			}
		}

		return c;
	}
}
