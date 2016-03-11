package modelBuilder;

import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;

import dataIO.XmlLabel;

public abstract class SubmodelMaker extends AbstractAction
{
	private static final long serialVersionUID = 8084478264008177752L;
	
	private IsSubmodel _target;
	
	/**
	 * List of all sub-models made by this maker, in the order they were made.
	 */
	private LinkedList<IsSubmodel> _submodelsMade;
	
	private int _minToMake;
	
	private int _maxToMake;
	
	public SubmodelMaker(String name, Requirement req, IsSubmodel target)
	{
		super(name);
		this._minToMake = req._min;
		this._maxToMake = req._max;
		this._target = target;
		this._submodelsMade = new LinkedList<IsSubmodel>();
	}
	
	public boolean mustMakeMore()
	{
		return this._submodelsMade.size() < this._minToMake;
	}
	
	public boolean canMakeMore()
	{
		return this._submodelsMade.size() < this._maxToMake;
	}
	
	public String getName()
	{
		return (String) this.getValue(Action.NAME);
	}
	
	public String[] getClassNameOptions()
	{
		return null;
	}
	
	public IsSubmodel getLastMadeSubmodel()
	{
		return this._submodelsMade.getLast();
	}
	
	protected void addSubmodel(IsSubmodel aSubmodel)
	{
		this._target.acceptInput(this.getName(), aSubmodel);
		this._submodelsMade.add(aSubmodel);
	}
	
	/**
	 * \brief Clear way of specifying exactly how many sub-model instances may
	 * be made.
	 */
	public static enum Requirement
	{
		EXACTLY_ONE(1, 1),
		
		ZERO_OR_ONE(0, 1),
		
		ONE_TO_MANY(1, Integer.MAX_VALUE),
		
		ZERO_TO_MANY(0, Integer.MAX_VALUE);
		
		private final int _min, _max;
		
		Requirement(int min, int max)
		{
			_min = min;
			_max = max;
		}
	}
}
