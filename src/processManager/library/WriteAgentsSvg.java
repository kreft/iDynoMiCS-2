package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.SvgExport;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Shape;
import shape.ShapeConventions.DimName;
import utility.ExtraMath;

/**
 * \brief TODO
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
		
		/* grab shape from agents */
		Shape shape = agents.getShape();
		
		/* Draw computational domain rectangle. */
		// FIXME Safety: this assumes the shape is a rectangle!
		double[] size = shape.getDimensionLengths();
		
		/* check if this shape is cylindrical or cartesian */
		//TODO Stefan: Maybe we should use another check?
		if (shape instanceof CylindricalShape)
			this._svg.rectangle( Vector.zeros(size), size, "GRAY");
		else if (shape instanceof CartesianShape)
			this._svg.circle(Vector.zeros(size), size, "GRAY");
		else
			Log.out(Tier.CRITICAL,
					"Warning! "+this._name+" computational domain neither "
							+ "rectangular nor circular");
		/* Draw solute grid for specified solute, if any. */
		if ( ! environment.isSoluteName(this._solute) )
		{
			Log.out(Tier.CRITICAL,
						"Warning! "+this._name+" can't find solute "+
						this._solute+" in the environment");
		}
		else
		{
			shape = environment.getShape();
			
			SpatialGrid solute = environment.getSoluteGrid(_solute);
			
			int nDim = agents.getNumDims();
			double[] origin;
			double[] dimension = new double[3];
			for ( int[] coord = shape.resetIterator(); 
					shape.isIteratorValid(); coord = shape.iteratorNext() )
			{
				/* Identify exact voxel location and size. */
				origin = shape.getVoxelOrigin(coord);
				shape.getVoxelSideLengthsTo(dimension, coord);
				/*
				 * Scale the solute concentration for coloring.
				 * First, map the concentration to the real interval [0, 1].
				 */
				double concn = solute.getValueAtCurrent(this._arrayType);
				concn /= Math.abs(this._maxConcn);
				concn = ExtraMath.restrict(concn, 0.0, 1.0);
				/* Flip this, so that higher concentration is darker. */
				if ( this._higherIsDarker )
					concn = 1.0 - concn;
				/* Map this to the integer interval [0, 255]. */
				int c = (int) Math.round(255.0 * concn);
				/* Write the solute square. */
				String pigment = "rgb(" + c + "," + c + "," + c + ")";
				if (shape instanceof CartesianShape)
					this._svg.rectangle(Vector.subset(origin, nDim), 
							Vector.subset(dimension, nDim),pigment);
				else if (shape instanceof CylindricalShape)
					this._svg.circleElement(Vector.zerosDbl(2),
											origin, dimension, 100, pigment);
			}
		}
		/* Draw all located agents. */
		// NOTE currently only coccoid
		for ( Agent a: agents.getAllLocatedAgents() )
			if ( a.isAspect("body") )
			{
				List<double[]> joints = ((Body) a.get("body")).getJoints();
				for ( int i = 0; joints.size() > i; i++ )
				{
					this._svg.circle(joints.get(i), 
							a.getDouble("radius"), 
							a.getString("pigment"));
				}
			}
		/* Close the SVG file */
		this._svg.closeSvg();
	}
}