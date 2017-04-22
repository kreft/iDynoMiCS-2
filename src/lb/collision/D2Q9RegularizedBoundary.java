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

import lb.D2Q9;
import lb.collision.regularized.EastRegularizedBoundary;
import lb.collision.regularized.EastRhoByVelocity;
import lb.collision.regularized.NorthRegularizedBoundary;
import lb.collision.regularized.NorthRhoByVelocity;
import lb.collision.regularized.PiComputer;
import lb.collision.regularized.RhoComputer;
import lb.collision.regularized.SouthRegularizedBoundary;
import lb.collision.regularized.SouthRhoByVelocity;
import lb.collision.regularized.UByVelocity;
import lb.collision.regularized.UComputer;
import lb.collision.regularized.WestRegularizedBoundary;
import lb.collision.regularized.WestRhoByVelocity;

/** Implementation of second order accurate boundary condition for
 *  straight walls. The implementation is generic, it works for
 *  velocity/density boundaries of any orientation. The code
 *  depending on those parameters is implemented in the PiComputer,
 *  RhoComputer and UComputer classes, the policy classes
 *  responsible for the computation of the momenta of the
 *  particle distribution functions.
 */
public class D2Q9RegularizedBoundary implements CollisionOperator {
	
	private final int[][] C;
	private final double[] T;
	private final CollisionOperator lbgk;
        private final PiComputer piComputer;
        private final RhoComputer rhoComputer;
        private final UComputer uComputer;
	
	private final static int XX = 0;
	private final static int YY = 1;
	private final static int XY = 2;
		
	private double[] neqPi;
	
	public D2Q9RegularizedBoundary(double omega,
                                       PiComputer piComp,
                                       UComputer  uComp,
                                       RhoComputer rhoComp)
        {
		C = D2Q9.getInstance().getC();
		T = D2Q9.getInstance().getT();
		lbgk = new LBGK(D2Q9.getInstance(), omega);
		neqPi = new double[3];
                piComputer = piComp;
                uComputer  = uComp;
                rhoComputer = rhoComp;
	}

	public double rho(double[] f) {
		return rhoComputer.computeRho(f, this);
	}

	public double[] u(double[] f) {
		return uComputer.computeU(f, this);
	}
	
	public double fEq(int i, double rho, double[] u, double uNorm2) {
		double cDotU = C[i][0]*u[0]+C[i][1]*u[1];
		return rho*T[i]*(1 + 3*cDotU + 4.5*cDotU*cDotU - 1.5*uNorm2);
	}
	
	public double fEq(double rho, int i, double[] f) {
                double[] u   = uComputer.computeU(f, this);
                double uSqr  = uComputer.computeUSqr(f, this);
                return fEq(i, rho, u, uSqr);
	}

	public void update(double[] f) {
		double rho = rhoComputer.computeRho(f, this);
		double[] u = uComputer.computeU(f, this);
		double uSqr = uComputer.computeUSqr(f, this);
		piComputer.computeNeqPi(rho, f, neqPi, this);
		for(int i=0; i<9; i++) {
			f[i] = fEq(i, rho, u, uSqr) + 9./2 * T[i] * (
					(C[i][0]*C[i][0]-1./3) * neqPi[XX] + 
					(C[i][1]*C[i][1]-1./3) * neqPi[YY] +
					C[i][0]*C[i][1] * 2. * neqPi[XY]);
		}
		lbgk.update(f);

	}
	
	public static D2Q9RegularizedBoundary getNorthVelocityBoundary (
                double[] u0, double omega)
        {
		return new D2Q9RegularizedBoundary (
                        omega,
                        new NorthRegularizedBoundary(),
                        new UByVelocity(u0),
                        new NorthRhoByVelocity() );
	}

	public static D2Q9RegularizedBoundary getSouthVelocityBoundary (
                double[] u0, double omega)
        {
		return new D2Q9RegularizedBoundary (
                        omega,
                        new SouthRegularizedBoundary(),
                        new UByVelocity(u0),
                        new SouthRhoByVelocity() );
	}

	public static D2Q9RegularizedBoundary getEastVelocityBoundary (
                double[] u0, double omega)
        {
		return new D2Q9RegularizedBoundary (
                        omega,
                        new EastRegularizedBoundary(),
                        new UByVelocity(u0),
                        new EastRhoByVelocity() );
	}

	public static D2Q9RegularizedBoundary getWestVelocityBoundary (
                double[] u0, double omega)
        {
		return new D2Q9RegularizedBoundary (
                        omega,
                        new WestRegularizedBoundary(),
                        new UByVelocity(u0),
                        new WestRhoByVelocity() );
	}

}
