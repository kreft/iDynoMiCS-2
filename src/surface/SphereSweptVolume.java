package surface;


import java.util.List;

import agent.body.Point;

public class SphereSweptVolume extends Surface{
	
    public List<Point> _points;
	
    /**
     * Rest length of internal springs connecting the points.
     */
    public double[] _lengths;
	
	/**
	 * Rest angles of torsion springs 
	 */
    public double[] _angles;
	
	/**
	 * 
	 */
    public double _radius;

	
	public Type type() {
		if(_points.size() == 1)
			return Surface.Type.SPHERE;
		else if(_points.size() == 2)
			return Surface.Type.ROD;
		else
			return Surface.Type.STRAND;
	}

}
