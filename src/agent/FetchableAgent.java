package agent;

import generalInterfaces.Copyable;
import idynomics.Idynomics;

/**
 * Simplifies fetching agents when loading from protocol,
 * agent only needs to be found on first use.
 */
public class FetchableAgent implements Copyable {

    public FetchableAgent()
    {
        /* blank constructor is required for instantiable method */
    }

    public FetchableAgent(int identity)
    {
        _agentID = identity;
    }

    public FetchableAgent(Agent agent)
    {
        this._agent = agent;
        _agentID = agent.identity();
    }

    private int _agentID;

    private Agent _agent;

    public Agent get() {
        if (this._agent == null)
            this._agent = Idynomics.simulator.findAgent(_agentID);
        return _agent;
    }

    @Override
    public Object copy() {
        return new FetchableAgent(this._agent);
    }
}
