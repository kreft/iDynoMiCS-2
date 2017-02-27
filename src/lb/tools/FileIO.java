/*  Lattice Boltzmann sample, written in Java
 *
 *  Main author: Jean-Luc Falcone
 *  Co-author: Jonas Latt
 *  Copyright (C) 2006 University of Geneva
 *  Address: Jean-Luc Falcone, Rue General Dufour 24,
 *           1211 Geneva 4, Switzerland 
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public 
 *  License along with this program; if not, write to the Free 
 *  Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301, USA.
*/

package lb.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lb.D2Q9Lattice;

/** Helper class for data output into a textfile */
public class FileIO {
	
	public static void save(String filePrefix, D2Q9Lattice lattice ) {
		PrintWriter uFile=null;
		PrintWriter rhoFile=null;
		PrintWriter xFile=null;
		PrintWriter yFile=null;
		try {
			uFile = new PrintWriter(new FileWriter(filePrefix+"_u.dat"));
			rhoFile = new PrintWriter(new FileWriter(filePrefix+"_rho.dat"));
			xFile = new PrintWriter(new FileWriter(filePrefix+"_x.dat"));
			yFile  = new PrintWriter(new FileWriter(filePrefix+"_y.dat"));
			double[][] uMatrix = lattice.normU();
			double[][] rhoMatrix = lattice.rho();
			int XX = lattice.getXX();
			int YY = lattice.getYY();
			for(int x=0; x<XX; x++) {
				for (int y=0; y<YY; y++) {
					uFile.print(uMatrix[x][y] +" ");
					rhoFile.print(rhoMatrix[x][y] +" ");
					
					xFile.print(lattice.getU(x, y)[0] +" ");
					yFile.print(lattice.getU(x, y)[1] +" ");
				}	
				uFile.println();
				rhoFile.println();
				xFile.println();
				yFile.println();
				
			} 	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			uFile.close();
			rhoFile.close();
			xFile.close();
			yFile.close();
		}
	}

}
