package processManager;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log.tier;
import dataIO.Log;
import dataIO.SvgExport;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class WriteAgentsSvg extends ProcessManager
{
	/**
	 * The SVG exporter.
	 */
	protected SvgExport _svg = new SvgExport();
	/**
	 * Maximum concentration value to use for the color gradient when plotting
	 * solute concentration.
	 */
	protected double _maxConcn = 2.0;
	/**
	 * Name of the solute that is used to draw the solute gradient, if any.
	 */
	protected String _solute;
	/**
	 * The prefix for the SVG file output path.
	 */
	protected String _prefix;
	/**
	 * TODO
	 */
	protected ArrayType _arrayType;
	/**
	 * TODO
	 * TODO make this set-able from xml.
	 */
	protected boolean _higherIsDarker = true;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		
		this._solute = this.getString("solute");
		this._maxConcn = ( this.isAspect("maxConcentration") ? 
									this.getDouble("maxConcentration") : 2.0);
		this._prefix = this.getString("prefix");
		
		this._arrayType = ArrayType.CONCN;
		if ( this.isAspect("arrayType") ) 
			this._arrayType = ArrayType.valueOf(this.getString("arrayType"));
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/* Initiate new file. */
		this._svg.newSvg(this._prefix);
		/* Draw computational domain rectangle. */
		// FIXME Safety: this assumes the shape is a rectangle!
		double[] size = agents.getShape().getDimensionLengths();
		this._svg.rectangle( Vector.zeros(size), size, "GRAY");
		/* Draw solute grid for specified solute, if any. */
		if ( ! environment.isSoluteName(this._solute) )
		{
			Log.out(tier.CRITICAL,
						"Warning! "+this._name+" can't find solute "+
						this._solute+" in the environment");
		}
		else
		{
			SpatialGrid solute = environment.getSoluteGrid(_solute);
			int nDim = agents.getNumDims();
			double[] origin;
			double[] dimension = new double[3];
			for ( int[] coord = solute.resetIterator(); 
					solute.isIteratorValid(); coord = solute.iteratorNext() )
			{
				/* Identify exact voxel location and size. */
				origin = solute.getVoxelOrigin(coord);
				solute.getVoxelSideLengthsTo(dimension, coord);
				/*
				 * Scale the solute concentration for coloring.
				 * First, map the concentration to the real interval [0, 1].
				 */
				double concn = solute.getValueAtCurrent(this._arrayType);
				concn /= this._maxConcn;
				concn = ExtraMath.restrict(concn, 0.0, 1.0);
				/* Flip this, so that higher concentration is darker. */
				if ( this._higherIsDarker )
					concn = 1.0 - concn;
				/* Map this to the integer interval [0, 255]. */
				int c = (int) Math.round(255.0 * concn);
				/* Write the solute square. */
				this._svg.rectangle(Vector.subset(origin, nDim), 
									Vector.subset(dimension, nDim),
									"rgb(" + c + "," + c + "," + c + ")");
			}
		}
		/* Draw all located agents. */
		// NOTE currently only coccoid
		for ( Agent a: agents.getAllLocatedAgents() )
			if ( a.isAspect("joints") )
			{
				@SuppressWarnings("unchecked")
				List<double[]> joints = (List<double[]>) a.get("joints");
				for ( int i = 0; joints.size() > i; i++ )
				{
					this._svg.circle(joints.get(i), 
							a.getDouble("radius"), 
							a.getString("pigment"));
				}
			}
			else
			{
				tier level = tier.CRITICAL;
				Log.out(level, "Couldn't find joints in located agent!");
				Log.out(level, "Aspects found: ");
				for ( String name : a.aspectRegistry.getAllAspectNames() )
					Log.out(level, "\t"+name);
				System.exit(-1);
			}
		/* Close the SVG file */
		this._svg.closeSvg();
	}
}