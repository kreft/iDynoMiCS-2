package boundary;

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
}
