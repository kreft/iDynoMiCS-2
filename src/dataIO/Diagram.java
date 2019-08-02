package dataIO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import agent.Agent;
import agent.SpeciesLib;
import compartment.Compartment;
import idynomics.Idynomics;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import utility.Helper;

public class Diagram
{
	protected FileHandler _diagramFile = new FileHandler();

	protected SpeciesLib _lib = Idynomics.simulator.speciesLibrary;

	public void createCustomFile(String fileName)
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".dot";
		this._diagramFile.fnew(fileString);
		this._diagramFile.write("digraph " + fileName + " {\n");
	}

	public void closeFile()
	{
		this._diagramFile.write("}\n");
		this._diagramFile.fclose();
	}

	public void speciesDiagram()
	{
		this._diagramFile.write("node [shape = circle]\n");
		String[] species = _lib.getAllSpeciesNames();
		for ( String s : species)
		{
			this._diagramFile.write(s + " \n");
			List<String> subs = this._lib.get(s).reg().getSubModuleNames();
			for ( String t : subs )
				this._diagramFile.write(t + " -> " + s + " \n");
		}
	}

	@SuppressWarnings("unchecked")
	public void reactionDiagram(Compartment comp)
	{
		if( Helper.isNullOrEmpty( comp ) )
			return;

		this._diagramFile.write(
				"node [shape = circle, fillcolor = green, style = filled]\n");

		for ( String c : comp.environment.getSoluteNames() )
			this._diagramFile.write( c + " \n");

		Map<Reaction,String> reactions = new HashMap<Reaction,String>();
		List<String> species = new LinkedList<String>();

		this._diagramFile.write(
				"node [shape = circle, fillcolor = lightblue, style = filled]\n");

		for ( Agent a : comp.agents.getAllAgents() )
		{
			String spec = a.getString( XmlRef.species );
			Object agentReactions = a.getValue( XmlRef.reactions );
			if ( agentReactions != null)
				for ( Reaction r : (List<Reaction>) agentReactions )
					if ( ! reactions.keySet().contains( r ) )
						reactions.put( r , a.getString( XmlRef.species ));
			if ( ! species.contains( a.getString( spec ) ) )
				this._diagramFile.write( spec + " \n");
		}

		if ( Helper.isNullOrEmpty( reactions ) )
			return;

		this._diagramFile.write(
				"node [shape = box, fillcolor = orange, style = filled]\n");
		for ( Reaction r : reactions.keySet() )
		{
			String reacDesc = r.getName()+ "_" + reactions.get(r);
			this._diagramFile.write(reacDesc + "\n");
			Map<String,Double> sto = r.getStoichiometryAtStdConcentration();
			String product;
			for ( String s : r.getReactantNames() )
			{
				product = s.equals(AspectRef.agentMass) || s.equalsIgnoreCase(
						AspectRef.biomass) ? reactions.get(r) : s;
				if ( sto.get(s) < 0 )
					this._diagramFile.write(product +" -> " + reacDesc + "\n");
				else
					this._diagramFile.write(reacDesc +" -> " + product + "\n");
			}
		}
	}

}
