package bookkeeper;

import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;

public class KeeperEntry implements Settable 
{
	
	public enum EventType
	{
		
		REMOVED( 101 ),
		TRANSFER( 102 ),
		ARRIVE( 103 ),
		REACTION( 201 ),
		ODE( 202 ),
		OTHER( 300 ),
		TEST( 999 );
		
		public final int iD;
		
		EventType(int eventID)
		{
			this.iD = eventID;
		}
	}
	
	private Settable _parent;
	private EventType _type;
	private String _event;
	private String _identity;
	private String _value;
	private Settable _settable;
	
	public KeeperEntry(Settable parent, EventType eventType, String event,
			String identity, String value, Settable storedSettable)
	{
		this._parent = parent;
		this._type = eventType;
		this._event = event;
		this._identity = identity;
		this._value = value;
		this._settable = storedSettable;
	}
	
	@Override
	public Module getModule() 
	{
		/* The bookkeeper node. */
		Module modelNode = new Module(defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.add( new Attribute(XmlRef.eventID, 
				String.valueOf( this._type.iD ), null, true ) );
		
		modelNode.add( new Attribute(XmlRef.identity, 
				this._identity, null, true ) );

		modelNode.add( new Attribute(XmlRef.event, 
				String.valueOf( this._event ), null, true ) );
		
		modelNode.add( new Attribute(XmlRef.valueAttribute, 
				String.valueOf( this._value ), null, true ) );

		if( this._settable != null )
			modelNode.add( this._settable.getModule() );
		
		return modelNode;	
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.keeperEntry;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parent = parent;
	}

	@Override
	public Settable getParent() 
	{
		return this._parent;
	}

}
