package agent.activity;

import agent.Agent;
import idynomics.Timer;

public class Conjugation extends Activity {
	
	public boolean prerequisites(Agent episome)
	{
		if ( (Boolean) episome.getState("isRepressed"))
			return false;
		if ( (Integer) episome.getState("copyNumberThreshold") <  // copyNumberThreshold may be stored as species parameter
				(Integer) episome.getState("getCopyNumber"))
			return false;

		if (Timer.getCurrentTime() - 
				(Double) episome.getState("getLastExchange") < 
				(Double) episome.getState("exchangeLag")) // exchangeLag may be stored as species parameter
			return false;

		if (Timer.getCurrentTime() - 
				(Double) episome.getState("getLastReception") < 
				(Double) episome.getState("receptionLag")) // receptionLag  may be stored as species parameter
			return false;		
		return true;
	}

	public void execute(Agent episome)
	{
		if (prerequisites(episome))
		{
			
		}
	}

	@Override
	public void execute(Agent agent, Agent secondActor) {
		// TODO Auto-generated method stub
		
	}
}
