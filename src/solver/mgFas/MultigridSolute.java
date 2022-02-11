/**
 * \package diffusionSolver.multigrid
 * \brief Package of classes used to aid solver calculation for multi-grid scenarios.
 * 
 * Package of classes used to capture the diffusion solvers that can be defined in the protocol file. This package is 
 * part of iDynoMiCS v1.2, governed by the CeCILL license under French law and abides by the rules of distribution of free software.  
 * You can use, modify and/ or redistribute iDynoMiCS under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at 
 * the following URL  "http://www.cecill.info".
 */
package solver.mgFas;

import dataIO.Log;
import idynomics.Global;
import processManager.library.PDEWrapper;
import utility.ExtraMath;
import debugTools.QuickCSV;
import linearAlgebra.Array;

import java.util.LinkedList;
import java.util.List;


/**
 * \brief Implements static utility functions for used in multigrid method.
 * 
 * @author João Xavier (xavierj@mskcc.org), Memorial Sloan-Kettering Cancer
 * Center (NY, USA).
 */
public class MultigridSolute 
{
	/**
	 * Name of the solute in this multigrid
	 */
	public String soluteName;
	
	/**
	 * The simulation solute grid containing the concentrations of this solute
	 */
	public SoluteGrid realGrid;
	
	/**
	 * 
	 */
	protected double _referenceSystemSide;
	
	/**
	 * 
	 */
	protected double _diffusivity;
		
	/**
	 * Maximum solute level in the connected bulk.
	 */
	protected double sBulkMax;
	
	/**
	 * Solute level in the connected bulk.
	 */
	protected double sBulk;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _relDiff;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _bLayer;
	
	/**
	 * Concentration of this solute.
	 *
	 * Grids stored from finest [0] to coarsest [length]
	 */
	public SoluteGrid[] _conc;
	
	/**
	 * 
	 */
	public SoluteGrid[] _reac;
	
	/**
	 * 
	 */
	public SoluteGrid[] _diffReac;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _rhs, _itemp;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _itau;

	/**
	 * where _tempres[order].grid[i][j][k] =  ( lop - _rhs ) / dlop
	 */
	private SoluteGrid[] _tempRes;
	
	/**
	 * 
	 */
	public double truncationError;

	/**
	 * storing previous residual for analysis.
	 */
	private double _res[];

	/**
	 * As more smoothing may be required stage is increased
	 */
	private int _stage = 0;

	private int _tempOrder = 0;

	private List<Double> _tempNums = new LinkedList<Double>();

	/**
	 * \brief 
	 * 
	 * Should be ok not to fill this with zeros at initialisation, as it should
	 * be filled in fillDiff() before it's ever called.
	 */
	private static final double[][][] _diff = new double[3][3][3];
	
	/**
	 * 
	 */
	private static double[][][] u;
	
	/**
	 * 
	 */
	private static double[][][] rd;
	
	/**
	 * 
	 */
	private static double[][][] bl;
	
	/**
	 * 
	 */
	private static int _i;
	
	/**
	 * 
	 */
	private static int _j;
	
	/**
	 * 
	 */
	private static int _k;
	
	/**
	 * 
	 */
	public static final Double BLTHRESH = 0.1;
	
	/**
	 * 
	 */
	private static int maxOrder;
	
	/**
	 * Size of original solute grid in I direction
	 */
	private static int _nI;
	
	/**
	 * Size of original solute grid in J direction
	 */
	private static int _nJ;
	
	/**
	 * Size of original solute grid in K direction
	 */
	private static int _nK;

	private PDEWrapper manager;
	
	/**
	 * \brief Create a Multigrid solute for each solute being processed by a
	 * solver.
	 * 
	 * @param aSolute	The solute grid containing the concentrations of this
	 * solute.
	 * @param relDiff	Diffusivity grid for this solute.
	 * @param bLayer	Boundary layer.
	 * @param sBulk	Max level of this solute in the bulk.
	 */
	public MultigridSolute(SoluteGrid aSolute, MultigridSolute relDiff,
										MultigridSolute bLayer, Double sBulk, PDEWrapper wrap)
	{
		realGrid = aSolute;
		soluteName = realGrid.gridName;

		_nI = realGrid.getGridSizeI();
		_nJ = realGrid.getGridSizeJ();
		_nK = realGrid.getGridSizeK();

		setReferenceSide();

		this.manager = wrap;

		this.sBulkMax = sBulk;
		this.sBulk = sBulk;

		_relDiff = relDiff._conc;
		_bLayer = bLayer._conc;

		_conc = new SoluteGrid[maxOrder];
		_rhs = new SoluteGrid[maxOrder];
		_reac = new SoluteGrid[maxOrder];
		_diffReac = new SoluteGrid[maxOrder];
		_itemp = new SoluteGrid[maxOrder];
		_itau = new SoluteGrid[maxOrder];
		_tempRes = new SoluteGrid[maxOrder];

		_res = new double[maxOrder];

		for (int iGrid = 0; iGrid<maxOrder; iGrid++)
		{
			_i = (_nI-1)/ExtraMath.exp2(iGrid)+1;
			_j = (_nJ-1)/ExtraMath.exp2(iGrid)+1;
			_k = (_nK-1)/ExtraMath.exp2(iGrid)+1;
			double r = _referenceSystemSide/referenceIndex(_i,_j,_k);

			// Padding is automatically generated by the constructor
			_conc[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute, sBulk);
			_rhs[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);
			_reac[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);
			_diffReac[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);
			_itemp[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);
			_itau[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);
			_tempRes[maxOrder-iGrid-1] = new SoluteGrid(_i, _j, _k, r, aSolute);

			_res[maxOrder-iGrid-1] = Double.MAX_VALUE;
		}
	}

	/**
	 * \brief Constructor used for biomass, bLayer and relative diffusivity
	 * grids.
	 * 
	 * @param aSolute	SoluteGrid to be used by the Multigrid
	 * @param gridName	Name of the solute grid
	 */
	public MultigridSolute(SoluteGrid aSolute, String gridName, PDEWrapper wrap)
	{
		soluteName = gridName;
		realGrid = aSolute;

		this.manager = wrap;

		_nI = aSolute.getGridSizeI();
		_nJ = aSolute.getGridSizeJ();
		_nK = aSolute.getGridSizeK();
		
		setReferenceSide();
		_conc = new SoluteGrid[maxOrder];

		for (int iGrid = 0; iGrid<maxOrder; iGrid++)
		{
			int i = (_nI-1)/ExtraMath.exp2(iGrid)+1;
			int j = (_nJ-1)/ExtraMath.exp2(iGrid)+1;
			int k = (_nK-1)/ExtraMath.exp2(iGrid)+1;

			double r = _referenceSystemSide/referenceIndex(i,j,k);
			if( Log.shouldWrite( Log.Tier.DEBUG ) )
				Log.out( Log.Tier.DEBUG,
						"MGsolute: " + r + "  " + i + "  " + iGrid + " " + _referenceSystemSide );
			// with padding for boundary conditions
			_conc[maxOrder-iGrid-1] = new SoluteGrid(i, j, k, r, aSolute);
		}
	}

	/**
	 * \brief Beginning of each nested loop.
	 * 
	 * @param order	Integer noting the order of process
	 */
	public void initLoop(int order)
	{
		MultigridUtils.interpolateBoundaryLayer(_conc[order],
										_conc[order-1], _bLayer[order].grid);
		// Set each solute's r.h.s. to 0
		_rhs[order].resetToZero();
	}

	public void downward(int order, int outer)
	{
		MultigridUtils.restrictBoundaryLayer(_conc[order], _conc[order-1], _bLayer[order-1].grid);
		//
		computeResidual(_itemp, order);
		//
		MultigridUtils.restrictBoundaryLayer(_itemp[order], _itemp[order-1], _bLayer[order-1].grid);
		// reduce grid value _g temporarily
		order--;
		computeResidual(_itau, order);
		MultigridUtils.subtractTo(_itau[order].grid, _itemp[order].grid);
		
		// sum tau to rhs of _g - 1
		MultigridUtils.restrictBoundaryLayer(_rhs[order+1], _rhs[order], _bLayer[order].grid);
		MultigridUtils.addTo(_rhs[order].grid, _itau[order].grid);
		
		// compute the truncation error for this V-cycle
		// for all chemicals
		if ( order+1 == outer )
			truncationError = .3*MultigridUtils.computeNorm(_itau[order].grid);
	}
	
	/**
	 * 
	 * @param order
	 * @param outer
	 */
	public void downward1(int order, int outer)
	{
		MultigridUtils.restrictBoundaryLayer(_conc[order], _conc[order-1], _bLayer[order-1].grid);
		//
		computeResidual(_itemp, order);
		//
		MultigridUtils.restrictBoundaryLayer(_itemp[order], _itemp[order-1], _bLayer[order-1].grid);
	}
	
	/**
	 * 
	 * @param order
	 * @param outer
	 */
	public void downward2(int order, int outer) {
		// reduce grid value _g temporarily
		order--;
		computeResidual(_itau, order);
		MultigridUtils.subtractTo(_itau[order].grid, _itemp[order].grid);

		// sum tau to rhs of _g - 1
		MultigridUtils.restrictBoundaryLayer(_rhs[order+1], _rhs[order], _bLayer[order].grid);

		MultigridUtils.addTo(_rhs[order].grid, _itau[order].grid);

		// compute the truncation error for this V-cycle
		// for all chemicals
		if ( order+1 == outer )
			truncationError = 0.3*MultigridUtils.computeNorm(_itau[order].grid);
	}
	
	/**
	 * 
	 * @param order
	 */
	public void upward(int order)
	{
		MultigridUtils.restrictBoundaryLayer(_conc[order], _itemp[order-1], _bLayer[order-1].grid);
		MultigridUtils.subtractTo(_conc[order-1].grid, _itemp[order-1].grid);
		MultigridUtils.interpolateBoundaryLayer(_itau[order], _conc[order-1], _bLayer[order].grid);
		MultigridUtils.addTo(_conc[order].grid, _itau[order].grid);
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */
	public boolean breakVCycle(int order)
	{
		/* Absolute concentration that is negligible,
		ratio at which the residual is small relative to the concentration. */
		double NEGLIGIBLE = Global.negligible_change;
		double RELATIVE = manager.solverResidualRatioThreshold; // fast: 1e-3, fine 1e-4

		if (order != _tempOrder) {
			_tempOrder = order;
			_tempNums = new LinkedList<Double>();
		}

		/* We obtain data on mgFas concentrations and
		residuals to decide when to stop the cycle */
		double[][][] ratio = Array.copy( this._conc[order].grid );
		Array.restrictMinimum( ratio, NEGLIGIBLE / RELATIVE );
		Array.elemRatioTo( ratio, _tempRes[order].grid, ratio);
		double maxRatio = MultigridUtils.largestRealNonZero( ratio , NEGLIGIBLE);
		double locResidual = MultigridUtils.largestRealNonZero( _tempRes[order].grid ,
				NEGLIGIBLE * NEGLIGIBLE );
		double smallestConc = MultigridUtils.smallestNonZero( this._conc[order].grid ,
				NEGLIGIBLE * NEGLIGIBLE ); // only for reporting

		if ( Log.shouldWrite( Log.Tier.EXPRESSIVE ) )
			System.out.println( this.soluteName + " order: " + order +
					", ratio: " + maxRatio + ", smallest concentration: " +
					smallestConc + ", max local residual: " + locResidual );

		if ( Log.shouldWrite(Log.Tier.DEBUG) ) {
			Log.out(Log.Tier.DEBUG,
					this.getClass().getSimpleName() + " vCycle stagnated:\n "
							+ "\torder: " + order + ", ratio: " + maxRatio
							+ ", \n\tsmallest concentration: " + smallestConc
							+ ", \n\tmax local residual: " + locResidual);
		}

		/* Stopping because of converged outcome */

		/* The local residuals are small in relation to the local concentration. */
		if( ( maxRatio <= RELATIVE ) ) {
			return true;
		}

		/* Debugging stagnating solver scenarios */

		/* Diminishing change in residual, the solver seems to have stopped converging. */
		if ( Log.shouldWrite(Log.Tier.DEBUG) ||
				almostEqual( _res[order], locResidual,locResidual * 1e-6 ) ) {
			Log.out( Log.Tier.CRITICAL, this.soluteName + " stagnant Vcycle in "
					+ this.getClass().getSimpleName() + " residual res: " + locResidual );
		}
		this._res[order] = locResidual;

		/* The solver loops in the same patern, the solver seems to have stopped converging. */
		if( Log.shouldWrite( Log.Tier.DEBUG ) ) {
			for (Double d : _tempNums)
				if ( almostEqual( d, maxRatio, maxRatio * 1e-6 ) ) {
					Log.out(Log.Tier.CRITICAL, this.soluteName + " repeated number patern in "
							+ this.getClass().getSimpleName() + ": " + maxRatio);
					return true;
				}
			_tempNums.add(maxRatio);
		}

//		computeResidual(_itemp, order); //assigns lop to _itemp.
//		MultigridUtils.subtractTo(_itemp[order].grid, _rhs[order].grid);
//		Double res = MultigridUtils.computeNorm(_itemp[order].grid);
//		if( Log.shouldWrite(Log.Tier.DEBUG))
//			Log.out( Log.Tier.DEBUG,  "mgFasTruncation: " + truncationError);
//		if( res <= truncationError )
//		{
//			System.out.println("mgFas stop condi: truncation error passed residual.");
//			return true;
//		}
		return false;
	}

	public static boolean almostEqual(double a, double b, double eps){
		return Math.abs(a-b)<eps;
	}

	/**
	 * 
	 * @param order
	 * @return
	 */
	public double[][][] relax(int order)
	{
		int nI = _conc[order].getGridSizeI();
		int nJ = _conc[order].getGridSizeJ();
		int nK = _conc[order].getGridSizeK();
		
		Double h = _referenceSystemSide/referenceIndex(nI, nJ, nK);
		Double h2i = 0.5f/(h*h);
		// red-black relaxation
		// iterate through system
		// isw, jsw and ksw alternate between values 1 and 2
		
		double[][][] difference = new double[nI][nJ][nK];
		u = _conc[order].grid;
		bl = _bLayer[order].grid;
		rd = _relDiff[order].grid;
		
		Double lop, dlop, res;
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
							fillDiff();
							
							// compute L operator
							lop = computeLop(order, h2i);
							
							// compute derivative of L operator
							dlop = computeDiffLop(order, h2i);
							
							// compute residual
							res = (lop-_rhs[order].grid[_i][_j][_k])/dlop;

							double absRes = Math.abs(res);
							totalRes += absRes;
							difference[_i - 1][_j - 1][_k - 1] = absRes;
							// update concentration (test for NaN)
							//LogFile.writeLog("NaN generated in multigrid solver "+"while computing rate for "+soluteName);
							//LogFile.writeLog("location: "+_i+", "+_j+", "+_k);
							//LogFile.writeLog("dlop: "+dlop+"; lop: "+lop+"; grid: "+_rhs[order].grid[_i][_j][_k]);

							_tempRes[order].grid[_i][_j][_k] = res;

							u[_i][_j][_k] -= res;
							// if negative concentrations, put 0 value
							u[_i][_j][_k] = (u[_i][_j][_k]<0 ? 0 : u[_i][_j][_k]);
						}
					}
				}
			}
			_conc[order].refreshBoundary();
			// refresh the padding elements to enforce
			// boundary conditions for all solutes
//			this.setWellmixed(order);
		}

		if (!_conc[order]._recordKeeper.isEmpty())
			for (RecordKeeper r : _conc[order]._recordKeeper)
				r.step(u, order, this.soluteName);
		
		return difference;
	}
	
	/**
	 * 
	 */
	private void fillDiff()
	{
		//TODO shouldn't this be a diffusivity grid?
		_diff[0][1][1] = realGrid.diffusivity*rd[_i-1][_j][_k];
		_diff[2][1][1] = realGrid.diffusivity*rd[_i+1][_j][_k];
		_diff[1][0][1] = realGrid.diffusivity*rd[_i][_j-1][_k];
		_diff[1][2][1] = realGrid.diffusivity*rd[_i][_j+1][_k];
		_diff[1][1][0] = realGrid.diffusivity*rd[_i][_j][_k-1];
		_diff[1][1][2] = realGrid.diffusivity*rd[_i][_j][_k+1];
		_diff[1][1][1] = realGrid.diffusivity*rd[_i][_j][_k];
	}
	
	/**
	 * \brief Compute the L-operator
	 * 
	 * @param order
	 * @param h2i
	 * @return
	 */
	private double computeLop(int order, Double h2i)
	{	
		return ( (_diff[2][1][1]+_diff[1][1][1])*(u[_i+1][_j][_k]-u[_i][_j][_k])
		        +(_diff[0][1][1]+_diff[1][1][1])*(u[_i-1][_j][_k]-u[_i][_j][_k])
		        +(_diff[1][2][1]+_diff[1][1][1])*(u[_i][_j+1][_k]-u[_i][_j][_k])
		        +(_diff[1][0][1]+_diff[1][1][1])*(u[_i][_j-1][_k]-u[_i][_j][_k])
		        +(_diff[1][1][2]+_diff[1][1][1])*(u[_i][_j][_k+1]-u[_i][_j][_k])
		        +(_diff[1][1][0]+_diff[1][1][1])*(u[_i][_j][_k-1]-u[_i][_j][_k]))
		        *h2i + _reac[order].grid[_i][_j][_k];
	}
	
	/**
	 * 
	 * @param order
	 * @param h2i
	 * @return
	 */
	private Double computeDiffLop(int order, Double h2i)
	{
		return -h2i
		        *(6.0f*_diff[1][1][1]
		              +_diff[2][1][1]+_diff[0][1][1]
		              +_diff[1][2][1]+_diff[1][0][1]
		              +_diff[1][1][2]+_diff[1][1][0])
		       +_diffReac[order].grid[_i][_j][_k];
	}
	
	/**
	 * 
	 * @param res
	 * @param order
	 */
	private void computeResidual(SoluteGrid[] res, int order)
	{
		int nI = res[order].getGridSizeI();
		int nJ = res[order].getGridSizeJ();
		int nK = res[order].getGridSizeK();

		// h = gridsize
		Double h = _referenceSystemSide/referenceIndex(nI,nJ,nK);
		Double h2i = 0.5f/(h*h);
		Double lop; // temporary variable for L-operator
		
		u = _conc[order].grid;
		bl = _bLayer[order].grid;
		rd = _relDiff[order].grid;
		
		// iterate through system
		for (_k = 1; _k <= nK; _k++)
			for (_j = 1; _j <= nJ; _j++)
				for (_i = 1; _i <= nI; _i++)
					// compute lop only inside boundary layer
					if (bl[_i][_j][_k] >= BLTHRESH)
					{
						// compute diffusivity values and that of surrounding
						// neighbours
						fillDiff();
						
						// compute L operator
						lop = computeLop(order, h2i);
						
						// update concentration (test for NaN)
						//LogFile.writeLog("MultigridSolute.computeResidual: NaN generated"+soluteName);
						res[order].grid[_i][_j][_k] = lop;
					}
		res[order].refreshBoundary();
	}
	
	/**
	 *
	 * @param order
	 */
	public void truncateConcToZero(int order)
	{
		int nI = _conc[order].getGridSizeI();
		int nJ = _conc[order].getGridSizeJ();
		int nK = _conc[order].getGridSizeK();
		double[][][] bl = _bLayer[order].grid;
		double[][][] u = _conc[order].grid;
		
		for (int _i = 1; _i <= nI; _i++)
			for (int _j = 1; _j <= nJ; _j++)
				for (int _k = 1; _k <= nK; _k++)
					if (bl[_i][_j][_k] >= BLTHRESH)
						u[_i][_j][_k] = Math.max(u[_i][_j][_k], 0.0);
	}
	
	/* _________________________ TOOLBOX ____________________________ */
	/**
	 * 
	 * @param value
	 */
	public void resetMultigridCopies(Double value)
	{
		for (int order = 0; order < maxOrder; order++)
			_conc[order].setAllValueAt(value);
	}
	
	/**
	 * 
	 */
	public void resetMultigridCopies()
	{
		this._stage = 0;

		for (int order = 0; order < maxOrder; order++)
		{
			// TODO Test whether changes lead to issues
			// NOTE: the solution of the previous timestep should be a better starting point than
			// the bulk concentrations!
			// NOTE: removing this breaks the solver
			setWellmixed(order);
			_itau[order].resetToZero();
			_itemp[order].resetToZero();
			 _reac[order].resetToZero();
			 _diffReac[order].resetToZero();
			_rhs[order].resetToZero();
			_res[order] = Double.MAX_VALUE;
		}
	}

	/**
	 *
	 */
	public int getStage()
	{
		return this._stage;
	}

	/**
	 * 
	 * @param value
	 */
	public void resetFinest(Double value)
	{
		_conc[maxOrder-1].setAllValueAt(value);
	}
	
	/**
	 * 
	 * @param order
	 */
	public void resetReaction(int order)
	{
		_reac[order].resetToZero();
		_diffReac[order].resetToZero();
	}
	
	/**
	 * Set all grids elements to the value defined for Bulk. For elements
	 * located in the convective part (i.e. outside the BLayer, we take the
	 * value defined in the BulkBoundary Class)
	 */
	public void setSoluteGridToBulk(int order)
	{
		int maxI = _conc[order].getGridSizeI();
		int maxJ = _conc[order].getGridSizeJ();
		int maxK = _conc[order].getGridSizeK();
		
		for (_i = 1; _i <= maxI; _i++)
			for (_j = 1; _j <= maxJ; _j++) 
				for (_k = 1; _k <= maxK; _k++)
				{
					if (_bLayer[order].grid[_i][_j][_k] <= BLTHRESH)
					{
						// outside the boundary layer (will not be solved)
						_conc[order].grid[_i][_j][_k] = sBulk;
					}
					else
					{
						// inside the biofilm (value is not really important
						// now)
						_conc[order].grid[_i][_j][_k] = sBulkMax;
					}
				}
	}

	public void setWellmixed(int order) {
		int maxI = _conc[order].getGridSizeI();
		int maxJ = _conc[order].getGridSizeJ();
		int maxK = _conc[order].getGridSizeK();

		for (_i = 1; _i <= maxI; _i++)
			for (_j = 1; _j <= maxJ; _j++)
				for (_k = 1; _k <= maxK; _k++) {
					if (_bLayer[order].grid[_i][_j][_k] <= BLTHRESH) {
						// outside the boundary layer (will not be solved)
						_conc[order].grid[_i][_j][_k] = sBulk;
					}
				}
	}

	/**
	 * 
	 * @return
	 */
	public SoluteGrid getFinest()
	{
		return _conc[maxOrder-1];
	}
	
	/**
	 * Called by the chemostat solver.
	 * 
	 * @return
	 */
	public SoluteGrid getGrid()
	{
		return _conc[0];
	}
	
	/**
	 * 
	 * @param aGrid
	 */
	public void setFinest(SoluteGrid aGrid)
	{
		_conc[maxOrder-1] = aGrid;
	}
	
	/**
	 * 
	 */
	public void restrictToCoarsest()
	{
		for (int order = maxOrder - 1; order > 0; order--)
		{
			_conc[order-1].resetToZero();
			MultigridUtils.restrict(_conc[order], _conc[order-1]);
		}
	}
	
	/**
	 * Determine order of the finest grid.
	 */
	public void setReferenceSide()
	{
		_referenceSystemSide = Double.valueOf(Math.min(_nI, _nJ));
		if (_nK>1) _referenceSystemSide = Math.min(_referenceSystemSide, _nK);

		maxOrder = ExtraMath.log2(_referenceSystemSide).intValue();

		/*
		 * Switch from node system to voxel system (subtract 1)
		 */
		_referenceSystemSide -= 1;
		_referenceSystemSide *= realGrid.getResolution();
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
	
	/**
	 * 
	 */
	public void applyComputation()
	{
		Array.copyTo(realGrid.grid, _conc[maxOrder-1].grid);
	}
	
	/**
	 * 
	 */
	public void readSoluteGrid()
	{
		Array.copyTo(_conc[maxOrder-1].grid, realGrid.grid);
	}
	
	/**
	 * Update bulk concentration.
	 */
	public void readBulk()
	{
		this.sBulk = manager.fetchBulk(this.soluteName);
		for( SoluteGrid s : this._conc) {
			s.updateBulk( sBulk );
		}
	}
}