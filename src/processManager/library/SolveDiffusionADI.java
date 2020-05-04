package processManager.library;

import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import grid.ArrayType;
import grid.SpatialGrid;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import solver.adi.Bacillus;
import solver.adi.Diffuse;
import utility.Helper;



public class SolveDiffusionADI extends ProcessManager {
	
	private Diffuse adi = new Diffuse();
	private String[] _soluteNames;
	
	public String SOLUTES = AspectRef.soluteNames;

	/**
	 * 
	 * Initiation from protocol file: 
	 * 
	 * TODO verify and finalise
	 */
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		adi.init();
		adi.postParamInit();
		
		this._soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(this._environment.getSoluteNames()));
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		int i = 0;
		for ( String soluteName : this._soluteNames )
		{
			SpatialGrid solute = this._environment.getSoluteGrid(soluteName);
			double[][][] solarray = solute.getArray(ArrayType.CONCN);
			adi.assignSolute(i, normalizeSoluteArray(solarray));
			i++;
		}

		for ( Agent a : this._agents.getAllLocatedAgents() )
		{
			adi.mobileroot.addChild(new Bacillus(a));
		}
		
		adi.stepResources();
		
		this.postStep();
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/

	/**
	 * Normalize Solute Array to 1
	 */
	private double[][] normalizeSoluteArray(double[][][] solluteArray)
	{
		return new double[5][5];
	}
	
	/**
	 * Convert normalized solute matrix to solute Array
	 */
	private double[][][] convertToSoluteArray(double[][] normalizedMatrix)
	{
		return new double[5][5][];
	}

	public void prestep(Collection<SpatialGrid> variables, double dt)
	{

	}

	
	private void postStep() 
	{
		// update agents
		// update solute grids
		int i = 0;
		for ( String soluteName : this._soluteNames )
		{
			this._environment.setConcentration( soluteName, 
					convertToSoluteArray(adi.getSolute(i)));
			i++;
		}
	}
}
