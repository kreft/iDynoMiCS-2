package shape;

import boundary.Boundary;

public final class ShapeDimension
{
	public enum DimName
	{
		X, Y, Z, R, THETA, PHI;
	}
	
	public static abstract class Dimension
	{
		protected double _length;
		
		public void setLength(double length)
		{
			// TODO check length >= 0
			this._length = length;
		}
		
		/**
		 * \brief Get the length of this dimension.
		 * 
		 * @return
		 */
		public double getLength()
		{
			return this._length;
		}
		
		/**
		 * \brief Get the shortest distance between two positions along this
		 * dimension.
		 * 
		 * @param a
		 * @param b
		 * @return
		 */
		public abstract double getShortest(double a, double b);
		
		public abstract double applyBoundary(double a);
		
		public abstract boolean isInside(double a);
	}
	
	public static class BoundedDimension extends Dimension
	{
		protected Boundary[] _boundaries = new Boundary[2];
		
		protected boolean[] _required = new boolean[2];
		
		public void setMinRequired()
		{
			this._required[0] = true;
		}
		
		public void setMaxRequired()
		{
			this._required[1] = true;
		}
		
		public void setMinBoundary(Boundary aBoundary)
		{
			this._boundaries[0] = aBoundary;
		}
		
		public void setMaxBoundary(Boundary aBoundary)
		{
			this._boundaries[1] = aBoundary;
		}
		
		public Boundary[] getBoundaries()
		{
			return this._boundaries;
		}
		
		public double getShortest(double a, double b)
		{
			return a - b;
		}
		
		public double applyBoundary(double a)
		{
			// TODO use length minus some tidy amount?
			return Math.max(0.0, Math.min(this._length, a));
		}
		
		public boolean isInside(double a)
		{
			return ( a >= 0.0 ) && ( a < this._length );
		}
	}
	
	public static class CyclicDimension extends Dimension
	{
		
		public double getShortest(double a, double b)
		{
			double out = a - b;
			if ( Math.abs(out) > 0.5 * this._length )
				out -= 0.5 * Math.signum(out) * this._length;
			return out;
		}
		
		public double[] getCyclicPoints(double a)
		{
			return new double[]{a - this._length, a + this._length};
		}
		
		public double applyBoundary(double a)
		{
			// TODO check this modulo behaves with negative numbers
			return a % this._length;
		}
		
		public boolean isInside(double a)
		{
			return true;
		}
	}
}