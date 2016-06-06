package glRender;

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
	
	/**
	 * TODO Stefan: copied (and slightly modified) from 
	 * http://stackoverflow.com/questions/9970281/java-calculating-the-angle-between-two-points-in-degrees
	 * 
	 * Calculates the angle from centerPt to targetPt in degrees.
	 * The return should range from [0,360), rotating CLOCKWISE, 
	 * 0 and 360 degrees represents NORTH,
	 * 90 degrees represents EAST, etc...
	 *
	 * Assumes all points are in the same coordinate space.  If they are not, 
	 * you will need to call SwingUtilities.convertPointToScreen or equivalent 
	 * on all arguments before passing them  to this function.
	 *
	 * @param dx Target point x minus center x.
	 * @param dy Target point y minus center y.  
	 * @return angle in degrees.  This is the angle from centerPt to targetPt.
	 */
	public static double calcRotationAngleInDegrees(double dx, double dy)
	{
	    // calculate the angle theta from the deltaY and deltaX values
	    // (atan2 returns radians values from [-PI,PI])
	    // 0 currently points EAST.  
	    // NOTE: By preserving Y and X param order to atan2,  we are expecting 
	    // a CLOCKWISE angle direction.  
	    double theta = Math.atan2(dy, dx);

	    // rotate the theta angle clockwise by 90 degrees 
	    // (this makes 0 point NORTH)
	    // NOTE: adding to an angle rotates it clockwise.  
	    // subtracting would rotate it counter-clockwise
	    theta += Math.PI/2.0;

	    // convert from radians to degrees
	    // this will give you an angle from [0->270],[-180,0]
	    double angle = Math.toDegrees(theta);

	    // convert to positive range [0-360)
	    // since we want to prevent negative angles, adjust them now.
	    // we can assume that atan2 will not return a negative value
	    // greater than one partial rotation
	    if (angle < 0) {
	        angle += 360;
	    }

	    return angle;
	}
}
