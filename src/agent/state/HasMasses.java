package agent.state;

import java.util.HashMap;

public interface HasMasses
{
	public abstract double getTotal();
	
	public abstract double getVolume(HashMap<String,Double> densities);
}
