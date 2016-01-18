package grid;

import java.util.HashMap;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

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
		super(totalLength);
		ResolutionCalculator r = new ResolutionCalculator();
		_resCalc = new UniformResolution[3][];
		_resCalc[0] = new UniformResolution[1];
		_resCalc[0][0] = r.new UniformResolution();
		_resCalc[0][0].init(res[0], totalLength[0]);
		
		_resCalc[2] = new UniformResolution[1];
		_resCalc[2][0] = r.new UniformResolution();
		_resCalc[2][0].init(res[2], totalLength[2]);
		
		int nr = _resCalc[0][0].getNVoxel();
		_resCalc[1] = new UniformResolution[nr];
		for (int i=0; i<nr; ++i){
			_resCalc[1][i] = r.new UniformResolution();
			_resCalc[1][i].init(res[1], nRows(i));
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
	
//	/**
//	 * @param nVoxel - length in each dimension
//	 * @param resolution - Array of length 3,
//	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
//	 *  (r and z) and length 1 for dependent dimensions (t and p), 
//	 *  which implicitly scale with r.
//	 */
//	public CylindricalGrid(int[] nVoxel, double[][] resolution)
//	{
//		super(nVoxel, resolution);
//	}
//	
	
//	protected double[][] convertResolution(int[] nVoxel, double[] oldRes)
//	{
//		double [][] res = new double[3][0];
//		/*
//		 * The angular dimension theta is set by linearAlgebra.PolarArray, so
//		 * we deal with it separately.
//		 */
//		for ( int i = 0; i < 3; i += 2 )
//			res[i] = Vector.vector( nVoxel[i] , oldRes[i]);
//		res[1] = Vector.vector(1, oldRes[1]);
//		return res;
//	}
	
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
		return  ((loc2[0]*loc2[0]-loc1[0]*loc1[0])
					* (loc2[1]-loc1[1])
					* (loc2[2]-loc1[2])
				)/2;
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
	private double getArcLength(int[] coord)
	{
		// number of elements in row r
		int nt = _resCalc[1][coord[0]].getNVoxel();
		return _radSize[1]/nt;
	}
	
	@Override
	// TODO: Assumes constant resolution at the moment?!
	public int[] getCoords(double[] loc, double[] inside) {
		int[] coord = new int[3];
		/*
		 * determine i 
		 */
		cartLoc2Coord(loc[0], _resCalc[0][0], 0, coord, inside);
//		if (isOutside(coord[0], 0)!=null) return null;
		/*
		 * determine j
		 */
		polarLoc2Coord(loc[1], getArcLength(coord), 1, coord, inside);
		/*
		 * determine k
		 */
		cartLoc2Coord(loc[2], _resCalc[2][0], 2, coord, inside);
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
		cartCoord2Loc(coord[0], _resCalc[0][0], inside[0], 0, loc);
		/*
		 * determine t 
		 */
		polarCoord2Loc(coord[1], getArcLength(coord), inside[1], 1, loc);
		/*
		 * Determine z (like in cartesian grid)
		 */
		cartCoord2Loc(coord[2], _resCalc[2][0], inside[2], 2, loc);
		return loc;
	}

	@Override
	public void calcMinVoxVoxResSq() {
		double m = Double.MAX_VALUE;
		for ( int axis = 0; axis < 3; axis+=2 )
			for ( int i = 0; i < _resCalc[axis][0].getNVoxel() - 1; i++ )
				m = Math.min(m, _resCalc[axis][0].getResolution(i)
						* _resCalc[axis][0].getResolution(i+1));
		for (int i=0; i<_resCalc[1].length; ++i){
			for ( int j = 0; j < _resCalc[1][i].getNVoxel() - 1; j++ ){
				m = Math.min(m, _resCalc[1][i].getResolution(j)
						* _resCalc[1][i].getResolution(j+1));
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
		
		int nr = _resCalc[0][0].getNVoxel();
		BoundarySide bs = isOutside(coord,0);
		if (bs==BoundarySide.CIRCUMFERENCE)
			coord[0] = coord[0]%(nr-1);
		if (bs==BoundarySide.INTERNAL){
			coord[0] = 0;
			/*
			 * 2 = 180° at coord[0]=0
			 */
			coord[1] = coord[1]+2;  
		}

		int nz = _resCalc[2][0].getNVoxel();
		bs = isOutside(coord,2);
		if (bs==BoundarySide.ZMAX)
			coord[2] = coord[2]%(nz-1);
		if (bs==BoundarySide.ZMIN)
			coord[2] = nz+coord[2];
		
		int nt = _resCalc[1][coord[0]].getNVoxel();
		bs = isOutside(coord,1);
		if (bs!=null){
			switch (bs){
			case YMAX: coord[1] = coord[1]%(nt-1); break;
			case YMIN: coord[1] = nt+coord[1]; break;
			case INTERNAL:
				coord[1] = coord[1]%nt; 
				if (coord[1] < 0) coord[1] += nt;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		return coord;
	}
	
	@Override
	public double getNbhSharedSurfaceArea() {
		//TODO: implement
		System.err.println(
				"tried to call unimplemented method getNbhSharedSurfaceArea()");
		return 1;
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
		if (_nbhIdx>3){
			int[] cc = _currentCoord;
			/*
			 * change in r (-1 or 1)
			 */
			int dr = NBH_DIRECS[_nbhIdx][0];
			if (isOutside(new int[]{cc[0]+dr, -1, -1}, 0)==null){
				/*
				 * compute number of voxels (along azimuthal dimension) for this 
				 * and the neighboring row.
				 */
				int nt_cur=_resCalc[1][cc[0]].getNVoxel();
				int nt_nbh=_resCalc[1][cc[0] + dr].getNVoxel();
				/*
				 * compute the neighbor row length to current row length ratio 
				 * for the azimuthal dimension.
				 */
				double drt=(double)nt_nbh/nt_cur;
//									System.out.println(nt_cur+" "+nt_nbh+" "+drt);
//									System.out.println(cc[1]*drt+"  "+(cc[1]+1)*drt);
				/*
				 * Loop through all neighbors along the azimuthal dimension.
				 * Starting from the current azimuthal angle coordinate times the 
				 * length ratio and ending at the next polar angle coordinate
				 * times the length ratio.
				 */
				for (int t=(int)(cc[1]*drt);  t<(cc[1]+1)*drt; t++){
//					System.out.println((cc[1]*drt)+" "+((cc[1]+1)*drt)+" "+t);
					_subNbhSet.add(new int[]{
							cc[0]+dr, t, cc[2] + NBH_DIRECS[_nbhIdx][2]});
				}
			}
			/*
			* only change r coordinate if outside the grid along radial dimension.
			*/
			else {
				_subNbhSet.add(new int[]{cc[0]+dr, cc[1], cc[2]});
			}
		}
		/*
		 * add the relative position to current coord if moving along azimuthal
		 * or z dimension.
		 */
		else{ 
			_subNbhSet.add(Vector.add(
					Vector.copy(_currentCoord),NBH_DIRECS[_nbhIdx]));
		}
	}
	
	@Override
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.INTERNAL;
			if ( coord[0] >= _resCalc[0][0].getNVoxel() )
				return BoundarySide.CIRCUMFERENCE;
			return null;
		case 1:
			if ( coord[1] < 0 )
				return _radSize[1]==2*Math.PI ?
							BoundarySide.INTERNAL : BoundarySide.YMIN;
			if (isOutside(coord,0)!=null)  
				return BoundarySide.UNKNOWN;
			int nt=_resCalc[1][coord[0]].getNVoxel();
			if ( coord[1] >= nt)
				return _radSize[1]==2*Math.PI ?
							BoundarySide.INTERNAL : BoundarySide.YMAX;
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
	
	/**
	 * \brief Computes the number of rows in matrix i for resolution 1.
	 * 
	 * <p>This is the total length if transformed to a Cartesian coordinate
	 * system.</p>
	 * 
	 * @param i Matrix index.
	 * @return The number of rows for a given radius.
	 */
	private int nRows(int i)
	{
		return (int) _ires[1] * s(i);
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
