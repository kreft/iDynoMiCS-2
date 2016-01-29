package agent.event;

public class EventLoader {

	/**
	 * Allows assignment of secondary states from the xml file
	 * @param the exact class name as defined in the agent.state.secondary package.
	 * @return
	 */
	public static Event getEvent(String className, String inputStates)
	{
		Class<?> c;
		try {
			c = Class.forName("agent.event.library." + className);
			Event myEvent = (Event) c.newInstance();
			myEvent.setInput(inputStates);
			return myEvent;
		} catch (ClassNotFoundException e ){
			System.out.println("ERROR: the class " + className + " could not be found. Check the agent.event.library package for the existence of this class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException e)  {
			System.out.println("ERROR: the class " + className + " could not be accesed or instantieated. Check whether the called class is a valid Event object.");
			e.printStackTrace();
		}
		return null;
	}
}
