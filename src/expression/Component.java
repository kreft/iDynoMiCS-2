/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public abstract class Component
{
	
	public abstract String getName();
	
	public abstract String reportValue(HashMap<String, Double> variables);
	
	public abstract double getValue(HashMap<String, Double> variables);
	
	public abstract Component getDifferential(String withRespectTo);
}