package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;

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
		int[] coord = new int[3];
		// determine r 
		double counter = 0.0;
		countLoop: for ( int i = 0; i < this._nVoxel[0]; i++ )
		{
			if ( counter >= loc[0] )
			{
				coord[0] = i;
				break countLoop;
			}
			counter += this._res[0][i];
		}
		// is in right array (octand 5-8)?
		boolean is_right = loc[2]>=Math.PI; 
		// does right array exist?
		boolean right_ex = _res[2][0] > 1;  
		// compute the octand the location is in
		int ioct=(int)((int)round10(loc[1]/(Math.PI/2))
				*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
		// transform p to octand 1
		double p = (is_right ? loc[2]-Math.PI : loc[2]);
		// transform t to octand 1
		double t = loc[1]%(Math.PI/2);
		// number of grid cells for current radius 
		int npt=npt(coord[0]);
		// determine p coordinate
		coord[1]=(int)round10(p/getArcLengthP(npt)-1);
		// determine t coordinate
		coord[2]=(int)round10(t/getArcLengthT(coord[1],npt));
		// transform p and t into original octand again
		if (is_right) {
			coord[2]=npt-coord[2];
			coord[1]=npt-coord[1]-1;
		}
		coord[1] += right_ex ? (ioct-1)/2*npt : (ioct-1)*npt;
		return coord;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#getLocation(int[], double[])
	 */
	public double[] getLocation(int[] coord, double[] inside){
		// number of grid cells for current radius 
		int npt=npt(coord[0]);
		// the t 'real' t-coordinate for each octand (concatenated vertical)
		int mod_t = coord[1]%npt;
		// does right array exist?
		boolean right_ex = _res[2][0] > 1;
		// is in right array (octand 5-8)?
		boolean is_right = coord[2] > mod_t;
		// compute the octand the location is in
		int ioct=(int)((coord[1]/npt)*(right_ex ? _res[2][0] : 1)+1
					+ (is_right ? 1 : 0)); 
		// transform to octand 1
		int p = (is_right ? npt-mod_t-1 : mod_t);
		int t = (is_right ? npt-coord[2] : coord[2]);
		// compute location inside octand 1
		double td = (t+inside[2])*getArcLengthT(p, npt);
		// 1-ti because p starts at positive z axis...
		double pd=(p+(1-inside[1]))*getArcLengthP(npt); 
		// transform to original octand again
		if (is_right) pd+=Math.PI;
		td += right_ex ? (ioct-1)/2*(Math.PI/2) : (ioct-1)*(Math.PI/2);
		return new double[]{coord[0]+inside[0],td,pd};
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

	/**
	 * @param c - a spherical (r,t,p)-coordinate
	 * @return - the number of cells in row c[1]
	 */
	private int nt(int[] c){return (c[2] > c[1] ? npt(c[0])-c[1] : c[1]+1);} 
	
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
	private double getArcLengthT(double p, double npt){
		return round10((Math.PI/2)/(p+1));
	}
	
	/**
	 * @param npt - width and height of each triangle
	 * @return - the arc length in p direction
	 */
	private double getArcLengthP(double npt){return round10((Math.PI/2)/npt);}
	
	/**
	 * @param x - any double
	 * @return - rounded value with 1e-10 precision
	 */
	private double round10(double x){return Math.round(x*1e10)*1e-10;}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#idx2coord(int, int[])
	 */
	@Override
	public int[] idx2coord(int idx, int[] coord) {
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
		int npt=npt(r);
		// number of cells in each octand
		int nco=(r+1)*(2*r+1); 
		// index of octand in r slice
		int ioct=(int)Math.ceil((double)idxr/nco); 
		// does right array exist?
		boolean right_ex = _res[2][0] > 1;
		// left or right triangle in quadrant matrix (p=0 | p=90)
		boolean is_right=right_ex ? ioct%_res[2][0]==0 : false;
		// index starting with 1 in each octand
		int idxo=(idxr-1)%nco+1; 
		// p-coordinate (row)
		int p = (int)Math.ceil(1.0/2*(Math.sqrt(8*idxo+1)-3));
		// p-coordinate (column)
		int t = idxo-snt(p-1)-1;
		// transform  p and t to given octand
		if (is_right) {
			p=npt-p-1;
			t=npt-t;  // npr=ntr+1
		}
		p += right_ex ? ((int)Math.ceil((double)ioct/_res[2][0])-1)*npt 
				: (ioct-1)*npt;
		
		if (coord==null) coord = new int[]{r,t,p};
		else {coord[0]=r; coord[1]=p; coord[2]=t;}
		return coord;
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void currentNbhIdxChanged() {
		if (isNbhIteratorValid()){
			if (_nbhIdx>3){ // moving in r
				double t_scale=(_currentCoord[1]+1)/(_currentCoord[0]+1);
				double p_scale=(_currentCoord[2]+1)/(_currentCoord[1]+1);
				if (!_isMultNbh && (t_scale%1 > 0.5 || t_scale%1 == 0d)) {
					_currentNeighbor[1]=_currentCoord[1]+_nbhs[_nbhIdx][1]*(int)Math.ceil(t_scale);
					_currentNeighbor[2]=_currentCoord[2]+_nbhs[_nbhIdx][2]*(int)Math.ceil(p_scale);
					_nbhIdx--;
					_isMultNbh=true;
				} else {
					_currentNeighbor[1]=_currentCoord[1]+_nbhs[_nbhIdx][1]*(int)Math.floor(t_scale);
					_currentNeighbor[2]=_currentCoord[2]+_nbhs[_nbhIdx][2]*(int)Math.floor(p_scale);
					_isMultNbh=false;
				}
			}else if (_nbhIdx<2){ // moving in p
				double p_scale=(_currentCoord[2]+1)/(_currentCoord[1]+1);
				if (!_isMultNbh && (p_scale%1 > 0.5 || p_scale%1 == 0d)) {
					_currentNeighbor[2]=_currentCoord[2]+_nbhs[_nbhIdx][2]*(int)Math.ceil(p_scale);
					_nbhIdx--;
					_isMultNbh=true; 
				} else {
					_currentNeighbor[2]=_currentCoord[2]+_nbhs[_nbhIdx][2]*(int)Math.ceil(p_scale);
					_isMultNbh=false;
				}
			}else{  // moving in t
				
			}
		}
//		System.out.println(Arrays.toString(_currentNeighbor));
	}

	/* (non-Javadoc)
	 * @see grid.PolarGrid#coord2idx(int[])
	 */
	@Override
	public int coord2idx(int[] coord) {
		return coord[2] > coord[1] ? sn(coord[0]-1)+snt(coord[1]%npt(coord[0])-1)+coord[2]+1 
				: sn(coord[0]-1)+snt(npt(coord[0])-coord[1]%npt(coord[0])-2)+nt(coord)-coord[2];
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
}
