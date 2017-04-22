package processManager.library;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static lb.tools.FileIO.save;

import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import lb.D2Q9;
import lb.D2Q9Lattice;
import lb.collision.BounceBack;
import lb.collision.CollisionOperator;
import lb.collision.D2Q9RegularizedBoundary;
import lb.collision.LBGK;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Ball;
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
	
	public static int ox = 2;
	public static int oy = 10;
	public static int ry = 6;
	
	public static int XX = 40; // number of cells in x-direction
	public static int YY = 40; // number of cells in y-direction
	
//	public static final double U_MAX = 0.1e-6; // maximum velocity of Poiseuille inflow = 0.1 µm/s
//	public static final double RE = 100; // Reynolds number
//	public static final double NU = 1e-6; // 1e-6 m2/s at room temperature U_MAX * 2.0 * OBST_R / RE; // kinematic viscosity
//	public static final double OMEGA = 1.0 / ( 3.0 * NU + 0.5 ); // relaxation parameter
	
	public static final double LX = 0.05; // length of cavity [m]
	public static final double RE = 100; // Reynolds number
	public static final double NU = 0.000001; // kinematic viscosity water, 20 deg C [m^2/s]
	public static final double U_MAX = RE*NU/LX; // velocity in [m/s] is independent of resolution XX

	public static final double OMEGA = 1.0 / ( 3.0 * NU + 0.5 ); // relaxation parameter
	
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
	
	public static LBGK lbgk = null;
	
	public static D2Q9Lattice lattice = null;
	
	public static double latticeMultiplier = 2.0;
	
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
		
		// TODO super dirty for testintg

		slices = Vector.floor(Vector.times(agents.getShape().getDimensionLengths(),latticeMultiplier));
		XX = slices[0];
		YY = slices[1];
		
		this.lbgk = new LBGK(D2Q9.getInstance(), OMEGA);
		this.lattice = new D2Q9Lattice(slices[0], slices[1], lbgk);
		
		CollisionOperator northRegul = 
		D2Q9RegularizedBoundary.getNorthVelocityBoundary(new double[] {0,0}, OMEGA);
		CollisionOperator southRegul =
			D2Q9RegularizedBoundary.getSouthVelocityBoundary(new double[] {0,0}, OMEGA);
	
		lattice.addRectangularBoundary(1,XX,1,1,southRegul);
		lattice.addRectangularBoundary(1,XX,YY,YY,northRegul);
			
		initializeVelocity(lattice,lbgk);
		addObstacle(lattice);
	}
	
	public static void initializeVelocity(D2Q9Lattice lattice, LBGK lbgk) {
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
	
	public static double computePoiseuille(int y) {
		double realY = y-2;
		double realYY = YY-2;
		return 4 * U_MAX / ( realYY*realYY ) * ( realYY*realY - realY*realY );
	}
	
	public static void addObstacle(D2Q9Lattice lattice) {
		CollisionOperator bounceBack = new BounceBack(D2Q9.getInstance());
		for (int x=1; x<=XX; x++) {
			for (int y=1; y<=YY; y++) {
				if ( ( x-XX/ox )*( x-XX/ox ) + ( y-YY/oy )*( y-YY/oy ) <= (YY/ry)*(YY/ry) ) {
					lattice.setCollision(x,y,bounceBack);
				}
			}
		}
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
			
			Ball object = new Ball(	new double[] {
					((double) XX/ox) / (double) latticeMultiplier, 
					((double) YY/oy) / (double) latticeMultiplier }, 
					(YY/ry) / (double) latticeMultiplier);
			
			LinkedList<Surface> statics = new LinkedList<Surface>();
			statics.addAll(this._shapeSurfs);
			statics.add(object);
			
			this._iterator.collision(statics, agentSurfs, 0.0, agent, null);
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " Finished updating agent forces");
	}
	
	public static void setVelocity(D2Q9Lattice lattice, LBGK lbgk, int x, int y, double[] u) {
		double rho = 1;
				double normU = u[0]*u[0];
				for (int i=0; i<9; i++) {
					lattice.setF( x, y, lbgk.fEq( i,rho,u,normU ), i );
		}	
	}

	public static void setSmoothVelocity(D2Q9Lattice lattice, LBGK lbgk, int xp, int yp, double a, double b, int size) 
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
		int rLat	= 1;
		int uLat	= rLat;
		_tMech		= 0.0;
		_dtMech 	= this._dtBase; // start with initial base timestep than adjust

		// Mechanical relaxation
		while(_tMech < _timeStepSize) 
		{	
			if (uLat == rLat)
			{
//				setSmoothVelocity(lattice,lbgk,slices[0]/2,slices[1]/2,U_MAX,U_MAX,3);
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
					/* Note unit conversion with lattice vector field */
					point.euStep(this._dtMech, radius, Vector.times(lattice.getU(posi[0], posi[1]),6e7)); // convert to µm/min
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
		save("LBDEM/dump_" + this._timeForNextStep,lattice);
		Log.out(Tier.DEBUG,
				"Relaxed "+this._agents.getNumAllAgents()+" agents after "+
						nstep+" iterations");
	}
}
