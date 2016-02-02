/**
 * 
 */
package shape;

/**
 * @author cleggrj
 *
 */
public final class ShapeConventions
{
	/**
	 * 
	 * 
	 */
	public enum DimName
	{
		X(BoundarySide.XMIN, BoundarySide.XMAX),
		Y(BoundarySide.YMIN, BoundarySide.YMAX),
		Z(BoundarySide.ZMIN, BoundarySide.ZMAX),
		R(BoundarySide.RMIN, BoundarySide.RMAX),
		THETA(BoundarySide.THETAMIN, BoundarySide.THETAMAX),
		PHI(BoundarySide.PHIMIN, BoundarySide.PHIMAX);
		
		public BoundarySide minBndry;
		public BoundarySide maxBndry;
		
		DimName(BoundarySide min, BoundarySide max)
		{
			this.minBndry = min;
			this.maxBndry = max;
		}
	}
	
	/**
	 * 
	 * 
	 */
	public enum BoundarySide
	{
		/*
		 * Cartesian boundaries.
		 */
		XMIN(DimName.X), XMAX(DimName.X),
		YMIN(DimName.Y), YMAX(DimName.Y),
		ZMIN(DimName.Z), ZMAX(DimName.Z),
		/*
		 * Polar boundaries (cylinder uses zmin & zmax instead of phimin &
		 * phimax).
		 */
		RMIN(DimName.R), RMAX(DimName.R),
		THETAMIN(DimName.THETA), THETAMAX(DimName.THETA),
		PHIMIN(DimName.PHI), PHIMAX(DimName.PHI),
		/*
		 * Internal boundary (e.g. a membrane).
		 */
		INTERNAL(null),
		/*
		 * Non-spatial connection between this shape and another (e.g. inflow or
		 * outflow to/from a chemostat compartment).
		 */
		CONNECTION(null);
		
		public DimName dim;
		
		BoundarySide(DimName d)
		{
			this.dim = d;
		}
		
		public static boolean isSideBoundary(BoundarySide aSide)
		{
			return !(aSide == INTERNAL || aSide == CONNECTION);
		}
	}
}
