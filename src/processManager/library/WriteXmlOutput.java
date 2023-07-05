package processManager.library;

import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.XmlExport;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;

/**
 * TODO this should be a general class rather than a process manager
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class WriteXmlOutput extends ProcessManager
{
	
	public static String FILE_PREFIX = AspectRef.filePrefix;

	/**
	 * The SVG exporter.
	 */
	protected XmlExport _xmlExport = new XmlExport();

	/**
	 * The prefix for the file output path.
	 */
	protected String _prefix;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._prefix = this.getString(FILE_PREFIX);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep()
	{
		/* Initiate new file. */
		this._xmlExport.newXml(this._prefix);
		this._xmlExport.writeState();
		this._xmlExport.closeXml();
		
	}
}
