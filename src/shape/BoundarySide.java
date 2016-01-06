/**
 * 
 */
package shape;

/**
 * 
 * 
 */
public enum BoundarySide
{
	/*
	 * Cartesian boundaries.
	 */
	XMIN, XMAX, YMIN, YMAX, ZMIN, ZMAX,
	/*
	 * Polar boundaries (cylinder uses zmin & zmax instead of phimin & phimax).
	 */
	RMIN, RMAX, THETAMIN, THETAMAX, PHIMIN, PHIMAX,
	/*
	 * Internal boundary (e.g. a membrane).
	 */
	INTERNAL,
	/*
	 * Non-spatial connection between this shape and another (e.g. inflow or
	 * outflow to/from a chemostat compartment).
	 */
	CONNECTION,
	/*
	 * 
	 */
	UNKNOWN;
	
	public static BoundarySide getSideFor(String side)
	{
		if ( side.equalsIgnoreCase("xmin") )
			return XMIN;
		else if ( side.equalsIgnoreCase("xmax") )
			return XMAX;
		else if ( side.equalsIgnoreCase("ymin") )
			return YMIN;
		else if ( side.equalsIgnoreCase("ymax") )
			return YMAX;
		else if ( side.equalsIgnoreCase("zmin") )
			return ZMIN;
		else if ( side.equalsIgnoreCase("zmax") )
			return ZMAX;
		else if ( side.equalsIgnoreCase("rmin") )
			return RMIN;
		else if ( side.equalsIgnoreCase("rmax") )
			return RMAX;
		else if ( side.equalsIgnoreCase("thetamin") )
			return THETAMIN;
		else if ( side.equalsIgnoreCase("thetamax") )
			return THETAMAX;
		else if ( side.equalsIgnoreCase("phimin") )
			return PHIMIN;
		else if ( side.equalsIgnoreCase("phimax") )
			return PHIMAX;
		else if ( side.equalsIgnoreCase("internal") )
			return INTERNAL;
		else if ( side.equalsIgnoreCase("connection") )
			return CONNECTION;
		else
			return UNKNOWN;
	}
	
	public static boolean isSideBoundary(BoundarySide aSide)
	{
		return ! ( aSide == INTERNAL || aSide == CONNECTION || aSide == UNKNOWN );
	}
}
