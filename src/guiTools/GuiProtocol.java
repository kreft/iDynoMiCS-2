/**
 * 
 */
package guiTools;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agent.Agent;
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
	
	// TODO quick fix, needs doing better
	private static int vertSize = 30;
	private static int horizSize = GroupLayout.DEFAULT_SIZE;
	
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
		
		
		Agent aAgent = new Agent();
		appendAspectInterface(aAgent);
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
		
		System.out.println("vert "+vertAI);
		System.out.println("horiz "+horizAI);
		
		/* click button to open this */
		JButton addAspect = newAspectButton(vertAI, horizAI);
		vertAI.addComponent(addAspect);
		horizAI.addComponent(addAspect);
		
		// NOTE dummy new aspect until I get the button working
		addNewAspectTo(vertAI, horizAI);
		
		verticalLayoutGroup.addGroup(vertAI);
		horizontalLayoutGroup.addGroup(horizAI);
	}
	
	private static class AspectListener implements ActionListener
	{
		private Group vertical;
		private Group horizontal;
		
		AspectListener(Group vert, Group horiz)
		{
			super();
			this.vertical = vert;
			this.horizontal = horiz;
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			// FIXME this doesn't work yet
			addNewAspectTo(this.vertical, this.horizontal);
			// FIXME this approach does add new Aspect boxes, but they creep
			// down the screen!
			//mainView.repaint();
			mainView.validate();
		}
	}
	
	public static JButton newAspectButton(Group vert, Group horiz)
	{
		JButton addAspect = new JButton("Add new aspect");
		addAspect.addActionListener(new AspectListener(vert, horiz));
		return addAspect;
	}
	
	public static void addNewAspectTo(Group vert, Group horiz)
	{
		// TODO for testing only, remove when working
		System.out.println("Adding new aspect to ");
		System.out.println("vert "+vert);
		System.out.println("horiz "+horiz);
		/* Set up the layout. */
		ParallelGroup vertAspect = layout.createParallelGroup();
		vert.addGroup(vertAspect);
		SequentialGroup horizAspect = layout.createSequentialGroup();
		horiz.addGroup(horizAspect);
		/* Aspect name. */
		JTextField nameLabel = newLabel();
		nameLabel.setText("Aspect name:");
		vertAspect.addComponent(nameLabel, vertSize, vertSize, vertSize);
		horizAspect.addComponent(nameLabel, horizSize, horizSize, horizSize);
		JTextField nameField = newField();
		vertAspect.addComponent(nameField, vertSize, vertSize, vertSize);
		horizAspect.addComponent(nameField, horizSize, horizSize, horizSize);
		/* Aspect type */
		// FIXME selection box
		// TODO Presumably, the options should be "primary", "calculated", etc?
		JTextField typeLabel = newLabel();
		typeLabel.setText("type");
		vertAspect.addComponent(typeLabel, vertSize, vertSize, vertSize);
		horizAspect.addComponent(typeLabel, horizSize, horizSize, horizSize);
		JTextField typeField = newField();
		vertAspect.addComponent(typeField, vertSize, vertSize, vertSize);
		horizAspect.addComponent(typeField, horizSize, horizSize, horizSize);
		/* Aspect value. */
		JTextField valueLabel = newLabel();
		valueLabel.setText("value");
		vertAspect.addComponent(valueLabel, vertSize, vertSize, vertSize);
		horizAspect.addComponent(valueLabel, horizSize, horizSize, horizSize);
		JTextField valueField = newField();
		vertAspect.addComponent(valueField, vertSize, vertSize, vertSize);
		horizAspect.addComponent(valueField, horizSize, horizSize, horizSize);
		// FIXME add button that passes the fields to the aspect interface
	}
	
	private static JTextField newLabel()
	{
		JTextField label = new JTextField();
		label.setEditable(false);
		label.setBackground(Color.LIGHT_GRAY);
		label.setFont(new Font("arial", Font.BOLD, 20));
		return label;
	}
	
	private static JTextField newField()
	{
		JTextField field = new JTextField();
		field.setEditable(true);
		field.setFont(new Font("arial", Font.PLAIN, 20));
		return field;
	}
}
