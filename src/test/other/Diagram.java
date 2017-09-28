package test.other;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import agent.Agent;
import agent.SpeciesLib;
import dataIO.FileHandler;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import utility.Helper;

public class Diagram {
	
	protected FileHandler _diagramFile = new FileHandler();
	
	protected SpeciesLib _lib = Idynomics.simulator.speciesLibrary;
	
	public void createCustomFile(String fileName)
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".dot";
		_diagramFile.fnew(fileString);
		Log.out(Tier.EXPRESSIVE, "Writing new file: " + fileString);
		_diagramFile.write("digraph " + fileName + " {\n");
	}

	public void closeFile()
	{
		_diagramFile.write("}\n");
		_diagramFile.fclose();
	}
	
	public void speciesDiagram()
	{
		_diagramFile.write("node [shape = circle]\n");
		String[] species = _lib.getAllSpeciesNames();
		for (String s : species)
		{
			_diagramFile.write(s + "; \n");
			List<String> subs = _lib.get(s).reg().getSubModuleNames();
			for(String t : subs)
				_diagramFile.write(t + " -- " + s + "; \n");
		}
	}

	@SuppressWarnings("unchecked")
	public void reactionDiagram(Compartment comp)
	{
		
		if( Helper.isNullOrEmpty( comp ) )
			return;
		
		_diagramFile.write("node [shape = circle, fillcolor = green]\n");
		
		for(String c : comp.environment.getSoluteNames() )
			_diagramFile.write( c + "; \n");
		
		 Map<Reaction,String> reactions = new HashMap<Reaction,String>();
		
		for(Agent a : comp.agents.getAllAgents())
			for( Reaction r : (List<Reaction>) a.getValue( XmlRef.reactions ) )
				if(! reactions.keySet().contains( r ) )
					reactions.put( r , a.getString( XmlRef.species ));
		
		if ( Helper.isNullOrEmpty( reactions ) )
			return;
		
		_diagramFile.write("node [shape = square, fillcolor = red]\n");
		for (Reaction r : reactions.keySet() )
		{
			_diagramFile.write(r.getName() + "; \n");
			Map<String,Double> sto = r.getStoichiometry();
			for (String s : sto.keySet() )
			{
				
				if( sto.get(s) < 0 )
					_diagramFile.write( (s == AspectRef.agentMass ? 
							reactions.get(r) + " " : "") + s + " -> " + 
							r.getName() + "; \n");
				else 
					_diagramFile.write( r.getName() + " -> " + 
							(s == AspectRef.agentMass ? reactions.get(r) + " " :
							"") + s + "; \n");
			}
		}
	}
	
}
