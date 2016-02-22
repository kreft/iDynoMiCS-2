package grid;

import grid.resolution.ResCalcFactory;
import grid.resolution.ResolutionCalculator.ResCalc;
import linearAlgebra.Array;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.CyclicGrid;
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
	 * dimensions and the corresponding resolution calculator. </br>
	 * 
	 * <p>Notes:
	 * <ul>
	 * <li> The array has three rows, one for each dimension.</li>
	 * <li> A row may contain a single value or a vector.</li>
	 * <li> _resCalc[0] is the radial angle and has length 1 (single value).</li>
	 * <li> _resCalc[1] is the azimuthal angle.</li>
	 * <li> _resCalc[2] is the z-dimension.</li>
	 * <li> The number of voxels along the azimuthal dimension {@code η_θ} 
	 *  	is dependent on the radius {@code r}: </br>
	 *  	η_θ(r) = N₀ * s(r) with s(r) = 2 * r + 1;	</li>  
	 * </ul></p>
	 * 
	 * <p>For example, a disc (-> N₀ = 4) with radius 2 
	 * 			 and uniform resolution 1 would have: </br>
	 * 		_nVoxel = { {2}, {4,12}, {1} }.</br>
	 * 		_res = { {1.0, 1.0}, {{1.0}^4, {1.0}^12}, {1.0}]</p>
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
	public CylindricalGrid(ResCalc[][] resCalc)
	{
		/*
		 * Set up members of super class.
		 */
		super();
		this._dimName[1] = DimName.THETA;
		this._dimName[2] = DimName.Z;
		
		this._resCalc = resCalc;
		
		/* handle periodicity here or in another place? */
		if (getTotalLength(1) < 0 || getTotalLength(1) > 2 * Math.PI)
			throw new IndexOutOfBoundsException(
										"0 <= totalLength <= 2π not satisfied");
		/* 
		 * Add cyclic boundaries for theta if we have a full circle.
		 */
		if ( ExtraMath.areEqual(this.getTotalLength(1), 2 * Math.PI, 1E-10) )
		{
			this.addBoundary(DimName.THETA, 0, new CyclicGrid());
			this.addBoundary(DimName.THETA, 1, new CyclicGrid());
		}
		
		resetIterator();
		resetNbhIterator();
	}
	
	/**
	 * @param totalLength - length in each dimension
	 * @param res -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public CylindricalGrid(double[] totalLength, double resolution)
	{
		this(ResCalcFactory.createUniformResCalcForCylinder(totalLength, resolution));
	}

	public CylindricalGrid()
	{
		this(new double[]{1.0, Math.PI / 2.0, 1.0}, 1.0);
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
//		for ( int shell = 0; shell < this._resCalc[1].length; shell++ )
//		{
//			rC = this._resCalc[1][shell];
//			for ( int i = 0; i < rC.getNVoxel() - 1; i++ )
//				m = Math.min(m, rC.getResolution(i) * rC.getResolution(i+1));
//		}
		System.out.println(m);
		this._minVoxVoxDist = m;
	}
	
	@Override
	public double getNbhSharedSurfaceArea()
	{
//		int absDiff = 0, cumulativeAbsDiff = 0;
//		double area = 1.0;
//		ResCalc rC;
//		for ( int i = 0; i < 3; i++ )
//		{
//			absDiff = Math.abs(this._currentCoord[i] - this._currentNeighbor[i]);
//			if ( absDiff == 0 ){
//				rC = this.getResolutionCalculator(this._currentCoord,i);
//				area *= rC.getResolution(this._currentCoord[i]);
//			}
//			else
//				cumulativeAbsDiff += absDiff;
//		}
//		return ( cumulativeAbsDiff == 1 ) ? area : 0.0;
		return 1;
	}
	
	@Override
	public int[] getNVoxel(int[] coords, int[] outNVoxel)
	{
		if (outNVoxel == null)
			outNVoxel = new int[3];
		/*
		 * resolution calculator in first dimension ({@code r})
		 * should always be stored in resCalc[0][0] (no checking needed)
		 */
		ResCalc rC = getResolutionCalculator(coords, 0);
		outNVoxel[0] = rC.getNVoxel();
		/*
		 * check if the coordinate is valid for 2nd dimension 
		 * ({@code theta})
		 */
		boolean is_inside_r = coords[0] >= 0 && coords[0] < rC.getNVoxel();
		if (is_inside_r){
			rC = getResolutionCalculator(coords, 1);
			outNVoxel[1] = rC.getNVoxel();
		}
		else outNVoxel[1] = 0;

		/*
		* resolution calculator in third dimension ({@code z})
		* should always be stored in resCalc[2][0] (no checking needed)
		*/
		rC = getResolutionCalculator(coords, 2);
		outNVoxel[2] = rC.getNVoxel();
		
		return outNVoxel;
	}
	
	@Override
	public double getTotalLength(int dim)
	{
		return this._resCalc[dim][0].getTotalLength();
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		return this._resCalc[axis][(axis == 1) ? coord[0] : 0];
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
		if ( ! this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
			this._nbhValid = false;
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
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] - 1) )
			return this._currentNeighbor;
		/* See if we can take one of the theta-neighbors. */
		if ( this.moveNbhToMinus(1) || this.nbhJumpOverCurrent(1) )
			return this._currentNeighbor;
		/* See if we can take one of the z-neighbors. */
		if ( this.moveNbhToMinus(2) || this.nbhJumpOverCurrent(2) )
			return this._currentNeighbor;
		/* See if we can use the outside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
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
