package modelBuilder;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class InputSetter extends AbstractAction
{
	private static final long serialVersionUID = 8870528967030074657L;
	
	private IsSubmodel _target;
	
	// TODO description
	
	// TODO flexible options/restrictions setter & getter
	
	// TODO default value
	
	public InputSetter(String name, IsSubmodel target)
	{
		super(name);
		this._target = target;
	}
	
	public String getName()
	{
		return (String) this.getValue(Action.NAME);
	}
	
	protected void addInput(Object input)
	{
		this._target.acceptInput(this.getName(), input);
	}
	
}
