package grid;

import java.util.HashMap;
import java.util.Iterator;

import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public class CylindricalGrid extends CartesianGrid{
	int ires;
	double nt_rad;
	
	public CylindricalGrid(int[] nVoxel, double resolution)
	{
		nVoxel[1] = nVoxel[1]%361; // theta periodic in 1..360
		this._nVoxel = Vector.copy(nVoxel);  // [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		this._nVoxel[0]=this._nVoxel[0];
		this._res = resolution;				 // scales r & ires TODO: change 1/res in code, not here
		this.nt_rad = nVoxel[1]*Math.PI/180;
		this.ires=PolarArray.computeIRES(nVoxel[0], nt_rad, resolution);
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
		double loc_rad = loc[1]*Math.PI/180;
		return new int[]{
				(int)(loc[0]/_res),
				(int)(loc_rad*ires*(2*(loc[0]/_res)-_res)/(nt_rad*_res)),
				(int)(loc[2]/_res)};
	}

	@Override
	public double[] getVoxelOrigin(int[] coords) {
		double r=coords[0]*_res;
		double t = (r==0 ? 0 : coords[1]*nt_rad*_res/(ires*2*r));
		double z=coords[2]*_res;
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
	
	@Override
	protected boolean iteratorExceeds(int axis)
	{
		switch(axis){
		case 0: case 2: 
			return _currentCoord[axis] >= this._nVoxel[axis]; 
		case 1: 
			return _currentCoord[axis] >= ires*(2*(_currentCoord[0]+1)-1);
		default: throw new IllegalArgumentException("axis must be <= 3");
		}
	}
	
	@Override
	// needs to increment z first, then t, then r
	public int[] iteratorNext()
	{
		_currentCoord[2]++;
		if ( this.iteratorExceeds(2) )
		{
			_currentCoord[2] = 0;
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = 0;
				_currentCoord[0]++;
			}
		}
		return _currentCoord;
	}
	
	public int[] resetNbhIterator(boolean inclDiagonalNhbs)
	{
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentNeighbor[i] = this._currentCoord[i];
		this._inclDiagonalNhbs = inclDiagonalNhbs;
		for ( int axis = 0; axis < 3; axis++ )
			if ( this._nVoxel[axis] > 1 )
				this._currentNeighbor[axis]--;
		if ( (! this._inclDiagonalNhbs) && isDiagNbh() )
			return this.nbhIteratorNext();
		return this._currentNeighbor;
	}
	
	@Override
	protected boolean nbhIteratorExceeds(int axis) throws RuntimeException
	{
		/*
		 * If this is a trivial axis and we're not on it, then we're
		 * definitely in the wrong place.
		 */
		if ((axis==0 || axis==2) && (this._nVoxel[axis] == 1 && 
				this._currentNeighbor[axis] != this._currentCoord[axis] ))
		{
			return true;
		}
		switch(axis){
		case 0: case 2: 
			return _currentNeighbor[axis] >  this._currentCoord[axis] + 1;
		case 1: 
			switch(_currentCoord[0] - _currentNeighbor[0]){
			case 1: // rn < rc
				return _currentNeighbor[1] >  (this._currentCoord[1]/ires)+1;
			case 0: // rn = rc
				return _currentNeighbor[1] >  this._currentCoord[1] + 1;
			case -1: // rn > rc
				return _currentNeighbor[1] >  (this._currentCoord[1]*ires)+1;
			default: throw new RuntimeException("unknown error");
			}
//			return _currentNeighbor[1]*nt_rad/(ires*2*_currentNeighbor[0]) 
//					>  this._currentCoord[1]*nt_rad/(ires*2*_currentCoord[0]);
		default: throw new IllegalArgumentException("axis must be <= 3");
		}
	}
	
	public boolean isNbhIteratorValid()
	{
		for ( int axis = 0; axis < 3; axis++ )
			if ( nbhIteratorExceeds(axis) )
				return false;
		return true;
	}
	
	public int[] nbhIteratorNext()
	{
		this._currentNeighbor[1]++;
		if ( this.nbhIteratorExceeds(1) )
		{
			this._currentNeighbor[1] = this._currentCoord[1] - 1;
			this._currentNeighbor[0]++;
			if ( this.nbhIteratorExceeds(0) )
			{
				this._currentNeighbor[0] = this._currentCoord[0] - 1;
				this._currentNeighbor[2]++;
			}
		}
		if ( Vector.areSame(this._currentNeighbor, this._currentCoord) )
			return this.nbhIteratorNext();
		if ( (! this._inclDiagonalNhbs) && isDiagNbh() )
			return this.nbhIteratorNext();
		return _currentNeighbor;
	}

	@Override
	protected boolean isDiagNbh()
	{
//		int counter = 0;
//		int diff;
//		for ( int axis = 0; axis < 3; axis+=2 )
//		{
//			if (axis==0 || axis==3)
//				diff = (int) Math.abs(this._currentNeighbor[axis] - 
//					this._currentCoord[axis]);
//			else 
//				diff = Math.abs(this._currentNeighbor[axis] - 
//						this._currentCoord[axis])/(2*ires);
//			if ( diff == 1 )
//				counter++;
//			if ( counter > 1 )
//				return true;
//		}
		return false;
	}
	
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
