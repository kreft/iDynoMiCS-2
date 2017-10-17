package processManager.library;

import org.w3c.dom.Element;

import analysis.quantitative.Raster;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import utility.Helper;

public class AgentDetachment extends ProcessManager
{
	
	private String DETACHMENT_RATE = AspectRef.detachmentRate;
	private String RASTER_SCALE = AspectRef.rasterScale;
	private String VERBOSE = AspectRef.verbose;
	
	private boolean _verbose = false;
	private Raster _raster;
	private double _detachmentRate;
	private double _rasterScale;
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		this._verbose = Helper.setIfNone( this.getBoolean(VERBOSE), false );
		
		this._raster = new Raster( agents, _verbose );
		
		this._detachmentRate = Helper.setIfNone( 
				this.getDouble(DETACHMENT_RATE), 0.0 );
		
		this._rasterScale = Helper.setIfNone( 
				this.getDouble(RASTER_SCALE), 1.0 );
	}
	
	@Override
	protected void internalStep()
	{
		this._raster.rasterize( _rasterScale );
		
	}

}
