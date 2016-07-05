package nodeFactory;

import nodeFactory.ModelNode.Requirements;
import referenceLibrary.ClassRef;

public class Constructable {
	
	protected String _classRef;
	
	protected String _label;
	
	protected String[] _options;
	
	protected Requirements _requirement;

	public Constructable(String classRef, Requirements requirement) 
	{
		init(classRef, null, requirement, ClassRef.simplify( classRef ) );
	}
	
	public Constructable(String classRef, Requirements requirement, String label) 
	{
		init(classRef, null, requirement, label);
	}

	public Constructable(String classRef, String[] classRefs, 
			Requirements requirement) 
	{
		init(classRef, classRefs, requirement, ClassRef.simplify( classRef ) );
	}
	
	private void init(String classRef, String[] classRefs, 
			Requirements requirement, String label)
	{
		this._classRef = classRef;
		this._options = classRefs;
		this._requirement = requirement;
		this._label = label;
	}

	public String classRef() 
	{
		return this._classRef;
	}
	
	public String label() 
	{
		return this._label;
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
