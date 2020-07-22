package bookkeeper;

import java.util.LinkedList;

import bookkeeper.KeeperEntry.EventType;
import dataIO.CsvExport;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;


public class Bookkeeper implements Settable 
{
	
	private Settable _parent = null;
	
	private LinkedList<KeeperEntry> _entries = new LinkedList<KeeperEntry>();
	
	
	public void clear()
	{
		this._entries = new LinkedList<KeeperEntry>();
	}
	
	public void register(EventType eventType, String event,
			String identity, String value, Settable storedSettable)
	{
		this._entries.add(new KeeperEntry(this, eventType, event,
			identity, value, storedSettable));
	}
	
	public void toFile(String compartmentPrefix)
	{
		CsvExport output = new CsvExport();
		output.createFile(compartmentPrefix + "_registered_events");
		if( !_entries.isEmpty() )
		{
			output.writeLine(this._entries.getFirst().getModule().getHeader());
			for ( KeeperEntry e : this._entries )
				output.writeLine( e.getModule().getCSV());
		}
		output.closeFile();
	}

	@Override
	public Module getModule() 
	{
		/* The bookkeeper node. */
		Module modelNode = new Module(defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		
		/* Add entries as child nodes. */
		for ( KeeperEntry e : this._entries )
			modelNode.add( e.getModule() );

		return modelNode;	
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.bookkeeper;
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
