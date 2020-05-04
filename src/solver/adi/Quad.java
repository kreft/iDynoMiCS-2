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
/** Quad's role is to be a 2-d sorting tree. That which can be quadded
*  is something with 2-d extent. Like a rectangle or circle. All those
*  quaddable things have mcr's, minimum containing rectangles.
*
*  The basic algorithm is to have a slew of things. If more than N, choose
*  a pivot and sort the things into 2 (or 4) subtrees by area. Now find the
*  areas and counts of each of the subtrees. Recursively, if more than N in
*  this subtree...
*
*  A Quad's parent Quad is larger or equal in size to self. All other Quads
*  directly descended from that parent (one recursive subquadding) are the
*  siblings of this Quad. Children are subquads of me.
*
*  This all forms a sideways tree:
*  parentQuad -> otherQuad -> ...
*     |
*     v
*    thisQuad -> siblingQuad -> ...
*     |
*     v
*    childQuad -> siblingQuad -> ...
*/

package solver.adi;
import java.util.Vector;

public class Quad extends McBrick implements Steppable {
// type has public access for speed of QuadDisplay only
public int	type;
private boolean visible;
private boolean dead = false; // only remove sets true
private Quad	parent;
// these are public for QuadDisplay speed
public Quad	sibling;
public Quad	child;

// This stuff doesn't really belong in Quad in a redesign...but let's
// translate first and tidy later.
// Possible values for "type" field.
//GINGER:  do all this with Interfaces instead....?
public final static int QUAD_INVALID = 0;
public final static int QUAD_AQUAD = 1;
public final static int QUAD_CRITTER = 2;
public final static int QUAD_LANDSCAPE = 3;
public final static int QUAD_FEED = 4;
public final static int QUAD_DIFFUSE = 5;
public final static int QUAD_SESSILE = 6;
public final static int QUAD_MOBILE = 7;
public final static int QUAD_NEWBIES = 8;
public final static int QUAD_DRAWMYSELF = 9;

private final String[] typeStrings =
  { "Invalid", "Sorting", "Critter", "Landscape",
    "Feed", "Diffuse", "Sessile", "Mobile", "Newbies", "DrawMyself" };

// this number is a rather platform- and application- specific tradeoff between
// the cost of recursion and the cost of checking N (maxPerQuad) quads and the
// overhead of having more quads to check. Last tuned for Gecko Woods ~10,000 trees,
// busily spawning saplings (first 100 steps, pop 90 -> 11,000.)
// vfb, 991104.
// vfb, 010310: no longer actually a max, since if sortquads are created with
// only one or two members, they're now flattened back out again, so can end up
// with 10 or maybe more in one quad if they're really evenly distributed.
// Became really important with 3d sort, and 8 subquads... Started to up the
// maxPerQuad to 15, but really, not what I wanted.... 7-ish in each sibling
// collection is probably for the best.
public static int maxPerQuad = 7;

private static boolean sort3D = false;
private static McrCircle scratchwin = new McrCircle();

public Quad() {
  super();
  type 	  = QUAD_AQUAD;
  visible = true;
  dead    = false;
  sibling = child = parent = null;
  setNullMcr();
}
public static void setMaxPerQuad(int n) {maxPerQuad=n;}
public static boolean isAgent(int type) {
  return ( type == QUAD_CRITTER) || ( type == QUAD_DRAWMYSELF) ;
}
public static boolean isCollector(int type) {
  return (type == QUAD_AQUAD) || ( type == QUAD_SESSILE)
   || (type == QUAD_MOBILE) || (type == QUAD_NEWBIES) || (type == QUAD_LANDSCAPE);
}
public static boolean isPickable(int type) {
  return (type==QUAD_CRITTER) || (type==QUAD_FEED) || (type==QUAD_DIFFUSE)
         || (type==QUAD_DRAWMYSELF);
}
/** These are low-level, don't propagate mcr's or correct backlinks.*/
public void setParent(Quad q) { parent = q; }

public void setChild(Quad q)  { child = q; }

public void setSibling(Quad q) { sibling = q; }

public Quad getParent() {return parent;}

public Quad getChild() {return child;}

public Quad getSibling() {return sibling;}

public int getType() {return type;}

public void setType(int t) {type = t;}


/** here setFrom sets only the mcr, not the links. */

public void setFrom(Quad q) {super.setFrom((Mcr) q);}

// public void setData(Object d, int quad_type) { data=d; type=quad_type;}

public void setVisible(boolean b) {visible=b;}

public boolean getVisible(){return visible;}

static public void setSort3D(boolean b) {sort3D=b;}

static public boolean isSort3D(){return sort3D;}



//public void paint(QuadDisplay2D qd) {
//
//}


/** if not an agent, fake radius Globalsd on mcr. */

public double getRadius() {

  double fake;
  double fakey;
  fake = ux - lx;
  fakey = uy - ly;
  if (fakey > fake)
    fake = fakey;
  fake /= 2.0;
  return fake;
}

/** High-level, add node as first child of this parent. */
public void addChild(Quad q) {
  q.sibling   = child;
  q.parent    = this;
  child = q;
  q.growMcr();
}

/** Find low sorting quad this guy fits inside. Default is this. */
private Quad findFitQuad(Quad newbie) {
  if (child == null)
    return this;
  Quad thislevel = child;
  Quad answer = null;
  do {
    // never answer with an agent
    if (!isAgent(thislevel.type)) {
      if (thislevel.intersectMcr(newbie, null) == Mcr.MCR_CONTAINS) {
        return thislevel.findFitQuad(newbie);
      }
    }
  } while ((thislevel = thislevel.sibling) != null);
  return this;
}
/** High-level, push newbie down into quadtree rooted by this quad.
 *  Side effect: newbies would get a chance to step on the round they're
 *  born if not deliberately checked in <agent>.step for age (but must
 *  step agents born on day 0 on step 0.) Tried doing multi-stepping in
 *  Woods model--a LOT of side effects if do that....
 */
public void addNewbie(Quad q) {
  Quad addto  = this.findFitQuad(q);
  q.sibling   = addto.child;
  q.parent    = addto;
  addto.child = q;
  q.growMcr();
}
/** High-level, add node as first child of this parent's newbie collector.
 *  Newbies are sorted every step instead of occassionally. This keeps
 *  speeds reasonable between big requads on plant systems with lots of
 *  offspring.
 */
/* version to add newbie to newbies collector
public void addNewbie(Quad q) {
  Quad addto  = this.findNewbies();
  q.sibling   = addto.child;
  q.parent    = addto;
  addto.child = q;
  q.growMcr();
}
*/

/** Find/create a newbies collector. Called with a root of some sort,
 *  like sessile or mobile root.
 */
public Quad findNewbies() {
  Quad childlist = child;
  while (childlist != null) {
    if (childlist.type == QUAD_NEWBIES)
      return childlist;
    childlist = childlist.sibling;
  }
  // found no newbies, so make one
  Quad newbies = new Quad();
  newbies.type = QUAD_NEWBIES;
  newbies.visible = true;
  addChild(newbies);
  return newbies;
}

private void garbled(String msg) {
//  System.err.println("***ERROR--garbled tree at time " + Globals.getCurrentTime()
//                     + " : " + msg);
//  System.err.println("   this    = " + Globals.pointerString(this) + " = " + this);
//  System.err.println("   parent  = " + Globals.pointerString(parent) + " = " + parent);
//  System.err.println("   child   = " + Globals.pointerString(child));
//  System.err.println("   sibling = " + Globals.pointerString(sibling));
}

/** Only this quad tree node.  Correct parent, siblings, children.
 *  Lots of error checking.  If self has substructure, it squashes up
 *  to the front of self's prior sibling chain. Doesn't actually dispose of
 *  anything, just unplugs this Quad from tree, patching all links over.
 */
public void remove() {
  Quad sis;
  Quad kid;

  // fix the kids' parent
  if (child != null) {
    kid = child;
    while (kid.sibling != null) {
      kid.parent = parent;
      kid = kid.sibling;
    }
    kid.parent = parent;
    kid.sibling = sibling;
  }

  // now, "child" is head of list, "kid" is end of list and its sibling set
  // or, if we had no children, child is null and kid undefined

  if (parent == null)
    return; // already off-tree
  sis = parent.child;

  if (child != null) {
    if (sis == this) {
      parent.child = child;
    } else {
      while ((sis != null) && (sis.sibling != this))
        sis = sis.sibling;
      if (sis==null)
        garbled(" in Quad.remove (with child)");
      else
        sis.sibling = child;
    }
  } else {
    if (sis == this) {
      parent.child = sibling;
    } else {
      while ((sis != null) && (sis.sibling != this))
        sis = sis.sibling;
      if (sis == null)
        garbled(" in Quad.remove (without child)");
      else
        sis.sibling = sibling;
    }
  }
  dead    = true;
  parent  = null;
  child   = null;
  sibling = null;
}

/** doesn't delete the Quad it's called with, only its children */
public void deleteBelow() {
  Quad delthis;
  Quad delnext;

  if (child != null) {
    delthis = child;
    child = null;
    while (delthis != null) {
      delnext = delthis.sibling;
      if (delthis != null)
        delthis.deleteBelow();
//      if (delthis != null)
//        delthis.deleteQuick();
      delthis = delnext;
    }
  }
}

/** grows mcr's above this node, as needed */
public void growMcr() {
  Quad thisone;

  if (parent == null)
      return;

  // if current mcr contains new mcr, get out.
  if (parent.intersectMcr(this, null) == MCR_CONTAINS)
    return;

  // otherwise, have work to do.
  thisone = parent;
  if (thisone != null) do {
    thisone.growMcr(this);
    thisone = thisone.parent;
  } while ( (thisone != null) &&
            (thisone.intersectMcr(this, null) != MCR_CONTAINS) );
}

/** This only picks leaves. Does 3d pick if pickwin's instanceof McBrick. */
public Quad pickWin(Mcr pickwin, Quad rootPick, boolean pickinvis) {
//System.err.println("pickWin begins, pickwin=" + pickwin + ",pickinvis=" + pickinvis);
//System.err.println("                root=" + rootPick);
//System.err.println("                this=" + this);

  Quad lastPick = this;

  /** if using 3d sorting ONLY, AND pickwin is really McBrick,
   *  then use pickBrick instead of pickwin. Else, use the Mcr.
   */
  McBrick pickBrick = null;

  if ( sort3D && (pickwin instanceof McBrick) ) {
    pickBrick = (McBrick) pickwin;
  }

  if (lastPick == null)
      return null;

  do {
    // go down?
    if ( (pickBrick != null) && (lastPick.child!=null) && (pickBrick.crossMcr(lastPick)) ) {
      lastPick = lastPick.child;
    }  else if ( (lastPick.child!=null) && (pickwin.crossMcr(lastPick)) ) {
      lastPick = lastPick.child;

    // stop here?
    } else if (lastPick == rootPick) {
      return(null);

    // go across?
    } else if (lastPick.sibling != null) {
      lastPick = lastPick.sibling;

    // pop up?
    } else {
      do {
        lastPick = lastPick.parent;
        if ( (lastPick == rootPick) || (lastPick == null) ) {
          return(null);
        }
      } while (lastPick.sibling == null);
      lastPick = lastPick.sibling;
    }

    // are we there yet?
    if ( isPickable(lastPick.type) ) {
      if ( ( (pickBrick != null) && (pickBrick.crossMcr(lastPick)) ) ||
            pickwin.crossMcr(lastPick) )
      {
        if ( pickinvis || lastPick.visible ) {
          return(lastPick);
        }
      }
    }
  } while (true);
}

/** calls other with invisibles = false */
public Quad pickWin(Mcr pickwin, Quad rootPick) {
  return pickWin(pickwin, rootPick, false);
}

/** Pick leaves, start here, pick things crossing this. */
public Quad pick() {
  return pickWin(this, this, false);
}

/** Pick things within radius of this point, using pickRoot as start. */
public Quad pickRadius(double pickR, double pickX, double pickY, Quad pickRoot) {
  Quad answer;
  Circleable candidate;
  double dist;

  scratchwin.circleToInscribedSquare(pickX, pickY, pickR);
  answer = this;

  while ( (answer =
           answer.pickWin(scratchwin, pickRoot, false) ) != null ) {
    if (answer!=null) {
      candidate = (Circleable) answer;
      if (candidate!=null) {
    	dist = linearAlgebra.Vector.distanceEuclid( new double[] {pickX, pickY}, 
    			new double[] {candidate.getX(), candidate.getY()} );
        dist -= pickR;
        dist -= candidate.getRadius();
        if (dist <= 0) {
          return (Quad) candidate;
        }
      }
    }
  }
  return null;
}

/** recursively subquad children of this quad */
public void subQuad() {
  /* vfb, 010308, need to simplify to do 3d without extra traversals.
   * So encoding binary: 000 = 0 = lz, ly, lx
   *                     001 = 1 = lz, ly, ux
   *                     010 = 2 = lz, uy, lx
   *                     011 = 3 = lz, uy, ux
   *                     100 = 4 = uz, ly, lx
   *                     101 = 5 = uz, ly, ux
   *                     110 = 6 = uz, uy, lx
   *                     111 = 7 = uz, uy, ux
   */
  int count = 0;
  Quad kid,  newparent,  nextkid;

  kid = child;
  if (kid == null)
    return;

this.setNullMcb();

  // count children, calculating mcr.
  do {
    count++;
    growMcr(kid);
  } while ((kid = kid.sibling) != null);

  // do we have few enough members of the quad to quit?
  if (count <= maxPerQuad)
    return;

  // drat, there's work to do.
  // Allocate the subquads and thread together.
  double pivotx, pivoty, pivotz;
  double kidx, kidy, kidz;

  int numQuads;
  if (sort3D)
    numQuads = 8;
  else
    numQuads = 4;
  // note we have 8 here instead of numQuads to simplify coding below, else
  // we'd have to case out sort3d from non-sort3d when testing qc contents.
  int[] qc = new int[8]; // quad count--count in each sub
  Quad[] subquad = new Quad[numQuads];

  int kidquad; // encoding for which quad kid lands in

  for (int i=0;i<numQuads;i++) {
    subquad[i] = new Quad();
    qc[i] = 0;
  }
  for (int i=0;i<numQuads-1;i++) {
    subquad[i].parent  = this;
    subquad[i].sibling = subquad[i+1];
  }
  subquad[numQuads-1].parent  = this;
  subquad[numQuads-1].sibling = null;

  // divvy up the kids into grandkids.
  pivotx=getCenterX();
  pivoty=getCenterY();
  pivotz=getCenterZ();

  kid = child;

  if (kid != null) {
    do {
      kidquad = 0; // because coding is additive...

      kidx = kid.getCenterX();
      kidy = kid.getCenterY();
      kidz = kid.getCenterZ();

      // If a kid is coincident on the pivot, we have to choose whether
      // to go above or below.  If we always go below (for instance), then
      // it's possible (though hopefully uncommon) to get into an infinite
      // recursion due to >MaxPerQuad coincident leaves. Hence the == clause.

      if (kidx > pivotx) {
        kidquad += 1;
      } else if (kidx == pivotx) {
        if ( (qc[1] + qc[3] + qc[5] + qc[7]) <
             (qc[0] + qc[2] + qc[4] + qc[6]) )
             kidquad += 1;
      }

      if (kidy > pivoty) {
        kidquad += 2;
      } else if (kidy == pivoty) {
        if ( (qc[2] + qc[3] + qc[6] + qc[7]) <
             (qc[0] + qc[1] + qc[4] + qc[5]) )
             kidquad += 2;
      }

      if (sort3D) {
        if (kidz > pivotz) {
          kidquad += 4;
        } else if (kidz == pivotz){
          if ( (qc[4] + qc[5] + qc[6] + qc[7]) <
               (qc[0] + qc[1] + qc[2] + qc[3]) )
               kidquad += 4;
        }
      }
      qc[kidquad]++;
      newparent = subquad[kidquad];
      nextkid = kid.sibling;
      kid.parent=newparent;
      kid.sibling=newparent.child;
      newparent.child=kid;
    } while ((kid = nextkid) != null);
  }

  // forget my ex-kids, replace with subquad list
  child = subquad[0];

//String buffy = "";
//for (int i=0; i<numQuads; i++) buffy += qc[i] + " ";
//System.err.println(" subquad  counts, total = " + count + " : " + buffy + " over " + this);

  // And subquad.  Note that some subQuads may nix themselves.
  // Must do it here rather on sub-recursion, lest we delete our root.
  // vfb, 010310: changed to squash out 1- and 2- as well as 0- child sortquads
  Quad nextsib, firstkid;
  for (int i=numQuads-1; i>=0; i--) {
    if ((qc[i] > 2) && (qc[i] != count))
      subquad[i].subQuad();

    // remove 0-, 1-, and 2-child collectors
    else if (qc[i] < 3) {
      nextsib = subquad[i].sibling;
      if (qc[i] > 0) {
        nextkid = subquad[i].child;
        firstkid = subquad[i].child;
        while (nextkid != null) {
          nextkid.parent = this;
          if (nextkid.sibling == null) {
            nextkid.sibling = nextsib;
            nextkid = null;
          } else {
            nextkid = nextkid.sibling;
          }
        }
        if (i > 0)
          subquad[i-1].sibling = firstkid;
        else
          this.child = firstkid;
      } else if (i > 0) {
        subquad[i-1].sibling = subquad[i].sibling;
      } else {
        this.child = subquad[i].sibling;
      }
      subquad[i].dead    = true;
      subquad[i].parent  = null;
      subquad[i].child   = null;
      subquad[i].sibling = null;
    }
  }
}

/** flatten subtree into simple child list */
public void squash() {
  Quad thisone,  nextone,  list;

  thisone = pickWin(this, this, true);
  list = null;
  while (thisone!=null) {
    nextone = thisone.pickWin(this, this, true);
    thisone.remove();
    thisone.parent = this;
    thisone.sibling = list;
    thisone.dead = false; // remove assumes true
    list    = thisone;
    thisone = nextone;
  }
  // removed, 991104, vfb--checked to see if ran faster leaving this to
  // the garbage collector. Did.
  // deleteBelow();
  child = list;
}

/** go beneath collectors and squash/subQuad */
public void rootQuad() {
  Quad sib = child;
  setNullMcb();
  while (sib != null) {
    sib.squash();
    sib.subQuad();
/*
if (sib.type==QUAD_SESSILE) {
Vector species = Species.getAllSpecies();
double allpop = 0;
for (int i=0; i<species.size(); i++) {
allpop += ((Species) species.elementAt(i)).getPopulation();
}
System.err.println("rootQuad after subquad at t=" + Globals.getCurrentTime()
+ ", pop=" + allpop);
sib.printTreeCounts();
}
*/
    sib.growMcr();
    sib = sib.sibling;
  }
/*
long now = Globals.getCurrentTime();
System.err.println("time " + now + " after rootQuad ----------------------");
this.printTree();
*/
}

private static int quads;
private static int leaves;

/** Debug. */
public void printTree() {
  quads = 1;
  leaves = 0;
  System.err.println(this.toString());
  if (child != null)
    child.printTree(2);
//  Globals.log("Tree contains " + quads + " sorting quads and " + leaves + " others.");
}

/** Only print stats, not the tree guts */
public void printTreeCounts() {
  quads = 1;
  leaves = 0;
  if (child != null)
    child.printTreeCountsChild();
//  Globals.log("Tree contains " + quads + " sorting quads and " + leaves + " others.");
}

/** Just traverse the tree and count it--debug routine.*/
public void printTreeCountsChild() {
  // These trees can be vast--don't do both directions recursively....
  int i;
  Quad thislevel = this;

  do {
    if (isCollector(thislevel.type)) quads+=1; else leaves+=1;
    if (thislevel.child != null)
      thislevel.child.printTreeCountsChild();
  } while ((thislevel = thislevel.sibling) != null);
}


/** Just traverse the tree and print it--debug routine.  The depth
 * parameter controls indentation. */
public void printTree(int depth) {
  // These trees can be vast--don't do both directions recursively....
  int i;
  Quad thislevel = this;
  String buffy = "";
  for (i=0; i<depth; i++) buffy += "-";

  do {
    if (isCollector(thislevel.type)) quads+=1; else leaves+=1;
    System.err.println(buffy + thislevel.toString());
    if (thislevel.child != null)
      thislevel.child.printTree(depth+2);
  } while ((thislevel = thislevel.sibling) != null);
}

private int maxDepthSibs(int depth) {
  int thisdepth;
  Quad thislevel = this;
  int mydepth = depth; // going down

  do {
    if (isPickable(thislevel.type)) leaves++; else quads++;
    thisdepth = thislevel.child.maxDepthSibs(depth+1);
    if (thisdepth > mydepth) mydepth = thisdepth;
  } while ((thislevel = thislevel.sibling) != null);
  return(mydepth);
}

/** Collect some stats about the tree. */
public int maxDepth() {
  leaves = 0; quads = 0;
  int depth = 1;
  if (isPickable(type)) leaves++; else quads++;
  depth = child.maxDepthSibs(depth);
  System.err.println("Tree depth = " + depth + ", quads=" + quads + ", leaves=" + leaves);
  return(depth);
}

/** Need to a. sort and b. flatten out newbies collector. If leave the
 *  sorted newbies under a newbies collector, they'll get resorted each
 *  step until requad round, so want them out from under newbies until
 *  then. Returns Quad to use as next in tree instead of the newbies
 *  collector that is no more.
 */
private Quad sortNewbies() {
  if (child == null)
    return this; // nothing to do here
  // sort
  subQuad();

  Quad childlist = child;
  Quad endnewbies = sibling;
  Quad lastchild = null;
  Quad answer = child;

  // flatten
  // normally, newbies is first child of parent, but be careful...
  // here we're finding who pointed to newbies collector, and
  // replacing that pointer with pointer to child
  if (parent.child == this)
    parent.child = child;
  else {
    Quad seeker = parent.child;
    while ((seeker != null) && (seeker.sibling != this)) {
      seeker = seeker.sibling;
    }
    // one garbled tree....
    if (seeker == null) {
      parent.garbled("Newbies not child of parent in sortNewbies!");
      parent.child = child; // best we can do....
    } else {
      seeker.sibling = child;
    }
  }

  // now correct the children's parent to be newbie's parent
  while (childlist != null) {
    childlist.parent = parent;
    lastchild = childlist;
    childlist = childlist.sibling;
  }
  if (lastchild == null)
    lastchild = child;
  lastchild.sibling = endnewbies;

  // and wipe out all connection fields of the newly retired newbies collector
  parent = null;
  child = null;
  sibling = null;

  return answer;
}

/** vfb, 000119, changed logic - either this or next could die during
 *  the course of the step, thus getting us lost in traversal. Fix it
 *  strongly, so that not only won't step the dead, but will correctly
 *  resume and complete the traversal.
 */
private void substep() {
  Quad next = this;
  Steppable stepper;
  Vector toDoList = new Vector(maxPerQuad*2, maxPerQuad);

  // first fill the toDoList for safe traversal
  do {
    toDoList.addElement(next);
  } while ((next = next.sibling) != null);

  // now traverse the toDoList
  for (int curId = 0; curId < toDoList.size(); curId++) {
    next = (Quad) toDoList.elementAt(curId);
    if (!(next.dead)) {
      if (next.type == QUAD_NEWBIES)
        next = next.sortNewbies();
      if (isAgent(next.type)) {
        stepper = (Steppable) next;
        stepper.step();
      }
      if (next.child != null)
        next.child.substep();
    }
  }
  toDoList.removeAllElements();
}

/** actually substeps the child, recursion launcher */
public void step() {
 if (child != null) child.substep();
}

//public void stepOn(int roundth) { if ( (Globals.getCurrentTime() % roundth) == 0) step();}

private String pointerString(Quad q) {
  return (q==null)? "null" : (new Integer(q.hashCode())).toString();
}

public String toString() {

  // dunno how to print address of Object, so use hashCode as good-enough id

  return "Quad[" + this.getCenterX() + ", " + this.getCenterY() + " " + this.getCenterZ() + " " // Globals.pointerString(this)

    + ",type=" + typeStrings[type]

  // + ",data=" + (data==null?"null":data.toString())

    + ",parent="  + pointerString(parent)

    + ",child="   + pointerString(child)

    + ",sibling=" + pointerString(sibling)

    + ",super=" + super.toString()

    + "]";

}

}



