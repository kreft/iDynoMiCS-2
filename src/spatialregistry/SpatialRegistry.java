package spatialregistry;

import java.util.List;

public abstract class SpatialRegistry<T> {
	
	public abstract List<T> search(float[] coords, float[] dimension);
	public abstract List<T> cyclicsearch(float[] coords, float[] dimension);
	public abstract List<T> all();

}
