package grid;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.UniformResolution;
import linearAlgebra.Array;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

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
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
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
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param totalLength
	 * @param res
	 */
	public SphericalGrid(double[] totalLength, double[] res)
	{
		super(totalLength);
		this._dimName[1] = DimName.PHI;
		this._dimName[2] = DimName.THETA;
		/*
		 * Set up sphere-specific members
		 */
		
		this._radSize[2] = Math.toRadians(totalLength[2]%181);
		this._ires[2] = PolarArray.ires(this._radSize[2]); 
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
		this._resCalc[0][0][0].init(res[0], totalLength[0]);
		int numShells = _resCalc[0][0][0].getNVoxel();
		/*
		 * For each radial shell we have a known number of voxels in phi,
		 * but the number of voxels in theta varies.
		 */
		this._resCalc[1] = new UniformResolution[numShells][1];
		this._resCalc[2] = new UniformResolution[numShells][];
		int numRings;
		double phi_scale, ring_length;
		for ( int shell = 0; shell < numShells; ++shell )
		{
			/* Set up for theta. */
			this._resCalc[1][shell][0] = resolution.new UniformResolution();
			double tRes = getTargetResolution(shell, res[1], totalLength[1]);
			this._resCalc[1][shell][0].init(tRes, totalLength[1]);
			/* Determine how many for phi, and set these up. */
			numRings = this._resCalc[1][shell][0].getNVoxel();
			this._resCalc[2][shell] = new UniformResolution[numRings];
			for ( int j = 0; j < numRings; ++j )
			{
				/*
				 * Scale phi to peak at π / 2 instead of s(shell), where it 
				 * would peak for a resolution of one. This way we can use it as
				 * an input argument for a sine (which peaks at sin(π / 2) = 1
				 * naturally). Actually we let it peak at s(shell) - 0.5 to keep
				 * things symmetric around the equator.
				 */
				phi_scale = 0.5 * Math.PI / (s(shell)-0.5);
				/*
				 * compute the sine of the scaled phi coordinate and scale the 
				 * result to be 
				 * sin(0) = number of voxels at r=0 (iresTheta)
				 * sin(π / 2) = s(shell) * iresTheta
				 * sin(π) = number of voxels at r=0 (iresTheta)
				 * This is the number of voxels in theta.
				 */
				ring_length = this._ires[1] + 
							(s(shell)-1)*this._ires[1]*Math.sin(j * phi_scale);
				this._resCalc[2][shell][j] = resolution.new UniformResolution();
				this._resCalc[2][shell][j].init(res[1], totalLength[2]);
			}
		}
	}
	
	public SphericalGrid(double[] totalSize, double res)
	{
		this(totalSize, Vector.vector(3, res));
	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this._array.containsKey(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
		{
			this._array.put(type,
					PolarArray.createSphere(this._resCalc, initialValues));
		}
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Computes the arc length along the azimuthal dimension of the 
	 * grid element at the given coordinate.
	 * 
	 * @param coord Coordinates of a voxel in the grid.
	 * @return The arc length along the azimuthal dimension (theta) for this
	 * voxel.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double getArcLengthTheta(int[] coord)
	{
		ResCalc resCalc = this._resCalc[2][coord[0]][coord[1]];
		return resCalc.getResolution(coord[2])
				* this.getTotalLength(1) / resCalc.getTotalLength();
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
		ResCalc resCalc = this._resCalc[1][coord[0]][0];
		// TODO surely we can just return resCalc.getResolution(coord[1])?
		return resCalc.getResolution(coord[1])
				* this.getTotalLength(2) / resCalc.getTotalLength();
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
				this.getTotalLength(1),
				this._resCalc[1][coord[0]][0],
				coord,
				inside);
		/*
		 * Determine k.
		 */
		polarLoc2Coord(2,
				loc[2],
				this.getTotalLength(2),
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
				this.getTotalLength(1),
				this._resCalc[1][coord[0]][0],
				inside[1],
				loc);
		/*
		 * Determine theta.
		 */
		polarCoord2Loc(2,
				coord[2],
				this.getTotalLength(2),
				this._resCalc[2][coord[0]][coord[1]],
				inside[2],
				loc);
		return loc;
	}
	

	@Override
	public void calcMinVoxVoxResSq()
	{
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method calcMinVoxVoxResSq()");
	}
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1 = getVoxelOrigin(coord);
		double[] loc2 = getVoxelUpperCorner(coord);
		/* r */
		double out = ExtraMath.cube(loc1[0]) - ExtraMath.cube(loc2[0]);
		/* phi */
		out *= loc1[1] - loc2[1];
		/* theta */
		out *= Math.cos(loc1[2]) - Math.cos(loc2[2]);
		return out / 3.0;
	}
	
	@Override
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		out[0] = ( this._resCalc[0][0][0].getNVoxel() > 1 );  
		out[1] = ( this.getTotalLength(1) > 0.0 );
		out[2] = ( this.getTotalLength(2) > 0.0 );
		return out;
	}

	@Override
	public int numSignificantAxes()
	{
		int out = 0;
		out += (this._resCalc[0][0][0].getNVoxel() > 1 ) ? 1 : 0;
		out += (this.getTotalLength(1) > 0) ? 1 : 0;
		out += (this.getTotalLength(2) > 0) ? 1 : 0;
		return out;
	}

	@Override
	public double getNbhSharedSurfaceArea()
	{
		// TODO Auto-generated method stub
		System.err.println(
				"tried to call unimplemented method getNbhSharedSurfaceArea()");
		return 1;
	}
	
	@Override
	public int[] getNVoxel(int[] coords)
	{
		return new int[]{this._resCalc[0][0][0].getNVoxel(),
						 this._resCalc[1][coords[0]][0].getNVoxel(),
						 this._resCalc[2][coords[0]][coords[1]].getNVoxel()};
	}
	
	protected double getTotalLength(int axis)
	{
		return this._resCalc[axis][0][0].getTotalLength();
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		switch ( axis )
		{
			/* r */
			case 0: return this._resCalc[0][0][0];
			/* phi */
			case 1: return this._resCalc[1][coord[0]][0];
			/* theta */
			case 2: return this._resCalc[2][coord[0]][coord[1]];
			// TODO throw an exception?
			default: return null;
		}
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	// @Override
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
			if ( isOutside(new int[]{nbh_coord[0] , -1, -1}) == null )
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
						this.getTotalLength(1),
						_resCalc[1][cc[0]][0], 
						0, 
						bounds_phi);
				
				polarCoord2Loc(1, 
						cc[1],
						this.getTotalLength(1),
						_resCalc[1][cc[0]][0], 
						1, 
						bounds_phi);
				
				/*
				 * current coord bounds theta
				 */
				polarCoord2Loc(0, 
						cc[2],
						this.getTotalLength(1),
						_resCalc[2][cc[0]][cc[1]], 
						0, 
						bounds_theta);
				
				polarCoord2Loc(1, 
						cc[2],
						this.getTotalLength(1),
						_resCalc[2][cc[0]][cc[1]], 
						1, 
						bounds_theta);
				
				/*
				 * first neighbor phi coordinate
				 */
				polarLoc2Coord(1, 
						bounds_phi[0],
						this.getTotalLength(1),
						_resCalc[1][nbh_coord[0]][0], 
						nbh_coord, 
						null);
				
				/*
				 * First neighbor phi location 0 (origin)
				 */
				polarCoord2Loc(0, 
						nbh_coord[1],
						this.getTotalLength(1),
						_resCalc[1][nbh_coord[0]][0], 
						0, 
						bounds_nbh_phi);
				
				while(bounds_nbh_phi[0] < bounds_phi[1]){	
					/*
					 * next neighbor in phi 
					 */
					polarCoord2Loc(1, 
							nbh_coord[1],
							this.getTotalLength(1),
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
							this.getTotalLength(1),
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							nbh_coord, 
							null);
					
					/*
					 * First neighbor theta location 0 (origin)
					 */
					polarCoord2Loc(0, 
							nbh_coord[2],
							this.getTotalLength(1),
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							0, 
							bounds_nbh_theta);
					
					while(bounds_nbh_theta[0] < bounds_theta[1])
					{	
						/*
						 * next neighbor in theta
						 */
						polarCoord2Loc(1, 
								nbh_coord[2],
								this.getTotalLength(1),
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
			}
			/*
			* only change r coordinate if outside the grid along radial dimension.
			*/
			else _subNbhSet.add(new int[]{cc[0] + dr,cc[1],cc[2]});
		}
		/*
		 * Moving along polar dimension.
		 */
		else if ( this._nbhIdx < 2 )
		{ 
			/*
			 * change in phi (-1 or 1)
			 */
			int dPhi = NBH_DIRECS[this._nbhIdx][2];
			int[] nbh_coord = new int[3];
			nbh_coord[1] = cc[1] + dPhi;
//			System.out.println(dp);
			if ( isOutside(new int[]{cc[0],nbh_coord[1],-1}) == null )
			{
				nbh_coord[0] = cc[0] + NBH_DIRECS[_nbhIdx][0];
				double[] bounds = new double[2];
				double[] bounds_nbh = new double[2];
				
				double len_cur = getArcLengthTheta(cc);
				
				/*
				 * current coord bounds theta
				 */
				polarCoord2Loc(0, 
						cc[2],
						this.getTotalLength(1),
						_resCalc[2][cc[0]][cc[1]], 
						0, 
						bounds);
				
				polarCoord2Loc(1, 
						cc[2],
						this.getTotalLength(1),
						_resCalc[2][cc[0]][cc[1]], 
						1, 
						bounds);
				
				/*
				 * first neighbor theta coordinate
				 */
				polarLoc2Coord(2, 
						bounds[0],
						this.getTotalLength(1),
						_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
						nbh_coord, 
						null);
				
				/*
				 * First neighbor theta location 0 (origin)
				 */
				polarCoord2Loc(0, 
						nbh_coord[2],
						this.getTotalLength(1),
						_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
						0, 
						bounds_nbh);
				
				while(bounds_nbh[0] < bounds[1])
				{	
					/*
					 * next neighbor in theta 
					 */
					polarCoord2Loc(1, 
							nbh_coord[2],
							this.getTotalLength(1),
							_resCalc[2][nbh_coord[0]][nbh_coord[1]], 
							1, 
							bounds_nbh);
					
					double len_nbh = getArcLengthTheta(nbh_coord);
					
					double sA = getSharedArea(dPhi,
							len_cur,
							bounds,
							bounds_nbh,
							len_nbh);
					
					_subNbhSet.add(Vector.copy(nbh_coord));
					_subNbhSharedAreaSet.add(sA);
					bounds_nbh[0] = bounds_nbh[1];
					nbh_coord[2]++;
				}
			}
			/*
			* only change p coordinate if outside the grid along polar dimension.
			*/
			else
				this._subNbhSet.add(new int[]{cc[0],cc[1]+dPhi,cc[2]});
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
