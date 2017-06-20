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

/** Full-way bounce-back condition for no-slip walls */
public class BounceBack implements CollisionOperator {
	
	private final double RHO = 1.0;
	private final double U[];
	private final int[] OPPOSITE;
	private final int F_SIZE;
	double[] fPrime;
	
	public BounceBack(Params p) {
		U = new double[p.getDim()];
		OPPOSITE = p.getOpposite();
		F_SIZE = p.getFSize();
		fPrime = new double[F_SIZE];
	}

	public double rho(double[] f) {
		return RHO;
	}

	public double[] u(double[] f) {
		return U;
	}

	public void update(double[] f) {
		for(int i=0; i<F_SIZE; i++) {
			fPrime[i] = f[OPPOSITE[i]];
		}
		System.arraycopy(fPrime, 0, f, 0, F_SIZE);
	}

	public double fEq(int i, double rho, double[] u, double uNorm2) {
		throw new RuntimeException("This method does not exist");
	}

}
