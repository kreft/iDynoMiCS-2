package grid;

import java.util.Arrays;
import java.util.HashMap;

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
		return (int[])(new Stats(loc).transToRightOct(null));
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
		return (double[])(new Stats(coord,inside).transToRightOct(null));
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelOrigin(int[])
	 */
	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord,new double[]{0d,0d,0d});
	}
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelCentre(int[])
	 */
	public double[] getVoxelCentre(int[] coord){
		return getLocation(coord,new double[]{0.5,0.5,0.5});
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isOutside(int[])
	 */
	@Override
	protected BoundarySide isOutside(int[] coord) {
		if ( coord[0] < 0 )
			return BoundarySide.INTERNAL;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.CIRCUMFERENCE;
		if ( coord[1] < 0 )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
		if ( coord[1] >= _res[1][0]*(2*coord[0]-1) )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
		if ( coord[2] < 0 )
			return _nVoxel[2]==360 ? BoundarySide.INTERNAL : BoundarySide.ZMIN;
		if ( coord[2] >= this._nVoxel[2] )
			return _nVoxel[2]==360 ? BoundarySide.INTERNAL : BoundarySide.ZMAX;
		return null;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#length()
	 */
	public int length(){return sn(_nVoxel[0]-1);}
	
	
	/*************************************************************************
	 * PRIVATE UTILITY METHODS
	 ************************************************************************/
	
	
	/**
	 * width and height of each triangle 
	 * (number of cells in the orthogonal directions of the p-t plane)
	 * @param r - radius
	 * @return -  width and height of each triangle 
	 */
	private int npt(int r){return 2*r+1;}
	
	private int no(int r){return npt(r)*(1+r);}

	/**
	 * @param p - phi coordinate (row index)
	 * @return - number of cells in an octant until and including row p (for left triangle)
	 */
	private int snt(int p){return (int)(1.0/2*(p+1)*(p+2));} 
	
	/**
	 * @param r - radius
	 * @return - the number of grid cells until and including matrix r
	 */
	private int sn(int r){
		return (int)(1.0/6*_res[1][0]*_res[2][0]*(r+1)*(r+2)*(4*r+3));
	}
	
	/**
	 * @param p - p coordinate 
	 * @param npt - width and height of each triangle
	 * @return - the arc length in t direction
	 */
	private double getArcLengthT(int p, double npt){
		return round10((Math.PI/2)/(p+1));
	}
	
	/**
	 * @param npt - width and height of each triangle
	 * @return - the arc length in p direction
	 */
	private double getArcLengthP(double npt){return round10((Math.PI/2)/npt);}

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
		return (int[])(new Stats(idx).transToRightOct(coord));
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void currentNbhIdxChanged() {
		nbhq.clear();
		if (isNbhIteratorValid()){ // only if inside boundaries
			if (_nbhIdx>3){ // moving in r
				int[] cc=_currentCoord;
				int[] nbh=Vector.add(Vector.copy(cc),_nbhs[_nbhIdx]);
				if (nbh[0]>=0){ // only positive r
					Stats stats = new Stats(nbh,new double[]{0d,0d,0d});
					System.out.println(stats.lp+" "+stats.lt+"  "+stats.mod_t+"  "+stats.npt);
				
					double t1 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,0d})[1];
					double t2 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,1d})[1];
					double p1 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],1d,0d})[2];
					double p2 = getLocation(cc, new double[]{_nbhs[_nbhIdx][0],0d,0d})[2];
					System.out.println(t1+" "+t2+" "+p1+" "+p2);
					for (double t=t1; t<t2; t+=stats.lt){
						for (double p=p1; p<p2; p+=stats.lp){
//							double[] inside=new double[3];
							int[] c=getCoords(
									new double[]{nbh[0],t,p}//,
//									inside
									);
							//						System.out.println(t1+" "+t2+" "+l);
							System.out.println(Arrays.toString(new double[]{nbh[0],t,
									p})+"  "+Arrays.toString(c)+"  "/*+Arrays.toString(inside)*/);
//							if (inside[1]<=0.5){
//								nbhq.add(c);
//							}
							nbhq.add(c);
						}
					}
				}
//			}else if (_nbhIdx<2){ // moving in p
//								
			}else{ // moving in t  
				_currentNeighbor=Vector.add(
						Vector.copy(_currentCoord),_nbhs[_nbhIdx]);
				nbhq.add(_currentNeighbor);
			}
		}
	}

//	/* (non-Javadoc)
//	 * @see grid.PolarGrid#coord2idx(int[])
//	 */
//	@Override
//	public int coord2idx(int[] coord) {
//		return coord[2] > coord[1] ? 
//				sn(coord[0]-1)+snt(coord[1]%npt(coord[0])-1)+coord[2]+1 
//				: sn(coord[0]-1)+snt(npt(coord[0])-coord[1]%npt(coord[0])-2) 
//					+ nt(coord)-coord[2];
//	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#coord2idx(int[])
	 */
	@Override
	public int coord2idx(int[] coord) {
		int npt=npt(coord[0]);
		int p_mod_npt=coord[1]%npt;
		int sn_prev = sn(coord[0]-1);
		int no = no(coord[0]);
		int idxq = coord[2] > p_mod_npt ?  // right array
				sn_prev+no+snt(npt-p_mod_npt-2)+npt-coord[2]+1
				: sn_prev+snt(p_mod_npt-1)+coord[2]+1;
		return idxq+2*no*(coord[1]/npt);
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
	
	/**
	 * Transforms a coordinate to location and vice versa.
	 * Transforms both to octand 1. 
	 * Stores or computes all informations to restore original state.
	 * 
	 * CARE: its (r,t,p) in space and (r,p,t) in array at the moment!
	 * // TODO: switch p and t cooordinate in array 
	 */
	private class Stats{
		private int npt, mod_t, ioct;
		private double r, p, t, lt, lp;  
		private boolean right_ex, is_right, returns_coord;
		
		private Stats(int r, int p, int t, double ir, double ip, double it){
			// flag indicating that this Stat is returning 
			// true:  an array-coordinate
			// false: a location
			returns_coord=false;  
			// number of grid cells for current radius 
			npt=npt(r);
			// the 'real' t-coordinate for each octand (concatenated vertical)
			mod_t = p%npt;
			// does right array exist?
			right_ex = _res[2][0] > 1;
			// is in right array (octand 5-8)?
			is_right = t > mod_t;
			// compute the octand the location is in
			ioct=(int)((p/npt)*(right_ex ? _res[2][0] : 1)+1
					+ (is_right ? 1 : 0)); 
			// transform to octand 1
			this.p = (is_right ? npt-mod_t-1 : mod_t);
			this.t = (is_right ? npt-t : t);

			lp=getArcLengthP(npt);
			lt=getArcLengthT((int) this.p,npt);
			
			// determine r (like in cartesian grid)
			for ( int i = 0; i < r; i++ ){
				this.r += _res[0][i];
			}
			this.r+=ir*_res[0][r];
			// compute location inside octand 1
			this.t = (this.t+it)*lt;
			// 1-ti because p starts at positive z axis...
			this.p=(this.p+(1-ip))*lp; 
		}
		
		private Stats(int[] coord, double[] inside){
			this(coord[0],coord[1],coord[2],inside[0],inside[1],inside[2]);
		}
		
		private Stats(double r, double t, double p){
			// flag indicating that this Stat is computed for location, not coord
			returns_coord=true;
			// determine r (like in cartesian grid)
			double counter = 0.0;
			countLoop: for ( int i = 0; i < _nVoxel[0]; i++ )
			{
				if ( counter >= r )
				{
					this.r = i;
					break countLoop;
				}
				counter += _res[0][i];
			}
			// is in right array (octand 5-8)?
			is_right = p>=Math.PI; 
			// does right array exist?
			right_ex = _res[2][0] > 1;  
			// compute the octand the location is in
			ioct=(int)((int)round10(t/(Math.PI/2))
					*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
			// transform p to octand 1
			this.p = (is_right ? p-Math.PI : p);
			// transform t to octand 1
			this.t = t%(Math.PI/2);
			// number of grid cells for current radius 
			npt=npt((int)this.r);
			// determine p coordinate
			lp=getArcLengthP(npt);
			this.p=(int)round10(this.p/lp-1);
			// determine t coordinate
			lt=getArcLengthT((int)this.p, npt);
			this.t=(int)round10(this.t/lt);
		}
		
		private Stats(double[] loc){
			this(loc[0],loc[1],loc[2]);
		}
		
		private Stats(int idx){
			returns_coord=true;
			//TODO: make more variables than x?
			// idx=sn(r) solved for r with mathematica
			double x = Math.pow(-27*Math.pow(_res[2][0],3)*Math.pow(_res[1][0],3)
					+ 432*Math.pow(_res[2][0],2)*Math.pow(_res[1][0],2)*idx
					+ 2*Math.sqrt(3)*Math.sqrt(-25*Math.pow(_res[2][0],6)
					* Math.pow(_res[1][0],6)
					- 1944*Math.pow(_res[2][0],5)*Math.pow(_res[1][0],5)*idx
					+ 15552*Math.pow(_res[2][0],4)
					* Math.pow(_res[1][0],4)*idx*idx),1.0/3);
			int r = (int)(
						(7*_res[2][0]*_res[1][0]) / (4*Math.pow(3,1.0/3) * x) + x
						/ (4*Math.pow(3,2.0/3)*_res[2][0]*_res[1][0]) - 1.0/4);
			// index starting with 1 in this r slice
			int idxr=idx-sn(r-1); 
			// width and height of each triangle
			npt=npt(r);
			// number of cells in each octand
			int nco=no(r); 
			// index of octand in r slice
			ioct=(int)Math.ceil((double)idxr/nco); 
			// does right array exist?
			right_ex = _res[2][0] > 1;
			// left or right triangle in quadrant matrix (p=0 | p=90)
			is_right=right_ex ? ioct%_res[2][0]==0 : false;
			// index starting with 1 in each octand
			int idxo=(idxr-1)%nco+1; 
			// radius
			this.r = r;
			// p-coordinate (row)
			p = (int)Math.ceil(1.0/2*(Math.sqrt(_res[1][0]*_res[2][0]*idxo+1)-3));
			// t-coordinate (column)
			t = idxo-snt((int)p-1)-1;
		}
		
		private Object transToRightOct(Object o){
			// transform to original octand again
			//TODO: maybe add some !instanceof -> error
			if (returns_coord){
				if (is_right) {
					t=npt-t;
					p=npt-p-1;
				}
				p += right_ex ? (ioct-1)/2*npt : (ioct-1)*npt;
				if (o==null) return new int[]{(int)r,(int)p,(int)t};
				else {
					int[] d = (int[])o;
					d[0]=(int)r;d[1]=(int)p;d[2]=(int)t;
					return d;
				}
			}else{
				if (is_right) p+=Math.PI;
				t += right_ex ? (ioct-1)/2*(Math.PI/2) : (ioct-1)*(Math.PI/2);
				if (o==null) return new double[]{r,t,p};
				else {
					double[] d = (double[])o;
					d[0]=r;d[1]=t;d[2]=p;
					return d;
				}
			}
			
		}
	}
}
