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
//		System.out.println(Arrays.toString(loc));
//		System.out.println((int)(loc[0]/_res)+"  "+
//				(int)(loc[1]*_res[1][0]*2*loc[0]/(nt_rad*_res))+"  "+
//				(int)(loc[2]*_res[2][0]*2*loc[0]/(np_rad*_res)));
//		System.out.println();
//		return new int[]{
//				(int)(loc[0]/_res),
//				(int)(loc[1]*_res[1][0]*2*loc[0]/(nt_rad*_res)),
//				(int)(loc[2]*_res[2][0]*2*loc[0]/(np_rad*_res))};
		return null;
	}
	
	@Override
	public double[] getVoxelOrigin(int[] coords) {
		int r=coords[0],t=coords[1],p=coords[2];
		int ntr=nt(r);
		boolean right_ex = _res[2][0] > 1;
		boolean is_right = p > t%ntr;
		int ioct=(int)((t/ntr)*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
//		System.out.println(t+"  "+ioct);
		t = is_right ? ntr-t%ntr-1 : t%ntr;
		p = is_right ? ntr-p : p;
//		System.out.println(Arrays.toString(coords));
//		System.out.println("t: "+t+", p:"+p);
//		System.out.println();
		double pd, td;
		if (r==0){ pd = is_right ? Math.PI : 0; td = 0;}
		else{
			if (is_right) td = (ntr-t-1)*(nt_rad/(_res[1][0]*2*r));
			else td = t*(nt_rad/(_res[1][0]*2*r));
			if (t == 0) pd=is_right ? Math.PI : 0;
			else pd = p*(Math.PI/2/t);
		}
		if (is_right) td+=Math.PI/2;
		pd += right_ex ? (ioct-1)/2*(Math.PI/2) : ioct*(Math.PI/2);
				
		return new double[]{r,td,pd};
	}
	
	public double[] getVoxelCentre(int[] coords)
	{
		int r=coords[0],t=coords[1],p=coords[2];
		int ntr=nt(r);
		boolean right_ex = _res[2][0] > 1;
		boolean is_right = p > t%ntr;
		int ioct=(int)((t/ntr)*(right_ex ? _res[2][0] : 1)+1+(is_right ? 1 : 0)); 
//		System.out.println(t+"  "+ioct);
		t = is_right ? ntr-t%ntr-1 : t%ntr;
		p = is_right ? ntr-p : p;
//		System.out.println(Arrays.toString(coords));
//		System.out.println("t: "+t+", p:"+p);
//		System.out.println();
		double[] o=getVoxelOrigin(coords);
		double rd=o[0]+0.5, td=o[1], pd=o[2];
		if (r==0){ pd = Math.PI/4; td = Math.PI/4;}
		else{
//			if (is_right) td -= 0.5*(nt_rad/(_res[1][0]*2*r));
//			else td += 0.5*(nt_rad/(_res[1][0]*2*r));
//			if (t == 0) pd=Math.PI/4;
//			else pd += 0.5*(Math.PI/2/t);
//			td += 0.5*(nt_rad/(_res[1][0]*2*r));
//			if (t == 0) pd=Math.PI/4;
//			else pd += 0.5*(Math.PI/2/t);
		}
//		if (is_right) td+=Math.PI/2;
//		pd += right_ex ? (ioct-1)/2*(Math.PI/2) : ioct*(Math.PI/2);
//		r+=0.5;		
		
		return new double[]{rd,td,pd};
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

	@Override
	public void currentIdxChanged() {
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
		_currentCoord[0]=r;
		_currentCoord[1]=t;
		_currentCoord[2]=p;
	}

	@Override
	public void currentNbhIdxChanged() {
		//TODO: adjust for sphere (copied from CylindricalGrid)
		if (isNbhIteratorValid()){
			_currentNeighbor=Vector.add(Vector.copy(_currentCoord),nbhs[nbhIdx]);
			if (nbhIdx>3){ // moving in r
				double t_scale=2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1);
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

	@Override
	public int coord2idx(int[] coord) {
		return coord[2] > coord[1] ? sn(coord[0]-1)+snp(coord[1]%nt(coord[0])-1)+coord[2]+1 
				: sn(coord[0]-1)+snp(nt(coord[0])-coord[1]%nt(coord[0])-2)+np(coord)-coord[2];
	}

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
		return 0;
	}
	
//	private double c2deg(int[] coords){
//		int r1=coords[0]+1;
//		int w = 
//		int t2 = (int)(-Math.acos((2*Math.PI*(1 + 3*(-1 + r1)*r1)*w 
//				 + (2*Math.sqrt(-(Math.pow(1 + 3*(-1 + r1)*r1,2)*w*w*(Math.PI*Math.PI
//				 - 8*Math.pow(1 + 3*(-1 + r1)*r1,2)*w*w 
//				 + 8*Math.pow(1 + 3*(-1 + r1)*r1,2)*w*w*Math.cos(w))*Math.sin(w)*Math.sin(w))))
//				 / (-1 + Math.cos(w))) / (8*Math.pow(1 + 3*(-1 + r1)*r1,2)*w*w)));
//	}
}
