package spatialRegistry.splitTree;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Testing potential further speed ups
 * 
 * NOTE do not use this method, it is not thread safe, just for testing purposes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ParallelBuild<T> extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -767857163097668802L;

	static ForkJoinPool pool = new ForkJoinPool(8,
			ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true);
	
	
	Node<T> _node;
	
	public ParallelBuild(Node<T> node)
	{
		this._node = node;
	}

	@Override
	protected void compute()
	{
		_node.cull();
	}
	
}
