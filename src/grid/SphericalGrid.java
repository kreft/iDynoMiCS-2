package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public class SphericalGrid extends CylindricalGrid{
	double np_rad, iresP;
	
	public SphericalGrid(int[] nVoxel, double resolution)
	{
		super(nVoxel, resolution);
		this.np_rad = nVoxel[2]*Math.PI/180;
		this.iresP=PolarArray.computeIRES(nVoxel[0], np_rad, resolution);
		this._nVoxel[2] = nVoxel[2]%181; // phi periodic in 1..180
		nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
	}
	
	public SphericalGrid(){this(Vector.vector(3, 1),1);}
	
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
					this.iresT, this.iresP, initialValues);
			this._array.put(type, array);
		}
	}
	
	@Override
	public double getVoxelVolume() {
		// TODO most probably not correct -> prove
		return nt_rad*_res*_res/(iresT*2)*np_rad*_res*_res/(iresP*2);
	}
	
	@Override
	public int[] getCoords(double[] loc) {
//		System.out.println(Arrays.toString(loc));
//		System.out.println((int)(loc[0]/_res)+"  "+
//				(int)(loc[1]*iresT*2*loc[0]/(nt_rad*_res))+"  "+
//				(int)(loc[2]*iresP*2*loc[0]/(np_rad*_res)));
//		System.out.println();
		return new int[]{
				(int)(loc[0]/_res),
				(int)(loc[1]*iresT*2*loc[0]/(nt_rad*_res)),
				(int)(loc[2]*iresP*2*loc[0]/(np_rad*_res))};
	}
	
	@Override
	public double[] getVoxelOrigin(int[] coords) {
		double r = coords[0];
		double p,t;
		
		int idx_ires=(int)(coords[1]/(r+1));
		int quadt = (int)(idx_ires%iresT), 
			quadp=(int)(idx_ires/iresT);
		
		if (r==0){ p = 0; t = 0;}
		else{
			if (quadp==1) {
				p=(r-(coords[1]%(r+1)))*(Math.PI/2/r);	
			}
			else p = (coords[1]%(r+1))*(Math.PI/2/r);	
//			p = (coords[1]%(r+1))*(Math.PI/2/r);	
		}
		if ((coords[1]%(r+1))==0) t = 0;
		else t = coords[2]*(Math.PI/(4*(coords[1]%(r+1))));
		
//		System.out.println(quadt+"  "+quadp);
		
//		t+=quadt*(nt_rad/4); 
//		p+=quadp*(np_rad/2); 
		t+=quadt*(Math.PI/2); 
		p+=quadp*(Math.PI/2); 
				
		return new double[]{r,t,p};
	}

//	public double[] getVoxelCentre(int[] coords)
//	{
//		int idx_ires=(int)(coords[1]/(coords[0]+1));
//		int quadt = (int)(idx_ires%iresT), 
//			quadp=(int)(idx_ires/iresT);
//		
//		double[] c=getVoxelOrigin(coords);
//		double r=c[0], t=c[1], p=c[2];
//		
//		if ((coords[1]%(r+1))!=0) t+=(Math.PI/(4*(coords[1]%(coords[0]+1))))/2;
//		if (r!=0) p+=(Math.PI/2/coords[0])/2;	
//		r+=0.5;
//		
//		return new double[]{r,t,p};
//	}
	
	public double[] getVoxelCentre(int[] coords)
	{
		int idx_ires=(int)(coords[1]/(coords[0]+1));
		int quadt = (int)(idx_ires%iresT), 
			quadp=(int)(idx_ires/iresT);
		
		double[] c=getVoxelOrigin(coords);
		double r=c[0], t=c[1], p=c[2];
		if (r==0){ 
			p = Math.PI/4; t = Math.PI/4;
//			t+=quadt*(nt_rad/4);
//			p+=quadp*(np_rad/2);
		}else {
//			p += 0.5*(Math.PI/2/coords[0]);	
			if (quadp==1) p=(coords[0]-(coords[1]%(coords[0]+1))+0.5)*(Math.PI/2/coords[0]);
			else p=((coords[1]%(coords[0]+1))+0.5)*(Math.PI/2/coords[0]);
			if ((coords[1]%(coords[0]+1))==0){
				t =0;
//				t+=quadt*(nt_rad/4);
//			}else t += 0.5*(Math.PI/(4*(coords[1]%(coords[0]+1))));
			}else t=(coords[2]+0.5)*(Math.PI/(4*(coords[1]%(coords[0]+1))));
		}
		r+=0.5;
		t+=quadt*(nt_rad/4); // nt_rad/4 = pi/2 TODO: check for nt!=360, np!=180
		p+=quadp*(np_rad/2); // np_rad/2 = pi/2
		
//		if (c[0]==0 || (coords[1]%(c[0]+1))==0) 
//			return new double[]{c[0]+0.5,c[1]+0.5*(Math.PI/2),c[2]+0.5*(Math.PI/2)};
//		return new double[]{c[0]+0.5,c[1]+0.5*(Math.PI/2/c[0]),c[2]+0.5*(Math.PI/(4*(coords[1]%(c[0]+1))))};
		return new double[]{r,t,p};
	}
	
	@Override
	protected BoundarySide isOutside(int[] coord) {
		if ( coord[0] < 0 )
			return BoundarySide.INTERNAL;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.CIRCUMFERENCE;
		if ( coord[1] < 0 )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
		if ( coord[1] >= iresT*(2*coord[0]-1) )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
		if ( coord[2] < 0 )
			return _nVoxel[2]==360 ? BoundarySide.INTERNAL : BoundarySide.ZMIN;
		if ( coord[2] >= this._nVoxel[2] )
			return _nVoxel[2]==360 ? BoundarySide.INTERNAL : BoundarySide.ZMAX;
		return null;
	}
	
	@Override
	public boolean isIteratorValid() {return idx<=length();}
	
	public void setCurrent(int[] new_current){
		_currentCoord=new_current;
		int i=_currentCoord[0], j=_currentCoord[1], k=_currentCoord[2];
		idx=(int)(1.0/6*i*(i+1)*(2*i+1)*8 + (k+(j%(i+1))*(j%(i+1))+(i+1)*(i+1)*(j/(i+1))))+1;
//		System.out.println(idx);
//		System.out.println(new_current[0]+"  "+new_current[1]+"  "+new_current[2]+"  "+idx);
	}
	
	@Override
	public int[] iteratorNext() {
		idx++;
		//TODO: eventually change matrix layout to avoid this formula (change p and t? concatenate 'ires blocks' horizontal?)..
		//TODO: variables to speed computation up a bit..
		//TODO: compute over integers, not reals, should simplify a lot.. 
		int r = (int) (1.0/2*((iresP*iresT)/(Math.pow(3, 1.0/3)*Math.pow(108*iresP*iresP*iresT*iresT*idx
				+ Math.sqrt(3)*Math.sqrt(3888*Math.pow(iresP, 4)*Math.pow(iresT, 4)*idx*idx
				- Math.pow(iresP, 6)*Math.pow(iresT,6)),1.0/3))
				+ Math.pow(108*iresP*iresP*iresT*iresT*idx+Math.sqrt(3)*Math.sqrt(3888*Math.pow(iresP, 4)*Math.pow(iresT, 4)*idx*idx 
				- Math.pow(iresP, 6)*Math.pow(iresT, 6)),1.0/3)/(Math.pow(3, 2.0/3)*iresP*iresT))-1.0/2); 
		double idx_tp2 = idx-1.0/6*r*(r+1)*(2*r+1)*iresT*iresP;
		int t=(int) (Math.ceil(Math.pow((idx_tp2-1)%((r+1)*(r+1))+1,1.0/2))+(r+1)*(Math.ceil((idx_tp2)/((r+1)*(r+1)))-1))-1;
		int p=(int) (((idx_tp2-1)%((r+1)*(r+1))+1) - Math.pow(t%(r+1),2))-1;
//		p = -(8*Math.pow(r,3))/3-4*r*r-j mod (i+1)^2-1/3 i (3 j+4)-j+x-1;
		_currentCoord[0]=r; _currentCoord[1]=t; _currentCoord[2]=p;
		return _currentCoord;
	}
	
	public int[] nbhIteratorNext(){
		nbhIdx++;
		if (isNbhIteratorValid()){
			_currentNeighbor=Vector.add(Vector.copy(_currentCoord),nbhs[nbhIdx]);
			if (nbhIdx>3){ // moving in r
				System.out.println(2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1));
				_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.round(2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1));
				_currentNeighbor[2]=_currentCoord[2]+nbhs[nbhIdx][1]*(int)Math.round(2*(_currentCoord[2]+1)/(2.0*_currentCoord[0]+1));
			}
		}
		return _currentNeighbor;
	}
	
	public int length(){
		int nr=_nVoxel[0];
		return (int)(1.0/6*nr*(nr+1)*(2*nr+1)*iresT*iresP);
	}
}
