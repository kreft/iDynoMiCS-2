/**
 * \package simulator.geometry.boundaryConditions
 * \brief Package of boundary conditions that can be used to capture agent
 * behaviour at the boundary of the computation domain.
 * 
 * This package is part of iDynoMiCS v1.2, governed by the CeCILL license
 * under French law and abides by the rules of distribution of free software.  
 * You can use, modify and/ or redistribute iDynoMiCS under the terms of the
 * CeCILL license as circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info".
 */
package solver.mgFas.utils;

import java.util.LinkedList;

import shape.Shape;
import solver.mgFas.SoluteGrid;
import solver.mgFas.SolverGrid;

/**
 * \brief Group all methods expected by the interface but common to most of
 * the boundary classes.
 * 
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 */
public abstract class AllBC
{
	/* _____________________________ FIELDS _______________________________ */
	/**
	 * The name of the boundary describing its side (xOy,...).
	 */
	protected String	_mySide;
	
	/**
	 * The shape of the boundary.
	 */
	protected Shape _myShape;
	
	/**
	 * Boolean noting whether this boundary is the supporting structure
	 * (substratum).
	 */
	protected boolean	_isSupport	= false;
	
	/**
	 * Boolean noting whether this boundary can contain active solute.
	 */
	protected boolean	activeForSolute	= true;
	
	/* _________________ INTERNAL TEMPRARY VARIABLES ______________________ */
	/**
	 * Discrete coordinates of a voxel inside the computation domain but along
	 * the boundary.
	 */ 
	protected static DiscreteVector dcIn	= new DiscreteVector();
	
	/**
	 * Discrete coordinates of the voxel in front of the one outside the boundary
	 */
	protected static DiscreteVector dcOut	= new DiscreteVector();

	/* ________________________ CONSTRUCTION METHODS ________________________ */

	/**
	 * \brief Generic constructor called to dynamically instantiate a child
	 * class object.
	 *
	 * @param root	Set of XML tags relating to one boundary condition
	 * @param aSim	The current simulation object used to simulate the
	 * conditions specified in this protocol file.
	 * @param aDomain	The computation domain to which this boundary
	 * condition is assigned.
	 */
//	public static AllBC staticBuilder(XMLParser root, Simulator aSim,Domain aDomain)
//	{
//		// Create the object
//		AllBC out = (AllBC) root.instanceCreator("simulator.geometry.boundaryConditions");
//
//		// Initialise & declare the boundary
//		out.init(aSim, aDomain, root);
//
//		return out;
//	}

	/**
	 * \brief Initialises the boundary condition.
	 *
	 * This method should be overridden by each boundary condition class file.
	 *
	 * @param aSim	The current simulation object used to simulate the
	 * conditions specified in this protocol file.
	 * @param aDomain	The computation domain to which this boundary
	 * condition is assigned.
	 * @param root	Set of XML tags relating to one boundary condition.
	 */
//	public abstract void init(Simulator aSim, Domain aDomain, XMLParser root);

	/**
     * \brief Used during the initialisation, load the class describing the
     * shape of the boundary defined in the parent class.
     *
     * @param geometryRoot	Usually an XML set of elements that describe the
     * boundary to be created.
     * @param aDomain	The computational domain which this boundary is
     * associated with.
     */
//	public void readGeometry(XMLParser geometryRoot, Domain aDomain)
//	{
//		// Set the name of the boundary.
//		_mySide = geometryRoot.getName();
//		// Set the class to use to define the shape.
//		String className = "simulator.geometry.shape.";
//		className += geometryRoot.getChildParser("shape").getAttribute("class");
//		// Build the instance used to describe the shape.
//		try
//		{
//			_myShape = (IsShape) Class.forName(className).newInstance();
//			_myShape.readShape(new
//					XMLParser(geometryRoot.getChildElement("shape")), aDomain);
//		}
//		catch (Exception e)
//		{
//
//		}
//	}
	
	/**
	 * \brief Determines if a point is outside the boundary.
	 * 
	 * @param position	ContinuousVector to check.
	 * @return	Boolean value noting whether this coordinate is outside the
	 * boundary (true) or not (false).
	 */
	public Boolean isOutside(ContinuousVector position)
	{
		return !_myShape.isInside(new double[] { position.x,
				position.y,
				position.z } );
	}

	/**
	 * \brief Determine whether this boundary is the supporting structure
	 * (substratum).
	 *
	 * @return Boolean noting whether this boundary is the supporting
	 * structure (true) or not (false).
	 */
	public boolean isSupport()
	{
		return _isSupport;
	}
	
	/**
	 * \brief Return the name of the side of the domain which this boundary is
	 * on.
	 * 
	 * TODO this is a duplicate of getSide()!
	 * 
	 * @return	String containing the name of the side of the domain
	 * (e.g. x0z, xNz, etc).
	 */
	public String getSideName()
	{
		return this._mySide;
	}
	
	/**
	 * \brief Solver for the variable concentration boundary condition.
	 *  
	 * Initialises the course along the shape of the boundary during multigrid
	 * computation.
	 * 
	 * @param aSoluteGrid	Grid of solute information which is to be
	 * refreshed by the solver.
	 * @see ComputationDomain.refreshBoundaries()
	 */
	public abstract void refreshBoundary(SoluteGrid aSoluteGrid);
	
	/**
     * \brief Method used if a boundary modifies the local diffusivity
     * constant. Most of boundaries do not modify it.
     * 
     * @param relDiff	Relative difference grid
     * @param aSolutegrid	Grid of solute information which is to be
     * refreshed by the solver.
     * 
     * @see BoundaryGasMembrane
     */
	public void refreshDiffBoundary(SoluteGrid relDiff, SoluteGrid aSolutegrid)
	{
		
	};

	/* ______________INTERACTION WITH THE PARTICLES _____________________ */

	/**
	 * \brief Method used by another which gets the indexed grid position of a
	 * continuous vector.
	 *
	 * Some boundary conditions (e.g. BoundaryCyclic) need the input corrected
	 * due to the condition, some don't and just return the input. Maybe we'll
	 * change this at some point as to just return the input looks a bit daft
	 * - but we'll leave it here for the moment.
	 *
	 * @param position ContinuousVector that gives the current location of an
	 * agent to check on the grid.
	 */
	public ContinuousVector lookAt(ContinuousVector position)
	{
		return position;
	}



	/* ___________________ INTERACTION WITH THE DOMAIN _________________________ */


	/**
	 * \brief Determine if this boundary is active for solute.
	 * 
	 * @return	Boolean noting whether this boundary is active for solute
	 * (true) or not (false).
	 */
	public boolean isActive()
	{
		return activeForSolute;
	}
	
	/**
	 * \brief Determines if a discrete vector location is outside the boundary.
	 * 
	 * @param dc	DiscreteVector to check.
	 * @param aSpatialGrid	The grid to check whether a point is outside.
	 * @return	Boolean value noting whether this coordinate is outside the
	 * boundary (true) or not (false).
	 */
	public boolean isOutside(DiscreteVector dc, SolverGrid aSpatialGrid)
	{
		ContinuousVector temp = new ContinuousVector();
		temp.setToVoxelCenter(dc, aSpatialGrid.getResolution());
		return isOutside(temp);
	}

	/* ____________________ TOOLBOX ______________________________ */

	/**
     * \brief Calculate the orthogonal projection of a location on the
     * boundary.
     * 
     * @param position	A continuous vector stating the point to be used in
     * the calculation.
     * @return ContinuousVector stating the point on the boundary after the
     * orthogonal projection. 
     */
	public ContinuousVector getOrthoProj(ContinuousVector position)
	{
		return getOrthoProj(position);
	}
	
	/**
	 * \brief Return the name of the side of the domain which this boundary is
	 * on.
	 * 
	 * @return	String containing the name of the side of the domain (e.g.
	 * x0z, xNz, etc).
	 */
	public String getSide()
	{
		return _mySide;
	}

	/**
	 * \brief Returns the distance from a point to the boundary.
	 *
	 * @param position	The continuous vector of points to calculate how far
	 * the point is from the boundary.
	 * @return	Double value stating the distance from the point to the
	 * boundary.
	 */
	public Double getDistance(ContinuousVector position)
	{
		return getDistanceShape(position);
	}

	/**
	 * NOTE Comming from isShape, correct behavior?
	 *
	 * \brief Gets the distance from a point on the other side
	 * (ContinuousVector).
	 *
	 * Used in cyclic boundaries.
	 *
	 * @return Double stating distance to that shape.
	 */
	public Double getDistanceShape(ContinuousVector point)
	{
		ContinuousVector diff = getOrthoProj(point);
		diff.subtract(point);
		return diff.norm();
	}

}
