package test.junit.oldTests;

import org.junit.Test;

import compartment.Compartment;
import processManager.ProcessManager;
import settable.Settable;
import test.OldTests;
/**
 * \brief Test checking that Compartments behave as they should.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CompartmentTest
{
	public class DummyProcess extends ProcessManager
	{
		@Override
		protected void internalStep()
		{
			System.out.println("\nTime is: "+this._timeForNextStep);
			System.out.println(this._name);
		}

		@Override
		public void setParent(Settable parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Test
	public void processManagersShouldIterateInTurn()
	{
		// TODO make this a proper unit test
		double tStep = 4.5;
		double tMax = 4.5;
		
		OldTests.setupSimulatorForTest(tStep, tMax, "processManagersShouldIterateInTurn");
		/*
		 * Put some dummy ProcessManagers into a Compartment, and check they
		 * are stepped in the correct order.
		 */
		Compartment c = new Compartment();
		ProcessManager pm;
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
