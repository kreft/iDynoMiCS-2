/**
 * 
 */
package boundary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.AgentBoundary.AgentMethod;
import dataIO.XmlLabel;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.GridBoundary.GridMethod;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class Boundary implements CanPrelaunchCheck, IsSubmodel, XMLable
{
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
	
	public void init(Element xmlElem)
	{
		Element xmlGrid;
		String variableName, className;
		GridMethod aGridMethod;
		NodeList gridNodes = xmlElem.getElementsByTagName("gridMethods");
		for ( int i = 0; i < gridNodes.getLength(); i++ )
		{
			xmlGrid = (Element) gridNodes.item(i);
			className = xmlGrid.getAttribute(XmlLabel.classAttribute);
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
	
	@Override
	public String getXml() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/
	
	public static Object getNewInstance(String className)
	{
		return XMLable.getNewInstance(className, "boundary.");
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
	@Override
	public Map<String, Class<?>> getParameters()
	{
		/* No parameters here. */
		return new HashMap<String, Class<?>>();
	}

	@Override
	public void setParameter(String name, String value)
	{
		/* No parameters here. */
	}

	@Override
	public List<SubmodelMaker> getSubmodelMakers()
	{
		// TODO GridMethod, AgentMethod
		return new LinkedList<SubmodelMaker>();
	}
	
}
