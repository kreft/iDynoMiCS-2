package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import dataIO.XmlExport;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class WriteXmlOutput extends ProcessManager
{

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
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		this._prefix = this.getString("prefix");
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/* Initiate new file. */
		this._xmlExport.newXml(this._prefix);
		this._xmlExport.writeState();
		this._xmlExport.closeXml();
		
	}
}
