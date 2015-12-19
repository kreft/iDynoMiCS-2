package grid;

import java.util.Arrays;
import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot;

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
	private double getArcLength(double r){
		return _nt_rad/(_res[1][0]*(2*r+1));
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCoords(double[])
	 */
	@Override
	public int[] getCoords(double[] loc) {
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
					break countLoop;
				}
				counter += this._res[dim][i];
			}
		}
		// determine t coordinate
//		coord[1]=(int)(loc[1]*_res[1][0]*2*loc[0]/(nt_rad*_res));
//		coord[1]=(int)(loc[1]*_res[1][0]*2*loc[0]/_nt_rad);
		coord[1]=(int)round10(loc[1]/getArcLength(coord[0]));
		return coord;
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
					inside[dim] = counter-loc[dim];
					break countLoop;
				}
				counter += this._res[dim][i];
			}
		}
		// determine t coordinate
		double t=(int)round10(loc[1]/getArcLength(coord[0]));
		coord[1]=(int)t;
		inside[1]=t-coord[1];
		return coord;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#getLocation(int[], double[])
	 */
	public double[] getLocation(int[] coord, double[] inside){
		// determine r and z location (like in cartesian grid)
		double r=0, z=0; 
		for ( int i = 0; i < coord[0]; i++ ){
			r += this._res[0][i];
		}
		for ( int i = 0; i < coord[2]; i++ ){
			z += this._res[2][i];
		}
		r+=inside[0]*this._res[0][coord[0]];
		z+=inside[2]*this._res[2][coord[2]];
		// determine t location (dependent on r)
		double t;
		if (r==0) {
			t = Math.min((coord[1]+inside[1])*(Math.PI/2),_nt_rad);
		}else{
			double l=getArcLength(coord[0]); // the length (degree) at r+1
			if (r>0) t = (coord[1]+inside[1])*l;
			//TODO: swap 180 degree if r<0 ?? or don't allow <0 ?  
			else t = Math.abs((coord[1]+inside[1])*l)-Math.PI; 
		}
		return new double[]{r,t,z};
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelOrigin(int[])
	 */
	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord, new double[]{0d,0d,0d});
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getVoxelCentre(int[])
	 */
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, new double[]{0.5,0.5,0.5});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isOutside(int[])
	 */
	@Override
	protected BoundarySide isOutside(int[] coord) {
		//TODO: check for not full circle
		if ( coord[0] < 0 )
			return BoundarySide.INTERNAL;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.CIRCUMFERENCE;
		if ( coord[1] < 0 )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
		if ( coord[1] >= _res[1][0]*(2*coord[0]-1) )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
		if ( coord[2] < 0 )
			return BoundarySide.ZMIN;
		if ( coord[2] >= this._nVoxel[2] )
			return BoundarySide.ZMAX;
		return null;
	}
	
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
		// TODO Auto-generated method stub
		return null;
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
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#idx2coord(int, int[])
	 */
	@Override
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

//	/* (non-Javadoc)
//	 * @see grid.PolarGrid#currentNbhIdxChanged()
//	 */
//	@Override
//	public void currentNbhIdxChanged() {
//		if (isNbhIteratorValid()){ // only if inside boundaries
//			// add the relative position to current index for constant r 
//			_currentNeighbor=Vector.add(
//					Vector.copy(_currentCoord),_nbhs[_nbhIdx]);
//			if (_nbhIdx>3){ // moving in r
//				// the scaling factor for r=r+-1
//				double t_scale=(2.0*_currentCoord[0]+3)/(2.0*_currentCoord[0]+1);
//				// if scaling has high or no error
//				// -> insert additional ceiled neighbor
//				System.out.println(t_scale);
//				if (!_isMultNbh && (t_scale%1 > 0.5 || t_scale%1 == 0d)) {
//					_currentNeighbor[1]=_currentCoord[1]
//							+ _nbhs[_nbhIdx][1]*(int)Math.ceil(t_scale);
//					_nbhIdx--;
//					_isMultNbh=true;
//				// else reset and insert default floored neighbor
//				} else {
//					_currentNeighbor[1]=_currentCoord[1]
//							+ _nbhs[_nbhIdx][1]*(int)Math.floor(t_scale);
//					_isMultNbh=false;
//				}
//			}
//		}
//	}
	
	/* (non-Javadoc)
	 * @see grid.PolarGrid#currentNbhIdxChanged()
	 */
	@Override
	public void currentNbhIdxChanged() {
		nbhq.clear();
		if (isNbhIteratorValid()){ // only if inside boundaries
			if (_nbhIdx>3){ // moving in r
				int[] cc=_currentCoord;
				double l=getArcLength(cc[0]+_nbhs[_nbhIdx][0]);
				if (l>0){ // only for positive r
					double t1 = getLocation(cc, new double[]{1d,0d,0d})[1];
					double t2 = getLocation(cc, new double[]{1d,1d,0d})[1];
					for (double t=t1; t<t2; t+=l){
//						double[] inside=new double[3];
						int[] c=getCoords(
								new double[]{cc[0]+_nbhs[_nbhIdx][0],t,
										cc[2]+_nbhs[_nbhIdx][2]}//,
//								inside
								);
//						System.out.println(t1+" "+t2+" "+l);
						System.out.println(Arrays.toString(new double[]{cc[0]+_nbhs[_nbhIdx][0],t,
										cc[2]+_nbhs[_nbhIdx][2]})+"  "+Arrays.toString(c)+"  "/*+Arrays.toString(inside)*/);
//						if (inside[1]<=0.5){
//							nbhq.add(c);
//						}
						nbhq.add(c);
					}
				}
			}else{ // add the relative position to current index for constant r 
				_currentNeighbor=Vector.add(
						Vector.copy(_currentCoord),_nbhs[_nbhIdx]);
				nbhq.add(_currentNeighbor);
			}
		}
	}
}
