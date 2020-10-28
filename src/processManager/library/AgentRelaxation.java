package processManager.library;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;
import idynomics.Global;
import idynomics.Idynomics;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import physicalObject.PhysicalObject;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.LinearSpring;
import surface.Point;
import surface.Rod;
import surface.Spring;
import surface.Surface;
import surface.TorsionSpring;
import surface.collision.Collision;
import surface.collision.Decompress;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentRelaxation extends ProcessManager
{
	
	public String COLLISION_FUNCTION = AspectRef.collisionFunction;
	public String ATTRACTION_FUNCTION = AspectRef.attractionFunction;
	
	public String SEARCH_DIST = AspectRef.collisionSearchDistance;
	public String PULL_EVALUATION = AspectRef.collisionPullEvaluation;
	public String CURRENT_PULL_DISTANCE = AspectRef.collisionCurrentPullDistance;
	public String RELAXATION_METHOD = AspectRef.collisionRelaxationMethod;
	public String FAST_RELAXATION = AspectRef.fastAgentRelaxation;
	public String STATIC_TIMESTEP = AspectRef.staticAgentTimeStep;
	
	public String BASE_DT = AspectRef.collisionBaseDT;
	public String MAX_MOVEMENT = AspectRef.collisionMaxMOvement;
	public String MAX_ITERATIONS = AspectRef.maxIterations;
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;
	public String DIVIDE = AspectRef.agentDivision;
	public String MASS = AspectRef.agentMass;
	
	public String UPDATE_BODY = AspectRef.bodyUpdate;
	public String EXCRETE_EPS = AspectRef.agentExcreteEps;
	
	public String STOCHASTIC_DIRECTION = AspectRef.agentStochasticDirection;
	public String STOCHASTIC_STEP = AspectRef.agentStochasticStep;
	public String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	
	public String STATIC_AGENT = AspectRef.staticAgent;
	
	public String LOW_STRESS_SKIP = AspectRef.stressThreshold;
	public String GRAVITY = AspectRef.gravity_testing;
	public String COMPRESSION_DURATION = AspectRef.LimitCompressionDuration;
	public String STIFFNESS = AspectRef.spineStiffness;
	
	public String SPINE_FUNCTION = AspectRef.genreicSpineFunction;
	
	public String DECOMPRESSION = AspectRef.agentDecompression;
	public String DECOMPRESSION_CELL_LENGTH = AspectRef.decompressionCellLength;
	private String DECOMPRESSION_THRESHOLD = AspectRef.decompressionThreshold;
	
	
	/**
	 * Available relaxation methods.
	 */
	private enum Method
	{
		/**
		 * TODO
		 */
		SHOVE,
		/**
		 * Euler's method.
		 */
		EULER,
		/**
		 * Heun's method.
		 */
		HEUN, 
	}
	
	/* *
	 * Relaxation parameters (overwritten by init)
	 */
	
	/**
	 * Base time step, time step that will be used in mechanical relaxation
	 * unless it is over ruled due to the use of {@link #_maxMove}, 
	 * {@link #_timeLeap} or {@link #_stressThreshold}. {@link #_dtStatic} 
	 * forces the use of the base time step.
	 */
	protected double _dtBase;
	
	/**
	 * force static {@link #_dtBase} in relaxation thereby ignoring max movement,
	 * time leaping and stress threshold (and thus also does not quantify
	 * related variables).
	 */
	private boolean _dtStatic;
	
	/**
	 * The maximum displacement of any given object in a single times step, note
	 * this is ignored if {@link #_dtStatic} is enabled.
	 */
	private double _maxMove;
	
	/**
	 * previously known as _timeLeap. with the option enabled dtMech 
	 * is always scaled to the fastest moving object + the 
	 * {@link Global#agent_move_safety} margin. Disabling will cap the maximum
	 * dtMech.
	 */
	private boolean _fastRelaxation;
	
	/**
	 * Selected stepping {@link #Method}
	 */
	private Method _method;
	
	/**
	 * Collision iterator object, used to evaluate physical agent 
	 * interactions. (see {@link Collision} java doc).
	 */
	private Collision _iterator;
	
	/**
	 * The Shape object associated with the current compartment. (See
	 * {@link Shape} java doc).
	 */
	private Shape _shape;
	
	/**
	 * Collection holds all solid surfaces of the {@link #_shape}.
	 */
	private Collection<Surface> _shapeSurfs;
	
	/**
	 * value under which the relaxation may be considered completed
	 */
	private double _stressThreshold;
	
	/**
	 * 
	 */
	private Integer _maxIter;
	
	/**
	 * enable gravity/buoyancy forces
	 */
	private Boolean _gravity;
	
	/**
	 * Current tMech
	 */
	private double tMech = 0.0;
	
	/** 
	 * limit duration of biofilm compression 
	 */
	private double compresionDuration = 0.0;
	
	/**
	 * TODO check whether implementation is finished
	 * Default spine function, fall back for if none is defined by the agent.
	 */
	private Expression _spineFunction = 
			new Expression( "stiffness * ( dh + SIGN(dh) * dh * dh * 100.0 )" );
	
	private Boolean _decompression;
	
	private Decompress decompressionMatrix;


	
	/* ************************************************************************
	 * Initiation
	 * ***********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		/* Obtaining relaxation parameters. 
		 * Base time step */
		this._dtBase = Helper.setIfNone( this.getDouble(BASE_DT), 
				Global.mechanical_base_step );
		
		/* Maximum displacement per step, set default if none */
		this._maxMove = Helper.setIfNone( this.getDouble(MAX_MOVEMENT), 
				Global.mechanical_max_movement );
		
		/* Maximum displacement per step, set default if none */
		this._maxIter = (Integer) Helper.setIfNone( this.getInt(MAX_ITERATIONS), 
				Global.mechanical_max_iterations );
		
		/* Set relaxation method, set default if none */
		this._method = Method.valueOf( Helper.setIfNone(
				this.getString(RELAXATION_METHOD), Method.EULER.toString() ) );
		
		/* Time leaping */
		this._fastRelaxation = Helper.setIfNone( 
				this.getBoolean(FAST_RELAXATION), false );
		
		/* force static dt (ignores maxMovement, timeleap, stress thresholds
		 * but thereby does not has to evaluate these constituants either */
		this._dtStatic = Helper.setIfNone( 
				this.getBoolean(STATIC_TIMESTEP), false );
		
		/* Shape of associated compartment */
		this._shape = agents.getShape();
		
		/* Surface objects of compartment, FIXME discovered circle returns a 
		 * rod type shape (2 points) instead of circle (2d sphere, 1 point). */
		this._shapeSurfs  = this._shape.getSurfaces();
		
		/* Collision iterator */
		this._iterator = new Collision( this.getString(COLLISION_FUNCTION),
				this.getString(ATTRACTION_FUNCTION), this._shape);
		
		/* Stress threshold, used to skip remaining steps on very low stress,
		 * 0.0 by default */
		this._stressThreshold = Helper.setIfNone( 
				this.getDouble(LOW_STRESS_SKIP), 0.0 );
		
		/* Include gravity / buoyancy ( experimental ) */
		this._gravity = Helper.setIfNone( this.getBoolean(GRAVITY), false);

		/* Limit the duration of biofilm compression */
		this.compresionDuration = Helper.setIfNone( 
				this.getDouble(COMPRESSION_DURATION), 0.0 );
		
		/* Set default spine function for rod type agents, this function is
		 * used if it is not overwritten by the agent, obtain
		 * ComponentExpression from process manager otherwise fall back default
		 * is used. */
		if ( ! Helper.isNullOrEmpty( this.getValue(SPINE_FUNCTION) ) )
			this._spineFunction = (Expression) this.getValue(SPINE_FUNCTION);
		
		/* Include decompression */
		this._decompression = Helper.setIfNone( this.getBoolean(DECOMPRESSION), 
				false);
		

		if ( this._decompression )
			decompressionMatrix = new Decompress( 
					this._agents.getShape().getDimensionLengths(),
					Helper.setIfNone(this.getDouble(DECOMPRESSION_CELL_LENGTH), //possibly change to must set since very depended on case.
					0.0 ), Helper.setIfNone(this.getDouble(DECOMPRESSION_THRESHOLD), 
					this._stressThreshold ), 
					this._agents.getShape().getIsCyclicNaturalOrderIncludingVirtual(),
					(double) this.getOr(AspectRef.traversingFraction, Global.traversing_fraction),
					(double) this.getOr(AspectRef.dampingFactor, Global.damping_factor));
		
	}

	/* ************************************************************************
	 * STEPPING
	 * ***********************************************************************/

	@Override
	protected void internalStep()
	{
		/* current step of mechanical relaxation */
		int nstep = 0;
		/* current time in mechanical relaxation. */
		tMech = 0.0;
		/* start with initial base time step than adjust */
		double dtMech = this._dtBase; 
		/* agent radius */
		double radius;
		/* highest velocity in the system squared */
		double vs;
		/* highest force in the system */
		double st;
		/* All located agents in this compartment */
		Collection<Agent> allAgents = this._agents.getAllLocatedAgents();

		/* if higher order ODE solvers are used we need additional space to 
		 * write. */
		switch ( _method )
		{
		case HEUN :
			for( Agent agent : allAgents )
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					point.initialiseC(2);
		default:
		}

		/* Mechanical relaxation */
		while( tMech < this.getTimeStepSize() && nstep < this._maxIter) 
		{	
			this._agents.refreshSpatialRegistry();
			this.updateForces( this._agents );
			if ( this._decompression )
				decompressionMatrix.buildDirectionMatrix();
			
			/* adjust step size unless static step size is forced */
			if( !this._dtStatic || this._method == Method.SHOVE )
			{
				/* obtain current highest particle velocity and highest force */
				vs = 0.0;
				st = 0.0;
				for(Agent agent : allAgents )
				{
					radius = agent.getDouble(RADIUS);
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					{
						if ( Vector.normSquare( point.dxdt( radius ) ) > vs )
							vs = Vector.normSquare( point.dxdt( radius ) );	
						if ( Vector.normSquare( point.getForce() ) > st )
							st = Vector.normSquare( point.getForce() );
					}
				}

				/* Stress Threshold allows finishing relaxation early if the
				 * mechanical stress is low. Default value is 0.0 -> only skip 
				 * if there is no mechanical stress in the system at all. 
				 * */
				if ( _stressThreshold != 0.0 && ( this.tMech > this.compresionDuration || compresionDuration == 0.0 ) )
				{
					if ( Math.sqrt(st) * Global.agent_stress_scaling < 
							_stressThreshold )
						break;
				} else if ( this._method == Method.SHOVE )
				{
					if (_stressThreshold == 0.0)
					{
					Log.out(Tier.CRITICAL, AspectRef.stressThreshold + " must "
							+ "be set for relaxation method " + 
							Method.SHOVE.toString() );
					Idynomics.simulator.interupt(null);
					return;
					}
				}
	
				/* When stochastic movement is enabled update vs to represent 
				 * the highest velocity object in the system accounting for 
				 * stochastic movement to. */
				for( Agent agent : allAgents )
					if ( agent.isAspect( STOCHASTIC_DIRECTION ) )
					{
						double[] move = 
								(double[]) agent.get( STOCHASTIC_DIRECTION );
						vs = Math.max( Vector.dotProduct( move, move ), vs );
					}
				
				/* fast relaxation: set the time step to match 'maxMovement'
				 * with the the fastest object, for a 'fast' run. */
				if ( this._fastRelaxation || this._method == Method.SHOVE) 
					dtMech = this._maxMove / ( Math.sqrt(vs) + 
							Global.agent_move_safety );
				/* no fast relaxation: adjust the time step with the fastest
				 * object yet cap the maximum dtMech */
				else
					dtMech = this._maxMove / Math.max( Math.sqrt(vs) + 
							Global.agent_move_safety, 1.0 );
				/* TODO we may want to make the dt cap settable as well */
			}
			
			/* prevent to relaxing longer than the global _timeStepSize */
			if (dtMech > this._timeStepSize - tMech )
				dtMech = this._timeStepSize - tMech;

			/* If stochastic movement is enabled for the agent, update the agent
			 * perform the stochastic movement.  */
			for(Agent agent : allAgents )
				if ( agent.isAspect(STOCHASTIC_STEP) )
					agent.event(STOCHASTIC_MOVE, dtMech);

			/* perform the step using (method) */
			switch ( this._method )
			{
			case SHOVE :
			{
				for ( Agent agent : allAgents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.shove( dtMech, agent.getDouble(RADIUS) );
				/* NOTE: is stopped when {@link _stressThreshold} is reached 
				 * TODO add max iter for shove? */
				break;
			}
			case EULER :
			{
				for ( Agent agent : allAgents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.euStep( dtMech, agent.getDouble(RADIUS) );
				tMech += dtMech;
				break;
			}
				/* NOTE : higher order ODE solvers don't like time Leaping.. 
				 * be careful.  */
			case HEUN :
				for(Agent agent : allAgents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.heun1( dtMech, agent.getDouble(RADIUS) );
				this.updateForces( this._agents );
				for(Agent agent : allAgents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.heun2( dtMech, agent.getDouble(RADIUS) );
				tMech += dtMech;
				break;
			}

			/* NOTE that with proper boundary surfaces for any compartment
			 * shape this should never yield any difference, it is here as a
			 * fail safe 
			 * 
			 * FIXME this seems to result in crashes
			 * */
			for(Agent agent : allAgents)
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					this._shape.applyBoundaries( point.getPosition() );
			
			nstep++;
		}
		
		/* Leave with a clean spatial tree. */
		this._agents.refreshSpatialRegistry();
		
		/* Notify user */
		if( Log.shouldWrite( Tier.EXPRESSIVE ) )
		{
			if (nstep == this._maxIter )
				Log.out( Tier.EXPRESSIVE, this.getName() +
						" stop condition iterations: " + this._maxIter);
			else
				Log.out( Tier.EXPRESSIVE, this.getName() +
						" stop condition stress threshold");
		}
		if( Log.shouldWrite( Tier.DEBUG ) )
			Log.out( Tier.DEBUG, "Relaxed " + this._agents.getNumAllAgents() + 
					" agents after " + nstep + " iterations" );
	}
	
	
	/**
	 * \brief Update all forces on all agent mass points.
	 * 
	 * @param environment 
	 * @param agents
	 */
	private void updateForces(AgentContainer agents) 
	{
		/* Calculate forces. */
		for ( Agent agent: agents.getAllLocatedAgents() ) 
		{
			Body body = (Body) agent.get(AspectRef.agentBody);
			List<Surface> agentSurfs = body.getSurfaces();

			
//			/* surface operations */
//			for ( Surface s : agentSurfs )
//				if ( s instanceof Rod )
			spineEvaluation(agent, body);
			
			/* Look for neighbors and resolve collisions */
			neighboorhoodEvaluation(agent, agentSurfs, agents);
			
			/*
			 * Collisions with other physical objects and
			 * Boundary collisions
			 */
			for( PhysicalObject p : this._agents.getAllPhysicalObjects() )
			{
				this._iterator.collision(p.getSurface(), p, agentSurfs, agent, 0.0);
			}
			
			
			/* NOTE: testing purposes only */
			if (this._gravity)
				gravityEvaluation(agent, body);

			if ( this._decompression )
				for( Point p : ((Body) agent.get(BODY)).getPoints())
				{
					decompressionMatrix.addPressure(
							p.getPosition(), Vector.normEuclid(p.getForce()));
					p.addToForce(decompressionMatrix.getDirection(p.getPosition()));
				}
			
			/*
			 * TODO friction
			 * FIXME here we need to selectively apply surface collision methods
			 */
			this._iterator.collision(this._shapeSurfs, null, agentSurfs, agent, 0.0);
		}
	}
	
	/**
	 * \brief Perform neighborhood search and perform collision detection and 
	 * response.
	 * 
	 * interaction for consideration is passed to the collision iterator, the
	 * collision iterator evaluates both push and pull interactions (within
	 * the pull distance).
	 * 
	 * @param agent the vocal agent (only once per time step per agent).
	 * @param surfaces (the surfaces of the agent).
	 * @param agents (all agents in the compartment).
	 */
	private void neighboorhoodEvaluation(Agent agent, List<Surface> surfaces, 
			AgentContainer agents)
	{
		double searchDist = (agent.isAspect(SEARCH_DIST) ?
				agent.getDouble(SEARCH_DIST) : 0.0);
		
		/* Perform neighborhood search and perform collision detection and
		 * response. */
		Collection<Agent> nhbs = agents.treeSearch(agent, searchDist);
		for ( Agent neighbour: nhbs )
			if ( agent.identity() > neighbour.identity() )
			{
				/* obtain maximum distance for which pulls should be considered
				 */
				Double pull = null;
				if( searchDist != 0.0 )
				{
					agent.event(PULL_EVALUATION, neighbour);
					pull = agent.getDouble(CURRENT_PULL_DISTANCE);
				}
				if ( pull == null || pull.isNaN() )
					pull = 0.0;

				/* pass this agents and neighbor surfaces as well as the pull
				 * region to the collision iterator to update the net forces. */
				this._iterator.collision(surfaces, agent, 
						((Body) neighbour.get(BODY)).getSurfaces(), neighbour, 
						pull);
			}
	}

	/**
	 *\brief update the forces currently acting upon the mass points due to the
	 * connecting spinal spring of rod type agents.
	 * 
	 * @param agent the vocal agent (once per step per surface of the agent)
	 * @param s surface of the agent
	 */
	private void spineEvaluation(Agent agent, Body b)
	{
		for( Spring s : b.getSprings())
		{
			if( !s.ready())
			{
				/* possible change to set vars */
				s.setStiffness( Helper.setIfNone( agent.getDouble(STIFFNESS), 
						5.0));
				if( s instanceof LinearSpring)
				{
					Expression spineFun;
					if ( !Helper.isNullOrEmpty( agent.getValue(SPINE_FUNCTION)))
						spineFun = (Expression) agent.getValue(SPINE_FUNCTION);
					else
						spineFun = this._spineFunction;
					s.setSpringFunction( spineFun );
				}
				else if( s instanceof TorsionSpring )
				{
					Expression torsFun = null;
					if ( !Helper.isNullOrEmpty( agent.getValue(AspectRef.torsionFunction)))
						torsFun = (Expression) agent.getValue(AspectRef.torsionFunction);
					else
					{
						/* TODO set default maybe? */
						Idynomics.simulator.interupt(
								"missing torsion spring function in agentrelax");
					}
					s.setSpringFunction( torsFun );
				}
			}
			s.applyForces(this._shape);
		}
		
//		/*
//		 * calculate rest length of rod cell spine spring
//		 * total volume - sphere volume = cylinder volume ->
//		 * cylinder length = rest length
//		 */
//		double l = ((Rod) s)._length;
//		double stiffness = Helper.setIfNone( agent.getDouble(STIFFNESS), 10.0);
//
//		/*
//		 * calculate current length of spine spring
//		 */
//		Point a = ((Rod) s)._points[0];
//		Point b = ((Rod) s)._points[1];
//		double[] diff = this._shape.getMinDifferenceVector( 
//				a.getPosition(), b.getPosition() );
//		double dn = Vector.normEuclid(diff);
//		
//		/* rod type agent spine function, replacing hard coded Hooke's law
//		 * double[] fV	= Vector.times(diff, stiffness * (dn - l));  */
//		HashMap<String, Double> springVars = new HashMap<String,Double>();
//		springVars.put("stiffness", stiffness);
//		springVars.put("dh", dn-l);
//		Expression spine;
//		
//		/* Obtain ComponentExpression from agent otherwise use the
//		 * default expression */
//		if (agent.isAspect(SPINE_FUNCTION))
//			spine = (Expression) this.getValue(SPINE_FUNCTION);
//		else
//			spine = this._spineFunction;
//		
//		double fs		= spine.getValue(springVars);
//		double[] fV		= Vector.times(diff, fs);
//	
//		/* apply forces */
//		Vector.addEquals( b.getForce(), fV ) ;
//		Vector.reverseEquals(fV);
//		Vector.addEquals( a.getForce(), fV ) ;
	}

	/**
	 * FIXME this all needs to be properly set from the protocol file if we
	 * we start using this
	 * \brief gravityEvaluation,
	 * 
	 * NOTE: testing purposes only
	 * graf 9.81 m/s2 ~ 35e9 µm/min2
	 * 
	 * density difference 1 - ( ρ solute / ρ microbe )
	 * 
	 * TODO sort out the forces for RC, this needs to become fully
	 * settable from protocol file in final version.
	 * 
	 * @param agent the vocal agent (once per time step per agent)
	 * @param body the body of the agent
	 */
	private void gravityEvaluation(Agent agent, Body body)
	{
		if ( tMech < compresionDuration || compresionDuration == 0.0 )
		{
			/* note should be mass per point */
			double fg = agent.getDouble(MASS) * 1e-12 * 35.316e9 /* 1E16 */ * Global.density_difference;
			double[] fgV;
			
			if( this._shape.isOriented() )
			{
				fgV = Vector.times(this._shape.getOrientation().inverse(), fg );
			} else {
				if( this._shape.getNumberOfDimensions() == 3)
					 fgV = Vector.times(new double[]{ 0, 0, -1 }, fg );
				else
					 fgV = Vector.times(new double[]{ 0, -1 }, fg );
			}
			
			for ( Point p : body.getPoints() )
				Vector.addEquals( p.getForce(), fgV ) ;
		}
	}
}
