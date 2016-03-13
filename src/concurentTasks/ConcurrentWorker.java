package concurentTasks;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * work in progress, most iDynomics elements are not suitable (yet) for
 * concurrent acces
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ConcurrentWorker  extends RecursiveAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6134110076222352391L;

	final static ForkJoinPool pool = new ForkJoinPool(8);
	final static int workSize = 6;

	private ConcurrentTask task;

	private boolean concurrent = true;

	public ConcurrentWorker(ConcurrentTask task)
	{
		setTask(task);
	}
	
	public ConcurrentWorker()
	{
		
	}
	
	public void setTask(ConcurrentTask task)
	{
		this.task = task;
	}
	
	public void executeTask(ConcurrentTask task)
	{
		setTask(task);
		if (! this.concurrent )
			task.task();
		else
			pool.invoke(new ConcurrentWorker(this.task));
	}
	
	public void concurrentEnabled(boolean bool)
	{
		this.concurrent = bool;
	}

	/**
	 * compute on invoke
	 */
	@Override
	protected void compute() 
	{
		if ( worksplitter() ) 
			task.task();
	}
	
	/**
	 * worker splits up task when it exceeds the limit
	 */
	private boolean worksplitter() 
	{
		int listSize = task.size();
		if ( listSize > workSize) 
		{
			invokeAll(new ConcurrentWorker(task.part(0, listSize/2)),
					new ConcurrentWorker(task.part((listSize/2)+1, listSize)));
			return false;
		}
		return true;
	}

}
