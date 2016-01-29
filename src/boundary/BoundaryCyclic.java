package boundary;

import agent.AgentBoundary.CyclicBoundary;
import grid.GridBoundary.Cyclic;

public class BoundaryCyclic extends BoundaryConnected
{
	public BoundaryCyclic()
	{
		super();
		this._defaultGridMethod = new Cyclic();
		this._agentMethod = new CyclicBoundary();
	}
}