package processManager.library;

import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.GraphicalExporter;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
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
		/*
		 * Initiate general process manager settings
		 */
		super.init(xmlElem, compartment);

		/* set the shape */
		this._shape = _compartment.getShape();
		
		/* If any, solute to plot */
		this._solute = this.getString(SOLUTE_NAME);
		
		/* ArrayType to plot (CONCN if unspecified_ */
		this._arrayType = ArrayType.valueOf( (String) this.getOr(ARRAY_TYPE, 
				ArrayType.CONCN.toString() ) );
		
		/* Output naming */
		if (_solute == null)
			this._prefix = "agents";
		else
			this._prefix = _solute + "_" + _arrayType.toString();

		/* get instance of appropriate output writer */
		this._graphics = GraphicalExporter.getNewInstance(
				this.getString(OUTPUT_WRITER) );
		
		/* write scene files (used by pov ray) */
		this._graphics.sceneFiles( this._prefix, this._shape );
		
		/* set max concentration for solute grid color gradient */
		this._maxConcn = (double) this.getOr( MAX_VALUE, 2.0 );

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
		
		/* 
		 * Draw computational domain  
		 */
		double[] size = _shape.getDimensionLengths();
		
		/* check if this shape is cylindrical or cartesian */
		//TODO Stefan: Maybe we should use another check?
		
		
		if (_shape instanceof CartesianShape)
			this._graphics.rectangle( Vector.zeros(size), size, "lightblue");
		else if (_shape instanceof CylindricalShape)
			this._graphics.circle(Vector.zeros(size), 
					this._shape.getDimension(DimName.R).getLength(), "lightblue");
		else
			Log.out(Tier.BULK,
					"Computational domain neither rectangular nor circular, "
					+ this._name + " will not draw a computational domain.");
		
		/* Draw solute grid for specified solute, if any. */
		if ( ! environment.isSoluteName(this._solute) )
		{
			//TODO Bas [08/06/16] this should not be a critical warning since
			// this is a sensible option if the user does not want to plot a 
			// solute (null solute).
			Log.out(Tier.BULK, this._name+" can't find solute " + this._solute +
					" in the environment, no solute will be draw");
		}
		else
		{
				
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
				boolean neg = false;
				if ( concn < 0.0 )
				{
					neg = true;
					concn *= -1.0;
				}
					
				concn /= Math.abs(this._maxConcn);
				concn = ExtraMath.restrict(concn, 0.0, 1.0);
				/* Flip this, so that higher concentration is darker. */
				if ( this._higherIsDarker )
					concn = 1.0 - concn;
				/* Map this to the integer interval [0, 255]. */
				int c = (int) Math.round(255.0 * concn);
				/* Write the solute square or circle element. */
				String pigment;
				if (neg)
					pigment = "rgb(" + 255 + "," + c + "," + c + ")";
				else
					pigment = "rgb(" + c + "," + c + "," + c + ")";
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