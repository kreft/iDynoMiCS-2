package agent.state;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;

public class StateLoader {
	
	/**
	 * Allows assignment of secondary states from the xml file
	 * @param the exact class name as defined in the agent.state.library package
	 * @return
	 */
	public static State getSecondary(String className, String inputStates)
	{
		Class<?> c;
		try {
			c = Class.forName("agent.state.library." + className);
			SecondaryState myState = (SecondaryState) c.newInstance();
			myState.setInput(inputStates);
			return myState;
		} catch (ClassNotFoundException e ){
			Feedback.out(LogLevel.QUIET,"ERROR: the class " + className + 
					" could not be found. Check the agent.state.library package"
					+ " for the existence of this class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException e)  {
			Feedback.out(LogLevel.QUIET,"ERROR: the class " + className + 
					" could not be accesed or instantieated. Check whether the "
					+ "called class is a valid State object.");
			e.printStackTrace();
		}
		return null;
	}
}
