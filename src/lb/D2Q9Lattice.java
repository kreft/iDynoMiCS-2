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


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lb.collision.*;

/** D2Q9 regular lattice. This class is implemented in terms of
 *  a regular array of Cell objects.
 */
public class D2Q9Lattice{

	private final int XX;
	private final int YY;
	private final int F_SIZE;
	private final int[][] C;
	private final Params params;
	
	private Site[][] lattice;
	private Site[][] tempLattice;
	
	
	
	public D2Q9Lattice(int XX, int YY, CollisionOperator bulkOperator) {
		params = D2Q9.getInstance();
		this.XX = XX;
		this.YY = YY;
		F_SIZE = params.getFSize();
		C = params.getC();
		lattice = new Site[XX+2][YY+2];
		tempLattice = new Site[XX+2][YY+2];
		for(int x=0; x<XX+2; x++) {
			for(int y=0; y<YY+2; y++) {
				lattice[x][y] = new Site(params, bulkOperator, params.getT());
				tempLattice[x][y] = new Site(params, bulkOperator, params.getT());
			}
		}
	}
	
	public void setCollision(int x, int y, CollisionOperator collisionOperator) {
		lattice[x][y] = new Site(params, collisionOperator, params.getT());
		tempLattice[x][y] = new Site(params, collisionOperator, params.getT());
	}
	
	public void setF(int x, int y, double f, int i) {
		lattice[x][y].setF(f,i);
	}
	
	public void getF(int x, int y, int i) {
		lattice[x][y].getF(i);
	}
		
	public void step() {
		for(int x=1; x<=XX; x++) {
			for(int y=1; y<=YY; y++) {
				lattice[x][y].collide();
			}
		}
		periodicBoundary();
		propagate();
	}

	private void periodicBoundary() {
		// North & South Boundary
		for(int x=1; x<=XX; x++) {
			lattice[x][0].setF( lattice[x][YY].getF()[6], 6);
			lattice[x][0].setF( lattice[x][YY].getF()[2], 2);
			lattice[x][0].setF( lattice[x][YY].getF()[5], 5);
			lattice[x][YY+1].setF( lattice[x][1].getF()[7], 7);
			lattice[x][YY+1].setF( lattice[x][1].getF()[4], 4);
			lattice[x][YY+1].setF( lattice[x][1].getF()[8], 8);
		}
		// East & West Boundary
		for(int y=1; y<=YY; y++) {
			lattice[0][y].setF( lattice[XX][y].getF()[5], 5);
			lattice[0][y].setF( lattice[XX][y].getF()[8], 8);
			lattice[0][y].setF( lattice[XX][y].getF()[1], 1);
			lattice[XX+1][y].setF( lattice[1][y].getF()[7], 7);
			lattice[XX+1][y].setF( lattice[1][y].getF()[3], 3);
			lattice[XX+1][y].setF( lattice[1][y].getF()[6], 6);
		}
		// Corners
		lattice[XX+1][0].setF( lattice[1][YY].getF()[6], 6);
		lattice[0][YY+1].setF( lattice[XX][1].getF()[8], 8);
		lattice[XX+1][YY+1].setF( lattice[1][1].getF()[7], 7);
		lattice[0][0].setF( lattice[XX][YY].getF()[5], 5);
	}

	private void propagate() {
		int prevX, prevY;
		for(int x=1; x<=XX; x++) {
			for(int y=1; y<=YY; y++) {
				for(int i=0; i<F_SIZE; i++) {
					prevX = x-C[i][0];
					prevY = y-C[i][1];
			        	tempLattice[x][y].setF(lattice[prevX][prevY].getF()[i], i);
				}
			}
		}
		Site[][] swapLattice = lattice;
		lattice = tempLattice;
		tempLattice = swapLattice;	
	}
	
	public double[][] normU() {
		double[][] normU = new double[XX][YY];
		for(int x=0; x<XX; x++) {
			for(int y=0; y<YY; y++) {
				normU[x][y] = norm(lattice[x+1][y+1].u());
			}
		}
		return normU;
	}
	
	public double[] getU(int x, int y)
	{
		return lattice[x+1][y+1].u();
	}
	
	
	private double norm(double[] x) {
		double sum=0;
		for(int i=0; i<x.length; i++) {
			sum += x[i]*x[i];
		}
		return Math.sqrt(sum);
	}
	
	public double[][] rho() {
		double[][] rho = new double[XX][YY];
		for(int x=0; x<XX; x++) {
			for(int y=0; y<YY; y++) {
				rho[x][y] = lattice[x+1][y+1].rho();
			}
		}
		return rho;
	}
	
	public int getXX() {
		return XX;
	}
	
	public int getYY() {
		return YY;
	}
	
	public void addRectangularBoundary(int oX1, int oX2, int oY1, int oY2,
			CollisionOperator operator) {
		for(int x=oX1; x<=oX2; x++) {
			for (int y=oY1; y<=oY2; y++) {
				setCollision(x,y,operator);
			}
		}
	}
	

	




}
