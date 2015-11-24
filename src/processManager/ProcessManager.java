package processManager;

import java.util.Collection;

import boundary.Boundary;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public interface ProcessManager
{

	// should the constructor be in the interface?
	
	public void init();

	public String getName();
	
	public void setPriority(int priority);
	
	public int getPriority();
	
	public void setTimeForNextStep(double newTime);
	
	public double getTimeForNextStep();
	
	public void setTimeStepSize(double newStepSize);
	
	public double getTimeStepSize();

	public void showBoundaries(Collection<Boundary> boundaries);

	public void step(EnvironmentContainer environment, AgentContainer agents);
	
	public void internalStep(EnvironmentContainer environment,
											AgentContainer agents);

	public StringBuffer report();
	
}