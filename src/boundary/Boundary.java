/**
 * 
 */
package boundary;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.agent.AgentMethod;
import boundary.grid.GridMethod;
import dataIO.XmlRef;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.Instantiatable;
import utility.Helper;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Boundary implements CanPrelaunchCheck, Instantiatable
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
		NodeList gridNodes = xmlElem.getElementsByTagName(XmlRef.gridMethod);
		for ( int i = 0; i < gridNodes.getLength(); i++ )
		{
			xmlGrid = (Element) gridNodes.item(i);
			className = xmlGrid.getAttribute(XmlRef.classAttribute);
			try
			{
				aGridMethod = (GridMethod) Class.forName(className).newInstance();
				aGridMethod.init(xmlGrid);
				if ( xmlGrid.hasAttribute(XmlRef.variable) )
				{
					variableName = xmlGrid.getAttribute(XmlRef.variable);
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
	 * TODO
	 * @return
	 */
	public String getName()
	{
		return XmlRef.dimensionBoundary;
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
	
	/**
	 * TODO
	 * @return
	 */
	public AgentMethod getAgentMethod()
	{
		return this._agentMethod;
	}
	
	/**
	 * TODO
	 * @param partner
	 */
	public void setPartner(Boundary partner)
	{
		this._partner = partner;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public boolean needsPartner()
	{
		return ( this._partnerCompartmentName != null ) &&
				( this._partner == null );
	}
	
	/**
	 * TODO
	 * @return
	 */
	public String getPartnerCompartmentName()
	{
		return this._partnerCompartmentName;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public abstract Boundary makePartnerBoundary();
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param agent
	 */
	public void addOutboundAgent(Agent anAgent)
	{
		this._agentMethod.addOutboundAgent(anAgent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 */
	public void acceptInboundAgent(Agent anAgent)
	{
		this._agentMethod.acceptInboundAgent(anAgent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptInboundAgents(List<Agent> agents)
	{
		this._agentMethod.acceptInboundAgents(agents);
	}
	
	/**
	 * TODO
	 */
	public void pushAllOutboundAgents()
	{
		if ( this._partner == null )
		{
			if ( this._agentMethod.hasOutboundAgents() )
			{
				// TODO throw exception? Error message to log?
			}
		}
		else
		{
			AgentMethod partnerMethod = this._partner.getAgentMethod();
			this._agentMethod.pushOutboundAgents(partnerMethod);
		}
	}
	
	// TODO delete once agent method gets full control of agent transfers
	public List<Agent> getAllInboundAgents()
	{
		return this._agentMethod.getAllInboundAgents();
	}
	
	// TODO delete once agent method gets full control of agent transfers
	public void clearArrivalsLoungue()
	{
		this._agentMethod.clearArrivalsLoungue();
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
		return (Boundary) Instantiatable.getNewInstance(className,
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

	/**
	 * TODO
	 * @param minMax
	 * @return
	 */
	public static String extremeToString(int minMax)
	{
		return minMax == 0 ? "minimum" : "maximum";
	}
	
	/**
	 * TODO
	 * @param minMax
	 * @return
	 */
	public static int extremeToInt(String minMax)
	{
		return ( minMax.equals("minimum") ) ? 0 : 1;
			
	}
	
	// FIXME to be replaced by modelnode paradigm?
//	@Override
//	public List<InputSetter> getRequiredInputs()
//	{
//		// TODO GridMethod, AgentMethod
//		return new LinkedList<InputSetter>();
//	}
//	
//	
//	public void acceptInput(String name, Object input)
//	{
//		// TODO
//	}
//	
//	public static class BoundaryMaker extends SubmodelMaker
//	{
//		private static final long serialVersionUID = 6401917989904415580L;
//		
//		public BoundaryMaker(int minMax, Requirement req, IsSubmodel target)
//		{
//			super(extremeToString(minMax), req, target);
//		}
//		
//		@Override
//		public void doAction(ActionEvent e)
//		{
//			// TODO safety properly
//			String bndryName;
//			if ( e == null )
//				bndryName = "";
//			else
//				bndryName = e.getActionCommand();
//			Boundary bndry = (Boundary) Boundary.getNewInstance(bndryName);
//			this.addSubmodel(bndry);
//		}
//		
//		public Object getOptions()
//		{
//			return Boundary.getAllOptions();
//		}
//	}
}
