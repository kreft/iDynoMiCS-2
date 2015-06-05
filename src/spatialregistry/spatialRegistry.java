package spatialregistry;

import java.util.List;

public abstract class spatialRegistry<T> {
	
	public abstract List<T> search(float[] coords, float[] dimension);
	public abstract List<T> cyclicsearch(float[] coords, float[] dimension);
	public abstract List<T> all();
	public abstract void insert(float[] coords, float[] dimensions, T entry);

}
