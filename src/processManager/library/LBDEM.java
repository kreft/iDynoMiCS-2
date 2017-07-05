package processManager.library;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import dataIO.FileHandler;
import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static lb.tools.FileIO.save;

import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import lb.D2Q9;
import lb.D2Q9Lattice;
import lb.collision.CollisionOperator;
import lb.collision.D2Q9RegularizedBoundary;
import lb.collision.LBGK;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
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
public class LBDEM extends ProcessManager
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
	
	public String FLOW_PROFILE = "flowProfile";

	private boolean _debugPlots = false;
	
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
	
	/*************************************************************************
	 * LB settings
	 ************************************************************************/
	
	public int XX; // number of cells in x-direction
	public int YY; // number of cells in y-direction
		
	public double LX; // typical length [m]
	public double RE; // Reynolds number
	public double NU; // kinematic viscosity water, 20 deg C [m^2/s]
	public double U_scale;
	public double U_MAX; // velocity in [m/s] is independent of resolution XX
	public double max_relative_stochastic_velocity;
	public double extend_base_step;

	public double OMEGA; // relaxation parameter
	
	/* 
	 * Lattice units conversion (wiki)
	 * 
	 * \delta_x is the basic unit for lattice spacing, so if the domain of
	 * length L has N lattice units along its entire length, the space unit is
	 * simply defined as \delta_x = L/N. Speeds in the Lattice Botltzmann
	 * simulations are typically given in terms of the speed of sound. The
	 * discrete time unit can therefore be fiven as \delta_t = \delta_x / C_s,
	 * where the denominator C_s is the physical speed of sound (340.29 m/s).
	 * 
	 * For small-scale flows (such as those seen in porous media mechanics), 
	 * operating with the true speed of sound can lead to unacceptably short 
	 * time steps. It is therefore common to raise the lattice Mach number to 
	 * something much larger than the real Mach number, and compensating for 
	 * this by raising the viscosity as well in order to preserve the Reynolds 
	 * number.
	 */
	
	public LBGK lbgk = null;
	
	public D2Q9Lattice lattice = null;
	
	/*
	 * amount of distance units per lattice
	 */
	public double latticeMultiplier = 1;
	
	/*
	 * velocity drivers for stochastic flow profile. 
	 */
	private LinkedList<Driver> _drivers = new LinkedList<Driver>();
	
	private class Driver
	{
		int x, y, c;
		double[] u;
		
		public Driver(int x, int y, int c, double[] u)
		{
			this.x = x;
			this.y = y;
			this.c = c;
			this.u = u;
		} 
	}
		
	
	private int[] slices;
	
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
		/* in water [ µm / min ] */
		double speedOfSound = Helper.setIfNone( this.getDouble("speedOfSound"), 
				89040000000.0 ); 	
		this.max_relative_stochastic_velocity = Helper.setIfNone( 
				this.getDouble("maxRelStoVelocity"), 0.001 );	
		this.extend_base_step = Helper.setIfNone( 
				this.getDouble("extendBaseStep"), 100.0 );
		
		/* note Clb / Cp becomes 6.4841674e-12 min / µm */
		this._dtBase = Helper.setIfNone( this.getDouble(BASE_DT), 
				(latticeMultiplier/speedOfSound) * extend_base_step * (1 / Math.sqrt(3)) );
		
		this._maxMovement = Helper.setIfNone( this.getDouble(MAX_MOVEMENT), 0.01 );	
		this._method = Method.valueOf( Helper.setIfNone(
				this.getString(RELAXATION_METHOD), Method.EULER.toString() ) );
		this._timeLeap	= true;
		
		this._shape = agents.getShape();
		this._shapeSurfs  = this._shape.getSurfaces();
		this._iterator = this._shape.getCollision();
		this._stressThreshold = Helper.setIfNone( 
				this.getDouble( LOW_STRESS_SKIP ), 0.0 );	
		
		/*
		 * Lattice Boltzmann settings
		 */
		this.LX = Helper.setIfNone( this.getDouble("lengthScale"), 1e-4 );	
		this.RE = Helper.setIfNone( this.getDouble("reynolds"), 100.0 );	
		this.NU = Helper.setIfNone( this.getDouble("kinematicViscosity"), 1e-6);	
		this.U_scale = Helper.setIfNone( this.getDouble("velocityScale"), 
				latticeMultiplier / _dtBase );	
		
		this.U_MAX = (1 / U_scale) * RE*NU/LX;
		this.OMEGA = 1.0 / ( 3.0 * NU + 0.5 );
		
		/*
		 * setup D2Q9 Lattice boltamann Bhatnagar–Gross–Krook
		 */
		slices = Vector.floor( Vector.times(
				agents.getShape().getDimensionLengths(), 1/latticeMultiplier ));
		this.XX = slices[0];
		this.YY = slices[1];
		
		this.lbgk = new LBGK(D2Q9.getInstance(), OMEGA);
		this.lattice = new D2Q9Lattice(slices[0], slices[1], lbgk);
		
		/*
		 * setup flow field based on selected scenario, use pipe flow if no
		 * scenario is specified
		 */
		
		if ( Helper.isNone( this.getString( FLOW_PROFILE ) ) ||  
				this.getString( FLOW_PROFILE ) == "pipeFlow" )
		{
			/* pipe flow profile */
			CollisionOperator northRegul = 
					D2Q9RegularizedBoundary.getNorthVelocityBoundary(
					new double[] { 0, 0 }, OMEGA );
			CollisionOperator southRegul =
					D2Q9RegularizedBoundary.getSouthVelocityBoundary(
					new double[] { 0, 0 }, OMEGA );
		
			lattice.addRectangularBoundary(1,XX,1,1,southRegul);
			lattice.addRectangularBoundary(1,XX,YY,YY,northRegul);
				
			initializeVelocity(lattice,lbgk);
		}
		else
		{
			/* random profile */
			this.set( FLOW_PROFILE , "randomFlow");
//			initializeVelocityb(lattice,lbgk);
		}
		
		/* LB output folder */
		if ( _debugPlots )
		{
			FileHandler fileio = new FileHandler();
			fileio.fnew(Idynomics.global.outputLocation + "LBDEM/out");
		}
	}
	
	public void initializeVelocity(D2Q9Lattice lattice, LBGK lbgk) {
		double rho = 1;
		for (int x=1; x<=XX; x++) {
			for (int y=1; y<=YY; y++) {
				double u[] = {computePoiseuille(y), 0};
				double normU = u[0]*u[0];
				for (int i=0; i<9; i++) {
					lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
				}
			}
		}	
	}
	
	public void initializeVelocityb(D2Q9Lattice lattice, LBGK lbgk) {
		double rho = 1;
		for (int x=1; x<=XX; x++) {
			for (int y=1; y<=YY; y++) {
				double u[] = {U_MAX, 0};
				double normU = u[0]*u[0];
				for (int i=0; i<9; i++) {
					lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
				}
			}
		}	
	}
	
	public double computePoiseuille(int y) {
		double realY = y-2;
		double realYY = YY-2;
		return 4 * U_MAX / ( realYY*realYY ) * ( realYY*realY - realY*realY );
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
					double stiffness = Helper.setIfNone(
							agent.getDouble( STIFFNESS ), 10.0 );

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
			
			double searchDist = ( agent.isAspect( SEARCH_DIST ) ?
					agent.getDouble( SEARCH_DIST ) : 0.0 );
			if ( Log.shouldWrite( level ) )
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
			
			LinkedList<Surface> statics = new LinkedList<Surface>();
			statics.addAll(this._shapeSurfs);

			this._iterator.collision(statics, agentSurfs, 0.0, agent, null);
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " Finished updating agent forces");
	}
	
	public void setVelocity(D2Q9Lattice lattice, LBGK lbgk, int x, int y, double[] u) {
		double rho = 1;
				double normU = u[0]*u[0];
				for (int i=0; i<9; i++) {
					lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
		}	
	}

	public void setSmoothVelocity(D2Q9Lattice lattice, LBGK lbgk, int xp, int yp, double a, double b, int size) 
	{
		for ( int x = -size; x < size; x++ )
		{
			for ( int y = -size; y < size; y++)
			{
				setVelocity(lattice,lbgk,x+xp,y+yp, new double[] { 
						( size-Math.abs(x) ) * a, 
						( size-Math.abs(y) ) * b});
			}
		}
	}

	@Override
	protected void internalStep()
	{

		int nstep	= 0;
		/* recycle current lattice rLat times state to reduce computational demand */
		int rLat	= 1; 
		int uLat	= rLat;
		_tMech		= 0.0;
		_dtMech 	= this._dtBase; // start with initial base timestep than adjust

		// Mechanical relaxation
		while(_tMech < _timeStepSize) 
		{	
			if (uLat == rLat)
			{
				if ( Helper.isNone( this.getString( FLOW_PROFILE ) ) ||  
						this.getString( FLOW_PROFILE ) == "pipeFlow" )
				{
					/* handled by LB border implementation  */
				}
				else if ( Helper.isNone( this.getString( FLOW_PROFILE ) ) ||  
						this.getString( FLOW_PROFILE ) == "randomFlow" )
				{
					if (this._drivers.size() < 1 )
					{
						/* generating random direction vector which does not
						 * exceed U_MAX. Note work with vector length + angle 
						 * to prevent diagonal movement artifacts. NOTE we
						 * could also incorperate this in the vector class.
						 */
						double a = ExtraMath.getUniRandDbl();
						double b = ExtraMath.getUniRandDbl();
						/* NOTE: we should avoid getting fluid speeds higher
						 * than U_MAX, we multiply by 0.1 for safety (prevent
						 * LB blowing up 6.28318530718*/
						double[] vec = Vector.uncylindrify( new double[]{
								a * this.U_MAX * max_relative_stochastic_velocity, b * 6.28318530718 } );
						/* Create and save Driver object */
						Driver p = new Driver( 
								Vector.randomInts( 1, 0, this.XX )[0], 
								Vector.randomInts( 1, 0, this.YY )[0],
								50, vec );
						this._drivers.add(p);
//						System.out.println(Vector.toString(vec)+" "+a+" "+b);
					}
					for ( Driver s : this._drivers )
					{
						double[] current = lattice.getU(s.x, s.y);
						double[] vnew = Vector.add(current, s.u);
						
						if (Vector.normEuclid(vnew) < max_relative_stochastic_velocity * this.U_MAX)
							setVelocity(lattice, lbgk, s.x, s.y, 
									Vector.add(current, s.u) );
						else
							setVelocity(lattice, lbgk, s.x, s.y, 
									Vector.normaliseEuclid(current, max_relative_stochastic_velocity *U_MAX) );
						s.c -= 1;
					}
					this._drivers.removeIf( p-> p.c == 0 );
				}
				else
				{
					/* FIXME some testing code delete in final version */
					setSmoothVelocity(lattice,lbgk,slices[0]/2,slices[1]/2,
							U_MAX,U_MAX,3);
				}
				lattice.step();
				uLat = 0;
			}
			uLat++;
			
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

			// prevent to relaxing longer than the global _timeStepSize
			if ( this._dtMech > this._timeStepSize - this._tMech )
				this._dtMech = this._timeStepSize - this._tMech;

			for(Agent agent: this._agents.getAllLocatedAgents())
			{
				if (agent.isAspect(STOCHASTIC_STEP))
					agent.event(STOCHASTIC_MOVE, _dtMech);
			}

			/// Euler's method
			for ( Agent agent: this._agents.getAllLocatedAgents() )
			{
				Body body = ((Body) agent.get(BODY));
				double radius = agent.getDouble(RADIUS);
				for ( Point point: body.getPoints() )
				{
					int[] posi = Vector.floor(Vector.times(point.getPosition(),latticeMultiplier));
					/* Note unit conversion with lattice vector field (SI)
					 *  converted to µm/min (agents) */
					point.euStep(this._dtMech, radius, Vector.times(
							lattice.getU(posi[0], posi[1]), latticeMultiplier/_dtBase ) );
				}
			}
			this._tMech += this._dtMech;

	
			for(Agent agent: this._agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get(BODY)).getPoints())
				{
					this._agents.getShape().applyBoundaries(point.getPosition());
				}
			nstep++;
		}
		this._agents.refreshSpatialRegistry();
		if ( _debugPlots )
			save( Idynomics.global.outputLocation + "LBDEM/dump_" + 
					this._timeForNextStep, lattice );
		Log.out( Tier.DEBUG, "Relaxed " + this._agents.getNumAllAgents() +
				" agents after " + nstep + " iterations");
	}
}
