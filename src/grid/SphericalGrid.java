package grid;

import java.util.HashMap;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * \brief A grid with a spherical (r,t,p) coordinate system.
 *  
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
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
	 * - _resCalc[0] is the radial angle and has length 1 (single value).
	 * - _resCalc[1] is the polar angle.
	 * - _resCalc[2] is the azimuthal angle.
	 * 
	 * - To keep the volume over the grid cells fairly constant for same 
	 * 		resolutions, some dependencies between the _nVoxels were implemented:
	 *  * The number of voxels along the polar dimension (np) 
	 *  	is dependent on the radius (r): np=ires[2]*s(r) with s(r)=2*r+1;
	 * 	* The number of voxels along the azimuthal dimension (nt) 
	 * 		is dependent on the polar angle (p).
	 *    This dependency is actually a sine with the domain scaled from [0,pi]
	 *    to [0,np(r)-1] and the co-domain scaled from [0,1] so that it peaks 
	 *    with a value of ires[1]*s(r)  	  
	 * 
	 * <p>For example, a sphere (-> ires[1]=4, ires[2]=2) with radius 2  
	 * 			and resolution 1 would have:
	 * 		_nVoxel = [ [[2]], [[2],[6]], [[4,4],[4,12,20,20,12,4]] ].
	 *		_res = ...
	 * </p> 
	 */
	protected ResCalc[][][] _resCalc;
	
	/**
	 * @param totalSize
	 * @param res
	 */
	public SphericalGrid(double[] totalSize, double[] res){
		super(totalSize);
		
		/*
		 * Set up sphere-specific members
		 */
		_radSize[2] = Math.toRadians(totalSize[2]%181);
		_ires[2] = PolarArray.ires(_radSize[2]); 

		/*
		 * Set up uniform resolution calculator array
		 */
		ResolutionCalculator r = new ResolutionCalculator();
		_resCalc = new UniformResolution[3][][];
		_resCalc[0] = new UniformResolution[1][1];
		_resCalc[0][0][0] = r.new UniformResolution();
		_resCalc[0][0][0].init(res[0], totalSize[0]);

		int nr = _resCalc[0][0][0].getNVoxel();
		_resCalc[1] = new UniformResolution[nr][1];
		_resCalc[2] = new UniformResolution[nr][];
		for (int i=0; i<nr; ++i){
			_resCalc[1][i][0] = r.new UniformResolution();
			_resCalc[1][i][0].init(res[2], nRows(i));
			int np = _resCalc[1][i][0].getNVoxel();
			_resCalc[2][i] = new UniformResolution[np];
			for (int j=0; j<np; ++j){
				_resCalc[2][i][j] = r.new UniformResolution();
				_resCalc[2][i][j].init(res[1], nCols(i,j));
			}
		}
	}
	
	public SphericalGrid(double[] totalSize, double res)
	{
		this(totalSize, Vector.vector(3, res));
	}
	
	/**
	 * Constructs a Grid with lengths (1,90,90) -- one grid cell
	 */
	public SphericalGrid()
	{
		this(new double[]{1, 90, 90}, 1.0);
	}
	
//	protected double[][] convertResolution(int[] nVoxel, double[] oldRes)
//	{
//		double [][] res = new double[3][0];
//		/*
//		 * The angular dimensions theta and TODO are set by
//		 * linearAlgebra.PolarArray, so we nVoxel here.
//		 */
//		res[0] = Vector.vector( nVoxel[0] , oldRes[0]);
//		/*
//		 * Just give res one value in the theta and TODO dimensions.
//		 */
//		for ( int i = 1; i < 3; i++ )
//			res[i] = Vector.vector( 1 , oldRes[i]);
//		return res;
//	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
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
			double[][][] array = PolarArray.createSphere(
					_resCalc, initialValues
			);
			
			this._array.put(type, array);
		}
	}
	
	/**
	 * \brief Computes the arc length along the azimuthal dimension of the 
	 * grid element at the given coordinate.
	 * 
	 * TODO: Assumes constant resolution at the moment?!
	 * 
	 * @param coord - A coordinate inside the grid.
	 * @return - The arc length along the azimuthal dimension of the grid 
	 * 				element at the given coordinate.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthT(int[] coord){
		int nk=_resCalc[2][coord[0]][coord[1]].getNVoxel();
		return _radSize[1]/nk;
	}
	
	/**
	 * \brief Computes the arc length along the polar dimension of the 
	 * grid element at the given coordinate.
	 * 
	 * TODO: Assumes constant resolution at the moment?!
	 * 
	 * @param coord - A coordinate inside the grid.
	 * @return - The arc length along the polar dimension of the grid element 
	 * 				at the given coordinate.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthP(int[] coord){
		int nj=_resCalc[1][coord[0]][0].getNVoxel();
		return _radSize[2]/nj;
	}
	
	@Override
	public int[] getCoords(double[] loc, double[] inside) {
		int[] coord = new int[3];
		/*
		 * determine i (like in cartesian grid)
		 */
		cartLoc2Coord(loc[0], _resCalc[0][0][0], 0, coord, inside);
		/*
		 * determine j
		 */
		polarLoc2Coord(loc[2], getArcLengthP(coord), 1, coord, inside);
		/*
		 * determine k
		 */
		polarLoc2Coord(loc[1], getArcLengthT(coord), 2, coord, inside);
		return coord;
	}
	
	@Override
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = new double[3];
		/*
		 * determine r
		 */
		cartCoord2Loc(coord[0], _resCalc[0][0][0], inside[0], 0, loc);
		/*
		 * determine t
		 */
		polarCoord2Loc(coord[2], getArcLengthT(coord), inside[2], 1, loc);
		/*
		 * determine p
		 */
		polarCoord2Loc(coord[1], getArcLengthP(coord), inside[1], 2, loc);
		
		return loc;
	}
	
	@Override
	public boolean isIteratorValid() {
		return _currentCoord[0] < _resCalc[0][0][0].getNVoxel();
	}
	
	@Override
	protected boolean iteratorExceeds(int axis) {
		switch(axis){
		case 0: return _currentCoord[0] >=  _resCalc[0][0][0].getNVoxel();
		case 1: return iteratorExceeds(0) ? true : _currentCoord[1] 
							>= _resCalc[1][_currentCoord[0]][0].getNVoxel();
		case 2: return (iteratorExceeds(0) || iteratorExceeds(1)) ? true 
				: _currentCoord[2] >= _resCalc[2][_currentCoord[0]]
											[_currentCoord[1]].getNVoxel();
		default: throw new RuntimeException("0 < axis <= 3 not satisfied");
		}
	}

	@Override
	//TODO: assumes constant resolution for each r at the moment?
	public void fillNbhSet() {
		int[] cc = _currentCoord;
//		System.out.println(Arrays.toString(cc));
		/*
		 * Moving along radial dimension.
		 */
		if (_nbhIdx>3){ 
			/*
			 * change in r (-1 or 1)
			 */
			int dr = NBH_DIRECS[_nbhIdx][0];
			if (isOutside(new int[]{cc[0]+dr,-1,-1},0)==null){
				/*
				 * compute number of voxels along azimuthal and polar dimension
				 * for this and the neighboring 'constant-radius-shell'.
				 */
				double np_cur = _resCalc[1][cc[0]][0].getNVoxel();
				double np_nbh = _resCalc[1][cc[0] + dr][0].getNVoxel();
				double nt_cur= _resCalc[2][cc[0]][cc[1]].getNVoxel();
				double nt_nbh;
				/*
				 * compute the neighbor nVoxel to current nVoxel ratio 
				 * for the polar dimension.
				 */
				double drt;
				double drp=np_nbh/np_cur;
				/*
				 * Loop through all neighbors along the polar dimension.
				 * Starting from the current polar angle coordinate times the 
				 * nVoxel ratio and ending at the next polar angle coordinate
				 * times the nVoxel ratio.
				 */
				for (int p=(int)(cc[1]*drp);  p<(cc[1]+1)*drp; p++){
					nt_nbh=_resCalc[2][cc[0] + dr][p].getNVoxel();
					/*
					 * compute the neighbor nVoxel to current nVoxel ratio 
					 * for the azimuthal dimension.
					 */
					drt=nt_nbh/nt_cur;
//					System.out.println(drt+" "+nt_nbh+" "+nt_cur);
					/*
					 * Loop through all neighbors along the azimuthal dimension.
					 */
					for (int t=(int)(cc[2]*drt);  t<(cc[2]+1)*drt; t++){
						_subNbhSet.add(new int[]{cc[0]+dr,p,t});
					}
				}
			}
			/*
			* only change r coordinate if outside the grid along radial dimension.
			*/
			else _subNbhSet.add(new int[]{cc[0] + dr,cc[1],cc[2]});
		}
		/*
		 * Moving along polar dimension.
		 */
		else if (_nbhIdx<2){ 
			/*
			 * change in p (-1 or 1)
			 */
			int dp = NBH_DIRECS[_nbhIdx][2];
//			System.out.println(dp);
			if (isOutside(new int[]{cc[0],cc[1]+dp,-1},1)==null){
				/*
				 * compute number of voxels (along azimuthal dimension) for this 
				 * and the neighboring row in the matrix with index cc[0].
				 */
				double nt_cur=_resCalc[2][cc[0]][cc[1]].getNVoxel();
				double nt_nbh=_resCalc[2][cc[0]][cc[1] + dp].getNVoxel();
				/*
				 * compute the neighbor row length to current row length ratio 
				 * for the azimuthal dimension.
				 */
				double drt=nt_nbh/nt_cur;
//				System.out.println(drt+" "+nt_nbh+" "+nt_cur);
				/*
				 * Loop through all neighbors along the azimuthal dimension.
				 */
				for (int t=(int)(cc[2]*drt);  t<(cc[2]+1)*drt; t++){
					_subNbhSet.add(new int[]{cc[0],cc[1]+dp,t});
				}
			}
			/*
			* only change p coordinate if outside the grid along polar dimension.
			*/
			else  _subNbhSet.add(new int[]{cc[0],cc[1]+dp,cc[2]});
		}
		/*
		 * add the relative position to current coord if moving along azimuthal
		 * dimension
		 */
		else{ 
			_subNbhSet.add(new int[]{
					cc[0]+NBH_DIRECS[_nbhIdx][0],
					cc[1]+NBH_DIRECS[_nbhIdx][2],
					cc[2]+NBH_DIRECS[_nbhIdx][1]});
		}
	}

	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method calcMinVoxVoxResSq()");
	}

	@Override
	public int[] cyclicTransform(int[] coord) {
		BoundarySide bs = isOutside(coord,0);
		if (bs==BoundarySide.CIRCUMFERENCE)
			coord[0] = coord[0]%(_resCalc[0][0][0].getNVoxel()-1);
		if (bs==BoundarySide.INTERNAL)
			coord[0] = _resCalc[0][0][0].getNVoxel()+coord[0];
		
		bs = isOutside(coord,1);
		if (bs!=null){
			int np=_resCalc[2][coord[0]][0].getNVoxel();;
			switch (bs){
			case YMAX: coord[1] = coord[1]%(np-1); break;
			case YMIN: coord[1] = np+coord[2]; break;
			case INTERNAL:
				coord[1] = coord[1]%np; 
				if (coord[1] < 0)	coord[1] += np;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		
		bs = isOutside(coord,2);
		if (bs!=null){
			int nt=_resCalc[2][coord[0]][coord[1]].getNVoxel();
			switch (bs){
			case YMAX: coord[2] = coord[2]%(nt-1); break;
			case YMIN: coord[2] = nt+coord[2]; break;
			case INTERNAL:
				coord[2] = coord[2]%nt; 
				if (coord[2] < 0) coord[2] += nt;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		return coord;
	}

	@Override
	public double getVoxelVolume(int[] coord) {
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1=getVoxelOrigin(coord);
		double[] loc2=getLocation(coord,VOXEL_All_ONE_HELPER);

		return ((loc1[0]*loc1[0]*loc1[0]-loc2[0]*loc2[0]*loc2[0])
					* (loc1[1]-loc2[1])
					* (Math.cos(loc1[2])-Math.cos(loc2[2]))
				)/3;
	}
	
	@Override
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		out[0] = (_resCalc[0][0][0].getNVoxel() > 1 );  
		out[1] = _radSize[1] > 0;
		out[2] = _radSize[2] > 0;
		return out;
	}

	@Override
	public int numSignificantAxes()
	{
		int out = 0;
		out += (_resCalc[0][0][0].getNVoxel() > 1 ) ? 1 : 0;
		out += _radSize[1] > 0 ? 1 : 0;
		out += _radSize[2] > 0 ? 1 : 0;
		return out;
	}

	@Override
	public double getNbhSharedSurfaceArea() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getNbhSharedSurfaceArea()");
		return 1;
	}

	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getCurrentNbhResSq()");
		return 1;
	}

	@Override
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.INTERNAL;
			if ( coord[0] >= _resCalc[0][0][0].getNVoxel() )
				return BoundarySide.CIRCUMFERENCE;
			break;
		case 1:
			if (isOutside(coord,0)!=null)  
				return BoundarySide.UNKNOWN;
			int np=_resCalc[1][coord[0]][0].getNVoxel();
			if ( coord[1] < 0 )
				return _radSize[2]==Math.PI ? BoundarySide.INTERNAL : BoundarySide.ZMIN;
			if ( coord[1] >= np )
				return _radSize[2]==Math.PI ? BoundarySide.INTERNAL : BoundarySide.ZMAX;
			break;
		case 2:
			if (isOutside(coord,0)!=null || isOutside(coord,1)!=null)  
				return BoundarySide.UNKNOWN;
			int nt=_resCalc[2][coord[0]][coord[1]].getNVoxel();
			if ( coord[2] < 0 )
				return _radSize[1]==2*Math.PI ? BoundarySide.INTERNAL : BoundarySide.YMIN;
			if ( coord[2] >= nt)
				return _radSize[1]==2*Math.PI ? BoundarySide.INTERNAL : BoundarySide.YMAX;
			break;
			default: throw new IllegalArgumentException("dim must be > 0 and < 3");
		}
		return null;
	}
	
	/**
	 * \brief computes the number of rows in matrix i for resolution 1.
	 * 
	 * This is the total length if transformed to a cartesian coordinate system.
	 * 
	 * @param i - matrix index
	 * @return - the number of rows for a given radius.
	 */
	private int nRows(int i) {
		return (int)_ires[2]*s(i);
	}
	
	/**
	 * \brief Computes the number of columns in matrix i, row j for resolution 1.
	 * 
	 * This is the total length if transformed to a cartesian coordinate system.
	 * 
	 * @param i - matrix index
	 * @param j - row index
	 * @return - the number of elements in row j
	 */
	private int nCols(int i, int j){
		double res = _resCalc[1][i][0].getResolutionSum(s(i))/s(i);
		double p_scale=(Math.PI/2)/(s(i)-0.5) * res;
		double nt=_ires[1]+(s(i)-1)*_ires[1]*Math.sin(j*p_scale)*res;
		return (int)Math.round(nt);
	}
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	public static final GridGetter standardGetter()
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(double[] totalLength, double resolution) 
			{
				return new SphericalGrid(totalLength,resolution);
			}
		};
	}
}
