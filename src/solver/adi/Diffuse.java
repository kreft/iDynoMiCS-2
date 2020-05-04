//Title:        BacSim/Gecko
//Version:      Java nitrifying biofilm model with EPS
//Copyright:    Copyright (c) 1997-2005, Jan-Ulrich Kreft, All rights reserved
//Author:       Jan-Ulrich Kreft, kreft@uni-bonn.de
//Company:      University of Bonn
//Author:       Ginger Booth, ginger@gingerbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Bacterial Ecology Simulator (BacSim)
/**
* Diffuse implements the 3-D substrate resource eaten by BacSim agents.
* This is an active landscape, feeding the Bacilli. Substrate nutrients
* diffuse through the volume. This class can account for 95% of BacSim
* run times.
* 990923, vfb, translated from the Swarm Objective-C BacSim source.
* last Obj-C comments:
* Redesign of Diffusion, using ADI diffusion-reaction algorithm
* Only used by bacteria, other critters are fed by the land or eat critters.
* We have lattices for substrates, reactions, biomasses, etc.
* Each cycle, diffusion steps first, then tells Bacilli to move, then Bacilli step
* The printing is done in between to insure synchrony
*/

package solver.adi;

import solver.adi.Quad;
import java.util.Vector;
import dataIO.Log;
import java.awt.*;


public class Diffuse extends ResourceQuad {
	
  
  public Quad mobileroot = new Quad();
  public final static double EPSILON = 2.2204e-16;
	
	
  // some globals
  public static final double pH = 7.0;
  // Used in Diffuse for converting substrate concentrations from ionized to unionized
  public static final double celsius= 30.0; // temperature in degrees celsius

  // values for setGeometry
  public static final int APERIODIC_GEOMETRY  = 0; // aperiodic x,y,z
  public static final int PERIODIC_GEOMETRY   = 1; // periodic x,y,z
  public static final int BIOFILM_GEOMETRY    = 2; // periodic y,z, carrier x
  public static final int MAX_GEOMETRY        = 2; // can add more later if desired...

  // defining properties
  private int numSubs;    // number of substrates
  private int numSpecies; // number of species
  private int stepSize;   // multiplier on time-unit of 1 min (was in GeckoModelSwarm)

  // we need one var per substrate (numSubs)
  private final int numResources = 4;
  private double[]  extConcn; // array of subConcns in the bulk liquid (external), doesn't change, open system
  private double[]  diffC;    // array of diffCs

  private double cellSize;      // only square/cubic grid, size of one grid element
  private int	 cellsX;
  private int	 cellsY;
  private int    boundaryLayer; // the width of the hydrodynamic boundary layer
  private int    carrierLayer;  // it says: "the width of the carrier layer in microns": AN INT?
  private double sliceThickness;// the width of the third dimension (third dimension always periodic)

  private int geometry = BIOFILM_GEOMETRY; // periodic y,z, carrier x
  private int printCycle = 100; // protocol can override--dump printv every 100th now

  private int  numColors;		        // how many colors used to display density
  private double  highColorDensity;	// for density >= this, use high color
  private double  lowColorDensity;	// for density < this, use low color
  // translation note: there's no longer a set number of diffusion colors
  // nor any predefined colors:  no color map
  private static Color nullColor =  new Color(153,51,51);

  // working variables
  private int front;        // front of the biomass
  private int countDiff;    // reset before each cycle
  private int totDiffSteps; // total number of diffusion steps made this run

  // normalized for ADI, we need one var per substrate (numSubs)
  private double[] D;
  private double[] Ar;
  private double[] dt;
  // for conversion
  private double[] max;
  private double[] maxlast;
  // arguments for tridag, reuseable, hence only one set in total
  private double[] aa;
  private double[] bb;
  private double[] cc;
  private double[] rr;
  private double[] uu;
  private int jj;
  private double[] gam; // internal to tridag
  private double[] hh;  // internal to tridagPeriodic
  private double[] vv;  // internal to tridagPeriodic

  private boolean relaxed;      // has substrate field relaxed to the steady state?
  private boolean[] relaxedSub; // for individual substrates
  private boolean stable;       // is the diffusion algorithm stable?

  // matrices
  private double[][][] s;	// substrate concn matrix, one per substrate (numSubs)
  private double[][][] d;   // effective diffusion coefficient matrix, one per substrate (numSubs)
  private double[][][] r;   // reaction rate matrix, one per substrate (numSubs)
  private double[][][] x;   // biomass matrix, one per species (numSpecies)
  private double[][][] g;   // growth rate matrix, one per species (numSpecies)
  private double[][]  xtot; // total biomass matrix, all species and epsMass
  private double[][]  xbac; // total biomass matrix, all species
  private double[][]  xeps; // total epsMass matrix
  private double[][]  por;  // porosity matrix
  private double[][]  tmp;	// temporary buffer matrix for s, only one needed
  private double[][]  delta;// save off differences between s step n and s step n+1

  // these two could be combined but would be a lot of work, I mean a lot
  private byte[][] c;       // carrier = 2, biofilm = 1, all liquid = 0
  private byte[][] cs;     // carrier = 2, (biofilm + boundary layer) = 1, bulk liquid = 0

  private double[] colorDensity;	// density->color lookup map
  private Color[] colors;	        // color id lookup map
  private double cellVol;		      // volume of lattice cell

  private double dtmin;        // smallest timestep of all dt[i] is the one to be used
  private double dtfix;        // fixed timestep in order to make maxDelta/dt... independent of changes in dtmin
  private double maxDelta;     // for conversion criterion
  private long lastDtminChange; // store time step of last change of dtmin as a response to instability
  private boolean haveEps = false; // do we have eps at all?

  static public double KaNH3 = Math.exp(6344/(273+celsius)); // Acidity constant for Ammonia--temperature dependent
  static public double KbHNO2 = Math.exp(-2300/(273+celsius)); // Basicity constant for Nitrite

  // create list of agents on the fly for each step for faster access
  // one list for each type of agents, saves superfluous questions
  private final static int chunk = 100; // allocation chunk for Vectors
  private Vector bac1List  = new Vector(chunk, chunk);
  private Vector bac2List  = new Vector(chunk, chunk);
  private Vector epsList   = new Vector(chunk, chunk);

  private double doledThisStep = 0; // bookkeeping for stats
  // control hooks, were IFDEFS
  private static boolean extraLoco = false;
  private static boolean verbose = false;

  public Diffuse() {
    super();
    init();
    

    mobileroot.setType(Quad.QUAD_MOBILE);
  }

  /** Ginger's routine: getting halt ship, overboard, array index oob, but...all the
   *  coords are wrapped and pass test at end of Bacillus.step and Eps.shove,
   *  so...what the hell?
   *  Decision: it's not just for debugging...let it fix the problem.
   */
//  private void debugWrap(QuadCircleSpecies qritter) {
//    // double savex = qritter.x;
//    // double savey = qritter.y;
//    BacShove.wrapXYZ(qritter, this);
//    /*
//    if ((savex != qritter.x) || (savey != qritter.y)) {
//      System.err.println("Needed a wrap! was " + savex + "," + savey + ", to " + qritter.x
//      + "," + qritter.y + " traceback : !!!");
//      Thread.dumpStack();
//    }
//    */
//  }

  public void init() {
    type		          = QUAD_DIFFUSE;
    resource              = 1; // We don't deal with resources, but need some meaningful value for LandRoot

    numSubs               = 1;
    numSpecies            = 1;

    extConcn = new double[] { 1, 1, 1, 1 };
    diffC    = new double[] { 10000, 10000, 10000, 10000 };

    D                     = null;
    Ar                    = null;
    dt                    = null;
    max                   = null;
    maxlast               = null;

    cellSize              = 1.0;
    cellsX                = 11;
    cellsY                = 11;
    boundaryLayer         = 0;
    carrierLayer          = 0;
    sliceThickness        = 1.0;
    geometry              = APERIODIC_GEOMETRY;
    relaxed               = true;
    relaxedSub            = null;
    stable                = true;
    front                 = 0;
    countDiff             = 0;
    totDiffSteps          = 0;
    numColors             = 10;
    highColorDensity 	= 1.0;
    lowColorDensity 	= 0.00001;
    s                     = null;
    tmp                   = null;
    delta                 = null;
    r                     = null;
    x                     = null;
    g                     = null;
    xtot                  = null;
    xbac                  = null;
    xeps                  = null;
    por                   = null;
    d                     = null;
    c                     = null;
    cs                    = null;
    colorDensity 	        = null;
    colors  		= null;
    cellVol 		= 0.0;
  }
  
  public void assignSolute(int i, double[][] normalizedConcentrationMatrix)
  {
	  s[i] = normalizedConcentrationMatrix;
  }
  
  public double[][] getSolute(int i)
  {
	  return s[i];
  }
  

  private double[][] allocLattice() {
    // lattice[xi][yi]
    double l[][] = new double[cellsX][];
    for (int i=0; i < cellsX; i++)
      l[i] = new double[cellsY];
    return l;
  }

  private byte[][] allocByteLattice() {
    // lattice[xi][yi]
    byte[][] l = new byte[cellsX][];
    for (int i=0; i < cellsX; i++)
      l[i] = new byte[cellsY];
    return l;
  }

  private void fillLattice(double[][] which, double val) {
    for (int i=0; i < cellsX; i++)
      for (int j=0; j < cellsY; j++)
        which[i][j] = val;
  }

  private void fillLattice(byte[][] which, byte val) {
    for (int i=0; i < cellsX; i++)
      for (int j=0; j < cellsY; j++)
        which[i][j] = val;
  }

  private void log(String msg) {
//    Globals.getUI().log(msg);
  }
  private void log(int sigdigits, double d) {
    // this is something C really does a lot better....
//    Globals.getUI().log("" + TruncVal.truncVal(d, sigdigits));
  }
  private void log(String msg, int sigdigits, double d) {
    // this is something C really does a lot better....
//    Globals.getUI().log(msg + TruncVal.truncVal(d, sigdigits));
  }

  private void printLattice(double[][] which) {
    for (int j=0; j < cellsY; j++) {
      for (int i=0; i < cellsX; i++) {
        log(3, which[i][j]);
      }
      log("\n");
    }
  }

  public void printLattice(char[][] which) {
    for (int j=0; j < cellsY; j++) {
      for (int i=0; i < cellsX; i++) {
        log(which[i][j] + " ");
      }
      log("\n");
    }
  }

  private void initColors() {
    int i, colorstep;
    double colDelta;
    double divisor;

    // allocate the space
    colorDensity = new double[numColors];
    colors       = new Color[numColors];

    // JanK's more-blue==higher concentration scheme
    // unless we want them all named, let's NOT give
    // them to QuadDisplay
    for (i = 0; i < numColors; i++) {
      float level = ((float)i) / numColors;
      colors[i] = new Color( level, level, 1.0f);
    }

    // Here is a linear scheme for our normalized substrate field,
    // from 1.4 to 0 since products can be > 1
    colDelta = 1.4 / numColors;
    colorDensity[0] = 1.4 - colDelta;
    for (i = 1; i < numColors - 1; i++)
      colorDensity[i] = colorDensity[i-1] - colDelta;
    colorDensity[numColors - 1] = 0.0;

    // translation : skipped colorIds logic
  }

  // translation: QuadDisplay bails out and calls this for anybody who isn't
  // a Critter or a QuadQuad. Ie., Diffuse controls how Diffuse is painted.
//  public void paint(QuadDisplay2D qd) {
//    // translation: set QuadDisplay's background color if desired.
//    // not our business--we've been told to draw this.
//
//    double lx, ly, ux, uy; // local here!
//    for (int i=0; i < cellsX; i++) {
//      lx = getMcrCellLX(i,0);
//      ux = getMcrCellUX(i,0);
//      for (int j=0; j < cellsY; j++) {
//        ly = getMcrCellLY(i,j);
//        uy = getMcrCellUY(i,j);
//        qd.drawRect(getColorCell(i,j), lx, ly, ux, uy, true);
//      }
//    }
//  }

  public Color getColorCell(int x, int y) {
    if (c[x][y] == (byte)2)
      return getColorDensity(-1.0); // make the carrier a color out of diffuse color range
    else
      return getColorDensity( s[0][x][y] ); // display oxygen for now
  }



  public Color getColorDensity(double den) {

    if (den < 0)
      return nullColor; // some kind of brown

    for (int i=0; i < numColors; i++) {
      if (den >= colorDensity[i]) {
        return colors[i];
      }
    }
    return nullColor;
  }


  /** copymom() not only copies the prototype Diffuse with its defining
   *  settings, but prepares all the derivative data structures, making
   *  the resulting Diffuse ready for use.
   *
   *  Translation note: in the Obj-C version, postParamInit was inline here.
   */
  public Diffuse copymom() {
    Diffuse newbie = new Diffuse();
    newbie.setFrom(this);
    newbie.postParamInit();
    return newbie;
  }

  /** Expand defining settings into working data structures. */
  public void postParamInit() {
    int i, j;
    initColors();
    growMcr();
    cellVol	= cellSize * cellSize * sliceThickness;

    D             = new double[numSubs];
    dt            = new double[numSubs];
    Ar            = new double[numSubs];
    relaxedSub  	= new boolean[numSubs];
    for (i=0; i < numSubs; i++)
      relaxedSub[i] = true;

    // Scale our diffusion coefficients diffC[]
    for (i=0; i < numSubs; i++)
      D[i] = diffC[i] / (cellSize * cellSize * 2.0);

    // Courant condition for stability
    // Note this is our choice for dt, we could use other values,
    // with this dt it happens that diffC cancels out in D*dt
    // However, this affects only the speed of relaxation per step, not the steady state obtained,
    // because diffC is implicit in dt, so with a different diffC, we alter the number of diffSteps

    for (i=0; i < numSubs; i++)
      dt[i] = cellSize * cellSize / (4.0 * diffC[i]);
    dtmin = dt[0];
    for (i=0; i < numSubs; i++)
      if (dt[i] < dtmin)
        dtmin = dt[i];

    dtfix = dtmin;
//    Globals.log("Diffusion time step not-increased to " + dtmin + " at " + Globals.getCurrentTime());

    recalcParams(); // recalc diffusion time step (dt) dependent coefficients

    // note new double[x] initializes to 0 as part of Java spec.
    // only need to initialize after array allocation if -not- 0
    max           = new double[numSubs];
    maxlast       = new double[numSubs];

    s = new double[numSubs][][];
    d = new double[numSubs][][];
    x = new double[numSubs][][];
    g = new double[numSubs][][];
    r = new double[numSubs][][];
    for (i=0; i < numSubs; i++) {
      s[i]  = allocLattice();
      d[i]  = allocLattice();
      x[i]  = allocLattice();
      g[i]  = allocLattice();
      r[i]  = allocLattice();
    }

    xtot  = allocLattice();
    xbac  = allocLattice();
    xeps  = allocLattice();
    por   = allocLattice();
    tmp   = allocLattice();
    delta = allocLattice();
    c     = allocByteLattice();
    cs    = allocByteLattice();

    for (i=0; i < numSubs; i++) {
      fillLattice(s[i], 1.0);
      fillLattice(d[i], D[i]);
      // fillLattice(r[i], 0.0); // unnecessary, for doc'n
    }
    fillLattice(por, 1.0);
    fillLattice(tmp, 1.0);
    fillLattice(delta, 1.0);
    fillLattice(c, (byte)0);
    fillLattice(cs, (byte)0);

    /* documentation:
    for (i=0; i < numSpecies; i++) {
      [ fillLattice: (GRate**) x[i]  WithFloats: 0.0];
      [ fillLattice: (GRate**) g[i]  WithFloats: 0.0];
    }

    [ fillLattice: (GRate**) xtot  WithFloats: 0.0];
    [ fillLattice: (GRate**) xbac  WithFloats: 0.0];
    [ fillLattice: (GRate**) xeps  WithFloats: 0.0];
    */

    // make carrier in c and cs matrix
    if (geometry == BIOFILM_GEOMETRY) {
      for (i=0; i < Math.round(Math.round(carrierLayer/cellSize)); i++) {
        for (j=0; j < cellsY; j++) {
          c[i][j] = (byte)2;
          cs[i][j] = (byte)2;
        }
      }
    }

    // Allocate the vectors for tridiag
    int cellsMax = cellsX > cellsY ? cellsX + 1 : cellsY + 1;
    aa = new double[cellsMax];
    bb = new double[cellsMax];
    cc = new double[cellsMax];
    rr = new double[cellsMax];
    uu = new double[cellsMax];
    gam= new double[cellsMax];
    hh = new double[cellsMax];
    vv = new double[cellsMax];
  }

  /** This is how we get "born" by Site. */
//  public Object newCopy() {
//    getSite().notifyBirth();
//    return this.copymom();
//  }

  public void setFrom(Diffuse other) {
    super.setFrom(other);
    // let's just assume no setX routines do anything besides set X....
    printCycle = other.printCycle;
    numSubs = other.numSubs;
    numSpecies = other.numSpecies;
    stepSize = other.stepSize;
    for (int i=0; i<numSubs; i++)
      extConcn[i] = other.extConcn[i];
    for (int i=0; i<numSubs; i++)
      diffC[i] = other.diffC[i];
    cellSize = other.cellSize ;
    cellsX = other.cellsX ;
    cellsY = other.cellsY ;
    boundaryLayer = other.boundaryLayer  ;
    carrierLayer  = other.carrierLayer  ;
    sliceThickness = other.sliceThickness ;
    geometry = other.geometry  ;
    numColors = other.numColors ;
    highColorDensity = other.highColorDensity  ;
    lowColorDensity = other.lowColorDensity  ;

    /* these were in copymom in Obj-C, but I think non-heritable:
    newbie->relaxed               = relaxed;
    newbie->stable                = stable;
    newbie->haveInitMcr           = haveInitMcr;
    newbie->front                 = front;
    newbie->countDiff       	    = countDiff;
    newbie->totDiffSteps          = totDiffSteps;
    */
  }

  // Jan--any of these you want automagically appearing on the Parameters
  // popup menu need both get and set, so the machinery realizes it's a
  // property.
  public void setNumSubs(int s) {numSubs=s;}
  public int getNumSubs() {return numSubs;}
  public void setNumSpecies(int s) {numSpecies=s;}
  public int getNumSpecies() {return numSpecies;}
  public void setStepSize(int s) {stepSize=s;}
  public int getStepSize() {return stepSize;}

  // we need one var per substrate (numSubs)

  public void setExtConcn(double e0, double e1, double e2, double e3) {
    extConcn[0] = e0;
    extConcn[1] = e1;
    extConcn[2] = e2;
    extConcn[3] = e3;
  }
  public void setExtConcn(int which, double value) {extConcn[which] = value;}
  public void setExtConcn0(double e) {extConcn[0] = e;}  // set by Protocol
  public void setExtConcn1(double e) {extConcn[1] = e;}  // set by Protocol
  public void setExtConcn2(double e) {extConcn[2] = e;}  // set by Protocol
  public void setExtConcn3(double e) {extConcn[3] = e;}  // set by Protocol

  public void setDiffC(double e0, double e1, double e2, double e3) {
    setDiffC(0, e0);
    setDiffC(1, e1);
    setDiffC(2, e2);
    setDiffC(3, e3);
  }
  public void setDiffC(int which, double value) {diffC[which] = stepSize * value;}
  public void setDiffC0(double e) {setDiffC(0, e);}
  public void setDiffC1(double e) {setDiffC(1, e);}
  public void setDiffC2(double e) {setDiffC(2, e);}
  public void setDiffC3(double e) {setDiffC(3, e);}
  public void setCellSize(double s) {cellSize=s; growMcr();}
  public void setCellsX(int c) {cellsX = c; growMcr();}
  public int getCellsX() {return cellsX;}
  public void setCellsY(int c) {cellsY = c; growMcr();}
  public int getCellsY() {return cellsY;}
  /** vfb: as an int, I guess boundaryLayer is expressed as num cells wide */
  public int getBoundaryLayer() {return boundaryLayer;} // the width of the hydrodynamic boundary layer
  // the width of the hydrodynamic boundary layer
  public void setBoundaryLayer(int i) {boundaryLayer= (int) Math.rint(i / cellSize);}
  // vfb: I really don't get the int/double schizm
  public double getCarrierLayer() {
    if (geometry == BIOFILM_GEOMETRY)
      return carrierLayer;
    else
      return 0.0;
  }
  public void setCarrierLayer(int i) {carrierLayer = i;}
  public double getSliceThickness() {return sliceThickness;}// the width of the third dimension (third dimension always periodic)
  public void setSliceThickness(double d) {sliceThickness=d;}// the width of the third dimension (third dimension always periodic)
  public int getGeometry(){return geometry;}
  public boolean isBiofilm() { return (geometry == BIOFILM_GEOMETRY);}
  public boolean isPeriodicX() { return (geometry == PERIODIC_GEOMETRY); }
  public boolean isPeriodicY() { return (geometry != APERIODIC_GEOMETRY); }
  public boolean isPeriodicZ() { return (geometry != APERIODIC_GEOMETRY); }
  public void setGeometry(int i){geometry = i % (MAX_GEOMETRY+1);}
  public int getNumColors() {return numColors;}		        // how many colors used to display density
  public void setNumColors(int i) {numColors=i;}		        // how many colors used to display density
  public double getHighColorDensity() {return highColorDensity;}	// for density >= this, use high color
  public void setHighColorDensity(double d) {highColorDensity=d;}	// for density >= this, use high color
  public void setLowColorDensity(double d) {lowColorDensity=d;}	// for density < this, use low color
  public void setPrintCycle(int c) {printCycle = c;}
  // were ifdef type hooks
  public static void setExtraLoco(boolean b) {extraLoco = b;}
  public static boolean isExtraLoco() {return extraLoco;}
  public static void setVerbose(boolean b) {verbose = b;}
  public static boolean isVerbose() {return verbose;}

  // used in QuadDisplay
  public double getMcrCellLX(int x, int y) {return lx + x * cellSize;}
  public double getMcrCellLY(int x, int y) {return ly + y * cellSize;}
  public double getMcrCellUX(int x, int y) {return lx + (x+1) * cellSize; }
  public double getMcrCellUY(int x, int y) {return ly + (y+1) * cellSize; }

  // Bacillus needs to know whether we have periodic y boundaries for cell movement
  public boolean getPeriodicY() {return geometry != APERIODIC_GEOMETRY;}

  // Bacillus also need to know the length of the y coordinate (used only if biofilm)
  public double getLengthY() { return (cellSize * cellsY); }
  public double getLengthX() { return (cellSize * cellsX); }
  public double getCellSize() {return cellSize;}

  public double getLengthZ() { return sliceThickness; }

  public void growMcr() {
    setMcr(0,0,cellsX*cellSize, cellsY*cellSize);
    super.growMcr();
  }

  // Bacillus needs to know how much it overlaps with carrier
  public double getOverlapWithCarrierAt(double xi, double yi) {
    // remember agent coords are floats, xi is "left edge" of bacterial cell (x - distRadius), yi is center

    if (geometry == BIOFILM_GEOMETRY) { // got carrier at all?
      if (xi - carrierLayer >= 0) { // no overlap
        return 0.0;
      }
      return (-xi + carrierLayer); // return overlap as positive value
    }
    return 0.0;
  }

  // Bacillus needs to find out for normalising kms and kis
  public double getExtConcn(int which) {
    return extConcn[which];
  }

  /** We need a getConcn message for access from the outside (Bacilli)
   * Internally, we don't use messages for access, we use s[sub][xi][yi]
   */
  public double getConcnSub(int which, double xi, double yi) {
    // remember agent coords are doubles
    return s[which][(int) (xi / cellSize)][(int) (yi / cellSize)];
  }

  public void doleOut(double what, double[][] latt,
                      double xpos, double ypos,
                      double radius)
  {
    int xi, yi;
    int xm, xp, ym, yp;
    double bacArea;
    double rad, blx, bly, bux, buy, llx, lly, lux, luy;
    double quant;

    // agent coord converted to integer lattice position, that's why i
    xi  = (int) (xpos / cellSize);
    yi  = (int) (ypos / cellSize);

    // calc lattice neighbours
    if (geometry != APERIODIC_GEOMETRY) {
      if (yi==0) ym=cellsY-1; else ym=yi-1;
      if (yi==cellsY-1) yp=0; else yp=yi+1;
    } else {
      ym=yi-1;
      yp=yi+1;
    }
    xm=xi-1;
    xp=xi+1;

    // area to be shared
    rad = McBrick.CubertPiOver6 * radius; // radius of cube with equivalent volume
    // doleOut and avgSub rely on the assumption that agent-equiv-vol-cubes can't cover more than two lattice cells per dimension
    if ( (2*rad) > cellSize) {
      log("Warning, cube > cellSize, with ", 6, (2*rad) );
      log("\n");
    }
    bacArea = 4.0 * rad * rad;
    // amount to be shared
    quant = what / bacArea;

    // calc windows
    blx = xpos - rad; // lower x of bacillus equiv-volume cube
    bly = ypos - rad; // lower y
    bux = xpos + rad; // upper x
    buy = ypos + rad; // upper y
    llx = xi * cellSize;  // lower x of lattice element = grid node
    lly = yi * cellSize;  // lower y
    lux = llx + cellSize; // upper x
    luy = lly + cellSize; // upper y

    // calc the compass needle case
    if (blx < llx) { // to the left
      if (bly < lly) { // to the bottom
        // SW case
        latt[xi][yi] += quant * (bux - llx) * (buy - lly); // C
        latt[xm][yi] += quant * (llx - blx) * (buy - lly); // W
        latt[xm][ym] += quant * (llx - blx) * (lly - bly); // SW
        latt[xi][ym] += quant * (bux - llx) * (lly - bly); // S
      }
      else if (buy > luy) { // or to the top, relies on cellSize > cube
        // NW case
        latt[xi][yi] += quant * (bux - llx) * (luy - bly); // C
        latt[xi][yp] += quant * (bux - llx) * (buy - luy); // N
        latt[xm][yp] += quant * (llx - blx) * (buy - luy); // NW
        latt[xm][yi] += quant * (llx - blx) * (luy - bly); // W
      }
      else { // neither => middle
        // W case
        latt[xi][yi] += quant * (bux - llx) * (buy - bly); // C
        latt[xm][yi] += quant * (llx - blx) * (buy - bly); // W
      }
    }
    else if (bux > lux) { // to the right
      if (bly < lly) { // to the bottom
        // SE case
        latt[xi][yi] += quant * (lux - blx) * (buy - lly); // C
        latt[xi][ym] += quant * (lux - blx) * (lly - bly); // S
        latt[xp][ym] += quant * (bux - lux) * (lly - bly); // SE
        latt[xp][yi] += quant * (bux - lux) * (buy - lly); // E
      }
      else if (buy > luy) { // or to the top
        // NE case
        latt[xi][yi] += quant * (lux - blx) * (luy - bly); // C
        latt[xp][yi] += quant * (bux - lux) * (luy - bly); // E
        latt[xp][yp] += quant * (bux - lux) * (buy - luy); // NE
        latt[xi][yp] += quant * (lux - blx) * (buy - luy); // N
      }
      else { // neither => middle
        // E case
        latt[xi][yi] += quant * (lux - blx) * (buy - bly); // C
        latt[xp][yi] += quant * (bux - lux) * (buy - bly); // E
      }
    }
    else { // neither => middle
      if (bly < lly) { // to the bottom
        // S case
        latt[xi][yi] += quant * (bux - blx) * (buy - lly); // C
        latt[xi][ym] += quant * (bux - blx) * (lly - bly); // S
      }
      else if (buy > luy) { // or to the top
        // N case
        latt[xi][yi] += quant * (bux - blx) * (luy - bly); // C
        latt[xi][yp] += quant * (bux - blx) * (buy - luy); // N
      }
      else { // neither => middle, middle
        // C case
        latt[xi][yi] += quant * bacArea; // C
      }
    }
    doledThisStep += quant;
  }

  /** Average the subConcn in lattice cells we sit on, according to area percentage  */
  public double avgSub(int which, double xpos, double ypos, double radius) {
    int xi, yi;
    int xm, xp, ym, yp;
    double bacArea;
    double rad, blx, bly, bux, buy, llx, lly, lux, luy;
    double quant = 0.0; // averaged subConcn

    xi  = (int) (xpos / cellSize); // agent coord converted to integer lattice position, that's why i
    yi  = (int) (ypos / cellSize);

    // calc lattice neighbours
    if (geometry != APERIODIC_GEOMETRY) {
      if (yi==0) ym=cellsY-1; else ym=yi-1;
      if (yi==cellsY-1) yp=0; else yp=yi+1;
    } else {
      ym=yi-1;
      yp=yi+1;
    }
    xm=xi-1;
    xp=xi+1;

    // area to be shared
    // whether 2*rad exceeds the cellSize is checked in doleOut, don't repeat here
    rad = McBrick.CubertPiOver6 * radius; // radius of cube with equivalent volume
    bacArea = 4.0 * rad * rad;

    // calc windows
    blx = xpos - rad; // lower x of bacillus equiv-volume cube
    bly = ypos - rad; // lower y
    bux = xpos + rad; // upper x
    buy = ypos + rad; // upper y
    llx = xi * cellSize;  // lower x of lattice element = grid node
    lly = yi * cellSize;  // lower y
    lux = llx + cellSize; // upper x
    luy = lly + cellSize; // upper y

    // calc the compass needle case
    if (blx < llx) { // to the left
      if (bly < lly) { // to the bottom
        // SW case
        quant  = s[which][xi][yi] * (bux - llx) * (buy - lly) / bacArea; // C
        quant += s[which][xm][yi] * (llx - blx) * (buy - lly) / bacArea; // W
        quant += s[which][xm][ym] * (llx - blx) * (lly - bly) / bacArea; // SW
        quant += s[which][xi][ym] * (bux - llx) * (lly - bly) / bacArea; // S
      }
      else if (buy > luy) { // or to the top, relies on cellSize > cube
        // NW case
        quant  = s[which][xi][yi] * (bux - llx) * (luy - bly) / bacArea; // C
        quant += s[which][xi][yp] * (bux - llx) * (buy - luy) / bacArea; // N
        quant += s[which][xm][yp] * (llx - blx) * (buy - luy) / bacArea; // NW
        quant += s[which][xm][yi] * (llx - blx) * (luy - bly) / bacArea; // W
      }
      else { // neither => middle
        // W case
        quant  = s[which][xi][yi] * (bux - llx) * (buy - bly) / bacArea; // C
        quant += s[which][xm][yi] * (llx - blx) * (buy - bly) / bacArea; // W
      }
    }
    else if (bux > lux) { // to the right
      if (bly < lly) { // to the bottom
        // SE case
        quant  = s[which][xi][yi] * (lux - blx) * (buy - lly) / bacArea; // C
        quant += s[which][xi][ym] * (lux - blx) * (lly - bly) / bacArea; // S
        quant += s[which][xp][ym] * (bux - lux) * (lly - bly) / bacArea; // SE
        quant += s[which][xp][yi] * (bux - lux) * (buy - lly) / bacArea; // E
      }
      else if (buy > luy) { // or to the top
        // NE case
        quant  = s[which][xi][yi] * (lux - blx) * (luy - bly) / bacArea; // C
        quant += s[which][xp][yi] * (bux - lux) * (luy - bly) / bacArea; // E
        quant += s[which][xp][yp] * (bux - lux) * (buy - luy) / bacArea; // NE
        quant += s[which][xi][yp] * (lux - blx) * (buy - luy) / bacArea; // N
      }
      else { // neither => middle
        // E case
        quant  = s[which][xi][yi] * (lux - blx) * (buy - bly) / bacArea; // C
        quant += s[which][xp][yi] * (bux - lux) * (buy - bly) / bacArea; // E
      }
    }
    else { // neither => middle
      if (bly < lly) { // to the bottom
        // S case
        quant  = s[which][xi][yi] * (bux - blx) * (buy - lly) / bacArea; // C
        quant += s[which][xi][ym] * (bux - blx) * (lly - bly) / bacArea; // S
      }
      else if (buy > luy) { // or to the top
        // N case
        quant  = s[which][xi][yi] * (bux - blx) * (luy - bly) / bacArea; // C
        quant += s[which][xi][yp] * (bux - blx) * (buy - luy) / bacArea; // N
      }
      else { // neither => middle, middle
        // C case
        quant = s[which][xi][yi]; // C
      }
    }
    return quant;
  }

  /** upon request by Bacillus, calc Porosity stats, convenience method */
  public double calcPorosity() {
    // To do: minPoros depends heavily on resolution of the porosity lattice,
    // below 4 microns, the trace is really too noisy, above it, we average too
    // much and would fail to detect local overcrowding.
    // Optimal solution would be a porosity lattice with cellSize independent
    // of the substrate etc. lattice, with, say, 4 micron fixed resolution
    // (overhanging edges maybe if total system size not divisible by 4).

    int i, j;
    int count = 0;
    double sum = 0.0;
    double sumsqr = 0.0;
    double poros;
    double mass;
    double porosMin = 1.0;
    double xf, yf, radius;
    Quad q;
    Quad mobileRoot = this.mobileroot; //Globals.getMobileRoot();

    // calc xbac matrix

    // zero the matrix first
    fillLattice(xbac, 0.0);

    //traverse the agent quadding tree and read info from each agent
    // note mobileRoot is a Quad which -is- an mcr
    q = mobileRoot.pickWin(mobileRoot, mobileRoot, false);

    while (q != null) {
      if (q instanceof Bacillus) {
        Bacillus qb = (Bacillus) q;
        // vfb, 010105: I give up. Somehow, the wraps in Bacillus/Eps/BacShove
        // are missing some agents. And I've run out of ideas to follow up.
        // So. Force it again. Here.
//        debugWrap(qb);
        if (qb.getProcess() > 0) { // if not Eps
          // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
          xf  = qb.getX(); // agent coords, f for float
          yf  = qb.getY();
          // get the agents radius
          radius = qb.getRadius();
          // get the agents mass and dole it out over the lattice grids covered, for each lattice separately
          mass = qb.getDryMass();
          doleOut(mass, xbac, xf, yf, radius);
          //xbac[(int)(xf / cellSize)][(int)(yf / cellSize)] += mass;
        }
      }
      q = q.pickWin(mobileRoot, mobileRoot, false);
    }

    // calc porosity

    for (i=0; i < cellsX; i++) {
      for (j=0; j < cellsY; j++) {
        // Porosity is 1 - (biovolume / total volume)
        if (xbac[i][j] > EPSILON) { // We want to focus on the inside of the colony/biofilm
          poros   = 1.0 - (xbac[i][j]/290.0) / cellVol;
          sum    += poros;
          sumsqr += poros * poros;
          count++;
        } else {
          poros = 1.0;
        }
        if (poros < porosMin) {
          porosMin = poros;
        }
      }
    }

    /*
    porosAvg /= count;
    double porosSD = Math.sqrt( sq/count - (porosAvg * porosAvg));

    Globals.log("porosAvg=" + porosAvg + " porosSD=" + porosSD + " porosMin=" + porosMin);
    return porosMin;
    */
//    PrintWriter porFile = BacGlobals.getPorFile();
//    if (porFile == null)
//      return porosMin;
//
//    porFile.print( new PrintfFormat( "%d\t" ).sprintf( Globals.getCurrentTime() ) );
//    double avg = sum/count;
//    porFile.print( new PrintfFormat( "%g\t" ).sprintf( avg ) );  // mean
//    double stddev = Math.sqrt( (sumsqr/count) - ( avg * avg ) );
//    porFile.print( new PrintfFormat( "%g\t" ).sprintf( stddev ) ); // std dev
//    porFile.print( new PrintfFormat( "%g\n" ).sprintf( porosMin ) ); // std dev
    return porosMin;
  }

  /** We want to sum the biomass existing in every lattice cell
   * xtot is total biomass of all cells and eps, that is, cells and their products
   * xbac (x total) is any cellular biomass, not species-specific, useful for porosity
   * xeps is total mass of eps
   * x[i] is species and therefore process specific
   */
  private void update_x_Matrices() {
    boolean overBoard = false;
    int p, i;
    double xf, yf, radius;
    double mass, grate;
    Quad qq;
    Bacillus q;
    Quad mobileRoot = this.mobileroot;

    // zero the matrices
    fillLattice(xtot, 0.0);
    fillLattice(xbac, 0.0);
    fillLattice(xeps, 0.0);
    for (i=0; i < numSpecies; i++)
      fillLattice(x[i], 0.0);

    //traverse the agent quadding tree and read info from each agent
    qq = mobileRoot.pickWin(mobileRoot, mobileRoot, false);

    while (qq != null) {
      if (qq instanceof Bacillus) {
        q = (Bacillus) qq;

        // translator's note: Jan meant "double" by "float", in general.
        // GCoord was a double in the original Obj-C.
        // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
        xf  = q.getX(); // agent coords, f for float
        yf  = q.getY();
        // get the agents radius
        radius = q.getRadius();

        // check whether our agent went over board, if so, stop the ship
        if (geometry == BIOFILM_GEOMETRY) {
          overBoard = ( ((xf-radius)<0) ||
                        ((xf+radius)>(cellsX*cellSize)) ||
                        (yf<0) ||
                        (yf>(cellsY*cellSize)) );
        } else {
          overBoard = ( ((xf-radius)<0) ||
                        ((xf+radius)>(cellsX*cellSize)) ||
                        ((yf-radius)<0) ||
                        ((yf+radius)>(cellsY*cellSize)) );
        }
        if (overBoard) {
          log("Warning, agent over board at ", 6, xf);
          log(", ", 6, yf);
          log(", halting the ship!\n");
          // note, this used to set a geckoControlSwarm flag.
          // now, it stops the simulator, which should pass control
          // back up to GeckoApplet/Driver, which may do another
          // run or accept user direction
          idynomics.Idynomics.simulator.interupt = true;
//        Simulator.crashStop();
        } else {
          // get the agents mass and dole it out over the lattice grids covered, for each lattice separately
          mass = q.getDryMass();

          // xtot matrix
          doleOut(mass, xtot, xf, yf, radius);
          //xtot[(int)(xf / cellSize)][(int)(yf / cellSize)] += mass;

          // ask for the process carried out by this particular species
          p = (int) q.getProcess(); // one species does one process, species must know what they do, Eps returns -1

          if (p > 0) { // if bacterium
            doleOut(mass, xbac, xf, yf, radius);
            //xbac[(int)(xf / cellSize)][(int)(yf / cellSize)] += mass;
            // different indexing, process is [1, 2, ...] but x is [0, 1, ...]
            doleOut( mass, x[p-1], xf, yf, radius);
            //x[p-1][(int)(xf / cellSize)][(int)(yf / cellSize)] += mass;

            // get the agents growth rate and dole it out over the lattice grids covered
            grate = q.getGrowthRate();
            doleOut(grate, g[p-1], xf, yf, radius);
            //g[p-1][(int)(xf / cellSize)][(int)(yf / cellSize)] += grate;
          } else if (p == -1) {// if eps
            doleOut(mass, xeps, xf, yf, radius);
            //xeps[(int)(xf / cellSize)][(int)(yf / cellSize)] += mass;
          }
        }
      }
      qq = qq.pickWin(mobileRoot, mobileRoot, false);
    }
  }

  /** loop through the biomass matrix xtot to see where we have biomass,
   * rest is liquid unless carrier (fixed from start)
   */
  private void update_c_Matrix() {
    // carrier = 2, biofilm = 1, all liquid = 0
    for (int i=0; i < cellsX; i++) {
      for (int j=0; j < cellsY; j++) {
        if (c[i][j] != (byte)2) // re-calc if not carrier
          // set to 1 where biomass, else zero
          c[i][j] = (xtot[i][j] > 0.0) ? (byte)1 : (byte)0;
      }
    }
  }

  // find the highest peak of the biomass, called front
  private int findFront() {
    for (int i=cellsX-1; i >= 0; i--) { // start from top and go down
      for (int j=0; j < cellsY; j++) {
        if (c[i][j] == (byte)1)
          return i;
      }
    }
    return 0;
  }

  // recalc the matrix where and how to calc diffusion
  // has to be redone after each growth step, i.e. when the biomass changes
  private void update_cs_Matrix() {
    // carrier = 2, (biofilm + boundary layer) = 1, bulk liquid = 0

    int i, j;
    int top = 0;

    if (geometry == BIOFILM_GEOMETRY) {
      front = findFront(); // requires updated c matrix
      top = front + boundaryLayer + 1;
      if (top > cellsX-1) {
        top = cellsX-1;
        log("Warning, front over the top (", 6, top);
        log("), will stop sim!\n");
//      Simulator.crashStop();
        idynomics.Idynomics.simulator.interupt = true;
      }
    }

    // if (submerged) cs[i][j] = 0 outside colonyRadius + colonyBoundaryLayer

    for (i=0; i < cellsX; i++) {
      for (j=0; j < cellsY; j++) {
        if (c[i][j] == (byte)2) cs[i][j] = (byte)2; // if carrier, set to 2 in any case
        else {
          if (geometry == BIOFILM_GEOMETRY) {
            // bulk liquid on top of the biofilm + boundaryLayer
            if (i >= top) cs[i][j] = (byte)0;
            else cs[i][j] = (byte)1;
          } else {
            cs[i][j] = (byte)1;
            if ( (i == 0) || (j == 0) ||
                 (i == cellsX-1) || (j == cellsY-1) ) {
              cs[i][j] = (byte)0; // if border
            }
          }
        }
      }
    }
  }

  private void update_d_Matrices() {
    // loop through the biomass matrix xbac to calculate the fraction of biovolume and effective diffusion coefficient
    // 290 is the dryMass density of the cells, should use a param here
    // If density of agents differs, biomass and biovolume are not interchangeable anymore
    // Note that we don't smooth biomass density, we count a cell's biomass where it's center is

    // Calculation of the effective diffusion coefficient in biofilms is discussed in the Thesis of Evelien Beuling,
    // Amsterdam. Here we use Fricke's formula with a shape factor of 2.

    // 1.0 / (D*dt) is a convenient substitute for D, since D always used in this combination in adi

    int i, j, k;
    double bioVolFrac;

    for (i=0; i < cellsX; i++) {
      for (j=0; j < cellsY; j++) {
        // volume fraction is biovolume / total volume
        bioVolFrac = (xbac[i][j]/290.0) / cellVol;
        por[i][j] = 1.0 - bioVolFrac;
        // The closest attainable packing of spheres gives a volume fraction of 0.74
        if (bioVolFrac > 0.74) {
          log("Warning, volume fraction is ", 6, bioVolFrac);
          log("in " + i + "," + j + "\n");
          bioVolFrac = 0.74;
        }
        bioVolFrac = 0.0; // for evaluation of the effect of porosity
        for (k=0; k < numSubs; k++)
          d[k][i][j] = 1.0 / (((1.0-bioVolFrac)/(1.0+bioVolFrac/2.0))*D[k]*dtmin);
      }
    }
  }

  private void update_r_Matrices() {
    // We want to know x & y position and params for our substrate foo at the given concn (we know the concn)
    // Let agents calculate the reaction rate from this and store their answer in the reaction rate matrix r

    int k, i, j, p;
    int xi, yi;
    double xf, yf, radius;
    double rate;
    Quad qq;
    Bacillus q;
    Quad mobileRoot = this.mobileroot;

    // zero the matrices
    for (k=0; k < numSubs; k++)
      fillLattice(r[k], 0.0);

    //traverse the agent quadding tree and read info from each agent, calculate reaction rate and store in r
    qq = mobileRoot.pickWin(mobileRoot, mobileRoot, false);

    while (qq != null) {
      if (qq instanceof Bacillus) {
        q = (Bacillus) qq;

        // ask for the process carried out by this particular species
        p = (int) q.getProcess(); // one species does one process, species must know what they do, Eps returns -1
        if (p > 0) {
          // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
          xf  = q.getX(); // agent coords, f for float
          yf  = q.getY();
          xi  = (int) (xf / cellSize); // agent coord converted to integer lattice position, that's why i
          yi  = (int) (yf / cellSize);
          // get the agents radius
          radius = q.getRadius();

          // depending on the process, we ask for all relevant reaction rates
          // and provide the relevant subConcns
          switch (p) {
            case 1: // Nitroso
              // Ammonia (1) consumption (rate will be negative), supply
              // ammonia (1) and oxygen (0) concn
              rate = q.getRateForReaction(1, s[1][xi][yi], s[0][xi][yi]);
              doleOut(rate, r[1], xf, yf, radius);
              //r[1][xi][yi] += rate;
              // Nitrite (2) production (rate will be positive), follows from stoichiometry
              doleOut(-rate, r[2], xf, yf, radius);
              //r[2][xi][yi] += - rate;
              // Oxygen (0) consumption (rate will be negative), supply ammonia (1) and oxygen (0) concn
              rate = q.getRateForReaction( 2, s[1][xi][yi], s[0][xi][yi] );
              doleOut(rate, r[0], xf, yf, radius);
              //r[0][xi][yi] += rate;
              break;
            case 2: // Nitro
              // Nitrite (2) consumption (rate will be negative), supply
              // nitrite (2) and oxygen (0) concn
              rate = q.getRateForReaction( 11, s[2][xi][yi], s[0][xi][yi] );
              doleOut(rate, r[2], xf, yf, radius);
              //r[2][xi][yi] += rate;
              // Nitrate (3) production (rate will be positive), follows from stoichiometry
              doleOut(-rate, r[3], xf, yf, radius);
              //r[3][xi][yi] += - rate;
              // Oxygen (0) consumption (rate will be negative), supply nitrite (2) and oxygen (0) concn
              rate = q.getRateForReaction( 12, s[2][xi][yi], s[0][xi][yi] );
              doleOut(rate, r[0], xf, yf, radius);
              //r[0][xi][yi] += rate;
              break;

            default: ; // don't do anything for Eps, animals, and other agents not doing substrates
          }
        }
      }
      qq = qq.pickWin(mobileRoot, mobileRoot, false);
    }
  }

  // We want to know x & y position and params for our substrate foo at
  // the given concn (we know the concn) Let agents calculate the reaction
  // rate from this and store their answer in the reaction rate matrix r
  private void update_r_MatricesList() {
    int k, i, j, b;
    int xi, yi;
    double xf, yf, radius;
    double rate;
    Bacillus bac1, bac2;

    // zero the matrices
    for (k=0; k < numSubs; k++)
      fillLattice(r[k], 0.0);

    for (b=0; b<bac1List.size(); b++) {
      bac1 = (Bacillus) bac1List.elementAt(b);  // Nitroso: process 1
      // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
      xf  = bac1.getX(); // agent coords, f for float
      yf  = bac1.getY();
      xi  = (int) (xf / cellSize); // agent coord converted to integer lattice position, that's why i
      yi  = (int) (yf / cellSize);
      // get the agents radius
      radius = bac1.getRadius();

      // Ammonia (1) consumption (rate will be negative), supply ammonia (1) and oxygen (0) concn
      rate = bac1.getRateForReaction( 1, s[1][xi][yi], s[0][xi][yi] );
      doleOut(rate, r[1], xf, yf, radius);
      //r[1][xi][yi] += rate;
      // Nitrite (2) production (rate will be positive), follows from stoichiometry
      doleOut(-rate, r[2], xf, yf, radius);
      //r[2][xi][yi] += - rate;
      // Oxygen (0) consumption (rate will be negative), supply ammonia (1) and oxygen (0) concn
      rate = bac1.getRateForReaction( 2, s[1][xi][yi], s[0][xi][yi] );
      doleOut(rate, r[0], xf, yf, radius);
      //r[0][xi][yi] += rate;
    }

    for (b=0; b< bac2List.size(); b++) {
      bac2 = (Bacillus) bac2List.elementAt(b);
      // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
      xf  = bac2.getX(); // agent coords, f for float
      yf  = bac2.getY();
      xi  = (int) (xf / cellSize); // agent coord converted to integer lattice position, that's why i
      yi  = (int) (yf / cellSize);
      // get the agents radius
      radius = bac2.getRadius();

      // Nitrite (2) consumption (rate will be negative), supply nitrite (2) and oxygen (0) concn
      rate = bac2.getRateForReaction( 11, s[2][xi][yi], s[0][xi][yi] );
      doleOut(rate, r[2], xf, yf, radius);
      //r[2][xi][yi] += rate;
      // Nitrate (3) production (rate will be positive), follows from stoichiometry
      doleOut(-rate, r[3], xf, yf, radius);
      //r[3][xi][yi] += - rate;
      // Oxygen (0) consumption (rate will be negative), supply nitrite (2) and oxygen (0) concn
      rate = bac2.getRateForReaction( 12, s[2][xi][yi], s[0][xi][yi] );
      doleOut(rate, r[0], xf, yf, radius);
      //r[0][xi][yi] += rate;
    }
  }

  // We want to know x & y position and params for our substrate foo at
  // the given concn (we know the concn) Let agents calculate the reaction
  // rate from this and store their answer in the reaction rate matrix r
  private void update_r_Matrix(int k) {
    int i, j, b;
    int xi, yi;
    double xf, yf, radius;
    double rate, avgSub1, avgSub2;
    Bacillus bac1, bac2, eps;
    rate = 0; // compiler is concerned that I didn't set it in all switchcases....

    // zero the matrix
    fillLattice(r[k],0);

    if (k != 3) { // Nitroso doesn't deal with Nitrate (3)
      for (b=0; b<bac1List.size(); b++) { // Nitroso: process 1
        bac1 = (Bacillus) bac1List.elementAt(b);
        // get the agents position, note that agents x, y coordinates are GCoord, i.e. doubles
        xf  = bac1.getX(); // agent coords, f for float
        yf  = bac1.getY();
        //xi  = (int) (xf / cellSize); // agent coord converted to integer lattice position, that's why i
        //yi  = (int) (yf / cellSize);
        // get the agents radius
        radius = bac1.getRadius();

        // translation: Jan, this was a tad unclear.  Was esp. concerned by
        // case 2, that the reaction was 1 again.
        avgSub1 = avgSub(1, xf, yf, radius);
        avgSub2 = avgSub(0, xf, yf, radius);
        switch (k) {
        case 0:
          // Oxygen (0) consumption (rate will be negative), supply
          // ammonia (1) and oxygen (0) concn
          rate = bac1.getRateForReaction( 2, avgSub1, avgSub2 );
          //rate = [bac1 getRateForReaction: 2 Sub1: s[1][xi][yi] Sub2: s[0][xi][yi] ];
          break;
        case 1:
          // Ammonia (1) consumption (rate will be negative), supply
          // ammonia (1) and oxygen (0) concn
          rate = bac1.getRateForReaction(1, avgSub1, avgSub2);
          //rate = [bac1 getRateForReaction: 1 Sub1: s[1][xi][yi] Sub2: s[0][xi][yi] ];
          break;
        case 2:
          // Nitrite (2) production (rate will be positive), follows from stoichiometry
          rate = -1.0 * bac1.getRateForReaction(1, avgSub1, avgSub2);
          //rate = -1.0 * [bac1 getRateForReaction: 1 Sub1: s[1][xi][yi] Sub2: s[0][xi][yi] ];
          break;
        default:
          log("Warning, case 3 in update_r_Matrix shouldn't happen\n");
        }

        doleOut(rate, r[k], xf, yf, radius);
        //r[k][xi][yi] += rate;
      }
    }

    if (k != 1) { // Nitro doesn't deal with Ammonia (1)
      for (b=0; b<bac2List.size(); b++) {
        // Nitro: process 2
        bac2 = (Bacillus) bac2List.elementAt(b);
        // get the agents position, note that agents x, y coordinates are GCoord, i.e. floats
        xf  = bac2.getX(); // agent coords, f for float
        yf  = bac2.getY();
        //xi  = (int) (xf / cellSize); // agent coord converted to integer lattice position, that's why i
        //yi  = (int) (yf / cellSize);
        // get the agents radius
        radius = bac2.getRadius();

        // translation: 991015 put back the switch as per Jan's latest source
        // (reaction 12?)
        avgSub1 = avgSub(2, xf, yf, radius);
        avgSub2 = avgSub(0, xf, yf, radius);
        switch (k) {
          case 0: // Oxygen (0) consumption (rate will be negative), supply nitrite (2) and oxygen (0) concn
            rate = bac2.getRateForReaction( 12, avgSub1, avgSub2 );
            break;
          case 2: // Nitrite (2) consumption (rate will be negative), supply nitrite (2) and oxygen (0) concn
            rate = bac2.getRateForReaction( 11, avgSub1, avgSub2 );
            break;
          case 3: // Nitrate (3) production (rate will be positive), follows from stoichiometry
            rate = -1.0 * bac2.getRateForReaction( 11, avgSub1, avgSub2 );
            break;
          default: Log.out("Warning, case " + k + " in update_r_Matrix shouldn't happen");
        }
        doleOut(rate, r[k], xf, yf, radius);
        //r[k][xi][yi] += rate;
      }
    }
  }

  // Courtesy Cristian Picioreanu <C.Picioreanu@STM.TUDelft.NL>
  private void tridag() { // solves a tridiagonal matrix
    // all originally passed args were made ivars
    int i;
    double bet;

    if (bb[1] == 0.0){
      return;
    }
    bet = bb[1];
    uu[1] = rr[1]/bet;
    for (i = 2; i <= jj; i++){
      gam[i] = cc[i-1]/bet;
      bet = bb[i]-aa[i]*gam[i];
      if (bet == 0.0) {
        return;
      }
      uu[i] = (rr[i]-aa[i]*uu[i-1])/bet;
    }
    for (i = jj-1; i >= 1; i--)
      uu[i] = uu[i]-gam[i+1]*uu[i+1];
  }

  // Courtesy Cristian Picioreanu <C.Picioreanu@STM.TUDelft.NL>
  private void tridagPeriodic() { // for periodic boundaries, the matrix is not fully tridiagonal and has to be transformed first
    // all originally passed args were made ivars

    int i, i_;
    double ratio1, ratio2;

    for (i=1; i<=jj; i++) { vv[i]=0.0; hh[i]=0.0; }
    hh[1]= aa[2];      vv[1]= cc[jj-1];
    hh[jj-1]= aa[jj];  vv[jj-1]= cc[jj-1];

    for (i=2; i<=jj-1; i++) {
      i_=i-1;

      ratio2 = aa[i]/bb[i_];

      bb[i] = bb[i] - ratio2*cc[i_];
      rr[i] = rr[i] - ratio2*rr[i_];
      vv[i] = vv[i] - ratio2*vv[i_];

      ratio1 = hh[i_]/bb[i_];

      hh[i] = hh[i]  - ratio1*cc[i_];
      bb[jj]= bb[jj] - ratio1*vv[i_];
      rr[jj]= rr[jj] - ratio1*rr[i_];
    }

    ratio1= hh[jj-1]/bb[jj-1];
    bb[jj]= bb[jj] - ratio1*vv[jj-1];
    rr[jj]= rr[jj] - ratio1*rr[jj-1];

    uu[jj] = rr[jj]/bb[jj];
    uu[jj-1]= (rr[jj-1] - vv[jj-1]*uu[jj]) / bb[jj-1];
    for (i=jj-2; i>=1; i--) uu[i] = (rr[i]-cc[i]*uu[i+1]-vv[i]*uu[jj])/bb[i];
  }

  private void recalcParams() {
    int i;
    // We need these params for ADI
    // They are diffusion-timestep (dt) dependent, hence, they have to be recalculated only when dt changes
    // We normalize the length of the world in x and y, and the subConcn
    // We don't normalize the reaction rate, not necessary and not easy to do, but choose units in order to
    // have reaction rates close to unity

    // The reaction rate coefficient must be normalized by extConcn
    // Bacilli think amounts, we think concn, hence we have to divide by volume
    for (i=0; i < numSubs; i++) {
      Ar[i] = dtmin / (2.0 * cellVol * extConcn[i]);
    }
  }

  // Courtesy Cristian Picioreanu <C.Picioreanu@STM.TUDelft.NL>
  private void adi(int k) { // call adi for sub k
    // we consider the center of the lattice elements, not the nodes
    int i, j;
    maxDelta = 0.0;

    // ---- Step 1 ----- Diffusion only in Y direction --------------
    for (j = 1; j < cellsX-1; j++) { // terms in X direction are constant
      jj = 0;	// counts internal lattice points
      for (i = 1; i < cellsY-1; i++) { // --- Collecting terms for Y direction ---
        if (cs[i][j]!=(byte)0){ // don't calculate in the bulk liquid
    jj++;
    // calculate the right hand side of diffusion-reaction (directions orthogonal to diffusion direction)
    rr[jj] = s[k][i][j+1] + (d[k][i][j]-2.0)*s[k][i][j] + s[k][i][j-1] + Ar[k]*d[k][i][j]*r[k][i][j];

    if (cs[i-1][j] == (byte)0){
      rr[jj] += s[k][i-1][j];
      aa[jj] = 0.0;
    }else{
      aa[jj] = -1.0;
    }

    if (cs[i+1][j] == (byte)0){
      rr[jj] += s[k][i+1][j];
      cc[jj] = 0.0;
    }else{
      cc[jj] = -1.0;
    }

    bb[jj] = 2.0 + d[k][i][j];
        }
      }

      if (jj > 0) tridag();

      jj = 0;

      for (i = 1; i < cellsY-1; i++){
        if (cs[i][j]!=(byte)0){
    jj++;
    tmp[i][j] = (uu[jj] >= 0.0 ? uu[jj] : 0.0);
        }else{
    tmp[i][j] = 1.0;
        }
      }
    }

    // ---- Step 2 ----- Diffusion only in X direction --------------
    for (i = 1; i < cellsY-1; i++) {
      jj = 0;
      for (j = 1; j < cellsX-1; j++) {
        if (cs[i][j]!=(byte)0){
    jj++;
    rr[jj] = tmp[i+1][j] + (d[k][i][j]-2.0)*tmp[i][j] + tmp[i-1][j] + Ar[k]*d[k][i][j]*r[k][i][j];

    if (cs[i][j-1] == (byte)0){
      rr[jj] += tmp[i][j-1];
      aa[jj] = 0.0;
    }else{
      aa[jj] = -1.0;
    }

    if (cs[i][j+1] == (byte)0){
      rr[jj] += tmp[i][j+1];
      cc[jj] = 0.0;
    }else{
      cc[jj] = -1.0;
    }

    bb[jj] = 2.0 + d[k][i][j];
        }
      }

      if (jj > 0) tridag();

      jj = 0;

      for (j = 1; j < cellsX-1; j++){
        if (cs[i][j]!=(byte)0){
    jj++;
    //if (uu[jj] <= 0) {printLogD("solution is %g\t", uu[jj]); printLog("in %d, %d\n", i, j);}
    // Here we update the s matrix, before that, store the differences away for printing
    //delta[i][j] = s[k][i][j] - uu[jj];
    // Are we converging?
    if (Math.abs(uu[jj]-s[k][i][j]) > maxDelta) maxDelta = Math.abs(uu[jj]-s[k][i][j]);
    s[k][i][j] = (uu[jj] >= 0.0 ? uu[jj] : 0.0);
        }else{
    s[k][i][j] = 1.0;
        }
      }
    }
  }

  // Courtesy Cristian Picioreanu <C.Picioreanu@STM.TUDelft.NL>
  private void adiPlate(int k) { // call adiPlate for sub k
    // we consider the center of the lattice elements, not the nodes

    int top;
    int caz;
    int i, xi, yi;
    int xm, xp, ym, yp;

    maxDelta = 0.0;
    top = front + boundaryLayer + 1;

    for (yi = 0; yi < cellsY; yi++) {

      jj = 0;
      for (i = 0; i < top+1; i++) { aa[i]=0.0; bb[i]=0.0; cc[i]=0.0; rr[i]=0.0; };
      for (xi = 0; xi < top; xi++) {
        if (yi==0) ym=cellsY-1; else ym=yi-1;
        if (yi==cellsY-1) yp=0; else yp=yi+1;
        xm=xi-1;
        xp=xi+1;

        if (cs[xi][yi]==(byte)1) {
          jj++;

          caz = cs[xi][ym] + 4*cs[xi][yp];
          switch (caz) {
            case 0: case 1: case 4: case 5: // no carrier cases
              rr[jj] = s[k][xi][yp] + (d[k][xi][yi]-2.0)*s[k][xi][yi] +   s[k][xi][ym] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
              break;
            case 8: case 9: // carrier in yp
              rr[jj] =                (d[k][xi][yi]-2.0)*s[k][xi][yi] + 2*s[k][xi][ym] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
              break;
            case 2: case 6: // carrier in ym
              rr[jj] =                (d[k][xi][yi]-2.0)*s[k][xi][yi] + 2*s[k][xi][yp] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
              break;
            case 10:        // carrier both in ym and yp, gives rise to a singularity in tridag, avoid this case
              rr[jj] =                (d[k][xi][yi]-2.0)*s[k][xi][yi]                  + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
              break;
          }
          caz = cs[xm][yi] + 4*cs[xp][yi];
          switch (caz) {                                                                             // xp      xm
            case 5:	 aa[jj]=-1.0; cc[jj]=-1.0;                                                  break; // bio     bio
            case 6:	 aa[jj]= 0.0; cc[jj]=-2.0;                                                  break; // carrier bio
            case 9:	 aa[jj]=-2.0; cc[jj]= 0.0;                                                  break; // bio     carrier
            case 4:  aa[jj]= 0.0; cc[jj]=-1.0; rr[jj] = rr[jj] +   s[k][xm][yi];                break; // bulk    bio
            case 1:	 aa[jj]=-1.0; cc[jj]= 0.0; rr[jj] = rr[jj] +   s[k][xp][yi];                break; // bio     bulk
            case 0:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] +   s[k][xm][yi] + s[k][xp][yi]; break; // bulk    bulk
            case 8:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] + 2*s[k][xm][yi];                break; // bulk    carrier
            case 2:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] + 2*s[k][xp][yi];                break; // carrier bulk
            case 10: aa[jj]= 0.0; cc[jj]= 0.0;                                                  break; // carrier carrier
          }

          bb[jj] = 2.0 + d[k][xi][yi];
        }
      }

      if (jj > 0) tridag();

      jj = 0;
      for (xi = 0; xi < cellsX; xi++) {
        if (cs[xi][yi]==(byte)1) {
    jj++;
    tmp[xi][yi] = (uu[jj]>=0.0 ? uu[jj] : 0.0);
        }else{
    if (cs[xi][yi]==(byte)0) tmp[xi][yi] = 1.0;
    if (cs[xi][yi]==(byte)2) tmp[xi][yi] = 0.0;
        }
      }
    }

    for (xi = 0; xi < top; xi++) {
      for (i = 0; i < cellsY+1; i++) { aa[i]=0.0; bb[i]=0.0; cc[i]=0.0; rr[i]=0.0; };
      jj = 0;
      for (yi = 0; yi < cellsY; yi++) {

        if (yi==0) ym=cellsY-1; else ym=yi-1;
        if (yi==cellsY-1) yp=0; else yp=yi+1;
        xm=xi-1;
        xp=xi+1;

        if (cs[xi][yi]==(byte)1){
    jj++;

    caz = cs[xm][yi] + 4*cs[xp][yi];
    switch (caz){
    case 0: case 1: case 4: case 5:
      rr[jj] = tmp[xp][yi] + (d[k][xi][yi]-2.0)*tmp[xi][yi] +   tmp[xm][yi] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
      break;
    case 8: case 9:
      rr[jj] =               (d[k][xi][yi]-2.0)*tmp[xi][yi] + 2*tmp[xm][yi] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
      break;
    case 2: case 6:
      rr[jj] =               (d[k][xi][yi]-2.0)*tmp[xi][yi] + 2*tmp[xp][yi] + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
      break;
    case 10:
      rr[jj] =               (d[k][xi][yi]-2.0)*tmp[xi][yi]                 + Ar[k]*d[k][xi][yi]*r[k][xi][yi];
      break;
    }

    caz = cs[xi][ym] + 4*cs[xi][yp];
    switch (caz){
    case 5:	 aa[jj]=-1.0; cc[jj]=-1.0;                                                break;
    case 6:	 aa[jj]= 0.0; cc[jj]=-2.0;                                                break;
    case 9:	 aa[jj]=-2.0; cc[jj]= 0.0;                                                break;
    case 4:  aa[jj]= 0.0; cc[jj]=-1.0; rr[jj] = rr[jj] +   tmp[xi][ym];               break;
    case 1:	 aa[jj]=-1.0; cc[jj]= 0.0; rr[jj] = rr[jj] +   tmp[xi][yp];               break;
    case 0:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] +   tmp[xi][ym] + tmp[xi][yp]; break;
    case 8:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] + 2*tmp[xi][ym];               break;
    case 2:	 aa[jj]= 0.0; cc[jj]= 0.0; rr[jj] = rr[jj] + 2*tmp[xi][yp];               break;
    case 10: aa[jj]= 0.0; cc[jj]= 0.0;                                                break;
    }

    bb[jj] = 2.0 + d[k][xi][yi];
        }
      }

      if (jj > 0) tridagPeriodic();

      jj = 0;
      for (yi = 0; yi < cellsY; yi++) {
        if (cs[xi][yi]==(byte)1) {
          jj++;
          if (Math.abs(uu[jj]-s[k][xi][yi]) > maxDelta)
            maxDelta = Math.abs(uu[jj]-s[k][xi][yi]);
          s[k][xi][yi] = (uu[jj]>=0.0 ? uu[jj] : 0.0);
        }else{
          if (cs[xi][yi]==(byte)0)
            s[k][xi][yi] = 1.0;
          if (cs[xi][yi]==(byte)2)
            s[k][xi][yi] = 0.0;
        }
      }
    }
  }

  public void stepResources() {
    // This is the "main" routine of diffusion,
    // scheduled to repeat every plantcycle by the model swarm via landroot.
    // We don't remove any substrates but we consider reaction rates
    // We have to do some bookkeeping for the sake of our landroot!

    boolean found = false;
    Quad qq;
    Bacillus q;
    Quad mobileRoot = this.mobileroot;

    double minPorosity;

    int i, j, p, count;
    int trues;

    countDiff = 0;
    stable  = true;  // unless proven otherwise by the diffusion engine
    relaxed = false; // set to false before we start diffusion, if finished, set to true again
    for (i=0; i < numSubs; i++)
      relaxedSub[i] = false;

    // before everything else, tell critters to shove if biofilm is overcrowded
    // vfb, 991123, IFF they haven't already done the job in their stepping.
    if (calcPorosity() <= .26) {
      qq = mobileRoot.pickWin(mobileRoot, mobileRoot, false);
      if (qq == null)
        return; // nobody here yet

      while (!found && (qq != null)) {
        if (qq instanceof Bacillus) {
          q = (Bacillus) qq;
          found = true;
//          haveEps = q.haveEps(); // bad place to ask, only get here if porosity too low
//          if (extraLoco) {
//            if (calcPorosity() < 0.26)
//               minPorosity = BacShove.doTheLocomotion(this);
//          } else {
//            minPorosity = calcPorosity();
//          }
//        } else {
          qq = qq.pickWin(mobileRoot, mobileRoot, false);
        }
      }
    }

    update_x_Matrices(); // first update biomass, also check for agents outside landscape
    update_c_Matrix();   // it's easy to update c by looking at xtot
    update_cs_Matrix();  // cs depends on c, also check for front over the top
    update_d_Matrices(); // d depends on xbac

    // used to have some stopstuff here, but irrelevant now

    // create lists of agents for faster access
    // translation: maybe faster. epsList is a waste, tho....
    // wouldn't processing each agent for all subs the single time it's
    // visited this step, be faster? doesn't seem to affect calculations,
    // but maybe I've missed something
    bac1List.removeAllElements();
    bac2List.removeAllElements();
    epsList.removeAllElements();
    qq = mobileRoot.pickWin(mobileRoot, mobileRoot, false);
    while (qq != null) {
      p = -999;
      if (qq instanceof Bacillus) {
        p = ((Bacillus)qq).getProcess();
      }
//      } else if (qq instanceof Eps) {
//        // Jan: get rid of EPS matrices etc., since passive, this is only a waste of time and memory
//        p = ((Eps)qq).getProcess();
//      }
      switch (p) {
        case -1:  epsList.addElement(qq); break;
        case  1:  bac1List.addElement(qq); break;
        case  2:  bac2List.addElement(qq); break;
        default: log("Warning, undefined case in List shouldn't happen\n");
      }
      qq = qq.pickWin(mobileRoot, mobileRoot, false);
    }

    do {
      // if (countDiff % 50 == 0) printDiff("%d\t", Globals().getCurrentTime()); // prints the start of the max[i] line

      for (i=0; i < numSubs; i++) {

        // call the diffusion engine for sub i, but only if conversion for i has not been reached already
        if (!relaxedSub[i]) {
          update_r_Matrix(i); // necessary every time the subConcn changes
          if (geometry == BIOFILM_GEOMETRY)
            adiPlate(i);
          else
            adi(i);
          max[i] = maxDelta/dtfix;
          if (countDiff >= 100) {
            if ((max[i] - maxlast[i]) > 0) { // do we have an upward trend?
              stable = false;
          }
        }
        if (countDiff % 50 == 0)
          maxlast[i] = max[i];
        if (max[i] < 1.0)
          relaxedSub[i] = true;
        }
        // if (countDiff % 50 == 0) { printDiffD("%06.4f\t", max[i]);} // prints the max[i] line
      }

      // if (countDiff % 50 == 0) printDiff("\n"); // prints the end of the max[i] line

      countDiff++; totDiffSteps++;

      trues = 0;
      for (i=0; i < numSubs; i++) if (relaxedSub[i] == true) trues++;
      if (trues == numSubs) relaxed = true;

      // check whether we should make our diffusion timestep smaller
      if (!stable) {
        dtmin = dtmin * 0.9;
        stable = true;
//        Globals.log("Diffusion time step decreased to " + dtmin + " at " + Globals.getCurrentTime());
        countDiff = 0;
        for (i=0; i < numSubs; i++) relaxedSub[i] = false;
        
        // ***************************************************************************************************
        // ProcesManager
        
//        lastDtminChange = Globals.getCurrentTime();
        recalcParams();      // recalc diffusion time step (dt) dependent coefficients
        update_d_Matrices(); // depend on dt also
      }

      if (countDiff > 8000) {
        stable = false;
      }

    } while (!relaxed);


    // occasionally check whether we should make our diffusion timestep larger
    // only do so if our last instability prompted decrease of dtmin is "long" ago
//    if ( (countDiff > 1000) && ((Globals.getCurrentTime() - lastDtminChange) > 10) ) {
//      dtmin = dtmin * 1.14;
//      lastDtminChange = Globals.getCurrentTime();
//      Globals.log("Diffusion time step increased to " + dtmin + " at " + Globals.getCurrentTime());
//      recalcParams();      // recalc diffusion time step (dt) dependent coefficients
//    }

//    PrintWriter diffFile = BacGlobals.getDiffFile();
//    if (diffFile != null) {
//      diffFile.print( new PrintfFormat( "%d\t" ).sprintf( Globals.getCurrentTime() ) );
//      diffFile.print( new PrintfFormat( "%d\t" ).sprintf( countDiff ) );
//      diffFile.println();
//    }

    startres = 0.0;
    count = 0;
    for (i=0; i < cellsX; i++) {
      for (j=0; j < cellsY; j++) {
// translation: protocol says we're supposed to look at avg subconcn sub0.
//	        startres += s[2][i][j];
        if (c[i][j] == 1) { // only sum substrate concn where we have biomass
          count++;
          startres += s[0][i][j];
        }
      }
    }
    reservoir = startres / count;

    printStepResources();

    bac1List.removeAllElements();
    bac2List.removeAllElements();
    epsList.removeAllElements();
    // translation: my best guess at what these stats ought to be....
    //getSite().notifyBiomass(reservoir); // this is done automagically by setting reservoir
    getSite().notifyBiomassFed(doledThisStep);
    doledThisStep = 0;
    }

  // don't.
//  public double feedCritter(Critter eater, double percent, Mcr area) { return 0; }

  // translation: skipped most output routines.
  // the norm here is just one, provided for debug purposes...
  public String toString() {
    // and add whatever other things you want (cellsx, some status indicator, etc.)
    return "Diffuse[super=" + super.toString() + "]";
  }

  // if you want to open files, the fileStem is at:
  // String fileStem = ( (BatchUI) Globals.getUI()).getFileStem();
  // and gecko.camera.POVCamera is a simplish example.

  // printing routines at the end of the file
  public void printv() {
//    int i, j, k;
//    double d;
//    long time = Globals.getCurrentTime();
//    long run = Globals.getGeckoDriver().getNumRun();
//    DecimalFormat formRun = new java.text.DecimalFormat("000");
//    DecimalFormat formTime = new java.text.DecimalFormat("000000");
//    final String now = formTime.format(time);
//    String filename = ( (BatchUI) Globals.getUI()).getFileStem() +
//                      "_" + formRun.format(run) + "_";
//    final String filesuffix = ".ml"; // open with matlab?
//    PrintWriter file;
//
//    // there are numSubs lattices for subConcn and reaction rate
//    for (k=0; k < numSubs; k++) {
//      dumpLattice((filename + "s" + k + "_" + now + filesuffix), s[k]);
////	      dumpLattice((filename + "r" + k + "_" + now + filesuffix), r[k]);
//    }
//    // there are numSpecies lattices for biomass and growth rate
//    for (k=0; k < numSpecies; k++) {
//      dumpLattice((filename + "x" + k + "_" + now + filesuffix), x[k]);
////	      dumpLattice((filename + "g" + k + "_" + now + filesuffix), g[k]);
//    }
//
//    // print the total biomass lattice
//    //dumpLattice((filename + "xt_" + now + filesuffix), xtot);
//
//    // print the total cellular biomass and eps lattice, only if we have eps
//    if (haveEps) {
//      //dumpLattice((filename + "xb_" + now + filesuffix), xbac);
//      dumpLattice((filename + "xe_" + now + filesuffix), xeps);
//    }
//
//    // print the porosity lattice
//    // dumpLattice((filename + "p_" + now + filesuffix), por);
  }
  private void dumpLattice(String filename, double[][] latt) {
//    // Globals.getUI().openFile assumes you want to append
//    PrintWriter file;
//    try {
//      file = new PrintWriter(new FileWriter(filename), false);
//    } catch (Exception e) {
//      Globals.log("* Could not write lattice to " + filename + " : " + e.getMessage());
//      return;
//    }
//    // because Java number default will give you every roundoff-error
//    // informed digit, no matter how long the string is....
//    int siggy = 6; // significant digits
//
//    for (int j=0; j<cellsY; j++) {
//      for (int i=0; i<cellsX; i++) {
//        //file.print(TruncVal.truncVal(latt[i][j], siggy) + " " );
//        file.print(new PrintfFormat("%g ").sprintf(latt[i][j]));
//      }
//      file.println();
//    }
//    file.flush();
//    file.close();
  }

  /** was inside stepResources
   *  Printing and saving rngState is done here, _before_ the growth step,
   *  and _after_ the substrate field is relaxed. At this time, the bacteria
   * are frozen, the rng is not used, and the substrates are relaxed.
   */
  private void printStepResources() {
//    if ((Globals.getCurrentTime() % BacGlobals.getCheckpointCycle()) == 0) {
//      // noise run:
//      //if ( ((now >= 975) && (now <= 1025)) || ((now >= 9975) && (now <= 10025)) || ((now >= 29975) && (now <= 30025)) ) {
//      // prints substrate and reaction lattices, can be changed to print more
//      if (verbose)
//        printv();
//      //[self printLatticeHet];  // print SD of subConcn heterogeneity over total lattice
//      printSubConcnHet(); // this prints the heterogeneity over the cells only
//
//      int i = 1;
//      Quad mobileRoot = Globals.getMobileRoot();
//      Quad q = mobileRoot.pickWin(mobileRoot, mobileRoot, false);
//      while (q != null) {
//        if (q instanceof HasProcess) {
//          HasProcess p = (HasProcess) q;
//          if ((i == 1) && (p.getProcess() > 0) ) {
//            ((Bacillus)q).printFed();
//            i++;
//          }
//        }
//        if (q instanceof Bacillus)
//          ((Bacillus)q).printSpheresMatlab();
//        if (q instanceof Eps)
//          ((Eps)q).printSpheresMatlab();
//
//        q = q.pickWin(mobileRoot, mobileRoot, false);
//      }
//      Bacillus.closeSphereFile();
//      Eps.closeSphereFile();
//
//      /** This was verbatim from JanK's Diffuse.m source, but it didn't make
//          sense to me, and he says not.... it was verbatim, tho....
//      // If there is an in file, we have read it, so we should also read the rng-state
//      if (BacGlobals.getInputFile() != null)
//        BacGlobals.readRngState();
//      */
//
//      if ( (BacGlobals.restartFlag) && (Globals.getCurrentTime() == BacGlobals.startTime)
//           && (Globals.getGeckoDriver().getNumRun() == BacGlobals.startRun) ) {
//        BacGlobals.readRngState();
//      } else {
//        BacGlobals.saveRngState();
//      }
//    }
  }

  /** print SD of subConcn heterogeneity over all bacs */
  private void printSubConcnHet() {
//    int i, j, k, l;
//    int num; // number of lattice cells with bacteria of that type
//    double mean; // average subConcn in lattice
//    double sq; // sum of squared deviations
//    double stdDev; // Standard Deviation of SubConcn: not a sample SD, hence SD(n) not SD(n-1)
//    double sub; // subConcn
//    long now = Globals.getCurrentTime();
//    PrintWriter bacFile = BacGlobals.getBacFile();
//    if (bacFile==null) // would be if in applet.
//      return;
//
//    for (k=0; k < numSubs; k++) {
//      for (l=0; l < numSpecies; l++) {
//        mean = 0.0;
//        num  = 0;
//        sq = 0.0;
//
//        for (i=0; i < cellsX; i++) {
//          for (j=0; j < cellsY; j++) {
//            // count only lattice cells with bacteria of type l and no carrier
//            if ( (x[l][i][j] > 0) && (c[i][j] != (byte)2) ) {
//              mean += s[k][i][j];
//              num++;
//            }
//          }
//        }
//
//        mean = mean / num;
//        double single;
//
//        for (i=0; i < cellsX; i++) {
//          for (j=0; j < cellsY; j++) {
//            // count only lattice cells with bacteria of type l and no carrier
//            if ( (x[l][i][j] > 0) && (c[i][j] != (byte)2) ) {
//              sub = s[k][i][j];
//              single = sub - mean;
//              sq += single * single;
//            }
//          }
//        }
//
//        stdDev = Math.sqrt(sq / num);
//        // numerical identifier for species
//        bacFile.print(new PrintfFormat("%d\t").sprintf(Globals.getGeckoDriver().getNumRun()));
//        bacFile.print(new PrintfFormat("%d\t").sprintf(now));
//        bacFile.print(new PrintfFormat("%d\t").sprintf(k));
//        bacFile.print(new PrintfFormat("%d\t").sprintf(l));
//        bacFile.print(new PrintfFormat("%g\t").sprintf(mean));
//        bacFile.print(new PrintfFormat("%g\t").sprintf(stdDev));
//        bacFile.print(new PrintfFormat("%d\t").sprintf(num));
//        bacFile.println();
//      }
//    }
  }

  // debug routine--dump defining var values to log
  public void dumpVars() {
//    Globals.log("Diffuse parameters are:");
//    Globals.log("numSubs = " + numSubs);
//    Globals.log("numSpecies = " + numSpecies);
//    Globals.log("stepSize = " + stepSize);
//    Globals.log("extConcn = " + extConcn[0] + ", "
//      + extConcn[1] + ", "
//      + extConcn[2] + ", "
//      + extConcn[3]);
//    Globals.log("diffC = " + diffC[0] + ", "
//      + diffC[1] + ", "
//      + diffC[2] + ", "
//      + diffC[3]);
//    Globals.log("cellSize = " + cellSize);
//    Globals.log("cellsX = " + cellsX);
//    Globals.log("cellsY = " + cellsY);
//    Globals.log("boundaryLayer = " + boundaryLayer);
//    Globals.log("sliceThickness = " + sliceThickness);
//    Globals.log("geometry = " + geometry);
//    Globals.log("printCycle = " + printCycle);
//    Globals.log("numColors = " + numColors);
//    Globals.log("highColorDensity = " + highColorDensity);
//    Globals.log("lowColorDensity = " + lowColorDensity);
  }

} /* end Diffuse */

