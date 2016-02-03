package grid;

import java.util.Arrays;
import java.util.HashMap;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
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
		 * Set up members of super class
		 */
		super(totalLength);
		this._dimName[1] = DimName.THETA;
		this._dimName[2] = DimName.Z;
		/*
		 * Set up uniform resolution calculator array
		 */
		ResolutionCalculator resolution = new ResolutionCalculator();
		/*
		 * Set up for the radial coordinate, and find out how many shells we
		 * have.
		 */
		_resCalc = new UniformResolution[3][];
		_resCalc[0] = new UniformResolution[1];
		_resCalc[0][0] = resolution.new UniformResolution();
		_resCalc[0][0].init(res[0], totalLength[0]);
		int nr = _resCalc[0][0].getNVoxel();
		
		/* Set up for the height coordinate {@code z}. */
		_resCalc[2] = new UniformResolution[1];
		_resCalc[2][0] = resolution.new UniformResolution();
		_resCalc[2][0].init(res[2], totalLength[2]);
		
		/*
		 * Set up for the azimuthal coordinate {@code θ (theta)}. The number of
		 * voxels in theta depends on the radial coordinate {@code r}.
		 */
		_resCalc[1] = new UniformResolution[nr];
		for (int i=0; i<nr; ++i){
			_resCalc[1][i] = resolution.new UniformResolution();
			_resCalc[1][i].init(res[1], (int) (_ires[1] * s(i)));
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

	/**
	 * Constructs a Grid with lengths (1,90,1) -- one grid cell
	 */
	public CylindricalGrid()
	{
		this(new double[]{1,90,1},1);
	}
		
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this.hasArray(type) )
			PolarArray.setAllTo(_array.get(type), initialValues);
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
		/*
		 * mathematica:
		 * Integrate[
		 * 	Integrate[(1/2*r2^2),{t,t1,t2}] 
		 * 		- Integrate[(1/2 *r1^2),{t,t1,t2}], 
		 * {z,z1,z2}]
		 */
		double[] loc1 = getVoxelOrigin(coord);
		double[] loc2 = getVoxelUpperCorner(coord);
		/* r */
		double out = ExtraMath.sq(loc2[0]) - ExtraMath.sq(loc1[0]);
		/* theta */
		out *= (loc2[1] - loc1[1]);
		/* z */
		out *= (loc2[2] - loc1[2]);
		return out * 0.5;
	}
	
	/**
	 * \brief Computes the arc length of the grid element at the given coordinate.
	 * 
	 * TODO: Assumes constant resolution at the moment?!
	 * 
	 * @param coord - A coordinate inside the grid.
	 * @return - The arc length of the grid element at the given coordinate.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthTheta(int[] coord)
	{
		return this._resCalc[1][coord[0]].getResolution(coord[1])
			* this._radSize[1] / this._resCalc[1][coord[0]].getTotalLength();
	}
	
	@Override
	// TODO: Assumes constant resolution at the moment?!
	public int[] getCoords(double[] loc, double[] inside)
	{
		int[] coord = new int[3];
		/*
		 * determine i 
		 */
		cartLoc2Coord(0, 
				loc[0],
				_resCalc[0][0],
				coord, 
				inside);
		/*
		 * determine j
		 */
		polarLoc2Coord(1, 
				loc[1],
				this._radSize[1], 
				this._resCalc[1][coord[0]],
				coord, 
				inside);
		/*
		 * determine k
		 */
		cartLoc2Coord(2,
				loc[2],
				_resCalc[2][0], 
				coord, 
				inside);
		return coord;
	}
	
	@Override
	// TODO: Assumes constant resolution at the moment?!
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = new double[3];
		/*
		 * Determine r (like in Cartesian grid)
		 */
		cartCoord2Loc(0, 
				coord[0], 
				_resCalc[0][0], 
				inside[0], 
				loc);
		/*
		 * determine t 
		 */
		polarCoord2Loc(
				1, 								
				coord[1], 						
				this._radSize[1],				
				this._resCalc[1][coord[0]], 	
				inside[1], 
				loc);
		/*
		 * Determine z (like in Cartesian grid)
		 */
		cartCoord2Loc(2, 
				coord[2], 
				_resCalc[2][0], 
				inside[2], 
				loc);
		return loc;
	}

	@Override
	public void calcMinVoxVoxResSq()
	{
		double m = Double.MAX_VALUE;
		/* 
		 * Determine minimal squared resolution in r and z (axis 0 and 2). 
		 */ 
		for ( int axis = 0; axis < 3; axis+=2 ){
			int nVoxel = _resCalc[axis][0].getNVoxel();
			for ( int i = 0; i < nVoxel - 1; i++ ){
				double res_cur = _resCalc[axis][0].getResolution(i);
				double res_next = _resCalc[axis][0].getResolution(i+1);
				m = Math.min(m, res_cur	* res_next);
			}
		}
		/* 
		 * Determine minimal squared resolution in theta (axis 1). 
		 */ 
		for (int i=0; i<_resCalc[1].length; ++i){
			int nVoxel = _resCalc[1][i].getNVoxel();
			for ( int j = 0; j < nVoxel - 1; j++ ){
				double res_cur = _resCalc[1][i].getResolution(j);
				double res_next = _resCalc[1][i].getResolution(j+1);
				m = Math.min(m, res_cur * res_next);
			}
		}
		this._minVoxVoxDist = m;
	}
	
	@Override
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int axis = 0; axis < 3; axis+=2 )
			out[axis] = (_resCalc[axis][0].getNVoxel() > 1 );  
		out[1] = _radSize[1] > 0;
		return out;
	}

	@Override
	public int numSignificantAxes()
	{
		int out = 0;
		for ( int axis = 0; axis < 3; axis+=2 )
			out += (_resCalc[axis][0].getNVoxel() > 1 ) ? 1 : 0;
		out += _radSize[1] > 0 ? 1 : 0;
		return out;
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
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getCurrentNbhResSq()");
		return 1;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	@Override
	public int[] cyclicTransform(int[] coord)
	{
		/* Output the result as a new vector. */
		int[] transformed = new int[coord.length];
		/* Transform along radial dimension first. */
		// TODO do we want to forbid cyclic boundaries in r?
		transformed[0] = Math.floorMod(coord[0],
										this._resCalc[0][0].getNVoxel());
		/* theta */
		transformed[1] = Math.floorMod(coord[1],
									this._resCalc[1][coord[0]].getNVoxel());
		/* z */
		transformed[2] = Math.floorMod(coord[2], 
											this._resCalc[2][0].getNVoxel());
		return transformed;
	}
	
	@Override
	public void fillNbhSet()
	{
		/*
		 * Moving along radial dimension.
		 */
		int[] cc = this._currentCoord;
		if ( _nbhIdx > 3 )
		{			
			/*
			 * Change in r (-1 or 1)
			 */
			int[] nbh_coord = new int[3];
			int dr = NBH_DIRECS[_nbhIdx][0];
			nbh_coord[0] = cc[0] + dr;
			if ( isOutside(new int[]{nbh_coord[0], -1, -1}, 0) == null )
			{
				nbh_coord[2] = cc[2] + NBH_DIRECS[_nbhIdx][2];
				double[] bounds = new double[2];
				double[] bounds_nbh = new double[2];
				
				double len_cur = getArcLengthTheta(cc);
				
				/*
				 * bounds
				 */
				polarCoord2Loc(0, 
						cc[1],
						_radSize[1],
						_resCalc[1][cc[0]], 
						0, 
						bounds);
				
				polarCoord2Loc(1, 
						cc[1],
						_radSize[1],
						_resCalc[1][cc[0]], 
						1, 
						bounds);
				
				/*
				 * first neighbor theta coordinate
				 */
				polarLoc2Coord(1, 
						bounds[0],
						_radSize[1],
						_resCalc[1][nbh_coord[0]], 
						nbh_coord, 
						null);
				
				/*
				 * First neighbor theta location 0 (origin)
				 */
				polarCoord2Loc(0, 
						nbh_coord[1],
						_radSize[1],
						_resCalc[1][nbh_coord[0]], 
						0, 
						bounds_nbh);
				
				while ( bounds_nbh[0] < bounds[1] )
				{	
					/*
					 * next neighbor in theta 
					 */
					polarCoord2Loc(1, 
							nbh_coord[1],
							_radSize[1],
							_resCalc[1][nbh_coord[0]], 
							1, 
							bounds_nbh);
					
					double len_nbh = getArcLengthTheta(nbh_coord);
					
					double sA = getSharedArea(dr,
							len_cur,
							bounds,
							bounds_nbh,
							len_nbh);
					
					_subNbhSet.add(Vector.copy(nbh_coord));		
					_subNbhSharedAreaSet.add(sA);
					
					bounds_nbh[0] = bounds_nbh[1];
					nbh_coord[1]++;
				}
			}
			/*
			* only change r coordinate if outside the grid along radial dimension.
			*/
			else {
				int[] nbh = new int[]{cc[0]+dr, cc[1], cc[2]};
				_subNbhSet.add(nbh);
//				double out = 1.0;
//				out *= this._resCalc[1][nbh[0]].getResolution(nbh[1]);
//				out *= this._resCalc[2][0].getResolution(nbh[2]);
//				_subNbhSharedAreaSet.add(out);
				_subNbhSharedAreaSet.add(Double.NaN);
			}
		}
		/*
		 * Moving along azimuthal dimension.
		 */
		else if ( _nbhIdx < 2 )
		{ 
			int[] nbh = Vector.add(
					Vector.copy(_currentCoord),NBH_DIRECS[_nbhIdx]);
			_subNbhSet.add(nbh);
			double out = 1.0;
			out *= this._resCalc[0][0].getResolution(nbh[0]);
			out *= this._resCalc[2][0].getResolution(nbh[2]);
			_subNbhSharedAreaSet.add(out);
		}
		/*
		 * Moving along z dimension.
		 */
		else{ 
			int[] nbh = Vector.add(
					Vector.copy(_currentCoord),NBH_DIRECS[_nbhIdx]);
			_subNbhSet.add(nbh);
			double out = 1.0;
			out *= this._resCalc[0][0].getResolution(nbh[0]);
			out *= this._resCalc[1][nbh[0]].getResolution(nbh[1]);
			_subNbhSharedAreaSet.add(out);
		}
	}
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	public static final GridGetter standardGetter()
	{
		return new GridGetter()
		{			
			@Override
			public CylindricalGrid newGrid(double[] totalLength, double resolution) 
			{
				return new CylindricalGrid(totalLength,resolution);
			}
		};
	}
}
