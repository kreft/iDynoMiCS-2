/**
* \package idyno
* \brief Package of classes used to launch iDynomics
*
* Package of classes used to launch and iDynoMiCS simulation, and to update
* the package to the latest stable release. This package is part of iDynoMiCS
* v2.0, governed by the CeCILL license under French law and abides by the
* rules of distribution of free software. You can use, modify and/or
* redistribute iDynoMiCS under the terms of the CeCILL license as circulated
* by CEA, CNRS and INRIA at http://www.cecill.info
*/
package idynomics;

public class Idynomics
{
	/**
	* Version number of this iteration of iDynoMiCS - required by update
	* procedure.
	*/
	public static Double version_number = 2.0;
	
	/**
	 * Version description
	 */
	public static String version_description = "alpha build 2016.02.04";
	
	/**
	 * Simulator
	 */
	public static Simulator simulator = new Simulator();
	
	/**
	* \brief Main method used to start iDynoMiCS.
	* 
	* @param args Simulation arguments passed from the command line
	*/
	public static void main(String[] args)
	{
		
	}
}
