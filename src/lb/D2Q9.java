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

package lb;

/** D2Q9 lattice parameters */
public class D2Q9 implements Params {

	private final int[][] C = {{0,0},
			{1,0},{0,1},{-1,0},{0,-1},
			{1,1},{-1,1},{-1,-1},{1,-1}};
	
	private final double[] T = {4.0/9.0, 1.0/9.0,1.0/9.0,1.0/9.0,1.0/9.0,
	                                 1.0/36.0,1.0/36.0,1.0/36.0,1.0/36.0};
	
	private final int dim = 2;
	
	private final int fSize = 9;
	
	private final int[] opposite = { 0,3,4,1,2,7,8,5,6 };
	
	static private Params instance = null;
	
	private D2Q9() {}
	
	public static Params getInstance() {
		if (instance == null) {
			instance = new D2Q9();
		}
		return instance;
	}

	public int[][] getC() {
		return C;
	}

	public double[] getT() {
		return T;
	}

	public int getDim() {
		return dim;
	}
	
	public int getFSize() {
		return fSize;
	}

	public int[] getOpposite() {
		return opposite;
	}

}
