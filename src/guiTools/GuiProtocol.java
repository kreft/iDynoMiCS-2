/**
 * 
 */
package guiTools;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import aspect.AspectInterface;
import dataIO.XmlHandler;

/**
 * \brief Class for viewing, editing and/or making a new protocol file in the
 * Graphical User Interface (GUI).
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiProtocol
{
	private static JPanel mainView;
	
	private static GroupLayout layout;
	
	private static SequentialGroup verticalLayoutGroup;
	private static ParallelGroup horizontalLayoutGroup;
	
	private static Document protocolDoc;
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static JComponent getProtocolEditor()
	{
		mainView = new JPanel();
		layout = new GroupLayout(mainView);
		verticalLayoutGroup = layout.createSequentialGroup();
		horizontalLayoutGroup = layout.createParallelGroup();
		mainView.setLayout(layout);
		
		
		return new JScrollPane(mainView,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public static void newFile()
	{
		/* Create a new file. */
		protocolDoc = XmlHandler.newDocument();
		/* Create the root element, "document". */
		Element rootElem = protocolDoc.createElement("document");
		protocolDoc.appendChild(rootElem);
		/* Create the simulator element and work down the tree. */
		Element simElem = protocolDoc.createElement("simulator");
		rootElem.appendChild(simElem);
		
		// NOTE Temporary fix until we make Simulator an AspectInterface
		Element timeElem = protocolDoc.createElement("timer");
		simElem.appendChild(timeElem);
		
		//for ( String aspectName : Timer.reg().getAllAspectNames() )
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAI
	 */
	private static void appendAspectInterface(AspectInterface anAI)
	{
		SequentialGroup vertAI = layout.createSequentialGroup();
		ParallelGroup horizAI = layout.createParallelGroup();
		for ( String aspectName : anAI.reg().getAllAspectNames() )
		{
			/* Create layouts for this Aspect. */
			ParallelGroup vertAspect = layout.createParallelGroup();
			SequentialGroup horizAspect = layout.createSequentialGroup();
			/* Aspect name. */
			JLabel nameLabel = new JLabel(aspectName);
			nameLabel.setFont(new Font("arial", Font.BOLD, 20));
			vertAspect.addComponent(nameLabel);
			horizAspect.addComponent(nameLabel);
			/* Aspect value. */
			JTextArea valueField = new JTextArea();
			valueField.setForeground(Color.BLACK);
			Object value = anAI.reg().getValue(anAI, aspectName);
			// TODO incorporate aspect restrictions
			if ( value == null )
				valueField.setBackground(Color.RED);
			else
			{
				valueField.setBackground(Color.WHITE);
				valueField.setText(value.toString());
			}
			/* Aspect description. */
			String description = anAI.reg().getDescription(aspectName);
			if ( description != null )
			{
				JLabel descLabel = new JLabel(aspectName);
				descLabel.setFont(new Font("arial", Font.ITALIC, 15));
				vertAspect.addComponent(descLabel);
				horizAspect.addComponent(descLabel);
			}
			/* Append the Aspect layouts to the AspectInterface layouts. */
			vertAI.addGroup(vertAspect);
			horizAI.addGroup(horizAspect);
		}
		verticalLayoutGroup.addGroup(vertAI);
		horizontalLayoutGroup.addGroup(horizAI);
	}
}
