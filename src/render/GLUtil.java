package render;

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
		if (pos.length == 2)
			return new double[]{ pos[0], pos[1], 0.0 };
		return new double[]{ pos[0], 0.0, 0.0 };
	}

}
