package surface.collision;

import dataIO.Log;
import idynomics.Global;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;

public class Decompress {
	
	public int nDim;
	public double[] resolution;
	public double[][][] pressure;
	public double[][][][] shift; // directionDim, x, y, z
	public double thresholdLow;
	public double thresholdHigh;
	public int[] maxima;
	public boolean[] _periodicDimensions;
	public double traversingFraction = Global.traversing_fraction;
	public double[] _pows;
	
	public Decompress(double[] max, double targetResolution, double threshold,
			boolean[] periodicDimensions, double traversingFraction, double dampingFactor)
	{
		if ( traversingFraction != 0.0 )
			this.traversingFraction = traversingFraction;
		nDim = max.length;
		resolution = new double[3];
		this._periodicDimensions = periodicDimensions;
		if( targetResolution == 0.0)
		{
			Idynomics.simulator.interupt("Enabled " + this.getClass().getSimpleName()
					+ " without " + AspectRef.decompressionCellLength + " stopping.");
		}
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
		this._pows = new double[Vector.max(maxima)];
		for( int i = 0; i < Vector.max(maxima); i++)
			_pows[i] = Math.pow(dampingFactor, i);
		
		//TODO make them individually settable.
		this.thresholdHigh = threshold*2.0;
		this.thresholdLow = threshold*1.0;
	}
	
	public int[] translate(double[] location, double[] resolution) 
	{
		int out[] = new int[3];
		for(int i = 0; i < resolution.length; i++)
			if( i < location.length)
			{
				// FIXME here we need to safe some agents out of the domain, but this is double work since they shouldn't be
				out[i] = (int) (location[i]/resolution[i]);
				if(out[i] == maxima[i])
				{
					out[i] = 0;
				}
				if(out[i] == -1)
				{
					out[i] = maxima[i]-1;
				}
			}
			else
				out[i] = 0;
		return out;
	}
	
	public void addPressure(double[] location, double amount)
	{
		
		int[] loc = translate(location, resolution);
		pressure[loc[0]][loc[1]][loc[2]] += amount*traversingFraction;
	}
	
	public int[] getMop(double[] location)
	{
		return translate(location, resolution);
	}
	
	public double[] getDirection(double[] location)
	{
		int[] loc = translate(location, resolution);
		// FIXME dimensions appear to be swapped investigate
		double[] out = new double[] { 
				shift[0][loc[0]][loc[1]][loc[2]],
				shift[1][loc[0]][loc[1]][loc[2]],
				shift[2][loc[0]][loc[1]][loc[2]] };
		return out;
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
			if( pressure[i][j][k] > thresholdHigh)	
				for( int l = 0; l < resolution.length; l++)
				{
					int dim = l;
					int range[] = shiftRange(location, dim);
					for( int m = range[0]; m < location[dim]; m++)
					{
						directionAdd( dim, m, location, 
								-pressure[i][j][k] * this._pows[location[dim]-m]);
					}
					for( int m = range[1]; m > location[dim]; m--)
					{
						directionAdd( dim, m, location, 
								pressure[i][j][k] * this._pows[m-location[dim]]);
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
		if (mod > maxima[dim]-1)
			mod = mod-maxima[dim];
		if (mod < 0)
			mod = mod+maxima[dim];
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

		int stop = 0;
		int cursor;
		
		double value = Double.MAX_VALUE;
		if( this._periodicDimensions[dimension] )
			stop = 0-location[dimension];
		else
			stop = 0;
		
		while( value > thresholdLow && range[0] > stop )
		{
			range[0]--;
			cursor = range[0];
			if (cursor < 0)
				cursor = maxima[dimension]+range[0];
			
			temp[dimension] = cursor;
			value = pressure[temp[0]][temp[1]][temp[2]];
		}
		if( range[0] == stop && value > thresholdLow)
			range[0] = location[dimension];
		
		value = Double.MAX_VALUE;
		if( this._periodicDimensions[dimension] )
			stop = maxima[dimension]-1+location[dimension];
		else
			stop = maxima[dimension]-1;
		while( value > thresholdLow && range[1] < stop )
		{
			range[1]++;
			cursor = range[1];
			if (cursor > maxima[dimension]-1)
				cursor = range[1]-maxima[dimension];
			
			temp[dimension] = cursor;
			value = pressure[temp[0]][temp[1]][temp[2]];
		}
		if( range[1] == stop && value > thresholdLow)
			range[1] = location[dimension];

		return range;
	}
}
