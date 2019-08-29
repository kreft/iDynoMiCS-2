/**
 * 
 */
package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.ObjectFactory;
import grid.SpatialGrid;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.RegularReaction;
import reaction.Reaction;
import referenceLibrary.XmlRef;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.IntegerArray;
import shape.Shape;
import solver.PDEagentsOnly;
import solver.PDEexplicit;
import solver.PDEupdater;

public class SolveAgentReactionsOnly extends ProcessDiffusion
{
	double track = 0.0;
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._solver = new PDEagentsOnly();

	}
		
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		super.internalStep();
		this.postStep();
	}

	protected PDEupdater standardUpdater()
	{
		return new PDEupdater()
		{
			@Override
			public void prestep(Collection<SpatialGrid> variables, double dt)
			{
				for ( Agent agent : _agents.getAllLocatedAgents() )
					applyAgentReactions(agent, dt);
			}
		};
	}

	private void applyAgentReactions(Agent agent, double dt)
	{
		if( agent.identity() == 1)
		{
			track += dt;
			System.out.println(track);
		}
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = 
				(List<Reaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;

		@SuppressWarnings("unchecked")
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>)
						agent.getValue(VOLUME_DISTRIBUTION_MAP);
		HashMap<IntegerArray,Double> distributionMap = 
				mapOfMaps.get(agent.getCompartment().getShape());
		ProcessDiffusion.scale(distributionMap, 1.0);

		Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
		@SuppressWarnings("unchecked")
		Map<String,Double> newBiomass = (HashMap<String,Double>)
				ObjectFactory.copy(biomass);

		Map<String,Double> concns = new HashMap<String,Double>();

		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, productRate, volume, perVolume;
		for ( IntegerArray coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord.get());
			perVolume = Math.pow(volume, -1.0);
			for ( Reaction r : reactions )
			{
				concns.clear();
				for ( String varName : r.getConstituentNames() )
				{
					if ( this._environment.isSoluteName(varName) )
					{
						solute = this._environment.getSoluteGrid(varName);
						concn = solute.getValueAt(CONCN, coord.get());
					}
					else if ( biomass.containsKey(varName) )
					{
						concn = biomass.get(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else if ( agent.isAspect(varName) )
					{
						concn = agent.getDouble(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else
					{
						concn = 0.0;
					}
					concns.put(varName, concn);
				}

				for ( String productName : r.getReactantNames() )
				{
					productRate = r.getProductionRate(concns,productName);
					if ( this._environment.isSoluteName(productName) )
					{

					}
					else if ( newBiomass.containsKey(productName) )
					{
						newBiomass.put(productName, newBiomass.get(productName)
								+ (productRate * dt * volume));
					}
					else if ( agent.isAspect(productName) )
					{
						newBiomass.put(productName, agent.getDouble(productName)
								+ (productRate * dt * volume));
					}
					else
					{
						newBiomass.put(productName, (productRate * dt * volume));
						System.out.println("agent reaction catched " + 
								productName);
					}
				}
			}
		}

		ProcessMethods.updateAgentMass(agent, newBiomass);
	}
}