package testJUnit;

import org.junit.Test;

import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Timer;
import processManager.ProcessManager;

public class CompartmentTest
{
	public class DummyProcess extends ProcessManager
	{
		@Override
		protected void internalStep(
				EnvironmentContainer environment, AgentContainer agents)
		{
			System.out.println("\nTime is: "+this._timeForNextStep);
			System.out.println(this._name);
		}
		
	}
	
	@Test
	public void processManagersShouldIterateInTurn()
	{
		// TODO make this a proper unit test
		/*
		 * Put some dummy ProcessManagers into a Compartment, and check they
		 * are stepped in the correct order.
		 */
		Compartment c = new Compartment();
		ProcessManager pm;
		Timer.setTimeStepSize(4.5);
		Timer.setEndOfSimulation(4.5);
		/* Frequent, low priority. */
		pm = new DummyProcess();
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(1.0);
		pm.setPriority(0);
		pm.setName("timestep 1.0, priority 0");
		c.addProcessManager(pm);
		/* Frequent, medium priority. */
		pm = new DummyProcess();
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(1.0);
		pm.setPriority(1);
		pm.setName("timestep 1.0, priority 1");
		c.addProcessManager(pm);
		/* Infrequent, high priority. */
		pm = new DummyProcess();
		pm.setTimeForNextStep(0.0);
		pm.setTimeStepSize(2.0);
		pm.setPriority(2);
		pm.setName("timestep 2.0, priority 2");
		c.addProcessManager(pm);
		/*
		 * 
		 */
		c.step();
	}
}
