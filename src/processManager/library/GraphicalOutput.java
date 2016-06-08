package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.GraphicalExporter;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.PovExport;
import dataIO.SvgExport;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Dimension.DimName;
import shape.Shape;
import surface.Surface;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class GraphicalOutput extends ProcessManager
{
	
	public static String BODY = AspectRef.agentBody;
	public static String RADIUS = AspectRef.bodyRadius;
	public static String PIGMENT = AspectRef.agentPigment;
	
	public static String ARRAY_TYPE = AspectRef.gridArrayType;
	public static String MAX_VALUE = AspectRef.visualOutMaxValue;
	public static String SOLUTE_NAME = AspectRef.soluteName;
	public static String FILE_PREFIX = AspectRef.filePrefix;
	public static String OUTPUT_WRITER = AspectRef.graphicalOutputWriter;
	
	/**
	 * The exporter.
	 */
	protected GraphicalExporter _graphics;
	
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
	 * 
	 */
	protected int _pointsOnCurve = 10;
	
	/**
	 * 
	 */
	protected Shape _shape;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * Initiate the process manager from xml node
	 */
	public void init(Element xmlElem, Compartment compartment)
	{
		super.init(xmlElem, compartment);
		
		this._prefix = this.getString(FILE_PREFIX);
		this._shape = compartment.getShape();
		
		this._graphics = GraphicalExporter.getNewInstance(
				this.getString(OUTPUT_WRITER) );
		
		this._graphics.sceneFiles(this._prefix, this._shape);
		
		this._solute = this.getString(SOLUTE_NAME);
		this._maxConcn = ( this.isAspect(MAX_VALUE) ? 
									this.getDouble(MAX_VALUE) : 2.0 );
			
		this._arrayType = ArrayType.CONCN;
		if ( this.isAspect(ARRAY_TYPE) ) 
			this._arrayType = ArrayType.valueOf(this.getString(ARRAY_TYPE));
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{

		
		/* Initiate new file. */
		this._graphics.createFile(this._prefix);
		
		/* Draw computational domain rectangle. */
		// FIXME Safety: this assumes the shape is a rectangle!
		double[] size = _shape.getDimensionLengths();
		
		/* check if this shape is cylindrical or cartesian */
		//TODO Stefan: Maybe we should use another check?
		if (_shape instanceof CartesianShape)
		this._graphics.rectangle( Vector.zeros(size), size, "GRAY");
		else if (_shape instanceof CylindricalShape)
			this._graphics.circle(Vector.zeros(size), 
					_shape.getDimension(DimName.R).getLength(), "GRAY");
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
			_shape = environment.getShape();
			
			SpatialGrid solute = environment.getSoluteGrid(_solute);
			
			int nDim = agents.getNumDims();
			double[] origin;
			double[] dimension = new double[3];
			for ( int[] coord = _shape.resetIterator(); 
					_shape.isIteratorValid(); coord = _shape.iteratorNext() )
			{
				/* Identify exact voxel location and size. */
				origin = _shape.getVoxelOrigin(coord);
				_shape.getVoxelSideLengthsTo(dimension, coord);
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
				/* Write the solute square or circle element. */
				String pigment = "rgb(" + c + "," + c + "," + c + ")";
				if (_shape instanceof CartesianShape)
				this._graphics.rectangle(Vector.subset(origin, nDim), 
							Vector.subset(dimension, nDim),pigment);
				else if (_shape instanceof CylindricalShape)
					this._graphics.circleElement(Vector.zerosDbl(2), origin, 
									dimension, this._pointsOnCurve, pigment);
			}
		}
		/* Draw all located agents. */
		for ( Agent a: agents.getAllLocatedAgents() )
			if ( a.isAspect(BODY) )
			{
				List<Surface> surfaces = ((Body) a.getValue(BODY)).getSurfaces();
				for( Surface s : surfaces)
					this._graphics.draw(s, a.getString(PIGMENT));
			}
		/* Close the file */
		this._graphics.closeFile();
	}
}