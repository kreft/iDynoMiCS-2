package grid;

import java.util.Arrays;
import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public class SphericalGrid extends PolarGrid{
	double np_rad;
	
	public SphericalGrid(int[] nVoxel, double[][] resolution)
	{
		super(nVoxel, resolution);
		init();
	}
	
	public SphericalGrid(int[] nVoxel, double[] resolution)
	{
		super(nVoxel, resolution);
		init();
	}

	public SphericalGrid(){this(new int[]{1,90,90},new double[][]{{1},{1},{1}});}
	
	private void init(){
		this.np_rad = _nVoxel[2]*Math.PI/180;
		this._res[2][0]=PolarArray.computeIRES(_nVoxel[0], np_rad);
		this._nVoxel[2] = _nVoxel[2]%181; // phi periodic in 1..180
		nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
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
			PolarArray.applyToAll(_array.get(type), ()->{return initialValues;});
		else
		{
			double[][][] array = PolarArray.createSphere(this._nVoxel[0],
					this._res[1][0], this._res[2][0], initialValues);
			this._array.put(type, array);
		}
	}
	
	@Override
	public int[] getCoords(double[] loc) {
		// TODO: implement
		int[] coord = new int[3];
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
		boolean is_right = loc[2]>=Math.PI;
		boolean right_ex = _res[2][0] > 1;
		int ioct=(int)((int)round10(loc[1]/(Math.PI/2))*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
//		System.out.println((loc[1]/(Math.PI/2)));
		double p = (is_right ? loc[2]-Math.PI : loc[2]);
		double t = loc[1]%(Math.PI/2);
		int ntr=nt(coord[0]);
		coord[1]=(int)round10(p/getArcLengthP(ntr)-1);
		coord[2]=(int)round10(t/getArcLengthT(coord[1],ntr));
		System.out.println(t+" "+getArcLengthT(coord[1],ntr));
		if (is_right) {
			coord[2]=ntr-coord[2];
			coord[1]=ntr-coord[1]-1;
		}
		coord[1] += right_ex ? (ioct-1)/2*ntr : (ioct-1)*ntr;
		return coord;
	}
	
	public double[] getLocation(int[] coord, double[] inside){
		int ntr=nt(coord[0]);
		int mod_t = coord[1]%ntr;
		boolean right_ex = _res[2][0] > 1;
		boolean is_right = coord[2] > mod_t;
		int ioct=(int)((coord[1]/ntr)*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
		int p = (is_right ? ntr-mod_t-1 : mod_t);
		int t = (is_right ? ntr-coord[2] : coord[2]);
		double td = (t+inside[2])*getArcLengthT(p, ntr);
		double pd=(p+(1-inside[1]))*getArcLengthP(ntr); // 1-ti because p starts at positive z axis...
		if (is_right) pd+=Math.PI;
		td += right_ex ? (ioct-1)/2*(Math.PI/2) : (ioct-1)*(Math.PI/2);
//		System.out.println(Arrays.toString(new double[]{coord[0],td,pd}));
		return new double[]{coord[0]+inside[0],td,pd};
	}
	
	@Override
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord,new double[]{0d,0d,0d});
	}
	public double[] getVoxelCentre(int[] coord){
		return getLocation(coord,new double[]{0.5,0.5,0.5});
	}
	
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
	
	public int length(){return sn(_nVoxel[0]-1);}
	
	// utility functions 
	// width and height of each triangle (number of cells in the orthogonal directions of the p-t plane)
	private int nt(int r){return 2*r+1;}
	// number of cells in row coords[1]
	private int np(int[] c){return (c[2] > c[1] ? nt(c[0])-c[1] : c[1]+1);} 
	// number of cells in an octant until and including row t (for left triangle)
	private int snp(int t){return (int)(1.0/2*(t+1)*(t+2));} 
	// number of cells until and including matrix r
	private int sn(int r){return (int)(1.0/6*_res[1][0]*_res[2][0]*(r+1)*(r+2)*(4*r+3));}
	//
//	private double getArcLength(double r){return Math.pow(0.8498*r,-0.6696);}
//	//	
//	private double[] scaleArcLength(double l, double t, double ti, double ntr, double[] l_out){
//		if (l_out==null) l_out=new double[2]; 
//		double ls=Math.PI/2/l;
////		double sp=(0.153247*(1+ntr)*(-1+ntr)*ntr)/Math.pow(1+coord[0],837.0/625)
//		double sp;
//		// currently i don't know why we need -1 if origin should be located, it should be ntr too, but then there is a gap near 0 in p!
//		if (ti==0) sp=ls/(ntr-1); 
//		else if(ti==0.5) sp=ls/ntr;  // works for inside = 0.5 (center), not tested for other values
//		else sp=ls/(ntr-1);
////		sp=ls/(ntr+2*ti-1); 
//		double st=ls/(t+1);
//		l_out[0]=l*st;
//		l_out[1]=l*sp;
//		return l_out;
//	}
	
	private double getArcLengthT(double p, double ntr){return round10((Math.PI/2)/(p+1));}
	private double getArcLengthP(double ntr){return round10((Math.PI/2)/ntr);}
	private double round10(double x){return Math.round(x*1e10)*1e-10;}

	@Override
	public int[] idx2coord(int idx, int[] coord) {
		if (coord==null) coord=new int[3];
		//TODO: make variables for r to speed computation up
		int r = (int)((7*_res[2][0]*_res[1][0])/(4*Math.pow(3,1.0/3)*Math.pow(-27*Math.pow(_res[2][0],3)*Math.pow(_res[1][0],3)
				+ 432*Math.pow(_res[2][0],2)*Math.pow(_res[1][0],2)*idx
				+ 2*Math.sqrt(3)*Math.sqrt(-25*Math.pow(_res[2][0],6)*Math.pow(_res[1][0],6)
						- 1944*Math.pow(_res[2][0],5)*Math.pow(_res[1][0],5)*idx
						+ 15552*Math.pow(_res[2][0],4)*Math.pow(_res[1][0],4)*idx*idx),1.0/3))
				+ Math.pow(-27*Math.pow(_res[2][0],3)*Math.pow(_res[1][0],3)+432*Math.pow(_res[2][0],2)*Math.pow(_res[1][0],2)*idx
						+ 2*Math.sqrt(3)*Math.sqrt(-25*Math.pow(_res[2][0],6)*Math.pow(_res[1][0],6)
								- 1944*Math.pow(_res[2][0],5)*Math.pow(_res[1][0],5)*idx
								+ 15552*Math.pow(_res[2][0],4)*Math.pow(_res[1][0],4)*idx*idx),1.0/3)/(4*Math.pow(3,2.0/3)*_res[2][0]*_res[1][0])-1.0/4);
		int idxr=idx-sn(r-1); // index starting with 1 in this r slice
		int ntr=nt(r);
		int nco=(r+1)*(2*r+1); // number of cells in each octand
		int ioct=(int)Math.ceil((double)idxr/nco); // index of octand in r slice
		boolean right_ex = _res[2][0] > 1;
		boolean is_right=right_ex ? ioct%_res[2][0]==0 : false; // left or right triangle in quadrant matrix (p=0 | p=90)
		int idxo=(idxr-1)%nco+1; // index starting with 1 in each octand
		int t = (int)Math.ceil(1.0/2*(Math.sqrt(8*idxo+1)-3));
		int p = idxo-snp(t-1)-1;
		if (is_right) {
			t=ntr-t-1;
			p=ntr-p;  // npr=ntr+1
		}
		t += right_ex ? ((int)Math.ceil((double)ioct/_res[2][0])-1)*ntr : (ioct-1)*ntr;
		coord[0]=r;
		coord[1]=t;
		coord[2]=p;
		return coord;
	}

	@Override
	public void currentNbhIdxChanged() {
		if (isNbhIteratorValid()){
			if (nbhIdx>3){ // moving in r
				double t_scale=(_currentCoord[1]+1)/(_currentCoord[0]+1);
				double p_scale=(_currentCoord[2]+1)/(_currentCoord[1]+1);
				if (!isMultNbh && (t_scale%1 > 0.5 || t_scale%1 == 0d)) {
					_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.ceil(t_scale);
					_currentNeighbor[2]=_currentCoord[2]+nbhs[nbhIdx][2]*(int)Math.ceil(p_scale);
					nbhIdx--;
					isMultNbh=true;
				} else {
					_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.floor(t_scale);
					_currentNeighbor[2]=_currentCoord[2]+nbhs[nbhIdx][2]*(int)Math.floor(p_scale);
					isMultNbh=false;
				}
			}else if (nbhIdx<2){ // moving in p
				double p_scale=(_currentCoord[2]+1)/(_currentCoord[1]+1);
				if (!isMultNbh && (p_scale%1 > 0.5 || p_scale%1 == 0d)) {
					_currentNeighbor[2]=_currentCoord[2]+nbhs[nbhIdx][2]*(int)Math.ceil(p_scale);
					nbhIdx--;
					isMultNbh=true; 
				} else {
					_currentNeighbor[2]=_currentCoord[2]+nbhs[nbhIdx][2]*(int)Math.ceil(p_scale);
					isMultNbh=false;
				}
			}else{  // moving in t
				
			}
		}
//		System.out.println(Arrays.toString(_currentNeighbor));
	}

	@Override
	public int coord2idx(int[] coord) {
		return coord[2] > coord[1] ? sn(coord[0]-1)+snp(coord[1]%nt(coord[0])-1)+coord[2]+1 
				: sn(coord[0]-1)+snp(nt(coord[0])-coord[1]%nt(coord[0])-2)+np(coord)-coord[2];
	}

	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		// not important atm
	}

	@Override
	public int[] cyclicTransform(int[] coord) {
		double[] loc = new double[]{1d,1d,1d};
		loc = getLocation(coord, loc);
		loc[0] = coord[0]%_nVoxel[0];
		loc[1] = loc[1]%nt_rad;
		loc[2] = loc[2]%np_rad;
		coord = getCoords(loc);
		return coord;
	}

	@Override
	public double getVoxelVolume(int[] coord) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNbhSharedSurfaceArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		// not important atm
		return 0;
	}
}
