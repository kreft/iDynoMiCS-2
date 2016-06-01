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
import idynomics.NameRef;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import surface.Collision;
import surface.Point;
import surface.Rod;
import surface.Surface;
import utility.ExtraMath;
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
	private double _dtBase;	
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
	Collision _iterator;
	
	public String BODY = NameRef.agentBody;
	public String RADIUS = NameRef.bodyRadius;
	public String VOLUME = NameRef.agentVolume;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
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
		Collection<Surface> shapeSurfs = agents.getShape().getSurfaces();
		/* Calculate forces. */
		for ( Agent agent: agents.getAllLocatedAgents() ) 
		{
			Body body = (Body) agent.get(NameRef.agentBody);
			List<Surface> agentSurfs = body.getSurfaces();
			// NOTE: currently missing internal springs for rod cells.
			
			for ( Surface s : agentSurfs )
			{
				if ( s instanceof Rod )
				{
					double l = 0.0;
					
					// TODO cleanup
					if ( body.getJoints().size() > 1 )
					{
						double r = agent.getDouble(RADIUS);
						double v = agent.getDouble(VOLUME) - 
								ExtraMath.volumeOfASphere( r );
						l = ExtraMath.lengthOfACylinder( v, r );
					}
					
					Point a 		= ((Rod) s)._points[0];
					Point b 		= ((Rod) s)._points[1];
					double[] diff 	= Vector.minus( a.getPosition() , 
							b.getPosition() );
					double dn 		= Vector.normEuclid(diff);
					
					// 0.1 is spine stiffness, TODO implement properly
					double f 		= 20.0 * ( dn - l );
					double[] fV		= Vector.times(diff, f);
				
					Vector.addEquals( b.getForce(), fV ) ;
					Vector.addEquals( a.getForce(), Vector.reverse( fV ) ) ;
				}
			}
			
			double searchDist = (agent.isAspect("searchDist") ?
					agent.getDouble("searchDist") : 0.0);
			
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
					agent.event("evaluatePull", neighbour);
					Double pull = agent.getDouble("#curPullDist");
					if ( pull == null || pull.isNaN() )
						pull = 0.0;
					body = ((Body) neighbour.get("body"));
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


	protected void internalStep(EnvironmentContainer environment,
			AgentContainer agents)
	{
		/*
		 * Obtaining relaxation parameters.
		 */
		this._dtBase = Helper.setIfNone( getDouble("dtBase"), 0.002 );	
		this._maxMovement = Helper.setIfNone( getDouble("maxMovement"), 0.01 );	
		this._method = Method.valueOf( Helper.setIfNone(
				getString("relaxationMethod"), Method.EULER.toString() ) );
		this._timeLeap	= true;
		
		this._iterator = new Collision(null, agents.getShape());
		/**
		 * Update agent body now required
		 */
		for(Agent agent: agents.getAllLocatedAgents()) 
		{
			agent.event(NameRef.bodyUpdate);
			agent.event("divide");
			agent.event("epsExcretion");
		}

		int nstep	= 0;
		_tMech		= 0.0;
		_dtMech 		= 0.0005; // TODO (initial) time step.. needs to be build out of protocol file

		// if higher order ODE solvers are used we need additional space to write.
		switch (_method)
		{
		case HEUN :
			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get("body")).getPoints())
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
				for (Point point: ((Body) agent.get("body")).getPoints())
					if ( Vector.normSquare(point.dxdt((double) agent.get("radius"))) > _vSquare )
						_vSquare = Vector.normSquare(point.dxdt((double) agent.get("radius")));			
			}

			// FIXME this assumes linear force scaling improve..
			_vSquare = _vSquare * Math.pow(_iterator.getMaxForceScalar(), 2.0);

			for(Agent agent: agents.getAllLocatedAgents())
			{
				if (agent.isAspect("stochasticDirection"))
				{
					double[] move = (double[]) agent.get("stochasticDirection");
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
				if (agent.isAspect("stochasticStep"))
					agent.event("stochasticMove", _dtMech);
			}

			// perform the step using (method)
			switch ( this._method )
			{
			case SHOVE :
			{
				for ( Agent agent: agents.getAllLocatedAgents() )
				{
					Body body = ((Body) agent.get("body"));
					double radius = agent.getDouble(NameRef.bodyRadius);
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
					Body body = ((Body) agent.get("body"));
					double radius = agent.getDouble(NameRef.bodyRadius);
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
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.heun1(_dtMech, (double) agent.get("radius"));
				this.updateForces(agents);
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.heun2(_dtMech, (double) agent.get("radius"));
				// Set time step
				_tMech += _dtMech;
				break;
			}

			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get("body")).getPoints())
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
