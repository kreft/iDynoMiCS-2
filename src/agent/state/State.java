package agent.state;

/**
 * \brief Wrapper for an state object.
 * 
 *
 */
public abstract class State
{
	public abstract Object get();
	
	public abstract void set(Object newState) throws IllegalArgumentException;
	
	public interface StatePredicate<T> {boolean test(State s);}
}
