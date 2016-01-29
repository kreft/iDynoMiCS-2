package grid;

import java.util.Arrays;
import java.util.HashMap;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.BoundarySide;

/**
 * \brief A grid with a spherical coordinate system.
 *  
 *  <p>Here we use the {@code r, φ, θ)} convention:</p><ul><li>{@code r} is the
 *  <i>radial</i> coordinate, i.e. Euclidean distance from the origin</li><li>
 *  {@code φ (phi)} is the <i>polar</i> coordinate (also known as the
 *  <i>zenith</i> or <i>colatitude</i>) and takes values between 0 and π 
 *  radians</li></ul><p><li>
 *  {@code θ (theta)} is the <i>azimuthal</i> coordinate (also known as the
 *  <i>longitude</i>) and takes values between 0 and 2π radians</li>See 
 *  <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">here</a> 
 *  for more details.</p>  
 *  
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public class SphericalGrid extends PolarGrid
{
	/**
	 * \brief The number of voxels this grid has in each of the three spatial 
	 * dimensions and the corresponding resolution calculator.
	 * 
	 * Notes:
	 * - The array has three rows, one for each dimension.
	 * - A row may contain a single value, a vector or a matrix.
	 * - _resCalc[0] is the radial distance and has length 1 (single value).
	 * - _resCalc[1] is the polar angle φ.
	 * - _resCalc[2] is the azimuthal angle θ.
	 * 
	 * - To keep the volume over the grid cells fairly constant for same 
	 * 		resolutions, some dependencies between the number of voxels
	 * 		were implemented:
	 *  * The number of voxels along the polar dimension (η_φ) 
	 *  	is dependent on the radius r: η_φ(r)=ires[2]*s(r) with s(r)=2*r+1;
	 * 	* The number of voxels along the azimuthal dimension (η_θ) 
	 * 		is dependent on the radius r and the polar angle φ.
	 *    This dependency is actually a sine with the domain scaled from [0,π]
	 *    to [0,η_φ(r)-1] and the co-domain scaled from [0,1] so that it peaks 
	 *    with a value of ires[1]*s(r) at the equator.  	  
	 * 
	 * <p>For example, a sphere (-> ires[1]=4, ires[2]=2) with radius 2  
	 * 			and resolution 1 would have:
	 * 		_nVoxel = [ [[2]], [[2],[6]], [[4,4],[4,12,20,20,12,4]] ].
	 *		_res = ...
	 * </p> 
	 */
	protected ResCalc[][][] _resCalc;
	
	/**
	 * @param totalSize
	 * @param res
	 */
	public SphericalGrid(double[] totalSize, double[] res)
	{
		super(totalSize);
		/*
		 * Set up sphere-specific members
		 */
		_radSize[2] = Math.toRadians(totalSize[2]%181);
		_ires[2] = PolarArray.ires(_radSize[2]); 

		/*
		 * Set up uniform resolution calculator array
		 */
		ResolutionCalculator resolution = new ResolutionCalculator();
		this._resCalc = new UniformResolution[3][][];
		/*
		 * Set up for the radial coordinate, and find out how many shells we
		 * have.
		 */
		this._resCalc[0] = new UniformResolution[1][1];
		this._resCalc[0][0][0] = resolution.new UniformResolution();
		this._resCalc[0][0][0].init(res[0], totalSize[0]);
		int nr = _resCalc[0][0][0].getNVoxel();
		/*
		 * For each radial shell we have a known number of voxels in theta,
		 * but the number of voxels in phi varies. 
		 */
		this._resCalc[1] = new UniformResolution[nr][1];
		this._resCalc[2] = new UniformResolution[nr][];
		int np;
		for ( int i = 0; i < nr; ++i )
		{
			/* Set up for theta. */
			this._resCalc[1][i][0] = resolution.new UniformResolution();
			this._resCalc[1][i][0].init(res[2], (int) _ires[2] * s(i));
			/* Determine how many for phi, and set these up. */
			np = this._resCalc[1][i][0].getNVoxel();
			this._resCalc[2][i] = new UniformResolution[np];
			for ( int j = 0; j < np; ++j )
			{
				double p_scale=(Math.PI/2)/(s(i)-0.5);
				double nt=_ires[1]+(s(i)-1)*_ires[1]*Math.sin(j*p_scale);
				double length = (int)Math.round(nt);
				this._resCalc[2][i][j] = resolution.new UniformResolution();
				this._resCalc[2][i][j].init(res[1], length);
			}
		}
	}
	
	public SphericalGrid(double[] totalSize, double res)
	{
		this(totalSize, Vector.vector(3, res));
	}
	
	/**
	 * Constructs a Grid with lengths (1,90,90) -- one grid cell
	 */
	public SphericalGrid()
	{
		this(new double[]{1, 90, 90}, 1.0);
	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
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
					this._array.get(type), ()->{return initialValues;});
		else
		{
			double[][][] array = PolarArray.createSphere(
					this._resCalc, initialValues
			);
			
			this._array.put(type, array);
		}
	}
	
	/**
	 * \brief Computes the arc length along the azimuthal dimension of the 
	 * grid element at the given coordinate.
	 * 
	 * TODO: Assumes constant resolution at the moment?!
	 * 
	 * @param coord Coordinates of a voxel in the grid.
	 * @return The arc length along the azimuthal dimension (theta) for this
	 * voxel.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthTheta(int[] coord)
	{
		ResCalc resCalc = _resCalc[2][coord[0]][coord[1]];
		return resCalc.getResolution(coord[2])
				* _radSize[1] / resCalc.getTotalLength();
	}
	
	/**
	 * \brief Computes the arc length along the polar dimension of
	 *  the given voxel.
	 * 
	 * TODO: Assumes constant resolution at the moment?!
	 * 
	 * @param coord Coordinates of a voxel in the grid.
	 * @return The arc length along the polar dimension (phi) for this voxel.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthPhi(int[] coord)
	{
		ResCalc resCalc = _resCalc[1][coord[0]][0];
		return resCalc.getResolution(coord[1])
				* _radSize[2] / resCalc.getTotalLength();
	}
	
	@Override
	public int[] getCoords(double[] loc, double[] inside)
	{
		int[] coord = new int[3];
		/*
		 * Determine i (as in Cartesian grid).
		 */
		cartLoc2Coord(0,
				loc[0],
				this._resCalc[0][0][0],
				coord,
				inside);
		/*
		 * Determine j.
		 */
		polarLoc2Coord(1,
				loc[1],
				this._radSize[1],
				this._resCalc[1][coord[0]][0],
				coord,
				inside);
		/*
		 * Determine k.
		 */
		polarLoc2Coord(2,
				loc[2],
				this._radSize[2],
				this._resCalc[2][coord[0]][coord[1]],
				coord, 
				inside);
		return coord;
	}
	
	@Override
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = new double[3];
		/*
		 * Determine r.
		 */
		cartCoord2Loc(0,
				coord[0],
				_resCalc[0][0][0],
				inside[0],
				loc);
		/* 
		 * Determine phi.
		 */
		polarCoord2Loc(1,
				coord[1],
				this._radSize[1],
				this._resCalc[1][coord[0]][0],
				inside[1],
				loc);
		/*
		 * Determine theta.
		 */
		polarCoord2Loc(2,
				coord[2],
				this._radSize[2],
				this._resCalc[2][coord[0]][coord[1]],
				inside[2],
				loc);
		return loc;
	}
	
	@Override
	public boolean isIteratorValid()
	{
		return ! this.iteratorExceeds(0);
	}
	
	@Override
	protected boolean iteratorExceeds(int axis) {
		switch(axis){
		case 0: return _currentCoord[0] >=  _resCalc[0][0][0].getNVoxel();
		case 1: return iteratorExceeds(0) ? true : _currentCoord[1] 
							>= _resCalc[1][_currentCoord[0]][0].getNVoxel();
		case 2: return (iteratorExceeds(0) || iteratorExceeds(1)) ? true 
				: _currentCoord[2] >= _resCalc[2][_currentCoord[0]]
											[_currentCoord[1]].getNVoxel();
		default: throw new RuntimeException("0 < axis <= 3 not satisfied");
		}
	}

	@Override
	//TODO: assumes constant resolution for each r at the moment?
	public void fillNbhSet()
	{
		int[] cc = _currentCoord;
//		System.out.println(Arrays.toString(cc));
		/*
		 * Moving along radial dimension.
		 */
		if ( _nbhIdx > 3 )
		{ 
			/*
			 * Change in r (-1 or 1)
			 */
			int[] nbh_coord = new int[3];
			int dr = NBH_DIRECS[_nbhIdx][0];
			nbh_coord[0] = cc[0] + dr;
			if ( isOutside(new int[]{nbh_coord[0] , -1, -1}, 0)==null )
			{
				double[] bounds_theta = new double[2];
				double[] bounds_phi = new double[2];
				double[] bounds_nbh_theta = new double[2];
				double[] bounds_nbh_phi = new double[2];
				
				double len_cur_theta = getArcLengthTheta(cc);
				double len_cur_phi = getArcLengthPhi(cc);
				
				/*
				 * current coord bounds phi
				 */
				polarCoord2Loc(0, 
						cc[1],
						_radSize[1],
						_resCalc[1][cc[0]][0], 
						0, 
						bounds_phi);
				
				polarCoord2Loc(1, 
						cc[1],
						_radSize[1],
						_resCalc[1][cc[0]][0], 
						1, 
						bounds_phi);
				
				/*
				 * current coord bounds theta
				 */
				polarCoord2Loc(0, 
						cc[2],
						_radSize[1],
						_resCalc[2][cc[0]][cc[1]], 
						0, 
						bounds_theta);
				
				polarCoord2Loc(1, 
						cc[2],
						_radSize[1],
						_resCalc[2][cc[0]][cc[1]], 
						1, 
						bounds_theta);
				
				/*
				 * first neighbor phi coordinate
				 */
				polarLoc2Coord(1, 
						bounds_phi[0],
						_radSize[1],
						_resCalc[1][nbh_coord[0]][0], 
						nbh_coord, 
						null);
				
				/*
				 * First neighbor phi location 0 (origin)
				 */
				polarCoord2Loc(0, 
						nbh_coord[1],
						_radSize[1],
						_resCalc[1][nbh_coord[0]][0], 
						0, 
						bounds_nbh_phi);
				
				while(bounds_nbh_phi[0] < bounds_phi[1]){	
					/*
					 * next neighbor in phi 
					 */
					polarCoord2Loc(1, 
							nbh_coord[1],
							_radSize[1],
							_resCalc[1][nbh_coord[0]][0], 
							1, 
							bounds_nbh_phi);
					
					double len_nbh_phi = getArcLengthPhi(nbh_coord);
					
					double sA_phi = getSharedArea(dr,
							len_cur_phi,
							bounds_phi,
							bounds_nbh_phi,
							len_nbh_phi);
//					System.out.println(Arrays.toString(nbh_coord));
					/*
					 * first neighbor theta coordinate
					 */
					polarLoc2Coord(2, 
							bounds_theta[0],
							_radSize[1],
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							nbh_coord, 
							null);
					
					/*
					 * First neighbor theta location 0 (origin)
					 */
					polarCoord2Loc(0, 
							nbh_coord[2],
							_radSize[1],
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							0, 
							bounds_nbh_theta);
					
					while(bounds_nbh_theta[0] < bounds_theta[1]){	
						/*
						 * next neighbor in theta
						 */
						polarCoord2Loc(1, 
								nbh_coord[2],
								_radSize[1],
								_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
								1, 
								bounds_nbh_theta);
						
						double len_nbh_theta = getArcLengthTheta(nbh_coord);
						
						double sA_theta = getSharedArea(dr,
								len_cur_theta,
								bounds_theta,
								bounds_nbh_theta,
								len_nbh_theta);
						
//						System.out.print("phi: "+Arrays.toString(bounds_nbh_phi)+"  "+Arrays.toString(bounds_phi));
//						System.out.println(" theta: "+Arrays.toString(bounds_nbh_theta)+"  "+Arrays.toString(bounds_theta));
						
						_subNbhSet.add(Vector.copy(nbh_coord));
						_subNbhSharedAreaSet.add(sA_phi * sA_theta);
						
						bounds_nbh_theta[0] = bounds_nbh_theta[1];
						nbh_coord[2]++;
					}
					bounds_nbh_phi[0] = bounds_nbh_phi[1];
					nbh_coord[1]++;
				}
				
//				/*
//				 * Compute number of voxels along azimuthal and polar dimension
//				 * for this and the neighboring 'constant-radius-shell'.
//				 */
//				double np_cur = _resCalc[1][cc[0]][0].getNVoxel();
//				double np_nbh = _resCalc[1][cc[0] + dr][0].getNVoxel();
//				double nt_cur= _resCalc[2][cc[0]][cc[1]].getNVoxel();
//				double nt_nbh;
//				/*
//				 * Compute the neighbor nVoxel to current nVoxel ratio 
//				 * for the polar dimension.
//				 */
//				double drt;
//				double drp = np_nbh / np_cur;
//				/*
//				 * Loop through all neighbors along the polar dimension.
//				 * Starting from the current polar angle coordinate times the 
//				 * nVoxel ratio and ending at the next polar angle coordinate
//				 * times the nVoxel ratio.
//				 */
//				for (int phi = (int) (cc[1]*drp);  phi < (cc[1]+1)*drp; phi++ )
//				{
//					nt_nbh=_resCalc[2][cc[0] + dr][phi].getNVoxel();
//					/*
//					 * Compute the neighbor nVoxel to current nVoxel ratio 
//					 * for the azimuthal dimension.
//					 */
//					drt = nt_nbh / nt_cur;
//					//System.out.println(drt+" "+nt_nbh+" "+nt_cur);
//					/*
//					 * Loop through all neighbors along the azimuthal dimension.
//					 */
//					for ( int theta = (int) (cc[2]*drt); 
//											theta < (cc[2]+1)*drt; theta++ )
//					{
//						_subNbhSet.add( new int[]{cc[0]+dr, phi, theta} );
//					}
//				}
			}
			/*
			* only change r coordinate if outside the grid along radial dimension.
			*/
			else _subNbhSet.add(new int[]{cc[0] + dr,cc[1],cc[2]});
		}
		/*
		 * Moving along polar dimension.
		 */
		else if ( _nbhIdx < 2 )
		{ 
			/*
			 * change in phi (-1 or 1)
			 */
			int dphi = NBH_DIRECS[_nbhIdx][2];
			int[] nbh_coord = new int[3];
			nbh_coord[1] = cc[1] + dphi;
//			System.out.println(dp);
			if (isOutside(new int[]{cc[0],nbh_coord[1],-1},1)==null){
				nbh_coord[0] = cc[0] + NBH_DIRECS[_nbhIdx][0];
				double[] bounds = new double[2];
				double[] bounds_nbh = new double[2];
				
				double len_cur = getArcLengthTheta(cc);
				
				/*
				 * current coord bounds theta
				 */
				polarCoord2Loc(0, 
						cc[2],
						_radSize[1],
						_resCalc[2][cc[0]][cc[1]], 
						0, 
						bounds);
				
				polarCoord2Loc(1, 
						cc[2],
						_radSize[1],
						_resCalc[2][cc[0]][cc[1]], 
						1, 
						bounds);
				
				/*
				 * first neighbor theta coordinate
				 */
				polarLoc2Coord(2, 
						bounds[0],
						_radSize[1],
						_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
						nbh_coord, 
						null);
				
				/*
				 * First neighbor theta location 0 (origin)
				 */
				polarCoord2Loc(0, 
						nbh_coord[2],
						_radSize[1],
						_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
						0, 
						bounds_nbh);
				
				while(bounds_nbh[0] < bounds[1]){	
					/*
					 * next neighbor in theta 
					 */
					polarCoord2Loc(1, 
							nbh_coord[2],
							_radSize[1],
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							1, 
							bounds_nbh);
					
					double len_nbh = getArcLengthTheta(nbh_coord);
					
					double sA = getSharedArea(dphi,
							len_cur,
							bounds,
							bounds_nbh,
							len_nbh);
					
					_subNbhSet.add(Vector.copy(nbh_coord));
					_subNbhSharedAreaSet.add(sA);
					bounds_nbh[0] = bounds_nbh[1];
					nbh_coord[2]++;
				}
				
//				/*
//				 * compute number of voxels (along azimuthal dimension) for this 
//				 * and the neighboring row in the matrix with index cc[0].
//				 */
//				double nt_cur=_resCalc[2][cc[0]][cc[1]].getNVoxel();
//				double nt_nbh=_resCalc[2][cc[0]][cc[1] + dphi].getNVoxel();
//				/*
//				 * compute the neighbor row length to current row length ratio 
//				 * for the azimuthal dimension.
//				 */
//				double drt=nt_nbh/nt_cur;
////				System.out.println(drt+" "+nt_nbh+" "+nt_cur);
//				/*
//				 * Loop through all neighbors along the azimuthal dimension.
//				 */
//				for (int t=(int)(cc[2]*drt);  t<(cc[2]+1)*drt; t++){
//					_subNbhSet.add(new int[]{cc[0],cc[1]+dphi,t});
//				}
			}
			/*
			* only change p coordinate if outside the grid along polar dimension.
			*/
			else
				this._subNbhSet.add(new int[]{cc[0],cc[1]+dphi,cc[2]});
		}
		/*
		 * Add the relative position to the current coordinate if moving along
		 * azimuthal dimension.
		 */
		else
		{ 
			this._subNbhSet.add(new int[]{ cc[0] + NBH_DIRECS[_nbhIdx][0],
									  		cc[1] + NBH_DIRECS[_nbhIdx][2],
									  		cc[2] + NBH_DIRECS[_nbhIdx][1] });
		}
	}

	@Override
	public void calcMinVoxVoxResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method calcMinVoxVoxResSq()");
	}

	@Override
	public int[] cyclicTransform(int[] coord) {
		BoundarySide bs = isOutside(coord,0);
		if (bs==BoundarySide.RMAX)
			coord[0] = coord[0]%(_resCalc[0][0][0].getNVoxel()-1);
		if (bs==BoundarySide.RMIN)
			coord[0] = _resCalc[0][0][0].getNVoxel()+coord[0];
		
		bs = isOutside(coord,1);
		if (bs!=null){
			int np=_resCalc[2][coord[0]][0].getNVoxel();;
			switch (bs){
			case PHIMAX: coord[1] = coord[1]%(np-1); break;
			case PHIMIN: coord[1] = np+coord[2]; break;
			case INTERNAL:
				coord[1] = coord[1]%np; 
				if (coord[1] < 0)	coord[1] += np;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		
		bs = isOutside(coord,2);
		if (bs!=null){
			int nt=_resCalc[2][coord[0]][coord[1]].getNVoxel();
			switch (bs){
			case THETAMAX: coord[2] = coord[2]%(nt-1); break;
			case THETAMIN: coord[2] = nt+coord[2]; break;
			case INTERNAL:
				coord[2] = coord[2]%nt; 
				if (coord[2] < 0) coord[2] += nt;
				break;
			default: throw new RuntimeException("unknown boundary side"+bs);
			}
		}
		return coord;
	}

	@Override
	public double getVoxelVolume(int[] coord) {
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1=getVoxelOrigin(coord);
		double[] loc2=getLocation(coord,VOXEL_All_ONE_HELPER);

		return ((loc1[0]*loc1[0]*loc1[0]-loc2[0]*loc2[0]*loc2[0])
					* (loc1[1]-loc2[1])
					* (Math.cos(loc1[2])-Math.cos(loc2[2]))
				)/3;
	}
	
	@Override
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		out[0] = (_resCalc[0][0][0].getNVoxel() > 1 );  
		out[1] = _radSize[1] > 0;
		out[2] = _radSize[2] > 0;
		return out;
	}

	@Override
	public int numSignificantAxes()
	{
		int out = 0;
		out += (_resCalc[0][0][0].getNVoxel() > 1 ) ? 1 : 0;
		out += _radSize[1] > 0 ? 1 : 0;
		out += _radSize[2] > 0 ? 1 : 0;
		return out;
	}

	@Override
	public double getNbhSharedSurfaceArea() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getNbhSharedSurfaceArea()");
		return 1;
	}

	@Override
	public double getCurrentNbhResSq() {
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getCurrentNbhResSq()");
		return 1;
	}

	@Override
	protected BoundarySide isOutside(int[] coord, int dim) {
		switch (dim) {
		case 0:
			if ( coord[0] < 0 )
				return BoundarySide.RMIN;
			if ( coord[0] >= _resCalc[0][0][0].getNVoxel() )
				return BoundarySide.RMAX;
			break;
		case 1:
			if (isOutside(coord,0)!=null)  
				return BoundarySide.UNKNOWN;
			int np=_resCalc[1][coord[0]][0].getNVoxel();
			if ( coord[1] < 0 )
				return _radSize[2]==Math.PI ? BoundarySide.INTERNAL : BoundarySide.PHIMAX;
			if ( coord[1] >= np )
				return _radSize[2]==Math.PI ? BoundarySide.INTERNAL : BoundarySide.PHIMIN;
			break;
		case 2:
			if (isOutside(coord,0)!=null || isOutside(coord,1)!=null)  
				return BoundarySide.UNKNOWN;
			int nt=_resCalc[2][coord[0]][coord[1]].getNVoxel();
			if ( coord[2] < 0 )
				return _radSize[1]==2*Math.PI ? BoundarySide.INTERNAL : BoundarySide.THETAMAX;
			if ( coord[2] >= nt)
				return _radSize[1]==2*Math.PI ? BoundarySide.INTERNAL : BoundarySide.THETAMIN;
			break;
			default: throw new IllegalArgumentException("dim must be > 0 and < 3");
		}
		return null;
	}
	
//	/**
//	 * \brief Computes the number of columns in matrix i, row j for resolution 1.
//	 * 
//	 * This is the total length if transformed to a cartesian coordinate system.
//	 * 
//	 * @param i - matrix index
//	 * @param j - row index
//	 * @return - the number of elements in row j
//	 */
//	private int nCols(int i, int j){
//
////		double res = _resCalc[1][i][0].getCumResSum(s(i))/s(i);
////		double p_scale=(Math.PI/2)/(s(i)-0.5) * res;
////		double nt=_ires[1]+(s(i)-1)*_ires[1]*Math.sin(j*p_scale)*res;
////		return (int)Math.round(nt);
//	}
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	public static final GridGetter standardGetter()
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(double[] totalLength, double resolution) 
			{
				return new SphericalGrid(totalLength,resolution);
			}
		};
	}
}
