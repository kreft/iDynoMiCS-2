package agent.activity;

import agent.Agent;
import idynomics.Timer;

public class Conjugation extends Activity {
	
	// these parameters may be moved to the species parameters of the episome
	protected Double _exchangeLag;
	
	protected Double _receptionLag;
	
	protected int _copyNumberThreshold;
	
	protected Agent _episome;

	public boolean isTriggeredOnStep()
	{
		if (_episome.isRepressed())
			return false;
		if (_copyNumberThreshold < _episome.getCopyNumber())
			return false;

		if (Timer.getCurrentTime() - _episome.getLastExchange() < _exchangeLag)
			return false;

		if (Timer.getCurrentTime() - _episome.getLastReception() < _receptionLag)
			return false;		
		return true;
	}
	
	public boolean isTriggeredOnBirth()
	{
		return false;
	}
	
	public boolean isTriggeredOnDeath()
	{
		return false;
	}
	
	public void doyourthing()
	{
		
	}
	
	public Conjugation(Agent agent) {
		this._episome = agent;
		// TODO Auto-generated constructor stub
	}
}
