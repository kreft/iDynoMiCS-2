package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * \brief A grid with a spherical (r,t,p) coordinate system.
 *  
 * TODO Rob [11Jan2016]: radial, theta, phi? Let's use this convention:
 * http://mathworld.wolfram.com/SphericalCoordinates.html
 *  
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 */
public class SphericalGrid extends PolarGrid
{
	protected double _np_rad;
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	public SphericalGrid(int[] nVoxel, double[][] resolution)
	{
		super(nVoxel, resolution);
		init();
	}
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public SphericalGrid(int[] nVoxel, double[] resolution)
	{
		super(nVoxel, resolution);
		init();
	}

	/**
	 * Constructs a Grid with lengths (1,90,90) -- one grid cell
	 */
	public SphericalGrid()
	{
		this(new int[]{1, 90, 90}, new double[][]{{1.0}, {1.0}, {1.0}});
	}
	
	/**
	 * Shared constructor commands. Initializes all members.
	 * 
	 */
	private void init()
	{
		// length in p in radians
		this._np_rad = Math.toRadians( _nVoxel[2] );
		// inner resolution, depending on length in r and p
		this._ires[2]=PolarArray.ires(_nVoxel[0], _np_rad, _res[2][0]);
		this._nVoxel[2] = _nVoxel[2]%181; // phi periodic in 1..180
	}
	
	protected double[][] convertResolution(int[] nVoxel, double[] oldRes)
	{
		double [][] res = new double[3][0];
		/*
		 * The angular dimensions theta and TODO are set by
		 * linearAlgebra.PolarArray, so we nVoxel here.
		 */
		res[0] = Vector.vector( nVoxel[0] , oldRes[0]);
		/*
		 * Just give res one value in the theta and TODO dimensions.
		 */
		for ( int i = 1; i < 3; i++ )
			res[i] = Vector.vector( 1 , oldRes[i]);
		return res;
	}
	
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
			int[] nt = new int[_nVoxel[0]];
			int[][] np = new int[_nVoxel[0]][];
			for (int r=0; r<nt.length; ++r){
				nt[r] = nRows(r);
				np[r] = new int[nt[r]];
				for (int t=0; t<nt[r]; ++t){
					np[r][t] = nCols(r,t);
				}
			}
			
			double[][][] array = PolarArray.createSphere(
					this._nVoxel[0], nt, np, initialValues
			);
			
			this._array.put(type, array);
		}
	}
	
	private double getArcLengthT(int i, int j){
		int nk=nCols(i, j);
		return _nt_rad/nk;
	}
	
	private double getArcLengthP(int i){
		int nj=nRows(i);
		return _np_rad/nj;
	}
	
	@Override
	public int[] getCoords(double[] loc, double[] inside) {
		int[] coord = new int[3];
		/*
		 * determine i (like in cartesian grid)
		 */
		cartLoc2Coord(loc[0], _nVoxel[0], _res[0], 0, coord, inside);
		/*
		 * determine j
		 */
		polarLoc2Coord(loc[2], getArcLengthP(coord[0]),
				1, coord, inside);
		/*
		 * determine k
		 */
		polarLoc2Coord(loc[1], getArcLengthT(coord[0], coord[1]),
				2, coord, inside);
		return coord;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#getLocation(int[], double[])
	 */
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = new double[3];
		
		/*
		 * determine r
		 */
		cartCoord2Loc(coord[0], _res[0], inside[0], 0, loc);
		/*
		 * determine t
		 */
		polarCoord2Loc(coord[2], getArcLengthT(coord[0], coord[1]), inside[2], 
				1, loc);
		/*
		 * determine p
		 */
		polarCoord2Loc(coord[1], getArcLengthP(coord[0]), inside[1], 2, loc);
		
		return loc;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#length()
	 */
	public int length(){return N(_nVoxel[0]-1);}
		

//	/**
//	 * works only for r<=96 
//	 * 
//	 * @param idx
//	 * @param coord
//	 * @return
//	 */
//	@Deprecated
//	public int[] idx2coord(int idx, int[] coord) {
//		//TODO: make more variables than x?
//		// idx=N(r-1) solved for r with mathematica
//		
//		double iresT=_ires[1],
//				iresP=_ires[2],
//				ipt=iresP*iresT,
//				iptsq=ipt*ipt;
//		
////		double x=Math.pow(-27*ipttr+432*iptsq*idx+3.4641
////				*Math.sqrt(-25*iptse-1944*iptpe*idx+15552*iptqu*idx*idx),1.0/3);
//		double x=3.0*Math.pow(-iptsq*(
//					-0.1283*Math.sqrt(idx)*Math.sqrt(
//							-1944.0*ipt+15552.0*idx-(25.0*iptsq)/idx)
//					+ipt-16.0*idx),1.0/3);
//		
////		double rd = (1.21338*ipt)/x + (0.120187*x)/ipt - 0.25;
//		double rd = (7*ipt)/(4*Math.pow(3,1.0/3)*x) + x/(ipt*4*Math.pow(3,2.0/3)) - 0.25;
//		System.out.println(rd);
//		int r=(int)rd;
//		// index starting with 1 in this r slice
//		int idxr=idx-N(r-1); 
//		// number of rows
//		int np=np(r);			
//		// number of elements in each triangle
//		int sn=sn(r);
//		// is p >= Pi/2 ? 			
//		boolean is_right = idxr>sn;
//		// index starting with 1 in each octand (reverse for right array)
//		int idxo=(idxr-1)%sn+1; 
//		int idxor=2*sn-idxr+1;
//		
//		if (coord==null) coord = new int[3];
//		// r-coordinate
//		coord[0]=r;
//		// p-coordinate (column)
//		if (is_right) coord[1]=np-(int)Math.ceil(1.0/2*(Math.sqrt(8*idxor/iresT+1)-3))-1;
//		else coord[1]=(int)Math.ceil(1.0/2*(Math.sqrt(8*idxo/iresT+1)-3));
//		
//		// t-coordinate (row)
//		int n_prev = n(r, coord[1]);
////		System.out.println(idx+"  "+n_prev+" "+PolarArray.nt(coord[0], coord[1], iresT, iresP));
//		coord[2] = idxo-n_prev-1;
////		System.out.println(idx+Arrays.toString(coord));
////		if (is_right) 
////			coord[2]=PolarArray.nt(coord[0], coord[1], iresT, iresP)-coord[2];
//		return coord;
//	}
//	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#iteratorExceeds(int)
	 */
	@Override
	protected boolean iteratorExceeds(int axis) {
		switch(axis){
		case 0: return _currentCoord[0] >=  this._nVoxel[0];
		case 1: return _currentCoord[1] 
							>= nRows(_currentCoord[0]);
		case 2: return _currentCoord[2] >= nCols(
				_currentCoord[0], _currentCoord[1]);
		default: throw new RuntimeException("0 < axis <= 3 not satisfied");
		}
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void fillNbhSet() {
		int[] cc = _currentCoord;
//		System.out.println(Arrays.toString(cc));
		if (_nbhIdx>3){ // moving in r
			int dr = _nbhs[_nbhIdx][0];
			if (cc[0] + dr >= 0){
				double np_cur = nRows(cc[0]);
				double np_nbh = nRows(cc[0] + dr);
				double nt_cur=nCols(cc[0], cc[1]);
				double nt_nbh;
				double drt;
				double drp=np_nbh/np_cur;
				for (int p=(int)(cc[1]*drp);  p<(cc[1]+1)*drp; p++){
					nt_nbh=nCols(cc[0] + dr, p);
					drt=nt_nbh/nt_cur;
//					System.out.println(drt+" "+nt_nbh+" "+nt_cur);
					for (int t=(int)(cc[2]*drt);  t<(cc[2]+1)*drt; t++){
						_subNbhSet.add(new int[]{cc[0]+dr,p,t});
					}
				}
			}else _subNbhSet.add(new int[]{-1,cc[1],cc[2]});
		}else if (_nbhIdx<2){ // moving in p
			int dp = _nbhs[_nbhIdx][2];
//			System.out.println(dp);
			if (cc[1] + dp >= 0){
				double nt_cur=nCols(cc[0], cc[1]);
				double nt_nbh=nCols(cc[0], cc[1]+dp);
				double drt=nt_nbh/nt_cur;
//				System.out.println(drt+" "+nt_nbh+" "+nt_cur);
				for (int t=(int)(cc[2]*drt);  t<(cc[2]+1)*drt; t++){
					_subNbhSet.add(new int[]{cc[0],cc[1]+dp,t});
				}
			}else  _subNbhSet.add(new int[]{cc[0],-1,cc[2]});
		}else{ // add the relative position to current index for constant r 
			_subNbhSet.add(new int[]{
					cc[0]+_nbhs[_nbhIdx][0],
					cc[1]+_nbhs[_nbhIdx][2],
					cc[2]+_nbhs[_nbhIdx][1]});
		}
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#coord2idx(int[])
	 */
	@Deprecated
	public int coord2idx(int[] coord) {
		int N_prev = N(coord[0]-1);
		int n_prev = n(coord[0],coord[1]);
//		System.out.println(N_prev+" "+n_prev);
		return N_prev + n_prev + coord[2] + 1;
	
//		
//		int s =s(coord[0]);
//		int sn=sn(coord[0]);
//		int N_prev = N(coord[0]-1);
//		int n_prev = n(coord[0],coord[1]);
//		return coord[1]<s ? // is left array? 
//				N_prev + n_prev + coord[2] + 1
//				: N_prev + sn + n_prev + coord[2] + 1;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#calcMinVoxVoxResSq()
	 */
	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		// not important atm
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#cyclicTransform(int[])
	 */
	@Override
	public int[] cyclicTransform(int[] coord) {
		BoundarySide bs = isOutside(coord,0);
		if (bs==BoundarySide.CIRCUMFERENCE)
			coord[0] = coord[0]%(_nVoxel[0]-1);
		if (bs==BoundarySide.INTERNAL)
			coord[0] = _nVoxel[0]+coord[0];
		
		bs = isOutside(coord,1);
		if (bs!=null){
			int np=nRows(coord[0]);
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
			int nt=nCols(coord[0], coord[1]);
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

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelVolume(int[])
	 */
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

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getNbhSharedSurfaceArea()
	 */
	@Override
	public double getNbhSharedSurfaceArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCurrentNbhResSq()
	 */
	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		// not important atm
		return 0;
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#isOutside(int[], int)
	 */
	@Override
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.INTERNAL;
			if ( coord[0] >= this._nVoxel[0] )
				return BoundarySide.CIRCUMFERENCE;
			break;
		case 1:
			int nt=nRows(coord[0]);
			if ( coord[1] < 0 )
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
			if ( coord[1] >= nt)
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
			break;
		case 2:
			int np=nCols(coord[0],coord[1]);
			if ( coord[2] < 0 )
				return _nVoxel[2]==180 ? BoundarySide.INTERNAL : BoundarySide.ZMIN;
			if ( coord[2] >= np )
				return _nVoxel[2]==180 ? BoundarySide.INTERNAL : BoundarySide.ZMAX;
			break;
			default: throw new IllegalArgumentException("dim must be > 0 and < 3");
		}
		return null;
	}
}
