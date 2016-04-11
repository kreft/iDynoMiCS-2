package modelBuilder;

import java.awt.event.ActionEvent;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.ObjectFactory;

/**
 * \brief Action that controls the setting of parameters in model components.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class ParameterSetter extends InputSetter
{
	private static final long serialVersionUID = -5316527782390799034L;
	/**
	 * String description of the class of this parameter. E.g., "Double",
	 * "Integer", "String".
	 */
	private String _classType;
	/**
	 * Current value of the parameter. Useful for setting defaults or for
	 * modifying XMl files. Will be {@code null} if not known.
	 */
	private Object _currentValue;
	
	// TODO default value? Could reset to this if the user changes their mind...
	
	/**
	 * \brief Construct a parameter setter where the current value is not known.
	 * 
	 * <p>Use {@link #ParameterSetter(String, IsSubmodel, String, Object)} if
	 * the current value is known.</p>
	 * 
	 * @param name Name for this parameter, often taken from the XmlLabel class.
	 * @param target The sub-model destined to accept this parameter.
	 * @param classType Description of the class of this parameter. E.g.,
	 * "Double", "Integer", "String".
	 */
	public ParameterSetter(String name, IsSubmodel target, String classType)
	{
		super(name, target);
		this._classType = classType;
	}
	
	/**
	 * \brief Construct a parameter setter where the current value is known.
	 * 
	 * @param name Name for this parameter, often taken from the XmlLabel class.
	 * @param target The sub-model destined to accept this parameter.
	 * @param classType Description of the class of this parameter. E.g.,
	 * "Double", "Integer", "String".
	 * @param currentValue Value of this parameter, should it be known. Use
	 * {@link #ParameterSetter(String, IsSubmodel, String)} if it is not known.
	 */
	public ParameterSetter(String name,
					IsSubmodel target, String classType, Object currentValue)
	{
		this(name, target, classType);
		this._currentValue = currentValue;
	}
	
	@Override
	public void doAction(ActionEvent e)
	{
		if ( e == null )
		{
			// TODO safety
		}
		String cmd = e.getActionCommand();
		Log.out(Tier.DEBUG,
					"Setting parameter \""+this.getName()+"\" as \""+cmd+"\"");
		this._currentValue = ObjectFactory.loadObject(cmd, this._classType);
		this.acceptInput(this._currentValue);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return {@code Object} representation of the current value of this
	 * parameter, or {@code null} if this is not known/set.
	 */
	public Object getCurrentValue()
	{
		return this._currentValue;
	}
	
	// TODO refresh current value?
}
