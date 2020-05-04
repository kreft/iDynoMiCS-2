//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator

/** Collects the aspect of the landscape site best dealt with for the sites
*  as a whole--statistics, name, etc. Site is analogous to Species.
*  At time 0, the prototype site is cloned and hung on the quad tree, if it
*  is a Quad. Its initial mcr should be set in the Quad itself.
*
*  Although Site is a StatSource, that's for birth/death listeners.
*  In practice, Gecko doesn't use those listeners. Though they might be
*  useful if species could emerge or go extinct in the course of a run.
*  What they're actually for in the stat package is for notifying things
*  that refer to the StatSource, that the StatSource is going away or a
*  new one needs to be made. For current Gecko needs, that doesn't happen,
*  as Sites are specified and eternal at run begin.
*/

package solver.adi;

public class Site {

	public void notifyBiomass(double reservoir) {
		// TODO Auto-generated method stub
		
	}

	public void notifyBiomassFed(double fedOut) {
		// TODO Auto-generated method stub
		
	}

}
