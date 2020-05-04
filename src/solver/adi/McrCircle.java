

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
/** Extensions to Mcr for dealing with circles by Mcr.
*  WARNING: these are NOT THE MCR OF THE CIRCLE BUT OF
*  THE SQUARE WHOSE AREA IS EQUAL TO THE CIRCLE'S AREA.
*  In other words, these squares are SMALLER than the Mcr of
*  circles. The circle doesn't inscribe the square, unless
*  you use the methods that say "inscribed".
*/

package solver.adi;

public class McrCircle extends Mcr {
// Used in constructing circles' equivalent area squares. This is a factor
// on the -radius- not the width of the resulting square (=.8662). The "radius"
// of a square being half its width....
public static final double SqrtPiOver2 = Math.sqrt(Math.PI) / 2.0 ;

public McrCircle() {super();}

/** Sets this mcr as per circle coords passed in.  Orig mcr ignored. */
public void circleToSquare(Circleable critter) {
  circleToSquare(critter.getX(), critter.getY(), critter.getRadius());
}
/** Sets this mcr as per circle coords passed in.  Orig mcr ignored. */
public void circleToSquare(double x, double y, double r) {
  double equivradius = SqrtPiOver2 * r;
  lx = x - equivradius;
  ly = y - equivradius;
  ux = x + equivradius;
  uy = y + equivradius;
}

public double getEquivRadius(double r) {
   return SqrtPiOver2 * r;
}

/** Set this as mcr, not equivalent-area square. */
public void circleToInscribedSquare(double x, double y, double r) {
  lx = x - r;
  ly = y - r;
  ux = x + r;
  uy = y + r;
}

public double crossCircle(double x, double y, double r) {
  McrCircle square = new McrCircle();
  McrCircle crossm = new McrCircle();

  ((McrCircle) square).circleToSquare(x, y, r);

  if ( intersectMcr(square, crossm) == MCR_DISJOINT)
    return 0.0;
  else
    return crossm.getArea();
}

/** Return intersecting -areas- of two circles' equiv-area squares.

  * Defense of weird constant "errpercent":
  * If we use the actual equivalent squares here, we'll get our circles
  * aligning to a square grid, which gives the misbehavior that circles
  * are more likely to settle to 8 neighbors than 5 (for instance).  Given
  * that the landscape quads are rectangular, this really pushes the circles
  * onto a square grid.  So here we construct the equivalent square overlap
  * if the squares were rotated into alignment for minimum overlap.  This
  * error won't cancel out on average, though.  So, we use a fudge factor
  * of .015 (that being somewhere between 0 and 2%, on the generous side)
  * to calculate only the intersecting area.  By inspection, this appears
  * to work correctly.
  */
static public double crossCircles(double x1, double y1, double r1,
                           double x2, double  y2, double r2)
{
  // cross squares-rotated-to-min-overlap method
  final double errpercent = .015;

  double equivradius1 = (SqrtPiOver2 + errpercent) * r1;
  double equivradius2 = (SqrtPiOver2 + errpercent) * r2;
  double height;

  double d1 = (x2 - x1);
  double d2 = (y2 - y1);

  double dist = Math.sqrt((d1 * d1) + (d2 * d2));
  double width = equivradius1 + equivradius2 - dist;

  if (width < 0.0) width = 0.0;

  height = equivradius1 < equivradius2 ?
           equivradius1 : equivradius2;

  return (width * 2.0 * height);
}
static public double crossCircles(Circleable a, Circleable b) {
  return crossCircles(a.getX(), a.getY(), a.getRadius(),
                      b.getX(), b.getY(), b.getRadius());
}
}
