package agent.state;

import agent.body.Body;

/**
 * TODO Rob [5Oct2015]: This is probably a temporary thing, as we could make
 * Body itself a State.
 * 
 */
public class BodyState extends State
{
	protected Body _body;
	
	@Override
	public Object get()
	{
		return this._body;
	}

	@Override
	public void set(Object newState) throws IllegalArgumentException
	{
		try
		{
			this._body = (Body) newState;
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(
							"BodyState state must be given as a Body object");
		}
	}

}
