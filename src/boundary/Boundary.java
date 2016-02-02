/**
 * 
 */
package boundary;

import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.AgentBoundary.AgentMethod;
import generalInterfaces.CanPrelaunchCheck;
import grid.GridBoundary.GridMethod;
import shape.Shape;

/**
 * \brief Abstract class of boundary for a Compartment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class Boundary implements CanPrelaunchCheck
{
	/**
	 * The shape this Boundary takes (e.g. Plane, Sphere).
	 * 
	 * FIXME this should change to surface
	 */
	protected Shape _shape;
	
	/**
	 * The grid method this boundary should use for any variable that is not
	 * named in the dictionary {@link #_gridMethods}. 
	 */
	protected GridMethod _defaultGridMethod;
	
	/**
	 * Dictionary of grid methods that this boundary should use for each
	 * variable (e.g. a solute). If a variable is not in this list, use the
	 * default, {@link #_defaultGridMethod}, instead.
	 */
	protected HashMap<String,GridMethod> _gridMethods = 
											new HashMap<String,GridMethod>();
	
	/**
	 * The agent method this boundary should use for any agent. 
	 */
	protected AgentMethod _agentMethod;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public Boundary()
	{
		
	}
	
	public void init(Node xmlNode)
	{
		Element xmlBoundary = (Element) xmlNode;
		Element xmlGrid;
		String variableName, className;
		GridMethod aGridMethod;
		NodeList gridNodes = xmlBoundary.getElementsByTagName("gridMethods");
		for ( int i = 0; i < gridNodes.getLength(); i++ )
		{
			xmlGrid = (Element) gridNodes.item(i);
			className = xmlGrid.getAttribute("class");
			try
			{
				aGridMethod = (GridMethod) Class.forName(className).newInstance();
				aGridMethod.init(xmlGrid);
				if ( xmlGrid.hasAttribute("variable") )
				{
					variableName = xmlGrid.getAttribute("variable");
					this._gridMethods.put(variableName, aGridMethod);
				}
				else
					this._defaultGridMethod = aGridMethod;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aShape
	 */
	public void setShape(Shape aShape)
	{
		this._shape = aShape;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param aMethod
	 */
	public void setGridMethod(String soluteName, GridMethod aMethod)
	{
		this._gridMethods.put(soluteName, aMethod);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @return
	 */
	public GridMethod getGridMethod(String soluteName)
	{
		//System.out.println("Looking for "+soluteName); //bughunt
		if ( this._gridMethods.containsKey(soluteName) )
			return this._gridMethods.get(soluteName);
		else
			return this._defaultGridMethod;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param speciesName
	 * @param aMethod
	 */
	public void setAgentMethod(AgentMethod aMethod)
	{
		this._agentMethod = aMethod;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param speciesName
	 * @return
	 */
	public AgentMethod getAgentMethod()
	{
		return this._agentMethod;
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		if ( this._defaultGridMethod == null && this._gridMethods.isEmpty() )
			return false;
		return true;
	}
}
