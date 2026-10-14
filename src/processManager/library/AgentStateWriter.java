package processManager.library;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import debugTools.QuickCSV;
import idynomics.Idynomics;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * \brief Writes ID, species, location (x, y) and total mass of every agent
 * (excluding vector species) to a csv file using QuickCSV.
 * 
 * QuickCSV creates a new file on every write, so each call of this process
 * manager produces one file:
 *   [outputFolder]/[fileName]/[fileName]_[iteration]_[nr].csv
 * 
 * Optional aspects:
 *   fileName        (String) name/folder for the output, default "agentState"
 *   excludeSpecies  (String) comma separated species names to skip,
 *                   e.g. "vector,plasmidVector"
 */
public class AgentStateWriter extends ProcessManager
{
	public static final String FILE_NAME = AspectRef.fileName;
	public static final String EXCLUDE_SPECIES = "excludeSpecies";

	private String _fileName = "agentState";
	private Set<String> _excluded = new HashSet<String>();

	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		String name = this.getString(FILE_NAME);
		if ( ! Helper.isNullOrEmpty(name) )
			this._fileName = name;

		String exclude = this.getString(EXCLUDE_SPECIES);
		if ( ! Helper.isNullOrEmpty(exclude) )
			for ( String s : exclude.split(",") )
				if ( ! s.trim().isEmpty() )
					this._excluded.add(s.trim());
	}

	@Override
	protected void internalStep()
	{
		StringBuilder out = new StringBuilder();
		out.append("time,id,species,x,y,mass,growthtone,vectors");

		double time = Idynomics.simulator.timer.getEndOfCurrentIteration();

		for ( Agent agent : this._agents.getAllAgents() )
		{
			String species = agent.getString(XmlRef.species);

			/* skip vectors */
			if ( species != null && this._excluded.contains(species) )
				continue;

			/* location: body center (correct for rods and periodic domains) */
			String x = "", y = "";
			Object bodyObj = agent.get(AspectRef.agentBody);
			if ( bodyObj instanceof Body )
			{
				double[] c = ((Body) bodyObj).getCenter(this._agents.getShape());
				if ( c.length > 0 ) x = String.valueOf(c[0]);
				if ( c.length > 1 ) y = String.valueOf(c[1]);
			}

			/* mass: handles Double, Double[] and Map<String,Double> */
			double mass = Helper.totalMass( agent.get(AspectRef.agentMass) );

			/* growthtone: current specific growth rate / max growth rate */
			double growthTone = agent.getDouble(AspectRef.growthTone);

			/* vectors carried by this agent */
			String carried = vectorSpecies( agent.getVectors() );

			out.append("\n")
				.append(time).append(",")
				.append(agent.identity()).append(",")
				.append(species).append(",")
				.append(x).append(",")
				.append(y).append(",")
				.append(mass).append(",")
				.append(growthTone).append(",")
				.append(carried);
		}

		QuickCSV.write( this._compartmentName + "_" + this._fileName, 
				out.toString() );
	}

	/**
	 * Species names of the vector agents, separated by ';'.
	 * Empty string when the agent carries no vectors.
	 */
	private static String vectorSpecies(List<Agent> vectors)
	{
		if ( vectors == null )
			return "";
		StringBuilder out = new StringBuilder();
		for ( Agent v : vectors )
			out.append( out.length() > 0 ? ";" : "" )
				.append( v.getString(XmlRef.species) );
		return out.toString();
	}
}
