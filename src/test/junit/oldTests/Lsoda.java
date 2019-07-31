package test.junit.oldTests;

import linearAlgebra.Matrix;
import test.other.LsodaTest;

public class Lsoda {
	
	private static int gNyh = 0, gLenyh = 0;
	
	public static double[] y;
	
	public static double t;
	
	public static int istate;

	private static int ml, mu;

	private static int imxer;

	private static int[] mord = { 0, 12, 5 };

	private static double rh, pdh, rhup;
	
	private static double sqrteta, del;

	private static double[] yp1, yp2;

	private static double[] sm1 = { 0., 0.5, 0.575, 0.55, 0.45, 0.35, 0.25, 0.2, 0.15, 0.1, 0.075, 0.05, 0.025 };

	private static double ccmax, el0, h, hmin, hmxi, hu, rc, tn;

	private static int illin = 0, mxstep, mxhnil, nhnil;

	private static int ntrep = 0;

	private static int nslast;

	private static int nyh;

	private static int ierpj, ier = 0;

	private static int iersl;

	private static int jcur;

	private static int jstart;

	private static int kflag, corflag;

	private static int l;

	private static int meth;

	private static int miter;

	private static int maxord;

	private static int maxcor;

	private static int msbp;

	private static int mxncf;
	
	private static int m;

	private static int n;

	private static int nq;

	private static int nst;

	private static int nfe;

	private static int nje;

	private static int nqu;

	public static boolean init = false;

	private static double tsw, pdnorm;

	private static int ixpr = 0, jtyp, mused, mxordn, mxords;

	private static double conit, crate, hold, rmax;

	private static double[] el = new double[14];

	private static double[][] elco = new double[13][14], tesco = new double[13][4];

	private static int ialth, ipup, lmax, nslp;

	private static double pdest, pdlast, ratio;

	private static double[] cm1 = new double[13], cm2 = new double[6];

	private static int icount, irflag;

	private static double[][] yh, wm;

	private static double[] ewt, savf, acor;

	private static int[] ipvt;
	
	private static int orderflag = 0;

	private static final double ETA = 2.2204460492503131e-16;

	public Lsoda() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * lsoda - livermore solver for ordinary differential equations, with
	 * automatic method switching for stiff and nonstiff problems.
	 * 
	 * references 1. alan c. hindmarsh, odepack, a systematized collection of
	 * ode solvers, in scientific computing, r. s. stepleman et al. (eds.),
	 * north-holland, amsterdam, 1983, pp. 55-64. 2. linda r. petzold, automatic
	 * selection of methods for solving stiff and nonstiff systems of ordinary
	 * differential equations, siam j. sci. stat. comput. 4 (1983), pp. 136-148.
	 * 
	 * @param neq
	 * @param tout
	 * @param itol
	 * @param rtol
	 * @param atol
	 * @param itask
	 * @param iopt
	 * @param jt
	 * @param iwork1
	 * @param iwork2
	 * @param iwork5
	 * @param iwork6
	 * @param iwork7
	 * @param iwork8
	 * @param iwork9
	 * @param rwork1
	 * @param rwork5
	 * @param rwork6
	 * @param rwork7
	 * @param data
	 * @return 
	 */

	public static void ODElsoda(int neq, double tout, int itol, double[] rtol, double[] atol,
			int itask, int iopt, int jt, int iwork1, int iwork2, int iwork5,
			int iwork6, int iwork7, int iwork8, int iwork9, double rwork1, double rwork5,
			double rwork6, double rwork7, Object data)
	{
		int mxstp0 = 5000, mxhnl0 = 10, i, iflag = 0, lenyh;
		boolean ihit = false;
		double atoli, ayi, big, h0 = 0.0, hmax, hmx, rtoli, tcrit = 0, tdist, tnext, tol,
				tolsf, tp, size, sum, w0;
		
		if (istate == 1)
			_freevectors();
		if (istate < 1 || istate > 3) {
			System.err.println("[lsoda] illegal istate = " + istate);
			terminate();
			return;
		}
		if (itask < 1 || itask > 5) {
			System.err.println("[lsoda] illegal itask = " + itask);
			terminate();
			return;
		}
		if (!init && (istate == 2 || istate == 3)) {
			System.err.println("[lsoda] istate > 1 but lsoda not initialized");
			terminate();
			return;
		}
		if (istate == 1) {
			init = false;
			if (tout == t) {
				ntrep++;
				if (ntrep < 5) {
					return;
				}
				System.err.println(
						"[lsoda] repeated calls with istate = 1 and tout = t. run aborted.. apparent infinite loop");
				return;
			}
		}

		if (istate == 1 || istate == 3) {
			ntrep = 0;
			if (neq <= 0) {
				System.err.println("[lsoda] neq = " + neq + " is less than 1");
				terminate();
				return;
			}
			if (istate == 3 && neq > n) {
				System.err.println("[lsoda] istate = 3 and neq increased\n");
				terminate();
				return;
			}
			n = neq;
			if (itol < 1 || itol > 4) {
				System.err.println("[lsoda] itol = " + itol + " illegal");
				terminate();
				return;
			}
			if (iopt < 0 || iopt > 1) {
				System.err.println("[lsoda] iopt = " + iopt + " illegal");
				terminate();
				return;
			}
			if (jt == 3 || jt < 1 || jt > 5) {
				System.err.println("[lsoda] jt = " + jt + " illegal");
				terminate();
				return;
			}
			jtyp = jt;
			if (jt > 2) {
				ml = iwork1;
				mu = iwork2;
				if (ml < 0 || ml >= n) {
					System.err.println("[lsoda] ml = " + ml + " not between 1 and neq");
					terminate();
					return;
				}
				if (mu < 0 || mu >= n) {
					System.err.println("[lsoda] mu = " + mu + " not between 1 and neq");
					terminate();
					return;
				}
			}
			/* Next process and check the optional inputs. */

			/* Default options. */

			if (iopt == 0) {
				ixpr = 0;
				mxstep = mxstp0;
				mxhnil = mxhnl0;
				hmxi = 0.;
				hmin = 0.;
				if (istate == 1) {
					h0 = 0.;
					mxordn = mord[1];
					mxords = mord[2];
				}
			}
			/* Optional inputs. */
			else { /* if ( iopt = 1 ) */
				ixpr = iwork5;
				if (ixpr < 0 || ixpr > 1) {
					System.err.println("[lsoda] ixpr = " + ixpr + " is illegal");
					terminate();
					return;
				}
				mxstep = iwork6;
				if (mxstep < 0) {
					System.err.println("[lsoda] mxstep < 0");
					terminate();
					return;
				}
				if (mxstep == 0)
					mxstep = mxstp0;
				mxhnil = iwork7;
				if (mxhnil < 0) {
					System.err.println("[lsoda] mxhnil < 0");
					terminate();
					return;
				}
				if (istate == 1) {
					h0 = rwork5;
					mxordn = iwork8;
					if (mxordn < 0) {
						System.err.println("[lsoda] mxordn = " + mxordn + " is less than 0");
						terminate();
						return;
					}
					if (mxordn == 0)
						mxordn = 100;
					mxordn = Math.min(mxordn, mord[1]);
					mxords = iwork9;
					if (mxords < 0) {
						System.err.println("[lsoda] mxords = " + mxords + " is less than 0");
						terminate();
						return;
					}
					if (mxords == 0)
						mxords = 100;
					mxords = Math.min(mxords, mord[2]);
					if ((tout - t) * h0 < 0.) {
						System.err.println("[lsoda] tout = " + tout + " behind t = " + t
								+ ". integration direction is given by " + h0);
						terminate();
						return;
					}
				} /* end if ( *istate == 1 ) */
				hmax = rwork6;
				if (hmax < 0.) {
					System.err.println("[lsoda] hmax < 0.\n");
					terminate();
					return;
				}
				hmxi = 0.;
				if (hmax > 0)
					hmxi = 1. / hmax;
				hmin = rwork7;
				if (hmin < 0.) {
					System.err.println("[lsoda] hmin < 0.\n");
					terminate();
					return;
				}
			}
		}
		/*
		 * If *istate = 1, meth is initialized to 1. Also allocate memory for
		 * yh, wm, ewt, savf, acor, ipvt.
		 */
		if (istate == 1) {
			sqrteta = Math.sqrt(ETA);
			meth = 1;
			gNyh = nyh = n;
			gLenyh = lenyh = 1 + Math.max(mxordn, mxords);

			yh = new double[1 + lenyh][1 + nyh];
			wm = new double[1 + nyh][1 + nyh];
			ewt = new double[1 + nyh];
			savf = new double[1 + nyh];
			acor = new double[1 + nyh];
			ipvt = new int[1 + nyh];
		}
		/* Check rtol and atol for legality. */
		if (istate == 1 || istate == 3) {
			rtoli = rtol[1];
			atoli = atol[1];
			for (i = 1; i <= n; i++) {
				if (itol >= 3)
					rtoli = rtol[i];
				if (itol == 2 || itol == 4)
					atoli = atol[i];
				if (rtoli < 0.) {
					System.err.println("[lsoda] rtol = " + rtoli + " is less than 0.");
					terminate();
					freevectors();
					return;
				}
				if (atoli < 0.) {
					System.err.println("[lsoda] atol = " + atoli + " is less than 0.");
					terminate();
					freevectors();
					return;
				}
			}
		}
		/* If istate = 3, set flag to signal parameter changes to stoda. */
		if (istate == 3) {
			jstart = -1;
		}
		/*
		 * Block c. The next block is for the initial call only ( *istate = 1 ).
		 * It contains all remaining initializations, the initial call to f, and
		 * the calculation of the initial step size. The error weights in ewt
		 * are inverted after being loaded.
		 */
		if (istate == 1) {
			tn = t;
			tsw = t;
			maxord = mxordn;
			if (itask == 4 || itask == 5) {
				tcrit = rwork1;
				if ((tcrit - tout) * (tout - t) < 0.) {
					System.err.println("[lsoda] itask = 4 or 5 and tcrit behind tout");
					terminate();
					freevectors();
					return;
				}
				if (h0 != 0. && (t + h0 - tcrit) * h0 > 0.)
					h0 = tcrit - t;
			}
			jstart = 0;
			nhnil = 0;
			nst = 0;
			nje = 0;
			nslast = 0;
			hu = 0.;
			nqu = 0;
			mused = 0;
			miter = 0;
			ccmax = 0.3;
			maxcor = 3;
			msbp = 20;
			mxncf = 10;
			
			/* call the equations */
			double[] yhTemp = arrayShift(yh[2], 1);
			LsodaTest.eqns(arrayShift(y, 1), yhTemp);
			yh[2] = arrayShift(yhTemp, -1);

			nfe = 1;
			/* Load the initial value vector in yh. */
			yp1 = yh[1];
			for (i = 1; i <= n; i++)
				yp1[i] = y[i];
			yh[1] = yp1;
			/* Load and invert the ewt array. ( h is temporarily set to 1. ) */
			nq = 1;
			h = 1.;
			ewset(itol, rtol, atol, y);
			for (i = 1; i <= n; i++) {
				if (ewt[i] <= 0.) {
					System.err.println("[lsoda] ewt[" + i + "] = " + ewt[i] + " <= 0.");
					terminate2();
					return;
				}
				ewt[i] = 1. / ewt[i];
			}

			/*
			 * The coding below computes the step size, h0, to be attempted on
			 * the first step, unless the user has supplied a value for this.
			 * First check that tout - *t differs significantly from zero. A
			 * scalar tolerance quantity tol is computed, as max(rtol[i]) if
			 * this is positive, or max(atol[i]/Math.abs(y[i])) otherwise,
			 * adjusted so as to be between 100*ETA and 0.001. Then the computed
			 * value h0 is given by
			 * 
			 * h0^(-2) = 1. / ( tol * w0^2 ) + tol * ( norm(f) )^2
			 * 
			 * where w0 = max( Math.abs(*t), Math.abs(tout) ), f = the initial
			 * value of the vector f(t,y), and norm() = the weighted vector norm
			 * used throughout, given by the vmnorm function routine, and
			 * weighted by the tolerances initially loaded into the ewt array.
			 * 
			 * The sign of h0 is inferred from the initial values of tout and
			 * *t. Math.abs(h0) is made < Math.abs(tout-*t) in any case.
			 */
			if (h0 == 0.) {
				tdist = Math.abs(tout - t);
				w0 = Math.max(Math.abs(t), Math.abs(tout));
				if (tdist < 2. * ETA * w0) {
					System.err.println("[lsoda] tout too close to t to start integration");
					terminate();
					freevectors();
					return;
				}
				tol = rtol[1];
				if (itol > 2) {
					for (i = 2; i <= n; i++)
						tol = Math.max(tol, rtol[i]);
				}
				if (tol <= 0.) {
					atoli = atol[1];
					for (i = 1; i <= n; i++) {
						if (itol == 2 || itol == 4)
							atoli = atol[i];
						ayi = Math.abs(y[i]);
						if (ayi != 0.)
							tol = Math.max(tol, atoli / ayi);
					}
				}
				tol = Math.max(tol, 100. * ETA);
				tol = Math.min(tol, 0.001);
				sum = vmnorm(n, yh[2], ewt);
				sum = 1. / (tol * w0 * w0) + tol * sum * sum;
				h0 = 1. / Math.sqrt(sum);
				h0 = Math.min(h0, tdist);
				h0 = h0 * ((tout - t >= 0.) ? 1. : -1.);
			}
			/* Adjust h0 if necessary to meet hmax bound. */
			rh = Math.abs(h0) * hmxi;
			if (rh > 1.)
				h0 /= rh;
			/* Load h with h0 and scale yh[2] by h0. */
			h = h0;
			yp1 = yh[2];
			for (i = 1; i <= n; i++)
				yp1[i] *= h0;
			yh[2] = yp1;
		}
		/*
		 * Block d. The next code block is for continuation calls only ( *istate
		 * = 2 or 3 ) and is to check stop conditions before taking a step.
		 */
		if (istate == 2 || istate == 3) {
			nslast = nst;
			switch (itask) {
			case 1:
				if ((tn - tout) * h >= 0.) {
					intdy(tout, 0, y, iflag);
					if (iflag != 0) {
						System.err.println("[lsoda] trouble from intdy, itask = " + itask + ", tout = " + tout);
						terminate();
						freevectors();
						return;
					}
					t = tout;
					istate = 2;
					illin = 0;
					freevectors();
					return;
				}
				break;
			case 2:
				break;
			case 3:
				tp = tn - hu * (1. + 100. * ETA);
				if ((tp - tout) * h > 0.) {
					System.err.println("[lsoda] itask = " + itask + " and tout behind tcur - hu");
					terminate();
					freevectors();
					return;
				}
				if ((tn - tout) * h < 0.)
					break;
				successreturn(itask, ihit, tcrit);
				return;
			case 4:
				tcrit = rwork1;
				if ((tn - tcrit) * h > 0.) {
					System.err.println("[lsoda] itask = 4 or 5 and tcrit behind tcur");
					terminate();
					freevectors();
					return;
				}
				if ((tcrit - tout) * h < 0.) {
					System.err.println("[lsoda] itask = 4 or 5 and tcrit behind tout");
					terminate();
					freevectors();
					return;
				}
				if ((tn - tout) * h >= 0.) {
					intdy(tout, 0, y, iflag);
					if (iflag != 0) {
						System.err.println("[lsoda] trouble from intdy, itask = " + itask + ", tout = " + tout);
						terminate();
						freevectors();
						return;
					}
					t = tout;
					istate = 2;
					illin = 0;
					freevectors();
					return;
				}
			case 5:
				if (itask == 5) {
					tcrit = rwork1;
					if ((tn - tcrit) * h > 0.) {
						System.err.println("[lsoda] itask = 4 or 5 and tcrit behind tcur");
						terminate();
						freevectors();
						return;
					}
				}
				hmx = Math.abs(tn) + Math.abs(h);
				ihit = Math.abs(tn - tcrit) <= (100. * ETA * hmx);
				if (ihit) {
					t = tcrit;
					successreturn(itask, ihit, tcrit);
					return;
				}
				tnext = tn + h * (1. + 4. * ETA);
				if ((tnext - tcrit) * h <= 0.)
					break;
				h = (tcrit - tn) * (1. - 4. * ETA);
				if (istate == 2)
					jstart = -2;
				break;
			}
		}
		/*
		 * Block e. The next block is normally executed for all calls and
		 * contains the call to the one-step core integrator stoda.
		 * 
		 * This is a looping point for the integration steps.
		 * 
		 * First check for too many steps being taken, update ewt ( if not at
		 * start of problem). Check for too much accuracy being requested, and
		 * check for h below the roundoff level in *t.
		 */
		while (true) {
			if (istate != 1 || nst != 0) {
				if ((nst - nslast) >= mxstep) {
					System.err.println("[lsoda] " + mxstep + " steps taken before reaching tout");
					istate = -1;
					terminate2();
					return;
				}
				ewset(itol, rtol, atol, yh[1]);
				for (i = 1; i <= n; i++) {
					if (ewt[i] <= 0.) {
						System.err.println("[lsoda] ewt[" + i + "] = " + ewt[i] + " <= 0.");
						istate = -6;
						terminate2();
						return;
					}
					ewt[i] = 1. / ewt[i];
				}
			}
			tolsf = ETA * vmnorm(n, yh[1], ewt);
			if (tolsf > 0.01) {
				tolsf = tolsf * 200.;
				if (nst == 0) {
					System.err.println("lsoda -- at start of problem, too much accuracy\n"
							+ "requested for precision of machine, suggested scaling factor = " + tolsf);
					terminate();
					freevectors();
					return;
				}
				System.err.println("lsoda -- at t = " + t + ", too much accuracy requested\n"
						+ "for precision of machine, suggested scaling factor = " + tolsf);
				istate = -2;
				terminate2();
				return;
			}
			if ((tn + h) == tn) {
				nhnil++;
				if (nhnil <= mxhnil) {
					System.err.println("lsoda -- warning..internal t = " + tn + " and h = " + h
							+ " are such that in the machine, t + h = t on the next step\n"
							+ "solver will continue anyway.");
					if (nhnil == mxhnil) {
						System.err.println("lsoda -- above warning has been issued " + nhnil
								+ " times,\n it will not be issued again for this problem");
					}
				}
			}
			/* Call stoda */
			stoda(neq, data);

			/*
			 * Block f. The following block handles the case of a successful
			 * return from the core integrator ( kflag = 0 ). If a method switch
			 * was just made, record tsw, reset maxord, set jstart to -1 to
			 * signal stoda to complete the switch, and do extra printing of
			 * data if ixpr = 1. Then, in any case, check for stop conditions.
			 */
			if (kflag == 0) {
				init = true;
				if (meth != mused) {
					tsw = tn;
					maxord = mxordn;
					if (meth == 2)
						maxord = mxords;
					jstart = -1;
					if (ixpr != 0) {
						if (meth == 2)
							System.err.println("[lsoda] a switch to the stiff method has occurred ");
						if (meth == 1)
							System.err.println("[lsoda] a switch to the nonstiff method has " + "occurred at t = " + tn
									+ ", tentative step size h = " + h + "step nst = " + nst);
					}
				}
				/* itask = 1. If tout has been reached, interpolate. */
				if (itask == 1) {
					if ((tn - tout) * h < 0.)
						continue;
					intdy(tout, 0, y, iflag);
					t = tout;
					istate = 2;
					illin = 0;
					freevectors();
					return;
				}
				/* itask = 2. */
				if (itask == 2) {
					successreturn(itask, ihit, tcrit);
					return;
				}
				/* itask = 3. Jump to exit if tout was reached. */
				if (itask == 3) {
					if ((tn - tout) * h >= 0.) {
						successreturn(itask, ihit, tcrit);
						return;
					}
					continue;
				}
				/*
				 * itask = 4. See if tout or tcrit was reached. Adjust h if
				 * necessary.
				 */
				if (itask == 4) {
					if ((tn - tout) * h >= 0.) {
						intdy(tout, 0, y, iflag);
						t = tout;
						istate = 2;
						illin = 0;
						freevectors();
						return;
					} else {
						hmx = Math.abs(tn) + Math.abs(h);
						ihit = Math.abs(tn - tcrit) <= (100. * ETA * hmx);
						if (ihit) {
							successreturn(itask, ihit, tcrit);
							return;
						}
						tnext = tn + h * (1. + 4. * ETA);
						if ((tnext - tcrit) * h <= 0.)
							continue;
						h = (tcrit - tn) * (1. - 4. * ETA);
						jstart = -2;
						continue;
					}
				}
				/* itask = 5. See if tcrit was reached and jump to exit. */
				if (itask == 5) {
					hmx = Math.abs(tn) + Math.abs(h);
					ihit = Math.abs(tn - tcrit) <= (100. * ETA * hmx);
					successreturn(itask, ihit, tcrit);
					return;
				}
			}
			/*
			 * kflag = -1, error test failed repeatedly or with Math.abs(h) =
			 * hmin. kflag = -2, convergence failed repeatedly or with
			 * Math.abs(h) = hmin.
			 */
			if (kflag == -1 || kflag == -2) {
				System.err.println("lsoda -- at t = " + tn + " and step size h = " + h + ", the");
				if (kflag == -1) {
					System.err.println(
							"         error test failed repeatedly or\n" + "         with Math.abs(h) = hmin\n");
					istate = -4;
				}
				if (kflag == -2) {
					System.err.println("         corrector convergence failed repeatedly or\n"
							+ "         with Math.abs(h) = hmin\n");
					istate = -5;
				}
				big = 0.;
				imxer = 1;
				for (i = 1; i <= n; i++) {
					size = Math.abs(acor[i]) * ewt[i];
					if (big < size) {
						big = size;
						imxer = i;
					}
				}
				terminate2();
				return;
			}
		}
	}

	/**
	 * Purpose : Find largest component of double vector dx Find smallest index
	 * of maximum magnitude of dx. getMax = first i, i=1 to n, to minimize
	 * Math.abs( dx[1-incx+i*incx] ).
	 * 
	 * n : number of elements in input vector dx : double vector with n+1
	 * elements, dx[0] is not used incx : storage spacing between elements of dx
	 * 
	 * idamax : smallest index, 0 if n <= 0
	 */

	public static int getMax(int n, double[] dx, int incx) {
		double dmax, xmag;
		int ii, xindex;

		xindex = 0;
		if (n <= 0) {
			return xindex;
		}
		xindex = 1;
		if (n <= 1 || incx <= 0) {
			return xindex;
		}

		/* Increments not equal to 1 */
		if (incx != 1) {
			dmax = Math.abs(dx[1]);
			ii = 2;
			for (int i = 1 + incx; i <= n * incx; i = i + incx) {
				xmag = Math.abs(dx[i]);
				if (xmag > dmax) {
					xindex = ii;
					dmax = xmag;
				}
				ii++;
			}
			return xindex;
		}

		/* Increments equal to 1 */
		dmax = Math.abs(dx[1]);
		for (int i = 2; i <= n; i++) {
			xmag = Math.abs(dx[i]);
			if (xmag > dmax) {
				xindex = i;
				dmax = xmag;
			}
		}
		return xindex;
	}

	/**
	 * Scalar Vector multiplication.
	 * 
	 * dx = da * dx
	 * 
	 * @param n
	 *            number of elements in input vector
	 * @param da
	 *            double scale factor
	 * @param dx
	 *            double vector with n+1 elements, dx[0] is not used
	 * @param incx
	 *            storage spacing between elements of dx
	 */
	public static void dscal(int n, double da, double[] dx, int incx) {
		int m;

		if (n <= 0) {
			return;
		}

		/* Increments not equal to 1 */
		if (incx != 1) {
			for (int i = 1; i <= n * incx; i = i + incx) {
				dx[i] = da * dx[i];
			}
			return;
		}

		/*
		 * Increments equal to 1 Making sure remaining vector length is multiple
		 * of 5
		 */
		m = n % 5;

		if (m != 0) {
			for (int i = 1; i <= m; i++) {
				dx[i] = da * dx[i];
			}
			if (n < 5) {
				return;
			}
		}

		for (int i = m + 1; i <= n; i = i + 5) {
			dx[i] = da * dx[i];
			dx[i + 1] = da * dx[i + 1];
			dx[i + 2] = da * dx[i + 2];
			dx[i + 3] = da * dx[i + 3];
			dx[i + 4] = da * dx[i + 4];
		}

		return;
	}

	/**
	 * Inner product dx . dy
	 * 
	 * @param n
	 *            number of elements in input vector(s)
	 * @param dx
	 *            double vector with n+1 elements, dx[0] is not used
	 * @param incx
	 *            storage spacing between elements of dx
	 * @param dy
	 *            double vector with n+1 elements, dy[0] is not used
	 * @param incy
	 *            storage spacing between elements of dy
	 * @return dot product dx . dy, 0 if n <= 0
	 */
	private static double ddot(int n, double[] dx, int incx, double[] dy, int incy) {
		double dotprod;
		int ix, iy, m;

		/* return 0 if n is less than 0 */
		dotprod = 0.;
		if (n <= 0) {
			return dotprod;
		}

		/* Unequal or non-positive increments */
		if (incx != incy || incx < 1) {
			ix = 1;
			iy = 1;
			if (incx < 0) {
				ix = (-n + 1) * incx + 1;
			}
			if (incy < 0) {
				iy = (-n + 1) * incy + 1;
			}
			for (int i = 1; i <= n; i++) {
				dotprod = dotprod + dx[ix] * dy[iy];
				ix = ix + incx;
				iy = iy + incy;
			}
			return dotprod;
		}

		/*
		 * Code for both increments equal to 1. Make sure remaining vector
		 * length is a multiple of 5.
		 */

		if (incx == 1) {
			m = n % 5;
			if (m != 0) {
				for (int i = 1; i <= m; i++) {
					dotprod = dotprod + dx[i] * dy[i];
				}
				if (n < 5) {
					return dotprod;
				}
			}
			for (int i = m + 1; i <= n; i = i + 5) {
				dotprod = dotprod + dx[i] * dy[i] + dx[i + 1] * dy[i + 1] + dx[i + 2] * dy[i + 2]
						+ dx[i + 3] * dy[i + 3] + dx[i + 4] * dy[i + 4];
			}
			return dotprod;
		}

		/* Code for positive equal non-unit increments. */
		for (int i = 1; i <= n * incx; i = i + incx) {
			dotprod = dotprod + dx[i] * dy[i];
		}
		return dotprod;
	}

	/**
	 * This function computes dy = da * dx + dy
	 * 
	 * @param n
	 *            number of elements in input vector(s)
	 * @param da
	 *            double scalar multiplier
	 * @param dx
	 *            double vector with n+1 elements, dx[0] is not used
	 * @param incx
	 *            storage spacing between elements of dx
	 * @param dy
	 *            double vector with n+1 elements, dy[0] is not used,
	 *            overwritten
	 * @param incy
	 *            storage spacing between elements of dy
	 */
	private static void daxpy(int n, double da, double[] dx, int incx, double[] dy, int incy) {
		int ix, iy, m;

		if (n < 0 || da == 0.) {
			return;
		}

		/* Unequal or non-positive increments. */
		if (incx != incy || incx < 1) {
			ix = 1;
			iy = 1;
			if (incx < 0) {
				ix = (-n + 1) * incx + 1;
			}
			if (incy < 0) {
				iy = (-n + 1) * incy + 1;
			}
			for (int i = 1; i <= n; i++) {
				dy[iy] += (da * dx[ix]);
				ix = ix + incx;
				iy = iy + incy;
			}
			return;
		}

		/*
		 * Both increments equal to 1. Making the remaining vector length a
		 * multiple of 4.
		 */
		if (incx == 1) {
			m = n % 4;
			if (m != 0) {
				for (int i = 1; i <= m; i++) {
					dy[i] += (da * dx[i]);
				}
				if (n < 4) {
					return;
				}
			}
			for (int i = m + 1; i <= n; i = i + 4) {
				dy[i] += (da * dx[i]);
				dy[i + 1] += (da * dx[i + 1]);
				dy[i + 2] += (da * dx[i + 2]);
				dy[i + 3] += (da * dx[i + 3]);
			}
			return;
		}

		/* Equal, positive and non-unit increments */
		for (int i = 1; i <= n * incx; i = i + incx) {
			dy[i] += (da * dx[i]);
		}
		return;
	}

	/**
	 * solves the linear system a * x = b or Transpose(a) * x = b using the
	 * factors computed by dgeco or degfa.
	 * 
	 * @param a
	 *            double matrix of dimension ( n+1, n+1 )
	 * @param n
	 *            number of rows in a.
	 * @param ipvt
	 *            the pivot vector from degco or dgefa.
	 * @param b
	 *            the right hand side vector, overwritten
	 * @param job
	 *            a flag to switch between a (job = 0) or Transpose(a) (job !=
	 *            0)
	 */
	private static void dgesl(double[][] a, int n, int[] ipvt, double[] b, int job) {
		int j;
		double t;

		/* Temporary variables for shifting array values */
		double[] at = new double[n + 1];
		double[] bt = new double[n + 1];

		/* Solve a * x = b */
		if (job == 0) {

			/* First solving L * y = b */
			for (int k = 1; k <= n; k++) {
				t = ddot(k - 1, a[k], 1, b, 1);
				b[k] = (b[k] - t) / a[k][k];
				if (Double.isNaN(b[k]) || Double.isInfinite(b[k]))
					System.out.println("NaN encountered.");
			}

			/* Now solving U * x = y */
			for (int k = n - 1; k >= 1; k--) {
				at = arrayShift(a[k], k);
				bt = arrayShift(b, k);
				b[k] += ddot(n - k, at, 1, bt, 1);
				j = ipvt[k];
				if (j != k) {
					t = b[j];
					b[j] = b[k];
					b[k] = t;
				}
			}
			return;
		}
		/* solve Transpose(a) * x = b. 
		 * Solving Transpose(U) * y = b. */
		for (int k = 1; k <= n - 1; k++) {
			j = ipvt[k];
			t = b[j];
			if (j != k) {
				b[j] = b[k];
				b[k] = t;
			}
			at = arrayShift(a[k], k);
			bt = arrayShift(b, k);
			daxpy(n - k, t, at, 1, bt, 1);
			b = arrayShift(bt, -k);
			a[k] = arrayShift(at, -k);
		}
		for (int k = n; k >= 1; k--) {
			b[k] = b[k] / a[k][k];
			t = -b[k];
			daxpy(k - 1, t, a[k], 1, b, 1);
		}
	}

	/**
	 * Factoring a double matrix by gaussian elimination
	 * 
	 * @param a
	 *            double matrix of ( n+1 X n+1 ), 0th row and column not used
	 * @param n
	 *            number of rows of a
	 * @param ipvt
	 *            n+1 integer vector of pivot indices
	 */
	public static void dgefa(double[][] a, int n, int[] ipvt) {
		int j;
		double t;
		double[] aTemp = new double[a[0].length];

		/* Gaussian elimination with partial pivoting. */
		ier = 0;
		for (int k = 1; k <= n - 1; k++) {
			/*
			 * Find j = pivot index. Note that a[k]+k-1 is the address of the
			 * 0th element of the row vector whose 1st element is a[k][k].
			 */
			j = getMax(n - k + 1, arrayShift(a[k], k - 1), 1) + k - 1;
			ipvt[k] = j;
			/* Zero pivot implies this row already triangularized. */
			if (a[k][j] == 0.) {
				ier = k;
				continue;
			}
			/* Interchange if necessary. */
			if (j != k) {
				t = a[k][j];
				a[k][j] = a[k][k];
				a[k][k] = t;
			}
			/* Compute multipliers. */
			t = -1. / a[k][k];
			aTemp = arrayShift(a[k], k);
			dscal(n - k, t, aTemp, 1);
			a[k] = arrayShift(aTemp, -k);
			/* Column elimination with row indexing. */
			for (int i = k + 1; i <= n; i++) {
				t = a[i][j];
				if (j != k) {
					a[i][j] = a[i][k];
					a[i][k] = t;
				}
				aTemp = arrayShift(a[i], k);
				daxpy(n - k, t, arrayShift(a[k], k), 1, aTemp, 1);
				a[i] = arrayShift(aTemp, -k);
			}
		}
		ipvt[n] = n;
		if (a[n][n] == 0.0) {
			ier = n;
		}
	}

	/**
	 * Terminate lsoda due to illegal input.
	 * 
	 */
	private static void terminate() {
		if (illin == 5) {
			System.err.println("[lsoda] repeated occurrence of illegal input. run aborted.. apparent infinite loop");
		} else {
			illin++;
			istate = -3;
		}
	}

	/**
	 * Terminate lsoda due to errors
	 * 
	 */
	private static void terminate2() {
		yp1 = yh[1];
		for (int i = 1; i <= n; i++) {
			y[i] = yp1[i];
		}
		t = tn;
		illin = 0;
		freevectors();
		return;
	}

	/**
	 * Successful return
	 * 
	 * @param itask
	 * @param ihit
	 * @param tcrit
	 */
	public static void successreturn(int itask, boolean ihit, double tcrit) {
		yp1 = yh[1];
		for (int i = 1; i <= n; i++) {
			y[i] = yp1[i];
		}
		t = tn;
		if (itask == 4 || itask == 5) {
			if (ihit) {
				t = tcrit;
			}
		}
		istate = 2;
		illin = 0;
		freevectors();
	}

	/**
	 * stoda performs one step of the integration of an initial value problem
	 * for a system of ordinary differential equations.
	 * 
	 * @param neq
	 * @param y
	 * @param data
	 */
	private static void stoda(int neq, Object data) {
		int i, i1, j, ncf;
		double delp = 0.0, dsm = 0, dup, exup, r, told;
		double pnorm;
		
		corflag = 0;

		kflag = 0;
		orderflag = 0;
		told = tn;
		ncf = 0;
		ierpj = 0;
		iersl = 0;
		jcur = 0;

		/*
		 * On the first call, the order is set to 1, and other variables are
		 * initialized. rmax is the maximum ratio by which h can be increased in
		 * a single step. It is initially 1.e4 to compensate for the small
		 * initial h, but then is normally equal to 10. If a filure occurs (in
		 * corrector convergence or error test), rmax is set at 2 for the next
		 * increase. cfode is called to get the needed coefficients for both
		 * methods.
		 */
		if (jstart == 0) {
			lmax = maxord + 1;
			nq = 1;
			l = 2;
			ialth = 2;
			rmax = 10000.;
			rc = 0.;
			el0 = 1.;
			crate = 0.7;
			hold = h;
			nslp = 0;
			ipup = miter;
			/*
			 * Initialize switching parameters. meth = 1 is assumed initially.
			 */
			icount = 20;
			irflag = 0;
			pdest = 0.;
			pdlast = 0.;
			ratio = 5.;
			cfode(2);
			for (i = 1; i <= 5; i++)
				cm2[i] = tesco[i][2] * elco[i][i + 1];
			cfode(1);
			for (i = 1; i <= 12; i++)
				cm1[i] = tesco[i][2] * elco[i][i + 1];
			resetcoeff();
		}
		/*
		 * The following block handles preliminaries needed when jstart = -1.
		 * ipup is set to miter to force a matrix update. If an order increase
		 * is about to be considered ( ialth = 1 ), ialth is reset to 2 to
		 * postpone consideration one more step. If the caller has changed meth,
		 * cfode is called to reset the coefficients of the method. If h is to
		 * be changed, yh must be rescaled. If h or meth is being changed, ialth
		 * is reset to l = nq + 1 to prevent further changes in h for that many
		 * steps.
		 */
		if (jstart == -1) {
			ipup = miter;
			lmax = maxord + 1;
			if (ialth == 1)
				ialth = 2;
			if (meth != mused) {
				cfode(meth);
				ialth = l;
				resetcoeff();
			}
			if (h != hold) {
				rh = h / hold;
				h = hold;
				scaleh();
			}
		}
		if (jstart == -2) {
			if (h != hold) {
				rh = h / hold;
				h = hold;
				scaleh();
			}
		}
		/*
		 * Prediction. This section computes the predicted values by effectively
		 * multiplying the yh array by the pascal triangle matrix. rc is the
		 * ratio of new to old values of the coefficient h * el[1]. When rc
		 * differs from 1 by more than ccmax, ipup is set to miter to force pjac
		 * to be called, if a jacobian is involved. In any case, prja is called
		 * at least every msbp steps.
		 */
		while (true) {
			while (true) {
				if (Math.abs(rc - 1.) > ccmax)
					ipup = miter;
				if (nst >= nslp + msbp)
					ipup = miter;
				tn += h;
				for (j = nq; j >= 1; j--)
					for (i1 = j; i1 <= nq; i1++) {
						yp1 = yh[i1];
						yp2 = yh[i1 + 1];
						for (i = 1; i <= n; i++)
							yp1[i] += yp2[i];
						yh[i1] = yp1;
					}
				pnorm = vmnorm(n, yh[1], ewt);

				correction(neq, pnorm, delp, told, ncf);
				if (corflag == 0)
					break;
				if (corflag == 1) {
					rh = Math.max(rh, hmin / Math.abs(h));
					scaleh();
					continue;
				}
				if (corflag == 2) {
					kflag = -2;
					hold = h;
					jstart = 1;
					return;
				}
			}
			/*
			 * The corrector has converged. jcur is set to 0 to signal that the
			 * Jacobian involved may need updating later. The local error test
			 * is done now.
			 */
			jcur = 0;
			if (m == 0)
				dsm = del / tesco[nq][2];
			if (m > 0)
				dsm = vmnorm(n, acor, ewt) / tesco[nq][2];
			if (dsm <= 1.) {
				/*
				 * After a successful step, update the yh array. Decrease icount
				 * by 1, and if it is -1, consider switching methods. If a
				 * method switch is made, reset various parameters, rescale the
				 * yh array, and exit. If there is no switch, consider changing
				 * h if ialth = 1. Otherwise decrease ialth by 1. If ialth is
				 * then 1 and nq < maxord, then acor is saved for use in a
				 * possible order increase on the next step. If a change in h is
				 * considered, an increase or decrease in order by one is
				 * considered also. A change in h is made only if it is by a
				 * factor of at least 1.1. If not, ialth is set to 3 to prevent
				 * testing for that many steps.
				 */
				kflag = 0;
				nst++;
				hu = h;
				nqu = nq;
				mused = meth;
				for (j = 1; j <= l; j++) {
					yp1 = yh[j];
					r = el[j];
					for (i = 1; i <= n; i++)
						yp1[i] += r * acor[i];
					yh[j] = yp1;
				}
				icount--;
				if (icount < 0) {
					methodSwitch(dsm, pnorm);
					if (meth != mused) {
						rh = Math.max(rh, hmin / Math.abs(h));
						scaleh();
						rmax = 10.;
						endstoda();
						break;
					}
				}
				/*
				 * No method switch is being made. Do the usual step/order
				 * selection.
				 */
				ialth--;
				if (ialth == 0) {
					rhup = 0.;
					if (l != lmax) {
						yp1 = yh[lmax];
						for (i = 1; i <= n; i++)
							savf[i] = acor[i] - yp1[i];
						if (Double.isNaN(savf[1]) || Double.isNaN(savf[2]) || Double.isNaN(savf[3])
								|| Double.isInfinite(savf[1]) || Double.isInfinite(savf[2]) || Double.isInfinite(savf[3]))
							System.out.println("NaN encountered.");
						dup = vmnorm(n, savf, ewt) / tesco[nq][3];
						exup = 1. / (double) (l + 1);
						rhup = 1. / (1.4 * Math.pow(dup, exup) + 0.0000014);
					}
					orderswitch(dsm);
					/* No change in h or nq. */
					if (orderflag == 0) {
						endstoda();
						break;
					}
					/* h is changed, but not nq. */
					if (orderflag == 1) {
						rh = Math.max(rh, hmin / Math.abs(h));
						scaleh();
						rmax = 10.;
						endstoda();
						break;
					}
					/* both nq and h are changed. */
					if (orderflag == 2) {
						resetcoeff();
						rh = Math.max(rh, hmin / Math.abs(h));
						scaleh();
						rmax = 10.;
						endstoda();
						break;
					}
				}
				if (ialth > 1 || l == lmax) {
					endstoda();
					break;
				}
				yp1 = yh[lmax];
				for (i = 1; i <= n; i++)
					yp1[i] = acor[i];
				yh[lmax] = yp1;
				endstoda();
				break;
			}
			/*
			 * The error test failed. kflag keeps track of multiple failures.
			 * Restore tn and the yh array to their previous values, and prepare
			 * to try the step again. Compute the optimum step size for this or
			 * one lower. After 2 or more failures, h is forced to decrease by a
			 * factor of 0.2 or less.
			 */
			else {
				kflag--;
				tn = told;
				for (j = nq; j >= 1; j--)
					for (i1 = j; i1 <= nq; i1++) {
						yp1 = yh[i1];
						yp2 = yh[i1 + 1];
						for (i = 1; i <= n; i++)
							yp1[i] -= yp2[i];
						yh[i1] = yp1;
					}
				rmax = 2.;
				if (Math.abs(h) <= hmin * 1.00001) {
					kflag = -1;
					hold = h;
					jstart = 1;
					break;
				}
				if (kflag > -3) {
					rhup = 0.;
					orderswitch(dsm);
					if (orderflag == 1 || orderflag == 0) {
						if (orderflag == 0)
							rh = Math.min(rh, 0.2);
						rh = Math.max(rh, hmin / Math.abs(h));
						scaleh();
					}
					if (orderflag == 2) {
						resetcoeff();
						rh = Math.max(rh, hmin / Math.abs(h));
						scaleh();
					}
					continue;
				}
				/*
				 * Control reaches this section if 3 or more failures have
				 * occurred. If 10 failures have occurred, exit with kflag = -1.
				 * It is assumed that the derivatives that have accumulated in
				 * the yh array have errors of the wrong order. Hence the first
				 * derivative is recomputed, and the order is set to 1. Then h
				 * is reduced by a factor of 10, and the step is retried, until
				 * it succeeds or h reaches hmin.
				 */
				else {
					if (kflag == -10) {
						kflag = -1;
						hold = h;
						jstart = 1;
						break;
					} else {
						rh = 0.1;
						rh = Math.max(hmin / Math.abs(h), rh);
						h *= rh;
						yp1 = yh[1];
						for (i = 1; i <= n; i++)
							y[i] = yp1[i];
						/* Call to function */
						double[] savfTemp = arrayShift(savf, 1);
						LsodaTest.eqns(arrayShift(y, 1), savfTemp);
						savf = arrayShift(savfTemp, -1);
						if (Double.isNaN(savf[1]) || Double.isNaN(savf[2]) || Double.isNaN(savf[3])
								|| Double.isInfinite(savf[1]) || Double.isInfinite(savf[2]) || Double.isInfinite(savf[3]))
							System.out.println("NaN encountered.");
						nfe++;
						yp1 = yh[2];
						for (i = 1; i <= n; i++)
							yp1[i] = h * savf[i];
						yh[2] = yp1;
						ipup = miter;
						ialth = 5;
						if (nq == 1)
							continue;
						nq = 1;
						l = 2;
						resetcoeff();
						continue;
					}
				}
			}
		}
	}

	public static void ewset(int itol, double[] rtol, double[] atol, double[] ycur) {
		switch (itol) {
		case 1:
			for (int i = 1; i <= n; i++)
				ewt[i] = rtol[1] * Math.abs(ycur[i]) + atol[1];
			break;
		case 2:
			for (int i = 1; i <= n; i++)
				ewt[i] = rtol[1] * Math.abs(ycur[i]) + atol[i];
			break;
		case 3:
			for (int i = 1; i <= n; i++)
				ewt[i] = rtol[i] * Math.abs(ycur[i]) + atol[1];
			break;
		case 4:
			for (int i = 1; i <= n; i++)
				ewt[i] = rtol[i] * Math.abs(ycur[i]) + atol[i];
			break;
		}
	}

	/**
	 * Intdy computes interpolated values of the k-th derivative of the
	 * dependent variable vector y, and stores it in dky. This routine is called
	 * within the package with k = 0 and *t = tout, but may also be called by
	 * the user for any k up to the current order.
	 * 
	 * @param t
	 * @param k
	 * @param dky
	 * @param iflag
	 */
	public static void intdy(double t, int k, double[] dky, int iflag) {
		int ic, jp1;
		double c, r, s, tp;

		iflag = 0;
		if (k < 0 || k > nq) {
			System.err.println("[intdy] k = " + k + " illegal");
			iflag = -1;
			return;
		}
		tp = tn - hu - 100. * ETA * (tn + hu);
		if ((t - tp) * (t - tn) > 0.) {
			System.err.println("intdy -- t = " + t + " illegal. t not in interval tcur - hu to tcur");
			iflag = -2;
			return;
		}
		s = (t - tn) / h;
		ic = 1;
		for (int jj = l - k; jj <= nq; jj++)
			ic *= jj;
		c = (double) ic;
		yp1 = yh[l];
		for (int i = 1; i <= n; i++)
			dky[i] = c * yp1[i];
		for (int j = nq - 1; j >= k; j--) {
			jp1 = j + 1;
			ic = 1;
			for (int jj = jp1 - k; jj <= j; jj++)
				ic *= jj;
			c = (double) ic;
			yp1 = yh[jp1];
			for (int i = 1; i <= n; i++)
				dky[i] = c * yp1[i] + s * dky[i];
		}
		if (k == 0)
			return;
		r = Math.pow(h, (double) (-k));
		for (int i = 1; i <= n; i++)
			dky[i] *= r;
	}

	/**
	 * cfode is called by the integrator routine to set coefficients needed
	 * there. The coefficients for the current method, as given by the value of
	 * meth, are set for all orders and saved. The maximum order assumed here is
	 * 12 if meth = 1 and 5 if meth = 2. ( A smaller value of the maximum order
	 * is also allowed. ) cfode is called once at the beginning of the problem,
	 * and is not called again unless and until meth is changed.
	 * 
	 * @param meth
	 */
	private static void cfode(int meth) {
		int nqm1, nqp1;
		double agamq, fnq, fnqm1, pint, ragq, rqfac, rq1fac, tsign, xpin;
		double[] pc = new double[13];

		if (meth == 1) {
			elco[1][1] = 1.;
			elco[1][2] = 1.;
			tesco[1][1] = 0.;
			tesco[1][2] = 2.;
			tesco[2][1] = 1.;
			tesco[12][3] = 0.;
			pc[1] = 1.;
			rqfac = 1.;
			for (int nq = 2; nq <= 12; nq++) {
				/*
				 * The pc array will contain the coefficients of the polynomial
				 * p(x) = (x+1)*(x+2)*...*(x+nq-1). Initially, p(x) = 1.
				 */
				rq1fac = rqfac;
				rqfac = rqfac / (double) nq;
				nqm1 = nq - 1;
				fnqm1 = (double) nqm1;
				nqp1 = nq + 1;
				/* Form coefficients of p(x)*(x+nq-1). */
				pc[nq] = 0.;
				for (int i = nq; i >= 2; i--)
					pc[i] = pc[i - 1] + fnqm1 * pc[i];
				pc[1] = fnqm1 * pc[1];
				/* Compute integral, -1 to 0, of p(x) and x*p(x). */
				pint = pc[1];
				xpin = pc[1] / 2.;
				tsign = 1.;
				for (int i = 2; i <= nq; i++) {
					tsign = -tsign;
					pint += tsign * pc[i] / (double) i;
					xpin += tsign * pc[i] / (double) (i + 1);
				}
				/* Store coefficients in elco and tesco. */
				elco[nq][1] = pint * rq1fac;
				elco[nq][2] = 1.;
				for (int i = 2; i <= nq; i++)
					elco[nq][i + 1] = rq1fac * pc[i] / (double) i;
				agamq = rqfac * xpin;
				ragq = 1. / agamq;
				tesco[nq][2] = ragq;
				if (nq < 12)
					tesco[nqp1][1] = ragq * rqfac / (double) nqp1;
				tesco[nqm1][3] = ragq;
			}
			return;
		}
		/* meth = 2. */
		pc[1] = 1.;
		rq1fac = 1.;
		/*
		 * The pc array will contain the coefficients of the polynomial
		 * 
		 * p(x) = (x+1)*(x+2)*...*(x+nq).
		 * 
		 * Initially, p(x) = 1.
		 */
		for (int nq = 1; nq <= 5; nq++) {
			fnq = (double) nq;
			nqp1 = nq + 1;
			/* Form coefficients of p(x)*(x+nq). */
			pc[nqp1] = 0.;
			for (int i = nq + 1; i >= 2; i--)
				pc[i] = pc[i - 1] + fnq * pc[i];
			pc[1] *= fnq;
			/* Store coefficients in elco and tesco. */
			for (int i = 1; i <= nqp1; i++)
				elco[nq][i] = pc[i] / pc[2];
			elco[nq][2] = 1.;
			tesco[nq][1] = rq1fac;
			tesco[nq][2] = ((double) nqp1) / elco[nq][1];
			tesco[nq][3] = ((double) (nq + 2)) / elco[nq][1];
			rq1fac /= fnq;
		}
		return;
	}

	private static void scaleh()
	{
		double r;
		/*
		 * If h is being changed, the h ratio rh is checked against rmax, hmin,
		 * and hmxi, and the yh array is rescaled. ialth is set to l = nq + 1 to
		 * prevent a change of h for that many steps, unless forced by a
		 * convergence or error test failure.
		 */
		rh = Math.min(rh, rmax);
		rh = rh / Math.max(1., Math.abs(h) * hmxi * rh);
		/*
		 * If meth = 1, also restrict the new step size by the stability region.
		 * If this reduces h, set irflag to 1 so that if there are roundoff
		 * problems later, we can assume that is the cause of the trouble.
		 */
		if (meth == 1) {
			irflag = 0;
			pdh = Math.max(Math.abs(h) * pdlast, 0.000001);
			if ((rh * pdh * 1.00001) >= sm1[nq]) {
				rh = sm1[nq] / pdh;
				irflag = 1;
			}
		}
		r = 1.;
		for (int j = 2; j <= l; j++) {
			r *= rh;
			yp1 = yh[j];
			for (int i = 1; i <= n; i++)
				yp1[i] *= r;
			yh[j] = yp1;
		}
		h *= rh;
		rc *= rh;
		ialth = l;
	}

	/**
	 * prja is called by stoda to compute and process the matrix P = I - h *
	 * el[1] * J, where J is an approximation to the Jacobian.
	 * 
	 * @param neq
	 * @param y
	 */
	private static void prja(int neq) {
		double fac, hl0, r, r0, yj;
		nje++;
		ierpj = 0;
		jcur = 1;
		hl0 = h * el0;
		/* If miter = 2, make n calls to f to approximate J. */
		if (miter != 2) {
			System.err.println("[prja] miter != 2");
			return;
		}
		if (miter == 2) {
			fac = vmnorm(n, savf, ewt);
			r0 = 1000. * Math.abs(h) * ETA * ((double) n) * fac;
			if (r0 == 0.)
				r0 = 1.;
			for (int j = 1; j <= n; j++) {
				yj = y[j];
				r = Math.max(sqrteta * Math.abs(yj), r0 / ewt[j]);
				y[j] += r;
				fac = -hl0 / r;
				double[] acorTemp = arrayShift(acor, 1);
				if (Double.isNaN(acorTemp[1]) || Double.isNaN(acorTemp[2]) || Double.isNaN(acorTemp[3])
						|| Double.isInfinite(acor[1]) || Double.isInfinite(acor[2]) || Double.isInfinite(acor[3]))
					System.out.println("Got NaN");
				LsodaTest.eqns(arrayShift(y, 1), acorTemp);
				acor = arrayShift(acorTemp, -1);
				if (Double.isNaN(acor[1]) || Double.isNaN(acor[2]) || Double.isNaN(acor[3]) 
						|| Double.isInfinite(acor[1]) || Double.isInfinite(acor[2]) || Double.isInfinite(acor[3]))
					System.out.println("Got NaN");
				for (int i = 1; i <= n; i++)
					wm[i][j] = (acor[i] - savf[i]) * fac;
				y[j] = yj;
			}
			nfe += n;
			/* Compute norm of Jacobian. */
			pdnorm = fnorm(n, wm, ewt) / Math.abs(hl0);
			/* Add identity matrix. */
			for (int i = 1; i <= n; i++)
				wm[i][i] += 1.;
			/* Do LU decomposition on P. */
			dgefa(wm, n, ipvt);
			if (ier != 0)
				ierpj = 1;
			return;
		}
	}

	/**
	 * computes the weighted max-norm of the vector of length n contained in the
	 * array v, with weights contained in the array w of length n.
	 * 
	 * @param n
	 * @param v
	 * @param w
	 * @return
	 */
	private static double vmnorm(int n, double[] v, double[] w) {
		double vm;
		vm = 0.0;
		for (int i = 1; i <= n; i++)
			vm = Math.max(vm, Math.abs(v[i]) * w[i]);
		return vm;
	}

	/**
	 * computes the norm of a full n by n matrix, stored in the array a, that is
	 * consistent with the weighted max-norm on vectors, with weights stored in
	 * the array w.
	 * 
	 * @param n
	 * @param a
	 * @param w
	 * @return
	 */
	private static double fnorm(int n, double[][] a, double[] w) {
		double an, sum;
		double[] ap1 = new double[Matrix.rowDim(a)];

		an = 0.;
		for (int i = 1; i <= n; i++) {
			sum = 0.;
			ap1 = a[i];
			for (int j = 1; j <= n; j++)
				sum += Math.abs(ap1[j]) / w[j];
			an = Math.max(an, sum * w[i]);
		}
		return an;
	}

	/**
	 * corflag = 0 : corrector converged, 1 : step size to be reduced, redo
	 * prediction, 2 : corrector cannot converge, failure flag.
	 * 
	 * @param neq
	 * @param pnorm
	 * @param delp
	 * @param told
	 * @param ncf
	 */
	public static void correction(int neq, double pnorm, double delp, double told, int ncf)
	{
		double rm, rate, dcon;

		/*
		 * Up to maxcor corrector iterations are taken. A convergence test is
		 * made on the r.m.s. norm of each correction, weighted by the error
		 * weight vector ewt. The sum of the corrections is accumulated in the
		 * vector acor[i]. The yh array is not altered in the corrector loop.
		 */
		m = 0;
		corflag = 0;
		rate = 0.;
		del = 0.;
		yp1 = yh[1];
		for (int i = 1; i <= n; i++)
			y[i] = yp1[i];
		double[] savfTemp = arrayShift(savf, 1);
		LsodaTest.eqns(arrayShift(y, 1), savfTemp);
		savf = arrayShift(savfTemp, -1);
		if (Double.isNaN(savf[1]) || Double.isNaN(savf[2]) || Double.isNaN(savf[3]) 
				|| Double.isInfinite(savf[1]) || Double.isInfinite(savf[2]) || Double.isInfinite(savf[3]))
			System.out.println("NaN encountered.");
		nfe++;
		/*
		 * If indicated, the matrix P = I - h * el[1] * J is reevaluated and
		 * preprocessed before starting the corrector iteration. ipup is set to
		 * 0 as an indicator that this has been done.
		 */
		while (true) {
			if (m == 0) {
				if (ipup > 0) {
					prja(neq);
					ipup = 0;
					rc = 1.;
					nslp = nst;
					crate = 0.7;
					if (ierpj != 0) {
						corfailure(told, ncf);
						return;
					}
				}
				for (int i = 1; i <= n; i++)
					acor[i] = 0.;
				if (Double.isNaN(acor[1]) || Double.isNaN(acor[2]) || Double.isNaN(acor[3])
						|| Double.isInfinite(acor[1]) || Double.isInfinite(acor[2]) || Double.isInfinite(acor[3]))
					System.out.println("NaN encountered.");
			}
			if (miter == 0) {
				/*
				 * In case of functional iteration, update y directly from the
				 * result of the last function evaluation.
				 */
				yp1 = yh[2];
				for (int i = 1; i <= n; i++) {
					savf[i] = h * savf[i] - yp1[i];
					if (Double.isNaN(savf[i]) || Double.isInfinite(y[i]))
						System.out.println("NaN encountered.");
					y[i] = savf[i] - acor[i];
				}
				del = vmnorm(n, y, ewt);
				yp1 = yh[1];
				for (int i = 1; i <= n; i++) {
					y[i] = yp1[i] + el[1] * savf[i];
					acor[i] = savf[i];
					if (Double.isNaN(savf[i]) || Double.isInfinite(y[i]))
						System.out.println("NaN encountered.");
				}
			}
			/*
			 * In the case of the chord method, compute the corrector error, and
			 * solve the linear system with that as right-hand side and P as
			 * coefficient matrix.
			 */
			else {
				yp1 = yh[2];
				for (int i = 1; i <= n; i++)
					y[i] = h * savf[i] - (yp1[i] + acor[i]);
				if (Double.isNaN(y[1]) || Double.isNaN(y[2]) || Double.isNaN(y[3])
						|| Double.isInfinite(y[1]) || Double.isInfinite(y[2]) || Double.isInfinite(y[3]))
					System.out.println("NaN encountered.");
				solsy();
				if (Double.isNaN(y[1]) || Double.isNaN(y[2]) || Double.isNaN(y[3])
						|| Double.isInfinite(y[1]) || Double.isInfinite(y[2]) || Double.isInfinite(y[3]))
					System.out.println("NaN encountered.");
				del = vmnorm(n, y, ewt);
				yp1 = yh[1];
				for (int i = 1; i <= n; i++) {
					acor[i] += y[i];
					if (Double.isNaN(y[i]) || Double.isInfinite(y[i]))
						System.out.println("NaN encountered.");
					y[i] = yp1[i] + el[1] * acor[i];
				}
			}
			/*
			 * Test for convergence. If *m > 0, an estimate of the convergence
			 * rate constant is stored in crate, and this is used in the test.
			 * 
			 * We first check for a change of iterates that is the size of
			 * roundoff error. If this occurs, the iteration has converged, and
			 * a new rate estimate is not formed. In all other cases, force at
			 * least two iterations to estimate a local Lipschitz constant
			 * estimate for Adams method. On convergence, form pdest = local
			 * maximum Lipschitz constant estimate. pdlast is the most recent
			 * nonzero estimate.
			 */
			if (del <= 100. * pnorm * ETA)
				break;
			if (m != 0 || meth != 1) {
				if (m != 0) {
					rm = 1024.0;
					if (del <= (1024. * delp))
						rm = del / delp;
					rate = Math.max(rate, rm);
					crate = Math.max(0.2 * crate, rm);
				}
				dcon = del * Math.min(1., 1.5 * crate) / (tesco[nq][2] * conit);
				if (dcon <= 1.) {
					pdest = Math.max(pdest, rate / Math.abs(h * el[1]));
					if (pdest != 0.)
						pdlast = pdest;
					break;
				}
			}
			/*
			 * The corrector iteration failed to converge. If miter != 0 and the
			 * Jacobian is out of date, prja is called for the next try.
			 * Otherwise the yh array is retracted to its values before
			 * prediction, and h is reduced, if possible. If h cannot be reduced
			 * or mxncf failures have occured, exit with corflag = 2.
			 */
			m++;
			if (m == maxcor || (m >= 2 && del > 2.0 * delp)) {
				if (miter == 0 || jcur == 1) {
					corfailure(told, ncf);
					return;
				}
				ipup = miter;
				/* Restart corrector if Jacobian is recomputed. */
				m = 0;
				rate = 0.;
				del = 0.;
				yp1 = yh[1];
				for (int i = 1; i <= n; i++)
					y[i] = yp1[i];
				savfTemp = arrayShift(savf, 1);
				LsodaTest.eqns(arrayShift(y, 1), savfTemp);
				savf = arrayShift(savfTemp, -1);
				nfe++;
			}
			/* Iterate corrector. */
			else {
				delp = del;
				savfTemp = arrayShift(savf, 1);
				LsodaTest.eqns(arrayShift(y, 1), savfTemp);
				savf = arrayShift(savfTemp, -1);
				nfe++;
			}
		}
	}

	/**
	 * 
	 * @param told
	 * @param ncf
	 */
	private static void corfailure(double told, int ncf) {
		ncf++;
		rmax = 2.;
		tn = told;
		for (int j = nq; j >= 1; j--)
			for (int i1 = j; i1 <= nq; i1++) {
				yp1 = yh[i1];
				yp2 = yh[i1 + 1];
				for (int i = 1; i <= n; i++)
					yp1[i] -= yp2[i];
				yh[i1] = yp1;
			}
		if (Math.abs(h) <= hmin * 1.00001 || ncf == mxncf) {
			corflag = 2;
			return;
		}
		corflag = 1;
		rh = 0.25;
		ipup = miter;
	}

	/**
	 * manages the solution of the linear system arising from a chord iteration.
	 * It is called if miter != 0.
	 * 
	 */
	private static void solsy() {
		iersl = 0;
		if (miter != 2) {
			System.out.println("solsy -- miter != 2\n");
			return;
		}
		if (miter == 2)
			dgesl(wm, n, ipvt, y, 0);
		return;
	}

	/**
	 * We are current using an Adams method. Consider switching to bdf. If the
	 * current order is greater than 5, assume the problem is not stiff, and
	 * skip this section. If the Lipschitz constant and error estimate are not
	 * polluted by roundoff, perform the usual test. Otherwise, switch to the
	 * bdf methods if the last step was restricted to insure stability ( irflag
	 * = 1 ), and stay with Adams method if not. When switching to bdf with
	 * polluted error estimates, in the absence of other information, double the
	 * step size.
	 * 
	 * @param dsm
	 * @param pnorm
	 */
	private static void methodSwitch(double dsm, double pnorm) {
		int lm1, lm1p1, lm2, lm2p1, nqm1, nqm2;
		double rh1, rh2, rh1it, exm2, dm2, exm1, dm1, alpha, exsm;
		if (meth == 1) {
			if (nq > 5)
				return;
			if (dsm <= (100. * pnorm * ETA) || pdest == 0.) {
				if (irflag == 0)
					return;
				rh2 = 2.;
				nqm2 = Math.min(nq, mxords);
			} else {
				exsm = 1. / (double) l;
				rh1 = 1. / (1.2 * Math.pow(dsm, exsm) + 0.0000012);
				rh1it = 2. * rh1;
				pdh = pdlast * Math.abs(h);
				if ((pdh * rh1) > 0.00001)
					rh1it = sm1[nq] / pdh;
				rh1 = Math.min(rh1, rh1it);
				if (nq > mxords) {
					nqm2 = mxords;
					lm2 = mxords + 1;
					exm2 = 1. / (double) lm2;
					lm2p1 = lm2 + 1;
					dm2 = vmnorm(n, yh[lm2p1], ewt) / cm2[mxords];
					rh2 = 1. / (1.2 * Math.pow(dm2, exm2) + 0.0000012);
				} else {
					dm2 = dsm * (cm1[nq] / cm2[nq]);
					rh2 = 1. / (1.2 * Math.pow(dm2, exsm) + 0.0000012);
					nqm2 = nq;
				}
				if (rh2 < ratio * rh1)
					return;
			}
			/*
			 * The method switch test passed. Reset relevant quantities for bdf.
			 */
			rh = rh2;
			icount = 20;
			meth = 2;
			miter = jtyp;
			pdlast = 0.;
			nq = nqm2;
			l = nq + 1;
			return;
		}
		/*
		 * We are currently using a bdf method, considering switching to Adams.
		 * Compute the step size we could have (ideally) used on this step, with
		 * the current (bdf) method, and also that for the Adams. If nq >
		 * mxordn, we consider changing to order mxordn on switching. Compare
		 * the two step sizes to decide whether to switch. The step size
		 * advantage must be at least 5/ratio = 1 to switch. If the step size
		 * for Adams would be so small as to cause roundoff pollution, we stay
		 * with bdf.
		 */
		exsm = 1. / (double) l;
		if (mxordn < nq) {
			nqm1 = mxordn;
			lm1 = mxordn + 1;
			exm1 = 1. / (double) lm1;
			lm1p1 = lm1 + 1;
			dm1 = vmnorm(n, yh[lm1p1], ewt) / cm1[mxordn];
			rh1 = 1. / (1.2 * Math.pow(dm1, exm1) + 0.0000012);
		} else {
			dm1 = dsm * (cm2[nq] / cm1[nq]);
			rh1 = 1. / (1.2 * Math.pow(dm1, exsm) + 0.0000012);
			nqm1 = nq;
			exm1 = exsm;
		}
		rh1it = 2. * rh1;
		pdh = pdnorm * Math.abs(h);
		if ((pdh * rh1) > 0.00001)
			rh1it = sm1[nqm1] / pdh;
		rh1 = Math.min(rh1, rh1it);
		rh2 = 1. / (1.2 * Math.pow(dsm, exsm) + 0.0000012);
		if ((rh1 * ratio) < (5. * rh2))
			return;
		alpha = Math.max(0.001, rh1);
		dm1 *= Math.pow(alpha, exm1);
		if (dm1 <= 1000. * ETA * pnorm)
			return;
		/* The switch test passed. Reset relevant quantities for Adams. */
		rh = rh1;
		icount = 20;
		meth = 1;
		pdlast = 0.;
		nq = nqm1;
		l = nq + 1;
	}

	/**
	 * Returns from stoda to lsoda. Hence freevectors() is not executed.
	 */
	private static void endstoda() {
		double r = 1. / tesco[nqu][2];
		for (int i = 1; i <= n; i++)
			acor[i] *= r;
		if (Double.isNaN(acor[1]) || Double.isNaN(acor[2]) || Double.isNaN(acor[3])
				|| Double.isInfinite(acor[1]) || Double.isInfinite(acor[2]) || Double.isInfinite(acor[3]))
			System.out.println("NaN encountered.");
		hold = h;
		jstart = 1;
	}

	/**
	 * Regardless of the success or failure of the step, factors rhdn, rhsm, and
	 * rhup are computed, by which h could be multiplied at order nq - 1, order
	 * nq, or order nq + 1, respectively. In the case of a failure, rhup = 0. to
	 * avoid an order increase. The largest of these is determined and the new
	 * order chosen accordingly. If the order is to be increased, we compute one
	 * additional scaled derivative.
	 * 
	 * @param rhup
	 * @param dsm
	 */
	private static void orderswitch(double dsm) {
		int newq;
		double exsm, rhdn, rhsm, ddn, exdn, r;

		orderflag = 0;

		exsm = 1. / (double) l;
		rhsm = 1. / (1.2 * Math.pow(dsm, exsm) + 0.0000012);

		rhdn = 0.;
		if (nq != 1) {
			ddn = vmnorm(n, yh[l], ewt) / tesco[nq][1];
			exdn = 1. / (double) nq;
			rhdn = 1. / (1.3 * Math.pow(ddn, exdn) + 0.0000013);
		}
		/* If meth = 1, limit rh according to the stability region also. */
		if (meth == 1) {
			pdh = Math.max(Math.abs(h) * pdlast, 0.000001);
			if (l < lmax)
				rhup = Math.min(rhup, sm1[l] / pdh);
			rhsm = Math.min(rhsm, sm1[nq] / pdh);
			if (nq > 1)
				rhdn = Math.min(rhdn, sm1[nq - 1] / pdh);
			pdest = 0.;
		}
		if (rhsm >= rhup) {
			if (rhsm >= rhdn) {
				newq = nq;
				rh = rhsm;
			} else {
				newq = nq - 1;
				rh = rhdn;
				if (kflag < 0 && rh > 1.)
					rh = 1.;
			}
		} else {
			if (rhup <= rhdn) {
				newq = nq - 1;
				rh = rhdn;
				if (kflag < 0 && rh > 1.)
					rh = 1.;
			} else {
				rh = rhup;
				if (rh >= 1.1) {
					r = el[l] / (double) l;
					nq = l;
					l = nq + 1;
					yp1 = yh[l];
					for (int i = 1; i <= n; i++)
						yp1[i] = acor[i] * r;
					yh[l] = yp1;
					orderflag = 2;
					return;
				} else {
					ialth = 3;
					return;
				}
			}
		}
		/*
		 * If meth = 1 and h is restricted by stability, bypass 10 percent test.
		 */
		if (meth == 1) {
			if ((rh * pdh * 1.00001) < sm1[newq])
				if (kflag == 0 && rh < 1.1) {
					ialth = 3;
					return;
				}
		} else {
			if (kflag == 0 && rh < 1.1) {
				ialth = 3;
				return;
			}
		}
		if (kflag <= -2)
			rh = Math.min(rh, 0.2);
		/*
		 * If there is a change of order, reset nq, l, and the coefficients. In
		 * any case h is reset according to rh and the yh array is rescaled.
		 * Then exit or redo the step.
		 */
		if (newq == nq) {
			orderflag = 1;
			return;
		}
		nq = newq;
		l = nq + 1;
		orderflag = 2;
	}

	/**
	 * The el vector and related constants are reset whenever the order nq is
	 * changed, or at the start of the problem.
	 */
	private static void resetcoeff() {
		double[] ep1 = elco[nq];
		for (int i = 1; i <= l; i++)
			el[i] = ep1[i];
		rc = rc * el[1] / el0;
		el0 = el[1];
		conit = 0.5 / (double) (nq + 2);
	}

	private static void freevectors() {
	}

	public static void _freevectors() {
		if (wm != null)
			for (int i = 1; i <= gNyh; ++i)
				wm[i] = null;
		if (yh != null)
			for (int i = 1; i <= gLenyh; ++i)
				yh[i] = null;
		gNyh = gLenyh = 0;
		yh = null;
		wm = null;
		ewt = null;
		savf = null;
		acor = null;
		ipvt = null;
	}

	/**
	 * This function shifts the contents of an array by the specified integer
	 * amount and fills the values not found as 0
	 * 
	 * [1, 2, 3, 4, 5, 6, 7] shifted by 2 will give [3, 4, 5, 6, 7, 0, 0]
	 * [1, 2, 3, 4, 5, 6, 7] shifted by -2 will give [0, 0, 1, 2, 3, 4, 5]
	 * 
	 * @param arr
	 *            array whose contents need to shifted left
	 * @param by
	 *            the integer amount by which the array contents are shifted
	 * @return a new array after the shift
	 */
	public static double[] arrayShift(double[] arr, int by) {
		double[] brr = new double[arr.length];
		if (by >= 0) {
			for (int i = 0; i < arr.length; i++) {
				if (arr.length > i + by)
					brr[i] = arr[i + by];
				else
					brr[i] = 0;
			}
		}
		else
		{
			by = Math.abs(by);
			for (int i = arr.length - 1; i >= 0; i--) {
				if (i >= by)
					brr[i] = arr[i - by];
				else
					brr[i] = 0;
			}
		}
		return brr;
	}

}