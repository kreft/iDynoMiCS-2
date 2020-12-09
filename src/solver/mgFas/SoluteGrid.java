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

import grid.ArrayType;
import grid.SpatialGrid;
import settable.Settable;
import shape.Shape;

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

		///////////////////////////
		// TODO set diffusivity
		grid.getArray(ArrayType.DIFFUSIVITY);
		
		/*
		 * Set the initial concentration.
		 */
		
		///////////////////////////
		// TODO set concentration

		grid.getArray(ArrayType.CONCN);
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
		 * copy from iDyno 2 grid.
		 */
		this.grid = grid.getArray(type);
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
	public SoluteGrid(int nI, int nJ, int nK, double res)
	{
		super(nI, nJ, nK, res);
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
		double[] lengths = _domain.getShape().getRealLengths();
		
		_reso = getRes();
		_nI = (int) Math.ceil(lengths[0]/_reso);
		_nJ = (int) Math.ceil(lengths[1]/_reso);
		_nK = (int) Math.ceil(lengths[1]/_reso);
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
		 * TODO refresh boundaries
		for (AllBC aBC:_domain.getAllBoundaries()) 
				aBC.refreshBoundary(this);
				
		*/
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
}
