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

	import lb.collision.BounceBack;
	import lb.collision.CollisionOperator;
	import lb.collision.D2Q9RegularizedBoundary;
	import lb.collision.LBGK;

	import static lb.tools.FileIO.save;

	/** Main class. Implementation of the flow past a cylindrical obstacle
	 *  inside a tube. The inlet and outlet implement Dirichlet velocity
	 *  boundaries with a precalculated Poiseuille profile.
	 *  The flow becomes unsteady at a Reynolds number of around Re=100.
	 */

public class LidDriven {


		
		public static final int XX = 100; // number of cells in x-direction
		public static final int YY = 100; // number of cells in y-direction
		public static final int OBST_R = YY/10 + 1; // radius of the cylinder
		public static final int OBST_X = XX/5; // position of the cylinder
		public static final int OBST_Y = YY/2; // exact y-symmetry is avoided
		
		public static final double U_MAX = 0.02; // maximum velocity of Poiseuille inflow
		public static final double RE = 100; // Reynolds number
		public static final double NU = U_MAX * 2.0 * OBST_R / RE; // kinematic viscosity
		public static final double OMEGA = 1.0 / ( 3.0 * NU + 0.5 ); // relaxation parameter
		
		public static double computePoiseuille(int y) {
			double realY = y-2;
			double realYY = YY-2;
			return 4 * U_MAX / ( realYY*realYY ) * ( realYY*realY - realY*realY );
		}
		
		public static void addObstacle(D2Q9Lattice lattice) {
			CollisionOperator bounceBack = new BounceBack(D2Q9.getInstance());
			for (int x=1; x<=XX; x++) {
				for (int y=1; y<=YY; y++) {
					if ( ( x-OBST_X )*( x-OBST_X ) + ( y-OBST_Y )*( y-OBST_Y ) <= OBST_R*OBST_R ) {
						lattice.setCollision(x,y,bounceBack);
					}
				}
			}
		}
		
		public static void initializeVelocity(D2Q9Lattice lattice, LBGK lbgk) {
			double rho = 1;
			for (int x=1; x<=XX; x++) {
				for (int y=1; y<=YY; y++) {
					double u[] = {computePoiseuille(y), 0};
					double normU = u[0]*u[0];
					for (int i=0; i<9; i++) {
						lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
					}
				}
			}	
		}
		
		// FIXME testing
		public static void setVelocity(D2Q9Lattice lattice, LBGK lbgk, int x, int y, double[] u) {
			double rho = 1;
					double normU = u[0]*u[0];
					for (int i=0; i<9; i++) {
						lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
			}	
		}
		
		public static void setSmoothVelocity(D2Q9Lattice lattice, LBGK lbgk, int xp, int yp, double a, double b, int size) 
		{
			for ( int x = -size; x < size; x++ )
			{
				for ( int y = -size; y < size; y++)
				{
					setVelocity(lattice,lbgk,x+xp,y+yp, new double[] { 
							( size-Math.abs(x) ) * a, 
							( size-Math.abs(y) ) * b});
				}
			}
		}
		
		public static void addEastBoundary(D2Q9Lattice lattice) {
			for (int y=2; y<=YY-1; y++) {
				double[] u = {computePoiseuille(y),0};
				lattice.setCollision(1,y,D2Q9RegularizedBoundary.getEastVelocityBoundary(u,OMEGA));
			}
		}
		
		public static void addWestBoundary(D2Q9Lattice lattice) {
			for (int y=2; y<=YY-1; y++) {
				double[] u = {computePoiseuille(y),0};
				lattice.setCollision(XX-2,y,D2Q9RegularizedBoundary.getWestVelocityBoundary(u,OMEGA));
			}
		}
		
		public static void main(String[] args){

			LBGK lbgk = new LBGK(D2Q9.getInstance(), OMEGA);
			
			D2Q9Lattice lattice = new D2Q9Lattice(XX, YY, lbgk);
			
			CollisionOperator northRegul = 
				D2Q9RegularizedBoundary.getNorthVelocityBoundary(new double[] {0,0}, OMEGA);
			CollisionOperator southRegul =
				D2Q9RegularizedBoundary.getSouthVelocityBoundary(new double[] {0,0}, OMEGA);
			CollisionOperator westRegul = 
					D2Q9RegularizedBoundary.getWestVelocityBoundary(new double[] {0,0}, OMEGA);
			lattice.addRectangularBoundary(1,XX,1,1,southRegul);
			lattice.addRectangularBoundary(1,XX,YY,YY,northRegul);
			lattice.addRectangularBoundary(1, 1, 1, YY, westRegul);



			
			
			

			for(int t=0; t<20000; t++) {
				for ( int y = 1; y <= YY; y++)
				{
					setVelocity(lattice,lbgk,XX,y, new double[] { 
							0.0, 0.1});
				}
				lattice.step();
				if (t % 51 == 50) {
					System.out.println(t);
				}
			}
			save("PeriodicTest",lattice);
		}
	}
