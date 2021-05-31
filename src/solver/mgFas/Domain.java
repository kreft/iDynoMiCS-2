/**
 * \package simulator.geometry
 * \brief Package of boundary utilities that aid the creation of the environment being simulated
 * 
 * Package of boundary utilities that aid the creation of the environment being simulated. This package is 
 * part of iDynoMiCS v1.2, governed by the CeCILL license under French law and abides by the rules of distribution of free software.  
 * You can use, modify and/ or redistribute iDynoMiCS under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at 
 * the following URL  "http://www.cecill.info".
 */
package solver.mgFas;

import java.util.*;

import compartment.EnvironmentContainer;
import grid.ArrayType;
import shape.Shape;
import solver.mgFas.boundaries.AllBC;
import solver.mgFas.utils.ContinuousVector;
import solver.mgFas.utils.DiscreteVector;
import utility.ExtraMath;

/**
 * \brief Define the computation domain: an evenly spaced rectilinear grid
 * described by its dimensionality (2D or 3D), its size, geometry and the
 * behaviour at its boundaries.
 * 
 * See Figure 1 of the Lardon et al paper (2011) for a good description on how
 * this is divided into several regions - the support, the bulk, the biofilm
 * matrix, and the diffusion boundary layer. 
 * 
 * @since June 2006
 * @version 1.2
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre
 * for Infection Research (Germany).
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France.
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of
 * Engineering Sciences and Applied Mathematics, Northwestern University (USA).
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology,
 * University of Birmingham (UK).
 * @author Kieran Alden (k.j.alden@bham.ac.uk), Centre for Systems Biology,
 * University of Birmingham (UK).
 */
public class Domain
{
	/**
	 * Serial version used for the serialisation of the class.
	 */
	private static final long serialVersionUID    = 1L;

	/**
	 * Name of this computation domain, as supplied in the specified protocol
	 * file.
	 */
	public String	domainName;
	
	private Shape _shape;
	
	/**
	 * Domain X dimension in micrometers.
	 */
	public double length_X;
	
	/**
	 * Domain Y dimension in micrometers.
	 */
	public double length_Y;
	
	/**
	 * Domain Z dimension in micrometers.
	 */
	public double length_Z;
	
	/**
	 * Whether this computation domain is two or three dimensional.
	 */
	public boolean is3D;
	
	/**
	 * Number of grid elements in the x direction.
	 */
	public int _nI;
	
	/**
	 * Number of grid elements in the y direction.
	 */
	public int _nJ;
	
	/**
	 * Number of grid elements in the z direction.
	 */
	public int _nK;
	
	/**
	 * 
	 */
	protected int _i;
	
	/**
	 * 
	 */
	protected int _j;
	
	/**
	 * 
	 */
	protected int _k;

	/**
	 * Width of each side of the grid element (in micrometres).
	 */
	public double _resolution;
	
	/**
	 * The solute grid that is a component of this computation domain.
	 */
	protected SolverGrid _domainGrid;
	
	/**
	 * Boundary layer between bulk and biofilm
	 */
	protected SoluteGrid _boundaryLayer;
	
	/**
	 * 	Diffusivity of solutes in each area of this domain
	 */
	protected SoluteGrid _diffusivityGrid;

	/**
	 * List of all boundaries defined on this computation domain.
	 */
	private LinkedList<AllBC> _boundaryList = new LinkedList<AllBC>();
	
	/**
	 * Array to hold the X position that is the top of the boundary layer in
	 * each of the Y and Z positions. Used for self-attach scenarios.
	 */
	public int[][] _topOfBoundaryLayer;
	
	/**
	 * Grid to hold total biomass in a particular area.
	 */
	public SoluteGrid _biomassGrid;
	
	/**
	 * Band between the boundary and bulk, capturing change in diffusivity and
	 * solute levels.
	 */
	protected Double _dilationBand = 0.0; //I've set this default to 0 for now
	
	/**
	 * The ratio between the carrier surface (the substratum on which the
	 * biofilm grows) and the bulk compartment volume. The physical volume of
	 * the system that appears in the simulation definition. In m2/m3.
	 */
	protected double specificArea;
	
	/**
	 * Factor used to decrease solute diffusivity inside the biofilm.
	 * Multiplicative factor applied to the diffusivity in water.
	 */
	protected double _biofilmDiffusivity = 1.0;

	private EnvironmentContainer _environment;

	/*************************************************************************
	 * CLASS METHODS 
	 ************************************************************************/
	
	/**
	 * \brief Creates a computation domain compartment object with attributes
	 * specified in the protocol file.
	 * 
	 * The definition within the computationDomain markup of the protocol file
	 * notes how these regions are set up. This constructor sets up each
	 * computation domain that is specified in the protocol file.
	 *
	 */
	public Domain(Shape shape, EnvironmentContainer environment)
	{
		this._shape = shape;
		// Now determine if this computation domain is 2D or 3D
		is3D = shape.getSignificantDimensions().size() == 3;

		/* FIXME enforce all dimensions to be of the same resolution. */
		_resolution = Math.min( Math.min(
				shape.getResolutionCalculator(null, 0).getResolution(),
				shape.getResolutionCalculator(null, 1).getResolution()),
				shape.getResolutionCalculator(null, 2).getResolution() );
		
		double[] lengths = (shape.getRealLengths());

		this._environment = environment;

		_nI = (int) Math.ceil(lengths[0]/_resolution);
		_nJ = (int) Math.ceil(lengths[1]/_resolution);
		_nK = (is3D) ? (int) Math.ceil(lengths[2]/_resolution) : 1;
		
		// Now calculate the length of the grid in micrometres.
		length_X = _nI * _resolution;
		length_Y = _nJ * _resolution;
		length_Z = _nK * _resolution;
		
		// Create and initialise the domain grid.
		_domainGrid = new SoluteGrid(_nI, _nJ, _nK, _resolution);
		
		// Specific area is given in m2/m3.
//		specificArea = cdRoot.getParamDbl("specificArea");
		
		// Create the boundary layer and initialise it at "inside everywhere".
//		_dilationBand = cdRoot.getParamLength("boundaryLayer");
		_boundaryLayer = createGrid( "boundaryLayer", 1);
		
		// Create the biomass MASS grid and initialise it empty.
		_biomassGrid = createGrid( "totalBiomass", 0.0);
		
		// Create the relative diffusivity grid and initialise it at "liquid
		// everywhere".
		_biofilmDiffusivity = 1.0;
		_diffusivityGrid = createGrid( "diffusivityGrid", 1);
		
		// Now comes the definition of the behavior at the boundaries.
		// In general, there are 6 boundaries that must be addressed: y0z, yNz,
		// x0z, xNz, x0y, xNy. These represent the edges of the domain along
		// the non-named direction (i.e. y0z is the face at x=0, and yNz is the
		// face at x=N). (For 2D simulations the x0y and xNy directions are
		// included, but are made periodic.) Each <boundaryCondition> also
		// includes a <shape> mark-up to define the shape of the boundary.
		// The below call combines all boundary conditions in the XML file,
		// then processes each.

		/** Building
		for (XMLParser aBCMarkUp : cdRoot.getChildrenParsers("boundaryCondition"))
			AllBC.staticBuilder(aBCMarkUp, aSim, this);
		**/

		// Note the above has added all the boundaries to the array _boundaryList
		// Now apply these boundaries

		// Build the domain grid : 0 outside, 1 inside, -1 carrier

		applyAllBoundary();
		// KA May 2013
		// Now we're going to initialise all these grids.
		// Note this wasn't previously done, but with self attachment we need
		// to know where the boundary layer is before any agents are added. The
		// function below was part of the refreshBioFilmGrids method - this now
		// exists independently and is called whenever these grids need to be
		// refreshed.
	}
	
	/**
	 * \brief Creates a solute or species grid and initialises the values
	 * within that grid.
	 * 
	 * Used to create boundary and biomass grids.
	 * 
	 * @param gridName The name of the grid being created (e.g. boundaryLayer /
	 * totalBioMass).
	 * @param defaultValue	The default value to assign to all grid spaces.
	 * @return	Initialised solute grid of the size required by the simulation,
	 * initialised to the given default value.
	 */
	public SoluteGrid createGrid(String gridName, double defaultValue)
	{
		SoluteGrid aGrid = new SoluteGrid(_nI, _nJ, _nK, _resolution);
		aGrid.setAllValueAt(defaultValue);
		return aGrid;
	}
	
	/**
	 * \brief Applies all specified domain boundaries to this computation
	 * domain one by one.
	 * 
	 * This method should be used when you have finished to register all
	 * boundary conditions; this function will populate a 3-D matrix. Apply all
	 * boundaries one after one ; a point is outside of the computational
	 * domain if it is declared outside by at least one of the Boundary 
	 * Conditions.
	 */
	public void applyAllBoundary()
	{
		/* TODO do boundaries */
		DiscreteVector dC = new DiscreteVector();
		ContinuousVector cC;

		// Reset all the computational domain to "inside";
		_domainGrid.setAllValueAt( 1.0 );

		for (int i = 0; i < _domainGrid.getGridTotalSize(1); i++)
			for (int j = 0; j < _domainGrid.getGridTotalSize(2); j++)
				loop:
					for (int k = 0; k < _domainGrid.getGridTotalSize(3); k++)
				{
					dC.set(i-1, j-1, k-1);
					cC = _domainGrid.getContinuousCoordinates(dC);
					for (AllBC aBC : _boundaryList)
					{
						// skip if this gridCell has already been updated
						if ( _domainGrid.getValueAt(i, j, k) == -1.0 )
							continue loop;
						// Test if this grid cell is seen outside
						if ( aBC.isOutside(dC, _domainGrid) )
						{
							_domainGrid.setValueAt(-1.0, i, j, k);
							continue loop;
						}
						// label carrier part of the domain
						if ( aBC.isSupport() &&
											aBC.getDistance(cC) < _resolution )
							_domainGrid.setValueAt(0.0, i, j, k);
					}
				}
	}
	
	/* _______________________ LOCATED AGENTS ______________________________ */
	
	/**
     * \brief Test if a given location is outside a boundary.
     * 
     * Used to detect the crossed boundary when moving an agent.
     * 
     * @param newLoc The location to test
     * @return Boundary that the point has crossed (if applicable: null if no
     * boundary crossed)
     */
	
	
//	public AllBC testCrossedBoundary(Double radius, ContinuousVector newLoc)
//	{
//		// Test on the domain grid if the new location is inside the domain
////		if (_domainGrid.isValid(newLoc) && _domainGrid.getPaddedValueAt(newLoc) >= 0)
////			return null;
//		
//		// Find the first of the boundaries which has been crossed
//		for (AllBC aBoundary : _boundaryList) {
//			if ((aBoundary instanceof BoundaryCyclic ? aBoundary.overBoundary(0.0,newLoc) :
//				aBoundary.overBoundary(radius, newLoc)) != null)
//			{
//				/*
//				System.out.println("agent at "+newLoc.toString()+
//								" crossed boundary "+aBoundary.getSide());
//				*/
//				return aBoundary;
//			}
//		}
//		
//		// If you are here, it means that no boundary is being crossed.
//		return null;
//	}
	
	/**
     * \brief Test if a given location is outside a boundary other than the
     * bottom. Used in self-attach scenarios.
     * 
     * For self-attachment, the simulation detects a swimming agent may have
     * crossed the substratum boundary and will then assume that agent
     * attaches. However we need to check that move has not crossed any of the
     * other boundaries, else that move is invalid. To do this, all boundaries
     * are checked. If using the method above, y0z could still be returned and
     * thus we end up in a loop. Thus this has had to be adapted so this cannot
     * be returned.
     * 
	 * @param newLoc The new location of this swimming agent.
     * @return The boundary which this move crosses, if applicable. Null if no
     * such boundary.
     */
//	public AllBC testCrossedBoundarySelfAttach(ContinuousVector newLoc) 
//	{
//		// Test on the domain grid if the new location is inside the domain.
//		if (_domainGrid.isValid(newLoc) && _domainGrid.getValueAt(newLoc) >= 0)
//			return null;
//		
//		// Find the first of the boundaries which has been crossed.
//		// Added a check to not return y0z - we know this has been crossed as
//		// the cell has met the substratum. We are only interested here in
//		// checking the other 7 boundaries.
//		for (AllBC aBoundary : _boundaryList)
//			if ( aBoundary.isOutside(newLoc) )
//				if ( ! aBoundary.getSideName().equals("y0z") )
//					return aBoundary;
//		
//		// If you are here, it means that no boundary is being crossed.
//		return null;
//	}
	
	/**
	 * \brief Add a boundary condition to the list of boundaries on this domain.
	 * 
	 * @param aBC Boundary condition to add to the list of boundaries.
	 */
	public void addBoundary(AllBC aBC)
	{
		_boundaryList.add(aBC);
	}
	
//	@Override
//	public LinkedList<AllBC> getAllBoundaries() 
//	{
//		return _boundaryList;
//	}
	
//	public LinkedList<AllBC> getAllSupportBoundaries()
//	{
//		LinkedList<AllBC> out = new LinkedList<AllBC>();
//		for ( AllBC aBC : _boundaryList )
//			if ( aBC.isSupport() )
//				out.add(aBC);
//		return out;
//	}
//	
//	public LinkedList<ConnectedBoundary> getAllConnectedBoundaries()
//	{
//		LinkedList<ConnectedBoundary> out = 
//										new LinkedList<ConnectedBoundary>();
//		for ( AllBC aBC : _boundaryList )
//			if ( aBC instanceof ConnectedBoundary )
//				out.add((ConnectedBoundary) aBC);
//		return out;
//	}


//	public Bulk getChemostat()
//	{
//		Bulk aBulk;
//		for (ConnectedBoundary aBC : getAllConnectedBoundaries())
//		{
//			aBulk = aBC.getBulk();
//			if( aBulk != null && aBulk.nameEquals("chemostat") )
//				return aBulk;
//		}
//		return null;
//	}
	
	/**
	 * \brief Refresh relative diffusivity and boundary layer grids to ensure
	 * biomass updated this step is included.
	 * 
	 * Used in the creation of output files.
	 */
	public void refreshBioFilmGrids() 
	{
		/* TODO idyno 2 biofilm */
		// Build a grid with the concentration of agents skip the the
		// refreshment of the position of the agents relative to the
		// boundary layers.
		_biomassGrid.setAllValueAt(0.0);

			// Reset the grid
			_boundaryLayer.setAllValueAt(0.0);

			//


			// calculate the values in each of the grids
			calculateComputationDomainGrids2();

			/* TODO this is where the boundary padding should be updated */
			_boundaryLayer.refreshBoundary();

			_diffusivityGrid.refreshBoundary();
			_biomassGrid.refreshBoundary();

	}
	
	/**
	 * \brief Calculates the diffusivity and boundary layer grid levels.
	 *
	 * FIXME check this with debugger
	 * 
	 * In previous versions of iDynoMiCS this method could be found within
	 * refreshBioFilmGrids. This has been moved here as, with the addition of
	 * self attachment, this method needs to be called before agent
	 * initialisation. KA May 2013.
	 */
	public void calculateComputationDomainGrids()
	{
		for (int i = 1; i <= _nI; i++) 
			for (int j = 1; j <= _nJ; j++) 
				for (int k = 1; k <= _nK; k++)
					if ( _biomassGrid.grid[i][j][k] > 0.0 )
					{
						/*
						 * This is biomass.
						 */
						_boundaryLayer.grid[i][j][k] = 1.0;
						_diffusivityGrid.grid[i][j][k] = _biofilmDiffusivity;
					}
					else
					{
						/*
						 * This is liquid, check dilation sphere for biomass:
						 * checkDilationRadius will set the value to 1 if it is
						 * within the boundary layer.
						 */
						_boundaryLayer.grid[i][j][k] = checkDilationRadius(i, j, k);

						//LogFile.writeLog("_boundaryLayer["+i+"]["+j+"]["+k+"] = "+_boundaryLayer.grid[i][j][k]);
						if ( _domainGrid.grid[i][j][k] == -1.0 )
							_diffusivityGrid.grid[i][j][k] = Double.MIN_VALUE;
						else
							_diffusivityGrid.grid[i][j][k] = 1.0;
					}
	}

	/**
	 * work in progress method, updated to use updateWellMixed
	 */
	public void calculateComputationDomainGrids2()
	{
		this._environment.updateWellMixed();
		double[][][] temp = this._environment.getCommonGrid().getArray( ArrayType.WELLMIXED );
		double[][][] wellMixed = MultigridUtils.translateIn(temp);
		for (int i = 1; i <= _nI; i++)
			for (int j = 1; j <= _nJ; j++)
				for (int k = 1; k <= _nK; k++)
					// Note padding will be updated when .refreshBoundaryLayer() method is called.
					if ( (i != _nI) || !(j != _nJ) || !((_nK > 1) && k != _nK) || !( wellMixed[i][j][k] > 0.0 ) ) {
						/*
						 * This is biomass.
						 */
						_boundaryLayer.grid[i][j][k] = 1.0;
						_diffusivityGrid.grid[i][j][k] = _biofilmDiffusivity;
					} else {
						/*
						 * This is liquid, check dilation sphere for biomass:
						 * checkDilationRadius will set the value to 1 if it is
						 * within the boundary layer.
						 */
//						_boundaryLayer.grid[i][j][k] = checkDilationRadius(i, j, k);
						//LogFile.writeLog("_boundaryLayer["+i+"]["+j+"]["+k+"] = "+_boundaryLayer.grid[i][j][k]);
						if (_domainGrid.grid[i][j][k] == -1.0)
							_diffusivityGrid.grid[i][j][k] = Double.MIN_VALUE;
						else
							_diffusivityGrid.grid[i][j][k] = 1.0;

					}
	}

	
	/**
	 * \brief Returns a list of discrete vectors that specify the limit of the
	 * boundary layer.
	 * 
	 * @return LinkedList of DiscreteVectors with the limit of the boundary layer.
	 */
//	@Override
//	public LinkedList<DiscreteVector> getBorder()
//	{
//		Double v;
//		LinkedList<DiscreteVector> border = new LinkedList<DiscreteVector>();
//		for (_i = 1; _i<_nI+1; _i++)
//			for (_j = 1; _j<_nJ+1; _j++)
//				for (_k = 1; _k<_nK+1; _k++)
//				{
//					v = _boundaryLayer.grid[_i][_j][_k];
//					if ( v.equals(1.0) && bdryHasFreeNbh() )
//					{
//						// add the location if it has biomass or is in the boundary layer (v==1) and
//						// if the neighboring points are free (not biomass or bdry layer)
//						border.addLast(new DiscreteVector(_i, _j, _k));
//					}
//				}
//		return border;
//	}
	
	/**
	 * \brief Creates a list of doubles with the heights of the biofilm/liquid
	 * interface.
	 * 
	 * Used for writing simulation statistics. This routine is very basic in
	 * that it just captures the overall height (x-position) of each point from
	 * the nearest carrier, but for most cases it should be sufficient.
	 * 
	 * @return	Array of double values of the heights of the biofilm/liquid
	 * interface.
	 */
//	public Double[] getInterface()
//	{
//		currentSim.agentGrid.getLevelSet().refreshBorder(false, currentSim);
//		LinkedList<LocatedGroup> border =
//							currentSim.agentGrid.getLevelSet().getBorder();
//		/* 
//		 * Catch if there is no biomass for some reason; in that case return
//		 * zero height.
//		 */
//		if ( border.isEmpty() )
//			return ExtraMath.newDoubleArray(1);
//		// Now copy to regular array, but watch for infinite distances.
//		ListIterator<LocatedGroup> iter = border.listIterator();
//		Double [] out = new Double[border.size()];
//		while ( iter.hasNext() )
//		{
//			out[iter.nextIndex()] = iter.next().distanceFromCarrier;
//			if ( out[iter.previousIndex()] == Double.MAX_VALUE )
//				out[iter.previousIndex()] = 0.0;
//		}
//		return out;
//	}
	
	/**
	 * \brief Determines whether points in the boundary layer have free
	 * neighbours.
	 * 
	 * @author BVM 161208
	 * @return	Boolean noting whether the elements in the boundary layer have
	 * free neighbours.
	 */
	private Boolean bdryHasFreeNbh()
	{
		if ( is3D() )
		{
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++)
					for (int k = -1; k < 2; k++)
						if (_boundaryLayer.grid[_i+i][_j+j][_k+k] == 0.0 )
							return true;
			return false;
		}
		else
		{
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++)
					if (_boundaryLayer.grid[_i+i][_j+j][1] == 0.0 )
						return true;
			return false;
		}
	}
	
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * \brief Sets the value of a grid space in the boundary layer, indicating
	 * whether the space is part of the boundary layer, or biomass is present.
	 *  
	 * @param n	The N coordinate of the grid to check whether this square is
	 * in the boundary.
	 * @param m	The M coordinate of the grid to check whether this square is
	 * in the boundary.
	 * @param l	The L coordinate of the grid to check whether this square is
	 * in the boundary.
	 * @return	Integer noting whether or not the square is in the boundary
	 * (1 if yes, 0 if not).
	 */
	protected Double checkDilationRadius(int n, int m, int l)
	{
		/*
		 * For no boundary layer, liquid means it's outside the boundary
		 * (and this routine only checks the status of non-biomass elements).
		 */
		if ( _dilationBand == 0.0 )
			return 0.0;
		
		int nInterval, mInterval, lInterval;
		int jIndex, kIndex;
		Double deltaN, deltaM;
		Double dilationRadiusM, dilationRadiusL;

//		nInterval = (int) Math.floor(_dilationBand/_resolution);
		nInterval = 1;
		
		for (int i = -nInterval; i <= nInterval; i++)
		{
			// only proceed if neighbour is within computational
			// volume top and bottom boundaries
			if ( (n+i >= 0) && (n+i < _nI) )
			{
				deltaN = i*_resolution;
				// This calculates the range in the j direction based on a right triangle
				// with hypotenuse equal to the sphere's radius, so that the total area
				// checked is a sphere
				dilationRadiusM = ExtraMath.triangleSide(_dilationBand, deltaN);
				mInterval = (int) Math.floor(dilationRadiusM/_resolution);
				
				for (int j = -mInterval; j <= mInterval; j++) {
				if ( _nK == 1)
				{
						// 2D case
						jIndex = cyclicIndex(m+j,_nJ+2);
						if (_biomassGrid.grid[n+i][jIndex][1] > 0.0) 
							return 1.0;
						if (_domainGrid.grid[n+i][jIndex][1] == 0.0)
							return 1.0;
				}
				else
				{
						// 3D case
						deltaM = j*_resolution;
						// This calculates the range in the k direction based on
						// a right triangle with hypotenuse equal to the sphere's
						// radius, so that the total area checked is a sphere
						dilationRadiusL = ExtraMath.triangleSide(_dilationBand, deltaN, deltaM);
						lInterval = (int) Math.floor(dilationRadiusL/_resolution);

						for (int k = -lInterval; k <= lInterval; k++)
							if ( (i != 0) || (j != 0) || (k != 0) )
					{
								jIndex = cyclicIndex(m+j, _nJ+2);
								kIndex = cyclicIndex(l+k, _nK+2);
								if (_biomassGrid.grid[n+i][jIndex][kIndex] > 0.0)
										return 1.0;
								if (_domainGrid.grid[n+i][jIndex][kIndex] == 0.0)
									return 1.0;
					}
				}
			}
		}
		}
		return 0.0;
	}
	
	/**
	 * \brief For cyclic boundaries, returns the index of the grid space on the
	 * opposite side of the boundary.
	 * 
	 * @param val	The integer grid spqce to check.
	 * @param limit	The limit of the grid.
	 * @return	The integer of the grid square the opposite side of the
	 * boundary.
	 */
	protected final int cyclicIndex(int val, int limit)
	{
		return (val<0 ? limit+val : (val>=limit ? val-limit : val));
	}
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	protected Boolean checkDilationCoord(int[] coord)
	{
		// TODO update biomassGrid
//		for (AllBC aBC : _boundaryList )
//			aBC.applyBoundary(coord);
		/*
		 * Return true if this is biomass or substratum.
		 */
		return ( _biomassGrid.getValueAt(coord) > 0.0 ) ||
				( _domainGrid.getValueAt(coord) == 0.0 );
	}
	
	/**
	 * \brief Return longest side of this domain.
	 * 
	 * @return	Double of the length of the longest side of this domain.
	 */
	public Double getLongestSize()
	{
		return Math.max(Math.max(length_X, length_Y), length_Z);
	}
	
	/**
	 * \brief Return the resolution of this domain.
	 * 
	 * @return	Double value stating the resolution of this domain.
	 */
	public double getResolution()
	{
		return _resolution;
	}
	
	/**
     * \brief Returns the domain grid.
     * 
     * @return SpatialGrid within this domain.
     */
	public SolverGrid getGrid()
	{
		return _domainGrid;
	}
	
	/**
	 * \brief Determine if the simulation is recreating a 3D environment.
	 * 
	 * @return	A boolean value stating whether or not the environment is 3D.
	 */
	public Boolean is3D()
	{
		return _domainGrid.is3D();
	}
	
	/**
	 * \brief Return the diffusivity grid associated with this domain.
	 * 
	 * @return	SoluteGrid containing diffusivity grid statistics.
	 */
	public SoluteGrid getDiffusivity()
	{
		return _diffusivityGrid;
	}
	
	/**
	 * \brief Return the boundary layer grid associated with this domain.
	 * 
	 * @return	SoluteGrid containing boundary between bulk and biofilm.
	 */
	public SoluteGrid getBoundaryLayer() 
	{
		return _boundaryLayer;
	}
	
	/**
	 * \brief Return the biomass grid associated with this domain.
	 * 
	 * @return	SoluteGrid containing biomass throughout this domain.
	 */
	public SoluteGrid getBiomass()
	{
		return _biomassGrid;
	}
	
	/**
	 * \brief Used in testing to view the boundary layer matrix for a set part
	 * of the domain.
	 * 
	 * @author KA 210513
	 */
	public void printBoundaryLayer()
	{
		// Printing the Boundary Layer Grid
		for(int k = 1; k <= _boundaryLayer.getGridSizeK(); k++)
			for(int i = 1; i <= _boundaryLayer.getGridSizeI(); i++)
			{
				for(int j = 1; j <= _boundaryLayer.getGridSizeJ(); j++)
				{
					System.out.print(_boundaryLayer.getValueAt(i, j, k)+" ");
				}
				System.out.println();
			}
	}
	
	/**
	 * \brief Used in testing to view the biomass matrix for a set part of the
	 * domain.
	 * 
	 * @author KA 210513
	 */
	public void printBiomassGrid()
	{
		// Printing the Boundary Layer Grid
		for(int k = 1; k <= _biomassGrid.getGridSizeK(); k++)
			for(int i = 1; i <= _biomassGrid.getGridSizeI(); i++)
			{
				for(int j = 1; j <= _biomassGrid.getGridSizeJ(); j++)
				{
					System.out.print(_biomassGrid.getValueAt(i, j, k)+" ");
				}
				System.out.println();
			}
	}
}
