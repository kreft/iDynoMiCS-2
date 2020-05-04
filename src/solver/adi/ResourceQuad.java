//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator
/**
* 990331, vfb, original Java
* Translated from the Swarm Objective-C Gecko source, version 0.51
*/
/** In Obj-C version, ResourceQuad had dummy routines, here left abstract.
*  This Quad produces a single resource.
*/


package solver.adi;
//import gecko.zoo.*;

abstract public class ResourceQuad extends Quad implements Siteable {
protected int resource;	    // each only contains one resource....
protected double startres;	// reservoir at stepResource end
protected double reservoir;	// reservoir now
protected double fed;		    // amount fed out
private Site site;       // for collecting statistics

//abstract public double feedCritter(Critter eater, double percent, Mcr area);
abstract public void stepResources();

public void setSite(Site s) {site = s;}
public Site getSite() {return site;}

public void addResource(double amount, double x, double y) {
  if (isInsideOrOn(x,y))
    reservoir += amount;
}
public void setFrom(ResourceQuad other) {
  this.setMcr(other);
  resource  = other.resource;
  fed       = other.fed;
  startres  = other.startres;
  reservoir = other.reservoir;
  site      = other.site;
}

public boolean produces(int res) { return (resource == res); }
public double fedOut() {return fed;}
public double fedOutPercent() { return ( startres==0 ? 0.0 : fed/startres ) ; }
public void zeroFed() {fed=0.0;}
public int getResource() { return resource; }
public void setResource(int res) { resource=res;  }
public double getReservoir() { return reservoir;}
public void setReservoir(double amount) { reservoir = amount;}
public double getStartReservoir() { return startres;}
public void setStartReservoir(double startAmount) { startres = startAmount;}

/** Notifies site of what happened this round in terms of statistics. */
public void step() {
  site.notifyBiomass(getReservoir());
  site.notifyBiomassFed(fedOut());
  // alas, taxes are not paid to ResourceQuad's, but the LandRoot, and
  // in a different resource. So saying what Biomass was paid to this
  // quad makes no sense. Skip it.
  super.step();
}
}

