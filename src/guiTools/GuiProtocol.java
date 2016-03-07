/**
 * 
 */
package guiTools;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

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

import aspect.Aspect;
import aspect.AspectInterface;
import dataIO.XmlHandler;
import idynomics.Simulator;

/**
 * \brief Class for viewing, editing and/or making a new protocol file in the
 * Graphical User Interface (GUI).
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiProtocol
{
	/**
	 * 
	 * TODO Rob [7Mar2016]: this is a quick fix and needs further discussion
	 * about how it could be done better!
	 */
	public static enum ModuleRequirement
	{
		EXACTLY_ONE,
		
		ZERO_OR_ONE,
		
		ZERO_TO_MANY,
		
		ONE_TO_MANY
	}
	
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
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		verticalLayoutGroup = layout.createSequentialGroup();
		horizontalLayoutGroup = layout.createParallelGroup();
		layout.setVerticalGroup(verticalLayoutGroup);
		layout.setHorizontalGroup(horizontalLayoutGroup);
		mainView.setLayout(layout);
		mainView.setVisible(true);
		
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
		
		Simulator aSim = new Simulator();
		HashMap<AspectInterface,ModuleRequirement> modules = aSim.getModules();
		for (AspectInterface anAI : modules.keySet() )
		{
			switch (modules.get(anAI))
			{
				case EXACTLY_ONE:
					appendAspectInterface(anAI);
					break;
				// TODO
				default:
					break;
			}
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAI
	 */
	private static void appendAspectInterface(AspectInterface anAI)
	{
		/* Make and add the layout groups for this AspectInterface. */ 
		SequentialGroup vertAI = layout.createSequentialGroup();
		verticalLayoutGroup.addGroup(vertAI);
		ParallelGroup horizAI = layout.createParallelGroup();
		horizontalLayoutGroup.addGroup(horizAI);
		// TODO Name of the AspectInterface, e.g Timer?
		for ( String aspectName : anAI.reg().getAllAspectNames() )
		{
			// TODO Something's not quite right here... getValue should be
			// returning the value, not the Aspect object
			Object value = 
						(Object) anAI.reg().getValue(anAI, aspectName);
//			String description = anAspect.description;
			/* Create and add layout groups for this Aspect. */
			ParallelGroup vertAspect = layout.createParallelGroup();
			vertAI.addGroup(vertAspect);
			SequentialGroup horizAspect = layout.createSequentialGroup();
			horizAI.addGroup(horizAspect);
			/* Aspect name. */
			JLabel nameLabel = new JLabel(aspectName);
			nameLabel.setFont(new Font("arial", Font.BOLD, 20));
			vertAspect.addComponent(nameLabel);
			horizAspect.addComponent(nameLabel);
			/* Aspect value. */
			JTextArea valueField = new JTextArea();
			valueField.setForeground(Color.BLACK);
			// TODO incorporate aspect restrictions
			if ( value == null )
			{
				valueField.setBackground(Color.RED);
				valueField.setText("");
			}
			else
			{
				valueField.setBackground(Color.WHITE);
				valueField.setText(value.toString());
			}
			// TODO set the valueField preferred sizes here
			vertAspect.addComponent(valueField, 30, 30, 30);
			horizAspect.addComponent(valueField, 60, 60, 60);
			/* Aspect description. */
//			if ( description != null )
//			{
//				JLabel descLabel = new JLabel(description);
//				descLabel.setFont(new Font("arial", Font.ITALIC, 15));
//				vertAspect.addComponent(descLabel);
//				horizAspect.addComponent(descLabel);
//			}
			// FIXME make it so that aspect values are updated when this is
			// saved!
		}
	}
}
