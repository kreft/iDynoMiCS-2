package guiTools;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner; // to be implemented
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import dataIO.XmlRef;
import generalInterfaces.Instantiatable;
import nodeFactory.Constructable;
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
	private static HashMap<ModelAttribute,JTextArea> _attributes = 
			new HashMap<ModelAttribute,JTextArea>();
	
	/**
	 * Hashmap of all gui TextAreas associated with their ModelAttribute
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<ModelAttribute,JComboBox> _attributeSelectors = 
			new HashMap<ModelAttribute,JComboBox>();
	
	/**
	 * Obtain all attribute textarea values and set them in the modelAttribute
	 * objects.
	 */
	public static void setAttributes()
	{
		for ( ModelAttribute a : _attributes.keySet())
			a.value = _attributes.get(a).getText();
		
		for ( ModelAttribute a : _attributeSelectors.keySet())
			a.value = (String) _attributeSelectors.get(a).getSelectedItem();
	}
	
	/*
	 * The JComponent set in the gui
	 */
	public static void addComponent(ModelNode node, JComponent parent) {
		
		JTabbedPane tabs = GuiComponent.newPane();
		
		tabs.setUI(new BasicTabbedPaneUI() {
	        private final Insets borderInsets = new Insets(0, 0, 0, 0);
	        @Override
	        protected void paintContentBorder(Graphics g, int tabPlacement, 
	        		int selectedIndex) {
	        }
	        @Override
	        protected Insets getContentBorderInsets(int tabPlacement) {
	            return borderInsets;
	        }
	    });
		
		JScrollPane scrollPane = new JScrollPane();
		
		JPanel component = new JPanel();
		component.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
		scrollPane.add(component);
		tabs.addTab(node.getTag(), scrollPane);
		JPanel attr = new JPanel();
		attr.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		attr.add(GuiComponent.textPanel(node.getTag() + " " + node.getTitle(), 1));
		
		component.add(attr);
		scrollPane.setViewportView(component);
		
		if ( node.getRequirment().max > 1 )
		{
			NodeConstructor constructor = node.constructor;
			/* add button for optional childnode(s) */
			attr.add(GuiComponent.actionButton(constructor.defaultXmlTag() + " " + node.getTitle(), 
					new JButton("remove"), new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					GuiEditor.setAttributes();
					node.delete(node.getTitle());
					GuiMain.update();
				}
			}
			));
		}
		
		/* loop trough child constructors */
		for ( String c : node.getConstructables() )
		{
			Constructable constructable = node.getConstructable(c);
			/* add child to interface if exactly one is required and the node
			 * is not present yet */
			if ( constructable.requirement() == Requirements.EXACTLY_ONE )
			{
				NodeConstructor newNode = node.getConstruct(c);
				if ( ! node.hasChildNodes(newNode.defaultXmlTag()) )
				{
					node.add(newNode.getNode());
					node.add(newNode);
				}
			}
			else
			{
				/* add button for optional childnode(s) */
				attr.add(GuiComponent.actionButton(
						constructable.classRef(), 
						new JButton("add"), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						NodeConstructor newNode = node.getConstruct(c);
						if ( newNode != null )
						{
							node.add(newNode.getNode());
							addComponent(newNode.getNode(), component);
							node.add(newNode);
						}
					}
				}
				));
			}
		}
		
		/* add textareas for this ModelNode's attributes */
		for ( ModelAttribute a : node.getAttributes() )
		{
			if ( a.value == null &&  a.options == null )
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanel(a.tag, input));
				_attributes.put(a, input);
			}
			else if ( a.options == null && a.value.length() < 60)
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setText(a.value);
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanel(a.tag, input));
				_attributes.put(a, input);
			}
			else if ( a.options == null )
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setText(a.value);
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanelLarge(a.tag, input));
				_attributes.put(a, input);
			}
			else
			{
				/* options box */
				JComboBox<String> input = new JComboBox<String>(a.options);
				input.setSelectedItem(a.value);
				input.setEditable(a.editable);
				attr.add(GuiComponent.selectPanel(a.tag, input));
				_attributeSelectors.put(a, input);
			}
		}
		
		/* placement of this ModelNode in the gui */
		if ( node.isTag(XmlRef.speciesLibrary) )
		{
			tabs.setBackgroundAt(0,new Color(1f,1f,0f));
			/* exception for speciesLib add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab( (JTabbedPane) 
					parent.getParent().getParent().getParent().getParent(), 
					node.getTag() , tabs, "");
		}
		else if ( node.isTag(XmlRef.compartment) )
		{
			tabs.setBackgroundAt(0,new Color(1f,1f,0f));
			/* exception for compartments add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab( (JTabbedPane) 
					parent.getParent().getParent().getParent().getParent(), 
					node.getTag() + " " + node.getTitle(), tabs, "");
		} 
		else if ( node.isTagIn(new String[] 
				/* compartment container nodes */
				{XmlRef.agents, XmlRef.solutes, XmlRef.processManagers, 
				XmlRef.reactions, XmlRef.environment}) )
		{
			tabs.setBackgroundAt(0,new Color(1f,1f,0f));
			GuiComponent.addTab( (JTabbedPane) 
					parent.getParent().getParent().getParent(), 
					node.getTag(), tabs, "");
		}
//		else if ( node.isTagIn(new String[] {XmlRef.aspect, XmlRef.solute}) )
//		{
//			GuiComponent.addTab( (JTabbedPane) 
//					parent.getParent().getParent().getParent(), 
//					node.getTag() + " " + node.getTitle(), tabs, "" );
//		}
		else if ( node.isTagIn(new String[] {XmlRef.reaction}) && 
				node.getRequirment() == Requirements.IMMUTABLE)
		{
			GuiComponent.addTab( (JTabbedPane) 
					parent.getParent().getParent().getParent(), 
					node.getTag() + " " + node.getTitle(), tabs, "");
		}
		else if ( node.isTagIn(new String[] 
				{XmlRef.shapeDimension, XmlRef.point, XmlRef.stoichiometry,
						XmlRef.constant, XmlRef.speciesModule}) )
		{
			
			parent.add(component, null);
			parent.revalidate();
		} 
		else if( node.requireMaxOne() && parent != GuiMain.tabbedPane )
		{
			/* exactly one: append this component to the parent component */
			parent.add(component, null);
			parent.revalidate();
		}
		else if ( node.areRequirements(Requirements.ZERO_TO_MANY) )
		{
			tabs.setBackgroundAt(0,new Color(1f,1f,0f));
			/* species, agents, TODO: changes to spinner */
			GuiComponent.addTab( (JTabbedPane) 
					parent.getParent().getParent().getParent(), 
					node.getTag() + " " + node.getTitle(), tabs, "");
		} 
		else
		{
			/* else add component as Child tab of parent */
			GuiComponent.addTab((JTabbedPane) parent, node.getTag(), tabs, "");
		}
		
		/* add childnodes of this component to the gui */
		for ( ModelNode n : node.getAllChildNodes() )
			addComponent(n, component);
	}
}
