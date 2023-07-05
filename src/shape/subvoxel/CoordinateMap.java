package shape.subvoxel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import generalInterfaces.Copyable;

/**
 * \brief Map of coordinates to {@code double} values, that is more
 * user-friendly than using {@code HashMap<int[],Double>}.
 * 
 * <p>The main purpose of this class is to deal with the issue that integer
 * arrays ({@code int[]}) with identical elements haver different hash codes.
 * However, it also serves as an opportunity to introduce some new, useful
 * methods.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CoordinateMap implements Copyable
{
	/**
	 * Map of coordinates to values. Use {@code List<Integer>} instead of
	 * {@code int[]} as it is behaves under hash code generation.
	 */
	private HashMap<List<Integer>,Double> _map = 
										new HashMap<List<Integer>,Double>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public CoordinateMap()
	{
		
	}
	
	@Override
	public Object copy()
	{
		CoordinateMap out = new CoordinateMap();
		/*
		 * Making deep copies of all keys and values is probably a little
		 * excessive, but "better safe than sorry".
		 */
		List<Integer> copyKey;
		for ( List<Integer> key : this._map.keySet() )
		{
			copyKey = new ArrayList<Integer>();
			for ( Integer i : key )
				copyKey.add(new Integer((int) i));
			out._map.put(copyKey, new Double((double) this._map.get(key)));
		}
		return out;
	}
	
	/*************************************************************************
	 * MAP METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if this map contains the coordinate <b>key</b> given.
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @return {@code boolean}: true if the <b>key</b> is recognised, false if
	 * it is not.
	 */
	public boolean containsKey(int[] coord)
	{
		return this._map.containsKey(getKey(coord));
	}
	
	/**
	 * \brief Get the value to which the given coordinate is mapped, or
	 * {@code null} if it is not in the key set.
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @return The {@code double} value mapped to this coordinate.
	 */
	public double get(int[] coord)
	{
		return this._map.get(getKey(coord));
	}
	
	/**
	 * \brief Create a mapping from the given coordinate to the given value.
	 * 
	 * <p>Note that this will overwrite any existing value mapped to
	 * <b>coord</b> - use {@link #increase(int[],double)} to increase the
	 * value at the coordinate.</p>
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @param value The {@code double} value to be mapped to this coordinate.
	 * @see #increase(int[], double)
	 */
	public void put(int[] coord, double value)
	{
		this._map.put(getKey(coord), value);
	}
	
	/**
	 * Remove all keys and values from this map.
	 */
	public void clear()
	{
		this._map.clear();
	}
	
	/**
	 * \brief Get the set of keys for this map, i.e. every coordinate that has
	 * had a value entered.
	 * 
	 * @return Set of all coordinate keys.
	 */
	public Collection<int[]> keySet()
	{
		Collection<int[]> out = new ArrayList<int[]>(this._map.size());
		for ( List<Integer> key : this._map.keySet() )
			out.add(getCoord(key));
		return out;
	}
	
	/*************************************************************************
	 * NEW METHODS
	 ************************************************************************/
	
	/**
	 * \brief Add the given <b>incrementValue</b> to the existing value
	 * associated with this coordinate. If the coordinate is not recognised,
	 * simply puts <b>incrementValue</b> as the value (i.e. assumes we start at
	 * zero).
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @param incrementValue Value by which to increase the value associated
	 * with <b>coord</b>.
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
	 * \brief Calculates and returns the total of all values in this map.
	 * 
	 * @return the total of all values in this map.
	 */
	public double getTotal()
	{
		double total = 0.0;
		for ( List<Integer> key : this._map.keySet() )
			total += this._map.get(key);
		return total;
	}
	
	/**
	 * \brief Scales all values in this map so that their sum is equal to
	 * <b>newTotal</b>, but their relative sizes stay the same.
	 * 
	 * @param newTotal New total value for this map.
	 * @see #scale()
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
	 * \brief Scales all values in this map so that their sum is equal to
	 * one, but their relative sizes stay the same.
	 * 
	 * @see #scale(double)
	 */
	public void scale()
	{
		this.scale(1.0);
	}
	
	/*************************************************************************
	 * COORD-KEY CONVERSION
	 ************************************************************************/
	
	/**
	 * \brief Generate a hash-friendly key for the given coordinate.
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @return {@code List} of {@code Integer}s, with the property that two
	 * such lists containing identical integers in the same order will have the
	 * same hash code.
	 */
	private static List<Integer> getKey(int[] coord)
	{
		List<Integer> out = new ArrayList<Integer>();
		for ( int i : coord )
			out.add(i);
		return out;
	}
	
	/**
	 * \brief Returns the integer array coordinate for the given map <b>key</b>.
	 * 
	 * @param key {@code List} of {@code Integer}s used as an internal key.
	 * @return One-dimensional array of {@code int}s, with the same elements as
	 * in <b>key</b>.
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
