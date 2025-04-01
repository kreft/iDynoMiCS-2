package processManager.library;

import java.util.HashMap;

import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import utility.Helper;

public class InstantChange extends ProcessManager {
	private String VOLUMECHANGE = AspectRef.volumeChange;
	private String VOLUMETARGET = AspectRef.volumeTarget;
	private String SOLUTECONCS = AspectRef.soluteConcentrations;
	private String SOLUTEMASSES = AspectRef.soluteMasses;
	private String THRESHOLDVOLUME = AspectRef.thresholdVolume;
	
	private double _volumeChange;
	private double _volumeTarget;
	private InstantiableMap<String, Double> _soluteConcentrations;
	private InstantiableMap<String, Double> _soluteMasses;
	private double _thresholdVolume;
	
	private Compartment _compartment;
	
	private Shape _shape;
	
	private double _currentVolume;
	
	
	public void init( Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		if (this.isAspect(VOLUMECHANGE))
			this._volumeChange = this.getDouble(VOLUMECHANGE);
		
		if (this.isAspect(VOLUMETARGET))
			this._volumeTarget = this.getDouble(VOLUMETARGET);
		
		if (this.isAspect(THRESHOLDVOLUME))
			this._thresholdVolume = this.getDouble(THRESHOLDVOLUME);
		
		if (this.isAspect(SOLUTECONCS))
		{
			this._soluteConcentrations = 
				(InstantiableMap<String, Double>) this.getValue(SOLUTECONCS);
		}
		
		if (this.isAspect(SOLUTEMASSES))
		{
			this._soluteMasses = 
				(InstantiableMap<String, Double>) this.getValue(SOLUTEMASSES);
		}
		
		this._compartment = 
				Idynomics.simulator.getCompartment(this._compartmentName);
		
		this._shape = this._environment.getShape();
		
		/*
		 * Ensure the compartment being modified is a dimensionless one
		 */
		if (!(this._compartment.isDimensionless()))
		{
			//throw error
		}
		
		/*
		 * Ensure that only one of volume change and volume target are set
		 */
		else if (this._volumeChange != 0.0 && this._volumeTarget != 0.0)
		{
			//throw error
		}
		
	}


	@Override
	protected void internalStep() {
		
		this._currentVolume = this._shape.getTotalVolume();
		
		if (this._thresholdVolume != 0.0)
		{
			if (this._currentVolume >= this._thresholdVolume)
				return;
		}
		
		boolean volumeWillChange = this._volumeChange != 0
				|| this._volumeTarget != 0.0;
		
		if (volumeWillChange)
			this.volumeChangeStep();
		
		else
			this.volumeConstantStep();
		
	}

	/*
	 * Solute mass is instantly added to the compartment without
	 * changing the volume of the compartment
	 */
	private void volumeConstantStep() {
		
		HashMap<String, Double> newConcentrations =
				new HashMap<String, Double>();
		
		for (String soluteName: this._soluteMasses.keySet())
		{
			double massIncrease = this._soluteMasses.get(soluteName);
			
			if (massIncrease < 0.0)
			{
				//throw error
			}
			
			double currentConcentration = this._environment.
					getAverageConcentration(soluteName);
			
			double concentrationIncrease = 
					massIncrease / this._currentVolume;
			
			newConcentrations.put(soluteName,
					currentConcentration + concentrationIncrease);
			
		}
		
		for (String soluteName: newConcentrations.keySet())
		{
			this._environment.setAllConcentration(
					soluteName, newConcentrations.get(soluteName));
		}
		
	}


	private void volumeChangeStep() {
		double volumeChange;
		
		if (this._volumeTarget != 0.0)
			volumeChange = this._volumeTarget - this._currentVolume;
		
		else
			volumeChange = this._volumeChange;
		
		
		
		if (volumeChange > 0.0)
		{
			
			HashMap<String, Double> newMasses = 
					new HashMap<String, Double>();
			
			if (!Helper.isNullOrEmpty(this._soluteMasses))
			{
				for (String soluteName: this._soluteMasses.keySet())
				{
					double initialMass = this._environment.
							getAverageConcentration(soluteName) * 
							this._currentVolume;
					
					double massIncrease = this._soluteMasses.get(soluteName);
					
					newMasses.put(soluteName, initialMass + massIncrease);
				}
			}
			
			else
			{
				for (String soluteName: this._soluteConcentrations.keySet())
				{
					double initialMass = this._environment.
							getAverageConcentration(soluteName) * 
							this._currentVolume;
					
					double massIncrease = 
							this._soluteConcentrations.get(soluteName) *
							volumeChange;
					
					newMasses.put(soluteName, initialMass + massIncrease);
				}
			}
			
			double newVolume = this._currentVolume + volumeChange;
			
			this._shape.setTotalVolume(newVolume);
			
			
			for (String soluteName: newMasses.keySet())
			{
				double newConcentration = newMasses.get(soluteName) /
						newVolume;
				this._environment.setAllConcentration(
						soluteName, newConcentration);
			}
		}
		
		else
		{
			this._shape.setTotalVolume(
					this._currentVolume + volumeChange);
		}
		
		
	}
}
