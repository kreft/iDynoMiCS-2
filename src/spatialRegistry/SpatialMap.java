package spatialRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;

/**
 * The SpatialMap stores objects of type V for an integer defined coordinate
 * system. The SpatialMap allows only a single object to be stored in one
 * spatial position end thus prevents multiple entries at the same coordinates.
 * This is a safe alternative to a HashMap using keys of type int[] which can
 * be tested as unequal with identical values.
 * 
 * SpatialMap has similar functionality as Coordinate map but extends the
 * HasMap rather then encapsulating it. This approach allows generic type 
 * declaration and thus generalizes the code allowing any object to be stored. 
 * This class replaces the ObjectMatrix for use with the analysis rasterizer.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 *
 * @param <V> declared object type
 */
public class SpatialMap<V> extends HashMap<String, V> 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4494319866051027554L;
	
	/**
	 * if true only accept strictly formated coordinates as key, if false
	 * SpatialMap also allows entries with a simple String key.
	 */
	private boolean _strict = false;
	
	public SpatialMap() 
	{
		
	}
	
	public SpatialMap(boolean strict) 
	{
		this._strict = strict;
	}
	
	/*************************************************************************
	 * MAP METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if this map contains the coordinate <b>key</b> given.
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @return {@code boolean}: true if the <b>key</b> is recognized, false if
	 * it is not.
	 */
	public boolean containsKey(int[] coord)
	{
		return this.containsKey(Vector.toString(coord));
	}
	
	/**
	 * \brief Get the value to which the given int[] is mapped, or
	 * {@code null} if it is not in the key set.
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @return The entry mapped to this coordinate.
	 */
	public V get(int[] coord)
	{
		return this.get(Vector.toString(coord));
	}
	
	/**
	 * \brief Create a mapping from the given int[] to the given value.
	 * 
	 * <p>Note that this will overwrite any existing value mapped to
	 * <b>coord</b>
	 * 
	 * @param coord One-dimensional array of {@code int}s, of any length.
	 * @param value The entry to be mapped to this coordinate.
	 */
	public V put(int[] coord, V value)
	{
		return this.put(Vector.toString(coord), value);
	}

	@Override
	public V put(String coord, V value)
	{
		if (_strict) {
			try 
			{
				Vector.intFromString(coord);
			}
		 	catch (NumberFormatException e) 
			{
		 		Log.out(Tier.CRITICAL, "SpatialMap only allows int[] keys"
		 				+ "in strict mode.");
			    return null;
			}
		}
		return super.put(coord, value);
	}
	/**
	 * \brief Get the set of keys in int[] format for this map.
	 * 
	 * @return Set of all keys in int[] format.
	 */
	public Set<int[]> keySetNumeric()
	{
		Set<int[]> out = new HashSet<int[]>();
		for ( String key : this.keySet() )
			out.add(Vector.intFromString(key));
		return out;
	}
}
