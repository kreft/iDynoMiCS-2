package modelBuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * \brief Abstract action class that controls the setting of input to model
 * components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class InputSetter extends AbstractAction
{
	private static final long serialVersionUID = 8870528967030074657L;
	
	/**
	 * The model component that will be given this input.
	 */
	private IsSubmodel _target;
	
	private List<ActionListener> _subsequentListeners = 
											new LinkedList<ActionListener>();
	
	// TODO description
	
	// TODO flexible options/restrictions setter & getter
	
	/**
	 * \brief Abstract constructor for an {@code AbstractAction} with a model
	 * component attached to it.
	 * 
	 *  @param name Name for this input, often taken from the XmlLabel class.
	 * @param target The sub-model destined to accept this input.
	 */
	public InputSetter(String name, IsSubmodel target)
	{
		super(name);
		this._target = target;
	}
	
	/**
	 * \brief Get the name of this action, that was set during construction.
	 * 
	 * @return {@code String} name.
	 */
	public String getName()
	{
		return (String) this.getValue(Action.NAME);
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Note that this method should be overridden by extensions that want
	 * to give a list of options. If no options are given, the constructor must
	 * be hard-coded into the actionPerformed() method.</p>
	 * 
	 * @return
	 */
	public Object getOptions()
	{
		return null;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean hasOptions()
	{
		return this.getOptions() != null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.doAction(e);
		for ( ActionListener al : this._subsequentListeners )
			al.actionPerformed(e);
	}
	
	protected abstract void doAction(ActionEvent e);
	
	/**
	 * \brief Process an {@code Object} as input. 
	 * 
	 * @param input An input to be passed to the sub-model this setter
	 * represents.
	 */
	protected void acceptInput(Object input)
	{
		this._target.acceptInput(this.getName(), input);
	}
	
	public void addListener(ActionListener al)
	{
		this._subsequentListeners.add(al);
	}
	
	/**
	 * \brief Call the {@code actionPerformed()} method from
	 * {@code AbstractAction} when there is only a {@code String}, and no
	 * {@code ActionEvent} to give it.
	 * 
	 * @param command {@code String} representation of the input {@code Object}.
	 */
	public void performAction(String command)
	{
		this.actionPerformed(new ActionEvent(this, 0, command));
	}
	
	/**
	 * \brief Construct an {@code ActionListener} that will trigger this
	 * setting action.
	 * 
	 * @param command {@code String} representation of the input {@code Object}.
	 * @return New {@code ActionListener} based on this <b>command</b>.
	 */
	public ActionListener getActionListener(String command)
	{
		return new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				performAction(command);
			}
		};
	}
}
