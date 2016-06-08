package processManager.library;

import java.util.List;
import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
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
	public static String SEARCH_DIST = AspectRef.collisionSearchDistance;
	public static String PULL_EVALUATION = AspectRef.collisionPullEvaluation;
	public static String CURRENT_PULL_DISTANCE = AspectRef.collisionCurrentPullDistance;
	public static String RELAXATION_METHOD = AspectRef.collisionRelaxationMethod;
	
	public static String BASE_DT = AspectRef.collisionBaseDT;
	public static String MAX_MOVEMENT = AspectRef.collisionMaxMOvement;
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;
	public String DIVIDE = AspectRef.agentDivision;
	
	public String UPDATE_BODY = AspectRef.bodyUpdate;
	public String EXCRETE_EPS = AspectRef.agentExcreteEps;
	
	public String STOCHASTIC_DIRECTION = AspectRef.agentStochasticDirection;
	public String STOCHASTIC_STEP = AspectRef.agentStochasticStep;
	public String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	
	
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
		HEUN
	}

	// FIXME work in progress
	// set Mechanical stepper
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

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, Compartment compartment)
	{
		super.init(xmlElem, compartment);
		
		/*
		 * Obtaining relaxation parameters.
		 */
		this._dtBase = Helper.setIfNone( getDouble(BASE_DT), 0.0003 );	
		this._maxMovement = Helper.setIfNone( getDouble(MAX_MOVEMENT), 0.01 );	
		this._method = Method.valueOf( Helper.setIfNone(
				getString(RELAXATION_METHOD), Method.EULER.toString() ) );
		this._timeLeap	= true;
		
		this._shape = compartment.getShape();
		this._iterator = new Collision(null, _shape);
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
		Log.out(level, "Updating agent forces");
		/*
		 * Updated bodies will required an updated spatial registry.
		 */
		agents.refreshSpatialRegistry();
		// TODO Move this into internalStep() and make shapeSurfs a class variable?
		Collection<Surface> shapeSurfs = _shape.getSurfaces();
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

					/*
					 * calculate current length of spine spring
					 */
					Point a 		= ((Rod) s)._points[0];
					Point b 		= ((Rod) s)._points[1];
					double[] diff 	= _shape.getMinDifference(a.getPosition() , 
							b.getPosition() );
					double dn 		= Vector.normEuclid(diff);
					if (dn > 2.0 )
						System.out.println(dn);
					
					/*
					 * Hooke's law: spring stiffness * displacement
					 * TODO implement stiffness properly in xml
					 */
					double f 		= 20.0 * ( dn - l );
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
			
			Log.out(level, "  Agent (ID "+agent.identity()+") has "+
					agentSurfs.size()+" surfaces, search dist "+searchDist);
			/*
			 * Perform neighborhood search and perform collision detection and
			 * response. 
			 */
			Collection<Agent> nhbs = agents.treeSearch(agent, searchDist);
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
					Log.out(level, "   interacting with neighbor (ID "+
							neighbour.identity()+") , which has "+t.size()+
							" surfaces, with pull distance "+pull);
					this._iterator.collision(agentSurfs, t, pull);
				}
			/*
			 * Boundary collisions
			 */
			this._iterator.collision(shapeSurfs, agentSurfs, 0.0);
		}
		Log.out(level, " Finished updating agent forces");
	}

	/**
	 * 
	 */
	protected void internalStep(EnvironmentContainer environment,
			AgentContainer agents)
	{
		/**
		 * Update agent body now required
		 */
		for(Agent agent: agents.getAllLocatedAgents()) 
		{
			agent.event(UPDATE_BODY);
			agent.event(DIVIDE);
			agent.event(EXCRETE_EPS); //FIXME probably not the best place to make this call
		}

		int nstep	= 0;
		_tMech		= 0.0;
		_dtMech 	= this._dtBase; // start with initial base timestep than adjust

		// if higher order ODE solvers are used we need additional space to write.
		switch (_method)
		{
		case HEUN :
			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get(BODY)).getPoints())
					point.initialiseC(2);
			break;
		default:
			break;
		}

		// Mechanical relaxation
		while(_tMech < _timeStepSize) 
		{	
			this.updateForces(agents);

			/// obtain current highest particle velocity
			_vSquare = 0.0;
			for(Agent agent: agents.getAllLocatedAgents())
			{
				for (Point point: ((Body) agent.get(BODY)).getPoints())
					if ( Vector.normSquare(point.dxdt((double) agent.get(RADIUS))) > _vSquare )
						_vSquare = Vector.normSquare(point.dxdt((double) agent.get(RADIUS)));			
			}

			// FIXME this assumes linear force scaling improve..
			_vSquare = _vSquare * Math.pow(_iterator.getMaxForceScalar(), 2.0);

			for(Agent agent: agents.getAllLocatedAgents())
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
				this._dtMech = this._maxMovement / (Math.sqrt(this._vSquare)+0.001);

			// prevent to relaxing longer than the global _timeStepSize
			if ( this._dtMech > this._timeStepSize - this._tMech )
				this._dtMech = this._timeStepSize - this._tMech;

			for(Agent agent: agents.getAllLocatedAgents())
			{
				if (agent.isAspect(STOCHASTIC_STEP))
					agent.event(STOCHASTIC_MOVE, _dtMech);
			}

			// perform the step using (method)
			switch ( this._method )
			{
			case SHOVE :
			{
				for ( Agent agent: agents.getAllLocatedAgents() )
				{
					Body body = ((Body) agent.get(BODY));
					double radius = agent.getDouble(RADIUS);
					for ( Point point: body.getPoints() )
						point.shove(this._dtMech, radius);
				}
				/* Continue until all overlap is resolved. */
				if ( this._vSquare == 0.0 )
					this._tMech = this._timeStepSize;
				break;
			}
			case EULER :
			{
				/// Euler's method
				for ( Agent agent: agents.getAllLocatedAgents() )
				{
					Body body = ((Body) agent.get(BODY));
					double radius = agent.getDouble(RADIUS);
					for ( Point point: body.getPoints() )
						point.euStep(this._dtMech, radius);
				}
				this._tMech += this._dtMech;
				break;
			}
				// NOTE : higher order ODE solvers don't like time Leaping.. be careful.
			case HEUN :
				/// Heun's method
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get(BODY)).getPoints())
						point.heun1(_dtMech, (double) agent.get(RADIUS));
				this.updateForces(agents);
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get(BODY)).getPoints())
						point.heun2(_dtMech, (double) agent.get(RADIUS));
				// Set time step
				_tMech += _dtMech;
				break;
			}

			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get(BODY)).getPoints())
				{
					agents.getShape().applyBoundaries(point.getPosition());
				}
			nstep++;
		}
		Log.out(Tier.DEBUG,
				"Relaxed "+agents.getNumAllAgents()+" agents after "+
						nstep+" iterations");
	}
}
