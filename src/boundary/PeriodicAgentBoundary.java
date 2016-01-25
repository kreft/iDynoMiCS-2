package boundary;

import linearAlgebra.Vector;

public class PeriodicAgentBoundary {

	/**
	 * angular or regular periodic boundaries.
	 */
	public boolean _angular = false;
	
	/**
	 * periodic distance in x, y, z, phi or theta direction
	 */
	public double _periodicDistance;
	
	/**
	 * periodic dimension (x, y, z) or (phi, theta)
	 */
	public int periodicDimension;
	
	  /**
	   * helper method that returns the upper search window in dimension (dim)
	   * 
	   * @param coord
	   * 		  the corner of the original search rectangle that is the lower 
	   * 		  bound of every dimension (eg. the top-left corner)
	   * @return A double array that represents the upper search window in dimension (dim).
	   */
	public double[] adddim(double[] coord) {
		double[] c = Vector.copy(coord);
		c[periodicDimension] = coord[periodicDimension] + _periodicDistance;
		return c;
	}
	  
	  /**
	   * helper method that returns the lower search window in dimension (dim)
	   * 
	   * @param coord
	   * 		  the corner of the original search rectangle that is the lower 
	   * 		  bound of every dimension (eg. the top-left corner)
	   * @return A double array that represents the lower search window in dimension (dim).
	   */
	public double[] subdim(double[] coord) {
		double[] c = Vector.copy(coord);
		c[periodicDimension] = coord[periodicDimension] - _periodicDistance;
		 return c;
	}
	
	/**
	 * returns in Frame locations if coord is out of the domain frame
	 * @param coord
	 * @return
	 */
	public double[] inFrameLocation(double[] coord) 
	{
		if(coord[periodicDimension] < 0.0)
			coord[periodicDimension] += _periodicDistance;
		if(coord[periodicDimension] > _periodicDistance)
			coord[periodicDimension] -= _periodicDistance;
		return coord;
	}
	
	/**
	 * returns shortest distance between two points (trough periodic boundary)
	 */
	public double periodicDistance(double distance)
	{
		if ( Math.abs(distance) > (0.5 * _periodicDistance) )
			distance -= Math.signum(distance) * _periodicDistance;
		return distance;
	}
}
