package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public class CylindricalGrid extends CartesianGrid{
	final double iresT;
	int nbhIdx, idx;
	int[][] nbhs;
	double nt_rad;
	
	public CylindricalGrid(int[] nVoxel, double resolution)
	{
		nVoxel[1] = nVoxel[1]%361; // theta periodic in 1..360
		this._nVoxel = Vector.copy(nVoxel);  // [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		this._res = resolution;				 // scales r & ires TODO: change 1/res in code, not here
		this.nt_rad = nVoxel[1]*Math.PI/180;
		this.iresT=PolarArray.computeIRES(nVoxel[0], nt_rad, resolution);
		nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
		resetIterator();
		resetNbhIterator();
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
			double[][][] array = PolarArray.createCylinder(this._nVoxel[0],
											this._nVoxel[2], this.iresT, initialValues);
			this._array.put(type, array);
		}
	}

	@Override
	public double getVoxelVolume() {
		// TODO Auto-generated method stub
		return nt_rad*_res*_res*_res/(iresT*2);
	}

	@Override
	public int[] getCoords(double[] loc) {
		return new int[]{
				(int)(loc[0]/_res),
				(int)(loc[1]*iresT*2*loc[0]/(nt_rad*_res)),
				(int)(loc[2]/_res)};
	}

	@Override
	public double[] getVoxelOrigin(int[] coords) {
		double r=coords[0]*_res;
		double t;
		if (r==0) t = Math.min(coords[1]*(Math.PI/2),nt_rad);
		else if (r>0) t = coords[1]*nt_rad*_res/(iresT*2*r);
		else t = Math.abs(coords[1]*nt_rad*_res/(iresT*2*r))-Math.PI;
		double z=coords[2]*_res;
//		System.out.println(coords[0]+"  "+coords[1]+"  "+coords[2]+" | "+r+"  "+t+"  "+z);
		return new double[]{r,t,z};
	}
	
	public double[] getVoxelCentre(int[] coords)
	{
		double r=(coords[0]+0.5)*_res;
		double t=(coords[1]+0.5)*nt_rad*_res/(iresT*2*r);
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
		if ( coord[1] >= iresT*(2*coord[0]-1) )
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
	
	@Override
	public boolean isIteratorValid() {return idx<=_nVoxel[2]*iresT*_nVoxel[0]*_nVoxel[0];}
	
	public void setCurrent(int[] new_current){
		_currentCoord=new_current;
		idx=(int)(new_current[2]*iresT*_nVoxel[0]*_nVoxel[0]+(new_current[1]+iresT*new_current[0]*new_current[0]+1)); 
//		System.out.println(new_current[0]+"  "+new_current[1]+"  "+new_current[2]+"  "+idx);
	}
	
	@Override
	public int[] resetIterator() {
		idx=1;
		return super.resetIterator();
	}
	
	@Override
	public int[] iteratorNext() {
		idx++;
		_currentCoord[2]=(int) Math.ceil(idx/(iresT*Math.pow(_nVoxel[0], 2)))-1;
		_currentCoord[0]=(int) Math.ceil(Math.pow((idx-(_currentCoord[2]*iresT*Math.pow(_nVoxel[0], 2)))/iresT,1.0/2))-1;
		_currentCoord[1]=(int) (idx - iresT*Math.pow(_currentCoord[0],2))-1;
		return _currentCoord;
	}
	
	public int[] resetNbhIterator(){
		nbhIdx=0;
		_currentNeighbor=Vector.add(Vector.copy(_currentCoord),nbhs[nbhIdx]);
		return _currentNeighbor;
	}
	
	public boolean isNbhIteratorValid(){return nbhIdx<nbhs.length;}
	
	public int[] nbhIteratorNext(){
		nbhIdx++;
		if (isNbhIteratorValid()){
			_currentNeighbor=Vector.add(Vector.copy(_currentCoord),nbhs[nbhIdx]);
			if (nbhIdx>3){ // moving in r
				System.out.println(2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1));
				_currentNeighbor[1]=_currentCoord[1]+nbhs[nbhIdx][1]*(int)Math.round(2*(_currentCoord[1]+1)/(2.0*_currentCoord[0]+1));
			}
		}
		return _currentNeighbor;
	}

	
	public CartesianGrid toCartesianGrid(ArrayType type){
		CartesianGrid grid = new CartesianGrid(
				new int[]{2*_nVoxel[0],2*_nVoxel[0],_nVoxel[2]}, _res);
		grid.newArray(type);
		grid.setAllTo(type, Double.NaN);
		this.resetIterator();
		int[] next=_currentCoord;
		do{
			double[] loc_p=this.getVoxelCentre(next);
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
