package concurentTasks;

/**
 * Concurrent tasks can be used in combination with concurrent workers to 
 * distribute work over multiple daemon threads in order to use the
 * computational power of multi-core machines.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface ConcurrentTask {
	
	public void task(int start, int end);
	
	public int size();
}
