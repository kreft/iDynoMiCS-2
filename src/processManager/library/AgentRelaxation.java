package processManager.library;

import java.util.Collection;
import java.util.LinkedList;
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
import linearAlgebra.Vector;
import physicalObject.PhysicalObject;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Point;
import surface.Surface;
import surface.collision.Collision;
import surface.collision.Decompress;
import surface.link.LinearSpring;
import surface.link.Link;
import surface.link.Spring;
import surface.link.TorsionSpring;
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
	 * {@link #_fastRelaxation} or {@link #_stressThreshold}. {@link #_dtStatic}
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
	 * Selected stepping
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
	//private Expression _spineFunction =
	//		new Expression( "stiffness * ( dh + SIGN(dh) * dh * dh * 100.0 )" );
	
	private Boolean _decompression;
	
	private Decompress decompressionMatrix;

	double moveScalar = this._maxMove;
	/* start with initial base time step than adjust */
	double dtMech = this._dtBase;
	/* agent radius */
	double radius;
	/* highest velocity in the system squared */
	double vs;
	/* highest force in the system */
	double st;

	Double pull = null;
	Double searchDist = null;

	double maxAgentOverlap = 0.1;

	double moveGranularity = 0.3;

	double shoveFactor = 1.25;

	/* ************************************************************************
	 * Initiation
	 * ***********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		this.maxAgentOverlap = Helper.setIfNone( this.getDouble(AspectRef.maxAgentOverlap),
				0.1 );

		this.moveGranularity = Helper.setIfNone( this.getDouble(AspectRef.moveGranularity),
				0.3 );

		this.shoveFactor = Helper.setIfNone( this.getDouble(AspectRef.shoveFactor),
				1.25 );

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

		/* force static dt */
		this._dtStatic = Helper.setIfNone( 
				this.getBoolean(STATIC_TIMESTEP), false );
		
		/* Shape of associated compartment */
		this._shape = agents.getShape();
		
		/* Surface objects of compartment, FIXME discovered circle returns a 
		 * rod type shape (2 points) instead of circle (2d sphere, 1 point). */
		this._shapeSurfs  = this._shape.getSurfaces();

		/* FIXME: we have to initiate collision scalar different, currently it is only possible trough globals. */
		if( this._method == Method.SHOVE )
		{
			Global.collision_scalar = 1.0;
		}
		
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
		int nstep = 0;
		tMech = 0.0;
		Collection<Agent> allAgents = this._agents.getAllLocatedAgents();

		/* With higher order ODE solvers, we need additional space to write. */
		switch ( _method )
		{
		case HEUN :
			for( Agent agent : allAgents )
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					point.initialiseC(2);
		default:
			//by default, we do nothing.
		}

		/* Mechanical relaxation */
		while( tMech < this.getTimeStepSize() && nstep < this._maxIter) 
		{
			this._agents.refreshSpatialRegistry();
			this._iterator.resetOverlap();

			this.updateForces( this._agents.getAllAgents(), this._agents );
			dtMech = sizeStep( allAgents );

			if ( this._decompression )
				decompressionMatrix.buildDirectionMatrix();

			for(Agent agent : allAgents )
				if ( agent.isAspect(STOCHASTIC_STEP) )
					agent.event(STOCHASTIC_MOVE, dtMech);

			move( allAgents, dtMech );

			if( Log.shouldWrite(Tier.DEBUG) )
			{
				for (Agent agent : allAgents)
					for (Point point : ((Body) agent.get(BODY)).getPoints())
						if (Double.isNaN(point.getPosition()[0]))
							Log.out(Tier.DEBUG, "encountered NaN in agent Relaxation.");
			}

			/* NOTE that with proper boundary surfaces for any compartment
			 * shape this should never yield any difference, it is here as a
			 * fail safe
			 *
			 * FIXME this seems to result in crashes
			 * */
			for(Agent agent : allAgents)
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					point.setPosition( this._shape.applyBoundaries( point.getPosition() ) );

			nstep++;
			if( dtMech == 0.0 )
				break;
		}
		/* Leave with a clean spatial tree. */
		this._agents.refreshSpatialRegistry();

		if( Log.shouldWrite( Tier.EXPRESSIVE ) )
		{
			if (nstep == this._maxIter )
				Log.out( Tier.EXPRESSIVE, this.getName() +
						" stop condition iterations: " + this._maxIter);
			else
				Log.out( Tier.EXPRESSIVE, this.getName() +
						" reached relaxation criteria, iterations: " + nstep );
		}
	}

	private void move(Collection<Agent> agents, double dtMech)
	{
		/* perform the step using (method) */
		switch ( this._method )
		{
			case SHOVE :
			{
				for ( Agent agent : agents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.shove( this.maxAgentOverlap, this.shoveFactor, agent.getDouble(RADIUS) );
				/* NOTE: is stopped when {@link _stressThreshold} is reached
				 * TODO add max iter for shove? */
				break;
			}
			case EULER :
			{
				for ( Agent agent : agents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.euStep( dtMech, agent.getDouble(RADIUS) );
				tMech += dtMech;
				break;
			}
			/* NOTE : higher order ODE solvers don't like time Leaping..
			 * be careful.  */
			case HEUN :
				for(Agent agent : agents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.heun1( dtMech, agent.getDouble(RADIUS) );
				this.updateForces( agents, this._agents ); // subset
				for(Agent agent : agents )
					for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
						point.heun2( dtMech, agent.getDouble(RADIUS) );
				tMech += dtMech;
				break;
		}
	}

	private double sizeStep(Collection<Agent> agents)
	{
		/* adjust step size unless static step size is forced */
		if( !this._dtStatic || this._method == Method.SHOVE )
		{
			/* obtain current highest particle velocity. */
			double ts;
			vs = 0.0;
			for(Agent agent : agents ) {
				radius = agent.getDouble(RADIUS);
				for ( Point point : ( (Body) agent.get(BODY) ).getPoints() ) {
					if ( ( ts = Vector.normSquare( point.dxdt(radius) ) ) > vs )
						vs = ts;
				}
			}

			if ( this._iterator.maxOverlap() > -maxAgentOverlap)
			{
				// system relaxed can stop loop
				return 0.0;
			}

			/* NOTE: stochastic movement really should only be used with static dt.
			 *
			 * When stochastic movement is enabled update vs to represent
			 * the highest velocity object in the system accounting for
			 * stochastic movement to. */
			for( Agent agent : agents )
				if ( agent.isAspect( STOCHASTIC_DIRECTION ) )
				{
					double[] move =
							(double[]) agent.get( STOCHASTIC_DIRECTION );
					vs = Math.max( Vector.dotProduct( move, move ), vs );
				}

			/* Scale dt such that the fastest moving object is moving a fraction of the
			* largest overlap. */
			moveScalar = Math.min( -this.moveGranularity *
					_iterator.maxOverlap(), this._maxMove );
			dtMech = moveScalar / (Math.sqrt(vs) + 1e-9 );
		}

		/* prevent to relaxing longer than the global _timeStepSize */
		if (dtMech > this._timeStepSize - tMech )
			dtMech = this._timeStepSize - tMech;

		return dtMech;
	}

	/**
	 * \brief Update all forces on all agent mass points.
	 * @param agents
	 * @param aContainer
	 */
	private Collection<Agent> updateForces(Collection<Agent> agents,
										   AgentContainer aContainer, boolean hs)
	{
		Collection<Agent> out = new LinkedList<Agent>();
		/* Calculate forces. */
		for ( Agent agent: agents )
		{
			Body body = (Body) agent.get(AspectRef.agentBody);
			List<Surface> agentSurfs = body.getSurfaces();

			if( hs )
			{
				for( Point p : ((Body) agent.get(BODY)).getPoints())
				{
					p.resetForce();
				}
			}
			
			/* spring operations */
			springEvaluation(agent, body);
			/* Look for neighbors and resolve collisions */
			neighboorhoodEvaluation(agent, agentSurfs, aContainer);
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
		return out;
	}


	private Collection<Agent> updateForces(Collection<Agent> agents,
										   AgentContainer aContainer) {
		return updateForces(agents, aContainer, false);
	}

	private void updateShapeForceOnly(Collection<Agent> agents, AgentContainer aContainer)
	{
		for ( Agent agent: agents ) {
			Body body = (Body) agent.get(AspectRef.agentBody);
			List<Surface> agentSurfs = body.getSurfaces();
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
	 * @param agentContainer (all agents in the compartment).
	 */
	private Collection<Agent> neighboorhoodEvaluation(Agent agent, List<Surface> surfaces,
										 AgentContainer agentContainer, boolean hs)
	{
		searchDist = (agent.isAspect(SEARCH_DIST) ?
				agent.getDouble(SEARCH_DIST) : 0.0);
		Collection<Agent> nhbs = agentContainer.treeSearch(agent, searchDist);
		/* Perform neighborhood search and perform collision detection and
		 * response. */
		for ( Agent neighbour: nhbs )
			if ( agent.identity() > neighbour.identity() )
			{
				/* obtain maximum distance for which pulls should be considered
				 */
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
		return nhbs;
	}

	private Collection<Agent> neighboorhoodEvaluation(Agent agent, List<Surface> surfaces,
													  AgentContainer agentContainer)
	{
		return neighboorhoodEvaluation(agent, surfaces,	agentContainer, false);
	}

	/**
	 *\brief update the forces currently acting upon the mass points due to the
	 * connecting spinal spring of rod type agents.
	 * 
	 * @param a the vocal agent (once per step per surface of the agent)
	 * @param b Body of the agent
	 */
	private void springEvaluation(Agent a, Body b)
	{
		for( Link l : b.getLinks() )
		{
			l.update();
		}
		for( Spring s : b.getSpringsToEvaluate())
		{
			if( s != null )
			{
				if( !s.ready())
				{
					/* possible change to set vars */
					s.setStiffness( Helper.setIfNone( a.getDouble(STIFFNESS), 
							1.0));
					if( s instanceof LinearSpring)
					{
						Expression spineFun;
						if ( !Helper.isNullOrEmpty( a.getValue(
								AspectRef.agentSpineFunction )))
							spineFun = new Expression((String) 
									a.getValue(AspectRef.agentSpineFunction ));
						else
							spineFun = Global.fallback_spinefunction;
						s.setSpringFunction( spineFun );
					}
					else if( s instanceof TorsionSpring )
					{
						Expression torsFun = null;
						if ( !Helper.isNullOrEmpty( 
								a.getValue(AspectRef.torsionFunction)))
							torsFun = (Expression) 
								a.getValue(AspectRef.torsionFunction);
						else
						{
							/* TODO set default maybe? */
							Idynomics.simulator.interupt(
									"missing torsion spring function in relax");
						}
						s.setSpringFunction( torsFun );
					}
				}
				s.applyForces(this._shape);
				if(Log.shouldWrite(Tier.DEBUG))
					Log.out(Tier.DEBUG,s.toString());
			}
		}
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
