/**
 * 
 */
package processManager.library;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import agent.predicate.HasAspect;
import agent.predicate.IsLocated;
import analysis.quantitative.Raster;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Surface;
import surface.collision.Collision;
import surface.predicate.AreColliding;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Structure Analysis
 * 
 * @author Sankalp @SankalpArya (sarya@ncsu.edu), NCSU, Raleigh
 */
public class StructureAnalysis extends ProcessManager {
	
	private String RASTER_SCALE = AspectRef.rasterScale;
	private String VERBOSE = AspectRef.verbose;
	/** 
	 * verbose raster output for debugging purposes 
	 */
	private boolean _verbose = false;
	/**
	 * Raster used to create euclidean distance maps
	 */
	private Raster _raster;
	/**
	 * size of the voxels of the raster.
	 */
	private double _rasterScale;
	/**
	 * Current Simulation Time
	 */
	private Double _currentTime = this.getTimeForNextStep();
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._verbose = Helper.setIfNone( this.getBoolean(VERBOSE), false );
		
		this._raster = new Raster( agents, _verbose );
		
		this._rasterScale = Helper.setIfNone( 
				this.getDouble( RASTER_SCALE ), 0.2 );
		
		Log.out(Tier.DEBUG, "Structure Analysis initialized");
				
	}

	@Override
	protected void internalStep() {
		this._raster.rasterize( _rasterScale );
		this._raster.plot( this._raster.agentMap(), 1.0, "dMap" + _currentTime);

	}
}
