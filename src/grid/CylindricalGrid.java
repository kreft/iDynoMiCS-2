package grid;

import java.util.HashMap;

import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.BoundarySide;

/**
 * @author qwer
 *
 */
public class CylindricalGrid extends PolarGrid{
	
	public CylindricalGrid(int[] nVoxel, double[][] resolution)
	{
		super(nVoxel, resolution);
	}
	
	public CylindricalGrid(int[] nVoxel, double[] resolution)
	{
		super(nVoxel, resolution);
	}

	public CylindricalGrid(){this(new int[]{1,90,1},new double[][]{{1},{1},{1}});}
	
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
			PolarArray.applyToAll(_array.get(type), ()->{return initialValues;});
		else
		{
			double[][][] array = PolarArray.createCylinder(this._nVoxel[0],
											this._nVoxel[2], this._res[1][0], initialValues);
			this._array.put(type, array);
		}
	}

	@Override
	public double getVoxelVolume(int[] coord) {
//		return nt_rad*_res*_res*_res/(iresT*2);
		double res_r=_res[0][coord[0]], res_z=_res[2][coord[2]];
		return 1.0/2*res_r*res_z*(2*coord[0]+res_r)*getArcLength(coord[0]);
	}
	
	// returns the arc length of a grid element at radius r in radian (assuming constant resolution in t)
	// = t2-t1
	private double getArcLength(double r){
//		return nt_rad*_res/(iresT*2*coord[0]);
		return nt_rad/(_res[1][0]*(2*r+1));
	}

	@Override
	public int[] getCoords(double[] loc) {
		int[] coord = new int[3];
		double counter;
		for ( int dim = 0; dim < 3; dim+=2 )
		{
			counter = 0.0;
			countLoop: for ( int i = 0; i < this._nVoxel[dim]; i++ )
			{
				counter += this._res[dim][i];
				if ( counter >= loc[dim] )
				{
					coord[dim] = i;
					break countLoop;
				}
			}
		}
//		coord[1]=(int)(loc[1]*_res[1][0]*2*loc[0]/(nt_rad*_res));
		coord[1]=(int)(loc[1]*_res[1][0]*2*loc[0]/nt_rad);
		return coord;
//		return new int[]{
//				(int)(loc[0]/_res),
//				(int)(loc[1]*iresT*2*loc[0]/(nt_rad*_res)),
//				(int)(loc[2]/_res)};
	}
	
	public double[] getLocation(int[] coord, double[] inside){
		double r=0, z=0;
		for ( int dim = 0; dim < 3; dim++ ){
			for ( int i = 0; i < coord[0]; i++ ){
				r += this._res[0][i];
			}
			for ( int i = 0; i < coord[2]; i++ ){
				z += this._res[2][i];
			}
			r+=inside[0]*this._res[0][coord[0]];
			z+=inside[2]*this._res[2][coord[2]];
		}
//		double r=(coord[0]+inside[0])*_res[0][coord[0]];
		double t;
		if (r==0) {
			t = Math.min((coord[1]+inside[1])*(Math.PI/2),nt_rad);
		}else{
			double l=getArcLength(coord[0]);
			if (r>0) t = (coord[1]+inside[1])*l;
			else t = Math.abs((coord[1]+inside[1])*l)-Math.PI;
		}
//		double z=(coord[2]+inside[2])*_res[2][coord[2]];
//		System.out.println(coord[0]+"  "+coord[1]+"  "+coord[2]+" | "+r+"  "+t+"  "+z);
		return new double[]{r,t,z};
	}

	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord, new double[]{0d,0d,0d});
	}
	
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, new double[]{0.5,0.5,0.5});
	}

	@Override
	protected BoundarySide isOutside(int[] coord) {
		if ( coord[0] < 0 )
			return BoundarySide.INTERNAL;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.RMAX;
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
	
	public int length(){return (int)(_nVoxel[2]*_res[1][0]*_nVoxel[0]*_nVoxel[0]);}	


	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] cyclicTransform(int[] coord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNbhSharedSurfaceArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	// converts idx to rtz coordinates
	public int[] idx2coord(int idx, int[] coord) {
		if (coord==null) coord=new int[3];
		coord[2]=(int) Math.ceil(idx/(_res[1][0]*Math.pow(_nVoxel[0], 2)))-1;
		double idx_z=idx-(coord[2]*_res[1][0]*Math.pow(_nVoxel[0], 2));
		coord[0]=(int) Math.ceil(Math.pow(idx_z/_res[1][0],1.0/2))-1;
		coord[1]=(int) (idx_z - _res[1][0]*Math.pow(coord[0],2))-1;
		return coord;
	}
	
	@Override
	public int coord2idx(int[] coord){
		return (int)(coord[2]*_res[1][0]*_nVoxel[0]*_nVoxel[0]
				+(coord[1]+_res[1][0]*coord[0]*coord[0]+1));
	}

	@Override
	public void currentNbhIdxChanged() {
		if (isNbhIteratorValid()){
			_currentNeighbor=Vector.add(Vector.copy(_currentCoord),nbhs[nbhIdx]);
			if (nbhIdx>3){ // moving in r
				double t_scale=2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1);
//				double t_scale=((double)_currentCoord[1])/_currentCoord[0];
				if (!isMultNbh && (t_scale%1 > 0.5 || t_scale%1 == 0d)) {
					_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.ceil(t_scale);
					nbhIdx--;
					isMultNbh=true;
				} else {
					_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.floor(t_scale);
					isMultNbh=false;
				}
			}
		}
	}
}
