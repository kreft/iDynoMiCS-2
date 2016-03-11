package modelBuilder;

import java.awt.event.ActionEvent;

import dataIO.ObjectFactory;

public class ParameterSetter extends InputSetter
{
	private static final long serialVersionUID = -5316527782390799034L;
	
	private String _classType;
	
	public ParameterSetter(String name, IsSubmodel target, String classType)
	{
		super(name, target);
		this._classType = classType;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ( e == null )
		{
			// TODO safety
		}
		String cmd = e.getActionCommand();
		this.addInput(ObjectFactory.loadObject(cmd, this._classType));
	}
}
