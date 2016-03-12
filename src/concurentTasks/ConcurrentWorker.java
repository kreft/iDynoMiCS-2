package concurentTasks;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ConcurrentWorker  extends RecursiveAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6134110076222352391L;

	private static ForkJoinPool pool = new ForkJoinPool(8);
	private static int workSize = 30;

	protected ConcurrentTask task;
	protected int s, e;

	
	/**
	 * create new worker with subset of tasks
	 * @param agentList
	 * @param agentContainer
	 * @param type
	 */
	public ConcurrentWorker(ConcurrentTask task)
	{
		set(task);
	}
	
	private ConcurrentWorker(ConcurrentTask task, int s, int e)
	{
		this.task = task;
		this.s = s;
		this.e = e;
	}
	
	public ConcurrentWorker()
	{
		
	}
	
	public void set(ConcurrentTask task)
	{
		this.task = task;
		this.s = 0;
		this.e = task.size();	
	}
	
	public void executeTask()
	{
		pool.invoke(new ConcurrentWorker(task, s, e));
	}
	
	public void executeTask(ConcurrentTask task)
	{
		set(task);
		executeTask();
//		task.task(s, e);
	}

	/**
	 * compute on invoke
	 */
	@Override
	protected void compute() 
	{
		if (worksplitter()) 
			task.task(s, e);
	}
	
	/**
	 * worker splits up task when it exceeds the limit
	 * @param type
	 * @param lim
	 * @return
	 */
	private boolean worksplitter() 
	{
		int listSize = s-e;
		if ( listSize > workSize) 
		{
			ConcurrentWorker a = new ConcurrentWorker(task, s, s+listSize/3);
			ConcurrentWorker b = new ConcurrentWorker(task, (listSize/3)+1, 
					listSize);
			invokeAll(a,b);
			return false;
		}
		return true;
	}

}
