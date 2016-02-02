package grid;

import java.util.Arrays;
import java.util.HashMap;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.BoundarySide;

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
	
	/**
	 * @param totalLength - length in each dimension
	 * @param res -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public CylindricalGrid(double[] totalLength, double[] res){
		/*
		 * Set up members of super class
		 */
		super(totalLength);
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
	public CylindricalGrid(){
		this(new double[]{1,90,1},1);
	}
		
	@Override
	public void newArray(ArrayType type, double initialValues) {
		/*
		 * First check that the array HashMap has been created.
		 */
		if ( this._array == null )
			this._array = new HashMap<ArrayType, double[][][]>();
		/*
		 * Now try resetting all values of this array. If it doesn't exist
		 * yet, make it.
		 */
		if ( this._array.containsKey(type) )
			PolarArray.applyToAll(
					_array.get(type), ()->{return initialValues;});
		else
		{
			double[][][] array = PolarArray.createCylinder(
					_resCalc, initialValues);
			this._array.put(type, array);
		}
	}
	
	public ResCalc[][] getResCalc(){
		return _resCalc;
	}
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		double[] loc1=getVoxelOrigin(coord);
		double[] loc2=getLocation(coord,VOXEL_All_ONE_HELPER);
		/*
		 * mathematica:
		 * Integrate[
		 * 	Integrate[(1/2*r2^2),{t,t1,t2}] 
		 * 		- Integrate[(1/2 *r1^2),{t,t1,t2}], 
		 * {z,z1,z2}]
		 */		
		return  ((loc2[0] * loc2[0] - loc1[0] * loc1[0])
					* (loc2[1] - loc1[1])
					* (loc2[2] - loc1[2])
				) / 2;
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
		return _resCalc[1][coord[0]].getResolution(coord[1])
			* _radSize[1] / _resCalc[1][coord[0]].getTotalLength();
	}
	
	@Override
	// TODO: Assumes constant resolution at the moment?!
	public int[] getCoords(double[] loc, double[] inside) {
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
		 * Determine r (like in cartesian grid)
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
		 * Determine z (like in cartesian grid)
		 */
		cartCoord2Loc(2, 
				coord[2], 
				_resCalc[2][0], 
				inside[2], 
				loc);
		return loc;
	}

	@Override
	public void calcMinVoxVoxResSq() {
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
	public int[] cyclicTransform(int[] coord) {
		/*
		 * This function will transform the given coordinate orthogonal
		 * to the boundary (or boundaries) it exceeds.
		 */
		
		/* Stores the boundary side for each dimension we are looking at */
		BoundarySide bs;
		/* Stores the number of voxels for each dimension we are looking at */
		int nVoxel;
		
		/* Transform along radial dimension first. */
		nVoxel = _resCalc[0][0].getNVoxel();
		bs = isOutside(coord,0);
		if (bs!=null){
			switch (bs){
			case RMAX: 
				/* flip azimuthal coordinate by 180° */
				
				coord[0] = coord[0]%(nVoxel-1); break;
			case RMIN: 
				coord[0] = 0;
				/* 
				 * If the boundary side is internal 
				 * 2 = 180° at coord[0]=0
				 */
				coord[1] = coord[1]+2;  
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}

		nVoxel = _resCalc[2][0].getNVoxel();
		bs = isOutside(coord,2);
		if (bs!=null){
			switch (bs){
			case ZMAX:
				coord[2] = coord[2]%(nVoxel-1); break;
			case ZMIN:
				coord[2] = nVoxel+coord[2]; break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		
		nVoxel = _resCalc[1][coord[0]].getNVoxel();
		bs = isOutside(coord,1);
		if (bs!=null){
			switch (bs){
			case THETAMAX: coord[1] = coord[1]%(nVoxel-1); break;
			case THETAMIN: coord[1] = nVoxel+coord[1]; break;
			case INTERNAL:
				coord[1] = coord[1]%nVoxel; 
				if (coord[1] < 0) coord[1] += nVoxel;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		return coord;
	}

//	/* (non-Javadoc)
//	 * @see grid.SpatialGrid#getNbhSharedSurfaceArea()
//	 */
//	@Override
//	public double getNbhSharedSurfaceArea() {
//		double sA=0, t1_nbh, t2_nbh;
//		boolean is_right, is_left, is_inBetween;
//		t1_nbh = t-inside[1]*len_nbh;
//		t2_nbh = t+(1-inside[1])*len_nbh;
//		
//		// t1 of nbh <= t1 of cc (right counter-clockwise)
//		if (dr < 0){
//			is_right = t1_nbh <= t1_cur;
//			is_left = t2_nbh >= t2_cur;
//			is_inBetween = is_left && is_right;
//			len_s = len_cur;
//		}else{
//			is_right = t1_nbh < t1_cur;
//			is_left = t2_nbh > t2_cur;
//			is_inBetween = !(is_left || is_right);
//			len_s = len_nbh;
//		}
//		
//		if (is_inBetween) sA = 1;
//		else if (is_right) sA = (t2_nbh-t1_cur)/len_s;
//		else sA = (t2_cur-t1_nbh)/len_s; // is_left
//		
//		return sA;
//	}

	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getCurrentNbhResSq()");
		return 1;
	}
	
	@Override
	public boolean isIteratorValid() {
		return _currentCoord[0] < _resCalc[0][0].getNVoxel();
	}
	
	@Override
	protected boolean iteratorExceeds(int axis) {
		switch(axis){
		case 0: case 2: 
			return _currentCoord[axis] >= _resCalc[axis][0].getNVoxel();
		case 1: 
			return iteratorExceeds(0) ? true : _currentCoord[axis] 
						>= _resCalc[axis][_currentCoord[0]].getNVoxel();
		default: 
			throw new RuntimeException("0 < axis <= 3 not satisfied");
		}
	}
	
	@Override
	public void fillNbhSet() {
		/*
		 * Moving along radial dimension.
		 */
		int[] cc = _currentCoord;
		if (_nbhIdx>3){			
			/*
			 * change in r (-1 or 1)
			 */
			int[] nbh_coord = new int[3];
			int dr = NBH_DIRECS[_nbhIdx][0];
			nbh_coord[0] = cc[0] + dr;
			if (isOutside(new int[]{nbh_coord[0], -1, -1}, 0) == null){
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
				
				while(bounds_nbh[0] < bounds[1]){	
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

	@Override
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.RMIN;
			if ( coord[0] >= _resCalc[0][0].getNVoxel() )
				return BoundarySide.RMAX;
			return null;
		case 1:
			if ( coord[1] < 0 )
				return _radSize[1]==2*Math.PI ?
							BoundarySide.INTERNAL : BoundarySide.THETAMIN;
			if (isOutside(coord,0)!=null)  
				return null;
			int nt=_resCalc[1][coord[0]].getNVoxel();
			if ( coord[1] >= nt)
				return _radSize[1]==2*Math.PI ?
							BoundarySide.INTERNAL : BoundarySide.THETAMAX;
			return null;
		case 2:
			if ( coord[2] < 0 )
				return BoundarySide.ZMIN;
			if ( coord[2] >= _resCalc[2][0].getNVoxel() )
				return BoundarySide.ZMAX;
			return null;
		default: throw new IllegalArgumentException("dim must be > 0 and < 3");
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
