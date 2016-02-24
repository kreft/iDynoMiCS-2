package grid;

import java.util.Arrays;

import org.w3c.dom.Node;

import grid.resolution.ResCalcFactory;
import grid.resolution.ResolutionCalculator.ResCalc;
import linearAlgebra.Array;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.CyclicGrid;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

/**
 * \brief A grid with a spherical coordinate system.
 *  
 *  <p>Here we use the {@code r, φ, θ)} convention:</p><ul><li>{@code r} is the
 *  <i>radial</i> coordinate, i.e. Euclidean distance from the origin</li><li>
 *  {@code φ (phi)} is the <i>polar</i> coordinate (also known as the
 *  <i>zenith</i> or <i>colatitude</i>) and takes values between 0 and π 
 *  radians</li></ul><p><li>
 *  {@code θ (theta)} is the <i>azimuthal</i> coordinate (also known as the
 *  <i>longitude</i>) and takes values between 0 and 2π radians</li>See 
 *  <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">here</a> 
 *  for more details.</p>  
 *  
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class SphericalGrid extends PolarGrid
{
	/**
	 * \brief The number of voxels this grid has in each of the three spatial 
	 * dimensions and the corresponding resolution calculator.
	 * 
	 * Notes:
	 * - The array has three rows, one for each dimension.
	 * - A row may contain a single value, a vector or a matrix.
	 * - _resCalc[0] is the radius and has length 1 (single value).
	 * - _resCalc[1] is the polar angle φ.
	 * - _resCalc[2] is the azimuthal angle θ.
	 * 
	 * - To keep the volume over the grid cells fairly constant for same 
	 * 		resolutions, some dependencies between the number of voxels
	 * 		were implemented:
	 *  * The number of voxels along the polar dimension (η_φ) 
	 *  	is dependent on the radius r: η_φ(r)= N₀ * s(r) with s(r)=2*r+1;
	 * 	* The number of voxels along the azimuthal dimension (η_θ) 
	 * 		is dependent on the radius r and the polar angle φ.
	 *    This dependency is actually a sine with the 
	 *    	- domain scaled from [0,π] to [0, η_φ(r) - 1] and the 
	 *    	- co-domain scaled from [0,1] so that it peaks with a value of 	
	 *    			η_φ(r) - 0.5 at the equator.  	  
	 * 
	 * <p>For example, a sphere (-> N₀_θ = 4, N₀_φ = 2) with radius 2  
	 * 			and resolution 1 would have:
	 * 		_nVoxel = [ [[2]], [[2],[6]], [[4,4],[4,12,20,20,12,4]] ].
	 *		_res = ...
	 * </p> 
	 */
	protected ResCalc[][][] _resCalc;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public SphericalGrid(double[] totalLength){
		this(totalLength, null);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param totalLength
	 * @param res
	 */
	public SphericalGrid(double[] totalLength, Node node)
	{
		super();
		this._dimName[1] = DimName.PHI;
		this._dimName[2] = DimName.THETA;
		
		/* define ResCalc array */
		this._resCalc = new ResCalc[3][][];
		this._resCalc[0] = new ResCalc[1][1];
		this._resCalc[1] = new ResCalc[1][];
		
		/* create appropriate ResCalc Objects for dimension combinations*/
		ResCalcFactory rcf = new ResCalcFactory(this._dimName);
		rcf.init(node);
		/* check for dim name equality and right order (this should usually 
		 * be ensured by the init(Node) function of ResCalcFactory) */ 
		if (!Arrays.equals(this._dimName, rcf.getDimNames()))
			//TODO: break cleaner
			throw new IllegalArgumentException(
					"tried to set inappropriate resolution calculator");
		Object[] resCalc = rcf.createResCalcForDimensions(totalLength);

		/* cast to correct data type and update the array */
		this._resCalc[0][0][0] = (ResCalc) resCalc[0];
		this._resCalc[1][0] = (ResCalc[]) resCalc[1];
		this._resCalc[2] = (ResCalc[][]) resCalc[2];
		
		/* handle periodicity here or in another place? */
		if (getTotalLength(1) < 0 || getTotalLength(1) > Math.PI)
			throw new IndexOutOfBoundsException(
										"0 <= totalLength <= π not satisfied");
		
		if (getTotalLength(2) < 0 || getTotalLength(2) > 2 * Math.PI)
			throw new IndexOutOfBoundsException(
										"0 <= totalLength <= 2π not satisfied");
		/* 
		 * Add cyclic boundaries for phi if we have a half circle.
		 */
		// TODO More robust definition of tolerance here.
		if ( ExtraMath.areEqual(this.getTotalLength(1), Math.PI, 1E-10) )
		{
			this.addBoundary(DimName.PHI, 0, new CyclicGrid());
			this.addBoundary(DimName.PHI, 1, new CyclicGrid());
		}
		
		/* 
		 * 
		 * Add cyclic boundaries for theta if we have a full circle.
		*/
		// TODO More robust definition of tolerance here.
		if ( ExtraMath.areEqual(this.getTotalLength(2), 2 * Math.PI, 1E-10) )
		{
			this.addBoundary(DimName.THETA, 0, new CyclicGrid());
			this.addBoundary(DimName.THETA, 1, new CyclicGrid());
		}
	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this._array.containsKey(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
		{
			this._array.put(type,
					PolarArray.createSphere(this._resCalc, initialValues));
		}
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/

	@Override
	public void calcMinVoxVoxResSq()
	{
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method calcMinVoxVoxResSq()");
	}
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		//TODO: wrong
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1 = getVoxelOrigin(coord);
		double[] loc2 = getVoxelUpperCorner(coord);
		/* r */
		double out = ExtraMath.cube(loc1[0]) - ExtraMath.cube(loc2[0]);
		/* phi */
		out *= loc1[1] - loc2[1];
		/* theta */
		out *= Math.cos(loc1[2]) - Math.cos(loc2[2]);
		return out / 3.0;
	}
	
	@Override
	public double getNbhSharedSurfaceArea()
	{
		int[] cur = this._currentCoord;
		int[] nbh = this._currentNeighbor;
		
		ResCalc rcCur;
		
		double area = 1.0;

		if (nbh[2] != cur[2]) {
			rcCur = getResolutionCalculator(cur, 0);
			area *= rcCur.getResolution(cur[0]);
			rcCur = getResolutionCalculator(cur, 1);
			area *= rcCur.getResolution(cur[1]);
		}
		else{
			area *= getNbhSharedArcLength(2);
			if (nbh[0] != cur[0]) {
				area *= getNbhSharedArcLength(1);
			}else {
				rcCur = getResolutionCalculator(cur, 0);
				area *= rcCur.getResolution(cur[0]);
			}
		}
		return area;
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
		 * check if the coordinate is valid for 2nd dimension ({@code phi})
		 */
		boolean is_inside_r = (coords[0] >= 0) && (coords[0] < rC.getNVoxel());
		if ( is_inside_r )
		{
			rC = getResolutionCalculator(coords, 1);
			outNVoxel[1] =  rC.getNVoxel();
		}
		else
			outNVoxel[1] = 0;
		/*
		* check if the coordinate is valid for 3rd dimension ({@code theta})
		*/
		boolean is_inside_phi = (coords[1] >= 0)&&(coords[1] < rC.getNVoxel());
		if ( is_inside_r && is_inside_phi )
		{
			rC = getResolutionCalculator(coords, 2);
			outNVoxel[2] = rC.getNVoxel();
		}
		else
			outNVoxel[2] = 0;
		return outNVoxel;
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		switch ( axis )
		{
			/* r */
			case 0: return this._resCalc[0][0][0];
			/* phi */
			case 1: return this._resCalc[1][0][coord[0]];
			/* theta */
			case 2: return this._resCalc[2][coord[0]][coord[1]];
			// TODO throw an exception?
			default: return null;
		}
	}
	
	@Override
	public double getTotalLength(int dim)
	{
		return this._resCalc[dim][0][0].getTotalLength();
	}
	
	/*************************************************************************
	 * ITERATOR
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 * @param shellIndex
	 * @return
	 */
	protected boolean setNbhFirstInNewRing(int ringIndex)
	{
		this._currentNeighbor[1] = ringIndex;
		
		/* If we are on an invalid shell, we are definitely in the wrong place*/
		if (isOnBoundary(this._currentNeighbor, 0))
			return false;
		
		/*
		 * First check that the new ring is inside the grid. If we're on a
		 * defined boundary, the theta coordinate is irrelevant.
		 */
		if (isOnBoundary(this._currentNeighbor, 1))
			return false;
		
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 2);
		/*
		 * We're on an intermediate ring, so find the voxel which has the
		 * current coordinate's minimum theta angle inside it.
		 */
		double theta = rC.getCumulativeResolution(this._currentCoord[2] - 1);
		
		rC = this.getResolutionCalculator(this._currentNeighbor, 2);
		
		this._currentNeighbor[2] = rC.getVoxelIndex(theta);
		return true;
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
		if ( this.setNbhFirstInNewShell( this._currentCoord[0] - 1 ) && 
				this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
			return this._currentNeighbor;
		
		/* 
		 * See if we can take one of the phi-minus-neighbors of the current 
		 * r-shell. 
		 */
		if ( this.setNbhFirstInNewShell( this._currentCoord[0]) 
				&& this.setNbhFirstInNewRing( this._currentCoord[1] - 1) )
			return this._currentNeighbor;
		/* See if we can take one of the theta-neighbors 
		 * in the current phi-shell.
		 */
		if ( this.moveNbhToMinus(2) || this.nbhJumpOverCurrent(2) )
			return this._currentNeighbor;
		/* See if we can take one of the phi-plus-neighbors. */
		if ( this.setNbhFirstInNewRing( this._currentCoord[1] + 1) )
			return this._currentNeighbor;
		
		/* See if we can use the outside r-shell. */
		if ( this.setNbhFirstInNewShell( this._currentCoord[0] + 1 ) && 
				this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
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
			 * We're in the r-shell just inside that of the 
			 * current coordinate.
			 * Try increasing theta by one voxel. If this fails, move out to
			 * the next ring. If this fails, move out to the next shell (which 
			 * must be existent, since it is the one of the current coord) 
			 * and try to move to the phi-minus ring. 
			 * If this fails, the phi-ring must be invalid, so try to move to
			 * the theta-minus neighbor in the current phi-ring.
			 * If this fails call this method again.
			 */
			if ( ! this.increaseNbhByOnePolar(2) )
				if ( ! this.increaseNbhByOnePolar(1) ||
						! this.setNbhFirstInNewRing(_currentNeighbor[1]) )
					if ( ! this.setNbhFirstInNewShell( this._currentCoord[0] ) 
						|| !this.setNbhFirstInNewRing( this._currentCoord[1] - 1 ) )						
						if ( ! this.moveNbhToMinus(2) )
							return this.nbhIteratorNext();
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[1] == this._currentCoord[1] - 1 )
			{
				/*
				 * We're in the phi-ring just inside that of the 
				 * current coordinate.
				 * Try increasing theta by one voxel. If this fails, move out to
				 * the next ring. If this fails, call this method again.
				 */
				if ( ! this.increaseNbhByOnePolar(2) )
					if ( ! this.moveNbhToMinus(2) )
						return this.nbhIteratorNext();
			}
			else if ( this._currentNeighbor[1] == this._currentCoord[1] )
			{
				/*
				 * We're in the same phi-ring as the current coordinate.
				 * Try to jump to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the phi-plus
				 * ring.
				 */
				if ( ! this.nbhJumpOverCurrent(2) )
					if ( ! this.setNbhFirstInNewRing(this._currentCoord[1] + 1) )
						return this.nbhIteratorNext();
			}
			else 
			{
				/* We're in the phi-ring just outside that of the 
				 * current coordinate. 
				 * Try increasing theta by one voxel. If this fails, move out 
				 * to the next shell. If this fails, move to the next rings. If
				 * this fails, we are finished.
				*/
				if ( ! this.increaseNbhByOnePolar(2) )
					if (! this.setNbhFirstInNewShell(this._currentCoord[0] + 1) 
						|| !this.setNbhFirstInNewRing( this._currentNeighbor[1]) )
						if (!this.increaseNbhByOnePolar(1)	||
								! this.setNbhFirstInNewRing(_currentNeighbor[1]) )
							if (!this.increaseNbhByOnePolar(1)	||
									! this.setNbhFirstInNewRing(_currentNeighbor[1]) )
								this._nbhValid = false;
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase phi and theta any more, then we've finished.
			 */
			if ( ! this.increaseNbhByOnePolar(2) )
				if ( ! this.increaseNbhByOnePolar(1) ||
						! this.setNbhFirstInNewRing(_currentNeighbor[1]) )
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
			public SpatialGrid newGrid(double[] totalLength, Node node) 
			{
				return new SphericalGrid(totalLength, node);
			}
		};
	}
}
