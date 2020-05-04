
//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator
/**
* 991026, vfb, original Java
* Minimum containing brick, Jan Kreft's idea, but written to support
* Gecko Woods (didn't seem needed in Bacsim.) McBrick subclasses McrCircle
* for convenience, since I'm normally intersecting cylinders. Ish. Am
* providing a lot of utilities I'm not using yet, so should be considered
* untested.
*/

package solver.adi;
//import courseware.jfcdep.*;

public class McBrick extends McrCircle {
// Used in constructing sphere's equivalent volume bricks.
public static final double CubertPiOver6 = Math.pow(Math.PI / 6.0, 1.0/3.0) ;
public double lz, uz;

public McBrick(Mcr m, double lowerz, double upperz) {
  super();
  setFrom(m);
  lz = lowerz;
  uz = upperz;
}
public McBrick(double cylx, double cyly, double cylr,
               double lowerz, double upperz)
{
  super();
  circleToSquare(cylx, cyly, cylr);
  lz = lowerz;
  uz = upperz;
}
public McBrick(double lx, double ly, double lz,
               double ux, double uy, double uz)
{
  super();
  setMcr(lx, ly, ux, uy);
  this.lz = lz;
  this.uz = uz;
}
/** gives uninitialized (inverted) uxyz, lxyz */
public McBrick() {
  super();
  // note, using 0 doesn't work (then landscape won't intersect items
  // in the z 10 to 12 range, etc.)
  lz = Double.MIN_VALUE;
  uz = Double.MAX_VALUE;
  /* inverted, ala Mcr...probably not a good idea.
  lz = Double.MAX_VALUE;
  uz = Double.MIN_VALUE;
  */
}
/** gives brick of lz=uz=0 */
public McBrick(Mcr m) {
  super();
  setFrom(m);
  lz = 0;
  uz = 0;
}
public void setMcb(double lx, double ly, double lz, double ux, double uy, double uz) {
  super.setMcr(lx, ly, ux, uz);
  this.lz = lz;
  this.uz = uz;
}

/** Extend this mcr to include argument mcr. */
public void growMcr(McBrick m) {
  super.growMcr(m);
  if (uz == Double.MAX_VALUE) uz = m.uz;
  else if (m.uz != Double.MAX_VALUE) uz = (uz > m.uz) ? uz : m.uz;

  if (lz == Double.MIN_VALUE) lz = m.lz;
  else if (m.lz != Double.MIN_VALUE) lz = (lz < m.lz) ? lz : m.lz;
}

public void growMcr(double x, double y, double z) {
  super.growMcr(x, y);
  if (uz == Double.MAX_VALUE) uz = z;
  else uz = (uz > z) ? uz : z;
  if (lz == Double.MIN_VALUE) lz = z;
  else lz = (lz < z) ? lz : z;
}
public double getCenterZ() {
  if (uz == Double.MAX_VALUE)
    return 0;
  if (lz == Double.MAX_VALUE)
    return 0;
  return (lz + uz) / 2.0;
}
public double getLenZ() { return uz - lz;}

/** Like intersectMcb, but all 3 kinds of intersection = true. Quicker. */
public boolean crossMcr(McBrick m) {
  if (super.crossMcr(m)) {
    double cuz, clz;//intersection
    // construct the intersection, decide quickly as possible
    cuz = (uz < m.uz) ? uz : m.uz;
    clz = (lz > m.lz) ? lz : m.lz;
    if (cuz <= clz)
        return(false);
    return(true);

  } else
    return false;
}

/** What, if any, intersection does this mcBrick have to argument mcBrick?
 *   Notes:
 *   a. The null mcr is disjoint everything.
 *   b. If two mcr's are coincident, answer is self contains thatone.
 *   c. If the mcr's do not intersect, crossm is possibly inverted.
 * NOTE: must pass an allocated McBrick as "crossm" if you want to see
 * the resulting intersection instead of just what kind it is!
 */
public int intersectMcr(McBrick m, McBrick crossm) {
  // construct the intersection crossm, then decide.
  if (crossm == null)
    crossm = new McBrick();
  if (this.isNullMcb())
    return MCR_DISJOINT;
  if (m.isNullMcb())
    return MCR_DISJOINT;
  int mcrAnswer = super.intersectMcr(m, crossm);
  crossm.uz = (uz < m.uz) ? uz : m.uz;
  crossm.lz = (lz > m.lz) ? lz : m.lz;

  if (mcrAnswer == MCR_DISJOINT)
    return MCR_DISJOINT;

  if (crossm.uz <= crossm.lz)
    return MCR_DISJOINT;

  if (mcrAnswer == MCR_CONTAINS) {
  	// then answer is contains still only if ...
  	//AS: bugfix: second condition now treats lz instead of repeating the same condition
  	if ((crossm.uz == m.uz) && (crossm.lz == m.lz))
  		return(MCR_CONTAINS);
  }

  if (mcrAnswer == MCR_CONTAINEDBY) {
  	//AS: bugfix: second condition now treats lz instead of repeating the same condition
  	if ((crossm.uz == uz) && (crossm.lz == lz))
  		return(MCR_CONTAINEDBY);
  }

  return(MCR_INTERSECT);
}

/** answer is "strictly inside" not "inside or on". */
public boolean isInside(double x, double y, double z) {
  if (isInside(x, y))
    if (uz > z)
      if (lz < z)
        return true;
  return false;
}

/** answer is "inside or on". */
public boolean isInsideOrOn(double x, double y, double z) {
  if (isInsideOrOn(x, y))
    if (uz >= z)
      if (lz <= z)
        return true;
  return false;
}

public boolean isNullMcb() {
  if (isNullMcr())
    return true;
  if (lz == Double.MIN_VALUE)
    return true;
  if (uz == Double.MAX_VALUE)
    return true;
  return false;
}
/** set to inverted Mcb */
public void setNullMcb(){
  setNullMcr();
  lz = Double.MIN_VALUE;
  uz = Double.MAX_VALUE;
}

/** Calculated as a brick. If brick created with a circle, then that's
 *  the area of the base, and thus you get the volume of a cylinder.
 */
public double getVolume() {
  double answer = getArea();
  answer *= (uz - lz);
  return answer;
}

/** Pretend it's a cone. If base r's don't match, take average of them.
 *  The base is in x and y, the point is uz. Logically, you can't very
 *  well use crossCylinder and coneVolume together...pick one.
 */
public double getConeVolume() {
  double r1 = 0.5 * (ux - lx);
  double r2 = 0.5 * (uy - ly);
  double r = 0.5 * (r1 + r2);
  double h = uz - lz;
  return (1.0/3.0) * Math.PI * r * r * h;
}

/** return volume of intersection, or zero.*/
public double crossCylinder(double cylx, double cyly, double cylr,
    double cyllz, double cyluz)
{
  McBrick cylbrick = new McBrick(cylx, cyly, cylr, cyllz, cyluz);
  McBrick crossm = new McBrick();
  if ( intersectMcr(cylbrick, crossm) == MCR_DISJOINT)
    return 0.0;
  else 
    return crossm.getVolume();
}

/** The resulting cube is the mcr returned as self.  Orig mcr ignored.
 *  This is the 3d equivalent of circleToSquare, giving a cuble of
 *  volume equal to that of the sphere.
 */
public void sphereToBrick(double x, double y, double z, double r) {
  double equivradius = CubertPiOver6 * r;
  lx = x - equivradius;
  ly = y - equivradius;
  lz = z - equivradius;
  ux = x + equivradius;
  uy = y + equivradius;
  uz = z + equivradius;
}

//public String toString() {
//  return "McBrick[lx=" + TruncVal.truncVal(lx,5)
//  + ",ux=" + TruncVal.truncVal(ux,5)
//   + ",ly=" + TruncVal.truncVal(ly,5)
//   + ",uy=" + TruncVal.truncVal(uy,5)
//   + ",lz=" + TruncVal.truncVal(lz,5)
//   + ",uz=" + TruncVal.truncVal(uz,5);
//}

}
