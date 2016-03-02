/**
 * 
 */
package guiTools;

import javax.swing.JScrollPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dataIO.XmlHandler;

/**
 * \brief Class for viewing, editing and/or making a new protocol file in the
 * Graphical User Interface (GUI).
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiProtocol
{
	private static JScrollPane mainView;
	
	private static Document protocolDoc;
	
	public static void newFile()
	{
		/* Create a new file. */
		protocolDoc = XmlHandler.newDocument();
		/* Create the root element, "document". */
		Element rootElem = protocolDoc.createElement("document");
		protocolDoc.appendChild(rootElem);
		/* Create the simulator element and work down the tree. */
		Element simElem = protocolDoc.createElement("simulator");
		protocolDoc.appendChild(simElem);
	}
	
	
}
