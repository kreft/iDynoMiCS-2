package modelBuilder;

import java.util.LinkedHashMap;

import javax.swing.AbstractAction;

public interface IsSubmodel
{
	public LinkedHashMap<String, Class<?>> getAttributes();
	
	public LinkedHashMap<AbstractAction,SubmodelRequirement>
													getAllSubmodelMakers();
	
	public IsSubmodel getLastMadeSubmodel();
	
}
