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

package lb.collision;


import lb.Params;

/** Implementation of LBGK bulk dynamics */
public class LBGK implements CollisionOperator {
	
	private final int[][] C;
	private final double[] T;
	
	private double[] u;
	double[] fPrime ;
	
	private double omega;
	
	public LBGK(Params p, double omega) {
		this.omega = omega;
		C = p.getC();
		T = p.getT();
		u = new double[2];
		fPrime = new double[9];
	}

	public double rho(double[] f) {
		double sum = 0;
		for(int i=0; i<9; i++) {
			sum += f[i];
		}
		return sum;
	}

	public double[] u(double[] f) {
		return u(f, rho(f));
	}
	
	private double[] u(double[] f, double rho) {
		computeU(f,rho);
		return new double[] {u[0], u[1]};
	}
	
	private void computeU(double[] f, double rho) {
		u[0] = u[1] = 0;
		for(int i=0; i<9; i++) {
			u[0] += C[i][0]*f[i];
			u[1] += C[i][1]*f[i];
		}
		u[0] /= rho;
		u[1] /= rho;
	}
		
	public double fEq(int i, double rho, double[] u, double uNorm2) {
		double cDotU = u[0]*C[i][0]+u[1]*C[i][1];
		return rho*T[i]*(1 + 3*cDotU + 4.5*cDotU*cDotU - 1.5*uNorm2);
	}
	
	public double fEqFast(int i, double rho, double uNorm2) {
		double cDotU = u[0]*C[i][0]+u[1]*C[i][1];
		return rho*T[i]*(1 + 3*cDotU + 4.5*cDotU*cDotU - 1.5*uNorm2);
	}

	public void update(double[] f) {
		double rho = rho(f);
		computeU(f,rho);
		double uNorm2 = u[0]*u[0]+u[1]*u[1];
		for(int i=0; i<9; i++) {
			fPrime[i] = (1-omega)*f[i] + omega*fEqFast(i, rho, uNorm2);
		}
		System.arraycopy(fPrime, 0, f, 0, 9);
	}
	
}

