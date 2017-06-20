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

import lb.collision.CollisionOperator;

/** Implementation of a D2Q9 lattice site (9 double precision variables) 
 */
public class Site {

	private Params params;
	private CollisionOperator collisionOperator;
	private double[] f;
	
	/* Note that the constructor copies initialF */
	public Site(Params p, CollisionOperator co, double[] initialF) {
		collisionOperator = co;
		params = p;
		f = new double[params.getFSize()];
		System.arraycopy(initialF, 0, f, 0, params.getFSize());
	}
	
	public double[] getF() {
		return f;
	}
	
	public double getF(int i) {
		return f[i];
	}
	
	public void setF(double d, int i) {
		f[i] = d;	
	}
	
	public void setF(double[] newF) {
		f = newF;	
	}
	
	public void collide() {
		collisionOperator.update(f);
	}
	
	public double rho() {
		return collisionOperator.rho(f);
	}
	
	public double[] u() {
		return collisionOperator.u(f);
	}
	
}
