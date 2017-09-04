package processManager.library;

import java.util.List;
import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;

import static dataIO.Log.Tier.*;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Collision;
import surface.Point;
import surface.Rod;
import surface.Surface;
import utility.Helper;



////////////////////////
// WORK IN PROGRESS, initial version
////////////////////////

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentRelaxation extends ProcessManager
{
	public String SEARCH_DIST = AspectRef.collisionSearchDistance;
	public String PULL_EVALUATION = AspectRef.collisionPullEvaluation;
	public String CURRENT_PULL_DISTANCE = AspectRef.collisionCurrentPullDistance;
	public String RELAXATION_METHOD = AspectRef.collisionRelaxationMethod;
	
	public String BASE_DT = AspectRef.collisionBaseDT;
	public String MAX_MOVEMENT = AspectRef.collisionMaxMOvement;
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;
	public String DIVIDE = AspectRef.agentDivision;
	
	public String UPDATE_BODY = AspectRef.bodyUpdate;
	public String EXCRETE_EPS = AspectRef.agentExcreteEps;
	
	public String STOCHASTIC_DIRECTION = AspectRef.agentStochasticDirection;
	public String STOCHASTIC_STEP = AspectRef.agentStochasticStep;
	public String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	
	public String STATIC_AGENT = AspectRef.staticAgent;
	
	public String LOW_STRESS_SKIP = AspectRef.stressThreshold;
	public String GRAVITY = AspectRef.gravity_testing;
	public String STIFFNESS = AspectRef.spineStiffness;
	
	public String SPINE_FUNCTION = AspectRef.genreicSpineFunction;
	
	
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
	/**
	 * Relaxation parameters (overwritten by init)
	 */
	protected double _dtBase;	
	
	/**
	 * TODO
	 */
	private double _maxMove;
	
	/**
	 * TODO
	 */
	private Method _method;
	
	/**
	 * TODO
	 */
	private boolean _timeLeap;
	
	/**
	 * TODO
	 */
	private Collision _iterator;
	
	/**
	 * 
	 */
	private Shape _shape;
	
	/**
	 * 
	 */
	private Collection<Surface> _shapeSurfs;
	
	/**
	 * value under which the relaxation may be considered completed
	 */
	private double _stressThreshold;
	
	/**
	 * enable gravity/buoyancy forces
	 */
	private Boolean _gravity;
	
	/**
	 * TODO check whether implementation is finished
	 * Default spine function, fall back for if none is defined by the agent.
	 */
	private Expression _spineFunction = 
			new Expression( "stiffness * ( dh + SIGN(dh) * dh * dh * 100.0 )" );
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		/* Obtaining relaxation parameters. 
		 * Base time step */
		this._dtBase = Helper.setIfNone( this.getDouble(BASE_DT), 0.0003 );	
		/* Maximum displacement per step, set default if none */
		this._maxMove = Helper.setIfNone( this.getDouble(MAX_MOVEMENT), 0.01 );	
		/* Set relaxation method, set default if none */
		this._method = Method.valueOf( Helper.setIfNone(
				this.getString(RELAXATION_METHOD), Method.EULER.toString() ) );
		/* Time leaping */
		this._timeLeap	= true;
		/* Shape of associated compartment */
		this._shape = agents.getShape();
		/* Surface objects of compartment, FIXME discovered circle returns a 
		 * rod type shape (2 points) instead of circle (2d sphere, 1 point). */
		this._shapeSurfs  = this._shape.getSurfaces();
		/* Collision iterator */
		this._iterator = this._shape.getCollision();
		/* Stress threshold, used to skip remaining steps on very low stress,
		 * 0.0 by default */
		this._stressThreshold = Helper.setIfNone( 
				this.getDouble(LOW_STRESS_SKIP), 0.0 );
		/* Include gravity / buoyancy ( experimental ) */
		this._gravity = Helper.setIfNone( this.getBoolean(GRAVITY), false);
		/* Set default spine function for rod type agents, this function is
		 * used if it is not overwritten by the agent, obtain
		 * ComponentExpression from process manager otherwise fall back default
		 * is used. */
		if ( ! Helper.isNone( this.getValue(SPINE_FUNCTION) ) )
			this._spineFunction = (Expression) this.getValue(SPINE_FUNCTION);
	}

	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * \brief Update forces on all agent mass points.
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

			/* surface operations */
			for ( Surface s : agentSurfs )
			{
				/* rod surfs */
				if ( s instanceof Rod )
				{
					/*
					 * calculate rest length of rod cell spine spring
					 * total volume - sphere volume = cylinder volume ->
					 * cylinder length = rest length
					 */
					double l = ((Rod) s)._length;
					double stiffness = Helper.setIfNone(
							agent.getDouble(STIFFNESS), 10.0);

					/*
					 * calculate current length of spine spring
					 */
					Point a = ((Rod) s)._points[0];
					Point b = ((Rod) s)._points[1];
					double[] diff = this._shape.getMinDifferenceVector(
							a.getPosition(), b.getPosition() );
					double dn = Vector.normEuclid(diff);
					
					/*
					 * rod type agent spine function, replacing hard coded
					 * Hooke's law
					 * double[] fV	= Vector.times(diff, stiffness * (dn - l));
					 */
					HashMap<String, Double> springVars = 
							new HashMap<String,Double>();
					springVars.put("stiffness", stiffness);
					springVars.put("dh", dn-l);
					Expression spine;
					/* Obtain ComponentExpression from agent otherwise use the
					 * default expression */
					if (agent.isAspect(SPINE_FUNCTION))
						spine = (Expression) this.getValue(SPINE_FUNCTION);
					else
						spine = this._spineFunction;
					double fs		= spine.getValue(springVars);
					double[] fV		= Vector.times(diff, fs);
				
					/*
					 * apply forces
					 */
					Vector.addEquals( b.getForce(), fV ) ;
					Vector.reverseEquals(fV);
					Vector.addEquals( a.getForce(), fV ) ;
				}
			}
			
			double searchDist = (agent.isAspect(SEARCH_DIST) ?
					agent.getDouble(SEARCH_DIST) : 0.0);
			
			/*
			 * Perform neighborhood search and perform collision detection and
			 * response. 
			 */
			Collection<Agent> nhbs = agents.treeSearch(agent, searchDist);
			for ( Agent neighbour: nhbs )
				if ( agent.identity() > neighbour.identity() )
				{
					agent.event(PULL_EVALUATION, neighbour);
					Double pull = agent.getDouble(CURRENT_PULL_DISTANCE);
					if ( pull == null || pull.isNaN() )
						pull = 0.0;
					body = ((Body) neighbour.get(BODY));
					List<Surface> t = body.getSurfaces();
					this._iterator.collision(agentSurfs, t, pull);
				}
			/*
			 * Boundary collisions
			 * 
			 * TODO friction
			 */
			// FIXME here we need to selectively apply surface collision methods
			this._iterator.collision(this._shapeSurfs, agentSurfs, 0.0);
			
			/*
			 * NOTE: testing purposes only
			 * graf 9.81 m/s2 ~ 35e9 µm/min2
			 * 
			 * density difference 1 - ( ρ solute / ρ microbe )
			 * 
			 * TODO sort out the forces for RC, this needs to become fully
			 * settable from protocol file in final version.
			 */
			if (this._gravity)
			{
				/* note should be mass per point */
				double fg = agent.getDouble("mass") * 1e-12 * 35.316e9;
				double[] fgV = Vector.times(new double[]{ 0, 0, -1 }, fg );
				
				body = (Body) agent.get(AspectRef.agentBody);
				for ( Point p : body.getPoints() )
					Vector.addEquals( p.getForce(), fgV ) ;
			}
		}
	}


	@Override
	protected void internalStep()
	{
		/* current step of mechanical relaxation */
		int nstep = 0;
		/* current time in mechanical relaxation. */
		double tMech = 0.0;
		/* start with initial base time step than adjust */
		double dtMech = this._dtBase; 
		/* agent radius */
		double radius;
		/* highest velocity in the system squared */
		double vs;
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
		while( tMech < _timeStepSize ) 
		{	
			this._agents.refreshSpatialRegistry();
			this.updateForces( this._agents );

			/* obtain current highest particle velocity */
			vs = 0.0;
			for(Agent agent : allAgents )
			{
				radius = agent.getDouble(RADIUS);
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					if ( Vector.normSquare( point.dxdt( radius ) ) > vs )
						vs = Vector.normSquare( point.dxdt( radius ) );		
			}

			/* FIXME this assumes linear force scaling improve.. */
			vs = vs * Math.pow( _iterator.getMaxForceScalar(), 2.0 );
			
			/* Stress Threshold allows finishing relaxation early if the
			 * mechanical stress is low. Default value is 0.0 -> only skip if
			 * there is no mechanical stress in the system at all. */
			if ( vs < _stressThreshold )
				break;

			/* When stochastic movement is enabled update vs to represent the
			 * highest velocity object in the system accounting for stochastic
			 * movement to. */
			for( Agent agent : allAgents )
				if ( agent.isAspect(STOCHASTIC_DIRECTION) )
				{
					double[] move = (double[]) agent.get(STOCHASTIC_DIRECTION);
					vs = Math.max( Vector.dotProduct( move, move ), vs );
				}
			
			/* time Leaping set the time step to match a max traveling distance
			/ divined by 'maxMovement', for a 'fast' run. */
			if ( this._timeLeap ) 
				dtMech = this._maxMove / ( Math.sqrt( vs ) + 0.001 );

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
				/* Continue until nearly all overlap is resolved. */
				if ( vs < 0.001 )
					tMech = this._timeStepSize;
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

			/* Note that with proper boundary surfaces for any compartment
			 * shape this should never yield any difference, it is here as a
			 * fail safe */
			for(Agent agent : allAgents)
				for ( Point point: ( (Body) agent.get(BODY) ).getPoints() )
					this._shape.applyBoundaries( point.getPosition() );
			
			nstep++;
		}
		
		/* Leave with a clean spatial tree. */
		this._agents.refreshSpatialRegistry();
		
		/* Notify user */
		Log.out( Tier.DEBUG, "Relaxed " + this._agents.getNumAllAgents() + 
				" agents after " + nstep + " iterations" );
	}
}
