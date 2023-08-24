package processManager.library;

import static grid.ArrayType.CONCN;

import java.util.*;

import idynomics.Idynomics;
import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import colour.ColourSpecification;
import colour.Palette;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.GraphicalExporter;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Global;
import instantiable.Instance;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Dimension.DimName;
import shape.Shape;
import surface.Rod;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class GraphicalOutput extends ProcessManager
{
	
	public static String BODY = AspectRef.agentBody;
	public static String RADIUS = AspectRef.bodyRadius;
	public static String PIGMENT = AspectRef.agentPigment;
	public String PALETTE = AspectRef.colourPalette;
	
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
	
	protected Palette palette;
	
	protected ColourSpecification colSpec;

	protected double[] filter;

	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * Initiate the process manager from xml node
	 */
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		/*
		 * Initiate general process manager settings
		 */
		super.init(xmlElem, environment, agents, compartmentName);

		String str;
		
		/* set the shape */
		this._shape = agents.getShape();
		
		/* If any, solute to plot */
		this._solute = this.getString(SOLUTE_NAME);
		
		/* ArrayType to plot (CONCN if unspecified_ */
		str = (String) this.getOr(ARRAY_TYPE, CONCN.toString());
		this._arrayType = ArrayType.valueOf(str);

		String filename = this.getString( AspectRef.fileName );
		if( Helper.isNullOrEmpty(filename))
			filename = "agents";
		
		/* Output naming */
		this._prefix = this._compartmentName + "_";
		if ( this._solute == null )
			this._prefix += filename;
		else
			this._prefix += this._solute + "_" + this._arrayType.toString();

		/* get instance of appropriate output writer */
		str = Helper.obtainIfNone( this.getString(OUTPUT_WRITER), 
				"output writer", true, this.options() );
		this._graphics = (GraphicalExporter) Instance.getNew(null, null, str);
		/* write scene files (used by pov ray) */
		this._graphics.init( this._prefix, this._shape );
		
		/* set max concentration for solute grid color gradient */
		this._maxConcn = (double) this.getOr( MAX_VALUE, 2.0 );
		
		this.palette = new Palette( String.valueOf( 
				this.getOr( AspectRef.colourPalette, Global.default_palette) ) );

		HashMap<String, float[]> gradients = null;
		if( this.reg().isGlobalAspect(AspectRef.gradientSpecification) )
			gradients = (HashMap<String, float[]>) this.getValue(AspectRef.gradientSpecification);
		
		/* In the future we may want to change the default to "species" */
		colSpec = new ColourSpecification(palette, (String)
				 this.getOr( AspectRef.colourSpecification, 
						 Global.default_colour_specification), gradients, Idynomics.simulator.getCompartment( this._compartmentName) );

		double[] filt = this.getDoubleA( AspectRef.filter );
		if( !Helper.isNullOrEmpty( filt ) )
			this._graphics.setFilter( filter = filt );
	}
	
	private Collection<String> options()
	{
		LinkedList<String> out = new LinkedList<String>();	
		out.add(ClassRef.svgExport);
		out.add(ClassRef.povExport);
		return out;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep()
	{
		if ( this.getInt(AspectRef.fileNumber) != null )
			this._graphics.setFileNumber(this.getInt(AspectRef.fileNumber));
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
		
		/* Draw solute grid for specified solute, if any. */
		if ( ! _environment.isSoluteName(this._solute) )
		{
			//skip
		}
		else
		{
				
			SpatialGrid solute = _environment.getSoluteGrid(_solute);
			
			int nDim = _agents.getNumDims();
			double[] origin;
			double[] dimension = new double[3];
			for ( int[] coord = _shape.resetIterator(); 
					_shape.isIteratorValid(); coord = _shape.iteratorNext() )
			{
				/* Identify exact voxel location and size. */
				origin = _shape.getVoxelOrigin(coord);
				_shape.getVoxelSideLengthsTo(dimension, coord);
				if( _shape.isNodeSystem() )
					Vector.minusEquals( origin, Vector.times( dimension, 0.5 ) );
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
		/*
		 * ^^
		 */
		List<Agent> sortedAgents = this._agents.getAllLocatedAgents();
		Collections.sort(sortedAgents, new Agent.AgentComparator());

		for ( Agent a: sortedAgents )
			if ( a.isAspect(BODY) )
			{
				List<Surface> surfaces = ((Body) a.getValue(BODY)).getSurfaces();
				/* update multi-point surfaces to use their closest shadow/projection point
				rather than computational domain point to have correct rendering over periodic
				boundaries
				 */
				for( Surface s : surfaces) {
					Surface renderObject = s;
					if( s instanceof Rod) {
						Rod r = (Rod) s;
						Rod projection = new Rod(r);
						projection._points[0].setPosition( Helper.searchClosestCyclicShadowPoint(
								this._shape,
								projection._points[0].getPosition(),
								projection._points[1].getPosition() ) );
						renderObject = projection;
					}
					this._graphics.draw(renderObject, colSpec.colorize(a));
				}
				
					
			}
		/* Close the file */
		this._graphics.closeFile();
	}
}