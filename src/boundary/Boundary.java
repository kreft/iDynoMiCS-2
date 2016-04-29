/**
 * 
 */
package boundary;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.agent.AgentMethod;
import boundary.grid.GridMethod;
import dataIO.XmlLabel;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import utility.Helper;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Boundary implements CanPrelaunchCheck, IsSubmodel, XMLable
{
	// TODO move this to XmlLabel?
	public final static String DEFAULT_GM = "defaultGridMethod";
	
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
	/**
	 * The boundary this is connected with - not necessarily set.
	 */
	protected Boundary _partner;
	/**
	 * 
	 */
	protected String _partnerCompartmentName;
	/**
	 * List of Agents that are leaving this compartment via this boundary, and
	 * so need to travel to the connected compartment.
	 */
	protected List<Agent> _departureLounge = new LinkedList<Agent>();
	
	/**
	 * List of Agents that have travelled here from the connected compartment
	 * and need to be entered into this compartment.
	 */
	protected List<Agent> _arrivalsLounge = new LinkedList<Agent>();
	
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
	
	public String getName()
	{
		return XmlLabel.dimensionBoundary;
		// TODO return dimension and min/max?
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param aMethod
	 */
	public void setGridMethod(String soluteName, GridMethod aMethod)
	{
		// TODO safety if overwriting the default?
		if ( soluteName.equals(DEFAULT_GM) )
			this._defaultGridMethod = aMethod;
		else
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
	
	public void setPartner(Boundary partner)
	{
		this._partner = partner;
	}
	
	public boolean needsPartner()
	{
		return ( this._partnerCompartmentName != null ) &&
				( this._partner == null );
	}
	
	public String getPartnerCompartmentName()
	{
		return this._partnerCompartmentName;
	}
	
	public abstract Boundary makePartnerBoundary();
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param agent
	 */
	public void addOutboundAgent(Agent agent)
	{
		// TODO Safety: check if agent is already in list?
		this._departureLounge.add(agent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptInboundAgents(List<Agent> agents)
	{
		for ( Agent traveller : agents )
			this._arrivalsLounge.add(traveller);
	}
	
	/**
	 * 
	 */
	public void pushAllOutboundAgents()
	{
		if ( this._partner == null )
		{
			if ( ! this._departureLounge.isEmpty() )
			{
				// TODO throw exception? Error message to log?
			}
		}
		else
		{
			this._partner.acceptInboundAgents(this._departureLounge);
			this._departureLounge.clear();
		}
	}
	
	public List<Agent> getAllInboundAgents()
	{
		return this._arrivalsLounge;
	}
	
	public void clearArrivalsLoungue()
	{
		this._arrivalsLounge.clear();
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
	
	public static Boundary getNewInstance(String className)
	{
		return (Boundary) XMLable.getNewInstance(className,
											"boundary.BoundaryLibrary$");
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
								BoundaryLibrary.class.getDeclaredClasses());
	}
	
	@Override
	public List<InputSetter> getRequiredInputs()
	{
		// TODO GridMethod, AgentMethod
		return new LinkedList<InputSetter>();
	}
	
	
	public void acceptInput(String name, Object input)
	{
		// TODO
	}
	
	public static String extremeToString(int minMax)
	{
		return minMax == 0 ? "minimum" : "maximum";
	}
	
	public static int extremeToInt(String minMax)
	{
		return ( minMax.equals("minimum") ) ? 0 : 1;
			
	}
	
	public static class BoundaryMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = 6401917989904415580L;
		
		public BoundaryMaker(int minMax, Requirement req, IsSubmodel target)
		{
			super(extremeToString(minMax), req, target);
		}
		
		@Override
		public void doAction(ActionEvent e)
		{
			// TODO safety properly
			String bndryName;
			if ( e == null )
				bndryName = "";
			else
				bndryName = e.getActionCommand();
			Boundary bndry = (Boundary) Boundary.getNewInstance(bndryName);
			this.addSubmodel(bndry);
		}
		
		public Object getOptions()
		{
			return Boundary.getAllOptions();
		}
	}
}
