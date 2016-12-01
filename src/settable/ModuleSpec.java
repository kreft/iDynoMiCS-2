package settable;

import referenceLibrary.ClassRef;
import settable.Module.Requirements;

public class ModuleSpec {
	
	protected String _classRef;
	
	protected String _label;
	
	protected String[] _options;
	
	protected Requirements _requirement;

	public ModuleSpec(String classRef, Requirements requirement) 
	{
		set(classRef, null, requirement, ClassRef.simplify( classRef ) );
	}
	
	public ModuleSpec(String classRef, Requirements requirement, String label) 
	{
		set(classRef, null, requirement, label);
	}

	public ModuleSpec(String classRef, String[] classRefs, 
			Requirements requirement) 
	{
		set(classRef, classRefs, requirement, ClassRef.simplify( classRef ) );
	}
	
	private void set(String classRef, String[] classRefs, 
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
