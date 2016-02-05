package grid;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import linearAlgebra.Array;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

/**
 * \brief A grid with 2D polar coordinates and a Cartesian {@code z}
 * coordinate.
 * 
 * <p>Here we use the {@code (r, θ, z)} convention:</p><ul><li>{@code r} is the
 *  <i>radial</i> coordinate, i.e. Euclidean distance from the origin axis</li>
 *  <li>{@code θ (theta)} is the <i>azimuthal</i> coordinate (also known as the
 *  <i>polar</i>) and takes values between 0 and 2π radians</li><li>
 *  {@code z} is the <i>height</i> coordinate and is essentially a Cartesian
 *  coordinate</li></ul><p>See 
 *  <a href="http://mathworld.wolfram.com/CylindricalCoordinates.html">here</a> 
 *  for more details.</p>  
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class CylindricalGrid extends PolarGrid
{
	/**
	 * \brief The number of voxels this grid has in each of the three spatial 
	 * dimensions and the corresponding resolution calculator.
	 * 
	 * Notes:
	 * - The array has three rows, one for each dimension.
	 * - A row may contain a single value or a vector.
	 * - _resCalc[0] is the radial angle and has length 1 (single value).
	 * - _resCalc[1] is the azimuthal angle.
	 * - _resCalc[2] is the z-dimension.
	 * 
	 * - To keep the volume over the grid cells fairly constant for same 
	 * 		resolutions, some dependencies between the _nVoxels were implemented:
	 *  * The number of voxels along the azimuthal dimension (np) 
	 *  	is dependent on the radius (r): nt=ires[1]*s(r) with s(r)=2*r+1;	  
	 * 
	 * <p>For example, a disc (-> ires[1]=4) with radius 2 
	 * 			 and uniform resolution 1 would have: 
	 * 		_nVoxel = [ [[2]], [[4],[12]], [[1]] ].
	 * 		_res = [[[1.0, 1.0]], [[1.0^4, 1.0^12]], [[1.0]]]</p>
	 */
	protected ResCalc[][] _resCalc;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * @param totalLength - length in each dimension
	 * @param res -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public CylindricalGrid(double[] totalLength, double[] res)
	{
		/*
		 * Set up members of super class.
		 */
		super(totalLength);
		this._dimName[1] = DimName.THETA;
		this._dimName[2] = DimName.Z;
		/*
		 * Set up uniform resolution calculator array.
		 */
		ResolutionCalculator resolution = new ResolutionCalculator();
		/*
		 * Set up for the radial coordinate, and find out how many shells we
		 * have.
		 */
		this._resCalc = new UniformResolution[3][];
		this._resCalc[0] = new UniformResolution[1];
		this._resCalc[0][0] = resolution.new UniformResolution();
		this._resCalc[0][0].init(res[0], totalLength[0]);
		int nr = this._resCalc[0][0].getNVoxel();
		/*
		 * Set up for the height coordinate {@code z}.
		 */
		this._resCalc[2] = new UniformResolution[1];
		this._resCalc[2][0] = resolution.new UniformResolution();
		this._resCalc[2][0].init(res[2], totalLength[2]);
		/*
		 * Set up for the azimuthal coordinate {@code θ (theta)}. The number of
		 * voxels in theta depends on the shell (i.e. size of r).
		 */
		this._resCalc[1] = new UniformResolution[nr];
		double targetRes;
		for ( int shell = 0; shell < nr; shell++ )
		{
			this._resCalc[1][shell] = resolution.new UniformResolution();
			// this._ires[1] * s(shell) =
			// (2 * Math.toRadians(totalSize[1]) / pi) * ((2 * shell) + 1)
			//this._resCalc[1][shell].init(res[1], this._ires[1] * s(shell));
			// Why not just initalise with totalLength[1] in radians and
			// save ourselves a lot of hassle?!
			targetRes = res[1] / ( ( 2 * shell ) + 1);
			this._resCalc[1][shell].init(targetRes, totalLength[1]);
		}
	}
	
	/**
	 * @param totalLength - length in each dimension
	 * @param res -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public CylindricalGrid(double[] totalLength, double res)
	{
		this(totalLength, Vector.vector(3, res));
	}
		
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this.hasArray(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
		{ 
			this._array.put(type, 
					  PolarArray.createCylinder(this._resCalc, initialValues));
		}
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		double[] origin = getVoxelOrigin(coord);
		double[] upper = getVoxelUpperCorner(coord);
		/* 
		 * r: pi times this number would be the area of a ring. 
		 */
		double volume = ExtraMath.sq(upper[0]) - ExtraMath.sq(origin[0]);
		/* 
		 * theta: this number divided by pi would be the arc length.
		 */
		volume *= (upper[1] - origin[1]) * 0.5;
		/* 
		 * z: height. 
		 */
		volume *= (upper[2] - origin[2]);
		return volume;
	}
	
	@Override
	public int[] getCoords(double[] loc, double[] inside)
	{
		// TODO inside doesn't seem to be used.
		// TODO this gives loc in cylindrical coordinates, shouldn't it be in
		// Cartesian?
		int[] coord = new int[3];
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			coord[dim] = rC.getVoxelIndex(loc[dim]);
			if ( inside != null )
			{
				inside[dim] = loc[dim] - 
								rC.getCumulativeResolution(coord[dim] - 1);
			}
		}
		return coord;
	}
	
	@Override
	public double[] getLocation(int[] coord, double[] inside)
	{
		// TODO this gives the location in cylindrical dimensions... convert to
		// Cartesian?
		double[] loc = Vector.copy(inside);
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			loc[dim] *= rC.getResolution(coord[dim]);
			loc[dim] += rC.getCumulativeResolution(coord[dim] - 1);
		}
		return loc;
	}

	@Override
	public void calcMinVoxVoxResSq()
	{
		// TODO cyclic boundaries (if any)
		double m = Double.MAX_VALUE;
		ResCalc rC;
		/* 
		 * Determine minimal squared resolution in r and z (axes 0 and 2). 
		 */ 
		for ( int axis = 0; axis < 3; axis += 2 )
		{
			rC = this._resCalc[axis][0];
			for ( int i = 0; i < rC.getNVoxel() - 1; i++ )
				m = Math.min(m, rC.getResolution(i) * rC.getResolution(i+1));
		}
		/* 
		 * Determine minimal squared resolution in theta (axis 1).
		 * 
		 * TODO This doesn't account for partially-overlapping voxel-voxel
		 * interfaces. 
		 */ 
		for ( int shell = 0; shell < this._resCalc[1].length; shell++ )
		{
			rC = this._resCalc[1][shell];
			for ( int i = 0; i < rC.getNVoxel() - 1; i++ )
				m = Math.min(m, rC.getResolution(i) * rC.getResolution(i+1));
		}
		this._minVoxVoxDist = m;
	}
	
	@Override
	public int[] getNVoxel(int[] coords)
	{
		// TODO make this a permanent vector, rather than initialising anew
		// each time it's called for.
		return new int[]{this._resCalc[0][0].getNVoxel(),
							this._resCalc[1][coords[0]].getNVoxel(),
							this._resCalc[2][0].getNVoxel()};
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		return this._resCalc[axis][ (axis == 1) ? coord[0] : 0 ];
	}
	
	/*************************************************************************
	 * ITERATOR
	 ************************************************************************/
	

	/**
	 * \brief Try moving the neighbor iterator to the r-shell just outside that
	 * of the current coordinate. Set the neighbor iterator valid flag to false
	 * if this fails.
	 */
	protected void moveNbhToOuterShell()
	{
		if ( ! this.setNbhFirstInNewShell(1, this._currentCoord[0] + 1) )
			this._nbhValid = false;
	}
	
	/**
	 * \brief Move the neighbor iterator to the same r- and z-indices as the
	 * current coordinate, and make the theta-index one less.
	 * 
	 * @return {@code boolean} reporting whether this is valid.
	 */
	protected boolean moveNbhToMinus(int dim)
	{
		Vector.copyTo(this._currentNeighbor, this._currentNeighbor);
		this._currentNeighbor[dim]--;
		return (this._currentCoord[dim] == 0) && 
										(this._dimBoundaries[dim][0] == null);
	}
	
	@Override
	public int[] resetNbhIterator()
	{
		/*
		 * First check that the neighbor iterator is initialised and set to the
		 * current coordinate.
		 */
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			Vector.copyTo(this._currentNeighbor, this._currentCoord);
		/*
		 * Now find the first neighbor.
		 */
		this._nbhValid = true;
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell(1, this._currentCoord[0] - 1) )
			return this._currentNeighbor;
		/* See if we can take one of the theta-neighbors. */
		if ( this.moveNbhToMinus(1) || this.nbhJumpOverCurrent(1) )
			return this._currentNeighbor;
		/* See if we can take one of the z-neighbors. */
		if ( this.moveNbhToMinus(2) || this.nbhJumpOverCurrent(2) )
			return this._currentNeighbor;
		/* See if we can use the outside r-shell. */
		if ( this.setNbhFirstInNewShell(1, this._currentCoord[0] + 1) )
			return this._currentNeighbor;
		/* There are no valid neighbors. */
		this._nbhValid = false;
		return this._currentNeighbor;
	}
	
	
	@Override
	public int[] nbhIteratorNext()
	{
		/*
		 * In the cylindrical grid, we start the 
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/* 
			 * We're in the r-shell just inside that of the current coordinate.
			 * Try increasing theta by one voxel. If this fails, move out to
			 * the next shell. If this fails, call this method again.
			 */
			if ( ! this.increaseNbhByOnePolar(1) )
				if ( ! this.moveNbhToMinus(1) )
					return this.nbhIteratorNext();
					
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[2] == this._currentCoord[2] )
			{
				/*
				 * We're in the same z-slice as the current coordinate.
				 * Try to move to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the z-minus
				 * voxel.
				 */
				if ( ! this.nbhJumpOverCurrent(1) )
					if ( ! this.moveNbhToMinus(2) )
						return this.nbhIteratorNext();
			}
			else if ( ! this.nbhJumpOverCurrent(2) )
			{
				/*
				 * We tried to move to the z-plus side of the current
				 * coordinate, but since we failed we must be finished.
				 */
				this.moveNbhToOuterShell();
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase theta any more, then we've finished.
			 */
			if ( ! this.increaseNbhByOnePolar(1) )
				this._nbhValid = false;
		}
		return this._currentNeighbor;
	}
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	public static final GridGetter standardGetter()
	{
		return new GridGetter()
		{			
			@Override
			public CylindricalGrid newGrid(double[] totalLength,
															double resolution) 
			{
				return new CylindricalGrid(totalLength, resolution);
			}
		};
	}
}
