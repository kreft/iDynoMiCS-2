package nodeFactory;

import nodeFactory.ModelNode.Requirements;

public class Constructable {
	
	protected String _classRef;
	
	protected String[] _options;
	
	protected Requirements _requirement;

	public Constructable(String classRef, Requirements requirement) 
	{
		init(classRef, null, requirement);
	}

	public Constructable(String classRef, String[] classRefs, 
			Requirements requirement) 
	{
		init(classRef, classRefs, requirement);
	}
	
	private void init(String classRef, String[] classRefs, 
			Requirements requirement)
	{
		this._classRef = classRef;
		this._options = classRefs;
		this._requirement = requirement;
	}

	public String classRef() 
	{
		return this._classRef;
	}

	public Requirements requirement() 
	{
		return this._requirement;
	}

	public String[] options() 
	{
		return this._options;
	}

}
