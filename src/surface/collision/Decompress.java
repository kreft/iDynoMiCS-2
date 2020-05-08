package surface.collision;

import linearAlgebra.Vector;

public class Decompress {
	
	public int nDim;
	public double[] resolution;
	public double[][][] pressure;
	public double[][][][] shift; // directionDim, x, y, z
	public double threshold = 1000;
	public double threshold2 = 1E14;
	public int[] maxima;
	
	public Decompress(double[] max, double targetResolution)
	{
		nDim = max.length;
		resolution = new double[3];
		for( int i = 0; i < nDim; i++ )
		{
			resolution[i] = max[i] / Math.ceil(max[i] / targetResolution);
		}
		
		maxima = new int[] { 
				(int) (max[0] / resolution[0]),
				(int) (max[1] / resolution[1]),
				(max.length == 3 ? (int) (max[2] / resolution[2]) : 1)
				};
		
		pressure = new double
				[maxima[0]]
				[maxima[1]]
				[maxima[2]];
		
		shift = new double
				[3]
				[maxima[0]]
				[maxima[1]]
				[maxima[2]];
	}
	
	public void addPressure(double[] location, double amount)
	{
		int[] loc = Vector.translate(location, resolution);
		pressure[loc[0]][loc[1]][loc[2]] += amount/1000.0;
	}
	
	public int[] getMop(double[] location)
	{
		return Vector.translate(location, resolution);
	}
	
	public double[] getDirection(double[] location)
	{
		int[] loc = Vector.translate(location, resolution);
		return new double[] { 
				shift[0][loc[0]][loc[1]][loc[2]],
				shift[1][loc[0]][loc[1]][loc[2]],
				shift[2][loc[0]][loc[1]][loc[2]] };
	}
	
	public void buildDirectionMatrix()
	{
		// clear shift matrix from previous iteration
		shift = new double
				[3]
				[maxima[0]]
				[maxima[1]]
				[maxima[2]];
		
		// build new shift matrix
		for( int i = 0; i < pressure.length; i++ )
			for( int j = 0; j < pressure[0].length; j++ )
				for( int k = 0; k < pressure[0][0].length; k++ )
		{
			int [] location = new int[] { i, j, k };
			if( pressure[i][j][k] > threshold2)	
				for( int l = 0; l < resolution.length; l++)
				{
					int range[] = shiftRange(location, l);
					for( int m = range[0]; m < location[l]; m++)
					{
						directionAdd( l, m, location, 
								-pressure[i][j][k]);
					}
					for( int m = range[1]; m > location[l]; m--)
					{
						directionAdd( l, m, location, 
								pressure[i][j][k]);
					}
				}
		}
		// reset pressure matrix for next step
		pressure = new double
				[maxima[0]]
				[maxima[1]]
				[maxima[2]];
	}
	
	public void directionAdd(int dim, int mod, int[] loc, double val)
	{
		switch(dim) {
		case 0 :
			shift[dim][mod][loc[1]][loc[2]] += val;
			break;
		case 1 :
			shift[dim][loc[0]][mod][loc[2]] += val;
			break;
		case 2 :
			shift[dim][loc[0]][loc[1]][mod] += val;
			break;
	}

	}

	public int[] shiftRange(int[] location, int dimension)
	{
		int[] range = new int[] { location[dimension] , location[dimension] };
		int[] temp = Vector.copy(location);
		
		double value = Double.MAX_VALUE;
		while( value > threshold && range[0] > 0 )
		{
			range[0]--;
			temp[dimension] = range[0];
			value = pressure[temp[0]][temp[1]][temp[2]];
		}
		if( range[0] == 0 && value > threshold)
			range[0] = location[dimension];
		
		value = Double.MAX_VALUE;
		while( value > threshold && range[1] < maxima[dimension]-1 )
		{
			range[1]++;
			temp[dimension] = range[1];
			value = pressure[temp[0]][temp[1]][temp[2]];
		}
		if( range[1] == maxima[dimension] && value > threshold)
			range[1] = location[dimension];
		
		System.out.println(Vector.toString(range));
		return range;
	}
}
