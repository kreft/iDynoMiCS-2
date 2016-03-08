package guiTools;

import java.util.LinkedHashMap;

import javax.swing.*;

import idynomics.Idynomics;
import idynomics.Simulator;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelRequirement;

public class GuiSimBuilder
{
	
	private static JPanel mainView;
	
	public static JComponent getSimulationBuilder()
	{
		mainView = new JPanel();
		
		Idynomics.simulator = new Simulator();
		LinkedHashMap<AbstractAction,SubmodelRequirement> actionReqs = 
								Idynomics.simulator.getAllSubmodelMakers();
		
		
		
		return new JScrollPane(mainView,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	
	private static void addSubmodelToMainView(IsSubmodel aSubmodel)
	{
		LinkedHashMap<AbstractAction,SubmodelRequirement> actionReqs = 
										aSubmodel.getAllSubmodelMakers();
		for ( AbstractAction aa : actionReqs.keySet() )
		{
			IsSubmodel lastSubmodelMade;
			JButton actionButton = null;
			switch ( actionReqs.get(aa) )
			{
			case EXACTLY_ONE:
				/* Exactly one, so do not make a button. */
				aa.actionPerformed(null);
				lastSubmodelMade = aSubmodel.getLastMadeSubmodel();
				break;
			case ZERO_OR_ONE:
				// TODO new action listener that removes this button once it
				// has been clicked
				actionButton = makeButton(aa);
				break;
			case ZERO_TO_MANY:
				actionButton = makeButton(aa);
				break;
			case ONE_TO_MANY:
				aa.actionPerformed(null);
				lastSubmodelMade = aSubmodel.getLastMadeSubmodel();
				actionButton = makeButton(aa);
				break;
			}
			
		}
	}
	
	private static JButton makeButton(AbstractAction aa)
	{
		String actionName = (String) aa.getValue(Action.NAME);
		JButton out = new JButton(actionName);
		out.addActionListener(aa);
		return out;
	}
}
