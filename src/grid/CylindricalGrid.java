package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public class CylindricalGrid extends CartesianGrid{
	final int ires;
	int nbhIdx, idx;
	final double[][] nbhs;
	double nt_rad;
	
	public CylindricalGrid(int[] nVoxel, double resolution)
	{
		nVoxel[1] = nVoxel[1]%361; // theta periodic in 1..360
		this._nVoxel = Vector.copy(nVoxel);  // [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		this._nVoxel[0]=this._nVoxel[0];
		this._res = resolution;				 // scales r & ires TODO: change 1/res in code, not here
		this.nt_rad = nVoxel[1]*Math.PI/180;
		this.ires=PolarArray.computeIRES(nVoxel[0], nt_rad, resolution);
		nbhs=new double[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
		resetIterator();
		resetNbhIterator(false);
	}

	public CylindricalGrid(){this(Vector.vector(3, 1),1);}
	
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
			double[][][] array = PolarArray.create(this._nVoxel[0], this.nt_rad,
											this._nVoxel[2], this.ires, initialValues);
			this._array.put(type, array);
		}
	}

	@Override
	public double getVoxelVolume() {
		// TODO Auto-generated method stub
		return nt_rad*_res*_res*_res/(ires*2);
	}

	@Override
	public int[] getCoords(double[] loc) {
		return new int[]{
				(int)(loc[0]/_res),
				(int)(loc[1]*ires*2*loc[0]/(nt_rad*_res)),
				(int)(loc[2]/_res)};
	}

	@Override
	public double[] getVoxelOrigin(int[] coords) {
		double r=coords[0]*_res;
		double t;
		if (r==0) t = Math.min(coords[1]*(Math.PI/2),nt_rad);
		else if (r>0) t = coords[1]*nt_rad*_res/(ires*2*r);
		else t = Math.abs(coords[1]*nt_rad*_res/(ires*2*r))-Math.PI;
		double z=coords[2]*_res;
//		System.out.println(coords[0]+"  "+coords[1]+"  "+coords[2]+" | "+r+"  "+t+"  "+z);
		return new double[]{r,t,z};
	}
	
	public double[] getVoxelCentre(int[] coords)
	{
		double r=(coords[0]+0.5)*_res;
		double t=(coords[1]+0.5)*nt_rad*_res/(ires*2*r);
		double z=(coords[2]+0.5)*_res;
		return new double[]{r,t,z};
	}

	@Override
	protected BoundarySide isOutside(int[] coord) {
		if ( coord[0] < 0 )
			return BoundarySide.INTERNAL;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.CIRCUMFERENCE;
		if ( coord[1] < 0 )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMIN;
		if ( coord[1] >= ires*(2*coord[0]-1) )
			return _nVoxel[1]==360 ? BoundarySide.INTERNAL : BoundarySide.YMAX;
		if ( coord[2] < 0 )
			return BoundarySide.ZMIN;
		if ( coord[2] >= this._nVoxel[2] )
			return BoundarySide.ZMAX;
		return null;
	}

	@Override
	public void setAllTo(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), ()->{return value;});
	}

	@Override
	public void addToAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v+value;});
	}

	@Override
	public void timesAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v*value;});
	}

	@Override
	public double getMax(ArrayType type) {
		final double[] max=new double[]{Double.NEGATIVE_INFINITY};
		PolarArray.applyToAll(_array.get(type),(double v)->{max[0]=v>max[0] ? v : max[0];});
		return max[0];
	}

	@Override
	public double getMin(ArrayType type) {
		final double[] min=new double[]{Double.POSITIVE_INFINITY};
		PolarArray.applyToAll(_array.get(type),(double v)->{min[0]=v<min[0] ? v : min[0];});
		return min[0];
	}

	@Override
	public void addArrayToArray(ArrayType destination, ArrayType source) {
		PolarArray.applyToAll(_array.get(destination), _array.get(source), (double vd, double vs)->{return vd+vs;});
	}
	
//	protected boolean iteratorExceeds(int axis)
//	{
//		switch(axis){
//		case 0: case 2: 
//			return _currentCoord[axis] >= this._nVoxel[axis]; 
//		case 1: 
//			return _currentCoord[axis] >= ires*(2*(_currentCoord[0]+1)-1);
//		default: throw new IllegalArgumentException("axis must be <= 3");
//		}
//	}
	
	@Override
	public boolean isIteratorValid() {
		return idx<=ires*_nVoxel[0]*_nVoxel[0];
	}
	
	@Override
	public int[] resetIterator() {
		idx=1;
		return super.resetIterator();
	}
	
	@Override
	public int[] iteratorNext() {
		idx++;
		_currentCoord[0]=(int) Math.ceil(Math.pow(idx/ires,1/2))-1;
		_currentCoord[1]=(int) (idx - ires*Math.pow(_currentCoord[0],2))-1;
		_currentCoord[2]=(int) Math.ceil(idx/(ires*Math.pow(_nVoxel[0], 2)))-1;
		return _currentCoord;
	}
	
//	@Override
//	// needs to increment z first, then t, then r
//	public int[] iteratorNext()
//	{
//		_currentCoord[2]++;
//		if ( this.iteratorExceeds(2) )
//		{
//			_currentCoord[2] = 0;
//			_currentCoord[1]++;
//			if ( this.iteratorExceeds(1) )
//			{
//				_currentCoord[1] = 0;
//				_currentCoord[0]++;
//			}
//		}
//		return _currentCoord;
//	}
	
//	public int[] resetNbhIterator()
//	{
//		if ( this._currentNeighbor == null )
//			this._currentNeighbor = Vector.copy(this._currentCoord);
//		else
//			for ( int i = 0; i < 3; i++ )
//				this._currentNeighbor[i] = this._currentCoord[i];
//		_currentNeighbor[0]--;
//		_currentNeighbor[1]=_currentNeighbor[1]/ires;
//		_currentNeighbor[2]--;
//		System.out.println("\tnbh: "+Arrays.toString(_currentNeighbor));
//		return this._currentNeighbor;
//	}
	
//	protected boolean nbhIteratorExceeds(int axis) throws RuntimeException
//	{
//		
//		int rn=_currentNeighbor[0], tn=_currentNeighbor[1], zn=_currentNeighbor[2];
//		int rc=_currentCoord[0], tc=_currentCoord[1], zc=_currentCoord[2];
//		if (axis==2) {
//		if (zn!=zc) return (rn != rc);
//		
//		}
//		else 
//			return _currentNeighbor[axis] >  this._currentCoord[axis] + 1;
//		case 1: 
//			return getVoxelOrigin(_currentNeighbor)[1] > getVoxelOrigin(_currentCoord)[1];//+nt_rad*_res/(ires*2*_currentCoord[0]);
//		default: throw new IllegalArgumentException("axis must be <= 3");
//		}
//	}
	
//	protected boolean nbhIteratorExceeds(int axis) throws RuntimeException
//	{
//		switch(axis){
//		case 0: case 2: 
//			return _currentNeighbor[axis] >  this._currentCoord[axis] + 1;
//		case 1: 
//			return getVoxelOrigin(_currentNeighbor)[1] > getVoxelOrigin(_currentCoord)[1];//+nt_rad*_res/(ires*2*_currentCoord[0]);
//		default: throw new IllegalArgumentException("axis must be <= 3");
//		}
//	}
	
//	public boolean isNbhIteratorValid()
//	{
//		for ( int axis = 0; axis < 3; axis++ )
//			if ( nbhIteratorExceeds(axis) )
//				return false;
//		return true;
//	}
	
	public int[] resetNbhIterator(){
		nbhIdx=0;
		_currentNeighbor=Vector.add(Vector.copy(_currentCoord),Vector.toInt(nbhs[nbhIdx]));
		return _currentNeighbor;
	}
	
	public boolean isNbhIteratorValid(){return nbhIdx<nbhs.length;}
	
	public int[] nbhIteratorNext(){
		nbhIdx++;
		if (isNbhIteratorValid()){
			if (nbhIdx==4){ // moving in r
				_currentNeighbor[0]=(int)(_currentCoord[0]+nbhs[nbhIdx][0]);
				_currentNeighbor[2]=(int)(_currentCoord[2]+nbhs[nbhIdx][2]);
				_currentNeighbor[1]=(int)(_currentCoord[1]-2*_currentCoord[1]/(2*_currentCoord[0]+1))-1;
			}else if(nbhIdx==5){ // moving in r)
				_currentNeighbor[0]=(int)(_currentCoord[0]+nbhs[nbhIdx][0]);
				_currentNeighbor[2]=(int)(_currentCoord[2]+nbhs[nbhIdx][2]);
				_currentNeighbor[1]=(int)(_currentCoord[1]+2*_currentCoord[1]/(2*_currentCoord[0]+1))+1;
			}else{
				_currentNeighbor=Vector.add(Vector.copy(_currentCoord),Vector.toInt(nbhs[nbhIdx]));
			}
		}
		return _currentNeighbor;
	}
	
	
//	public int[] nbhIteratorNext()
//	{
//		this._currentNeighbor[2]++;
//		if ( this.nbhIteratorExceeds(2) )
//		{
//			this._currentNeighbor[2] = this._currentCoord[2] - 1;
//			this._currentNeighbor[1]++;
//			if ( this.nbhIteratorExceeds(1) )
//			{
//				this._currentNeighbor[0]++;
//				switch(_currentCoord[0]-_currentNeighbor[0]){
//				case 1:
//					this._currentNeighbor[1] = (this._currentCoord[1]-1)/ires;
//				case 0: // rc = rn
//					this._currentNeighbor[1] = this._currentCoord[1]-1;
//					break;
//				case -1: // rc < rn
//					this._currentNeighbor[1] = (this._currentCoord[1]-1)*ires;
//					break;
//				default: //throw new RuntimeException("oops");
//				}
//			}
//		}
//		if ( Vector.areSame(this._currentNeighbor, this._currentCoord) )
//			return this.nbhIteratorNext();
//		System.out.println("\tnbh: "+Arrays.toString(_currentNeighbor));
//		return _currentNeighbor;
//	}

	
	public CartesianGrid toCartesianGrid(ArrayType type){
		CartesianGrid grid = new CartesianGrid(
				new int[]{2*_nVoxel[0],2*_nVoxel[0],_nVoxel[2]}, _res);
		grid.newArray(type);
		grid.setAllTo(type, Double.NaN);
		this.resetIterator();
//		System.out.println(grid.arrayAsText(type));
		int[] next=_currentCoord;
		do{
			double[] loc_p=this.getVoxelCentre(next);
			//			System.out.println(next[0]+"  "+next[1]+"  "+next[2]);
			//			System.out.println(loc_p[0]+"  "+loc_p[1]+"  "+loc_p[2]);
			//			System.out.println((int)(loc_p[0]*Math.cos(loc_p[1]))+"  "+(int)(loc_p[0]*Math.sin(loc_p[1]))+"  "+loc_p[2]);
			//			System.out.println();
			int[] ar = new int[]{
					(int)(Math.ceil((loc_p[0])*Math.sin(loc_p[1])/_res)+_nVoxel[0]-1),
					(int)(Math.ceil((loc_p[0])*Math.cos(loc_p[1])/_res)+_nVoxel[0]-1),
					next[2]
			};
			double val=grid.getValueAt(type, ar);
			if (Double.isNaN(val)) 
				grid.setValueAt(type, ar, 0);
			else
				grid.setValueAt(type, ar, val+1);
			next=this.iteratorNext();
		}while(this.isIteratorValid());
		return grid;
	}

}
