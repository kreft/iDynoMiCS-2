package grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 *
 *  A grid with a spherical (r,t,p) coordinate system.
 */
public class SphericalGrid extends PolarGrid{
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
	public SphericalGrid(){this(new int[]{1,90,90},new double[][]{{1},{1},{1}});}
	
	/**
	 * Shared constructor commands. Initializes all members.
	 * 
	 */
	private void init(){
		// length in p in radians
		this._np_rad = _nVoxel[2]*Math.PI/180;
		// inner resolution, depending on length in r and p
		this._res[2][0]=PolarArray.computeIRES(_nVoxel[0], _np_rad);
		this._nVoxel[2] = _nVoxel[2]%181; // phi periodic in 1..180
		 // neighbours
		_nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#newArray(grid.SpatialGrid.ArrayType, double)
	 */
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
			double[][][] array = PolarArray.createSphere(
					this._nVoxel[0],
					this._res[1][0], 
					this._res[2][0], 
					initialValues
			);
			this._array.put(type, array);
		}
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCoords(double[])
	 */
	@Override
	public int[] getCoords(double[] loc) {
		double r=loc[0], t=loc[1], p=loc[2];
		int[] coord = new int[3];
		// determine r (like in cartesian grid)
		double counter = 0.0;
		countLoop: for ( int i = 0; i < _nVoxel[0]; i++ )
		{
			if ( counter >=  r)
			{
				coord[0] = i;
				break countLoop;
			}
			counter += _res[0][i];
		}
		
		// determine p coordinate
		int np=PolarArray.np(coord[0],_res[2][0]);
		double lp=_np_rad/np;
		coord[1]=(int)(p/lp-1);
		
		// determine t coordinate
		int nt=PolarArray.nt(coord[0], coord[1], _res[1][0], _res[2][0]);
		double lt=_nt_rad/nt;
		coord[2]=(int)(t/lt);
		return coord;
	}
	
	@Override
	@Deprecated
	public int[] getCoords(double[] loc, double[] inside) {
		//TODO: implement
		return null;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#getLocation(int[], double[])
	 */
	public double[] getLocation(int[] coord, double[] inside){
		
		double length_p=PolarArray.np(coord[0],_res[2][0]);
		double length_t=PolarArray.nt(coord[0], coord[1], _res[1][0], _res[2][0]);
		
		// determine r (like in cartesian grid)
		double r=0;
		for ( int i = 0; i < coord[0]; i++ ){
			r += _res[0][i];
		}
		return new double[]{
			(r+inside[0])*_res[0][coord[0]],
			(coord[2]+inside[2])*(_nt_rad/length_t),
			(coord[1]+(1-inside[1]))*(_np_rad/length_p)
		}; 
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelOrigin(int[])
	 */
	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord,null);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelCentre(int[])
	 */
	public double[] getVoxelCentre(int[] coord){
		return getLocation(coord,new double[]{0.5,0.5,0.5});
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#length()
	 */
	public int length(){return PolarArray.N(_nVoxel[0]-1,_res[1][0],_res[2][0]);}

//	/* (non-Javadoc)
//	 * @see grid.PolarGrid#idx2coord(int, int[])
//	 */
//	@Override
//	public int[] idx2coord(int idx, int[] coord) {
//		//TODO: make more variables than x?
//		// idx=sn(r) solved for r with mathematica
//		double x = Math.pow(-27*Math.pow(_res[2][0],3)*Math.pow(_res[1][0],3)
//				+ 432*Math.pow(_res[2][0],2)*Math.pow(_res[1][0],2)*idx
//				+ 2*Math.sqrt(3)*Math.sqrt(-25*Math.pow(_res[2][0],6)
//				* Math.pow(_res[1][0],6)
//				- 1944*Math.pow(_res[2][0],5)*Math.pow(_res[1][0],5)*idx
//				+ 15552*Math.pow(_res[2][0],4)
//				* Math.pow(_res[1][0],4)*idx*idx),1.0/3);
//		int r = (int)(
//					(7*_res[2][0]*_res[1][0]) / (4*Math.pow(3,1.0/3) * x) + x
//					/ (4*Math.pow(3,2.0/3)*_res[2][0]*_res[1][0]) - 1.0/4);
//		// index starting with 1 in this r slice
//		int idxr=idx-sn(r-1); 
//		// width and height of each triangle
//		int npt=npt(r);
//		// number of cells in each octand
//		int nco=(r+1)*(2*r+1); 
//		// index of octand in r slice
//		int ioct=(int)Math.ceil((double)idxr/nco); 
//		// does right array exist?
//		boolean right_ex = _res[2][0] > 1;
//		// left or right triangle in quadrant matrix (p=0 | p=90)
//		boolean is_right=right_ex ? ioct%_res[2][0]==0 : false;
//		// index starting with 1 in each octand
//		int idxo=(idxr-1)%nco+1; 
//		// p-coordinate (row)
//		int p = (int)Math.ceil(1.0/2*(Math.sqrt(8*idxo+1)-3));
//		// p-coordinate (column)
//		int t = idxo-snt(p-1)-1;
//		// transform  p and t to given octand
//		if (is_right) {
//			p=npt-p-1;
//			t=npt-t;  // npr=ntr+1
//		}
//		p += right_ex ? ((int)Math.ceil((double)ioct/_res[2][0])-1)*npt 
//				: (ioct-1)*npt;
//		
//		if (coord==null) coord = new int[]{r,t,p};
//		else {coord[0]=r; coord[1]=p; coord[2]=t;}
//		return coord;
//	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#idx2coord(int, int[])
	 */
	@Override
	public int[] idx2coord(int idx, int[] coord) {
		//TODO: make more variables than x?
		// idx=sn(r) solved for r with mathematica
		double iresT=_res[1][0];
		double iresP=_res[2][0];
		double x = Math.pow(-27*Math.pow(iresP,3)*Math.pow(iresT,3)
				+ 432*Math.pow(iresP,2)*Math.pow(iresT,2)*idx
				+ 2*Math.sqrt(3)*Math.sqrt(-25*Math.pow(iresP,6)
				* Math.pow(iresT,6)
				- 1944*Math.pow(iresP,5)*Math.pow(iresT,5)*idx
				+ 15552*Math.pow(iresP,4)
				* Math.pow(iresT,4)*idx*idx),1.0/3);
		int r = (int)(
					(7*iresP*iresT) / (4*Math.pow(3,1.0/3) * x) + x
					/ (4*Math.pow(3,2.0/3)*iresP*iresT) - 1.0/4);
		int s = PolarArray.s(r);
		// index starting with 1 in this r slice
		int idxr=idx-PolarArray.N(r-1,iresT,iresP); 
		// number of rows
		int np=PolarArray.np(r,iresP);			
		// number of elements in each triangle
		int sn=PolarArray.sn(r,iresT);
		// is p >= Pi/2 ? 			
		boolean is_right = idxr>sn;
		// index starting with 1 in each octand (reverse for right array)
		int idxo=(idxr-1)%sn+1; 
		int idxor=2*sn-idxr+1;
		
		if (coord==null) coord = new int[3];
		// r-coordinate
		coord[0]=r;
		// p-coordinate (column)
		IntFunction<Integer> rsup = new IntFunction<Integer>() {
			@Override
			public Integer apply(int idxo) {
				return (int)Math.ceil(1.0/2*(Math.sqrt(8*idxo/iresT+1)-3));
			}
		};

		if (is_right) coord[1]=np-rsup.apply(idxor)-1;
		else coord[1]=rsup.apply(idxo);
		// t-coordinate (row)
		int n_prev = PolarArray.n(r, coord[1], iresT, iresP);
		coord[2] = idxo-n_prev-1;
//		if (is_right) 
//			coord[2]=PolarArray.nt(coord[0], coord[1], iresT, iresP)-coord[2];
		return coord;
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void currentNbhIdxChanged() {
//		_nbhSet.clear();
//		if (isNbhIteratorValid()){ // only if inside boundaries
//			if (_nbhIdx>3){ // moving in r
//				int[] cc=_currentCoord;
//				if (nbh[0]>=0){ // only positive r			
//					double t1 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,0d})[1];
//					double t2 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,1d})[1];
//					double p1 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],1d,0d})[2];
//					double p2 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,0d})[2];
//					System.out.println(t1+" "+t2+" "+p1+" "+p2);
//					for (double t=t1; t<t2; t+=stats.lt){
//						for (double p=p1; p<p2; p+=stats.lp){
////							double[] inside=new double[3];
//							int[] c=getCoords(
//									new double[]{nbh[0],t,p}//,
////									inside
//									);
//							//						System.out.println(t1+" "+t2+" "+l);
//							System.out.println(Arrays.toString(new double[]{nbh[0],t,
//									p})+"  "+Arrays.toString(c)+"  "/*+Arrays.toString(inside)*/);
////							if (inside[1]<=0.5){
////								nbhq.add(c);
////							}
//							_nbhSet.add(c);
//						}
//					}
//				}
////			}else if (_nbhIdx<2){ // moving in p
////								
//			}else{ // moving in t  
//				_currentNeighbor=Vector.add(
//						Vector.copy(_currentCoord),_nbhs[_nbhIdx]);
//				_nbhSet.add(_currentNeighbor);
//			}
//		}
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#coord2idx(int[])
	 */
	@Override
	public int coord2idx(int[] coord) {
		double iresT=_res[1][0],iresP=_res[2][0];
		int s =PolarArray.s(coord[0]);
		int sn=PolarArray.sn(coord[0], iresT);
		int N_prev = PolarArray.N(coord[0]-1,iresT, iresP);
		int n_prev = PolarArray.n(coord[0],coord[1],iresT, iresP);
		return coord[1]<s ? // is left array? 
				N_prev + n_prev + coord[2] + 1
				: N_prev + sn + n_prev + coord[2] + 1;
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
		double[] loc = new double[]{1d,1d,1d};
		loc = getLocation(coord, loc);
		loc[0] = coord[0]%_nVoxel[0];
		loc[1] = loc[1]%_nt_rad;
		loc[2] = loc[2]%_np_rad;
		coord = getCoords(loc);
		return coord;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelVolume(int[])
	 */
	@Override
	public double getVoxelVolume(int[] coord) {
		// TODO Auto-generated method stub
		return 0;
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
			int nt=PolarArray.nt(
					coord[0], coord[1], _res[1][0], _res[2][0]);
			if ( coord[2] < 0 )
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
			if ( coord[2] >= nt)
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
			break;
		case 2:
			int np=PolarArray.np(coord[0], _res[2][0]);
			if ( coord[1] < 0 )
				return _nVoxel[1]==180 ? BoundarySide.INTERNAL : BoundarySide.ZMIN;
			if ( coord[1] >= np )
				return _nVoxel[1]==180 ? BoundarySide.INTERNAL : BoundarySide.ZMAX;
			break;
			default: throw new IllegalArgumentException("dim must be > 0 and < 3");
		}
		return null;
	}
}
