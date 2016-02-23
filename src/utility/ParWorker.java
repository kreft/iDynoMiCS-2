package utility;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import idynomics.NameRef;
import surface.Collision;
import surface.Surface;

/**
 * 
 * @author baco
 *
 */
public class ParWorker  extends RecursiveAction 
{
	private static final long serialVersionUID = 1L;
	private AgentContainer _agentContainer;
	private int t, s, e;
	private int workSize = 200;
	
	/**
	 * create new worker with subset of tasks
	 * @param agentList
	 * @param agentContainer
	 * @param type
	 */
	public ParWorker(AgentContainer agentContainer)
	{
		this._agentContainer = agentContainer;
		this.t = 0;
		this.s = 0;
		this.e = agentContainer.getAllLocatedAgents().size();
	}
	
	private ParWorker(AgentContainer agentContainer, int s, int e)
	{
		this._agentContainer = agentContainer;
		this.t = 1;
		this.s = s;
		this.e = e;
	}

	/**
	 * compute on invoke
	 */
	@Override
	protected void compute() 
	{
		switch (t)
		{
		case 0 :
			// master thread
			ParWorker a = new ParWorker(_agentContainer, s, e);
			a.fork();
			a.join();
		break;
		case 1:
			if (worksplitter(t, workSize)) 
			{
				Collision iterator = new Collision(null, 
						_agentContainer.getShape());
				for(Agent agent: _agentContainer.getAllLocatedAgents().subList(s, e))
				{
					for(Agent neighbour: _agentContainer.treeSearch(
							((Body) agent.get(NameRef.agentBody)).getBoxes(0.0)))
					{
						if (agent.identity() > neighbour.identity())
						{
							iterator.collision((Surface) agent.get("surface"), 
									(Surface) neighbour.get("surface"));
						}
					}
					for(Surface s : _agentContainer.getShape().getSurfaces())
					{
						iterator.collision(s, (Surface) agent.get("surface"));
					}
				}
			}
		}
	}
	
	/**
	 * worker splits up task when it exceeds the limit
	 * @param type
	 * @param lim
	 * @return
	 */
	private boolean worksplitter(int type, int lim) 
	{
		int listSize = s-e;
		if ( listSize > lim) 
		{
			ParWorker a = new ParWorker(_agentContainer, s, s+listSize/2);
			a.fork();
			ParWorker b = new ParWorker(_agentContainer, (listSize/2)+1, 
					listSize);
			b.compute();
			a.join();
			return false;
		}
		return true;
	}

}
