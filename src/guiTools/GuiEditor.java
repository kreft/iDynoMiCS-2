package guiTools;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dataIO.XmlLabel;
import idynomics.GuiLaunch;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiEditor
{
	/**
	 * Hashmap of all gui TextAreas associated with their ModelAttribute
	 */
	static HashMap<ModelAttribute,JTextArea> attributes = 
			new HashMap<ModelAttribute,JTextArea>();
	
	/**
	 * Hashmap of all gui TextAreas associated with their ModelAttribute
	 */
	static HashMap<ModelAttribute,JComboBox> attributeSelectors = 
			new HashMap<ModelAttribute,JComboBox>();
	
	/**
	 * Obtain all attribute textarea values and set them in the modelAttribute
	 * objects.
	 */
	public static void setAttributes()
	{
		for ( ModelAttribute a : attributes.keySet())
			a.value = attributes.get(a).getText();
		
		for ( ModelAttribute a : attributeSelectors.keySet())
			a.value = (String) attributeSelectors.get(a).getSelectedItem();
	}
	
	/*
	 * The JComponent set in the gui
	 */
	public static void addComponent(ModelNode node, JComponent parent) {
		
		JTabbedPane tabs = GuiComponent.newPane();
		JPanel component = new JPanel();
		tabs.addTab(node.tag, component);
		component.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
		JPanel attr = new JPanel();
		attr.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		
		/* loop trough child constructors */
		for(NodeConstructor c : node.childConstructors.keySet())
		{
			/* add child to interface if exactly one is required and the node
			 * is not present yet */
			if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE && 
					node.getChildNodes(c.defaultXmlTag()).isEmpty())
			{
				NodeConstructor newNode = c.newBlank();
				node.add(newNode.getNode());
				node.add(newNode);
			}
			else if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE)
			{
				// required unique childNode is already present: do nothing
			}
			else
			{
				/* add button for optional childnode(s) */
				attr.add(GuiComponent.actionButton(c.defaultXmlTag(), 
						new JButton("add"), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						NodeConstructor newNode = c.newBlank();
						node.add(newNode.getNode());
						addComponent(newNode.getNode(), component);
						node.add(newNode);
					}
				}
				));
			}
		}
		
		/* add textareas for this ModelNode's attributes */
		for(ModelAttribute a : node.attributes)
		{
			if ( a.options == null)
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setText(a.value);
				input.setEditable(a.editable);
				attr.add(GuiComponent.inputPanel(a.tag, input));
				attributes.put(a, input);
			}
			else
			{
				/* options box */
				JComboBox<String> input = new JComboBox<String>(a.options);
				input.setSelectedItem(a.value);
				input.setEditable(a.editable);
				attr.add(GuiComponent.selectPanel(a.tag, input));
				attributeSelectors.put(a, input);
			}
		}
		component.add(attr);
		
		/* placement of this ModelNode in the gui */
		if(node.tag == XmlLabel.speciesLibrary )
		{
			/* exception for speciesLib add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab((JTabbedPane) parent.getParent().getParent(), 
					node.tag, tabs, "");
		}
		else if( node.tag == XmlLabel.compartment )
		{
			/* exception for compartments add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab((JTabbedPane) parent.getParent().getParent(), 
					node.tag, tabs, "");
		} 
		else if(node.tag == XmlLabel.aspect )
		{
			GuiComponent.addTab((JTabbedPane) parent.getParent(), 
					node.tag, tabs, "");
		}
		else if( node.requirement.maxOne() && parent != GuiMain.tabbedPane )
		{
			/* exactly one: append this component to the parent component */
			parent.add(component, null);
			parent.revalidate();
		}
		else if( node.requirement == Requirements.ZERO_TO_MANY)
		{
			/* species, agents, TODO: changes to spinner */
			GuiComponent.addTab((JTabbedPane) parent.getParent(), 
					node.tag, tabs, "");
		} 
		else
		{
			/* else add component as Child tab of parent */
			GuiComponent.addTab((JTabbedPane) parent, node.tag, tabs, "");
		}
		
		/* add childnodes of this component to the gui */
		for(ModelNode n : node.childNodes)
			addComponent(n, component);
	}
}
