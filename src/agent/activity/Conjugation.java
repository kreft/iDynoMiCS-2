package agent.activity;

import agent.Agent;
import idynomics.Timer;

public class Conjugation extends Activity {
	
	/*
	 * 
	 * 
	 */
	public boolean prerequisites(Agent[] agents)
	{
		if ( (Boolean) agents[0].getState("isRepressed"))
			return false;
		if ( (Integer) agents[0].getState("copyNumberThreshold") <  // copyNumberThreshold may be stored as species parameter
				(Integer) agents[0].getState("getCopyNumber"))
			return false;

		if (Timer.getCurrentTime() - 
				(Double) agents[0].getState("getLastExchange") < 
				(Double) agents[0].getState("exchangeLag")) // exchangeLag may be stored as species parameter
			return false;

		if (Timer.getCurrentTime() - 
				(Double) agents[0].getState("getLastReception") < 
				(Double) agents[0].getState("receptionLag")) // receptionLag  may be stored as species parameter
			return false;		
		return true;
	}

	public void execute(Agent[] agents, Double timestep)
	{
		if (prerequisites(agents))
		{
			
		}
	}

}
