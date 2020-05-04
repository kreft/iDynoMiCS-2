
//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator
/**
* 990326, vfb, original Java
* Translated from the Swarm Objective-C Gecko source, version 0.51
*/
/** Mcr = Minimum Containing Rectangle. These thingies are convenient
*  for deciding whether two 2-d areas overlap. Which is the crux of
*  picking.
*/

package solver.adi;

//import courseware.jfcdep.*;

public class Mcr extends Object {
public double lx, ly, ux, uy;
public final static int MCR_DISJOINT    = 0;
public final static int MCR_CONTAINS    = 1;
public final static int MCR_CONTAINEDBY = 2;
public final static int MCR_INTERSECT   = 3;

public Mcr() {
  setNullMcr();
}
/** set to inverted Mcr */
public void setNullMcr(){
  lx = Double.MAX_VALUE;
  ux = Double.MIN_VALUE;
  ly = Double.MAX_VALUE;
  uy = Double.MIN_VALUE;
}

public boolean isNullMcr() {
  if (lx == Double.MAX_VALUE)
    if (ux == Double.MIN_VALUE)
      if (ly == Double.MAX_VALUE)
        if (uy == Double.MIN_VALUE)
          return true;
  return false;
}
public void setMcr(Mcr m) {
  lx = m.lx;
  ly = m.ly;
  ux = m.ux;
  uy = m.uy;
}
/** Assume the values are right, but put in lower-upper order */
public void orderMcr() {
  double swap;
  if (lx > ux) {
    swap = lx;
    lx = ux;
    ux = swap;
  }
  if (ly > uy) {
    swap = ly;
    ly = uy;
    uy = swap;
  }
}
public void setMcr(double lx, double ly, double ux, double uy) {
  this.lx = lx;
  this.ly = ly;
  this.ux = ux;
  this.uy = uy;
}

public boolean equals(Mcr m) {
  return (this.lx == m.lx)
    && (this.ly == m.ly)
    && (this.ux == m.ux)
    && (this.uy == m.uy);
}

/** Most other routines use the name "setFrom".*/
public void setFrom(Mcr m) {setMcr(m);}

/** set to Mcr of this points list */
public void setMcr(double[] x, double[] y) {
  int npts = x.length;
  if (y.length < x.length) npts = y.length;
  setNullMcr();
  for (int i=0; i<npts; i++) {
    growMcr(x[i], y[i]);
  }
}

/** Move this mcr into the container mcr without changing its size. If too
 *  big to fit, it's the lowerleft corner that's coincident, upperright that
 *  exceeds "container".
 */
public void snapInto(Mcr container) {
  double delta;
  if (container.ux < ux) {
    delta = ux - container.ux;
    ux -= delta;
    lx -= delta;
  }
  if (container.uy < uy) {
    delta = uy - container.uy;
    uy -= delta;
    ly -= delta;
  }
  if (container.lx > lx) {
    delta = container.lx - lx;
    ux += delta;
    lx += delta;
  }
  if (container.ly > ly) {
    delta = container.ly - ly;
    uy += delta;
    ly += delta;
  }
}

public double getCenterX() {return (lx + ux) / 2.0;}
public double getCenterY() {return (ly + uy) / 2.0;}
public double getLenX() { return ux - lx; }
public double getLenY() { return uy - ly; }

/** What, if any, intersection does this mcr have to argument mcr?
 *   Notes:
 *   a. The null mcr is disjoint everything.
 *   b. If two mcr's are coincident, answer is self contains thatone.
 *   c. If the mcr's do not intersect, crossm is possibly inverted.
 * NOTE: must pass an allocated Mcr as "crossm" if you want to see
 * the resulting intersection instead of just what kind it is!
 */
public int intersectMcr(Mcr m, Mcr crossm) {
  // construct the intersection crossm, then decide.
  if (crossm == null)
    crossm = new Mcr();
  crossm.ux = (ux < m.ux) ? ux : m.ux;
  crossm.lx = (lx > m.lx) ? lx : m.lx;
  crossm.uy = (uy < m.uy) ? uy : m.uy;
  crossm.ly = (ly > m.ly) ? ly : m.ly;

  if (crossm.ux <= crossm.lx)
      return(MCR_DISJOINT);

  if (crossm.uy <= crossm.ly)
      return(MCR_DISJOINT);

  if ( (crossm.ux == m.ux) && (crossm.uy == m.uy) &&
       (crossm.lx == m.lx) && (crossm.ly == m.ly) )
       return(MCR_CONTAINS);

  if ( (crossm.ux == ux) && (crossm.uy == uy) &&
       (crossm.lx == lx) && (crossm.ly == ly) )
       return(MCR_CONTAINEDBY);

  return(MCR_INTERSECT);
}

/** Like intersectMcr, but all 3 kinds of intersection = true. Quicker. */
public boolean crossMcr(Mcr m) {
  double cux, clx, cuy, cly;
  
  // construct the intersection, decide quickly as possible
  cux = (ux < m.ux) ? ux : m.ux;
  clx = (lx > m.lx) ? lx : m.lx;
  if (cux <= clx)
      return(false);

  cuy = (uy < m.uy) ? uy : m.uy;
  cly = (ly > m.ly) ? ly : m.ly;
  if (cuy <= cly)
      return(false);
  return(true);
}

/** Find un-intersection, uncrossm, the mcr of self's extent beyond m.
 *  If you want to see the uncrossm, allocate it yourself instead of sending in null.
 * if self coincident with m, answer is containedby
 */
// Jan: Bug fix in unIntersectMcr thanks to Arnout Standaert
public int unIntersectMcr(Mcr m, Mcr uncrossm) {
	boolean[] corners = new boolean[] {false, false, false, false};
	int answer;

	if (uncrossm == null)
		uncrossm = new Mcr();
	Mcr crossm = new Mcr();
	answer = m.intersectMcr(this, crossm);

	// note we swapped order calling intersectMcr, so who contains who backwards
	if (answer == MCR_DISJOINT)
		uncrossm.setMcr(this);
	else if (answer == MCR_CONTAINS) {
		uncrossm.setNullMcr();
		answer = MCR_CONTAINEDBY;
	} else if (answer == MCR_CONTAINEDBY) {
		uncrossm.setMcr(this);
		answer = MCR_CONTAINS;
	}
	// all other cases (MCR_INTERSECT) boil down to a question of whether
	// the crossm has 2 of this's corners.  If so, we can lop off this mcr,
	// else not.
	else {
		if ((crossm.ux == ux) && (crossm.uy == uy))
			corners[0] = true;
		if ((crossm.ux == ux) && (crossm.ly == ly))
			corners[1] = true;
		if ((crossm.lx == lx) && (crossm.ly == ly))
			corners[2] = true;
		if ((crossm.lx == lx) && (crossm.uy == uy))
			corners[3] = true;

		uncrossm.setMcr(this);
		int sumcorners = 0;
		for (int i = 0; i < corners.length; i++) {
			if (corners[i])
				sumcorners = sumcorners+1;
		}

		if (sumcorners == 2) {
			if (corners[0] && corners[1])
				uncrossm.ux = crossm.lx;
			else if (corners[1] && corners[2])
				uncrossm.ly = crossm.uy;
			else if (corners[2] && corners[3])
				uncrossm.lx = crossm.ux;
			else
				uncrossm.uy= crossm.ly;
		}
	}
	return answer;
}

/** answer is "strictly inside" not "inside or on". */
public boolean isInside(double x, double y) {
  if (ux > x)
    if (lx < x)
      if (uy > y)
        if (ly < y)
          return true;
  return false;
}
/** answer is "inside or on". */
public boolean isInsideOrOn(double x, double y) {
  if (ux >= x)
    if (lx <= x)
      if (uy >= y)
        if (ly <= y)
          return true;
  return false;
}

/** Extend this mcr to include argument mcr. */
public void growMcr(Mcr m) {
  ux = (ux > m.ux) ? ux : m.ux;
  lx = (lx < m.lx) ? lx : m.lx;
  uy = (uy > m.uy) ? uy : m.uy;
  ly = (ly < m.ly) ? ly : m.ly;
}

public void growMcr(double x, double y) {
  ux = (ux > x) ? ux : x;
  lx = (lx < x) ? lx : x;
  uy = (uy > y) ? uy : y;
  ly = (ly < y) ? ly : y;
}

/** Height x width, except inverted mcr's have area 0. */
public double getArea() {
  double answer = (ux - lx);
  if (answer <= 0) 
      return(0);

  answer *= (uy - ly);
  if (answer <= 0) 
      return(0);
  return(answer);
}

//public String toString() {
//  return "Mcr[" + TruncVal.truncVal(lx) +","
//    +TruncVal.truncVal(ly) +","
//    +TruncVal.truncVal(ux) +","
//    +TruncVal.truncVal(uy) +"]";
//}
}
