package surface.collision;

import linearAlgebra.Vector;
import surface.Rod;
import surface.Voxel;

/**
 * TODO some work in progress methods not ready for production
 * TODO Copy to Collision to work on them
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class CollisionExcerpts {

	
	
	/**
	 * TODO real-time collsion detection pp 229
	 * @param rod
	 * @param voxel
	 * @param var 
	 * @return
	 */
	private CollisionVariables voxelRod(Rod rod, Voxel voxel, CollisionVariables var)
	{
		// Compute the AABB resulting from expanding b by sphere radius r 
//		AABB e = b; 
		double[] emin = voxel.getLower();
		double[] emax = voxel.getHigher();
		double r = rod.getRadius();
		Voxel e = new Voxel(Vector.minus(emin, r), Vector.add(emax, r));
		// Intersect ray against expanded AABB e. Exit with no intersection if ray 
		// misses e, else get intersection point p and time t as result 
		
		double[] p = new double[emin.length];
		var = RayAABB(rod._points[0].getPosition(), 
				rod._points[1].getPosition(), e, var, p);
		if ( var.distance == Double.MAX_VALUE || var.t > 1.0 ) 
//			return false;
			return var;
		// Compute which min and max faces of b the intersection point p lies 
		// outside of. Note, u and v cannot have the same bits set and 
		// they must have at least one bit set among them 
		int u=0,v=0; 
			if (p[0] < voxel.getLower()[0]) u |= 1; 
			if (p[0] > voxel.getHigher()[0]) v |= 1; 
		if (p.length > 1)
		{
			if (p[1] < voxel.getLower()[1]) u |= 2; 
			if (p[1] > voxel.getHigher()[1]) v |= 2; 
		}
		if (p.length > 2)
		{
			if (p[2] < voxel.getLower()[2]) u |= 4; 
			if (p[2] > voxel.getHigher()[2]) v |= 4;
		}
		// ‘Or’ all set bits together into a bit mask (note: here u+v==u|v) 
		int m = u + v;
		// Define line segment [c, c+d] specified by the sphere movement 
		//Segment seg(s.c, s.c + d);
		// --> Rod
		
		// If all 3 bits set (m == 7) then p is in a vertex region 
		if (m == 7) { 
			// Must now intersect segment [c, c+d] against the capsules of the three 
			// edges meeting at the vertex and return the best time, if one or more hit 
			float tmin = Float.MAX_VALUE; 
//			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
//					Corner(voxel, bool(v ^ 1)), rod._points[0].getPosition(), 
//					rod._points[1].getPosition(), rod.getRadius(), var) ) 
//				tmin = (float) Math.min(var.t, tmin);
//			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
//					Corner(voxel, bool(v ^ 2)), rod._points[0].getPosition(), 
//					rod._points[1].getPosition(), rod.getRadius(), var) ) 
//				tmin = (float) Math.min(var.t, tmin);
//			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
//					Corner(voxel, bool(v ^ 4)), rod._points[0].getPosition(), 
//					rod._points[1].getPosition(), rod.getRadius(), var) ) 
				tmin = (float) Math.min(var.t, tmin);
			if ( tmin == Float.MAX_VALUE ) 
//				return false; // No intersection
				return var;
		var.t = tmin; 
//		return true;
		return var;
		// Intersection at time t == tmin 
		}
		// If only one bit set in m, then p is in a face region 
		if ((m & (m - 1)) == 0) { 
			// Do nothing. Time t from intersection with 
			// expanded box is correct intersection time 
//			return true;
		}
		// p is in an edge region. Intersect against the capsule at the edge 
		return segmentCapsule(Corner(voxel, bool(u ^ 7)), 
				Corner(voxel, bool(v)), rod._points[0].getPosition(), 
				rod._points[1].getPosition(), rod.getRadius(), var);
	}
	

	/**
	 * TODO Real-time collision detection pp 180
	 * 
	 * // Intersect ray R(t)=p+t*d against AABB a. When intersecting, 
	 * // return intersection distance tmin and point q of intersection
	 * 
	 * @param p
	 * @param d
	 * @param a
	 * @param tmin
	 * @param q
	 * @return
	 */
	private CollisionVariables RayAABB( double[] p, double[] o, Voxel a, 
			CollisionVariables var, double[] q)
	{
		double[] d = Vector.minus(o, p);
		// set to -FLT_MAX to get first hit on line
		var.t = 0.0;  
		// set to max distance ray can travel (for segment)
		float tmax = Float.MAX_VALUE; 
		// For all three slabs 
		for(int i=0; i<d.length; i++) 
		{ 
			// Compute intersection t value of ray with near and far plane of slab 
			float ood = 1.0f / (float) d[i]; 
			float t1 = (float) ((a.getLower()[i] - p[i]) * ood); 
			float t2 = (float) ((a.getHigher()[i] - p[i]) * ood); 
			// Make t1 be intersection with near plane, t2 with far plane 
			if (t1 > t2) 
				Swap(t1, t2); 
			// Compute the intersection of slab intersection intervals 
			if (t1 > var.t) 
				var.t = t1; 
			if (t2 > tmax) 
				tmax = t2; 
			// Exit with no collision as soon as slab intersection becomes empty 
			if (var.t > tmax) 
			{
//				var.distance = Double.MAX_VALUE;
//				return var;
			}
		}
		var.t = clamp(var.t);
		// Ray intersects all 3 slabs. Return point (q) and intersection t value (tmin) 
	//		q=p+d* tmin;
			q = Vector.add(Vector.times(d, var.t), p); 
//			var = this.linesegPoint(p, o, q, var);
			return var;

	}
	
	/**
	 * TODO Real-time collision detection pp 197
	 * 
	 * Intersect segment S(t)=sa+t(sb-sa), 0<=t<=1 against cylinder specified by p, q and r
	 * @return
	 */
	private boolean intersectSegmentCylinder(double[] sa, double[] sb, 
			double[] p, double[] q, float r, float t)
	{
		double[] d = Vector.minus(q, p), 
				m = Vector.minus(sa,p), 
				n = Vector.minus(sb,sa); 
		double md = Vector.dotProduct(m, d); 
		double nd = Vector.dotProduct(n, d); 
		double dd = Vector.dotProduct(d, d); 
		// Test if segment fully outside either endcap of cylinder 
		if (md < 0.0f && md + nd < 0.0f) 
			return false; 
		// Segment outside ’p’ side of cylinder 
		if (md > dd && md + nd > dd) 
			return false; 
		// Segment outside ’q’ side of cylinder 
		double nn = Vector.dotProduct(n, n); 
		double mn = Vector.dotProduct(m, n); 
		double a = dd * nn - nd * nd;
		float k = (float) Vector.dotProduct(m, m) - r*r; 
		float c = (float) (dd * k - md * md); 
		if ( Math.abs(a) < Collision.EPSILON) { 
			// Segment runs parallel to cylinder axis 
			if (c > 0.0f) 
				return false;
		// ’a’ and thus the segment lie outside cylinder
		// Now known that segment intersects cylinder; figure out how it intersects 
			if (md < 0.0f)
				t = (float) (-mn/nn);
		// Intersect segment against ’p’ endcap
		else if (md > dd)
			t = (float) ((nd-mn)/nn); 
			// Intersect segment against ’q’ endcap 
		else 
			t = 0.0f;
		// ’a’ lies inside cylinder
		return true;
		}
		
		float b = (float) (dd*mn-nd*md); 
		float discr=(float) (b*b - a*c); 
		if (discr < 0.0f) 
			return false;
		// No real roots; no intersection
		
		t = (float) ((-b - Math.sqrt(discr)) / a); 
		if (t < 0.0f || t > 1.0f) 
			return false;
		// Intersection lies outside segment
		if( md+t*nd< 0.0f) { 
			// Intersection outside cylinder on ’p’ side 
			if (nd <= 0.0f) 
				return false;
		// Segment pointing away from endcap
		t = (float) (-md / nd); 
		// Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2 
		return k+2*t*(mn+t*nn)<= 0.0f;
		} 
		else if (md+t*nd>dd)
		{ 
			// Intersection outside cylinder on ’q’ side 
			if (nd >= 0.0f) return false; 
			// Segment pointing away from endcap 
			t = (float) ((dd - md) / nd); 
			// Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2 
			return k+dd-2*md+t*(2*(mn-nd)+t*nn)<= 0.0f;
		}
		// Segment intersects cylinder between the endcaps; t is correct 
		return true;
	}
	

	/**
	 * TODO Real-time collision detection pp 197
	 * slight adjust of IntersectSegmentCylinder
	 * @return
	 */
	private CollisionVariables segmentCapsule(double[] sa, double[] sb, 
			double[] p, double[] q, double r, CollisionVariables var)
	{
		double[] d = Vector.minus(q, p), 
				m = Vector.minus(sa,p), 
				n = Vector.minus(sb,sa); 
		double md = Vector.dotProduct(m, d); 
		double nd = Vector.dotProduct(n, d); 
		double dd = Vector.dotProduct(d, d); 		
		double nn = Vector.dotProduct(n, n); 
		double mn = Vector.dotProduct(m, n); 
		double a = dd * nn - nd * nd;
		
		float k = (float) ((float) Vector.dotProduct(m, m) - r*r); 
		float c = (float) (dd * k - md * md); 
		if ( Math.abs(a) < Collision.EPSILON) { 
			// Segment runs parallel to cylinder axis 
			if (c > 0.0f) 
			{
				// TODO
//				var.t = 0.5;
			}
		// ’a’ and thus the segment lie outside cylinder
		// Now known that segment intersects cylinder; figure out how it intersects 
			if (md < 0.0f)
				var.t = (float) (-mn/nn);
		// Intersect segment against ’p’ endcap
		else if (md > dd)
			var.t = (float) ((nd-mn)/nn); 
			// Intersect segment against ’q’ endcap 
		else 
			var.t = 0.0f;
		// ’a’ lies inside cylinder
			{
//		return true;
			}
		}
		
		float b = (float) (dd*mn-nd*md); 
		float discr = (float) (b*b - a*c); 
		if (discr < 0.0f) 
		{
//			TODO
//			return false;
		}
		// No real roots; no intersection
		
		var.t = (float) ((-b - Math.sqrt(discr)) / a); 
		if (var.t < 0.0f || var.t > 1.0f) 
		{
			var.t = clamp(var.t);

//			return false;
		}
		// Intersection lies outside segment
		if( md+var.t*nd< 0.0f) { 
			// Intersection outside cylinder on ’p’ side 
			if (nd <= 0.0f) 
			{
				// TODO
//				return false;
			}
		// Segment pointing away from endcap
			var.t = (float) (-md / nd); 
		// Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2 
			// TODO
//		return k+2*var.t*(mn+var.t*nn)<= 0.0f;
		} 
		else if (md+var.t*nd>dd)
		{ 
			// Intersection outside cylinder on ’q’ side 
			if (nd >= 0.0f) 
				{
				// TODO
//				return false; 
				}
			// Segment pointing away from endcap 
			var.t = (float) ((dd - md) / nd); 
			// Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2 
			{
				// TODO
			// return k+dd-2*md+var.t*(2*(mn-nd)+var.t*nn)<= 0.0f;
			}
		}
		// Segment intersects cylinder between the endcaps; t is correct 
		// return true;
		
		return var;
	}
	
	
	
	/* ************************************************************************
	 * placeholder methods
	 */

	private boolean bool(int n)
	{
		return n != 0;
	}
	
	//Support function that returns the AABB vertex with index n 
	private double[] Corner(Voxel b, boolean n) 
	{
		double[] p = new double[b.getHigher().length]; 
		for (int i = 0; i < p.length; i++)
			p[i] = ((n & true) ? b.getHigher()[i] : b.getLower()[i]); 
		return p;
	}
	
	private static double clamp(double a) 
	{
		return Math.max( Math.min(a, 1.0), 0.0 );
	}
	
	private void Swap(float t1, float t2) 
	{
		float temp = t1;
		t1 = t2;
		t2 = temp;
	}
}
