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

package lb.collision.regularized;

import lb.collision.D2Q9RegularizedBoundary;

/** Implementation of velocity on velocity boundaries */
public class UByVelocity implements UComputer {
	
	private double[] u;
        private double   uSqr;
	
	public UByVelocity(double[] _u) {
		u = _u;
                uSqr = u[0]*u[0] + u[1]*u[1];
	}
	
	public void setU(double[] _u) {
		u = _u;
                uSqr = u[0]*u[0] + u[1]*u[1];
	}

	public double[] computeU(double[] f, D2Q9RegularizedBoundary collOp) {
            return u;
        }

	public double computeUSqr(double[] f, D2Q9RegularizedBoundary collOp)
        {
            return uSqr;
        }

}
