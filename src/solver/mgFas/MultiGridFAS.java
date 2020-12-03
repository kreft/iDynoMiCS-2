package solver.mgFas;

import grid.SpatialGrid;
import linearAlgebra.Array;
import utility.ExtraMath;

/**
 * 
 * Work in progress, getting idyno 1 mgFas to idyno 2, testing
 * 
 * 
 *
 */
public class MultiGridFAS {
	
	protected final int NPRE = 1;
	protected final int NPOST = 1;
	protected final int NGMAX = 15;
	protected final double ALPHA = 0.33;
	public final Double BLTHRESH = 0.1;
	
	private Double _referenceSystemSide;
	private double maxOrder;
	/* nI = gridlength / resolution */
	private double _resolution;
	private int _nK;
	
	/* 1 per solute? */
	private double[][][] _rhs = new double[1][1][1];
	
	
	public void setReferenceSide(double[][][] grid)
	{
		int nI = Array.height(grid);
		int nJ = Array.width(grid);
		int nK = Array.depth(grid);
		_nK = nK;
		
		_referenceSystemSide = Double.valueOf(Math.min(nI, nJ));
		if (nK>1) _referenceSystemSide = Math.min(_referenceSystemSide, nK);

		maxOrder = ExtraMath.log2( _referenceSystemSide ).intValue();
		_referenceSystemSide -= 1;
		_referenceSystemSide *= _resolution;
	}
	
	public void mgfas(double u, int n, int maxcyc)
	{
		int j, jcycle, jj, jm1, jpost, jpre, nf, ng = 0, ngrid, nn;
		double res, trerr;
		SpatialGrid irho, irhs, itau, itemp;
		
	}
	
	/**
	 * This is meant to return the correct index value following the logic of
	 * setReferenceSide() above.
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	private Double referenceIndex(int i, int j, int k)
	{
		if (_nK > 1)
			return Double.valueOf(Math.min(i, Math.min(j, k)) - 1);
		else
			return Double.valueOf(Math.min(i, j) - 1);
	}
	
	
	private double[][][] fillDiff(double[][][] diffusivity, double[][][] rd)
	{
		int _i = 0, _j = 0, _k = 0;
		double[][][] _diff = new double[3][3][3];
		_diff[0][1][1] = diffusivity[_i-1][_j][_k]*rd[_i-1][_j][_k];
		_diff[2][1][1] = diffusivity[_i+1][_j][_k]*rd[_i+1][_j][_k];
		_diff[1][0][1] = diffusivity[_i][_j-1][_k]*rd[_i][_j-1][_k];
		_diff[1][2][1] = diffusivity[_i][_j+1][_k]*rd[_i][_j+1][_k];
		_diff[1][1][0] = diffusivity[_i][_j][_k-1]*rd[_i][_j][_k-1];
		_diff[1][1][2] = diffusivity[_i][_j][_k+1]*rd[_i][_j][_k+1];
		_diff[1][1][1] = diffusivity[_i][_j][_k]*rd[_i][_j][_k];
		
		return _diff;
	}
	
	/**
	 * \brief Compute the L-operator
	 * 
	 * @param order
	 * @param h2i
	 * @return
	 */
	private double computeLop(int order, Double h2i, double[][][] _diff, 
			double[][][] u, double[][][] _reac)
	{	
		int _i = 0, _j = 0, _k = 0;
		return ( (_diff[2][1][1]+_diff[1][1][1])*(u[_i+1][_j][_k]-u[_i][_j][_k])
		        +(_diff[0][1][1]+_diff[1][1][1])*(u[_i-1][_j][_k]-u[_i][_j][_k])
		        +(_diff[1][2][1]+_diff[1][1][1])*(u[_i][_j+1][_k]-u[_i][_j][_k])
		        +(_diff[1][0][1]+_diff[1][1][1])*(u[_i][_j-1][_k]-u[_i][_j][_k])
		        +(_diff[1][1][2]+_diff[1][1][1])*(u[_i][_j][_k+1]-u[_i][_j][_k])
		        +(_diff[1][1][0]+_diff[1][1][1])*(u[_i][_j][_k-1]-u[_i][_j][_k]))
		        *h2i + _reac[_i][_j][_k];
	}
	
	private Double computeDiffLop(int order, Double h2i, double[][][] _diff, double[][][] _diffReac)
	{
		int _i = 0, _j = 0, _k = 0;
		return -h2i
		        *(6.0f*_diff[1][1][1]
		              +_diff[2][1][1]+_diff[0][1][1]
		              +_diff[1][2][1]+_diff[1][0][1]
		              +_diff[1][1][2]+_diff[1][1][0])
		       +_diffReac[_i][_j][_k];
	}
	
	public double relax(int order, double[][][] _conc)
	{
		int _i, _j, _k;
		int nI = Array.height(_conc);
		int nJ = Array.width(_conc);
		int nK = Array.depth(_conc);
		
		double[][][] _bLayer = null;
		double[][][] _relDiff = null;
		
		
		/* h = 1.0 / (n - 1) */
		Double h = _referenceSystemSide/referenceIndex(nI, nJ, nK);
		Double h2i = 0.5f / ( h * h );
		
		/* red-black relaxation
		   iterate through system
		   isw, jsw and ksw alternate between values 1 and 2 */
		
		double[][][] u = _conc;
		double[][][] bl = _bLayer;
		double[][][] rd = _relDiff;
		
		Double lop, dlop, res;
		
		/* Apply an eventual modification of the local diffusivity for THIS
		   solute around the boundaries */
		
		// TODO
		/* refreshDiffBoundaries(order); */
		
		Double totalRes = 0.0;
		
		// bvm 22.12.09: now allows red-black for 2d AND 3d
		int ksw = 1;
		int isw, jsw;
		for (int pass = 1; pass <= 2; pass++, ksw = 3-ksw)
		{
			jsw = ksw;
			for (_k = 1; _k <= nK; _k++, jsw = 3-jsw)
			{
				isw = jsw;
				for (_j = 1; _j <= nJ; _j++, isw = 3-isw)
				{
					for (_i = isw; _i <= nI; _i += 2)
					{
						if (bl[_i][_j][_k] >= BLTHRESH)
						{
							// Case: Inside boundary layer
							// Equations must be solved here
							
							// compute diffusivity values
							// and that of surrounding neighbours
							
							/* diffusivity, rd (relDiff) */
							double[][][] _diff = fillDiff(new double[1][1][1], new double[1][1][1]);
							
							/* production rate */
							double[][][] _reac = new double[1][1][1];
							// compute L operator
							lop = computeLop(order, h2i, _diff, u, _reac);
							
							/* ?? don't see where this is set in idyno 1 */
							double[][][] _diffReac = new double[1][1][1];
							
							// compute derivative of L operator
							dlop = computeDiffLop(order, h2i, _diff, _diffReac);
							
							// compute residual
							res = (lop-_rhs[_i][_j][_k])/dlop;
							totalRes += Math.abs(res);
							// update concentration (test for NaN)
							//LogFile.writeLog("NaN generated in multigrid solver "+"while computing rate for "+soluteName);
							//LogFile.writeLog("location: "+_i+", "+_j+", "+_k);
							//LogFile.writeLog("dlop: "+dlop+"; lop: "+lop+"; grid: "+_rhs[order].grid[_i][_j][_k]);
							
							u[_i][_j][_k] -= res;
							// if negative concentrations, put 0 value
							u[_i][_j][_k] = (u[_i][_j][_k]<0 ? 0 : u[_i][_j][_k]);
						}
					}
				}
			}
			// refresh the padding elements to enforce
			// boundary conditions for all solutes
			refreshBoundary(_conc);
		}
		return totalRes;
	}

	private void refreshBoundary(double[][][] _conc) 
	{
		/*
		 * Examine all objects at the boundary of the grid, and adjusts
		 * them as specified by the boundary condition rules.
		 * 
		 * iterates over all boundaries
		 */
	}
}
