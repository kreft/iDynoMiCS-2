package modelBuilder;

import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class SubmodelMaker extends AbstractAction
{
	private static final long serialVersionUID = 8084478264008177752L;
	
	private IsSubmodel _lastMadeSubmodel;
	
	private int _nMade = 0;
	
	private int _minToMake;
	
	private int _maxToMake;
	
	public SubmodelMaker(String name, int minToMake, int maxToMake)
	{
		super(name);
		this._minToMake = minToMake;
		this._maxToMake = maxToMake;
	}
	
	protected void increaseMakeCounter()
	{
		this._nMade++;
	}
	
	public boolean mustMakeMore()
	{
		return this._nMade < this._minToMake;
	}
	
	public boolean canMakeMore()
	{
		return this._nMade < this._maxToMake;
	}
	
	public String getName()
	{
		return (String) this.getValue(Action.NAME);
	}
	
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
