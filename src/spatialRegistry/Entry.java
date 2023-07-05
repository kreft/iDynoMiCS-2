package spatialRegistry;


import linearAlgebra.Vector;
import spatialRegistry.splitTree.SplitTree;

/**
 * Extends {@link Area} allowing for the coupling of a specific area in the
 * {@link SplitTree} with a given object {@link T}.
 * @author Bastiaan
 *
 * @param <T>
 */
public class Entry<T> extends Area
{
	
	private T entry;
	
	public Entry(double[] low, double[] high, boolean[] periodic, T entry)
	{
		super(low, high, Vector.copy(periodic));
		this.setEntry(entry);
	}

	public T getEntry() {
		return entry;
	}

	public void setEntry(T entry) {
		this.entry = entry;
	}

}