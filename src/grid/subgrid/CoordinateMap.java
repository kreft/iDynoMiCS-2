package grid.subgrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * \brief Map of coordinates to {@code double} values, that is more
 * user-friendly than using {@code HashMap<int[],Double>}.
 * 
 * <p>The main purpose of this class is to deal with the issue that integer
 * arrays ({@code int[]}) with identical elements haver different hash codes.
 * However, it also serves as an opportunity to introduce some new, useful
 * methods.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class CoordinateMap
{
	/**
	 * 
	 */
	private HashMap<List<Integer>,Double> _map = 
										new HashMap<List<Integer>,Double>();
	
	/*************************************************************************
	 * MAP METHODS
	 ************************************************************************/
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	public boolean containsKey(int[] coord)
	{
		return this._map.containsKey(getKey(coord));
	}
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	public double get(int[] coord)
	{
		return this._map.get(getKey(coord));
	}
	
	/**
	 * 
	 * @param coord
	 * @param value
	 */
	public void put(int[] coord, double value)
	{
		this._map.put(getKey(coord), value);
	}
	
	
	
	/**
	 * 
	 */
	public void clear()
	{
		this._map.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<int[]> keySet()
	{
		Set<int[]> out = new HashSet<int[]>();
		for ( List<Integer> key : this._map.keySet() )
			out.add(getCoord(key));
		return out;
	}
	
	/*************************************************************************
	 * NEW METHODS
	 ************************************************************************/
	
	/**
	 * 
	 * @param coord
	 * @param incrementValue
	 */
	public void increase(int[] coord, double incrementValue)
	{
		List<Integer> key = getKey(coord);
		if ( this._map.containsKey(key) )
			this._map.put(key, this._map.get(key) + incrementValue);
		else
			this._map.put(key, incrementValue);
	}
	
	/**
	 * 
	 * @return
	 */
	public double getTotal()
	{
		double total = 0.0;
		for ( List<Integer> key : this._map.keySet() )
			total += this._map.get(key);
		return total;
	}
	
	/**
	 * 
	 * @param newTotal
	 */
	public void scale(double newTotal)
	{
		/* Find the multiplier, taking care not to divide by zero. */
		double multiplier = this.getTotal();
		if ( multiplier == 0.0 )
			return;
		multiplier = newTotal / multiplier;
		/* Now apply the multiplier. */
		for ( List<Integer> key : this._map.keySet() )
			this._map.put(key, this._map.get(key) * multiplier);
	}
	
	/**
	 * 
	 */
	public void scale()
	{
		this.scale(1.0);
	}
	
	/*************************************************************************
	 * COORD-KEY CONVERSION
	 ************************************************************************/
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	private static List<Integer> getKey(int[] coord)
	{
		List<Integer> out = new ArrayList<Integer>();
		for ( int i : coord )
			out.add(i);
		return out;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	private static int[] getCoord(List<Integer> key)
	{
		int n = key.size();
		int[] out = new int[n];
		for ( int i = 0; i < n; i++ )
			out[i] = key.get(i);
		return out;
	}
}
