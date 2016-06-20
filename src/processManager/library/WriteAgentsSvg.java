package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.SvgExport;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Shape;
import surface.Surface;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class WriteAgentsSvg extends ProcessManager
{
	
	public static String BODY = AspectRef.agentBody;
	public static String RADIUS = AspectRef.bodyRadius;
	public static String PIGMENT = AspectRef.agentPigment;
	
	public static String ARRAY_TYPE = AspectRef.gridArrayType;
	public static String MAX_VALUE = AspectRef.visualOutMaxValue;
	public static String SOLUTE_NAME = AspectRef.soluteName;
	public static String FILE_PREFIX = AspectRef.filePrefix;
	
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
	
	/**
	 * defines how many polygon points should be used to approximate a curve
	 *	TODO: make this a 'density' with respect to total dimension length. 
	 */
	protected int _pointsOnCurve = 10; 
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._solute = this.getString(SOLUTE_NAME);
		this._maxConcn = ( this.isAspect(MAX_VALUE) ? 
									this.getDouble(MAX_VALUE) : 2.0);
		this._prefix = this.getString(FILE_PREFIX);
		
		this._arrayType = ArrayType.CONCN;
		if ( this.isAspect(ARRAY_TYPE) ) 
			this._arrayType = ArrayType.valueOf(this.getString(ARRAY_TYPE));
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep()
	{
		/* Initiate new file. */
		this._svg.createFile(this._prefix);
		
		/* grab shape from agents */
		Shape shape = this._agents.getShape();
		
		/* Draw computational domain rectangle. */
		// FIXME Safety: this assumes the shape is a rectangle!
		double[] size = shape.getDimensionLengths();
		
		/* check if this shape is cylindrical or cartesian */
		//TODO Stefan: Maybe we should use another check?
		if (shape instanceof CartesianShape)
		this._svg.rectangle( Vector.zeros(size), size, "GRAY");
		else if (shape instanceof CylindricalShape)
			this._svg.circle(Vector.zeros(size), size, "GRAY");
		else
			Log.out(Tier.CRITICAL,
					"Warning! "+this._name+" computational domain neither "
							+ "rectangular nor circular");
		/* Draw solute grid for specified solute, if any. */
		if ( ! this._environment.isSoluteName(this._solute) )
		{
			Log.out(Tier.CRITICAL,
						"Warning! "+this._name+" can't find solute "+
						this._solute+" in the environment");
		}
		else
		{
			shape = this._environment.getShape();
			
			SpatialGrid solute = this._environment.getSoluteGrid(_solute);
			
			int nDim = this._agents.getNumDims();
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
					this._svg.circleElement(Vector.zerosDbl(2),	origin, 
									dimension, this._pointsOnCurve, pigment);
			}
		}
		/* Draw all located agents. */
		// NOTE currently only coccoid
		for ( Agent a: this._agents.getAllLocatedAgents() )
			if ( a.isAspect(BODY) )
			{
//				List<double[]> joints = ((Body) a.get(BODY)).getJoints();
//				for ( int i = 0; joints.size() > i; i++ )
//				{
//					this._svg.circle(joints.get(i), 
//							a.getDouble(RADIUS), 
//							a.getString(PIGMENT));
//				}
				
				List<Surface> surfaces = ((Body) a.getValue(BODY)).getSurfaces();
				for( Surface s : surfaces)
					this._svg.draw(s, a.getString(PIGMENT));
			}
		/* Close the SVG file */
		this._svg.closeFile();
	}
}