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

import java.io.Serializable;

import shape.Dimension;
import solver.mgFas.boundaries.CartesianPadding;
import solver.mgFas.utils.ContinuousVector;
import solver.mgFas.utils.DiscreteVector;
import utility.ExtraMath;
import linearAlgebra.Array;
import linearAlgebra.Vector;

/**
 * \brief Class defining a spatial grid, i.e. a matrix of double.
 * 
 * The grid is padded, 3D grid.
 * 
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre
 * for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology,
 * University of Birmingham (UK)
 */
public class SolverGrid implements Serializable
{
	/**
	 * Serial version used for the serialisation of the class
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name assigned to this spatial grid. Taken from an XML tag in the
	 * protocol file.
	 */
	public String gridName;
	
	/**
	 * The unit for all values stored on this grid.
	 */
	public String gridUnit = "g.L-1";

	/**
	 * The solute grid - a three dimensional array of Double values.
	 */
	public double[][][]       grid;

	/**
	 * Number of grid voxels in I direction
	 */
	protected int             _nI;
	
	/**
	 * Number of grid voxels in J direction
	 */
	protected int 			  _nJ;
	
	/**
	 * Number of grid voxels in K direction
	 */
	protected int			  _nK;

	/**
	 * Grid resolution = side length of a voxel
	 */
	protected double          _reso;

	/**
	 * Boolean noting whether this grid is 3D (true) or 2D (false)
	 */
	protected Boolean         _is3D;
	
	
	
	/**
	 * \brief Blank constructor.
	 */
	public SolverGrid()
	{
		
	}

	/**
	 * \brief Default constructor for an empty spatial grid
	 * 
	 * Sets the grid resolution and dimensions as provided in the simulation
	 * protocol file.
	 * 
	 * @param nI	The number of grid locations in the I direction.
	 * @param nJ	The number of grid locations in the J direction.
	 * @param nK	The number of grid locations in the K direction.
	 * @param resolution The grid resolution.
	 */
	public SolverGrid(int nI, int nJ, int nK, double resolution)
	{
		_nI = nI;
		_nJ = nJ;
		_nK = nK;
		_reso = resolution;
		// Create a padded grid.
		initGrids();
	}
	
	/**
	 * \brief Default constructor for an empty 2D spatial grid
	 * 
	 * Sets the grid resolution and dimensions as provided in the simulation
	 * protocol file.
	 * 
	 * @param nI	The number of grid locations in the I direction
	 * @param nJ	The number of grid locations in the J direction
	 * @param resolution the grid resolution
	 */
	public SolverGrid(int nI, int nJ, double resolution)
	{

		_nI = nI;
		_nJ = nJ;
		_nK = 1;
		_reso = resolution;
		// Create a padded grid.
		initGrids();
	}
	
	/**
	 * \brief Creates the solute grid at the required size
	 * 
	 * If this is a chemostat, this will simple be a 1x1x1; if not there are
	 * further checks to determine whether we are simulating 3D or not.
	 */
	protected void initGrids() 
	{
		_is3D = ! ( _nK == 1 );
		grid = Array.array(_nI+2, _nJ+2, _nK+2, 0.0);
	}

	/**
	 * \brief Determine if a given discrete position is valid or outside the
	 * grid (Padding excluded).
	 * 
	 * TODO Rob 13Mar2015: Surely this should be > 0 to exclude padding?
	 * 
	 * @param dC	DiscreteVector to validate
	 * @return Boolean stating whether this location is valid (true) or
	 * outside the grid.
	 */
	public Boolean isValid(int[] dC)
	{
		return (dC[0] >= 0) && (dC[0] < _nI) && 
				(dC[1] >= 0) && (dC[1] < _nJ) &&
				(dC[2] >= 0) && (dC[2] < _nK);
	}
	
	/**
	 * \brief Determine if a given voxel coordinate is valid or outside the
	 * grid.
	 * 
	 * @param i	I Coordinate of the grid location.
	 * @param j	J Coordinate of the grid location.
	 * @param k K Coordinate of the grid location.
	 * @return Boolean stating whether this location is valid (true) or
	 * outside the grid.
	 */
	public Boolean isValidOrPadded(int i, int j, int k)
	{
		return (i >= 0) && (i <= _nI) && 
				(j >= 0) && (j <= _nJ) &&
				(k >= 0) && (k <= _nK);
	}

	public Boolean isPadding(int i, int j, int k)
	{
		return (i > 0) && (i < _nI) &&
				(j > 0) && (j < _nJ) &&
				(k > 0) && (k < _nK);
	}

//	public Boolean shouldSolve(int i, int j, int k, double threshold)
//	{
//		if ( isPadding(i, j, k) )
//			return false;
//		if(	grid[i][j][k] > threshold)
//			return true;
//		if(	grid[i-1][j][k] > threshold &! isPadding(i-1, j, k) )
//			return true;
//		if( grid[i+1][j][k] > threshold &! isPadding(i+1, j, k) )
//			return true;
//		if( grid[i][j-1][k] > threshold &! isPadding(i, j-1, k) )
//			return true;
//		if( grid[i][j+1][k] > threshold &! isPadding(i, j+1, k) )
//			return true;
//		if( grid[i][j][k-1] > threshold &! isPadding(i, j, k-1) )
//			return true;
//		if( grid[i][j][k+1] > threshold &! isPadding(i, j, k+1) )
//			return true;
//		return false;
//	}

	/**
	 * \brief Determine if a given continuous location is valid or outside the
	 * grid.
	 * 
	 * @param position	ContinuousVector to validate.
	 * @return Boolean stating whether this location is valid (true) or
	 * outside the grid (false).
	 */
	public Boolean isValid(double[] position)
	{
		return isValid(getDiscreteCoordinates(position));
	}

	/**
	 * \brief Transform a location, expressed as a continuous vector into a
	 * discrete position on the basis of the resolution of the grid.
	 * 
	 * TODO Check why this is different to
	 * DiscreteVector(ContinuousVector cV, Double res)
	 * which uses Math.ceil() instead of Math.floor()
	 * 
	 * @param cC	ContinuousVector to be transformed
	 * @return	DiscreteVector created from this continuous location
	 */
	public int[] getDiscreteCoordinates(double[] cC)
	{
		int i = (int) Math.floor(cC[0]/_reso);
		int j = (int) Math.floor(cC[1]/_reso);
		int k = (int) Math.floor(cC[2]/_reso);
		return new int[] {i, j, k};
	}

	/**
	 * \brief Transform a position, expressed as a discrete vector into a
	 * continuous location on the basis of the resolution of the grid.
	 * 
	 * @param coord	DiscreteVector to be transformed.
	 * @return	ContinuousVector created from this discrete position.
	 */
	public double[] getContinuousCoordinates(int[] coord)
	{
		double[] temp = new double[] { 
				Double.valueOf(coord[0]),
				 Double.valueOf(coord[1]),
				 Double.valueOf(coord[2]) };
		Vector.addEquals(temp, 0.5);
		Vector.timesEquals(temp, _reso);
		return temp;
	}

	/**
	 * \brief Transform a position, expressed as a discrete vector into a
	 * continuous location on the basis of the resolution of the grid.
	 *
	 * @param coord	DiscreteVector to be transformed.
	 * @return	ContinuousVector created from this discrete position.
	 */
	public ContinuousVector getContinuousCoordinates(DiscreteVector coord)
	{
		ContinuousVector out = new ContinuousVector();
		out.setToVoxelCenter(coord, _reso);
		return out;
	}


	/**
	 * \brief Return the maximum value on this grid (padding included).
	 * 
	 * @return Maximum value of the grid.
	 */
	public Double getMax()
	{
		return Array.max(grid);
	}
	
	public Double getMaxUnpadded()
	{
		Double out = Double.NEGATIVE_INFINITY;
		for ( int i = 0; i < _nI; i++ )
			for ( int j = 0; j < _nJ; j++ )
				for ( int k = 0; k < _nK; k++ )
				{
					out = Math.max(out, grid[i][j][k]);
				}
					
		return out;
	}
	
	/**
	 * \brief Return the average value on this grid (padding excluded).
	 * 
	 * @return Average value of the grid.
	 */
	public Double getAverage()
	{
		return Array.sum(grid)/(_nI)/(_nJ)/(_nK);
	}

	/**
	 * \brief Return the sum of this grid (padding included).
	 * 
	 * @return	The sum of the values in this spatial grid.
	 */
	public Double getSum()
	{
		return Array.sum(grid);
	}

	/**
	 * \brief Return the minimum value on this grid (padding included)
	 * 
	 * @return Minimum value of the grid
	 */
	public Double getMin()
	{
		return Array.min(grid);
	}
	
	/**
	 * TODO Check and make sure this is used.
	 * 
	 * @return
	 */
	public Double getMinUnpadded()
	{
		Double out = Double.POSITIVE_INFINITY;
		for ( int i = 0; i < _nI; i++ )
			for ( int j = 0; j < _nJ; j++ )
				for ( int k = 0; k < _nK; k++ )
					out = Math.min(out, grid[i][j][k]);
		return out;
	}
	
	/**
	 * \brief For a given location, calculate the 2nd spatial derivative
	 * according to X.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 2nd spatial derivative according X
	 */
	public Double diff2X(int i, int j, int k)
	{
		Double value = grid[i+1][j][k] + grid[i-1][j][k] - 2*grid[i][j][k];
		value /= ExtraMath.sq(_reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 2nd spatial derivative according to X.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 2nd spatial derivative according X.
	 */
	public Double diff2X(int[] dV)
	{
		return diff2X(dV[0], dV[1], dV[2]);
	}

	/**
	 * \brief For a given location, calculate the 1st spatial derivative
	 * according to X.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 1st spatial derivative according X
	 */
	public Double diffX(int i, int j, int k)
	{
		Double value = (grid[i+1][j][k] - grid[i-1][j][k])/(2 * _reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 1st spatial derivative according to X.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 1st spatial derivative according X.
	 */
	public Double diffX(int[] dV)
	{
		return diffX(dV[0], dV[1], dV[2]);
	}
	
	/**
	 * \brief For a given location, calculate the 2nd spatial derivative
	 * according to Y.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 2nd spatial derivative according Y
	 */
	public Double diff2Y(int i, int j, int k)
	{
		Double value = grid[i][j+1][k] + grid[i][j-1][k] - 2*grid[i][j][k];
		value /= ExtraMath.sq(_reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 2nd spatial derivative according to Y.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 2nd spatial derivative according Y.
	 */
	public Double diff2Y(int[] dV)
	{
		return diff2Y(dV[0], dV[1], dV[2]);
	}

	/**
	 * \brief For a given location, calculate the 1st spatial derivative
	 * according to Y.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 1st spatial derivative according Y
	 */
	public Double diffY(int i, int j, int k)
	{
		Double value = (grid[i][j+1][k] - grid[i][j-1][k])/(2 * _reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 1st spatial derivative according to Y.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 1st spatial derivative according Y.
	 */
	public Double diffY(int[] dV)
	{
		return diffY(dV[0], dV[1], dV[2]);
	}
	
	/**
	 * \brief For a given location, calculate the 2nd spatial derivative
	 * according to Z.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 2nd spatial derivative according Z
	 */
	public Double diff2Z(int i, int j, int k)
	{
		Double value = grid[i][j][k+1] + grid[i][j][k-1] - 2*grid[i][j][k];
		value /= ExtraMath.sq(_reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 2nd spatial derivative according to Z.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 2nd spatial derivative according Z.
	 */
	public Double diff2Z(int[] dV)
	{
		return diff2Z(dV[0], dV[1], dV[2]);
	}

	/**
	 * \brief For a given location, calculate the 1st spatial derivative
	 * according to Z.
	 * 
	 * @param i	I position on the spatial grid
	 * @param j	J position on the spatial grid
	 * @param k	K position on the spatial grid
	 * @return 1st spatial derivative according Z
	 */
	public Double diffZ(int i, int j, int k)
	{
		Double value = (grid[i][j][k+1] - grid[i][j][k-1])/(2 * _reso);
		return Double.isFinite(value) ? value : 0.0;
	}

	/**
	 * \brief For a given location, expressed as a discrete vector, calculate
	 * the 1st spatial derivative according to Z.
	 * 
	 * @param dV	DiscreteVector containing the position of a grid location.
	 * @return 1st spatial derivative according Z.
	 */
	public Double diffZ(int[] dV)
	{
		return diffZ(dV[0], dV[1], dV[2]);
	}
	
	/**
	 * \brief Computes the average concentration seen in a sphere (or cube)
	 * centred around a given point.
	 * 
	 * @param cC	ContinuousVector containing the point to use as the centre
	 * of this search.
	 * @param extReso	Resolution to use in this search.
	 * @return	Average grid value seen around this point.
	 */
	public Double getValueAround(double[] cC, Double extReso)
	{
		return getValueAt(cC);
	}

	/**
	 * \brief Returns a vector of the first spatial derivatives in x, y & z.
	 * 
	 * Returns a vector of the first spatial derivatives in x, y & z
	 * (nabla cC - see http://en.wikipedia.org/wiki/Del). Does this by 
	 * first converting the ContinuousVector to a DiscreteVector and then
	 * estimating then gradient using the Mean Value Theorem 
	 * (http://en.wikipedia.org/wiki/Mean_value_theorem).
	 * 
	 * @param cC	ContinuousVector position used to calculate the gradient.
	 * @return	Vector of spatial derivatives in X,Y,Z.
	 */
	public double[] getGradient(double[] cC)
	{
		int[] dV = new int[] { (int) Math.ceil(cC[0]/_reso),
				 (int) Math.ceil(cC[0]/_reso),
				 (int) Math.ceil(cC[0]/_reso)};
		return new double[] { diffX(dV), diffY(dV), diffZ(dV)};
	}

	/**
	 * \brief Returns a vector of the first spatial derivatives in x and y,
	 * for 2D simulations. 
	 * 
	 * Returns a vector of the first spatial derivatives in x and y
	 * (nabla cC - see http://en.wikipedia.org/wiki/Del). Does this by 
	 * first converting the ContinuousVector to a DiscreteVector and then
	 * estimating then gradient using the Mean Value Theorem 
	 * (http://en.wikipedia.org/wiki/Mean_value_theorem).
	 * 
	 * @param cC	ContinuousVector position used to calculate the gradient.
	 * @return	Vector of spatial derivatives in X and Y.
	 */
	public double[] getGradient2D(double[] cC) 
	{
		int[] dV = new int[] { (int) Math.ceil(cC[0]/_reso),
				 (int) Math.ceil(cC[0]/_reso),
				 (int) Math.ceil(cC[0]/_reso)};
		return new double[] { diffX(dV), diffY(dV), diffY(dV)};
	}
	

	/**
	 * \brief Return the value on the padded grid at a given position
	 * (the coordinates are NOT corrected).
	 * 
	 * @param dc	DiscreteVector containing the location of the grid
	 * whose value should be returned.
	 * @return The double value at that location.
	 */
	public Double getValueAt(int[] dc)
	{
		return grid[dc[0]+1][dc[1]+1][dc[2]+1];
	}

	public Double getValueAt(int[] dc, boolean padded)
	{
		if( padded )
			return getValueAt(dc);
		else
			return grid[dc[0]][dc[1]][dc[2]];
	}
	
	/**
	 * \brief Return the value stored at the location given by the stated
	 * continuous vector.
	 * 
	 * @param cC	ContinuousVector containing the grid location to return.
	 * @return	Double value stored at that grid location.
	 */
	public Double getValueAt(double[] cC)
	{
		return getValueAt(getDiscreteCoordinates(cC));
	}
	
	/**
	 * \brief Return the value on the padded grid at a given position.
	 * 
	 * The coordinates are NOT corrected.
	 * 
	 * @param i	I Coordinate of the grid location to set
	 * @param j	J Coordinate of the grid location to set
	 * @param k K Coordinate of the grid location to set
	 * @return The double value at that location
	 */
	public Double getValueAt(int i, int j, int k) 
	{
		if (isValidOrPadded(i, j, k))
			return grid[i][j][k];
		else
			return Double.NaN;
	}

	/**
	 * \brief Set a grid location, expressed as a ContinuousVector, to a
	 * specified value.
	 * 
	 * The coordinates are corrected.
	 * 
	 * @param value	Value to set the specified location to
	 * @param cC	Continuous vector stating the location of the grid to be
	 * set to the given value.
	 */
	public void setValueAt(Double value, double[] cC)
	{
		setValueAt(value, getDiscreteCoordinates(cC));
	}

	/**
	 * \brief Set a grid location, expressed as a DiscreteVector, to a
	 * specified value.
	 * 
	 * The coordinates are corrected.
	 * 
	 * @param value	Value to set the specified location to
	 * @param dC	Discrete vector stating the location of the grid to be set
	 * to the given value.
	 */
	public void setValueAt(Double value, int[] dC) 
	{
		grid[dC[0]+1][dC[1]+1][dC[2]+1] = value;
	}

	public void setValueAt(Double value, int[] dC, boolean padded)
	{
		if( padded )
			setValueAt(value, dC );
		else
			grid[dC[0]][dC[1]][dC[2]] = value;
	}


	/**
	 * \brief Set a grid location to a specified value.
	 * 
	 * Note the coordinates are NOT corrected.
	 * 
	 * @param value	Value to set the grid location to
	 * @param i	I Coordinate of the grid location to set
	 * @param j	J Coordinate of the grid location to set
	 * @param k K Coordinate of the grid location to set
	 */
	public void setValueAt(Double value, int i, int j, int k) 
	{
		grid[i][j][k] = value;
	}

	/**
	 * \brief Add a value to that contained at the given discrete coordinates
	 * of this grid.
	 * 
	 * Coordinates are corrected for padding.
	 * 
	 * @param value	Value to add to the specified grid location
	 * @param cC	Continuous vector expressing the location of the grid to
	 * be increased.
	 */
	public void addValueAt(Double value, double[] cC) 
	{
		addValueAt(value, getDiscreteCoordinates(cC));
	}

	/**
	 * \brief Add a value to that contained at the given discrete coordinates
	 * of this grid.
	 * 
	 * Coordinates are corrected for padding.
	 * 
	 * @param value	Value to add to the specified grid location
	 * @param dC	Discrete vector expressing the location of the grid to be
	 * increased.
	 */
	public void addValueAt(Double value, int[] dC)
	{
		grid[dC[0]+1][dC[1]+1][dC[2]+1] += value;
	}

	public void addValueAt(Double value, int[] dC, boolean padded)
	{
		if( padded )
			addValueAt(value, dC);
		else
			grid[dC[0]][dC[1]][dC[2]] += value;
	}

	/**
	 * \brief Add a value to all locations on this grid (including the
	 * padding).
	 * 
	 * @param value	Value to be added to the contents of all grid voxels.
	 */
	public void addAllValues(Double value)
	{
		for (int i = 0; i < _nI+2; i++)
			for (int j = 0; j < _nJ+2; j++)
				for (int k = 0; k < _nK+2; k++)
					grid[i][j][k] += value;
	}

	/**
	 * \brief Checks a value at a given location and sets it to zero if the
	 * value is negative.
	 * 
	 * @param i	Voxel coordinate in I direction
	 * @param j	Voxel coordinate in J direction
	 * @param k	Voxel coordinate in K direction
	 */
	public void truncateValueAt(int i, int j, int k)
	{
		grid[i][j][k] = Math.max(grid[i][j][k], 0.0);
	}

	/**
	 * \brief Set all meshes of a grid with the same value, including the
	 * padding if not a chemostat simulation.
	 * 
	 * @param value	Value at which to set all the elements of the grid
	 */
	public void setAllValueAt(Double value) 
	{
		for (int i = 0; i < _nI+2; i++)
			for (int j = 0; j < _nJ+2; j++)
				for (int k = 0; k < _nK+2; k++)
					grid[i][j][k] = value;
	}
	
	/**
	 * \brief Set all meshes of the grid to zero.
	 */
	public void resetToZero()
	{
		setAllValueAt(0.0);
	}
	
	/**
	 * \brief Return the number of voxels in the X direction.
	 * 
	 * Ignores the padding.
	 * 
	 * @return Number of real voxels along X. 
	 */
	public int getGridSizeI() 
	{
		return _nI;
	}
	
	/**
	 * \brief Return the number of voxels in the Y direction.
	 * 
	 * Ignores the padding.
	 * 
	 * @return Number of real voxels along Y.
	 */
	public int getGridSizeJ() 
	{
		return _nJ;
	}

	/**
	 * \brief Return the number of voxels in the Z direction.
	 * 
	 * Ignores the padding.
	 * 
	 * @return Number of real voxels along Z. 
	 */
	public int getGridSizeK() 
	{
		return _nK;
	}

	/**
	 * \brief Return the number of voxels along a given direction.
	 * 
	 * (axeCode):
	 * 1 - X,
	 * 2 - Y,
	 * 3 - Z.
	 * 
	 * Includes padding.
	 * 
	 * @param axeCode Integer noting the direction to query.
	 * @return The number of voxels along a direction including padding bands.
	 */
	public int getGridTotalSize(int axeCode) 
	{
		switch (axeCode)
		{
		case 1:
			return _nI + 2;
		case 2:
			return _nJ + 2;
		case 3:
			return _nK + 2;
		default:
			return 0;
		}
	}

	/**
	 * \brief Returns the length (in distance unit) along a given direction
	 * 
	 * (axeCode):
	 * 1 - X,
	 * 2 - Y,
	 * 3 - Z.
	 * 
	 * Does not include padding.
	 * 
	 * @param axeCode	The direction of which the length is required:
	 * 1-X, 2-Y, 3-Z
	 * @return Double value stating the length (in distance unit) along a
	 * direction ignoring padding bands.
	 */ 
	public Double getGridLength(int axeCode) 
	{
		switch (axeCode)
		{
		case 1:
			return _nI*_reso;
		case 2:
			return _nJ*_reso;
		case 3:
			return _nK*_reso;
		default:
			return 0.0;
		}
	}

	/**
	 * \brief Return the volume of one voxel of the spatial grid.
	 * 
	 * @return	Double value stating the volume of one voxel of the spatial
	 * grid.
	 */
	public Double getVoxelVolume()
	{
		return Math.pow(_reso,3.0);
	}

	/**
	 * \brief Return the whole grid, including the padding.
	 * 
	 * @return The spatial grid.
	 */
	public double[][][] getGrid()
	{
		return grid;
	}

	/**
	 * \brief Return a clone of this spatial grid.
	 * 
	 * @return	A clone of this spatial grid.
	 */
	public double[][][] getCloneGrid()
	{
		return Array.copy(grid);
	}

	/**
	 * \brief Returns the resolution of this spatial grid.
	 * 
	 * @return	Double value stating the resolution (in micrometers) of this
	 * grid.
	 */
	public double getResolution()
	{
		return _reso;
	}

	/**
	 * \brief Determine if this spatial grid is 3D or 2D.
	 * 
	 * @return	Boolean noting whether this grid is 3D (true) or 2D (false).
	 */
	public Boolean is3D()
	{
		return _is3D;
	}

	/**
	 * \brief Set the values of this spatial grid to those contained in the
	 * supplied grid.
	 * 
	 * @param u	Matrix of values which to set the spatial grid to.
	 */
	public void setGrid(double[][][] u)
	{
		grid = Array.copy(u);
	}
	
	

	/**
	 * \brief Write the contents of this grid to the XML results files.
	 * 
	 * This shows the level of solute in each of the grid spaces.
	 * 
	 * @param bufferState	The output buffer writing the env_state file for
	 * this iteration.
	 * @param bufferSummary	The output buffer writing the env_sum file for
	 * this iteration.
	 * @throws Exception	Exception thrown if these buffers cannot be opened
	 * for writing to.
	 */
//	public void writeReport(ResultFile bufferState, ResultFile bufferSummary)
//															throws Exception
//	{
//		/*
//		 * Edit the markup for the solute grid
//		 */
//		StringBuffer value = new StringBuffer();
//		value.append("<solute name=\"").append(gridName);
//		value.append("\" unit=\"").append(gridUnit);
//		value.append("\" resolution=\"").append(_reso);
//		value.append("\" nI=\"").append(_nI);
//		value.append("\" nJ=\"").append(_nJ);
//		value.append("\" nK=\"").append(_nK);
//		value.append("\">\n");
//		/*
//		 * Write the markup in the file
//		 */
//		bufferState.write(value.toString());
//		/*
//		 * Rob 3/3/11: Changed to fix bug in envState output files and improve
//		 * code readability (plus efficiency... possibly). Note that for a
//		 * chemostat, i=j=k=1 and that in 2D k=1. The main fix here however,
//		 * is that grid is a Double[] and not an array, as previously coded
//		 * (this reduces the amount of storage space taken by envState files
//		 * by about a 2 thirds!)
//		 */
//
//		/*
//		 * KA 06062013 - turned off the printing of the padding. Will need
//		 * to ensure this is clear from v1.2
//		 * 
//		 * Fill the mark-up.
//		 */
//		if ( _nK == 1 )
//			for ( int i = 1; i < _nI + 1; i++ )
//				for ( int j = 1; j < _nJ + 1; j++ )
//					bufferState.write(String.valueOf(grid[i][j][1])+";\n");
//		else
//			for ( int i = 1; i < _nI + 1; i++ )
//				for ( int j = 1; j < _nJ + 1; j++ )
//					for ( int k = 1; k < _nK + 1; k++ )
//						bufferState.write(String.valueOf(grid[i][j][k])+";\n");
//		
//		/*
//		 * Close the mark-up
//		 */
//		bufferState.write("\n</solute>\n");
//	}

	public void syncBoundary(Domain domain) {
		/*
		 * Three possibilities: periodic, constant concentration, zero flux
		 */

		CartesianPadding pad = new CartesianPadding(_nI, _nJ, _nK);

		/* TODO accessing dimensions is overly complex, simplify */
		for (Dimension.DimName d : domain.getShape().getDimensionNames()) {
			Dimension dim = domain.getShape().getDimension(d);
			if (dim.isCyclic()) {
				pad.synchroniseCyclic(this, d.dimNum());
			}
		}
	}
}
