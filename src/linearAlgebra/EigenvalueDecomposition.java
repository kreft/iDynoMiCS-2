/**
 * 
 */
package linearAlgebra;

import utility.ExtraMath;

/**
 * \brief Eigenvalues and eigenvectors of a real matrix.
 * 
 * <p>If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is
 * diagonal and the eigenvector matrix V is orthogonal.</p>
 * 
 * <p>If A is not symmetric, then the eigenvalue matrix D is block diagonal
 * with the real eigenvalues in 1-by-1 blocks and any complex eigenvalues,
 * lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns of V
 * represent the eigenvectors in the sense that A*V = V*D. The matrix V may be
 * badly conditioned, or even singular, so the validity of the equation
 * A = V*D*inverse(V) depends upon V.cond().</p>
 * 
 * <p>This class is largely taken from the JAMA package, and merely modified by
 * Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.</p>
 * 
 * <p><b>JAMA Copyright Notice</b>: This software is a cooperative product of 
 * The MathWorks and the National Institute of Standards and Technology (NIST) 
 * which has been released to the public domain. Neither The MathWorks nor NIST
 * assumes any responsibility whatsoever for its use by other parties, and 
 * makes no guarantees, expressed or implied, about its quality, reliability, 
 * or any other characteristic.</p>
 */
//TODO replace complex parts with methods from utility.Complex?
public class EigenvalueDecomposition
{
	/**
	 * Machine epsilon ( https://en.wikipedia.org/wiki/Machine_epsilon )
	 */
	private static final double EPS = Math.pow(2.0, -52.0);
	/**
	 * Row and column dimension (square matrix).
	 */
	private int n;
	/**
	 * Symmetry flag.
	 */
	private boolean _isSymmetric;
	/**
	 * Real parts of the eigenvalues.
	 */
	private double[] d;
	/**
	 * Imaginary parts of the eigenvalues.
	 */
	private double[] e;
	/**
	 * Array for internal storage of eigenvectors.
	 */
	private double[][] V;
	/**
	 * Array for internal storage of non-symmetric Hessenberg form.
	 */
	private double[][] H;
	/**
	 * Real and imaginary parts of a complex division.
	 */
	private double cDivR, cDivI;
	
	/*************************************************************************
	 * CONSTRUCTOR
	 ************************************************************************/

	/**
	 * @param matrix Square matrix of {@code double} values.
	 */
	public EigenvalueDecomposition(double[][] A)
	{
		Matrix.checkSquare(A);

		this.n = Matrix.colDim(A);
		this.V = Matrix.zerosDbl(this.n);
		this.d = Vector.zerosDbl(this.n);
		this.e = Vector.zerosDbl(this.n);

		this._isSymmetric = Matrix.isSymmetric(A);

		if ( this._isSymmetric )
		{
			Matrix.copyTo(V, A);
			/* Tri-diagonalize. */
			this.tred2();
			/* Diagonalize. */
			this.tql2();
		}
		else
		{
			this.H = Matrix.copy(A);
			/* Reduce to Hessenberg form. */
			this.orthes();
			/* Reduce Hessenberg to real Schur form. */
			this.hqr2();
		}
	}

	/*************************************************************************
	 * PUBLIC METHODS
	 ************************************************************************/

	/**
	 * @return The eigenvector matrix.
	 */
	public double[][] getV()
	{
		return this.V;
	}

	/**
	 * @return The real parts of the eigenvalues.
	 */
	public double[] getRealEigenvalues()
	{
		return this.d;
	}

	/**
	 * @return The imaginary parts of the eigenvalues.
	 */
	public double[] getImagEigenvalues()
	{
		return this.e;
	}

	/** 
	 * @return The block diagonal eigenvalue matrix.
	 */
	public double[][] getD()
	{
		double[][] D = Matrix.zerosDbl(this.n);
		for (int i = 0; i < this.n; i++)
		{
			D[i][i] = this.d[i];
			if ( this.e[i] > 0 )
				D[i][i+1] = e[i];
			else if ( this.e[i] < 0)
				D[i][i-1] = e[i];
		}
		return D;
	}

	/*************************************************************************
	 * PRIVATE METHODS
	 ************************************************************************/

	/**
	 * \brief Symmetric Householder reduction to tridiagonal form.
	 * 
	 * <p>This is derived from the Algol procedure tred2 by Bowdler, Martin,
	 * Reinsch, and Wilkinson, Handbook for Auto. Comp., Vol.ii-Linear Algebra,
	 * and the corresponding Fortran subroutine in EISPACK.</p>
	 */
	private void tred2 ()
	{
		this.d = Matrix.getColumn(this.V, this.n-1);
		/* 
		 * Householder reduction to tridiagonal form.
		 */
		for (int i = this.n-1; i > 0; i--)
		{
			/* Scale to avoid under/overflow. */
			double scale = 0.0;
			double h = 0.0;
			for (int k = 0; k < i; k++)
				scale = scale + Math.abs(this.d[k]);
			if ( scale == 0.0 )
			{
				this.e[i] = this.d[i-1];
				for ( int j = 0; j < i; j++ )
				{
					this.d[j] = this.V[i-1][j];
					this.V[i][j] = 0.0;
					this.V[j][i] = 0.0;
				}
			}
			else
			{
				/* Generate Householder vector. */

				for ( int k = 0; k < i; k++ )
				{
					this.d[k] /= scale;
					h += ExtraMath.sq(this.d[k]);
				}
				double f = this.d[i-1];
				double g = Math.sqrt(h);
				if (f > 0)
					g = -g;
				this.e[i] = scale * g;
				h -= f * g;
				this.d[i-1] = f - g;
				for ( int j = 0; j < i; j++ )
					this.e[j] = 0.0;
				/* Apply similarity transformation to remaining columns. */
				for ( int j = 0; j < i; j++ )
				{
					f = this.d[j];
					this.V[j][i] = f;
					g = this.e[j] + this.V[j][j] * f;
					for ( int k = j+1; k <= i-1; k++ )
					{
						g += this.V[k][j] * this.d[k];
						this.e[k] += this.V[k][j] * f;
					}
					this.e[j] = g;
				}
				f = 0.0;
				for (int j = 0; j < i; j++)
				{
					this.e[j] /= h;
					f += this.e[j] * this.d[j];
				}
				double hh = f / (h + h);
				for (int j = 0; j < i; j++)
					e[j] -= hh * this.d[j];
				for (int j = 0; j < i; j++)
				{
					f = this.d[j];
					g = this.e[j];
					for (int k = j; k <= i-1; k++)
						this.V[k][j] -= (f * this.e[k] + g * this.d[k]);
					this.d[j] = this.V[i-1][j];
					this.V[i][j] = 0.0;
				}
			}
			this.d[i] = h;
		}
		/* Accumulate transformations. */
		for ( int i = 0; i < this.n-1; i++ )
		{
			this.V[this.n-1][i] = this.V[i][i];
			this.V[i][i] = 1.0;
			double h = this.d[i+1];
			if ( h != 0.0 )
			{
				for ( int k = 0; k <= i; k++ )
					this.d[k] = this.V[k][i+1] / h;
				for ( int j = 0; j <= i; j++ )
				{
					double g = 0.0;
					for ( int k = 0; k <= i; k++ )
						g += this.V[k][i+1] * this.V[k][j];
					for ( int k = 0; k <= i; k++ )
						this.V[k][j] -= g * this.d[k];
				}
			}
			for ( int k = 0; k <= i; k++ )
				this.V[k][i+1] = 0.0;
		}
		/* Copy the last column of V to d, then replace it with 0,0,...,0,1. */
		Matrix.getColumnTo(this.d, this.V, this.n-1);
		Matrix.setColumnTo(this.V, this.n-1, 0.0);
		this.V[this.n-1][this.n-1] = 1.0;
		this.e[0] = 0.0;
	}

	/**
	 * \brief Symmetric tridiagonal QL algorithm.
	 * 
	 * <p>This is derived from the Algol procedure tql2 by Bowdler, Martin,
	 * Reinsch, and Wilkinson, Handbook for Auto. Comp., Vol.ii-Linear Algebra,
	 * and the corresponding Fortran subroutine in EISPACK.</p>
	 */
	private void tql2 ()
	{
		/* Shift all elements of e up one, losing the first. */
		for (int i = 1; i < this.n; i++)
			this.e[i-1] = this.e[i];
		this.e[this.n-1] = 0.0;

		double f = 0.0;
		double tst1 = 0.0;
		for (int l = 0; l < this.n; l++)
		{
			/* Find the smallest subdiagonal element. */
			tst1 = Math.max(tst1, Math.abs(this.d[l]) + Math.abs(this.e[l]));
			int m = l;
			while ( m < this.n )
			{
				if ( Math.abs(this.e[m]) <= EPS * tst1 )
					break;
				m++;
			}
			/*
			 * If m == l, d[l] is an eigenvalue, otherwise, iterate.
			 */
			if ( m > l ) 
			{
				do
				{
					/* Compute implicit shift. */
					double g = this.d[l];
					double p = (this.d[l+1] - g) / (2.0 * this.e[l]);
					double r = Math.hypot(p,1.0);
					if ( p < 0 )
						r = -r;
					this.d[l] = this.e[l] / (p + r);
					this.d[l+1] = this.e[l] * (p + r);
					double dl1 = this.d[l+1];
					double h = g - this.d[l];
					for ( int i = l+2; i < this.n; i++ )
						this.d[i] -= h;
					f += h;
					/* Implicit QL transformation. */
					p = this.d[m];
					double c = 1.0;
					double c2 = c;
					double c3 = c;
					double el1 = this.e[l+1];
					double s = 0.0;
					double s2 = 0.0;
					for ( int i = m-1; i >= l; i-- )
					{
						c3 = c2;
						c2 = c;
						s2 = s;
						g = c * this.e[i];
						h = c * p;
						r = Math.hypot(p, this.e[i]);
						this.e[i+1] = s * r;
						s = this.e[i] / r;
						c = p / r;
						p = c * this.d[i] - s * g;
						this.d[i+1] = h + s * (c * g + s * this.d[i]);
						/* Accumulate transformation. */
						for ( int k = 0; k < this.n; k++ )
						{
							h = this.V[k][i+1];
							this.V[k][i+1] = s * this.V[k][i] + c * h;
							this.V[k][i] = c * this.V[k][i] - s * h;
						}
					}
					p = -s * s2 * c3 * el1 * this.e[l] / dl1;
					this.e[l] = s * p;
					this.d[l] = c * p;
					/* Check for convergence. */
				}
				while ( Math.abs(this.e[l]) > EPS*tst1 );
			}
			this.d[l] = this.d[l] + f;
			this.e[l] = 0.0;
		}
		/* Sort eigenvalues and corresponding vectors. */
		for ( int i = 0; i < this.n-1; i++ )
		{
			int k = i;
			double p = this.d[i];
			for (int j = i+1; j < this.n; j++)
				if ( this.d[j] < p )
				{
					k = j;
					p = this.d[j];
				}
			if ( k != i )
			{
				this.d[k] = this.d[i];
				this.d[i] = p;
				/* Swap values. */
				for ( int j = 0; j < this.n; j++ )
				{
					p = this.V[j][i];
					this.V[j][i] = this.V[j][k];
					this.V[j][k] = p;
				}
			}
		}
	}

	/**
	 * \brief Non-symmetric reduction to Hessenberg form.
	 * 
	 * <p>This is derived from the Algol procedures orthes and ortran by
	 * Bowdler, Martin, Reinsch, and Wilkinson, Handbook for Auto. Comp.,
	 * Vol.ii-Linear Algebra, and the corresponding Fortran subroutine in
	 * EISPACK.</p>
	 */
	private void orthes()
	{
		int low = 0;
		int high = this.n-1;
		double[] ort = Vector.zerosDbl(this.n);
		for ( int m = low+1; m <= high-1; m++ )
		{
			/* Scale column. */
			double scale = 0.0;
			for ( int i = m; i <= high; i++ )
				scale = scale + Math.abs(this.H[i][m-1]);
			if (scale != 0.0)
			{
				/* Compute Householder transformation. */
				double h = 0.0;
				for ( int i = high; i >= m; i-- )
				{
					ort[i] = this.H[i][m-1]/scale;
					h += ExtraMath.sq(ort[i]);
				}
				double g = Math.sqrt(h);
				if ( ort[m] > 0 )
					g = -g;
				h -= ort[m] * g;
				ort[m] -= g;
				/*
				 * Apply Householder similarity transformation
				 * H = (I-u*u'/h)*H*(I-u*u')/h)
				 * 
				 * Note that row/column is switched between the next two loops!
				 */
				for ( int j = m; j < this.n; j++ )
				{
					double f = 0.0;
					for ( int i = high; i >= m; i-- )
						f += ort[i] * this.H[i][j];
					f /= h;
					for ( int i = m; i <= high; i++ )
						this.H[i][j] -= f * ort[i];
				}
				for ( int i = 0; i <= high; i++ )
				{
					double f = 0.0;
					for (int j = high; j >= m; j--)
						f += ort[j] * this.H[i][j];
					f /= h;
					for (int j = m; j <= high; j++)
						this.H[i][j] -= f * ort[j];
				}
				ort[m] *= scale;
				this.H[m][m-1] = scale * g;
			}
		}
		/* Accumulate transformations (Algol's ortran). */
		Matrix.identityTo(this.V);
		for ( int m = high-1; m >= low+1; m-- )
			if ( this.H[m][m-1] != 0.0 )
			{
				for ( int i = m+1; i <= high; i++ )
					ort[i] = this.H[i][m-1];
				for ( int j = m; j <= high; j++ )
				{
					double g = 0.0;
					for (int i = m; i <= high; i++)
						g += ort[i] * this.V[i][j];
					/* Double division avoids possible underflow. */
					g = (g / ort[m]) / this.H[m][m-1];
					for ( int i = m; i <= high; i++ )
						this.V[i][j] += g * ort[i];
				}
			}
	}

	/**
	 * \brief Non-symmetric reduction from Hessenberg to real Schur form.
	 * 
	 * <p>This is derived from the Algol procedure hqr2 by by Martin and
	 * Wilkinson, Handbook for Auto. Comp., Vol.ii-Linear Algebra,
	 * and the corresponding Fortran subroutine in EISPACK.</p>
	 */
	private void hqr2 ()
	{
		/* Initialize. */
		int nn = this.n;
		// FIXME check that the original code did not mix up this.n and the n
		// defined in this routine!
		int n = nn - 1;
		int low = 0;
		int high = nn - 1;
		double exshift = 0.0;
		double p = 0.0, q = 0.0, r=0.0, s=0.0, z=0.0, t, w, x, y;
		/* Store roots isolated by balance and compute matrix norm. */
		double norm = 0.0;
		for ( int i = 0; i < nn; i++ )
		{
			if ( i < low | i > high )
			{
				this.d[i] = this.H[i][i];
				this.e[i] = 0.0;
			}
			for ( int j = Math.max(i-1, 0); j < nn; j++ )
				norm += Math.abs(H[i][j]);
		}
		/* Outer loop over eigenvalue index. */
		int iter = 0;
		while ( n >= low )
		{
			/* Look for single small sub-diagonal element. */
			int l = n;
			while ( l > low )
			{
				s = Math.abs(this.H[l-1][l-1]) + Math.abs(this.H[l][l]);
				if ( s == 0.0 )
					s = norm;
				if ( Math.abs(H[l][l-1]) < EPS * s )
					break;
				l--;
			}
			/* Check for convergence. */
			if ( l == n )
			{
				/* One root found. */
				this.H[n][n] += exshift;
				this.d[n] = this.H[n][n];
				this.e[n] = 0.0;
				n--;
				iter = 0;

			}
			else if (l == n-1)
			{
				/* Two roots found. */
				w = this.H[n][n-1] * this.H[n-1][n];
				p = (this.H[n-1][n-1] - this.H[n][n]) / 2.0;
				q = ExtraMath.sq(p) + w;
				z = Math.sqrt(Math.abs(q));
				this.H[n][n] += exshift;
				this.H[n-1][n-1] += exshift;
				x = this.H[n][n];
				if ( q >= 0 )
				{
					/* Real pair. */
					z = p + ( (p >= 0) ? z : -z );
					this.d[n-1] = x + z;
					this.d[n] = this.d[n-1];
					if ( z != 0.0 )
						this.d[n] = x - w / z;
					this.e[n-1] = 0.0;
					this.e[n] = 0.0;
					x = this.H[n][n-1];
					s = Math.abs(x) + Math.abs(z);
					p = x / s;
					q = z / s;
					r = Math.sqrt(p * p+q * q);
					p /= r;
					q /= r;
					/* Row modification. */
					for ( int j = n-1; j < nn; j++ )
					{
						z = this.H[n-1][j];
						this.H[n-1][j] = q * z + p * this.H[n][j];
						this.H[n][j] = q * this.H[n][j] - p * z;
					}
					/* Column modification. */
					for ( int i = 0; i <= n; i++ )
					{
						z = this.H[i][n-1];
						this.H[i][n-1] = q * z + p * this.H[i][n];
						this.H[i][n] = q * this.H[i][n] - p * z;
					}
					/* Accumulate transformations. */
					for ( int i = low; i <= high; i++ )
					{
						z = this.V[i][n-1];
						this.V[i][n-1] = q * z + p * this.V[i][n];
						this.V[i][n] = q * this.V[i][n] - p * z;
					}
					
				}
				else
				{
					/* Complex pair. */
					this.d[n-1] = x + p;
					this.d[n] = x + p;
					this.e[n-1] = z;
					this.e[n] = -z;
				}
				n -= 2;
				iter = 0;
			}
			else
			{
				/* No convergence yet. */
				/* Form shift. */
				x = this.H[n][n];
				y = 0.0;
				w = 0.0;
				if ( l < n )
				{
					y = this.H[n-1][n-1];
					w = this.H[n][n-1] * this.H[n-1][n];
				}
				/* Wilkinson's original ad hoc shift. */
				if ( iter == 10 )
				{
					exshift += x;
					for ( int i = low; i <= n; i++ )
						this.H[i][i] -= x;
					s = Math.abs(this.H[n][n-1]) + Math.abs(this.H[n-1][n-2]);
					x = y = 0.75 * s;
					w = -0.4375 * ExtraMath.sq(s);
				}
				/* MATLAB's new ad hoc shift. */
				if (iter == 30)
				{
					s = (y - x) / 2.0;
					s = ExtraMath.sq(s) + w;
					if ( s > 0 )
					{
						s = Math.sqrt(s);
						if ( y < x )
							s = -s;
						s = x - w / ((y - x) / 2.0 + s);
						for (int i = low; i <= n; i++)
							this.H[i][i] -= s;
						exshift += s;
						x = y = w = 0.964;
					}
				}
				/* Increase iterator. */
				iter++;
				/* Look for two consecutive small sub-diagonal elements */
				int m = n-2;
				while ( m >= l )
				{
					z = this.H[m][m];
					r = x - z;
					s = y - z;
					p = (r * s - w) / this.H[m+1][m] + this.H[m][m+1];
					q = this.H[m+1][m+1] - z - r - s;
					r = this.H[m+2][m+1];
					s = Math.abs(p) + Math.abs(q) + Math.abs(r);
					p /= s;
					q /= s;
					r /= s;
					if ( m == l )
						break;
					double test1 = Math.abs(this.H[m][m-1]) * 
							(Math.abs(q) + Math.abs(r));
					double test2 = Math.abs(p) * 
							(Math.abs(this.H[m-1][m-1]) + 
									Math.abs(z) + 
									Math.abs(this.H[m+1][m+1]));
					if ( test1 < EPS * test2)
						break;
					m--;
				}
				for ( int i = m + 2; i <= n; i++ ) 
				{
					this.H[i][i-2] = 0.0;
					if ( i > m + 2 )
						this.H[i][i-3] = 0.0;
				}
				/* Double QR step involving rows l:n and columns m:n. */
				for ( int k = m; k <= n-1; k++ )
				{
					boolean notlast = (k != n-1);
					if ( k != m )
					{
						p = this.H[k][k-1];
						q = this.H[k+1][k-1];
						r = (notlast ? this.H[k+2][k-1] : 0.0);
						x = Math.abs(p) + Math.abs(q) + Math.abs(r);
						if ( x == 0.0 )
							continue;
						p /= x;
						q /= x;
						r /= x;
					}
					s = ExtraMath.hypotenuse(p, q, r);
					if ( p < 0 )
						s = -s;
					if ( s != 0.0 )
					{
						if ( k != m )
							this.H[k][k-1] = -s * x;
						else if ( l != m )
							this.H[k][k-1] = - this.H[k][k-1];
						p += s;
						x = p / s;
						y = q / s;
						z = r / s;
						q /= p;
						r /= p;
						/* Row modification. */
						for ( int j = k; j < nn; j++ )
						{
							p = this.H[k][j] + q * this.H[k+1][j];
							if ( notlast )
							{
								p = p + r * this.H[k+2][j];
								this.H[k+2][j] = this.H[k+2][j] - p * z;
							}
							this.H[k][j] -= p * x;
							this.H[k+1][j] -= p * y;
						}
						/* Column modification. */
						for ( int i = 0; i <= Math.min(n, k+3); i++ )
						{
							p = x * this.H[i][k] + y * this.H[i][k+1];
							if ( notlast )
							{
								p = p + z * this.H[i][k+2];
								this.H[i][k+2] -= p * r;
							}
							this.H[i][k] -= p;
							this.H[i][k+1] -= p * q;
						}
						/* Accumulate transformations. */
						for ( int i = low; i <= high; i++ )
						{
							p = x * this.V[i][k] + y * this.V[i][k+1];
							if ( notlast )
							{
								p += z * this.V[i][k+2];
								this.V[i][k+2] -= p * r;
							}
							this.V[i][k] -= p;
							this.V[i][k+1] -= p * q;
						}
					}  // (s != 0)
				}  // k loop
			}  // check convergence
		}  // while (n >= low)
		/* Backsubstitute to find vectors of upper triangular form. */
		if (norm == 0.0)
			return;
		for ( n = nn-1; n >= 0; n-- )
		{
			p = this.d[n];
			q = this.e[n];
			if ( q == 0 )
			{
				/* Real vector. */
				int l = n;
				this.H[n][n] = 1.0;
				for ( int i = n-1; i >= 0; i-- )
				{
					w = this.H[i][i] - p;
					r = 0.0;
					for (int j = l; j <= n; j++)
						r += this.H[i][j] * this.H[j][n];
					if ( this.e[i] < 0.0 )
					{
						z = w;
						s = r;
					}
					else
					{
						l = i;
						if ( this.e[i] == 0.0 )
						{
							if ( w == 0.0 )
								this.H[i][n] = -r / (EPS * norm);
							else
								this.H[i][n] = -r / w;
						}
						else
						{
							/* Solve real equations. */
							x = this.H[i][i+1];
							y = this.H[i+1][i];
							q = ExtraMath.sq(this.d[i] - p)
									+ ExtraMath.sq(this.e[i]);
							t = (x * s - z * r) / q;
							this.H[i][n] = t;
							if ( Math.abs(x) > Math.abs(z) )
								this.H[i+1][n] = (-r - w * t) / x;
							else
								this.H[i+1][n] = (-s - y * t) / z;
						}
						/* Overflow control. */
						t = Math.abs(this.H[i][n]);
						if ( (EPS * t) * t > 1 )
							for ( int j = i; j <= n; j++ )
								H[j][n] = H[j][n] / t;
					}
				}
			}
			else if (q < 0)
			{
				/* Complex vector. */
				int l = n-1;
				// Last vector component imaginary so matrix is triangular
				if (Math.abs(H[n][n-1]) > Math.abs(H[n-1][n])) {
					H[n-1][n-1] = q / H[n][n-1];
					H[n-1][n] = -(H[n][n] - p) / H[n][n-1];
				} else {
					cdiv(0.0, -this.H[n-1][n], this.H[n-1][n-1]-p, q);
					this.H[n-1][n-1] = this.cDivR;
					this.H[n-1][n] = this.cDivI;
				}
				this.H[n][n-1] = 0.0;
				this.H[n][n] = 1.0;
				for ( int i = n-2; i >= 0; i-- )
				{
					double ra,sa,vr,vi;
					ra = 0.0;
					sa = 0.0;
					for (int j = l; j <= n; j++)
					{
						ra += this.H[i][j] * this.H[j][n-1];
						sa += this.H[i][j] * this.H[j][n];
					}
					w = this.H[i][i] - p;
					if ( this.e[i] < 0.0 )
					{
						z = w;
						r = ra;
						s = sa;
					}
					else
					{
						l = i;
						if ( this.e[i] == 0.0 )
						{
							this.cdiv(-ra, -sa, w, q);
							this.H[i][n-1] = this.cDivR;
							this.H[i][n] = this.cDivI;
						}
						else
						{
							/* Solve complex equations. */
							x = this.H[i][i+1];
							y = this.H[i+1][i];
							vr = ExtraMath.sq(this.d[i] - p) + 
									ExtraMath.sq(this.e[i])- ExtraMath.sq(q);
							vi = (this.d[i] - p) * 2.0 * q;
							if ( vr == 0.0 & vi == 0.0 )
							{
								vr = EPS * norm * (Math.abs(w) + Math.abs(q) +
										Math.abs(x) + Math.abs(y) + Math.abs(z));
							}
							this.cdiv(x*r-z*ra+q*sa, x*s-z*sa-q*ra, vr, vi);
							this.H[i][n-1] = this.cDivR;
							this.H[i][n] = this.cDivI;
							if ( Math.abs(x) > (Math.abs(z) + Math.abs(q)) )
							{
								this.H[i+1][n-1] =
										(-ra-w*this.H[i][n-1]+q*this.H[i][n])/x;
								this.H[i+1][n] =
										(-sa-w*this.H[i][n]-q*this.H[i][n-1])/x;
							}
							else
							{
								this.cdiv(-r -y*this.H[i][n-1],
										-s -y*this.H[i][n], z, q);
								this.H[i+1][n-1] = this.cDivR;
								this.H[i+1][n] = this.cDivI;
							}
						}
						/* Overflow control. */
						t = Math.max(Math.abs(this.H[i][n-1]),
								Math.abs(this.H[i][n]));
						if ( (EPS * t) * t > 1)
							for (int j = i; j <= n; j++)
							{
								this.H[j][n-1] /= t;
								this.H[j][n] /= t;
							}
					}
				}
			}
		}
		/* Vectors of isolated roots. */
		for ( int i = 0; i < nn; i++ )
			if ( i < low || i > high )
				for ( int j = i; j < nn; j++ )
					this.V[i][j] = this.H[i][j];
		/* Back transformation to get eigenvectors of original matrix. */
		for ( int j = nn-1; j >= low; j-- )
			for ( int i = low; i <= high; i++ )
			{
				z = 0.0;
				for ( int k = low; k <= Math.min(j, high); k++ )
					z += this.V[i][k] * this.H[k][j];
				this.V[i][j] = z;
			}
	}
	
	/**
	 * \brief Complex scalar division.
	 * 
	 * @param xr Real value of x.
	 * @param xi Imaginary value of x.
	 * @param yr Real value of y.
	 * @param yi Imaginary value of y.
	 */
	private void cdiv(double xr, double xi, double yr, double yi)
	{
		double r,d;
		if ( Math.abs(yr) > Math.abs(yi) )
		{
			r = yi/yr;
			d = yr + r*yi;
			this.cDivR = (xr + r*xi)/d;
			this.cDivI = (xi - r*xr)/d;
		}
		else
		{
			r = yr/yi;
			d = yi + r*yr;
			this.cDivR = (r*xr + xi)/d;
			this.cDivI = (r*xi - xr)/d;
		}
	}
}
