package processManager.library;

import java.util.List;
import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.Log.Tier;

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
	 * TODO
	 */
	private double _dtMech;
	
	/**
	 * TODO
	 */
	private double _vSquare;
	
	/**
	 * TODO
	 */
	private double _tMech;
	
	/**
	 * Relaxation parameters (overwritten by init)
	 */
	protected double _dtBase;	
	
	/**
	 * TODO
	 */
	private double _maxMovement;
	
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
	 * TODO gravity buoyancy implementation
	 */
	private Boolean _gravity;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		/*
		 * Obtaining relaxation parameters.
		 */
		this._dtBase = Helper.setIfNone( this.getDouble(BASE_DT), 0.0003 );	
		this._maxMovement = Helper.setIfNone( this.getDouble(MAX_MOVEMENT), 0.01 );	
		this._method = Method.valueOf( Helper.setIfNone(
				this.getString(RELAXATION_METHOD), Method.EULER.toString() ) );
		this._timeLeap	= true;
		
		this._shape = agents.getShape();
		// FIXME discovered circle returns a rod type shape (2 points) instead of circle (2d sphere, 1 point)
		this._shapeSurfs  = this._shape.getSurfaces();
		this._iterator = this._shape.getCollision();
		this._stressThreshold = Helper.setIfNone( this.getDouble(LOW_STRESS_SKIP), 0.0 );	
		this._gravity = Helper.setIfNone( this.getBoolean(GRAVITY), false);
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
		Tier level = BULK;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Updating agent forces");
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
					double stiffness = Helper.setIfNone(agent.getDouble(STIFFNESS), 10.0);

					/*
					 * calculate current length of spine spring
					 */
					Point a 		= ((Rod) s)._points[0];
					Point b 		= ((Rod) s)._points[1];
					double[] diff 	= this._shape.getMinDifferenceVector(
							a.getPosition(), b.getPosition() );
					double dn 		= Vector.normEuclid(diff);
					
					/*
					 * Hooke's law: spring stiffness * displacement
					 */
					double f 		= stiffness * ( dn - l );
					double[] fV		= Vector.times(diff, f);
				
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
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  Agent (ID "+agent.identity()+") has "+
						agentSurfs.size()+" surfaces, search dist "+searchDist);
			}
			/*
			 * Perform neighborhood search and perform collision detection and
			 * response. 
			 */
			Collection<Agent> nhbs = agents.treeSearch(agent, searchDist);
			if ( Log.shouldWrite(level) )
				Log.out(level, "  "+nhbs.size()+" neighbors found");
			for ( Agent neighbour: nhbs )
				if ( agent.identity() > neighbour.identity() )
				{
					agent.event(PULL_EVALUATION, neighbour);
					Double pull = agent.getDouble(CURRENT_PULL_DISTANCE);
					if ( pull == null || pull.isNaN() )
						pull = 0.0;
					body = ((Body) neighbour.get(BODY));
					List<Surface> t = body.getSurfaces();
					if ( Log.shouldWrite(level) )
						Log.out(level, "   interacting with neighbor (ID "+
								neighbour.identity()+") , which has "+t.size()+
								" surfaces, with pull distance "+pull);
					this._iterator.collision(agentSurfs, t, pull, agent, neighbour);
				}
			/*
			 * Boundary collisions
			 */
			// FIXME here we need to selectively apply surface collision methods
			this._iterator.collision(this._shapeSurfs, agentSurfs, 0.0, agent, null);
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " Finished updating agent forces");
	}


	@Override
	protected void internalStep()
	{

		int nstep	= 0;
		_tMech		= 0.0;
		_dtMech 	= this._dtBase; // start with initial base timestep than adjust

		// if higher order ODE solvers are used we need additional space to write.
		switch (_method)
		{
		case HEUN :
			for(Agent agent: this._agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get(BODY)).getPoints())
					point.initialiseC(2);
			break;
		default:
			break;
		}

		// Mechanical relaxation
		while(_tMech < _timeStepSize) 
		{	
			this._agents.refreshSpatialRegistry();
			this.updateForces(this._agents);

			/// obtain current highest particle velocity
			_vSquare = 0.0;
			for(Agent agent: this._agents.getAllLocatedAgents())
			{
				for (Point point: ((Body) agent.get(BODY)).getPoints())
					if ( Vector.normSquare(point.dxdt((double) agent.get(RADIUS))) > _vSquare )
						_vSquare = Vector.normSquare(point.dxdt((double) agent.get(RADIUS)));			
			}

			// FIXME this assumes linear force scaling improve..
			_vSquare = _vSquare * Math.pow(_iterator.getMaxForceScalar(), 2.0);
			
			if (_vSquare < _stressThreshold )
			{
				break;
			}

			for(Agent agent: this._agents.getAllLocatedAgents())
			{
				if (agent.isAspect(STOCHASTIC_DIRECTION))
				{
					double[] move = (double[]) agent.get(STOCHASTIC_DIRECTION);
					_vSquare = Math.max(Vector.dotProduct(move,move), _vSquare);
				}
			}
			// time Leaping set the time step to match a max traveling distance
			// divined by 'maxMovement', for a 'fast' run.
			if ( this._timeLeap ) 
				this._dtMech = this._maxMovement / 
						( Math.sqrt( this._vSquare ) + 0.001 );

			// prevent to relaxing longer than the global _timeStepSize
			if ( this._dtMech > this._timeStepSize - this._tMech )
				this._dtMech = this._timeStepSize - this._tMech;

			for(Agent agent: this._agents.getAllLocatedAgents())
			{
				if (agent.isAspect(STOCHASTIC_STEP))
					agent.event(STOCHASTIC_MOVE, _dtMech);
			}

			// perform the step using (method)
			switch ( this._method )
			{
			case SHOVE :
			{
				for ( Agent agent: this._agents.getAllLocatedAgents() )
				{
					Body body = ((Body) agent.get(BODY));
					double radius = agent.getDouble(RADIUS);
					for ( Point point: body.getPoints() )
						point.shove(this._dtMech, radius);
				}
				/* Continue until nearly all overlap is resolved. */
				if ( this._vSquare < 0.001 )
					this._tMech = this._timeStepSize;
				break;
			}
			case EULER :
			{
				/// Euler's method
				for ( Agent agent: this._agents.getAllLocatedAgents() )
				{
					Body body = ((Body) agent.get(BODY));
					double radius = agent.getDouble(RADIUS);
					for ( Point point: body.getPoints() )
						point.euStep(this._dtMech, radius, null);
				}
				this._tMech += this._dtMech;
				break;
			}
				// NOTE : higher order ODE solvers don't like time Leaping.. be careful.
			case HEUN :
				/// Heun's method
				for(Agent agent: this._agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get(BODY)).getPoints())
						point.heun1(_dtMech, (double) agent.get(RADIUS));
				this.updateForces(this._agents);
				for(Agent agent: this._agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get(BODY)).getPoints())
						point.heun2(_dtMech, (double) agent.get(RADIUS));
				// Set time step
				_tMech += _dtMech;
				break;
			}

			for(Agent agent: this._agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get(BODY)).getPoints())
				{
					this._agents.getShape().applyBoundaries(point.getPosition());
				}
			nstep++;
		}
		this._agents.refreshSpatialRegistry();
		Log.out(Tier.DEBUG,
				"Relaxed "+this._agents.getNumAllAgents()+" agents after "+
						nstep+" iterations");
	}
}
