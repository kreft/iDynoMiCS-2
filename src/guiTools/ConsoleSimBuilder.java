package guiTools;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import dataIO.Log;
import idynomics.GuiLaunch;
import idynomics.GuiLaunch.ViewType;
import idynomics.Idynomics;
import idynomics.Simulator;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;

public class ConsoleSimBuilder
{
	
	public static void makeSimulation()
	{
		GuiLaunch.setView(ViewType.CONSOLE);
		Log.printToScreen("~~~~~~~~~~~~~~~~~~~~~", false);
		Log.printToScreen("Making new simulation", false);
		Log.printToScreen("~~~~~~~~~~~~~~~~~~~~~", false);
		Idynomics.simulator = new Simulator();
		buildSubmodel(Idynomics.simulator);
	}
	
	private static void buildSubmodel(IsSubmodel aSubmodel)
	{
		String subName = aSubmodel.getClass().getSimpleName();
		GuiConsole.writeOut("Making submodel \""+subName+"\"\n");
		Map<String, Class<?>> parameters = aSubmodel.getParameters();
		for ( String name : parameters.keySet() )
		{
			Class<?> classType = parameters.get(name);
			if ( classType.equals(Boolean.class) )
			{
				Boolean value = GuiConsole.requestInputBoolean("Do you want "+
												subName+" to be "+name+"?");
				aSubmodel.setParameter(name, value.toString());
				GuiConsole.writeOut("\t"+subName+" is "+(value?"":"not ")+name+"\n");
			}
			else
			{
				String className = classType.getSimpleName();
				String value = GuiConsole.requestInput("Please enter \""+className+
							"\" value for "+subName+" parameter \""+name+"\": ");
				aSubmodel.setParameter(name, value);
				GuiConsole.writeOut("\t"+subName+" accepts \""+value+"\" as "+name+"\n");
			}
		}
		
		/* Now go through the sub-sub-models. */
		List<SubmodelMaker> makers = aSubmodel.getSubmodelMakers();
		for ( SubmodelMaker aMaker : makers )
		{
			GuiConsole.writeOut("Maker: \""+aMaker.getName()+"\"\n");
			while ( aMaker.mustMakeMore() )
			{
				makeSubmodel(aMaker);
			}
			while ( shouldMakeMore(aMaker) )
			{
				makeSubmodel(aMaker);
			}
		}
	}
	
	private static boolean shouldMakeMore(SubmodelMaker aMaker)
	{
		return aMaker.canMakeMore() &&
				GuiConsole.requestInputBoolean(aMaker.getName()+"?");
	}
	
	private static void makeSubmodel(SubmodelMaker aMaker)
	{
		if ( aMaker.makeImmediately() )
		{
			aMaker.actionPerformed(null);
			buildSubmodel(aMaker.getLastMadeSubmodel());
			return;
		}
		String[] options = aMaker.getClassNameOptions();
		if ( options != null )
		{
			if ( options.length == 0 )
			{
				Log.printToScreen("Empty list of class names!", true);
				return;
			}
			GuiConsole.writeOut("\tPossible options are:\n");
			for ( int i = 0; i < options.length; i++ )
				GuiConsole.writeOut("\t\t["+i+"] "+options[i]+"\n");
			String value = GuiConsole.requestInput(
								"Please choose an option by number: ");
			int i = Integer.valueOf(value);
			String optionToUse = options[i];
			aMaker.actionPerformed(new ActionEvent(aMaker, 0, optionToUse));
			buildSubmodel(aMaker.getLastMadeSubmodel());
		}
	}
}
