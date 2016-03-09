package modelBuilder;

import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class SubmodelMaker extends AbstractAction
{
	private static final long serialVersionUID = 8084478264008177752L;
	
	private IsSubmodel _lastMadeSubmodel;
	
	public SubmodelMaker(String name)
	{
		super(name);
	}
	
	public String getName()
	{
		return (String) this.getValue(Action.NAME);
	}
	
	public abstract SubmodelRequirement getRequirement();
	
	public abstract boolean makeImmediately();
	
	public String[] getClassNameOptions()
	{
		return null;
	}
	
	public IsSubmodel getLastMadeSubmodel()
	{
		return this._lastMadeSubmodel;
	}
	
	protected void setLastMadeSubmodel(IsSubmodel aSubmodel)
	{
		this._lastMadeSubmodel = aSubmodel;
	}
}
