/**
 * \package simulator
 * \brief Package of classes that create a simulator object and capture
 * simulation time.
 * 
 * This package is part of iDynoMiCS v1.2, governed by the CeCILL license
 * under French law and abides by the rules of distribution of free software.  
 * You can use, modify and/ or redistribute iDynoMiCS under the terms of the
 * CeCILL license as circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info".
 */
package solver.mgFas;

import java.util.LinkedList;

import boundary.WellMixedBoundary;
import grid.ArrayType;
import grid.SpatialGrid;
import linearAlgebra.Array;
import settable.Settable;
import shape.Dimension;
import solver.mgFas.boundaries.CartesianPadding;

/**
 * \brief Class for containing chemical solutes, that are represented by a
 * grid.
 * 
 * The grid is a padded 2D or 3D grid as specified in the protocol file,
 * unless the simulation is being run in chemostat conditions. Solute
 * diffusivity is expressed in the local time unit.
 * 
 * @since June 2006
 * @version 1.2
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre
 * for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of
 * Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 */
public class SoluteGrid extends SolverGrid 
{
	/**
	 * Serial version used for the serialisation of the class
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * All solute names are stored in a simulation dictionary. This holds the
	 * position of this solute in this list.
	 */
	public int soluteIndex;
	
	/**
	 * Diffusivity of this solute in water, if specified in the protocol file.
	 */
	public double diffusivity;
	
	/**
	 * Computation domain this solute is associated with. Provides description
	 * of diffusion, carrier, and bulk domains for this solute.
	 */
	private Domain _domain;
	
	protected LinkedList<RecordKeeper> _recordKeeper = new LinkedList<RecordKeeper>();



	public double bulk = 0.0;
	/*************************************************************************
	 * CLASS METHODS 
	 ************************************************************************/

	/**
	 * \brief Creates a solute grid for a solute specified in the simulation
	 * protocol file.
	 * 
	 * Each of the solutes specified in the XML protocol file exists in
	 * iDynoMiCS within its own solute grid. This constructor is used by
	 * createSolutes (in the Simulator class) to create the grids for each
	 * solute. Once created, the grid is populated with the initial
	 * concentration value of this solute.
	 */
	public SoluteGrid(Domain domain, String name, SpatialGrid grid, Settable parent)
	{
		/*
		 * Name the grid
		 */
		gridName = name;
		/*
		 * All solute names are stored in a simulation dictionary. Get the
		 * position of this solute in this list.
		 */
//		soluteIndex = aSim.getSoluteIndex(gridName);
		/*
		 * Get the computation domain in which this solute exists and store
		 * locally.
		 */
		_domain = domain;
		/*
		 * Now to set the resolution and create the grid. First check whether
		 * a specific resolution has been set for this grid.
		 */
		useDomaingrid();
		/*
		 * Now initialise the grid - setting the grid to the required size
		 */
		initGrids();
		/*
		 * Set the diffusivity - if specified in the XML file
		 */

//		///////////////////////////
//		// TODO set diffusivity
//		grid.getArray(ArrayType.DIFFUSIVITY);

		// Note seems to be taken from chemostat???
		diffusivity = grid.getDiffusivity();
		/*
		 * Set the initial concentration.
		 */

//		///////////////////////////
//		// TODO set concentration
//
		double[][][] in = grid.getArray(ArrayType.CONCN);
//		Array.setAll(this.grid, in[0][0][0]);
		Array.setAll(new double[in.length-1][in[0].length-1][in[0][0].length-1], in[0][0][0]);
	}

	public SoluteGrid(Domain domain, String name, ArrayType type, SpatialGrid grid)
	{
		/*
		 * Name the grid
		 */
		gridName = name;
		/*
		 * All solute names are stored in a simulation dictionary. Get the
		 * position of this solute in this list.
		 */
//		soluteIndex = aSim.getSoluteIndex(gridName);
		/*
		 * Get the computation domain in which this solute exists and store
		 * locally.
		 */
		_domain = domain;
		/*
		 * Now to set the resolution and create the grid. First check whether
		 * a specific resolution has been set for this grid.
		 */
		useDomaingrid();

		/*
		 * Set the diffusivity - if specified in the XML file
		 */

		/*
		 * Now initialise the grid - setting the grid to the required size
		 */
		initGrids();
	}
	
	/**
	 * \brief Constructor used to establish a solute grid when creating
	 * multigrids.
	 * 
	 * Simply calls the SpatialGrid constructor.
	 * 
	 * @param nI	Number of grid elements in X direction of grid.
	 * @param nJ	Number of grid elements in Y direction of grid.
	 * @param nK	Number of grid elements in Z direction of grid.
	 * @param res	The width of each grid element (in micrometres).
	 */
	public SoluteGrid(Domain domain, int nI, int nJ, int nK, double res)
	{
		super(nI, nJ, nK, res);
		this._domain = domain;
	}
	
	/**
	 * \brief Creates a solute grid of a specified size and resolution, and
	 * with a given name. Example - Used to define computation domain.
	 * 
	 * Creates a solute grid of a specified size and resolution, as calculated
	 * by the Domain class, and with the name specified in the protocol file.
	 * 
	 * @param nI	Number of grid elements in X direction of grid.
	 * @param nJ	Number of grid elements in Y direction of grid.
	 * @param nK	Number of grid elements in Z direction of grid.
	 * @param res	The width of each grid element (in micrometres).
	 * @param aName	The type of grid being created (e.g. domainGrid).
	 * @param aDomain	The computation domain to which this grid is part of.
	 */
	public SoluteGrid(int nI, int nJ, int nK, double res, String aName,
															   Domain aDomain)
	{
		super(nI, nJ, nK, res);
		gridName = aName;
		_domain = aDomain;
	}

	/**
	 * \brief Class for creating a new solute grid using a previous solute
	 * grid.
	 * 
	 * @param nI 	Number of grid elements in X direction of grid.
	 * @param nJ	Number of grid elements in Y direction of grid.
	 * @param nK	Number of grid elements in Z direction of grid.
	 * @param res	The width of each grid element (in micrometres).
	 * @param aSolG	The solute grid to use to create this grid.
	 */
	public SoluteGrid(int nI, int nJ, int nK, double res, SoluteGrid aSolG)
	{
		super(nI, nJ, nK, res);
		useExternalSoluteGrid(aSolG);
	}

	public SoluteGrid(int nI, int nJ, int nK, double res, SoluteGrid aSolG, double initial)
	{
		super(nI, nJ, nK, res);
		useExternalSoluteGrid(aSolG);

		this.setAllValueAt(initial);

	}

	/**
	 * \brief Initialise a solute grid based on the properties of another
	 * provided grid.
	 * 
	 * @param aSolG	Solute grid on which to base a new solute grid.
	 */
	public SoluteGrid(SoluteGrid aSolG)
	{
		gridName = aSolG.gridName;
		diffusivity = aSolG.diffusivity;
		_domain = aSolG._domain;
		/*
		 * 
		 */
		_reso = aSolG.getResolution();
		_nI = aSolG.getGridSizeI();
		_nJ = aSolG.getGridSizeJ();
		_nK = aSolG.getGridSizeK();
		/*
		 * 
		 */
		initGrids();
	}

	public SoluteGrid(SoluteGrid aSolG, double initial)
	{
		gridName = aSolG.gridName;
		diffusivity = aSolG.diffusivity;
		_domain = aSolG._domain;
		/*
		 *
		 */
		_reso = aSolG.getResolution();
		_nI = aSolG.getGridSizeI();
		_nJ = aSolG.getGridSizeJ();
		_nK = aSolG.getGridSizeK();
		/*
		 *
		 */
		initGrids();
	}

	/**
	 * \brief Initialise a new solute grid, cloning the properties of a
	 * provided solute grid.
	 * 
	 * @param aSolG	Solute grid for which the properties are being copied for
	 * this new grid.
	 */
	public void useExternalSoluteGrid(SoluteGrid aSolG) 
	{
		gridName = aSolG.gridName;
		soluteIndex = aSolG.soluteIndex;
		diffusivity = aSolG.diffusivity;
		_domain = aSolG._domain;
	}
	
	/**
	 * \brief Use the size and the resolution used to define the computation
	 * domain to define the solute grid.
	 * 
	 * Use the size and the resolution used to define the computation domain
	 * to define the solute grid. This is in cases where the protocol file
	 * does not specifically specify a resolution to use for this solute.
	 */
	public void useDomaingrid() 
	{
		_reso = _domain.getGrid().getResolution();
		_nI = _domain.getGrid().getGridSizeI();
		_nJ = _domain.getGrid().getGridSizeJ();
		_nK = _domain.getGrid().getGridSizeK();
	}

	public double getRes()
	{
		/* TODO now taking the smallest, but actually we should check wether they match together. */
		return
				Math.min( Math.min(_domain.getShape().getResolutionCalculator(null, 0).getResolution(),
				_domain.getShape().getResolutionCalculator(null, 1).getResolution() ),
				_domain.getShape().getResolutionCalculator(null, 2).getResolution() );
	}
	
	/*************************************************************************
	 * MAIN METHODS 
	 ************************************************************************/
	
	/**
	 * \brief Examines all objects at the boundary of the grid, and adjusts
	 * them as specified by the boundary condition rules.
	 */
	public void refreshBoundary()
	{
		/*
		* Three possibilities: periodic, constant concentration, zero flux
		*/
		CartesianPadding pad = new CartesianPadding(_nI, _nJ, _nK );

		/* TODO accessing dimensions is overly complex, simplify */
		for( Dimension.DimName d : this._domain.getShape().getDimensionNames() )
		{
			Dimension dim = this._domain.getShape().getDimension( d );
			if ( dim.isCyclic() )
			{
				pad.cyclic(this, d.dimNum(), true);
				pad.cyclic(this, d.dimNum(), false);
			}
			else {
				if ( dim.isBoundaryDefined(0) && dim.getBoundary(0) instanceof WellMixedBoundary) {
					pad.constantConcentration(this, d.dimNum(), false, bulk);

				} else {
					pad.zeroFlux(this, d.dimNum(), false);
				}

				if (dim.isBoundaryDefined(1) && dim.getBoundary(1) instanceof WellMixedBoundary) {
					pad.constantConcentration(this, d.dimNum(), true, bulk);
				} else {
					pad.zeroFlux(this, d.dimNum(), true);
				}
			}
		}
//
//		/* solid bound */
//		pad.zeroFlux( this, 1, false);
//
//		/*	bulk boundary */
//		pad.constantConcentration( this, 1, true, bulk);
//
//		/* cyclic bound 1 */
//		pad.cyclic( this, 0, false);
//
//		/* cyclic bound 2 */
//		pad.cyclic( this, 0, true);
//
//		/* virtual z dimension (Thanks Tim!) */
//		pad.zeroFlux( this, 2, false);
//		pad.zeroFlux( this, 2, true);

	}

	/**
	 * \brief Returns the name of this solute grid.
	 * 
	 * Name was specified in the protocol file.
	 * 
	 * @return	String value representing the name of this grid.
	 */
	public String getName() 
	{
		return gridName;
	}
	
	/**
	 * \brief Returns the diffusivity of the solute in this grid.
	 * 
	 * Diffusivity was specified in the protocol file.
	 * 
	 * @return	Double value representing the diffusivity in water of this
	 * solute.
	 */
	public double getDiffusivity() 
	{
		return diffusivity;
	}
	
	/**
	 * \brief Returns the computation domain that this solute is associated
	 * with.
	 * 
	 * @return	Computation domain associated with the solute in this grid.
	 */
	public Domain getDomain() 
	{
		return _domain;
	}

	public void updateBulk(double bulk)
	{
		this.bulk = bulk;
	}
	
	public void setRecordKeeper(RecordKeeper r)
	{
		this._recordKeeper.add(r);
	}
}
