package modelBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * \brief An abstract Action that controls the creation of a sub-model, i.e.
 * some part of a more general model.
 * 
 * <p>Builds on {@code InputSetter} by keeping a record of all inputs (i.e.
 * sub-model instances) it has created, so that it can control their numbers by
 * use of {@code Requirement}.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class SubmodelMaker extends InputSetter
{
	private static final long serialVersionUID = 8084478264008177752L;
	
	/**
	 * List of all sub-models made by this maker, in the order they were made.
	 */
	private LinkedList<IsSubmodel> _submodelsMade;
	
	/**
	 * Minimum/maximum limits on the number of sub-model instances this maker
	 * is allowed to make.
	 */
	private int _minToMake, _maxToMake;
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param req
	 * @param target
	 */
	public SubmodelMaker(String name, Requirement req, IsSubmodel target)
	{
		super(name, target);
		this._minToMake = req._min;
		this._maxToMake = req._max;
		this._submodelsMade = new LinkedList<IsSubmodel>();
	}
	
	/**
	 * \brief Check if more instances of this maker's sub-model <b>must</b> be
	 * made.
	 * 
	 * @return {@code boolean}: true if more are required, false if enough have
	 * been made already.
	 */
	public boolean mustMakeMore()
	{
		return this._submodelsMade.size() < this._minToMake;
	}
	
	/**
	 * \brief Check if more instances of this maker's sub-model <b>may</b> be
	 * made.
	 * 
	 * @return {@code boolean}: true if more are permitted, false if the
	 * maximum have been made already.
	 */
	public boolean canMakeMore()
	{
		return this._submodelsMade.size() < this._maxToMake;
	}
	
	/**
	 * \brief Check if it is possible to make multiple sub-models through this
	 * maker.
	 * 
	 * @return {@code boolean}: true if multiple sub-model instances permitted,
	 * false if not.
	 */
	public boolean canMakeMultiples()
	{
		return this._maxToMake > 1;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public List<IsSubmodel> getAllSubmodelsMade()
	{
		return this._submodelsMade;
	}
	
	/**
	 * \brief Get the last sub-model instance to be made by this maker.
	 * 
	 * @return {@code IsSubmodel} instance.
	 */
	public IsSubmodel getLastMadeSubmodel()
	{
		return this._submodelsMade.getLast();
	}
	
	/**
	 * \brief Add a newly-created instance of this sub-model to the target
	 * model, and to the list of sub-models created by this maker.
	 * 
	 * @param aSubmodel Newly-created instance of this sub-model.
	 */
	protected void addSubmodel(IsSubmodel aSubmodel)
	{
		this.acceptInput(aSubmodel);
		this._submodelsMade.add(aSubmodel);
	}
	
	/**
	 * \brief Clear way of specifying exactly how many sub-model instances may
	 * be made.
	 */
	public static enum Requirement
	{
		/**
		 * Exactly one instance of this sub-model may be made: no more, no fewer.
		 */
		EXACTLY_ONE(1, 1),
		/**
		 * This submodel is optional, but if it is made then only one instance
		 * is permitted.
		 */
		ZERO_OR_ONE(0, 1),
		/**
		 * There must be at least one instance of this sub-model made, but there
		 * is no upper limit.
		 */
		ONE_TO_MANY(1, Integer.MAX_VALUE),
		/**
		 * Any number of instances of this sub-model are permitted, even none at
		 * all.
		 */
		ZERO_TO_MANY(0, Integer.MAX_VALUE);
		
		/*
		 * Note that the use of Integer.MAX_VALUE is due to the lack of an
		 * "infinity" in Integers (as exists in Double). However, at a value of 
		 * (2^31 - 1) > 2 billion, Integer.MAX_VALUE is should survive most
		 * usages.
		 */
		
		private final int _min, _max;
		
		Requirement(int min, int max)
		{
			_min = min;
			_max = max;
		}
	}
}
