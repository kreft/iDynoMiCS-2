package guiTools;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import idynomics.Idynomics;
import idynomics.Simulator;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelRequirement;

//WORK IN PROGRESS, MAY NEVER BE USED
public class GuiSimBuilder
{
	
	private static JPanel mainView;
	
	//private static GroupLayout layout;
	private static FlowLayout layout;
	
	//private static SequentialGroup verticalLayoutGroup;
	//private static ParallelGroup horizontalLayoutGroup;
	
	public static JComponent getSimulationBuilder()
	{
		mainView = new JPanel();
		//layout = new GroupLayout(mainView);
		layout = new FlowLayout();
		//layout.setAutoCreateGaps(true);
		//layout.setAutoCreateContainerGaps(true);
		//verticalLayoutGroup = layout.createSequentialGroup();
		//horizontalLayoutGroup = layout.createParallelGroup();
		//layout.setVerticalGroup(verticalLayoutGroup);
		//layout.setHorizontalGroup(horizontalLayoutGroup);
		mainView.setLayout(layout);
		mainView.setVisible(true);
		
//		String[] shapeNames = Shape.getAllOptions();
//		for ( String name : shapeNames )
//			System.out.println(name);
		
		Idynomics.simulator = new Simulator();
		JPanel smPanel = renderSubmodel(Idynomics.simulator);
		layout.addLayoutComponent("", smPanel);
		//verticalLayoutGroup.addComponent(smPanel);
		//horizontalLayoutGroup.addComponent(smPanel);
		
		mainView.validate();
		
		return new JScrollPane(mainView,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	private static JPanel renderSubmodel(IsSubmodel aSubmodel)
	{
		System.out.println("Trying to render a "+aSubmodel.getClass().getName());
		// TODO Make this runnable?
		JPanel smPanel = new JPanel();
		//GroupLayout smLayout = new GroupLayout(smPanel);
		//smLayout.setAutoCreateGaps(true);
		//smLayout.setAutoCreateContainerGaps(true);
		//Group vertGroup = smLayout.createSequentialGroup();
		//Group horizGroup = smLayout.createParallelGroup();
		//smLayout.setVerticalGroup(vertGroup);
		//smLayout.setHorizontalGroup(horizGroup);
		FlowLayout layout = new FlowLayout();
		smPanel.setLayout(layout);
		smPanel.setVisible(true);
		
		/* Add the requested attributes for this sub-model. */
		Map<String, Class<?>> parameters = aSubmodel.getParameters();
		for ( String param : parameters.keySet() )
		{
			
		}
		/* Now go through the sub-sub-models. */
		List<SubmodelMaker> makers = aSubmodel.getSubmodelMakers();
		
		JPanel smp;
		for ( SubmodelMaker aMaker : makers )
		{
			if ( aMaker.makeImmediately() )
			{
				aMaker.actionPerformed(null);
				smp = renderSubmodel(aMaker.getLastMadeSubmodel());
				//vertGroup.addComponent(smp);
				//horizGroup.addComponent(smp);
				layout.addLayoutComponent("", smp);
				if (aMaker.getRequirement() == SubmodelRequirement.EXACTLY_ONE)
					continue;
			}
			if ( aMaker.getClassNameOptions() != null )
			{
				JComboBox<String> jcb = makeSelector(aMaker);
				//vertGroup.addComponent(jcb);
				//horizGroup.addComponent(jcb);
				layout.addLayoutComponent("", jcb);
				continue;
			}
			JButton actionButton = makeButton(aMaker);
			//vertGroup.addComponent(actionButton);
			//horizGroup.addComponent(actionButton);
			layout.addLayoutComponent("", actionButton);
//			switch ( aMaker.getRequirement() )
//			{
//			case EXACTLY_ONE:
//				/* Exactly one, so do not make a button. */
//				
//				aMaker.actionPerformed(null);
//				smp = renderSubmodel(aMaker.getLastMadeSubmodel());
//				vertGroup.addComponent(smp);
//				horizGroup.addComponent(smp);
//				break;
//			case ZERO_OR_ONE:
//				// TODO new action listener that removes this button once it
//				// has been clicked
//				actionButton = makeButton(aMaker);
//				break;
//			case ZERO_TO_MANY:
//				actionButton = makeButton(aMaker);
//				break;
//			case ONE_TO_MANY:
//				actionButton = makeButton(aMaker);
				aMaker.actionPerformed(null);
//				smp = renderSubmodel(aMaker.getLastMadeSubmodel());
//				vertGroup.addComponent(smp);
//				horizGroup.addComponent(smp);
//				break;
//			}
		}
		return smPanel;
	}
	
	private static JButton makeButton(SubmodelMaker maker)
	{
		String actionName = maker.getName();
		JButton out = new JButton(actionName);
		out.addActionListener(maker);
		return out;
	}
	
	private static JComboBox<String> makeSelector(SubmodelMaker maker)
	{
		String[] names = maker.getClassNameOptions();
		JComboBox<String> out = new JComboBox<String>(names);
		out.setAction(maker);
		out.setEditable(false);
		return out;
	}
}
