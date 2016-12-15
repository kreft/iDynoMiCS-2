package dataIO;

import java.util.List;

import agent.Agent;
import agent.Body;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import instantiatable.Instance;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Dimension.DimName;
import shape.Shape;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

/**
 * TODO merge with GraphicalOutput process manager
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class DrawMediator {
	
	public static String BODY = AspectRef.agentBody;
	public static String RADIUS = AspectRef.bodyRadius;
	public static String PIGMENT = AspectRef.agentPigment;
	
	public static String ARRAY_TYPE = AspectRef.gridArrayType;
	public static String MAX_VALUE = AspectRef.visualOutMaxValue;
	public static String SOLUTE_NAME = AspectRef.soluteName;
	public static String FILE_PREFIX = AspectRef.filePrefix;
	public static String OUTPUT_WRITER = AspectRef.graphicalOutputWriter;
	
	private GraphicalExporter _graphics;
	private String _prefix;
	private ArrayType _arrayType;
	private String _solute;
	private Double _maxConcn;
	private Boolean _higherIsDarker = true;
	private Double _pointsOnCurve = 8.0;
	
	public static DrawMediator drawState()
	{
		if (Helper.compartmentAvailable())
		{
			DrawMediator drawy = new DrawMediator();
			if ( Idynomics.simulator.getCompartmentNames().size() == 1)
				drawy.drawState( Idynomics.simulator.getCompartment( 
						Idynomics.simulator.getCompartmentNames().get(0) ) );
			else
				drawy.drawState( Idynomics.simulator.getCompartment( Helper.
						obtainInput( Idynomics.simulator.getCompartmentNames(), 
						"Select compartment", false ) ) );
			return drawy;
		}
		else
			return null;
	}
	
	public void drawState(Compartment compartment)
	{
		EnvironmentContainer _environment = compartment.environment;
		AgentContainer _agents = compartment.agents;
		Shape _shape = compartment.getShape();
		
		if ( _environment.getSoluteNames().size() == 1)
			this._solute = _environment.getSoluteNames().iterator().next(); // not to happy about having to get an iterator to get the only item in the collection.
		if ( Helper.isNone( _solute) )
			this._solute = Helper.obtainInput( _environment.getSoluteNames(), 
					"solute to plot", false);
		
		/* ArrayType to plot (CONCN if unspecified_ */
		if ( Helper.isNone( _arrayType) )
			this._arrayType = ArrayType.valueOf( Helper.obtainInput(
					Helper.enumToStringArray( ArrayType.class ), "array type", false ) );

		
		this._prefix = compartment.name + "_" + _solute + "_" + 
				_arrayType.toString();

		/* get instance of appropriate output writer */
		if ( Helper.isNone(_graphics))
			this._graphics = (GraphicalExporter) Instance.getNew(
					null, null, ClassRef.svgExport,	ClassRef.povExport );
		
		/* write scene files (used by pov ray) */
		this._graphics.init( this._prefix, _shape );
		
		/* set max concentration for solute grid color gradient */
		this._maxConcn = Double.valueOf( Helper.obtainInput(
				String.valueOf( _maxConcn ), "Max concentration value" ) );
		
		this.drawState(_environment, _agents, _shape, Helper.obtainInput("", "fileName"));
	}
	
	public void drawState(EnvironmentContainer _environment, AgentContainer _agents, Shape _shape)
	{
		this.drawState(_environment, _agents, _shape, null);
	}
	
	public void drawState(EnvironmentContainer _environment, AgentContainer _agents, Shape _shape, String fileName)
	{
		/* Initiate new file. */
		if (fileName == null)
			_graphics.createFile(_prefix);
		else
			_graphics.createCustomFile(fileName);
		
		/* 
		 * Draw computational domain  
		 */
		double[] size = _shape.getDimensionLengths();
		
		/* check if this shape is cylindrical or cartesian */
		//TODO Stefan: Maybe we should use another check?
		
		
		if (_shape instanceof CartesianShape)
			_graphics.rectangle( Vector.zeros(size), size, "lightblue");
		else if (_shape instanceof CylindricalShape)
			_graphics.circle(Vector.zeros(size), 
					_shape.getDimension(DimName.R).getLength(), "lightblue");
		else
			Log.out(Tier.BULK,
					"Computational domain neither rectangular nor circular, "
					+ " will not draw a computational domain.");
		
		/* Draw solute grid for specified solute, if any. */
		if ( ! _environment.isSoluteName(_solute) )
		{
			//NOTE Bas [08/06/16] this should not be a critical warning since
			// this is a sensible option if the user does not want to plot a 
			// solute (null solute).
			Log.out(Tier.BULK, " can't find solute " + _solute +
					" in the environment, no solute will be draw");
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
				/*
				 * Scale the solute concentration for coloring.
				 * First, map the concentration to the real interval [0, 1].
				 */
				double concn = solute.getValueAtCurrent(_arrayType);
				boolean neg = false;
				if ( concn < 0.0 )
				{
					neg = true;
					concn *= -1.0;
				}
					
				concn /= Math.abs(_maxConcn);
				concn = ExtraMath.restrict(concn, 0.0, 1.0);
				/* Flip this, so that higher concentration is darker. */
				if ( _higherIsDarker )
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
				_graphics.rectangle(Vector.subset(origin, nDim), 
							Vector.subset(dimension, nDim),pigment);
				else if (_shape instanceof CylindricalShape)
					_graphics.circleElement(Vector.zerosDbl(2), origin, 
									dimension, _pointsOnCurve, pigment);
			}
		}
		/* Draw all located agents. */
		for ( Agent a: _agents.getAllLocatedAgents() )
			if ( a.isAspect(BODY) )
			{
				List<Surface> surfaces = ((Body) a.getValue(BODY)).getSurfaces();
				for( Surface s : surfaces)
					_graphics.draw(s, a.getString(PIGMENT));
			}
		/* Close the file */
		_graphics.closeFile();
	}

}
