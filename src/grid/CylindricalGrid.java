package grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.jlibsedml.validation.ISedMLValidator;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 *
 * A grid with polar (r,t) coordinates and a cartesian z coordinate. 
 */
public class CylindricalGrid extends PolarGrid{
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	public CylindricalGrid(int[] nVoxel, double[][] resolution)
	{
		super(nVoxel, resolution);
	}
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public CylindricalGrid(int[] nVoxel, double[] resolution)
	{
		super(nVoxel, resolution);
	}

	/**
	 * Constructs a Grid with lengths (1,90,1) -- one grid cell
	 */
	public CylindricalGrid(){
		this(new int[]{1,90,1},new double[][]{{1},{1},{1}});
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
			double[][][] array = PolarArray.createCylinder(
					this._nVoxel[0],
					this._nVoxel[2], 
					this._res[1][0], 
					initialValues
				);
			this._array.put(type, array);
		}
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelVolume(int[])
	 */
	@Override
	public double getVoxelVolume(int[] coord) {
		double res_r=_res[0][coord[0]], res_z=_res[2][coord[2]];
		// let A(r) be the area enclosed by a polar curve r=r(t):
		// A(r)= 1/2 \int_t1^t2 r^2 dt
		// then the voxel volume is \int_z^{z+res_z} A(r+res_r) - A(r) dz, or:
		return 1.0/2*res_r*res_z*(2*coord[0]+res_r)*getArcLength(coord[0]);
	}
	
	/**
	 * returns the arc length of a grid element at radius r in radians
	 * (assuming constant resolution in t)
	 * 
	 * @param r - the radius.
	 * @return - the arc length of the grid elements at r+1.
	 */
	private double getArcLength(int r){
		// r-coordinate to t coord in spherical grid
		int rs=PolarArray.s(r)-1;
		// number of elements in row r
		int nt = PolarArray.nt(	_nVoxel[0]-1, rs, _res[1][0], 1);
		return _nt_rad/nt;
	}
	
	public int[] getCoords(double[] loc, double[] inside) {
		int[] coord = new int[3];
		// determine r and z coordinate
		double counter;
		for ( int dim = 0; dim < 3; dim+=2 )
		{
			counter = 0.0;
			countLoop: for ( int i = 0; i < this._nVoxel[dim]; i++ )
			{
				if ( counter >= loc[dim] )
				{
					coord[dim] = i;
					if (inside!=null) inside[dim] = counter-loc[dim];
					break countLoop;
				}
				counter += this._res[dim][i];
			}
		}
		// determine t coordinate
		double t=loc[1]/getArcLength(coord[0]);
		coord[1]=(int)(t);
		if (inside!=null) inside[1]=Math.abs(t-coord[1]);
		return coord;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#getLocation(int[], double[])
	 */
	public double[] getLocation(int[] coord, double[] inside){
		double ri, ti, pi;
		if (inside==null) {ri=0; ti=0; pi=0;}
		else {ri=inside[0]; ti=inside[1]; pi=inside[2];}
		
		// determine r and z location (like in cartesian grid)
		double r=0, z=0; 
		for ( int i = 0; i < coord[0]; i++ ){
			r += this._res[0][i];
		}
		for ( int i = 0; i < coord[2]; i++ ){
			z += this._res[2][i];
		}
		r+=ri*this._res[0][coord[0]];
		z+=pi*this._res[2][coord[2]];
		// determine t location (dependent on r)
		double t;
		if (r==0) {
			t = Math.min((coord[1]+ti)*(Math.PI/2),_nt_rad);
		}else{
			double l=getArcLength(coord[0]); // the length (degree) at r+1
			if (r>0) t = (coord[1]+ti)*l;
			//TODO: swap 180 degree if r<0 ?? or don't allow <0 ?  
			else t = Math.abs((coord[1]+ti)*l)-Math.PI; 
		}
		return new double[]{r,t,z};
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelOrigin(int[])
	 */
	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord, null);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelCentre(int[])
	 */
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, new double[]{0.5,0.5,0.5});
	}
	
//	/**
//	 * no longer maintained and may not work
//	 * 
//	 * @param type
//	 * @return
//	 */
//	@Deprecated
//	public CartesianGrid toCartesianGrid(ArrayType type){
//		CartesianGrid grid = new CartesianGrid(
//				new int[]{2*_nVoxel[0],2*_nVoxel[0],_nVoxel[2]}, _res);
//		grid.newArray(type);
//		grid.setAllTo(type, Double.NaN);
//		this.resetIterator();
//		int[] next=_currentCoord;
//		do{
//			double[] loc_p=this.getVoxelCentre(next);
//			int[] ar = new int[]{
//					(int)(Math.ceil((loc_p[0])*Math.sin(loc_p[1])/_res)+_nVoxel[0]-1),
//					(int)(Math.ceil((loc_p[0])*Math.cos(loc_p[1])/_res)+_nVoxel[0]-1),
//					next[2]
//			};
//			double val=grid.getValueAt(type, ar);
//			if (Double.isNaN(val)) 
//				grid.setValueAt(type, ar, 0);
//			else
//				grid.setValueAt(type, ar, val+1);
//			next=this.iteratorNext();
//		}while(this.isIteratorValid());
//		return grid;
//	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#length()
	 */
	public int length(){
		return (int)(_nVoxel[2]*_res[1][0]*_nVoxel[0]*_nVoxel[0]);
	}	

	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		
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
		
		bs = isOutside(coord,2);
		if (bs==BoundarySide.ZMAX)
			coord[2] = coord[2]%(_nVoxel[2]-1);
		if (bs==BoundarySide.ZMIN)
			coord[2] = _nVoxel[2]+coord[2];
		
		bs = isOutside(coord,1);
		if (bs!=null){
			int nt=PolarArray.nt(
					_nVoxel[0], PolarArray.s(coord[0])-1, _res[1][0], _res[2][0]);
			switch (bs){
			case YMAX: coord[1] = coord[1]%(nt-1); break;
			case YMIN: coord[1] = nt+coord[2]; break;
			case INTERNAL:
				coord[1] = coord[1]%nt; 
				if (coord[1] < 0) coord[1] += nt;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		return coord;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getNbhSharedSurfaceArea()
	 */
	@Override
	public double getNbhSharedSurfaceArea() {
//		if (_nbhIdx>3){ // moving in r
//			
//		}else return 1;
		return 0;
	}

//	/* (non-Javadoc)
//	 * @see grid.SpatialGrid#getNbhSharedSurfaceArea()
//	 */
//	@Override
//	public double getNbhSharedSurfaceArea() {
////		double sA=0, t1_nbh, t2_nbh;
////		boolean is_right, is_left, is_inBetween;
////		t1_nbh = t-inside[1]*len_nbh;
////		t2_nbh = t+(1-inside[1])*len_nbh;
////		
////		// t1 of nbh <= t1 of cc (right counter-clockwise)
////		if (dr < 0){
////			is_right = t1_nbh <= t1_cur;
////			is_left = t2_nbh >= t2_cur;
////			is_inBetween = is_left && is_right;
////			len_s = len_cur;
////		}else{
////			is_right = t1_nbh < t1_cur;
////			is_left = t2_nbh > t2_cur;
////			is_inBetween = !(is_left || is_right);
////			len_s = len_nbh;
////		}
////		
////		if (is_inBetween) sA = 1;
////		else if (is_right) sA = (t2_nbh-t1_cur)/len_s;
////		else sA = (t2_cur-t1_nbh)/len_s; // is_left
////		
////		return sA;
//	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCurrentNbhResSq()
	 */
	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	/**
	 * deprecated because it causes problem in SphericalGrid and may 
	 * fail in CylindricalGrid for very large indices as well
	 * 
	 * @param idx - an index
	 * @param coord - a array to write coord into (optional)
	 * @return - a (r,p,t) coordinate
	 */
	@Deprecated
	public int[] idx2coord(int idx, int[] coord) {
		if (coord==null) coord=new int[3];
		// determine z coordinate
		coord[2]=(int) Math.ceil(idx/(_res[1][0]*Math.pow(_nVoxel[0], 2)))-1;
		// 'reset' iterator to 1 in current z array 
		double idx_z=idx-(coord[2]*_res[1][0]*Math.pow(_nVoxel[0], 2));
		// determine r coordinate
		coord[0]=(int) Math.ceil(Math.pow(idx_z/_res[1][0],1.0/2))-1;
		// determine t coordinate
		coord[1]=(int) (idx_z - _res[1][0]*Math.pow(coord[0],2))-1;
		return coord;
	}
	
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#coord2idx(int[])
	 */
	@Override
	public int coord2idx(int[] coord){
		return (int)(coord[2]*_res[1][0]*_nVoxel[0]*_nVoxel[0]
				+(coord[1]+_res[1][0]*coord[0]*coord[0]+1));
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#iteratorExceeds(int)
	 */
	protected boolean iteratorExceeds(int axis) {
		switch(axis){
		case 0: case 2: return _currentCoord[axis] >=  this._nVoxel[axis];
		case 1: return _currentCoord[axis] >= PolarArray.nt(
				_nVoxel[0], PolarArray.s(_currentCoord[0])-1, _res[1][0], _res[2][0]);
		default: throw new RuntimeException("0 < axis <= 3 not satisfied");
		}
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void fillNbhSet() {
		if (_nbhIdx>3){ // moving in r
			int[] cc = _currentCoord;
			int dr = _nbhs[_nbhIdx][0];
			if (cc[0] + dr >= 0){
				// _nVoxel[0] isntead of _nVoxel[0] - 1 to allow neighbors outside
				double nt_cur=PolarArray.nt(
						_nVoxel[0], PolarArray.s(cc[0])-1, _res[1][0], _res[2][0]);
				double nt_nbh=PolarArray.nt(
						_nVoxel[0], PolarArray.s(cc[0]+dr)-1, _res[1][0], _res[2][0]);
				double drt=nt_nbh/nt_cur;
//									System.out.println(nt_cur+" "+nt_nbh+" "+drt);
//									System.out.println(cc[1]*drt+"  "+(cc[1]+1)*drt);
				for (int t=(int)(cc[1]*drt);  t<(cc[1]+1)*drt; t++){
					System.out.println((cc[1]*drt)+" "+((cc[1]+1)*drt)+" "+t);
					_subNbhSet.add(new int[]{cc[0]+dr,t,cc[2]+_nbhs[_nbhIdx][2]});
				}
			}else _subNbhSet.add(new int[]{-1,cc[1],cc[2]});
		}else{ // add the relative position to current index for constant r 
			_subNbhSet.add(Vector.add(
					Vector.copy(_currentCoord),_nbhs[_nbhIdx]));
		}
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#isOutside(int[], int)
	 */
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.INTERNAL;
			if ( coord[0] >= this._nVoxel[0] )
				return BoundarySide.CIRCUMFERENCE;
			return null;
		case 1:
			if ( coord[1] < 0 )
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
			int nt=PolarArray.nt(
					_nVoxel[0]-1, PolarArray.s(coord[0])-1, _res[1][0], _res[2][0]);
			if ( coord[1] >= nt)
				return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
			return null;
		case 2:
			if ( coord[2] < 0 )
				return BoundarySide.ZMIN;
			if ( coord[2] >= this._nVoxel[2] )
				return BoundarySide.ZMAX;
			return null;
		default: throw new IllegalArgumentException("dim must be > 0 and < 3");
		}
	}
}
