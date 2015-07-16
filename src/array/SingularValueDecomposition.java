package array;

import Jama.util.Maths;

/**
 * 
 * <p>Credit to the JAMA package</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 *
 */
public class SingularValueDecomposition
{
	private double[][] u, v;
	
	private double[] s;
	
	private int m, n;
	
	private final double eps = Math.pow(2.0,-52.0);
	
	private final double tiny = Math.pow(2.0,-966.0);
	
	public SingularValueDecomposition (double[][] matrix)
	{
		Matrix.checkDimensions(matrix);
		double[][] a = Matrix.copy(matrix);
		this.m = Matrix.rowDim(a);
		this.n = Matrix.colDim(a);
		
		int nu = Math.min(m,n);
		s = Vector.zerosDbl(Math.min(m+1,n));
		u = Matrix.zerosDbl(m, nu);
		v = Matrix.zerosDbl(n);
		
		double[] e = Vector.zerosDbl(n);
		double[] work = Vector.zerosDbl(m);
		
		int nct = Math.min(m-1,n);
		int nrt = Math.max(0,Math.min(n-2,m));
		for ( int k = 0; k < Math.max(nct,nrt); k++ )
		{
			if ( k < nct )
			{
				/*
				 * Compute the transformation for the k-th column and place
				 * the k-th diagonal in s[k].
				 * 
				 * Compute 2-norm of k-th column without under/overflow.
				 * Can't use 
				 * s[k] = Vector.normEuclid(Matrix.getRowAsColumn(matrix, k));
				 * as we start with i = k.
				 */
				s[k] = 0;
	            for ( int i = k; i < m; i++ )
	               s[k] = Maths.hypot(s[k], a[i][k]);
	            if ( s[k] != 0.0 )
	            {
	            	if ( a[k][k] < 0.0 )
	            		s[k] = -s[k];
	            	for ( int i = k; i < m; i++ )
		            	a[i][k] /= s[k];
	            	a[k][k]++;
	            }
	            s[k] = -s[k];
			}
			for (int j = k+1; j < n; j++)
			{
				if ( ( k < nct ) && ( s[k] != 0.0 ) )
				{
					/*
					 * Apply the transformation.
					 * 
					 * Can't use
					 * Vector.dotProduct(
					 *		Matrix.getRowAsColumn(matrix, k), 
					 * 		Matrix.getRowAsColumn(matrix, j));
					 * as we start with i = k.
					 */
					double temp = 0.0;
					for ( int i = k; i < m; i++ )
						temp += a[i][k] * a[i][j];
					temp /= -a[k][k];
					for ( int i = k; i < m; i++ )
		                  a[i][j] += temp * a[i][k];
					/*
					 * Place the k-th row of A into e for the subsequent
					 * calculation of the row transformation.
					 */
					e[j] = a[k][j];
				}
			}
			if ( k < nct )
			{
				/*
				 * Place the transformation in U for subsequent back
				 * multiplication.
				 */
				for ( int i = k; i < m; i++ )
		               u[i][k] = a[i][k];
			}
			if (k < nrt)
			{
				/*
				 * Compute the k-th row transformation and place the k-th
				 * super-diagonal in e[k]. Compute 2-norm without
				 * under/overflow.
				 */
				e[k] = 0.0;
				for ( int i = k+1; i < n; i++ )
		               e[k] = Maths.hypot(e[k], e[i]);
				if ( e[k] != 0.0 )
				{
					if (e[k+1] < 0.0)
						e[k] = -e[k];
					for ( int i = k+1; i < n; i++ )
						e[i] /= e[k];
					e[k+1]++;
				}
				e[k] = -e[k];
				if ( ( k+1 < m ) && ( e[k] != 0.0 ) )
				{
					/*
					 * Apply the transformation.
					 */
					for ( int i = k+1; i < m; i++ )
						work[i] = 0.0;
					for ( int j = k+1; j < n; j++ )
						for ( int i = k+1; i < m; i++ )
							work[i] += e[j] * a[i][j];
					for ( int j = k+1; j < n; j++ )
					{
						double temp = -e[j]/e[k+1];
						for ( int i = k+1; i < m; i++ )
		                     a[i][j] += temp * work[i];
					}
				}
				/*
				 * Place the transformation in V for subsequent back
				 * multiplication.
				 */
				for ( int i = k+1; i < n; i++ )
	                  v[i][k] = e[i];
			}
		}
		/*
		 * Set up the final bi-diagonal matrix or order p.
		 */
		int p = Math.min(n, m+1);
		if ( nct < n )
	         s[nct] = a[nct][nct];
		if ( m < p )
	         s[p-1] = 0.0;
		if ( nrt + 1 < p )
	         e[nrt] = a[nrt][p-1];
		e[p-1] = 0.0;
		/*
		 * Generate u.
		 * 
		 * Can't use
		 * u = Matrix.identity(u);
		 * here because we start with j = nct.
		 */
		for ( int j = nct; j < nu; j++ )
		{
            for ( int i = 0; i < m; i++ )
               u[i][j] = 0.0;
            u[j][j] = 1.0;
         }
         for ( int k = nct-1; k >= 0; k-- )
         {
            if ( s[k] != 0.0 )
            {
               for (int j = k+1; j < nu; j++)
               {
                  double temp = 0.0;
                  for (int i = k; i < m; i++)
                     temp += u[i][k]*u[i][j];
                  temp /= -u[k][k];
                  for (int i = k; i < m; i++)
                     u[i][j] += temp*u[i][k];
               }
               for (int i = k; i < m; i++ )
                  u[i][k] = -u[i][k];
               u[k][k]++;
               for (int i = 0; i < k-1; i++)
                  u[i][k] = 0.0;
            }
            else
            {
               for (int i = 0; i < m; i++)
                  u[i][k] = 0.0;
               u[k][k] = 1.0;
            }
         }
         /*
          * Generate v.
          */
         for (int k = n-1; k >= 0; k--)
         {
             if ( ( k < nrt ) & ( e[k] != 0.0 ) )
                for ( int j = k+1; j < nu; j++ )
                {
                   double temp = 0.0;
                   for ( int i = k+1; i < n; i++ )
                      temp += v[i][k] * v[i][j]; 
                   temp /= -v[k+1][k];
                   for ( int i = k+1; i < n; i++ )
                      v[i][j] += temp * v[i][k];
                }
             for (int i = 0; i < n; i++)
                v[i][k] = 0.0;
             v[k][k] = 1.0;
          }
         /*
          * Main iteration loop for the singular values.
          */
         int pp = p-1;
         int iter = 0;
         while ( p > 0 )
         {
        	 int k,kase;

             // Here is where a test for too many iterations would go.

             // This section of the program inspects for
             // negligible elements in the s and e arrays.  On
             // completion the variables kase and k are set as follows.

             // kase = 1     if s(p) and e[k-1] are negligible and k<p
             // kase = 2     if s(k) is negligible and k<p
             // kase = 3     if e[k-1] is negligible, k<p, and
             //              s(k), ..., s(p) are not negligible (qr step).
             // kase = 4     if e(p-1) is negligible (convergence).

             for (k = p-2; k >= -1; k--) {
                if (k == -1) {
                   break;
                }
                if (Math.abs(e[k]) <=
                      tiny + eps*(Math.abs(s[k]) + Math.abs(s[k+1]))) {
                   e[k] = 0.0;
                   break;
                }
             }
             if (k == p-2) {
                kase = 4;
             } else {
                int ks;
                for (ks = p-1; ks >= k; ks--) {
                   if (ks == k) {
                      break;
                   }
                   double t = (ks != p ? Math.abs(e[ks]) : 0.) + 
                              (ks != k+1 ? Math.abs(e[ks-1]) : 0.);
                   if (Math.abs(s[ks]) <= tiny + eps*t)  {
                      s[ks] = 0.0;
                      break;
                   }
                }
                if (ks == k) {
                   kase = 3;
                } else if (ks == p-1) {
                   kase = 1;
                } else {
                   kase = 2;
                   k = ks;
                }
             }
             k++;
             /*
              * Perform the task indicated by kase.
              */
             switch (kase)
             {
                /*
                 * Deflate negligible s(p).
                 */
                case 1:
                {
                   double f = e[p-2];
                   e[p-2] = 0.0;
                   for (int j = p-2; j >= k; j--)
                   {
                      double t = Maths.hypot(s[j],f);
                      double cs = s[j]/t;
                      double sn = f/t;
                      s[j] = t;
                      if (j != k)
                      {
                         f = -sn*e[j-1];
                         e[j-1] = cs*e[j-1];
                      }
                      for (int i = 0; i < n; i++)
                      {
                    	  t = cs*v[i][j] + sn*v[i][p-1];
                    	  v[i][p-1] = -sn*v[i][j] + cs*v[i][p-1];
                    	  v[i][j] = t;
                      }
                   }
                }
                break;
                /*
                 * Split at negligible s(k).
                 */
                case 2:
                {
                   double f = e[k-1];
                   e[k-1] = 0.0;
                   for (int j = k; j < p; j++)
                   {
                      double t = Maths.hypot(s[j],f);
                      double cs = s[j]/t;
                      double sn = f/t;
                      s[j] = t;
                      f = -sn*e[j];
                      e[j] = cs*e[j];
                      for (int i = 0; i < m; i++)
                      {
                    	  t = cs*u[i][j] + sn*u[i][k-1];
                    	  u[i][k-1] = -sn*u[i][j] + cs*u[i][k-1];
                    	  u[i][j] = t;
                      }
                   }
                }
                break;
                /*
                 * Perform one QR step.
                 */
                case 3:
                {
                   /*
                    * Calculate the shift.
                    */
                   double scale = Math.max(Math.max(Math.max(Math.max(
                           Math.abs(s[p-1]),Math.abs(s[p-2])),Math.abs(e[p-2])), 
                           Math.abs(s[k])),Math.abs(e[k]));
                   double sp = s[p-1]/scale;
                   double spm1 = s[p-2]/scale;
                   double epm1 = e[p-2]/scale;
                   double sk = s[k]/scale;
                   double ek = e[k]/scale;
                   double b = ((spm1 + sp)*(spm1 - sp) + epm1*epm1)/2.0;
                   double c = (sp*epm1)*(sp*epm1);
                   double shift = 0.0;
                   if ( ( b != 0.0 ) || ( c != 0.0 ) )
                   {
                      shift = Math.sqrt( b*b + c );
                      if ( b < 0.0 )
                         shift = -shift;
                      shift = c/(b + shift);
                   }
                   double f = (sk + sp)*(sk - sp) + shift;
                   double g = sk * ek;
                   /*
                    * Chase zeros.
                    */
                   for (int j = k; j < p-1; j++)
                   {
                      double t = Maths.hypot(f,g);
                      double cs = f/t;
                      double sn = g/t;
                      if (j != k) {
                         e[j-1] = t;
                      }
                      f = cs*s[j] + sn*e[j];
                      e[j] = cs*e[j] - sn*s[j];
                      g = sn*s[j+1];
                      s[j+1] = cs*s[j+1];
                      for (int i = 0; i < n; i++)
                      {
                    	  t = cs*v[i][j] + sn*v[i][j+1];
                    	  v[i][j+1] = -sn*v[i][j] + cs*v[i][j+1];
                    	  v[i][j] = t;
                      }
                      t = Maths.hypot(f,g);
                      cs = f/t;
                      sn = g/t;
                      s[j] = t;
                      f = cs*e[j] + sn*s[j+1];
                      s[j+1] = -sn*e[j] + cs*s[j+1];
                      g = sn*e[j+1];
                      e[j+1] = cs*e[j+1];
                      if ( j < m-1 )
                         for (int i = 0; i < m; i++)
                         {
                            t = cs*u[i][j] + sn*u[i][j+1];
                            u[i][j+1] = -sn*u[i][j] + cs*u[i][j+1];
                            u[i][j] = t;
                         }
                   }
                   e[p-2] = f;
                   iter = iter + 1;
                }
                break;
                /*
                 * Convergence.
                 */
                case 4:
                {
                   /*
                    * Make the singular values positive.
                    */
                   if ( s[k] <= 0.0 )
                   {
                      s[k] = -s[k];
                      for ( int i = 0; i <= pp; i++ )
                      	v[i][k] = -v[i][k];
                   }
                   /*
                    * Order the singular values.
                    */
                   while ( k < pp )
                   {
                      if ( s[k] >= s[k+1] )
                         break;
                      double t = s[k];
                      s[k] = s[k+1];
                      s[k+1] = t;
                      if ( k < n-1 )
                         for (int i = 0; i < n; i++)
                         {
                            t = v[i][k+1];
                            v[i][k+1] = v[i][k];
                            v[i][k] = t;
                         }
                      if ( k < m-1 )
                         for ( int i = 0; i < m; i++ )
                         {
                            t = u[i][k+1];
                            u[i][k+1] = u[i][k];
                            u[i][k] = t;
                         }
                      k++;
                   }
                   iter = 0;
                   p--;
                }
                break;
             }
         }
	}
	
	public double[][] getU()
	{
		return Matrix.copy(u);
	}
	
	public double[][] getV()
	{
		return Matrix.copy(v);
	}
	
	public double[] getSingularValues()
	{
		return Vector.copy(s);
	}
	
	public double[][] getS()
	{
		return Matrix.asDiagonal(s);
	}
	
	public double norm2()
	{
		return s[0];
	}
	
	public double condition()
	{
		return s[0]/s[Math.min(m,n)-1];
	}
	
	public int rank ()
	{
		double tol = Math.max(m,n) * s[0] * eps;
		int r = 0;
		for ( int i = 0; i < s.length; i++ )
			if ( s[i] > tol )
				r++;
		return r;
	}
}