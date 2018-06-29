package optimization.sampling;


/**
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class CustomSampler extends Sampler {
	
	private double[][] _ranges;
	int _d;

	public int size()
	{
		int out = 1;
		for (int i = 0; i < (_d); i++) 
		{
			out *= length(i);
		}
		return out;
	}
	
	public CustomSampler()
	{
		
	}
	
	public CustomSampler(double[][] ranges)
	{
		this._ranges = ranges;
		this._d = _ranges[0].length;
	}
	
		
	@Override
	public double[][] sample() 
	{	

		double[][] out = new double[size()][_d];
		
		/* cycle over each dimension */
		for (int i = 0; i < (_d); i++) 
		{
			double cur = this._ranges[0][i];
			out[0][i] = cur;
			if( i == 0) {
				for (int j = 1; j < out.length ; j++) 
				{
					cur = next(cur,i);
					out[j][i] = cur;
				}
			}
			else
			{
				for (int j = 1; j < out.length ; j++) 
				{
					boolean tally = true;
					for (int k = i-1; k >= 0 ; k--) 
					{
						if( out[j][k] != this._ranges[0][k] )
						{
							tally = false;
							break;
						}
					}
					if(tally)
						cur = next(cur,i);
					out[j][i] = cur;
				}
			}
		}
		return out;
	}	
	
	public double last(int dimension)
	{
		double temp = Double.MAX_VALUE;
		for( int i = 0; i < this._ranges.length; i++)
		{
			if( _ranges[i][dimension] != Double.MAX_VALUE )
				temp = _ranges[i][dimension];
			else
				return temp;
		}
		return temp;
	}
	
	public int length(int dimension)
	{
		int out = Integer.MAX_VALUE;
		for( int i = 0; i < this._ranges.length; i++)
		{
			if( _ranges[i][dimension] != Double.MAX_VALUE )
				out = i;
			else
				return out+1;
		}
		return out+1;
	}
	
	public double next(double current, int dimension)
	{
		int cur = 0;
		for (int i = 0; i < this._ranges.length; i++)
		{
			if( this._ranges[i][dimension] == current )
				cur = i;
		}
		if (cur+1 == this._ranges.length || 
				this._ranges[cur+1][dimension] == Double.MAX_VALUE )
			return( this._ranges[0][dimension] );
		else
			return( this._ranges[cur+1][dimension] );

	}
}
